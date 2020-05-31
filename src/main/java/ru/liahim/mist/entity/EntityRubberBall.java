package ru.liahim.mist.entity;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import ru.liahim.mist.api.block.IRubberBallCollideble;
import ru.liahim.mist.api.item.MistItems;

public class EntityRubberBall extends EntityThrowable {

	private int lastBounce;
	private float size = 0.3125F;

	public EntityRubberBall(World world) {
		super(world);
		this.setSize(size, size);
	}

	public EntityRubberBall(World world, double x, double y, double z) {
		super(world, x, y, z);
		this.setSize(size, size);
	}

	public EntityRubberBall(World world, EntityLivingBase entity) {
		super(world, entity);
		this.setSize(size, size);
	}

	protected ItemStack getItemStack() {
		return new ItemStack(MistItems.RUBBER);
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (result.typeOfHit == RayTraceResult.Type.ENTITY) {
			if (result.entityHit instanceof EntityPlayer && ((EntityPlayer)result.entityHit).isSneaking()) {
				world.setEntityState(this, (byte) 3);
				this.setDead();
				world.spawnEntity(new EntityItem(world, posX, posY, posZ, this.getItemStack()));
			} else {
				double damage = this.getMotion().lengthSquared();
				if (damage > 0.1) result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), (float) (damage / 2));
				this.motionX = -this.motionX * (rand.nextDouble() * 0.5 + 0.5);
				this.motionY = this.motionY * (rand.nextDouble() * 0.5 + 0.5);
				this.motionZ = -this.motionZ * (rand.nextDouble() * 0.5 + 0.5);
			}
		} else if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
			BlockPos pos = result.getBlockPos();
			IBlockState state = this.world.getBlockState(pos);
			if (!(state.getBlock() instanceof IRubberBallCollideble) || ((IRubberBallCollideble) state.getBlock()).isCollide(this.world, state, this, result, this.rand)) {
				this.world.playSound(null, pos, state.getBlock().getSoundType(state, this.world, pos, this).getStepSound(), SoundCategory.BLOCKS, 1.0F, 1.5F);
				doBounce(new Vec3d(result.sideHit.getDirectionVec()), state.getMaterial());
			}
		}
		this.lastBounce = 0;
	}

	private void doBounce(Vec3d norm, Material mat) {
		Vec3d motion = getMotion();
		double velAlongNorm = norm.dotProduct(motion);
		Vec3d newMotion = motion.add(norm.scale(velAlongNorm * -bounciness(1)));
		this.setMotion(newMotion.scale(getMaterialMultipler(mat)));
	}

	private double bounciness(double add) {
		return add + rand.nextDouble() * 0.1 + 0.7;
	}

	private double getMaterialMultipler(Material mat) {
		double i = 1;
		if (mat != null) {
			if (mat == Material.GROUND) i = 0.9;
			else if (mat == Material.GRASS) i = 0.9;
			else if (mat == Material.PLANTS) i = 0.8;
			else if (mat == Material.SAND || mat == Material.SNOW) i = 0.5;
		}
		return i;
	}

	public Vec3d getMotion() {
		return new Vec3d(this.motionX, this.motionY, this.motionZ);
	}

	private void setMotion(Vec3d vec) {
		this.motionX = vec.x;
		this.motionY = vec.y;
		this.motionZ = vec.z;
	}

	@Override
	protected float getGravityVelocity() {
		return 0.1F;
	}

	@Override
	public void onUpdate() {
		this.lastTickPosX = this.posX;
		this.lastTickPosY = this.posY;
		this.lastTickPosZ = this.posZ;
		this.onEntityUpdate();
		if (!world.isRemote) {
			this.lastBounce++;
			if (!world.isRemote && this.lastBounce < 5 && getMotion().lengthSquared() < 0.1F) {
				world.setEntityState(this, (byte) 3);
				this.setDead();
				world.spawnEntity(new EntityItem(world, posX, posY, posZ, this.getItemStack()));
				return;
			}

			Vec3d vec3d = new Vec3d(this.posX, this.posY, this.posZ);
			Vec3d vec3d1 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			RayTraceResult raytrace = this.world.rayTraceBlocks(vec3d, vec3d1, true, true, false);
			vec3d = new Vec3d(this.posX, this.posY, this.posZ);
			vec3d1 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

			if (raytrace != null) {
				vec3d1 = new Vec3d(raytrace.hitVec.x, raytrace.hitVec.y, raytrace.hitVec.z);
			}

			Entity entity = null;
			List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0D));
			double d0 = 0.0D;
			boolean flag = false;

			for (int i = 0; i < list.size(); ++i) {
				Entity entity1 = list.get(i);
				if (entity1.canBeCollidedWith()) {
					if (entity1 == this.ignoreEntity) flag = true;
					else if (this.thrower != null && this.ticksExisted < 2 && this.ignoreEntity == null) {
						this.ignoreEntity = entity1;
						flag = true;
					} else {
						flag = false;
						AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow(0.3D);
						RayTraceResult raytraceresult1 = axisalignedbb.calculateIntercept(vec3d, vec3d1);
						if (raytraceresult1 != null) {
							double d1 = vec3d.squareDistanceTo(raytraceresult1.hitVec);
							if (d1 < d0 || d0 == 0.0D) {
								entity = entity1;
								d0 = d1;
							}
						}
					}
				}
			}

			if (this.ignoreEntity != null) {
				if (flag) this.ignoreTime = 2;
				else if (this.ignoreTime-- <= 0) this.ignoreEntity = null;
			}

			if (entity != null) raytrace = new RayTraceResult(entity);

			if (raytrace != null) {
				if (raytrace.typeOfHit == RayTraceResult.Type.BLOCK && this.world.getBlockState(raytrace.getBlockPos()).getBlock() == Blocks.PORTAL) {
					this.setPortal(raytrace.getBlockPos());
				} else if (!ForgeEventFactory.onProjectileImpact(this, raytrace)) this.onImpact(raytrace);
			}
		}

		this.posX += this.motionX;
		this.posY += this.motionY;
		this.posZ += this.motionZ;
		float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
		this.rotationYaw = (float) (MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));

		for (this.rotationPitch = (float) (MathHelper.atan2(this.motionY, f) * (180D / Math.PI)); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
			;
		}

		while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
			this.prevRotationPitch += 360.0F;
		}

		while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
			this.prevRotationYaw -= 360.0F;
		}

		while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
			this.prevRotationYaw += 360.0F;
		}

		this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
		this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
		float f1 = this.isInWater() ? 0.8F : 0.99F;
		float f2 = this.getGravityVelocity();

		this.motionX *= f1;
		this.motionY *= f1;
		this.motionZ *= f1;

		if (!this.hasNoGravity()) {
			this.motionY -= f2;
		}

		this.setPosition(this.posX, this.posY, this.posZ);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setInteger("LastBounce", this.lastBounce);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		this.lastBounce = compound.getInteger("LastBounce");
	}
}
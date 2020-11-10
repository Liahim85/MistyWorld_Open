package ru.liahim.mist.entity;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import ru.liahim.mist.api.entity.IMatWalkable;
import ru.liahim.mist.capability.handler.ISkillCapaHandler;
import ru.liahim.mist.capability.handler.ISkillCapaHandler.Skill;
import ru.liahim.mist.entity.ai.EntityAITemptTamed;
import ru.liahim.mist.entity.ai.PathNavigateGroundMistUpper;
import ru.liahim.mist.entity.ai.PathNavigateGroundMistUpperSwamp;
import ru.liahim.mist.init.ModAdvancements;

public abstract class EntityAnimalMist extends EntityTameable {
	
	protected long revengeTimer;
	protected EntityAITemptTamed aiTempt;
	protected static final int temptationStackDropChance = 4;

	public EntityAnimalMist(World world) {
		super(world);
		this.stepHeight = 1.0F;
	}

	public void setRevengeTime(long time) {
		this.revengeTimer = time;
	}

	protected <T extends EntityAnimalMist> void closePanic(Class <T> clazz, EntityPlayer sourse, long time) {
		List<T> list = this.world.<T>getEntitiesWithinAABB(clazz, this.getEntityBoundingBox().grow(16.0D, 3.0D, 16.0D));
		for (T entity : list) {
			if (entity.getEntitySenses().canSee(sourse)) entity.setRevengeTime(time);
		}
	}

	@Override
	protected void updateAITasks() {
		if (this.revengeTimer > 0) --this.revengeTimer;
		super.updateAITasks();
	}

	@Override
	public void onDeath(DamageSource cause) {
		super.onDeath(cause);
		Entity entity = cause.getTrueSource();
		if (entity instanceof EntityPlayer && !((EntityPlayer)entity).capabilities.isCreativeMode) ISkillCapaHandler.getHandler((EntityPlayer)entity).addSkill(Skill.CUTTING, getSkillPoint());
	}

	protected int getSkillPoint() {
		return 1;
	}

	protected void dropWool(DamageSource cause, int meta) {
		int i = this.rand.nextInt(2);
		if (cause.getTrueSource() instanceof EntityPlayer) {
			int s = Skill.getLevel((EntityPlayer) cause.getTrueSource(), Skill.CUTTING);
			i += Math.min(this.rand.nextInt(s), 2);
		}
		if (i > 0) this.entityDropItem(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), i, meta), 0);
	}

	@Override
	public boolean isOnSameTeam(Entity entity) {
		if (this.isTamed() && entity instanceof EntityPlayer) return this.revengeTimer == 0;
		return super.isOnSameTeam(entity);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setLong("RevengeTime", this.revengeTimer);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		this.setRevengeTime(compound.getLong("RevengeTime"));
	}

	@Override
	@Nullable
	public EntityAgeable createChild(EntityAgeable ageable) {
		if (!(ageable instanceof EntityTameable)) return null;
		EntityTameable child = getChild();
		child.setTamed(this.isTamed());
		child.setOwnerId(this.getOwnerId());
		child.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue() * 0.5D);
		return child;
	}

	@Override
	public void setGrowingAge(int age) {
		if (age >= 0 && this.isChild()) {
			this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue() * 2D);
		}
		super.setGrowingAge(age);
	}

	protected abstract EntityTameable getChild();

	public boolean canBeTempted() { return this.revengeTimer == 0; }
	public boolean canBeTemptedByEntity(EntityPlayer player) {
	 	return getTameLevel() - Skill.getLevel(player, Skill.TAMING) <= 1;
	}
	public boolean isTamedByEntity(EntityPlayer player) { return this.isTamed(); }
	public boolean childCheck() { return true; }

	protected boolean tamedProcess(EntityPlayer player, ItemStack stack) {
		if ((this.aiTempt == null || this.aiTempt.isRunning()) && this.isBreedingItem(stack) && player.getDistanceSq(this) < 9.0D) {
			if (!this.world.isRemote) {
				if (!player.capabilities.isCreativeMode) stack.shrink(1);
				ISkillCapaHandler capa = ISkillCapaHandler.getHandler(player);
				int tameLevel = Skill.TAMING.getLevel(capa.getSkill(Skill.TAMING));
				if (tameLevel >= this.getTameLevel() && this.rand.nextInt(5) == 0 && !ForgeEventFactory.onAnimalTame(this, player)) {
					this.setTamedBy(player);
					this.setRevengeTarget(null);
					this.setAttackTarget(null);
					capa.addSkill(Skill.TAMING, this.getTameLevel() * 5);
					this.playTameEffect(true);
					this.world.setEntityState(this, (byte)7);
					if (player instanceof EntityPlayerMP) ModAdvancements.TAME_ANIMAL.trigger((EntityPlayerMP)player, this);
				} else {
					capa.addSkill(Skill.TAMING, Math.min(tameLevel, this.getTameLevel()));
					this.playTameEffect(false);
					this.world.setEntityState(this, (byte)6);
				}
			}
			return true;
		}
		return false;
	}

	protected void startRiding(EntityPlayer player) {
		 if (!this.world.isRemote && player.startRiding(this) && player instanceof EntityPlayerMP) {
			 ModAdvancements.RIDING_ANIMAL.trigger((EntityPlayerMP)player, this);
		 }
	}

	@Override
	protected PathNavigate createNavigator(World world) {
		return this instanceof IMatWalkable ? new PathNavigateGroundMistUpper(this, world) : new PathNavigateGroundMistUpperSwamp(this, world);
	}

	public abstract int getTameLevel();

	public boolean isDriven() {	return false; }
	public boolean boost() { return false; }

	public static boolean isDriven(Entity entity) {
		return entity instanceof EntityAnimalMist && ((EntityAnimalMist)entity).isDriven();
	}
}
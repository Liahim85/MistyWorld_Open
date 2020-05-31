package ru.liahim.mist.client.particle;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.common.Mist;

public class ParticleAcidRain extends Particle {
	
	int index;

	public ParticleAcidRain(World world, double xCoord, double yCoord, double zCoord) {
		super(world, xCoord, yCoord, zCoord, 0.0D, 0.0D, 0.0D);
        this.motionX *= 0.3D;
        this.motionY = Math.random() * 0.2D + 0.1D;
        this.motionZ *= 0.3D;
        this.particleRed = 1.0F;
        this.particleGreen = 1.0F;
        this.particleBlue = 1.0F;
        this.setParticleTexture(Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(new ResourceLocation(Mist.MODID, "particle/rain").toString()));
        this.index = this.rand.nextInt(4);
        this.setSize(0.01F, 0.01F);
        this.particleGravity = 0.06F;
        this.particleMaxAge = (int)(8.0D / (Math.random() * 0.8D + 0.2D));
	}
	
	@Override
	public int getFXLayer() {
        return 1;
    }
	
	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.motionY -= this.particleGravity;
		this.move(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.98D;
		this.motionY *= 0.98D;
		this.motionZ *= 0.98D;

		if (this.particleMaxAge-- <= 0) {
			this.setExpired();
		}

		if (this.onGround) {
			if (Math.random() < 0.5D) {
				this.setExpired();
			}

			this.motionX *= 0.7D;
			this.motionZ *= 0.7D;
		}

		BlockPos blockpos = new BlockPos(this.posX, this.posY, this.posZ);
		IBlockState iblockstate = this.world.getBlockState(blockpos);
		Material material = iblockstate.getMaterial();

		if (material.isLiquid() || material.isSolid()) {
			double d0;

			if (iblockstate.getBlock() instanceof BlockLiquid) {
				d0 = 1.0F - BlockLiquid.getLiquidHeightPercent(iblockstate.getValue(BlockLiquid.LEVEL).intValue());
			} else {
				d0 = iblockstate.getBoundingBox(this.world, blockpos).maxY;
			}

			double d1 = MathHelper.floor(this.posY) + d0;

			if (this.posY < d1) {
				this.setExpired();
			}
		}
	}
	
	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

    	float f = this.particleTexture.getInterpolatedU((index & 1) == 0 ? 0 : 8);
    	float f1 = this.particleTexture.getInterpolatedU((index & 1) == 0 ? 8 : 16);
    	float f2 = this.particleTexture.getInterpolatedV(index < 2 ? 0 : 8);
    	float f3 = this.particleTexture.getInterpolatedV(index < 2 ? 8 : 16);
        float f4 = 0.1F * this.particleScale;

        float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * partialTicks - interpPosX);
        float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * partialTicks - interpPosY);
        float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - interpPosZ);
        int i = this.getBrightnessForRender(partialTicks);
        int j = i >> 16 & 65535;
        int k = i & 65535;
        Vec3d[] avec3d = new Vec3d[] {new Vec3d(-rotationX * f4 - rotationXY * f4, -rotationZ * f4, -rotationYZ * f4 - rotationXZ * f4), new Vec3d(-rotationX * f4 + rotationXY * f4, rotationZ * f4, -rotationYZ * f4 + rotationXZ * f4), new Vec3d(rotationX * f4 + rotationXY * f4, rotationZ * f4, rotationYZ * f4 + rotationXZ * f4), new Vec3d(rotationX * f4 - rotationXY * f4, -rotationZ * f4, rotationYZ * f4 - rotationXZ * f4)};

        if (this.particleAngle != 0.0F) {
            float f8 = this.particleAngle + (this.particleAngle - this.prevParticleAngle) * partialTicks;
            float f9 = MathHelper.cos(f8 * 0.5F);
            float f10 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.x;
            float f11 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.y;
            float f12 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.z;
            Vec3d vec3d = new Vec3d(f10, f11, f12);

            for (int l = 0; l < 4; ++l) {
                avec3d[l] = vec3d.scale(2.0D * avec3d[l].dotProduct(vec3d)).add(avec3d[l].scale(f9 * f9 - vec3d.dotProduct(vec3d))).add(vec3d.crossProduct(avec3d[l]).scale(2.0F * f9));
            }
        }

        buffer.pos(f5 + avec3d[0].x, f6 + avec3d[0].y, f7 + avec3d[0].z).tex(f1, f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos(f5 + avec3d[1].x, f6 + avec3d[1].y, f7 + avec3d[1].z).tex(f1, f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos(f5 + avec3d[2].x, f6 + avec3d[2].y, f7 + avec3d[2].z).tex(f, f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos(f5 + avec3d[3].x, f6 + avec3d[3].y, f7 + avec3d[3].z).tex(f, f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
    }

	@SideOnly(Side.CLIENT)
	public static class Factory implements IParticleFactory {
		@Override
		public Particle createParticle(int id, World world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... params) {
			return new ParticleAcidRain(world, x, y, z);
		}
	}
}
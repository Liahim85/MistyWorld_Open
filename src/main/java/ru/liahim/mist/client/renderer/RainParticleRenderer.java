package ru.liahim.mist.client.renderer;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.IRenderHandler;
import ru.liahim.mist.client.particle.ParticleAcidRain;
import ru.liahim.mist.handlers.FogRenderer;

public class RainParticleRenderer extends IRenderHandler {
	
	private int rainSoundCounter;

	@Override
	public void render(/* 0 */float partialTicks, WorldClient world, Minecraft mc) {
		float f = mc.world.getRainStrength(1.0F);

		if (!mc.gameSettings.fancyGraphics) f /= 2.0F;

		if (f != 0.0F) {
			mc.entityRenderer.random.setSeed(mc.entityRenderer.rendererUpdateCount * 312987231L);
			Entity entity = mc.getRenderViewEntity();
			BlockPos blockpos = new BlockPos(entity);
			int i = 10;
			double d0 = 0.0D;
			double d1 = 0.0D;
			double d2 = 0.0D;
			int j = 0;
			int k = (int) (100.0F * f * f);

			if (mc.gameSettings.particleSetting == 1) k >>= 1;
			else if (mc.gameSettings.particleSetting == 2) k = 0;

			for (int l = 0; l < k; ++l) {
				BlockPos blockpos1 = world.getPrecipitationHeight(blockpos.add(mc.entityRenderer.random.nextInt(10) - mc.entityRenderer.random.nextInt(10), 0, mc.entityRenderer.random.nextInt(10) - mc.entityRenderer.random.nextInt(10)));
				Biome biome = world.getBiome(blockpos1);
				BlockPos blockpos2 = blockpos1.down();
				IBlockState iblockstate = world.getBlockState(blockpos2);

				if (blockpos1.getY() <= blockpos.getY() + 10 && blockpos1.getY() >= blockpos.getY() - 10 && biome.canRain() && biome.getTemperature(blockpos1) >= 0.15F) {
					double d3 = mc.entityRenderer.random.nextDouble();
					double d4 = mc.entityRenderer.random.nextDouble();
					AxisAlignedBB axisalignedbb = iblockstate.getBoundingBox(world, blockpos2);

					if (iblockstate.getMaterial() != Material.LAVA && iblockstate.getBlock() != Blocks.MAGMA) {
						if (iblockstate.getMaterial() != Material.AIR) {
							++j;

							if (mc.entityRenderer.random.nextInt(j) == 0) {
								d0 = blockpos2.getX() + d3;
								d1 = blockpos2.getY() + 0.1F + axisalignedbb.maxY - 1.0D;
								d2 = blockpos2.getZ() + d4;
							}

							if (FogRenderer.fogHeight > blockpos2.getY() + 4) mc.effectRenderer.addEffect(new ParticleAcidRain(world, blockpos2.getX() + d3, blockpos2.getY() + 0.1F + axisalignedbb.maxY, blockpos2.getZ() + d4));
							else world.spawnParticle(EnumParticleTypes.WATER_DROP, blockpos2.getX() + d3, blockpos2.getY() + 0.1F + axisalignedbb.maxY, blockpos2.getZ() + d4, 0.0D, 0.0D, 0.0D, new int[0]);
						}
					} else {
						world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, blockpos1.getX() + d3, blockpos1.getY() + 0.1F - axisalignedbb.minY, blockpos1.getZ() + d4, 0.0D, 0.0D, 0.0D, new int[0]);
					}
				}
			}

			if (j > 0 && mc.entityRenderer.random.nextInt(3) < this.rainSoundCounter++) {
				this.rainSoundCounter = 0;
				if (d1 > blockpos.getY() + 1 && world.getPrecipitationHeight(blockpos).getY() > MathHelper.floor(blockpos.getY())) {
					world.playSound(d0, d1, d2, SoundEvents.WEATHER_RAIN_ABOVE, SoundCategory.WEATHER, 0.1F, 0.5F, false);
				} else {
					world.playSound(d0, d1, d2, SoundEvents.WEATHER_RAIN, SoundCategory.WEATHER, 0.2F, 1.0F, false);
				}
			}
		}
	}
}
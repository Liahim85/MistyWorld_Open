package ru.liahim.mist.world.mapgen;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;
import ru.liahim.mist.api.block.IWettable;
import ru.liahim.mist.world.MistWorld;
import ru.liahim.mist.world.biome.BiomeMistUp;

public class MistRavineGen extends MapGenBase {

	protected static final IBlockState AIR = Blocks.AIR.getDefaultState();
	private final float[] multipliers = new float[1024];

	protected void addTunnel(long caveSeed, int chunkX, int chunkZ, ChunkPrimer chunkPrimer, double randX,
		double randY, double randZ, float caveSize, float randPI, float angleToGenerate, int loopOne, int loopEnd,
		double yScale, double width) {
		Random random = new Random(caveSeed);
		Random randomErosion = new Random(1234L);
		double centerX = chunkX * 16 + 8;
		double centerZ = chunkZ * 16 + 8;
		float f = 0.0F;
		float f1 = 0.0F;
		if (loopEnd <= 0) {
			int i = this.range * 16 - 16;
			loopEnd = i - random.nextInt(i / 4);
		}
		boolean shouldStop = false;
		if (loopOne == -1) {
			loopOne = loopEnd / 2;
			shouldStop = true;
		}
		float f2 = 1.0F;
		for (int j = 0; j < 256; ++j) {
			if (j == 0 || random.nextInt(3) == 0)
				f2 = 1.0F + random.nextFloat() * random.nextFloat();
			this.multipliers[j] = f2 * f2;
		}
		boolean isUp;
		for (; loopOne < loopEnd; ++loopOne) {
			double sizeXZ = width + MathHelper.sin(loopOne * (float)Math.PI / loopEnd) * caveSize;
			double sizeY = sizeXZ * yScale;
			sizeXZ = sizeXZ * (random.nextFloat() * 0.25D + 0.75D);
			sizeY = sizeY * (random.nextFloat() * 0.25D + 0.75D);
			float cosAngle = MathHelper.cos(angleToGenerate);
			float sinAngle = MathHelper.sin(angleToGenerate);
			randX += MathHelper.cos(randPI) * cosAngle;
			randY += sinAngle;
			randZ += MathHelper.sin(randPI) * cosAngle;
			isUp = this.world.getBiome(new BlockPos(randX, 0, randZ)) instanceof BiomeMistUp;
			if (!isUp && randY - sizeY < MistWorld.lowerStoneHight + 5) {
				randY = MistWorld.lowerStoneHight + sizeY + 4 + randomErosion.nextInt(3);
			}
			angleToGenerate = angleToGenerate * 0.7F;
			angleToGenerate = angleToGenerate + f1 * 0.05F;
			randPI += f * 0.05F;
			f1 = f1 * 0.8F;
			f = f * 0.5F;
			f1 = f1 + (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
			f = f + (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
			
			if (shouldStop || random.nextInt(4) != 0) {
				double distX = randX - centerX;
				double distZ = randZ - centerZ;
				double d5 = loopEnd - loopOne;
				double sizeSixteen = caveSize + 2.0F + 16.0F;
				if (distX * distX + distZ * distZ - d5 * d5 > sizeSixteen * sizeSixteen)
					return;
				if (randX >= centerX - 16.0D - sizeXZ * 2.0D && randZ >= centerZ - 16.0D - sizeXZ * 2.0D && randX <= centerX + 16.0D + sizeXZ * 2.0D && randZ <= centerZ + 16.0D + sizeXZ * 2.0D) {
					int minX = MathHelper.floor(randX - sizeXZ) - chunkX * 16 - 1;
					int maxX = MathHelper.floor(randX + sizeXZ) - chunkX * 16 + 1;
					int minY = MathHelper.floor(randY - sizeY) - 1;
					int maxY = MathHelper.floor(randY + sizeY) + 1;
					int minZ = MathHelper.floor(randZ - sizeXZ) - chunkZ * 16 - 1;
					int maxZ = MathHelper.floor(randZ + sizeXZ) - chunkZ * 16 + 1;
					if (minX < 0)
						minX = 0;
					if (maxX > 16)
						maxX = 16;
					if (minY < MistWorld.lowerStoneHight + (isUp ? 1 : 5))
						minY = MistWorld.lowerStoneHight + (isUp ? 1 : 5);
					//if (maxY > MistWorld.fogMaxHight_S + 4)
						//maxY = MistWorld.fogMaxHight_S + 4;
					if (minZ < 0)
						minZ = 0;
					if (maxZ > 16)
						maxZ = 16;
					boolean hasHitWater = false;
					for (int genX = minX; !hasHitWater && genX < maxX; ++genX) {
						for (int genZ = minZ; !hasHitWater && genZ < maxZ; ++genZ) {
							for (int genY = maxY + 1; !hasHitWater && genY >= minY - 1; --genY) {
								if (genY >= 0 && genY < 256) {
									if (isOceanBlock(chunkPrimer, genX, genY, genZ, chunkX, chunkZ))
										hasHitWater = true;
									if (genY != minY - 1 && genX != minX && genX != maxX - 1 && genZ != minZ && genZ != maxZ - 1)
										genY = minY;
								}
							}
						}
					}
					if (!hasHitWater) {
						for (int genX = minX; genX < maxX; ++genX) {
							double d10 = (genX + chunkX * 16 + 0.5D - randX) / sizeXZ;
							for (int genZ = minZ; genZ < maxZ; ++genZ) {
								double d7 = (genZ + chunkZ * 16 + 0.5D - randZ) / sizeXZ;
								boolean hitGrass = false;
								if (d10 * d10 + d7 * d7 < 1.0D) {
									for (int genY = maxY; genY > minY; --genY) {
										double d8 = (genY - 1 + 0.5D - randY) / sizeY;
										if ((d10 * d10 + d7 * d7) * this.multipliers[genY - 1] + d8 * d8 / 6.0D < 1.0D) {
											if (isTopBlock(chunkPrimer, genX, genY, genZ, chunkX, chunkZ))
												hitGrass = true;
											digBlock(chunkPrimer, genX, genY, genZ, chunkX, chunkZ, hitGrass, randomErosion, isUp);
										}
									}
								}
							}
						}
						if (shouldStop)
							break;
					}
				}
			}
		}
	}

	@Override
	protected void recursiveGenerate(World world, int genX, int genZ, int chunkX, int chunkZ,
		ChunkPrimer chunkPrimer) {
		if (this.rand.nextInt(50) == 0) {
			Biome biome = world.getBiome(new BlockPos(genX + chunkX * 16, 0, genZ + chunkZ * 16));
			double randX = genX * 16 + this.rand.nextInt(16);
			double randY = this.rand.nextInt(this.rand.nextInt(120) + 8) + 30;
			double randZ = genZ * 16 + this.rand.nextInt(16);
			double width = 1.0D;
			if (biome instanceof BiomeMistUp) {
				width = 1.5D;
			}
			int numberOfNormalNodes = 1;
			for (int j = 0; j < numberOfNormalNodes; ++j) {
				float randPi = this.rand.nextFloat() * ((float)Math.PI * 2F);
				float angleToGenerate = (this.rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
				float caveSize = (this.rand.nextFloat() * 2.0F + this.rand.nextFloat()) * 2.0F;
				this.addTunnel(this.rand.nextLong(), chunkX, chunkZ, chunkPrimer, randX, randY, randZ, caveSize, randPi, angleToGenerate, 0, 0, 3.0D, width);
			}
		}
	}

	protected boolean isOceanBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ) {
		IBlockState state = data.getBlockState(x, y, z);
		return (state == MistWorld.waterBlockUpper || state == MistWorld.waterBlockLower) && y != MistWorld.lowerStoneHight + 2;
	}

	private boolean isExceptionBiome(Biome biome) { //TODO Верхний блок каньона
		/*if (biome == Biomes.BEACH)
			return true;
		if (biome == Biomes.DESERT)
			return true;
		if (biome == Biomes.MUSHROOM_ISLAND)
			return true;
		if (biome == Biomes.MUSHROOM_ISLAND_SHORE)
			return true;*/
		return false;
	}

	private boolean isTopBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ) {
		Biome biome = world.getBiome(new BlockPos(x + chunkX * 16, 0, z + chunkZ * 16));
		IBlockState state = data.getBlockState(x, y, z);
		return /*isExceptionBiome(biome) ? state.getBlock() == Blocks.GRASS :*/ state == biome.topBlock;
	}

	protected boolean canReplaceBlock(IBlockState state) {
		return (state == MistWorld.worldStone || state == MistWorld.stoneBlockUpper || state.getBlock() instanceof IWettable ||
				state.getBlock() == Blocks.SNOW_LAYER || state == MistWorld.gravelBlock);
	}

	protected void digBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ, boolean foundTop, Random rand, boolean isUp) {
		Biome biome = world.getBiome(new BlockPos(x + chunkX * 16, 0, z + chunkZ * 16));
		IBlockState state = data.getBlockState(x, y, z);
		IBlockState top = /*isExceptionBiome(biome) ? Blocks.GRASS.getDefaultState() :*/ biome.topBlock;
		IBlockState filler = /*isExceptionBiome(biome) ? Blocks.DIRT.getDefaultState() :*/ biome.fillerBlock;
		if ((this.canReplaceBlock(state) || state == top || state == filler) && (y > MistWorld.fogMaxHight_S + 4 || data.getBlockState(x, y + 1, z) != MistWorld.gravelBlock)) {
			if (y == MistWorld.lowerStoneHight + 2) {
				data.setBlockState(x, y, z, MistWorld.waterBlockLower);
				if (data.getBlockState(x, y - 1, z) == MistWorld.worldStone)
					data.setBlockState(x, y - 1, z, MistWorld.gravelBlock);
				//if (data.getBlockState(x, y + 1, z) == AIR && rand.nextInt(4) == 0)
					//data.setBlockState(x, y + 1, z, Blocks.WATERLILY.getDefaultState()); //TODO лилия
			} else {
				data.setBlockState(x, y, z, AIR);
				if (y > MistWorld.lowerStoneHight + 2 && rand.nextInt(2) == 0) {
					if (!isUp && data.getBlockState(x, y - 1, z) == MistWorld.gravelBlock) data.setBlockState(x, y - 1, z, AIR);
					if (rand.nextInt(4) == 0) {
						if (data.getBlockState(x, y - 1, z) == MistWorld.worldStone && y - 1 != MistWorld.lowerStoneHight + 2)
							data.setBlockState(x, y - 1, z, AIR);
						if (!isOceanBlock(data, x, y + 1, z, chunkX, chunkZ))
							data.setBlockState(x, y + 1, z, AIR);
					} else {
						if (data.getBlockState(x, y - 1, z) == MistWorld.worldStone)
							data.setBlockState(x, y - 1, z, MistWorld.gravelBlock);
						if (data.getBlockState(x, y + 1, z) == MistWorld.worldStone)
							data.setBlockState(x, y + 1, z, MistWorld.gravelBlock);
					}
				}
				if (foundTop && data.getBlockState(x, y - 1, z).getBlock() == filler.getBlock())
					data.setBlockState(x, y - 1, z, top.getBlock().getDefaultState());
			}
		}
	}
}
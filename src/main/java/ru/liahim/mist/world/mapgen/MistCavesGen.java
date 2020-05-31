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

import com.google.common.base.MoreObjects;

public class MistCavesGen extends MapGenBase //��������
{

	protected static final IBlockState AIR = Blocks.AIR.getDefaultState();

	protected void addRoom(long caveSeed, int chunkX, int chunkZ, ChunkPrimer chunkPrimer, double randX, double randY,
		double randZ) {
		this.addTunnel(caveSeed, chunkX, chunkZ, chunkPrimer, randX, randY, randZ, 1.0F + this.rand.nextFloat() * 6.0F, 0.0F, 0.0F, -1, -1, 0.5D, 2.5D);
	}

	protected void addTunnel(long caveSeed, int chunkX, int chunkZ, ChunkPrimer chunkPrimer, double randX,
		double randY, double randZ, float caveSize, float randPI, float angleToGenerate, int loopOne, int loopEnd,
		double yScale, double width) {
		double centerX = chunkX * 16 + 8;
		double centerZ = chunkZ * 16 + 8;
		float f = 0.0F;
		float f1 = 0.0F;
		Random random = new Random(caveSeed);
		Random randomErosion = new Random(1234L);
		if (loopEnd <= 0) {
			int i = this.range * 16 - 16;
			loopEnd = i - random.nextInt(i / 4);
		}
		boolean shouldStop = false;
		if (loopOne == -1) {
			loopOne = loopEnd / 2;
			shouldStop = true;
		}
		int j = random.nextInt(loopEnd / 2) + loopEnd / 4;
		boolean isUp;
		for (boolean flag = random.nextInt(6) == 0; loopOne < loopEnd; ++loopOne) {
			double sizeXZ = width + MathHelper.sin(loopOne * (float)Math.PI / loopEnd) * caveSize;
			double sizeY = sizeXZ * yScale;
			float cosAngle = MathHelper.cos(angleToGenerate);
			float sinAngle = MathHelper.sin(angleToGenerate);
			randX += MathHelper.cos(randPI) * cosAngle;
			randY += sinAngle;
			randZ += MathHelper.sin(randPI) * cosAngle;
			isUp = this.world.getBiome(new BlockPos(randX, 0, randZ)) instanceof BiomeMistUp;
			if (!isUp && randY - sizeY < MistWorld.lowerStoneHight + 5) {
				randY = MistWorld.lowerStoneHight + sizeY + 4 + randomErosion.nextInt(3);
			}
			if (flag) angleToGenerate = angleToGenerate * 0.92F;
			else angleToGenerate = angleToGenerate * 0.7F;
			angleToGenerate = angleToGenerate + f1 * 0.1F;
			randPI += f * 0.1F;
			f1 = f1 * 0.9F;
			f = f * 0.75F;
			f1 = f1 + (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
			f = f + (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
			if (!shouldStop && loopOne == j && caveSize > 1.0F && loopEnd > 0) {
				this.addTunnel(random.nextLong(), chunkX, chunkZ, chunkPrimer, randX, randY, randZ, random.nextFloat() * 0.5F + 0.5F, randPI - ((float)Math.PI / 2F), angleToGenerate / 3.0F, loopOne, loopEnd, 1.0D, width);
				this.addTunnel(random.nextLong(), chunkX, chunkZ, chunkPrimer, randX, randY, randZ, random.nextFloat() * 0.5F + 0.5F, randPI + ((float)Math.PI / 2F), angleToGenerate / 3.0F, loopOne, loopEnd, 1.0D, width);
				return;
			}
			if (shouldStop || random.nextInt(4) != 0) {
				double distX = randX - centerX;
				double distZ = randZ - centerZ;
				double d6 = loopEnd - loopOne;
				double sizeSixteen = caveSize + 2.0F + 16.0F;
				if (distX * distX + distZ * distZ - d6 * d6 > sizeSixteen * sizeSixteen)
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
						BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
						for (int genX = minX; genX < maxX; ++genX) {
							double d10 = (genX + chunkX * 16 + 0.5D - randX) / sizeXZ;
							for (int genZ = minZ; genZ < maxZ; ++genZ) {
								double d8 = (genZ + chunkZ * 16 + 0.5D - randZ) / sizeXZ;
								boolean hitGrass = false;
								if (d10 * d10 + d8 * d8 < 1.0D) {
									for (int genY = maxY; genY > minY; --genY) {
										double d9 = (genY - 1 + 0.5D - randY) / sizeY;
										if (d9 > -0.7D && d10 * d10 + d9 * d9 + d8 * d8 < 1.0D) {
											IBlockState iblockstate1 = chunkPrimer.getBlockState(genX, genY, genZ);
											IBlockState iblockstate2 = MoreObjects.firstNonNull(chunkPrimer.getBlockState(genX, genY + 1, genZ), AIR);
											if (isTopBlock(chunkPrimer, genX, genY, genZ, chunkX, chunkZ))
												hitGrass = true;
											digBlock(chunkPrimer, genX, genY, genZ, chunkX, chunkZ, hitGrass, iblockstate1, iblockstate2, randomErosion, isUp);
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

	protected boolean canReplaceBlock(IBlockState state, IBlockState stateUp) {
		return (state == MistWorld.worldStone || state == MistWorld.stoneBlockUpper || state.getBlock() instanceof IWettable ||
				state.getBlock() == Blocks.SNOW_LAYER || state == MistWorld.gravelBlock);
	}

	@Override
	protected void recursiveGenerate(World worldIn, int genX, int genZ, int chunkX, int chunkZ,
		ChunkPrimer chunkPrimerIn) {
		int numberOfCaves = this.rand.nextInt(this.rand.nextInt(this.rand.nextInt(15) + 1) + 1);
		double width;
		int caveChance = 16;
		if (this.rand.nextInt(caveChance) != 0)
			numberOfCaves = 0;
		for (int j = 0; j < numberOfCaves; ++j) {
			double randX = genX * 16 + this.rand.nextInt(16);
			double randY = this.rand.nextInt(this.rand.nextInt(120) + 36);
			double randZ = genZ * 16 + this.rand.nextInt(16);
			int numberOfNormalNodes = 1;
			if (randY < (MistWorld.fogMaxHight_S - MistWorld.lowerStoneHight) / 4 + MistWorld.lowerStoneHight)
				width = 1.8D;
			else if (randY < (MistWorld.fogMaxHight_S - MistWorld.lowerStoneHight) / 2 + MistWorld.lowerStoneHight)
				width = 2D;
			else if (randY < ((MistWorld.fogMaxHight_S - MistWorld.lowerStoneHight) / 4) * 3 + MistWorld.lowerStoneHight)
				width = 1.8D;
			else width = 1.5D;
			if (this.rand.nextInt(16) == 0)
				width = width + 0.8;
			if (this.rand.nextInt(4) == 0) {
				this.addRoom(this.rand.nextLong(), chunkX, chunkZ, chunkPrimerIn, randX, randY, randZ);
				numberOfNormalNodes += this.rand.nextInt(4);
			}
			for (int l = 0; l < numberOfNormalNodes; ++l) {
				float randPi = this.rand.nextFloat() * ((float)Math.PI * 2F);
				float angleToGenerate = (this.rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
				float caveSize = this.rand.nextFloat() * 2.0F + this.rand.nextFloat();
				if (this.rand.nextInt(10) == 0)
					caveSize *= this.rand.nextFloat() * this.rand.nextFloat() * 3.0F + 1.0F;
				this.addTunnel(this.rand.nextLong(), chunkX, chunkZ, chunkPrimerIn, randX, randY, randZ, caveSize, randPi, angleToGenerate, 0, 0, 1.0D, width);
			}
		}
	}

	protected boolean isOceanBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ) {
		IBlockState state = data.getBlockState(x, y, z);
		return (state == MistWorld.waterBlockUpper || state == MistWorld.waterBlockLower) && y != MistWorld.lowerStoneHight + 2;
	}

	private boolean isExceptionBiome(Biome biome) { //TODO Верхний блок пещеры
		/*if (biome == Biomes.BEACH)
			return true;
		if (biome == Biomes.DESERT)
			return true;*/
		return false;
	}

	private boolean isTopBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ) {
		Biome biome = world.getBiome(new BlockPos(x + chunkX * 16, 0, z + chunkZ * 16));
		IBlockState state = data.getBlockState(x, y, z);
		return /*isExceptionBiome(biome) ? state.getBlock() == Blocks.GRASS : */state == biome.topBlock;
	}

	protected void digBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ, boolean foundTop,
		IBlockState state, IBlockState up, Random rand, boolean isUp) {
		Biome biome = world.getBiome(new BlockPos(x + chunkX * 16, 0, z + chunkZ * 16));
		IBlockState top = biome.topBlock;
		IBlockState filler = biome.fillerBlock;
		if ((this.canReplaceBlock(state, up) || state == top || state == filler) //&& state != MistWorld.stoneBlockUpper
				&& up != MistWorld.waterBlockUpper && up != MistWorld.waterBlockLower && (y > MistWorld.fogMaxHight_S + 4 || up != MistWorld.gravelBlock)) {
			if (y == MistWorld.lowerStoneHight + 2) {
				data.setBlockState(x, y, z, MistWorld.waterBlockLower);
				if (data.getBlockState(x, y - 1, z) == MistWorld.worldStone)
					data.setBlockState(x, y - 1, z, MistWorld.gravelBlock);
				//if (data.getBlockState(x, y + 1, z) == AIR && rand.nextInt(4) == 0)
					//data.setBlockState(x, y + 1, z, Blocks.WATERLILY.getDefaultState()); //TODO лилия
			}
			else if (state != MistWorld.gravelBlock || (state == MistWorld.gravelBlock && (data.getBlockState(x, y - 1, z) != MistWorld.gravelBlock ||
					(data.getBlockState(x, y - 1, z) == MistWorld.gravelBlock && rand.nextInt(3) == 0)))) {
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
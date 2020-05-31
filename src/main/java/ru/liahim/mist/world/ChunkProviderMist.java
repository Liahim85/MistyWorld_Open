package ru.liahim.mist.world;

import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.event.terraingen.InitNoiseGensEvent.ContextOverworld;
import ru.liahim.mist.api.biome.MistBiomes;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.init.ModConfig;
import ru.liahim.mist.world.biome.BiomeMistBorder;
import ru.liahim.mist.world.biome.BiomeMistBorderDown;
import ru.liahim.mist.world.biome.BiomeMistBorderUp;
import ru.liahim.mist.world.biome.BiomeMistUp;
import ru.liahim.mist.world.mapgen.MistCavesGen;
import ru.liahim.mist.world.mapgen.MistRavineGen;

public class ChunkProviderMist implements IChunkGenerator {

	private final Random rand;
	private NoiseGeneratorOctaves minLimitPerlinNoise;
	private NoiseGeneratorOctaves maxLimitPerlinNoise;
	private NoiseGeneratorOctaves mainPerlinNoise;
	private NoiseGeneratorPerlin surfaceNoise;
	public NoiseGeneratorOctaves scaleNoise;
	public NoiseGeneratorOctaves depthNoise;
	public NoiseGeneratorOctaves forestNoise;
	private final World world;
	private final double[] heightMap;
	private final float[] biomeWeights;
	private double[] depthBuffer = new double[256];
	private MistCavesGen caveGenerator;
	private MistRavineGen ravineGenerator;
	private Biome[] biomesForGeneration;
	double[] mainNoiseRegion;
	double[] swampNoiseRegion;
	double[] minLimitRegion;
	double[] maxLimitRegion;
	double[] depthRegion;
	double[] swampDepthRegion;
	//Settings
	private float mainNoiseScaleX = 80.0F;				//Растягивание по X
	private float mainNoiseScaleY = 160.0F;				//Растягивание по Y
	private float mainNoiseScaleZ = 80.0F;				//Растягивание по Z
	private float swampNoiseScaleX = 20.0F;				//Растягивание по X (Болота)
	private float swampNoiseScaleY = 80.0F;				//Растягивание по Y (Болота)
	private float swampNoiseScaleZ = 20.0F;				//Растягивание по Z (Болота)
	private double depthNoiseScaleX = 200.0D;			//Уровень шума X
	private double depthNoiseScaleZ = 200.0D;			//Уровень шума Z
	private double depthNoiseScaleExponent = 0.5D;		//Размер единицы шума
	private float coordinateScale = 684.412F;			//Размытие по X,Z
	private float heightScale = 684.412F;				//Размытие по Y
	private float baseSize = 8.5F;						//Средняя высота
	private float stretchY = 12.0F;						//Перепады высот
	private float upperLimitScale = 512.0F;				//Верхний предел
	private float lowerLimitScale = 512.0F;				//Нижний предел
	private boolean useCaves = true;					//Пещеры
	private boolean useRavines = true;					//Каньоны

	public ChunkProviderMist(World worldIn, long seed, boolean mapFeaturesEnabledIn) {
		this.world = worldIn;
		this.rand = new Random(seed);		

		this.caveGenerator = new MistCavesGen();
		this.ravineGenerator = new MistRavineGen();

		this.minLimitPerlinNoise = new NoiseGeneratorOctaves(this.rand, 16);
		this.maxLimitPerlinNoise = new NoiseGeneratorOctaves(this.rand, 16);
		this.mainPerlinNoise = new NoiseGeneratorOctaves(this.rand, 8);
		this.surfaceNoise = new NoiseGeneratorPerlin(this.rand, 4);
		this.scaleNoise = new NoiseGeneratorOctaves(this.rand, 10);
		this.depthNoise = new NoiseGeneratorOctaves(this.rand, 16);
		this.forestNoise = new NoiseGeneratorOctaves(this.rand, 8);
		this.heightMap = new double[825];
		this.biomeWeights = new float[25];
		for (int i = -2; i <= 2; ++i) {
			for (int j = -2; j <= 2; ++j) {
				float f = 10.0F / MathHelper.sqrt(i * i + j * j + 0.2F);
				this.biomeWeights[i + 2 + (j + 2) * 5] = f;
			}
		}
		ContextOverworld ctx = new ContextOverworld(minLimitPerlinNoise, maxLimitPerlinNoise, mainPerlinNoise, surfaceNoise, scaleNoise, depthNoise, forestNoise);
		//ctx = TerrainGen.getModdedNoiseGenerators(worldIn, this.rand, ctx);
		this.minLimitPerlinNoise = ctx.getLPerlin1();
		this.maxLimitPerlinNoise = ctx.getLPerlin2();
		this.mainPerlinNoise = ctx.getPerlin();
		this.surfaceNoise = ctx.getHeight();
		this.scaleNoise = ctx.getScale();
		this.depthNoise = ctx.getDepth();
		this.forestNoise = ctx.getForest();
	}

	@Override
	public Chunk generateChunk(int chunkX, int chunkZ) {
		this.rand.setSeed(chunkX * 341873128712L + chunkZ * 132897987541L); //!!!
		ChunkPrimer chunkprimer = new ChunkPrimer();
		this.setBlocksInChunk(chunkX, chunkZ, chunkprimer);
		this.biomesForGeneration = this.world.getBiomeProvider().getBiomes(this.biomesForGeneration, chunkX * 16, chunkZ * 16, 16, 16);
		this.replaceBiomeBlocks(chunkX, chunkZ, chunkprimer, this.biomesForGeneration);
		boolean check = false;
		for (int i = 0; i < this.biomesForGeneration.length; i++) {
			if (this.biomesForGeneration[i] instanceof BiomeMistBorder)
				check = true;
		}
		if (check) {
			this.erosionBorder(chunkX, chunkZ, chunkprimer);
		}
		if (this.useCaves)
			this.caveGenerator.generate(this.world, chunkX, chunkZ, chunkprimer);
		if (this.useRavines)
			this.ravineGenerator.generate(this.world, chunkX, chunkZ, chunkprimer);		
		Chunk chunk = new Chunk(this.world, chunkprimer, chunkX, chunkZ);
		byte[] abyte = chunk.getBiomeArray();
		for (int i = 0; i < abyte.length; ++i)
			abyte[i] = (byte)Biome.getIdForBiome(this.biomesForGeneration[i]);
		chunk.generateSkylightMap();
		return chunk;
	}

	public void setBlocksInChunk(int chunkX, int chunkZ, ChunkPrimer primer) {
		this.biomesForGeneration = this.world.getBiomeProvider().getBiomesForGeneration(this.biomesForGeneration, chunkX * 4 - 2, chunkZ * 4 - 2, 10, 10);
		this.generateHeightmap(chunkX * 4, 0, chunkZ * 4);
		for (int i = 0; i < 4; ++i) {
			int j = i * 5;
			int k = (i + 1) * 5;
			for (int l = 0; l < 4; ++l) {
				int i1 = (j + l) * 33;
				int j1 = (j + l + 1) * 33;
				int k1 = (k + l) * 33;
				int l1 = (k + l + 1) * 33;
				for (int i2 = 0; i2 < 32; ++i2) {
					double d0 = 0.125D;
					double d1 = this.heightMap[i1 + i2];
					double d2 = this.heightMap[j1 + i2];
					double d3 = this.heightMap[k1 + i2];
					double d4 = this.heightMap[l1 + i2];
					double d5 = (this.heightMap[i1 + i2 + 1] - d1) * 0.125D;
					double d6 = (this.heightMap[j1 + i2 + 1] - d2) * 0.125D;
					double d7 = (this.heightMap[k1 + i2 + 1] - d3) * 0.125D;
					double d8 = (this.heightMap[l1 + i2 + 1] - d4) * 0.125D;
					for (int j2 = 0; j2 < 8; ++j2) {
						double d9 = 0.25D;
						double d10 = d1;
						double d11 = d2;
						double d12 = (d3 - d1) * 0.25D;
						double d13 = (d4 - d2) * 0.25D;
						for (int k2 = 0; k2 < 4; ++k2) {
							double d14 = 0.25D;
							double d16 = (d11 - d10) * 0.25D;
							double lvt_45_1_ = d10 - d16;
							for (int l2 = 0; l2 < 4; ++l2) {
								if ((lvt_45_1_ += d16) > 0.0D)
									primer.setBlockState(i * 4 + k2, i2 * 8 + j2, l * 4 + l2, MistWorld.worldStone);
								else if (i2 * 8 + j2 < MistWorld.seaLevelDown)
									primer.setBlockState(i * 4 + k2, i2 * 8 + j2, l * 4 + l2, MistWorld.waterBlockLower);
							}
							d10 += d12;
							d11 += d13;
						}
						d1 += d5;
						d2 += d6;
						d3 += d7;
						d4 += d8;
					}
				}
			}
		}
	}

	private void generateHeightmap(int chunkX, int zero, int chunkZ) {
		this.depthRegion = this.depthNoise.generateNoiseOctaves(this.depthRegion, chunkX, chunkZ, 5, 5, this.depthNoiseScaleX, this.depthNoiseScaleZ, this.depthNoiseScaleExponent);
		float f = this.coordinateScale;
		float f1 = this.heightScale;
		this.mainNoiseRegion = this.mainPerlinNoise.generateNoiseOctaves(this.mainNoiseRegion, chunkX, zero, chunkZ, 5, 33, 5, f / this.mainNoiseScaleX, f1 / this.mainNoiseScaleY, f / this.mainNoiseScaleZ);
		this.swampNoiseRegion = this.mainPerlinNoise.generateNoiseOctaves(this.swampNoiseRegion, chunkX, zero, chunkZ, 5, 33, 5, f / this.swampNoiseScaleX, f1 / this.swampNoiseScaleY, f / this.swampNoiseScaleZ);
		this.minLimitRegion = this.minLimitPerlinNoise.generateNoiseOctaves(this.minLimitRegion, chunkX, zero, chunkZ, 5, 33, 5, f, f1, f);
		this.maxLimitRegion = this.maxLimitPerlinNoise.generateNoiseOctaves(this.maxLimitRegion, chunkX, zero, chunkZ, 5, 33, 5, f, f1, f);
		int i = 0;
		int j = 0;
		for (int k = 0; k < 5; ++k) {
			for (int l = 0; l < 5; ++l) {
				float heightVar = 0.0F;
				float baseHeight = 0.0F;
				float biomeWeight = 0.0F;
				Biome biome = this.biomesForGeneration[k + 2 + (l + 2) * 10];
				// TODO Mist hook start
				boolean swampUp = biome == MistBiomes.upSwamp;
				boolean swampDown = biome == MistBiomes.downSwamp;
				boolean borderUp = biome instanceof BiomeMistBorderUp;
				boolean borderDown = biome instanceof BiomeMistBorderDown;
				boolean upBiome = biome instanceof BiomeMistUp;
				boolean dunes = biome == MistBiomes.upDunes;
				float swampDownWeight = 0.0F;
				float swampUpWeight = 0.0F;
				float dunesWeight = 0.0F;
				// Mist hook end
				int smoothRadius = borderUp ? 1 : 2;
				for (int k1 = -smoothRadius; k1 <= smoothRadius; ++k1) {
					for (int l1 = -smoothRadius; l1 <= smoothRadius; ++l1) {
						Biome biome1 = this.biomesForGeneration[k + k1 + 2 + (l + l1 + 2) * 10];
						float baseHeight1 = biome1.getBaseHeight();
						float heightVar1 = biome1.getHeightVariation();
						float biomeWeight1 = this.biomeWeights[k1 + 2 + (l1 + 2) * 5] / (baseHeight1 + 2.0F);
						// TODO Mist hook start
						if (borderUp) {
							if (biome1.getBaseHeight() < biome.getBaseHeight()) {
								biomeWeight1 /= 3.0F;
							}
						} else if (borderDown) {
							if (biome1.getBaseHeight() > biome.getBaseHeight()) {
								biomeWeight1 *= 2.0F;
							}
						} else if (upBiome) {
							if (biome1 instanceof BiomeMistBorderUp) {
								biomeWeight1 *= 4.0F;
							}
						} else if (biome1.getBaseHeight() > biome.getBaseHeight()) {
							biomeWeight1 /= 2.0F;
						}
						if (!swampDown && biome1 == MistBiomes.downSwamp) {
							swampDownWeight = Math.max(swampDownWeight, biomeWeight1);
						} else if (swampDown && biome1 != MistBiomes.downSwamp) {
							swampDownWeight = Math.min(swampDownWeight, -biomeWeight1);
						}
						if (!swampUp && biome1 == MistBiomes.upSwamp) {
							swampUpWeight = Math.max(swampUpWeight, biomeWeight1);
						} else if (swampUp && biome1 != MistBiomes.upSwamp) {
							swampUpWeight = Math.min(swampUpWeight, -biomeWeight1);
						}
						if (!dunes && biome1 == MistBiomes.upDunes) {
							dunesWeight = Math.max(dunesWeight, biomeWeight1);
						} else if (dunes && biome1 != MistBiomes.upDunes) {
							dunesWeight = Math.min(dunesWeight, -biomeWeight1);
						}
						// Mist hook end
						heightVar += heightVar1 * biomeWeight1;
						baseHeight += baseHeight1 * biomeWeight1;
						biomeWeight += biomeWeight1;
					}
				}
				heightVar = heightVar / biomeWeight;
				baseHeight = baseHeight / biomeWeight;
				heightVar = heightVar * 0.9F + 0.1F;
				baseHeight = (baseHeight * 4.0F - 1.0F) / 8.0F;
				double depth;
				// TODO Mist hook
				if (swampUp || swampDown || dunes) depth = 0.0D;
				else {
					depth = this.depthRegion[j] / 8000.0D;
					if (depth < 0.0D)
						depth = -depth * 0.3D;
					depth = depth * 3.0D - 2.0D;
					if (depth < 0.0D) {
						depth = depth / 2.0D;
						if (depth < -1.0D)
							depth = -1.0D;
						depth = depth / 1.4D;
						depth = depth / 2.0D;
					} else {
						if (depth > 1.0D)
							depth = 1.0D;
						depth = depth / 8.0D;
					}
				}
				++j;
				double d8 = baseHeight;
				double d9 = heightVar;
				d8 = d8 + depth * 0.2D;
				d8 = d8 * this.baseSize / 8.0D;
				double d0 = this.baseSize + d8 * 4.0D;
				for (int l2 = 0; l2 < 33; ++l2) {
					double d1 = (l2 - d0) * this.stretchY * 128.0D / 256.0D / d9;
					if (d1 < 0.0D) {
						d1 *= 4.0D;
					}
					double d2 = this.minLimitRegion[i] / this.lowerLimitScale;
					double d3 = this.maxLimitRegion[i] / this.upperLimitScale;
					double d4 = (((swampUp || swampDown) ? this.swampNoiseRegion[i] + (swampUp ? 30 : 10) : this.mainNoiseRegion[i]) / 10.0D + 1.0D) / 2.0D;
					// TODO Mist hook start
					double d2_X = d2;
					double d3_X = d3;
					double d4_X = d4;
					if (borderUp) {
						if (d2 < 0) d2 /= 2;
						if (d2 < -50) d2 = -50;
					}
					if (swampDown || swampDownWeight > 0) {
						d2_X *= 4;
						if (d2_X > -60) d2_X = -60;
						if (d3_X > 20) d3_X = 10;
						if (d4_X > 0) d4_X /= 4;
						if (swampDownWeight > 0) {
							d2_X = (d2_X * swampDownWeight + d2 * (100 - swampDownWeight)) / 100;
							d3_X = (d3_X * swampDownWeight + d3 * (100 - swampDownWeight)) / 100;
							d4_X = (d4_X * swampDownWeight + d4 * (100 - swampDownWeight)) / 100;
						} else if (swampDownWeight < 0) {
							d2_X = (d2_X * (100 + swampDownWeight) + d2 * -swampDownWeight) / 100;
							d3_X = (d3_X * (100 + swampDownWeight) + d3 * -swampDownWeight) / 100;
							d4_X = (d4_X * (100 + swampDownWeight) + d4 * -swampDownWeight) / 100;
						}
						d2 = d2_X;
						d3 = d3_X;
						d4 = d4_X;
					} else if (swampUp || swampUpWeight > 0) {
						d2_X = -100;
						if (d3_X > 20) d3_X = 20;
						else if (d3_X < -20) d3_X = -20;
						else d3_X = 0;
						if (swampUpWeight > 0) {
							d2_X = (d2_X * swampUpWeight + d2 * (5 - swampUpWeight)) / 5;
							d3_X = (d3_X * swampUpWeight + d3 * (5 - swampUpWeight)) / 5;
						} else if (swampUpWeight < 0) {
							d2_X = (d2_X * (10 + swampUpWeight) + d2 * -swampUpWeight) / 10;
							d3_X = (d3_X * (10 + swampUpWeight) + d3 * -swampUpWeight) / 10;
						}
						d2 = d2_X;
						d3 = d3_X;
					} else if (dunes || dunesWeight > 0) {
						if (d4_X < 0) d4_X = -d4_X;
						//d2_X = (d2_X + d4_X - 160) / 4 - 20;
						d3_X = (d3_X + d4_X + 160) / 4 + 20;
						d4_X /= 7;
						if (dunesWeight > 0) {
							d3_X = (d3_X * dunesWeight + d3 * (2 - dunesWeight)) / 2;
							d4_X = (d4_X * dunesWeight + d4 * (2 - dunesWeight)) / 2;
						} else if (dunesWeight < 0) {
							d3_X = (d3_X * (20 + dunesWeight) + d3 * -dunesWeight) / 20;
							d4_X = (d4_X * (20 + dunesWeight) + d4 * -dunesWeight) / 20;
						}
						d3 = d3_X;
						d4 = d4_X;
					}
					// Mist hook end
					double d5 = MathHelper.clampedLerp(d2, d3, d4) - d1;
					if (l2 > 29) {
						double d6 = (l2 - 29) / 3.0F;
						d5 = d5 * (1.0D - d6) + -10.0D * d6;
					}
					this.heightMap[i] = d5;
					++i;
				}
			}
		}
	}

	public void replaceBiomeBlocks(int chunkX, int chunkZ, ChunkPrimer primer, Biome[] biomesIn) {
		if (!ForgeEventFactory.onReplaceBiomeBlocks(this, chunkX, chunkZ, primer, this.world)) return;
		double d0 = 0.03125D;
		this.depthBuffer = this.surfaceNoise.getRegion(this.depthBuffer, chunkX * 16, chunkZ * 16, 16, 16, d0 * 2.0D, d0 * 2.0D, 1.0D);
		Biome biome;
		for (int x = 0; x < 16; ++x) {
			for (int z = 0; z < 16; ++z) {
				biome = biomesIn[z + x * 16];
				biome.genTerrainBlocks(this.world, this.rand, primer, chunkX * 16 + x, chunkZ * 16 + z, this.depthBuffer[z + x * 16]);
			}
		}
	}

	public void erosionBorder(int chunkX, int chunkZ, ChunkPrimer primer) {
		IBlockState block;
		for (int x = 0; x < 16; ++x) {
			for (int z = 0; z < 16; ++z) {
				for (int y = MistWorld.lowerStoneHight; y <= MistWorld.fogMaxHight_S; ++y) {
					block = primer.getBlockState(x, y, z);
					if (block == MistWorld.worldStone || block == MistWorld.gravelBlock) {
						if ((x > 0 && primer.getBlockState(x - 1, y, z) == Blocks.AIR.getDefaultState()) ||
								(x < 15 && primer.getBlockState(x + 1, y, z) == Blocks.AIR.getDefaultState()) ||
								(z > 0 && primer.getBlockState(x, y, z - 1) == Blocks.AIR.getDefaultState()) ||
								(z < 15 && primer.getBlockState(x, y, z + 1) == Blocks.AIR.getDefaultState()))
						{
							if (this.rand.nextInt(3) == 0)
								primer.setBlockState(x, y, z, Blocks.GLOWSTONE.getDefaultState());
							else if (this.rand.nextInt(3) == 0)
								primer.setBlockState(x, y, z, MistWorld.gravelBlock);
						}
					}
				}
			}
		}
		for (int x = 0; x < 16; ++x) {
			for (int z = 0; z < 16; ++z) {
				for (int y = MistWorld.lowerStoneHight; y <= MistWorld.fogMaxHight_S; ++y) {
					block = primer.getBlockState(x, y, z);
					if (block == Blocks.GLOWSTONE.getDefaultState())
						primer.setBlockState(x, y, z, Blocks.AIR.getDefaultState());
				}
			}
		}
	}

	@Override
	public void populate(int x, int z) {

		if (ModConfig.dimension.disableCascadingLog) ForgeModContainer.logCascadingWorldGeneration = false;

		BlockFalling.fallInstantly = true;
		int i = x * 16;
		int j = z * 16;
		BlockPos blockpos = new BlockPos(i, 0, j);
		Biome biome = this.world.getBiome(blockpos.add(16, 0, 16));
		this.rand.setSeed(this.world.getSeed());
		long k = this.rand.nextLong() / 2L * 2L + 1L;
		long l = this.rand.nextLong() / 2L * 2L + 1L;
		this.rand.setSeed(x * k + z * l ^ this.world.getSeed());
		boolean flag = false;

		ForgeEventFactory.onChunkPopulate(true, this, this.world, this.rand, x, z, flag);

		biome.decorate(this.world, this.rand, new BlockPos(i, 0, j));

		/** Animals */
		if (TerrainGen.populate(this, this.world, this.rand, x, z, flag, PopulateChunkEvent.Populate.EventType.ANIMALS))
		WorldEntitySpawner.performWorldGenSpawning(this.world, biome, i + 8, j + 8, 16, 16, this.rand);

		if (TerrainGen.populate(this, this.world, this.rand, x, z, flag, PopulateChunkEvent.Populate.EventType.ICE)) {
			for (int x1 = 0; x1 < 16; ++x1) {
				for (int z1 = 0; z1 < 16; ++z1) {
					BlockPos blockpos1 = this.world.getPrecipitationHeight(blockpos.add(x1, 0, z1));
					BlockPos blockpos2 = blockpos1.down();
					if (this.world.canBlockFreezeWater(blockpos2))
						this.world.setBlockState(blockpos2, Blocks.ICE.getDefaultState(), Mist.FLAG);
					if (this.world.canSnowAt(blockpos1, true))
						this.world.setBlockState(blockpos1, Blocks.SNOW_LAYER.getDefaultState(), Mist.FLAG);
				}
			}
        }

		/** Seasons! */
		MistWorld.seasonalTest(this.world.getChunkFromChunkCoords(x, z));

		ForgeEventFactory.onChunkPopulate(false, this, this.world, this.rand, x, z, flag);

		/** Needed for falling blocks normal work. */
		BlockFalling.fallInstantly = false;
	}

	@Override
	public boolean generateStructures(Chunk chunk, int x, int z) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
		Biome biome = this.world.getBiome(pos);
		if (pos.getY() > MistWorld.getFogMaxHight()) return biome.getSpawnableList(creatureType);
		return null;
	}

	@Override
	public void recreateStructures(Chunk chunk, int x, int z) {
		// TODO Auto-generated method stub
	}

	@Override
	public BlockPos getNearestStructurePos(World world, String structureName, BlockPos position,
			boolean findUnexplored) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isInsideStructure(World world, String structureName, BlockPos pos) {
		// TODO Auto-generated method stub
		return false;
	}
}
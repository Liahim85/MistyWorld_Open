package ru.liahim.mist.world.biome;

import java.util.ArrayList;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.biome.EnumBiomeType;
import ru.liahim.mist.api.biome.MistBiomes;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.MistDirt;
import ru.liahim.mist.world.MistWorld;
import ru.liahim.mist.world.generators.BigRockGenerator;
import ru.liahim.mist.world.generators.LooseRockGenerator;

public abstract class BiomeMist extends Biome {

	public IBlockState secondTopBlock;
	public IBlockState zeroBlock;
	public IBlockState topBlockOuter;
	public IBlockState secondTopBlockOuter;
	public IBlockState fillerBlockOuter;
	public IBlockState zeroBlockOuter;
	protected int biomeColor;
	protected static final WorldGenerator minRockGen = new BigRockGenerator(0);
	protected static final WorldGenerator medRockGen = new BigRockGenerator(1);
	protected static final WorldGenerator maxRockGen = new BigRockGenerator(2);
	public static final WorldGenerator looseRockGen = new LooseRockGenerator();
	protected static final NoiseGeneratorPerlin TREE_NOISE = new NoiseGeneratorPerlin(new Random(1234L), 1);
	protected static final NoiseGeneratorPerlin SAND_NOISE = new NoiseGeneratorPerlin(new Random(2345L), 1);
	protected static final NoiseGeneratorPerlin CLAY_NOISE = new NoiseGeneratorPerlin(new Random(3456L), 1);
	protected static final NoiseGeneratorPerlin PEAT_NOISE = new NoiseGeneratorPerlin(new Random(4567L), 1);
	protected static final NoiseGeneratorPerlin GRAVEL_NOISE = new NoiseGeneratorPerlin(new Random(5678L), 1);
	protected static final NoiseGeneratorPerlin SAPROPEL_NOISE = new NoiseGeneratorPerlin(new Random(6789L), 1);
	protected static final NoiseGeneratorPerlin FLOATING_MAT_NOISE = new NoiseGeneratorPerlin(new Random(7890L), 1);

	public BiomeMist(BiomeProperties properties) {
		super(properties);
		this.spawnableMonsterList.clear();
		this.spawnableCreatureList.clear();
		this.spawnableWaterCreatureList.clear();
		this.spawnableCaveCreatureList.clear();
		this.topBlock = MistBlocks.GRASS_F.getDefaultState().withProperty(MistDirt.HUMUS, 2);
		this.fillerBlock = MistBlocks.DIRT_F.getDefaultState();
	}

	public abstract boolean isUpBiome();

	public abstract EnumBiomeType getBiomeType();

	public int getBiomeColor() {
		return biomeColor;
	}

	public int getDownGrassColor() {
		return -1;
	}

	public BiomeMist setBiomeColor(int c) {
		biomeColor = c;
		return this;
	}

	@Override
	public float getSpawningChance() {
		return 0.02F;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getSkyColorByTemp(float currentTemperature) {
		currentTemperature = MathHelper.clamp(Math.abs(1.25F - currentTemperature), 0.0F, 1.0F);
		int r = 140;
		int g = (int)(185 + currentTemperature * 15);
		int b = (int)(215 - currentTemperature * 5);
		return r << 16 | g << 8 | b;
	}

	public void genMistWorldBiomeTerrain(World worldIn, Random rand, ChunkPrimer chunkPrimer, int xIn, int zIn, double noiseVal) {
		int x = zIn & 15;
		int z = xIn & 15;
		int noiseX = (xIn >> 4 << 4) + x;
		int noiseZ = (zIn >> 4 << 4) + z;
		ArrayList<Double> noises = getNoises(noiseX, noiseZ);
		IBlockState topState = getTopBlock(rand, noises);
		IBlockState fillerState = getFillerBlock(rand, noises);
		IBlockState secondTopState = getSecondTopBlock(rand, noises);
		secondTopState = secondTopState == null ? fillerState : secondTopState;
		IBlockState zeroState = getZeroBlock(rand, noises);
		int currentFillerDepth = -1;
		int gravelFillerDepth = -1;
		int maxFillerDepth = (int)(noiseVal / 3.0D + 3.0D + rand.nextDouble() * 0.25D);
		boolean dunes = this == MistBiomes.upDunes;
		for (int y = 255; y >= 0; --y) {
			boolean notErosion = y > MistWorld.fogMaxHight_S - rand.nextInt(3) + 1;
			if (y <= rand.nextInt(5))
				chunkPrimer.setBlockState(x, y, z, BEDROCK);
			else if (y <= MistWorld.lowerStoneHight + rand.nextInt(3))
				chunkPrimer.setBlockState(x, y, z, MistWorld.stoneBlockLower);
			else if (gravelFillerDepth >= 0) {
				--gravelFillerDepth;
				chunkPrimer.setBlockState(x, y, z, MistWorld.gravelBlock);
			} else {
				IBlockState iblockstateCurrent = chunkPrimer.getBlockState(x, y, z);
				if (iblockstateCurrent.getMaterial() == Material.AIR) {
					if (this instanceof BiomeMistUp && !(this instanceof BiomeMistUpDesert)) {
						if (y == MistWorld.seaLevelUp)
							chunkPrimer.setBlockState(x, y, z, Blocks.GLASS.getDefaultState()); // TODO This for the Lake Decoration
						else if (y <= MistWorld.seaLevelUp && y > MistWorld.fogMaxHight_S + 4)
							chunkPrimer.setBlockState(x, y, z, MistWorld.waterBlockUpper);
					}
					currentFillerDepth = -1;
				} else if (iblockstateCurrent.getBlock() == MistWorld.worldStone.getBlock()) {
					if (currentFillerDepth == -1) {
						if (maxFillerDepth <= 0) {
							topState = AIR;
							if (notErosion)
								fillerState = MistWorld.stoneBlockUpper;
							else fillerState = MistWorld.stoneBlockMedium;
						} else if ((!this.isUpBiome() && y > MistWorld.fogMaxHight_S + 4) || (isUpBiome() && y < MistWorld.fogMinHight_S - 4)) {
							if (this.topBlockOuter != null)
								topState = this.topBlockOuter;
							else topState = MistWorld.gravelBlock;
						} else if (y >= MistWorld.fogMinHight_S - 4 && y <= MistWorld.fogMaxHight_S + 4) {
							topState = MistWorld.gravelBlock;
						} else if (y < MistWorld.seaLevelUp && this.isUpBiome() && this.getBiomeType() != EnumBiomeType.Border) {
							topState = getBottom(rand, noises);
							secondTopState = getSecondBottom(rand, noises);
						}
						// TODO Dunes filler depth check
						if (dunes && y > 126) maxFillerDepth = maxFillerDepth + (y - 126) / 3;

						currentFillerDepth = maxFillerDepth;
						if (y >= (MistWorld.seaLevelDown - 1)) placeTopBlock(chunkPrimer, x, y, z, topState, noises);
						else placeBottomBlock(chunkPrimer, x, y, z, getBottom(rand, noises), noises);
					} else if (currentFillerDepth > 0) {
						--currentFillerDepth;
						if ((!this.isUpBiome() && y > MistWorld.fogMaxHight_S + 4) || (isUpBiome() && y <= MistWorld.fogMinHight_S - 4)) {
							if (this.secondTopBlockOuter != null)
								secondTopState = this.secondTopBlockOuter;
							else secondTopState = notErosion ? MistWorld.stoneBlockUpper : MistWorld.stoneBlockMedium;
							if (this.fillerBlockOuter != null)
								fillerState = this.fillerBlockOuter;
							else fillerState = notErosion ? MistWorld.stoneBlockUpper : MistWorld.stoneBlockMedium;
							if (this.zeroBlockOuter != null)
								zeroState = this.zeroBlockOuter;
							else zeroState = notErosion ? MistWorld.stoneBlockUpper : MistWorld.stoneBlockMedium;
						} else if (y >= MistWorld.fogMinHight_S - 4 && y <= MistWorld.fogMaxHight_S + 4) {
							secondTopState = fillerState = zeroState = notErosion ? MistWorld.stoneBlockUpper
								: MistWorld.stoneBlockMedium;
						}
						if (fillerState == MistWorld.stoneBlockMedium || fillerState == MistWorld.stoneBlockUpper) {
							chunkPrimer.setBlockState(x, y, z, notErosion ? MistWorld.stoneBlockUpper
								: MistWorld.stoneBlockMedium);
						} else {
							if (currentFillerDepth == 0 && zeroState != null)
								chunkPrimer.setBlockState(x, y, z, zeroState);
							else if (maxFillerDepth - currentFillerDepth == 1)
								placeSecondTopBlock(chunkPrimer, x, y, z, secondTopState, noises);
							else placeFillerBlock(chunkPrimer, x, y, z, fillerState, noises);
						}
						if (currentFillerDepth == 0 && y <= MistWorld.lowerStoneHight + 12) {
							gravelFillerDepth = MistWorld.lowerStoneHight + 12 - y;
						}
					}
					else if (notErosion)
						chunkPrimer.setBlockState(x, y, z, MistWorld.stoneBlockUpper);
				}
			}
		}
	}

	@Nullable
	public ArrayList<Double> getNoises(int x, int z) {
		return null;
	}

	public IBlockState getTopBlock(Random rand, @Nullable ArrayList<Double> noises) {
		return this.topBlock;
	}

	public IBlockState getSecondTopBlock(Random rand, @Nullable ArrayList<Double> noises) {
		return this.secondTopBlock;
	}

	public IBlockState getFillerBlock(Random rand, @Nullable ArrayList<Double> noises) {
		return this.fillerBlock;
	}

	protected IBlockState getZeroBlock(Random rand, @Nullable ArrayList<Double> noises) {
		return this.zeroBlock;
	}

	protected IBlockState getBottom(Random rand, @Nullable ArrayList<Double> noises) {
		IBlockState state = getSecondTopBlock(rand, noises);
		return state != null ? state : getFillerBlock(rand, noises);
	}

	protected IBlockState getSecondBottom(Random rand, ArrayList<Double> noises) {
		return getFillerBlock(rand, noises);
	}

	protected void placeTopBlock(ChunkPrimer chunkPrimer, int x, int y, int z, IBlockState state, @Nullable ArrayList<Double> noises) {
		chunkPrimer.setBlockState(x, y, z, state);
	}

	protected void placeSecondTopBlock(ChunkPrimer chunkPrimer, int x, int y, int z, IBlockState state, @Nullable ArrayList<Double> noises) {
		chunkPrimer.setBlockState(x, y, z, state);
	}

	protected void placeFillerBlock(ChunkPrimer chunkPrimer, int x, int y, int z, IBlockState state, @Nullable ArrayList<Double> noises) {
		chunkPrimer.setBlockState(x, y, z, state);
	}

	protected void placeBottomBlock(ChunkPrimer chunkPrimer, int x, int y, int z, IBlockState state, @Nullable ArrayList<Double> noises) {
		chunkPrimer.setBlockState(x, y, z, state);
	}

	@Override
	public void genTerrainBlocks(World worldIn, Random rand, ChunkPrimer chunkPrimerIn, int x, int z, double noiseVal) {
		this.genMistWorldBiomeTerrain(worldIn, rand, chunkPrimerIn, x, z, noiseVal);
	}

	@Override
    public BiomeDecorator createBiomeDecorator() {
        return new MistBiomeDecorator();
    }

    protected MistBiomeDecorator getMistBiomeDecorator() {
    	return (MistBiomeDecorator)this.decorator;
    }

    protected final IBlockState grass = Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS);
    protected final IBlockState fern = Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.FERN);
    protected final IBlockState grassDown = Blocks.DOUBLE_PLANT.getDefaultState().withProperty(BlockDoublePlant.VARIANT, BlockDoublePlant.EnumPlantType.GRASS);
    protected final IBlockState grassUp = Blocks.DOUBLE_PLANT.getDefaultState().withProperty(BlockDoublePlant.VARIANT, BlockDoublePlant.EnumPlantType.GRASS).withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER);

	/**For the DecorateLakesGenerator. What plants will be planted instead of the remote water. Shores decoration.*/
	public boolean placePlant(World world, BlockPos pos, Random rand) {
		if (rand.nextInt(10) == 0 && Blocks.TALLGRASS.canBlockStay(world, pos, grass))
			return world.setBlockState(pos, grass, 2);
		else if (this.isSnowyBiome() && world.canSeeSky(pos.add(0, MistWorld.seaLevelUp - pos.getY(), 0)))
			return world.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState(), 2);
		return false;
	}

	@Override
	public void addDefaultFlowers() { addFlower(null, 100); }

	public NoiseGeneratorPerlin getGrassNoise() {
		return Biome.GRASS_COLOR_NOISE;
	}
}
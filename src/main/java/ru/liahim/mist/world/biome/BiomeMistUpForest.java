package ru.liahim.mist.world.biome;

import java.util.ArrayList;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenTallGrass;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.biome.EnumBiomeType;
import ru.liahim.mist.api.biome.MistBiomes;
import ru.liahim.mist.api.block.IWettable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.MistDirt;
import ru.liahim.mist.block.MistTreeTrunk;
import ru.liahim.mist.block.upperplant.MistMushroom;
import ru.liahim.mist.block.upperplant.MistMycelium;
import ru.liahim.mist.entity.EntityHorb;
import ru.liahim.mist.entity.EntityMonk;
import ru.liahim.mist.entity.EntityForestRunner;
import ru.liahim.mist.entity.EntityForestSpider;
import ru.liahim.mist.world.MistWorld;

public class BiomeMistUpForest extends BiomeMistUp {

	private IBlockState sand = MistBlocks.SAND.getDefaultState().withProperty(IWettable.WET, true);
	public int treePerChunk;

	public BiomeMistUpForest(BiomeProperties properties, int treePerChunk) {
		super(properties);
		this.topBlock = MistBlocks.GRASS_F.getDefaultState().withProperty(MistDirt.HUMUS, 2);
		this.secondTopBlock = MistBlocks.DIRT_F.getDefaultState().withProperty(MistDirt.HUMUS, 1);
		this.fillerBlock = MistBlocks.DIRT_F.getDefaultState();
		this.treePerChunk = treePerChunk;
		this.getMistBiomeDecorator().grassPerChunk = 5;
		this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityMonk.class, 8, 0, 2));
		this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityForestRunner.class, 20, 1, 4));
		this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityHorb.class, 10, 1, 3));
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityForestSpider.class, 20, 2, 4));
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntitySkeleton.class, 5, 0, 1));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getGrassColorAtPos(BlockPos pos) {
		return 0x91af28; //dense 0x96aa28
	}

	@Override
	public float getSpawningChance() {
		return 0.025F;
	}

	@Override
	public EnumBiomeType getBiomeType() {
		return EnumBiomeType.Forest;
	}

	@Override
	public void decorate(World world, Random rand, BlockPos pos) {
		double spruseNoise = 0;
		double birchNoise = 0;
		if (this.treePerChunk > 0) {
			spruseNoise = TREE_NOISE.getValue(pos.getX() * 0.003D, pos.getZ() * 0.003D);
			birchNoise = TREE_NOISE.getValue(pos.getX() * 0.002D, pos.getZ() * 0.002D);
		}
		if (birchNoise < -0.5D) {
			int j = (int)((-0.4D - birchNoise) * 10);
			for (int i = 0; i < j; ++i) {
	            int rx = rand.nextInt(16) + 8;
	            int rz = rand.nextInt(16) + 8;
	            BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
	            int k = rand.nextInt(3);
	            if (k == 0) minRockGen.generate(world, rand, randPos);
	            else if (k == 1) medRockGen.generate(world, rand, randPos);
	            else maxRockGen.generate(world, rand, randPos);
			}
		}
		if ((spruseNoise < -0.6D && rand.nextFloat() < 0.1F) || rand.nextFloat() < 0.01F) {
			int rx = rand.nextInt(16) + 8;
			int rz = rand.nextInt(16) + 8;
			BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
			((MistTreeTrunk) MistBlocks.SPRUSE_TRUNK).generateTrunk(world, randPos, rand);
		}
		if (spruseNoise > -0.6D) {
			if ((birchNoise > 0.65D && rand.nextFloat() < 0.03F) || rand.nextFloat() < 0.02F) {
				int rx = rand.nextInt(16) + 8;
				int rz = rand.nextInt(16) + 8;
				BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
				((MistTreeTrunk) MistBlocks.BIRCH_TRUNK).generateTrunk(world, randPos, rand);
			}
			if (birchNoise < 0.65D && rand.nextFloat() < 0.02F) {
				int rx = rand.nextInt(16) + 8;
				int rz = rand.nextInt(16) + 8;
				BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
				((MistTreeTrunk) MistBlocks.OAK_TRUNK).generateTrunk(world, randPos, rand);
			}
		}
		super.decorate(world, rand, pos);
		if (spruseNoise < -0.6D) {
			for (int i = 0; i < this.treePerChunk; ++i) {
	            int rx = rand.nextInt(16) + 8;
	            int rz = rand.nextInt(16) + 8;
	            BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
	            if (rand.nextInt(32) == 0) {
	            	if (rand.nextBoolean()) ((MistTreeTrunk)MistBlocks.OAK_TRUNK).generateTree(world, randPos, rand);
	            	else ((MistTreeTrunk)MistBlocks.BIRCH_TRUNK).generateTree(world, randPos, rand);
	            } else ((MistTreeTrunk)MistBlocks.SPRUSE_TRUNK).generateTree(world, randPos, rand);
			}
			if (rand.nextInt(16) == 0) {
				if (rand.nextInt(4) == 0) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_1.getDefaultState().withProperty(MistMushroom.TYPE_1, MistMushroom.MushroomType_1.BEIGE), rand);
				else MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_1.getDefaultState().withProperty(MistMushroom.TYPE_1, MistMushroom.MushroomType_1.SPOT), rand);
			}
			if (rand.nextInt(24) == 0) {
				if (rand.nextInt(4) == 0) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.ORANGE), rand);
				else MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.BROWN), rand);
			}
		} else if (birchNoise > 0.65D) {
			for (int i = 0; i < this.treePerChunk; ++i) {
	            int rx = rand.nextInt(16) + 8;
	            int rz = rand.nextInt(16) + 8;
	            BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
	            if (rand.nextInt(64) == 0) ((MistTreeTrunk)MistBlocks.OAK_TRUNK).generateTree(world, randPos, rand);
	            else ((MistTreeTrunk)MistBlocks.BIRCH_TRUNK).generateTree(world, randPos, rand);
			}
			if (rand.nextInt(16) == 0) {
				if (rand.nextInt(3) == 0) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_1.getDefaultState().withProperty(MistMushroom.TYPE_1, MistMushroom.MushroomType_1.SPOT), rand);
				else MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_1.getDefaultState().withProperty(MistMushroom.TYPE_1, MistMushroom.MushroomType_1.COPPER), rand);
			}
			if (rand.nextInt(24) == 0) {
				int i = rand.nextInt(6);
				if (i < 3) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.RED), rand);
				else if (i < 5) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.ORANGE), rand);
				else MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.PINK), rand);
			}
		} else {
			int j = this.treePerChunk;
	        if (rand.nextFloat() < this.getMistBiomeDecorator().extraTreeChance) ++j;
	        for (int i = 0; i < j; ++i) {
	            int rx = rand.nextInt(16) + 8;
	            int rz = rand.nextInt(16) + 8;
	            BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
	            if (rand.nextInt(6) > 0)
	            	((MistTreeTrunk)MistBlocks.OAK_TRUNK).generateTree(world, randPos, rand);
	            else ((MistTreeTrunk)MistBlocks.BIRCH_TRUNK).generateTree(world, randPos, rand);
	        }
	        if (this.treePerChunk > 0 && rand.nextFloat() < 0.03) {
		        j = rand.nextInt(3);
	        	for (int i = 0; i < j; ++i) {
	        		int rx = rand.nextInt(16) + 8;
		            int rz = rand.nextInt(16) + 8;
	                BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
	                ((MistTreeTrunk)MistBlocks.SPRUSE_TRUNK).generateTree(world, randPos, rand);
		        }
	        }
	        if (this.treePerChunk > 0) {
		        boolean dense = this == MistBiomes.upDenseForest;
		        if (rand.nextInt(dense ? 16 : 24) == 0) {
					if (rand.nextBoolean()) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_1.getDefaultState().withProperty(MistMushroom.TYPE_1, MistMushroom.MushroomType_1.SPOT), rand);
					else MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_1.getDefaultState().withProperty(MistMushroom.TYPE_1, MistMushroom.MushroomType_1.BEIGE), rand);
				}
				if (rand.nextInt(dense ? 24 : 32) == 0) {
					int i = rand.nextInt(8);
					if (i < 3) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.ORANGE), rand);
					else if (i < 5) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.PINK), rand);
					else MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.RED), rand);
				}
	        }
		}
	}

	@Override
	@Nullable
	public ArrayList<Double> getNoises(int x, int z) {
		ArrayList<Double> noises = new ArrayList<Double>();
		double sandNoise = SAND_NOISE.getValue(x * 0.02D, z * 0.02D);
		sandNoise = sandNoise + SAND_NOISE.getValue(x * 0.2D, z * 0.2D) * 0.2D;
		double clayNoise = CLAY_NOISE.getValue(x * 0.01D, z * 0.01D);
		noises.add(sandNoise); //0
		noises.add(clayNoise); //1
		double gravelNoise = GRAVEL_NOISE.getValue(x * 0.05D, z * 0.05D);
		gravelNoise = gravelNoise + GRAVEL_NOISE.getValue(x * 0.3D, z * 0.3D) * 0.3D;
		noises.add(gravelNoise); //2
		return noises;
	}

	@Override
	public IBlockState getTopBlock(Random rand, @Nullable ArrayList<Double> noises) {
		if (noises.get(1) > 0.7) return this.topBlock;
		return noises.get(0) > 0.6 ? MistBlocks.GRASS_S.getDefaultState().withProperty(MistDirt.HUMUS, 1) : this.topBlock;
	}

	@Override
	public IBlockState getSecondTopBlock(Random rand, @Nullable ArrayList<Double> noises) {
		double sandNoise = noises.get(0);
		double clayNoise = noises.get(1);
		if (noises.get(2) > 0.7D && sandNoise <= 0.6D) return MistWorld.gravelBlock;
		if (clayNoise > 0.7D) return sandNoise < 0.6D ? MistBlocks.DIRT_C.getDefaultState().withProperty(MistDirt.HUMUS, 1) : this.secondTopBlock;
		return sandNoise > 0.6D ? this.sand : sandNoise > 0.5D ? MistBlocks.DIRT_S.getDefaultState().withProperty(MistDirt.HUMUS, 0) : this.secondTopBlock;
	}

	@Override
	public IBlockState getFillerBlock(Random rand, @Nullable ArrayList<Double> noises) {
		double sandNoise = noises.get(0);
		double clayNoise = noises.get(1);
		if (noises.get(2) > 0.5D && sandNoise <= 0.5D) return MistWorld.gravelBlock;
		if (clayNoise > 0.7D) return MistBlocks.CLAY.getDefaultState();
		else if (clayNoise > 0.6D) return clayNoise > 0.65D || sandNoise < 0.5D ? MistBlocks.DIRT_C.getDefaultState().withProperty(MistDirt.HUMUS, 0) : this.fillerBlock;
		return sandNoise > 0.5D ? this.sand : sandNoise > 0.4D ? MistBlocks.DIRT_S.getDefaultState().withProperty(MistDirt.HUMUS, 0) : this.fillerBlock;
	}

	@Override
	protected IBlockState getBottom(Random rand, @Nullable ArrayList<Double> noises) {
		IBlockState state = super.getBottom(rand, noises);
		return noises.get(2) > 0.6D && state.getBlock() != MistBlocks.SAND ? MistWorld.gravelBlock : state;
	}

	@Override
	protected IBlockState getSecondBottom(Random rand, @Nullable ArrayList<Double> noises) {
		IBlockState state = super.getBottom(rand, noises);
		return noises.get(2) > 0.5D && state.getBlock() != MistBlocks.SAND ? MistWorld.gravelBlock : state;
	}

	@Override
	public WorldGenerator getRandomWorldGenForGrass(Random rand) {
		if (this == MistBiomes.upDenseForest)
			return new WorldGenTallGrass(rand.nextInt(4) == 0 ? BlockTallGrass.EnumType.FERN : BlockTallGrass.EnumType.GRASS);
		return super.getRandomWorldGenForGrass(rand);
	}

	@Override
	public void addDefaultFlowers() {
		addFlower(Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.FERN), 20);
	}
}
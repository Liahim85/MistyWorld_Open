package ru.liahim.mist.world.biome;

import java.util.ArrayList;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.biome.EnumBiomeType;
import ru.liahim.mist.api.biome.MistBiomes;
import ru.liahim.mist.api.block.IWettable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.MistClay;
import ru.liahim.mist.block.MistDirt;
import ru.liahim.mist.block.MistTreeTrunk;
import ru.liahim.mist.block.upperplant.MistMushroom;
import ru.liahim.mist.block.upperplant.MistMycelium;
import ru.liahim.mist.entity.EntityHulter;
import ru.liahim.mist.entity.EntityMomo;
import ru.liahim.mist.entity.EntitySniff;
import ru.liahim.mist.entity.EntityWoodlouse;
import ru.liahim.mist.world.MistWorld;

public class BiomeMistUpJungle extends BiomeMistUp {

	protected final int mistTropicTreePerChunk;
	private IBlockState redSand = MistBlocks.SAND.getDefaultState().withProperty(BlockSand.VARIANT, BlockSand.EnumType.RED_SAND).withProperty(IWettable.WET, true);

	public BiomeMistUpJungle(BiomeProperties properties, int treePerChunk) {
		super(properties);
		this.topBlock = MistBlocks.GRASS_T.getDefaultState().withProperty(MistDirt.HUMUS, 2);
		this.secondTopBlock = MistBlocks.DIRT_T.getDefaultState().withProperty(MistDirt.HUMUS, 1);
		this.fillerBlock = MistBlocks.DIRT_T.getDefaultState().withProperty(MistDirt.HUMUS, 0);
		this.mistTropicTreePerChunk = treePerChunk;
		getMistBiomeDecorator().grassPerChunk = 5;
		this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityMomo.class, 25, 2, 5));
		this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntitySniff.class, 10, 1, 2));
		this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityHulter.class, 5, 1, 3));
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityWoodlouse.class, 20, 2, 4));
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntitySkeleton.class, 5, 0, 1));
	}

	@Override
	public float getSpawningChance() {
		return 0.03F;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getGrassColorAtPos(BlockPos pos) {
		//double d = 0.3D; //0.01D
		//double d0 = BasementsGen.BASE_NOISE.getValue(pos.getX() * 0.01D * d, pos.getZ() * 0.01D * d);
		//return d0 <= 1.0D - 0.7D * d ? 0x67a847 : 0xff0000; //0.5D
		double d0 = GRASS_COLOR_NOISE.getValue(pos.getX() * 0.2D, pos.getZ() * 0.2D);
		return d0 < -0.1D ? 0x67a847 : 0x4e8f3c;
	}

	@Override
	public EnumBiomeType getBiomeType() {
		return EnumBiomeType.Jungle;
	}

	@Override
	public void decorate(World world, Random rand, BlockPos pos) {
		/*double treeNoise = TREE_NOISE.getValue(pos.getX() * 0.005D, pos.getZ() * 0.005D);
        if (treeNoise < -0.5D) {
        	int j = (int)((-0.4D - treeNoise) * 10);
			for (int i = 0; i < j; ++i) {
	            int rx = rand.nextInt(16) + 8;
	            int rz = rand.nextInt(16) + 8;
	            BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
	            int k = rand.nextInt(3);
	            if (k == 0) this.minRockGen.generate(world, rand, randPos);
	            else if (k == 1) this.medRockGen.generate(world, rand, randPos);
	            else this.maxRockGen.generate(world, rand, randPos);
			}
		}*/
		if (rand.nextFloat() < 0.02F) {
			int rx = rand.nextInt(16) + 8;
			int rz = rand.nextInt(16) + 8;
			BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
			if (rand.nextFloat() < 0.01F) {
				((MistTreeTrunk)MistBlocks.R_TREE_TRUNK).generateTrunk(world, randPos, rand);
			} else ((MistTreeTrunk) MistBlocks.T_TREE_TRUNK).generateTrunk(world, randPos, rand);
		}
		super.decorate(world, rand, pos);
		if (this == MistBiomes.upJungle && TREE_NOISE.getValue(pos.getX() * 0.005D, pos.getZ() * 0.005D) > 0.7D) {
        	int k = rand.nextInt(rand.nextInt(4) + 1) + 1;
        	for (int i = 0; i < k; ++i) {
                int rx = rand.nextInt(16) - 8;
                int rz = rand.nextInt(16) - 8;
                BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
                ((MistTreeTrunk)MistBlocks.R_TREE_TRUNK).generateTree(world, randPos, rand);
            }
        	if (rand.nextFloat() < 0.02F) {
    			int rx = rand.nextInt(16) + 8;
    			int rz = rand.nextInt(16) + 8;
    			BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
    			((MistTreeTrunk)MistBlocks.R_TREE_TRUNK).generateTrunk(world, randPos, rand);
    		}
        }
		int j = this.mistTropicTreePerChunk;
		if (rand.nextFloat() < this.getMistBiomeDecorator().extraTreeChance) ++j;
		for (int i = 0; i < j; ++i) {
			int rx = rand.nextInt(16);
			int rz = rand.nextInt(16);
			BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
			if (rand.nextFloat() < 0.01F) {
				((MistTreeTrunk)MistBlocks.R_TREE_TRUNK).generateTree(world, randPos, rand);
			} else ((MistTreeTrunk) MistBlocks.T_TREE_TRUNK).generateTree(world, randPos, rand);
		}
		if (this.mistTropicTreePerChunk > 0) {
			if (this == MistBiomes.upJungleEdge) {
				if (rand.nextInt(8) == 0) {
					int i = rand.nextInt(8);
					if (i < 6) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_1.getDefaultState().withProperty(MistMushroom.TYPE_1, MistMushroom.MushroomType_1.CUP), rand);
					else if (i < 7) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_1.getDefaultState().withProperty(MistMushroom.TYPE_1, MistMushroom.MushroomType_1.SILVER), rand);
				}
				if (rand.nextInt(16) == 0) {
					int i = rand.nextInt(6);
					if (i < 5) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.CORAL), rand);
					else MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.PURPLE), rand);
				}
			} else {
				if (rand.nextInt(16) == 0) {
					int i = rand.nextInt(8);
					if (i < 3) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_1.getDefaultState().withProperty(MistMushroom.TYPE_1, MistMushroom.MushroomType_1.AZURE), rand);
					else if (i < 5) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_1.getDefaultState().withProperty(MistMushroom.TYPE_1, MistMushroom.MushroomType_1.SILVER), rand);
					else if (i < 7) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_1.getDefaultState().withProperty(MistMushroom.TYPE_1, MistMushroom.MushroomType_1.CUP), rand);
					else MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_1.getDefaultState().withProperty(MistMushroom.TYPE_1, MistMushroom.MushroomType_1.GOLD), rand, 130);
				}
				if (rand.nextInt(24) == 0) {
					int i = rand.nextInt(8);
					if (i < 4) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.BLUE), rand);
					else if (i < 7) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.CORAL), rand);
					else MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.PURPLE), rand);
				}
			}
		}
	}


	@Override
	@Nullable
	public ArrayList<Double> getNoises(int x, int z) {
		ArrayList<Double> noises = new ArrayList<Double>();
		double sandNoise = SAND_NOISE.getValue(x * 0.03D, z * 0.03D);
		sandNoise = sandNoise + SAND_NOISE.getValue(x * 0.2D, z * 0.2D) * 0.2D;
		double clayNoise = CLAY_NOISE.getValue(x * 0.01D, z * 0.01D);
		clayNoise = clayNoise + CLAY_NOISE.getValue(x * 0.3D, z * 0.3D) * 0.02D;
		noises.add(sandNoise);
		noises.add(clayNoise);
		return noises;
	}

	@Override
	public IBlockState getTopBlock(Random rand, @Nullable ArrayList<Double> noises) {
		if (noises.get(1) > 0.6D) return this.topBlock;
		return noises.get(0) > 0.5D ? MistBlocks.GRASS_T.getDefaultState().withProperty(MistDirt.HUMUS, 1) : this.topBlock;
	}

	@Override
	public IBlockState getSecondTopBlock(Random rand, @Nullable ArrayList<Double> noises) {
		if (noises.get(1) > 0.6D) return this.secondTopBlock;
		double sandNoise = noises.get(0);
		return sandNoise > 0.5D ? this.redSand : sandNoise > 0.4 ? MistBlocks.DIRT_T.getDefaultState().withProperty(MistDirt.HUMUS, 0) : this.secondTopBlock;
	}

	@Override
	public IBlockState getFillerBlock(Random rand, @Nullable ArrayList<Double> noises) {
		if (noises.get(1) > 0.6D) return MistBlocks.CLAY.getDefaultState().withProperty(MistClay.VARIANT, MistClay.EnumClayType.RED_CLAY);
		return noises.get(0) > 0.4D ? this.redSand : this.fillerBlock;
	}

	@Override
	protected IBlockState getBottom(Random rand, @Nullable ArrayList<Double> noises) {
		if (noises.get(1) > 0.7D) return MistBlocks.CLAY.getDefaultState().withProperty(MistClay.VARIANT, MistClay.EnumClayType.RED_CLAY);
		double sandNoise = noises.get(0);
		return sandNoise > 0.5D ? this.redSand : sandNoise > 0.4 ? MistBlocks.DIRT_T.getDefaultState().withProperty(MistDirt.HUMUS, 0) : this.secondTopBlock;
	}

	@Override
	protected IBlockState getZeroBlock(Random rand, @Nullable ArrayList<Double> noises) {
		return noises.get(0) < -0.5D ? MistWorld.gravelBlock : null;
	}

	@Override
	public void addDefaultFlowers() {
		addFlower(Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.FERN), 20);
	}
}
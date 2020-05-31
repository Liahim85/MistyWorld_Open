package ru.liahim.mist.world.biome;

import java.util.ArrayList;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.biome.EnumBiomeType;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.MistDirt;
import ru.liahim.mist.block.MistTreeTrunk;
import ru.liahim.mist.block.upperplant.MistMushroom;
import ru.liahim.mist.block.upperplant.MistMycelium;
import ru.liahim.mist.entity.EntityBrachiodon;
import ru.liahim.mist.entity.EntityPrickler;
import ru.liahim.mist.entity.EntityWulder;
import ru.liahim.mist.entity.EntitySnowFlea;
import ru.liahim.mist.world.MistWorld;

public class BiomeMistUpColdBase extends BiomeMistUp {
	
	protected final int pinePerChunk;
	protected final float snowTreeChance;
	protected final boolean genSTree;
	protected final double gravelOffset;

	public BiomeMistUpColdBase(BiomeProperties properties, int pinePerChunk, float snowTreeChance, boolean genSTree) {
		super(properties);
		this.topBlock = MistBlocks.GRASS_R.getDefaultState().withProperty(MistDirt.HUMUS, 2);
		this.secondTopBlock = MistBlocks.DIRT_R.getDefaultState().withProperty(MistDirt.HUMUS, 1);
		this.fillerBlock = MistBlocks.DIRT_R.getDefaultState();
		this.pinePerChunk = pinePerChunk;
		this.snowTreeChance = snowTreeChance;
		this.genSTree = genSTree;
		this.gravelOffset = pinePerChunk > 0 ? 0.0D : 0.3D;
		if (genSTree) this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityWulder.class, 10, 1, 4));
		if (pinePerChunk <= 0) this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityBrachiodon.class, 5, 3, 5));
		else this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityPrickler.class, 20, 1, 3));
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntitySnowFlea.class, 20, 2, 4));
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntitySkeleton.class, 5, 0, 1));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getGrassColorAtPos(BlockPos pos) {
		return 0x737850;
	}

	@Override
	public float getSpawningChance() {
		return this.pinePerChunk <= 0 ? 0.005F : 0.02F;
	}

	@Override
	public EnumBiomeType getBiomeType() {
		return EnumBiomeType.Cold;
	}

	@Override
	public void decorate(World world, Random rand, BlockPos pos) {
        double treeNoise = TREE_NOISE.getValue(pos.getX() * 0.005D, pos.getZ() * 0.005D);
        if ((this.pinePerChunk > 0 && treeNoise < -0.4D) || treeNoise > 0.4D) {
			int j = (int)((Math.abs(treeNoise) - 0.3D) * 10);
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
        if (this.pinePerChunk > 0 || (this.pinePerChunk == 0 && treeNoise > 0.6D)) {
			if (this.snowTreeChance > 0.0D && treeNoise < -0.6D) {
				if (rand.nextFloat() < 0.1F) {
					int rx = rand.nextInt(16) + 8;
					int rz = rand.nextInt(16) + 8;
					BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
					((MistTreeTrunk) MistBlocks.SPRUSE_TRUNK).generateTrunk(world, randPos, rand, true);
				}
			} else if (rand.nextFloat() < 0.05F) {
				int rx = rand.nextInt(16) + 8;
				int rz = rand.nextInt(16) + 8;
				BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
				((MistTreeTrunk) MistBlocks.PINE_TRUNK).generateTrunk(world, randPos, rand, true);
			}
		}
        if (this.pinePerChunk > 0 && snowTreeChance > 0 && rand.nextFloat() < 0.02F) {
			int rx = rand.nextInt(16) + 8;
			int rz = rand.nextInt(16) + 8;
			BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
			((MistTreeTrunk) MistBlocks.SNOW_TRUNK).generateTrunk(world, randPos, rand, true);
		}
        super.decorate(world, rand, pos);
        if (this.pinePerChunk > 0) {
	        for (int i = 0; i < this.pinePerChunk; ++i) {
	            int rx = rand.nextInt(16) + 8;
	            int rz = rand.nextInt(16) + 8;
	            BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
	            if (this.snowTreeChance > 0.0D && treeNoise < -0.6D && rand.nextFloat() > 1 + treeNoise){
	            	((MistTreeTrunk)MistBlocks.SPRUSE_TRUNK).generateTree(world, randPos, rand, true);
	            } else {
	            	if (rand.nextFloat() < this.snowTreeChance) {
	            		((MistTreeTrunk)MistBlocks.SNOW_TRUNK).generateTree(world, randPos, rand, true);
	            	} else ((MistTreeTrunk)MistBlocks.PINE_TRUNK).generateTree(world, randPos, rand, true);
	            }
	        }
	        if (treeNoise < -0.6D) {
	        	if (rand.nextInt(16) == 0) {
					MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_1.getDefaultState().withProperty(MistMushroom.TYPE_1, MistMushroom.MushroomType_1.SPOT), rand);
				}
				if (rand.nextInt(32) == 0) {
					if (rand.nextInt(4) == 0) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.ORANGE), rand);
					else MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.BROWN), rand);
				}
	        } else if (this.snowTreeChance > 0) {
	        	if (rand.nextInt(24) == 0) {
					if (rand.nextBoolean()) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_1.getDefaultState().withProperty(MistMushroom.TYPE_1, MistMushroom.MushroomType_1.SPOT), rand);
					else MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_1.getDefaultState().withProperty(MistMushroom.TYPE_1, MistMushroom.MushroomType_1.LILAC), rand);
				}
				if (rand.nextInt(48) == 0) {
					int i = rand.nextInt(6);
					if (i < 3) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.BLACK), rand);
					else if (i < 5) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.ORANGE), rand);
					else MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.BROWN), rand);
				}
	        } else {
	        	if (rand.nextInt(24) == 0) {
					if (rand.nextBoolean()) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_1.getDefaultState().withProperty(MistMushroom.TYPE_1, MistMushroom.MushroomType_1.SPOT), rand);
					else MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_1.getDefaultState().withProperty(MistMushroom.TYPE_1, MistMushroom.MushroomType_1.LILAC), rand);
				}
	        	if (rand.nextInt(48) == 0) {
					int i = rand.nextInt(8);
					if (i < 4) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.BLACK), rand);
					else if (i < 7) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.ORANGE), rand);
					else MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.BROWN), rand);
				}
	        }
        } else if (this.pinePerChunk == 0) {
        	if (treeNoise > 0.6D) {
            	int j = rand.nextInt(3) + 2;
            	for (int i = 0; i < j; ++i) {
                    int rx = rand.nextInt(16) - 8;
                    int rz = rand.nextInt(16) - 8;
                    BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
                    ((MistTreeTrunk)MistBlocks.PINE_TRUNK).generateTree(world, randPos, rand, true);
                }
            	if (rand.nextInt(24) == 0) {
					if (rand.nextBoolean()) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_1.getDefaultState().withProperty(MistMushroom.TYPE_1, MistMushroom.MushroomType_1.SPOT), rand);
					else MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_1.getDefaultState().withProperty(MistMushroom.TYPE_1, MistMushroom.MushroomType_1.LILAC), rand);
				}
            	if (rand.nextInt(48) == 0) {
					int i = rand.nextInt(12);
					if (i < 6) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.BLACK), rand);
					else if (i < 11) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.ORANGE), rand);
					else MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.BROWN), rand);
				}
            }
        }
        if (this.genSTree && treeNoise > 0.8D) {
        	int j = rand.nextInt(rand.nextInt(4) + 1) + 1;
        	for (int i = 0; i < j; ++i) {
                int rx = rand.nextInt(16) - 8;
                int rz = rand.nextInt(16) - 8;
                BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
                ((MistTreeTrunk)MistBlocks.S_TREE_TRUNK).generateTree(world, randPos, rand, true);
            }
        }
    }

	/** 0 - Clay, 1 - Gravel */
	@Override
	@Nullable
	public ArrayList<Double> getNoises(int x, int z) {
		ArrayList<Double> noises = new ArrayList<Double>();
		double clayNoise = CLAY_NOISE.getValue(x * 0.01D, z * 0.01D);
		noises.add(clayNoise + CLAY_NOISE.getValue(x * 0.3D, z * 0.3D) * 0.02D); // 0
		double gravelNoise = GRAVEL_NOISE.getValue(x * 0.05D, z * 0.05D);
		noises.add(gravelNoise + GRAVEL_NOISE.getValue(x * 0.3D, z * 0.3D) * 0.3D); // 1
		return noises;
	}

	@Override
	public IBlockState getTopBlock(Random rand, @Nullable ArrayList<Double> noises) {
		return this.gravelOffset > 0.0D && noises.get(1) > 0.6D ? MistWorld.gravelBlock : this.topBlock;
	}

	@Override
	public IBlockState getSecondTopBlock(Random rand, @Nullable ArrayList<Double> noises) {
		if (noises.get(1) > 0.7D - this.gravelOffset) return MistWorld.gravelBlock;
		return noises.get(0) > 0.9D ? MistBlocks.CLAY.getDefaultState() : this.secondTopBlock;
	}

	@Override
	public IBlockState getFillerBlock(Random rand, @Nullable ArrayList<Double> noises) {
		if (noises.get(1) > 0.5D - this.gravelOffset) return MistWorld.gravelBlock;
		return noises.get(0) > 0.8D ? MistBlocks.CLAY.getDefaultState() : this.secondTopBlock;
	}

	@Override
	public IBlockState getZeroBlock(Random rand, @Nullable ArrayList<Double> noises) {
		return noises.get(1) > 0.2D - this.gravelOffset ? MistWorld.gravelBlock : null;
	}
}
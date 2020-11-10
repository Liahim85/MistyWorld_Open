package ru.liahim.mist.world.generators;

import java.util.Random;

import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import ru.liahim.mist.api.biome.EnumBiomeType;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.MistBlockSlabStone;
import ru.liahim.mist.block.MistCobblestone;
import ru.liahim.mist.block.MistMasonry;
import ru.liahim.mist.block.MistSand;
import ru.liahim.mist.block.MistStoneBrick;
import ru.liahim.mist.tileentity.TileEntityUrn.UrnLocation;
import ru.liahim.mist.tileentity.TileEntityUrn.UrnLootType;
import ru.liahim.mist.util.GenUtil;

public abstract class TombGenBase extends WorldGenerator {

	protected static IBlockState cobble = MistBlocks.COBBLESTONE.getDefaultState().withProperty(MistCobblestone.VARIANT, MistCobblestone.EnumType.NORMAL);
	protected static IBlockState cobbleMoss = MistBlocks.COBBLESTONE.getDefaultState().withProperty(MistCobblestone.VARIANT, MistCobblestone.EnumType.MOSSY);
	protected static IBlockState masonry = MistBlocks.MASONRY.getDefaultState().withProperty(MistMasonry.VARIANT, MistMasonry.EnumType.NORMAL);
	protected static IBlockState masonryMoss = MistBlocks.MASONRY.getDefaultState().withProperty(MistMasonry.VARIANT, MistMasonry.EnumType.MOSSY);
	protected static IBlockState cobbleStep = MistBlocks.COBBLESTONE_STEP.getDefaultState().withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP);
	protected static IBlockState cobbleStepMoss = MistBlocks.COBBLESTONE_MOSS_STEP.getDefaultState().withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP);
	protected static IBlockState cobbleSlab = MistBlocks.COBBLESTONE_SLAB.getDefaultState().withProperty(MistBlockSlabStone.VARIANT, MistBlockSlabStone.EnumType.NORMAL);
	protected static IBlockState cobbleSlabMoss = MistBlocks.COBBLESTONE_SLAB.getDefaultState().withProperty(MistBlockSlabStone.VARIANT, MistBlockSlabStone.EnumType.MOSSY);
	protected static IBlockState cobbleWall = MistBlocks.COBBLESTONE_WALL.getDefaultState();
	protected static IBlockState cobbleWallMoss = MistBlocks.COBBLESTONE_MOSS_WALL.getDefaultState();
	protected static IBlockState cobbleStairs = MistBlocks.COBBLESTONE_STAIRS.getDefaultState();
	protected static IBlockState cobbleStairsMoss = MistBlocks.COBBLESTONE_MOSS_STAIRS.getDefaultState();
	protected static IBlockState brick = MistBlocks.STONE_BRICK.getDefaultState().withProperty(MistStoneBrick.VARIANT, MistStoneBrick.EnumType.NORMAL);
	protected static IBlockState brickMoss = MistBlocks.STONE_BRICK.getDefaultState().withProperty(MistStoneBrick.VARIANT, MistStoneBrick.EnumType.MOSSY);
	protected static IBlockState brickStep = MistBlocks.STONE_BRICK_STEP.getDefaultState().withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP);
	protected static IBlockState brickStepMoss = MistBlocks.STONE_BRICK_MOSS_STEP.getDefaultState().withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP);
	protected static IBlockState brickSlab = MistBlocks.STONE_BRICK_SLAB.getDefaultState().withProperty(MistBlockSlabStone.VARIANT, MistBlockSlabStone.EnumType.NORMAL);
	protected static IBlockState brickSlabMoss = MistBlocks.STONE_BRICK_SLAB.getDefaultState().withProperty(MistBlockSlabStone.VARIANT, MistBlockSlabStone.EnumType.MOSSY);
	protected static IBlockState brickWall = MistBlocks.STONE_BRICK_WALL.getDefaultState();
	protected static IBlockState brickWallMoss = MistBlocks.STONE_BRICK_MOSS_WALL.getDefaultState();
	protected static IBlockState brickStairs = MistBlocks.STONE_BRICK_STAIRS.getDefaultState();
	protected static IBlockState brickStairsMoss = MistBlocks.STONE_BRICK_MOSS_STAIRS.getDefaultState();
	protected static IBlockState sand = MistBlocks.SAND.getDefaultState().withProperty(MistSand.VARIANT, BlockSand.EnumType.SAND).withProperty(MistSand.WET, false);
	protected static IBlockState redSandWet = MistBlocks.SAND.getDefaultState().withProperty(MistSand.VARIANT, BlockSand.EnumType.RED_SAND).withProperty(MistSand.WET, true);
	protected static IBlockState redSandDry = MistBlocks.SAND.getDefaultState().withProperty(MistSand.VARIANT, BlockSand.EnumType.RED_SAND).withProperty(MistSand.WET, false);

	protected static void placeUrn(GenUtil gen, BlockPos pos, EnumBiomeType biomeType, UrnLocation loc, Random rand, boolean air) {
		placeUrn(gen, pos, biomeType, loc, rand, air, 4);
	}

	protected static void placeUrn(GenUtil gen, BlockPos pos, EnumBiomeType biomeType, UrnLocation loc, Random rand, boolean air, int rarity) {
		if (rand.nextInt(rarity) == 0) {
			gen.setBlockState(pos, MistBlocks.URN.getDefaultState());
			UrnLootType.initializeType(gen.getTileEntity(pos), biomeType, loc, rand);
		} else placeWeb(gen, pos, rand, air);
	}

	protected static void placeWeb(GenUtil gen, BlockPos pos, Random rand, boolean air) {
		if (rand.nextInt(8) == 0) gen.setBlockState(pos, Blocks.WEB.getDefaultState());
		else if (air) gen.setBlockState(pos, Blocks.AIR.getDefaultState());
	}

	protected static boolean roomCheck(GenUtil gen, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, boolean airCheck) {
		BlockPos center = gen.set.center;
		BlockPos checkPos;
		for (int x = minX; x <= maxX; ++x) {
			for (int z = minZ; z <= maxZ; ++z) {
				for (int y = minY; y <= maxY; ++y) {
					if (x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ) {
						checkPos = gen.getPos(center.add(x, y, z));
						if ((airCheck && y == maxY && !gen.world.getBlockState(checkPos.up()).getMaterial().isReplaceable()) || !gen.world.isBlockNormalCube(checkPos, false)) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	protected static boolean roomCheck(World world, BlockPos center, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, boolean airCheck) {
		BlockPos checkPos;
		for (int x = minX; x <= maxX; ++x) {
			for (int z = minZ; z <= maxZ; ++z) {
				for (int y = minY; y <= maxY; ++y) {
					if (x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ) {
						checkPos = center.add(x, y, z);
						if ((airCheck && y == maxY && !world.getBlockState(checkPos.up()).getMaterial().isReplaceable()) || !world.isBlockNormalCube(checkPos, false)) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	protected static IBlockState getBlock(Type type, Random rand, int wet) {
		return type.getBlock(!(wet < 0 || (wet == 0 && rand.nextInt(4) == 0)));
	}

	protected static IBlockState getStep(Type type, EnumFacing face, boolean top, Random rand, int wet) {
		if (wet < 0 || (wet == 0 && rand.nextInt(4) == 0)) {
			if (top) return type.getStep(false).withProperty(BlockStairs.FACING, face).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM);
			else return type.getStep(false).withProperty(BlockStairs.FACING, face);
		} else if (top) return type.getStep(true).withProperty(BlockStairs.FACING, face).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM);
		else return type.getStep(true).withProperty(BlockStairs.FACING, face);
	}

	protected static IBlockState getWall(Type type, EnumFacing face, Random rand, int wet) {
		return getWall(type, face, false, rand, wet);
	}

	protected static IBlockState getWall(Type type, EnumFacing face, boolean top, Random rand, int wet) {
		if (wet < 0 || (wet == 0 && rand.nextInt(4) == 0)) {
			if (top) return type.getWall(false).withProperty(BlockStairs.FACING, face).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM);
			else return type.getWall(false).withProperty(BlockStairs.FACING, face);
		} else if (top) return type.getWall(true).withProperty(BlockStairs.FACING, face).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM);
		else return type.getWall(true).withProperty(BlockStairs.FACING, face);
	}

	protected static IBlockState getSlab(Type type, boolean top, Random rand, int wet) {
		if (wet < 0 || (wet == 0 && rand.nextInt(4) == 0)) {
			if (top) return type.getSlab(false).withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.TOP);
			else return type.getSlab(false);
		} if (top) return type.getSlab(true).withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.TOP);
		else return type.getSlab(true);
	}

	protected static IBlockState getStairs(Type type, EnumFacing face, boolean top, Random rand, int wet) {
		if (wet < 0 || (wet == 0 && rand.nextInt(4) == 0)) {
			if (top) return type.getStairs(false).withProperty(BlockStairs.FACING, face).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP);
			else return type.getStairs(false).withProperty(BlockStairs.FACING, face);
		} else if (top) return type.getStairs(true).withProperty(BlockStairs.FACING, face).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP);
		else return type.getStairs(true).withProperty(BlockStairs.FACING, face);
	}

	public static enum Type {

		COBBLE(cobble, cobbleMoss, cobbleStairs, cobbleStairsMoss, cobbleWall, cobbleWallMoss, cobbleSlab, cobbleSlabMoss, cobbleStep, cobbleStepMoss),
		BRICK(brick, brickMoss, brickStairs, brickStairsMoss, brickWall, brickWallMoss, brickSlab, brickSlabMoss, brickStep, brickStepMoss),
		MASONRY(masonry, masonryMoss, brickStairs, brickStairsMoss, brickWall, brickWallMoss, brickSlab, brickSlabMoss, brickStep, brickStepMoss);

		final IBlockState block;
		final IBlockState blockMoss;
		final IBlockState stairs;
		final IBlockState stairsMoss;
		final IBlockState wall;
		final IBlockState wallMoss;
		final IBlockState slab;
		final IBlockState slabMoss;
		final IBlockState step;
		final IBlockState stepMoss;

		Type(	IBlockState block,
				IBlockState blockMoss,
				IBlockState stairs,
				IBlockState stairsMoss,
				IBlockState wall,
				IBlockState wallMoss,
				IBlockState slab,
				IBlockState slabMoss,
				IBlockState step,
				IBlockState stepMoss) {

			this.block = block;
			this.blockMoss = blockMoss;
			this.stairs = stairs;
			this.stairsMoss = stairsMoss;
			this.wall = wall;
			this.wallMoss = wallMoss;
			this.slab = slab;
			this.slabMoss = slabMoss;
			this.step = step;
			this.stepMoss = stepMoss;
		}

		public IBlockState getBlock(boolean moss) {
			return moss ? this.blockMoss : this.block;
		}

		public IBlockState getStairs(boolean moss) {
			return moss ? this.stairsMoss : this.stairs;
		}

		public IBlockState getWall(boolean moss) {
			return moss ? this.wallMoss : this.wall;
		}

		public IBlockState getSlab(boolean moss) {
			return moss ? this.slabMoss : this.slab;
		}

		public IBlockState getStep(boolean moss) {
			return moss ? this.stepMoss : this.step;
		}
	}
}
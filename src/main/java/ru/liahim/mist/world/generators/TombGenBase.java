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
import ru.liahim.mist.block.MistSand;
import ru.liahim.mist.tileentity.TileEntityUrn.UrnLocation;
import ru.liahim.mist.tileentity.TileEntityUrn.UrnLootType;
import ru.liahim.mist.util.GenUtil;

public abstract class TombGenBase extends WorldGenerator {

	protected static IBlockState cobble = MistBlocks.COBBLESTONE.getDefaultState().withProperty(MistCobblestone.VARIANT, MistCobblestone.EnumType.NORMAL);
	protected static IBlockState cobbleMoss = MistBlocks.COBBLESTONE.getDefaultState().withProperty(MistCobblestone.VARIANT, MistCobblestone.EnumType.MOSSY);
	protected static IBlockState step = MistBlocks.COBBLESTONE_STEP.getDefaultState().withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP);
	protected static IBlockState stepMoss = MistBlocks.COBBLESTONE_MOSS_STEP.getDefaultState().withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP);
	protected static IBlockState slab = MistBlocks.COBBLESTONE_SLAB.getDefaultState().withProperty(MistBlockSlabStone.VARIANT, MistBlockSlabStone.EnumType.NORMAL);
	protected static IBlockState slabMoss = MistBlocks.COBBLESTONE_SLAB.getDefaultState().withProperty(MistBlockSlabStone.VARIANT, MistBlockSlabStone.EnumType.MOSSY);
	protected static IBlockState stairs = MistBlocks.COBBLESTONE_STAIRS.getDefaultState();
	protected static IBlockState stairsMoss = MistBlocks.COBBLESTONE_MOSS_STAIRS.getDefaultState();
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

	protected static IBlockState getCobble(Random rand, int wet) {
		if (wet < 0 || (wet == 0 && rand.nextInt(4) == 0)) return cobble;
		else return cobbleMoss;
	}

	protected static IBlockState getStep(EnumFacing face, boolean top, Random rand, int wet) {
		if (wet < 0 || (wet == 0 && rand.nextInt(4) == 0)) {
			if (top) return step.withProperty(BlockStairs.FACING, face).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM);
			else return step.withProperty(BlockStairs.FACING, face);
		} else if (top) return stepMoss.withProperty(BlockStairs.FACING, face).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM);
		else return stepMoss.withProperty(BlockStairs.FACING, face);
	}

	protected static IBlockState getSlab(boolean top, Random rand, int wet) {
		if (wet < 0 || (wet == 0 && rand.nextInt(4) == 0)) {
			if (top) return slab.withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.TOP);
			else return slab;
		} if (top) return slabMoss.withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.TOP);
		else return slabMoss;
	}

	protected static IBlockState getStairs(EnumFacing face, boolean top, Random rand, int wet) {
		if (wet < 0 || (wet == 0 && rand.nextInt(4) == 0)) {
			if (top) return stairs.withProperty(BlockStairs.FACING, face).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP);
			else return stairs.withProperty(BlockStairs.FACING, face);
		} else if (top) return stairsMoss.withProperty(BlockStairs.FACING, face).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP);
		else return stairsMoss.withProperty(BlockStairs.FACING, face);
	}
}
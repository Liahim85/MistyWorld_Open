package ru.liahim.mist.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.item.MistItems;

public class MistStoneBrick extends MistCobblestone {

	public MistStoneBrick() {
		super();
	}

	@Override
	@Nullable
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return MistItems.BRICK;
	}

	@Override
	public int quantityDropped(Random random) {
		return 4;
	}

	@Override
	public Block getFullBlock() {
		return this;
	}

	@Override
	public IBlockState getFullState(IBlockState state) {
		if (state.getBlock() == MistBlocks.STONE_BRICK_MOSS_STEP || state.getBlock() == MistBlocks.STONE_BRICK_MOSS_STAIRS || state.getBlock() == MistBlocks.STONE_BRICK_MOSS_WALL ||
				(state.getBlock() == MistBlocks.STONE_BRICK_SLAB && state.getValue(MistBlockSlabStone.VARIANT) == MistBlockSlabStone.EnumType.MOSSY))
			return this.getDefaultState().withProperty(VARIANT, EnumType.MOSSY);
		return this.getDefaultState();
	}

	@Override
	public Block getStepBlock(IBlockState state) {
		if (state.getBlock() == MistBlocks.STONE_BRICK_MOSS_WALL || state.getBlock() == MistBlocks.STONE_BRICK_MOSS_STAIRS ||
				(state.getBlock() == MistBlocks.STONE_BRICK_SLAB && state.getValue(MistBlockSlabStone.VARIANT) == MistBlockSlabStone.EnumType.MOSSY))
			return MistBlocks.STONE_BRICK_MOSS_STEP;
		return MistBlocks.STONE_BRICK_STEP;
	}

	@Override
	public Block getWallBlock(IBlockState state) {
		if (state.getBlock() == MistBlocks.STONE_BRICK_MOSS_STEP || state.getBlock() == MistBlocks.STONE_BRICK_MOSS_STAIRS) return MistBlocks.STONE_BRICK_MOSS_WALL;
		return MistBlocks.STONE_BRICK_WALL;
	}

	@Override
	public IBlockState getSlabBlock(IBlockState state) {
		if (state.getBlock() == MistBlocks.STONE_BRICK_MOSS_STEP || state.getBlock() == MistBlocks.STONE_BRICK_MOSS_STAIRS)
			return MistBlocks.STONE_BRICK_SLAB.getDefaultState().withProperty(MistBlockSlabStone.VARIANT, MistBlockSlabStone.EnumType.MOSSY);
		return MistBlocks.STONE_BRICK_SLAB.getDefaultState();
	}

	@Override
	public Block getStairsBlock(IBlockState state) {
		if (state.getBlock() == MistBlocks.STONE_BRICK_MOSS_STEP || (state.getBlock() == MistBlocks.STONE_BRICK && state.getValue(MistBlockMossy.VARIANT) == MistBlockMossy.EnumType.MOSSY)) return MistBlocks.STONE_BRICK_MOSS_STAIRS;
		return MistBlocks.STONE_BRICK_STAIRS;
	}
}
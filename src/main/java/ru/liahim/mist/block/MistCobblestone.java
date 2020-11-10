package ru.liahim.mist.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import ru.liahim.mist.api.block.IDividable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.item.MistItems;

public class MistCobblestone extends MistBlockMossy implements IDividable {

	public MistCobblestone() {
		super();
	}

	@Override
	@Nullable
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return MistItems.ROCKS;
	}

	@Override
	public int quantityDropped(Random random) {
		return random.nextInt(10) == 0 ? 3 : 4;
	}

	@Override
	public Block getFullBlock() {
		return this;
	}

	@Override
	public IBlockState getFullState(IBlockState state) {
		if (state.getBlock() == MistBlocks.COBBLESTONE_MOSS_STEP || state.getBlock() == MistBlocks.COBBLESTONE_MOSS_STAIRS || state.getBlock() == MistBlocks.COBBLESTONE_MOSS_WALL ||
				(state.getBlock() == MistBlocks.COBBLESTONE_SLAB && state.getValue(MistBlockSlabStone.VARIANT) == MistBlockSlabStone.EnumType.MOSSY))
			return this.getDefaultState().withProperty(VARIANT, EnumType.MOSSY);
		return this.getDefaultState();
	}

	@Override
	public Block getStepBlock(IBlockState state) {
		if (state.getBlock() == MistBlocks.COBBLESTONE_MOSS_WALL || state.getBlock() == MistBlocks.COBBLESTONE_MOSS_STAIRS ||
				(state.getBlock() == MistBlocks.COBBLESTONE_SLAB && state.getValue(MistBlockSlabStone.VARIANT) == MistBlockSlabStone.EnumType.MOSSY))
			return MistBlocks.COBBLESTONE_MOSS_STEP;
		return MistBlocks.COBBLESTONE_STEP;
	}

	@Override
	public Block getWallBlock(IBlockState state) {
		if (state.getBlock() == MistBlocks.COBBLESTONE_MOSS_STEP || state.getBlock() == MistBlocks.COBBLESTONE_MOSS_STAIRS) return MistBlocks.COBBLESTONE_MOSS_WALL;
		return MistBlocks.COBBLESTONE_WALL;
	}

	@Override
	public IBlockState getSlabBlock(IBlockState state) {
		if (state.getBlock() == MistBlocks.COBBLESTONE_MOSS_STEP || state.getBlock() == MistBlocks.COBBLESTONE_MOSS_STAIRS)
			return MistBlocks.COBBLESTONE_SLAB.getDefaultState().withProperty(MistBlockSlabStone.VARIANT, MistBlockSlabStone.EnumType.MOSSY);
		return MistBlocks.COBBLESTONE_SLAB.getDefaultState();
	}

	@Override
	public Block getStairsBlock(IBlockState state) {
		if (state.getBlock() == MistBlocks.COBBLESTONE_MOSS_STEP || (state.getBlock() == MistBlocks.COBBLESTONE && state.getValue(MistBlockMossy.VARIANT) == MistBlockMossy.EnumType.MOSSY)) return MistBlocks.COBBLESTONE_MOSS_STAIRS;
		return MistBlocks.COBBLESTONE_STAIRS;
	}
}
package ru.liahim.mist.block;

import ru.liahim.mist.api.block.IColoredBlock;
import ru.liahim.mist.init.BlockColoring;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MistBlockStairsColored extends MistBlockStairs implements IColoredBlock {

	@Override
	@SideOnly(Side.CLIENT)
	public IBlockColor getBlockColor() {
		return BlockColoring.GRASS_COLORING_1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IItemColor getItemColor() {
		return BlockColoring.BLOCK_ITEM_COLORING;
	}

	public MistBlockStairsColored(IBlockState modelState, boolean tick) {
		super(modelState, tick);
	}

	public MistBlockStairsColored(IBlockState modelState) {
		super(modelState, false);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		return layer == BlockRenderLayer.CUTOUT_MIPPED;
    }
}
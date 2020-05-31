package ru.liahim.mist.block;

import java.util.Random;

import javax.annotation.Nullable;

import ru.liahim.mist.api.item.MistItems;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;

public class MistMulchBlock extends MistBlock {

	public MistMulchBlock() {
		super(Material.WOOD, MapColor.BROWN);
		this.setHardness(0.5F);
		this.setSoundType(SoundType.GROUND);
		this.setHarvestLevel("shovel", 0);
	}

	@Override
	@Nullable
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return MistItems.MULCH;
	}

	@Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
		return 9;
    }
}
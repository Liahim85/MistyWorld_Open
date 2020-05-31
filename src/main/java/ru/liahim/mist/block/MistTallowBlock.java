package ru.liahim.mist.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ru.liahim.mist.api.item.MistItems;

public class MistTallowBlock extends MistBlock {

	public MistTallowBlock() {
		super(Material.CLAY, MapColor.WHITE_STAINED_HARDENED_CLAY);
		this.setHardness(0.5F);
		this.setSoundType(SoundType.SLIME);
		this.setHarvestLevel("shovel", 0);
	}

	@Override
	@Nullable
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return MistItems.TALLOW;
	}

	@Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
		return 4;
    }

	@Override
	public void onFallenUpon(World world, BlockPos pos, Entity entity, float fallDistance) {
		entity.fall(fallDistance, 0.7F);
	}
}
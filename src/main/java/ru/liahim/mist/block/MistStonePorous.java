package ru.liahim.mist.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import ru.liahim.mist.api.block.IMistStone;
import ru.liahim.mist.api.item.MistItems;

public class MistStonePorous extends MistBlock implements IMistStone {

	public MistStonePorous() {
		super(Material.ROCK);
		this.setHardness(5.0F);
		this.setResistance(20);
		this.setHarvestLevel("pickaxe", 0);
	}

	@Override
	public void getDrops(NonNullList ret, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		Random rand = world instanceof World ? ((World)world).rand : RANDOM;
		int count = quantityDropped(state, fortune, rand);
		for (int i = 0; i < count; i++) {
			Item item = this.getItemDropped(state, rand, fortune);
			if (item != null) {
				ret.add(new ItemStack(item, 1, this.damageDropped(state)));
			}
		}
		if (fortune > 3) fortune = 3;
		if (count < 3 && rand.nextInt(64 - fortune * 8) == 0) {
			ret.add(new ItemStack(Items.GOLD_NUGGET));
		}
	}

	@Override
	@Nullable
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return MistItems.ROCKS;
	}

	@Override
	public int quantityDropped(Random random) {
		return 1 + random.nextInt(3);
	}
}
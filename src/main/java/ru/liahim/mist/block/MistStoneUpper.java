package ru.liahim.mist.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import ru.liahim.mist.api.block.IMistStoneUpper;
import ru.liahim.mist.api.item.MistItems;

public class MistStoneUpper extends MistBlock implements IMistStoneUpper {

	public MistStoneUpper() {
		super(Material.ROCK);
		this.setHardness(100.0F);
		this.setResistance(1000);
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
		if (count < 4 && rand.nextInt(32 - fortune * 4) == 0) {
			ret.add(new ItemStack(Items.IRON_NUGGET));
		}
	}

	@Override
	@Nullable
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return MistItems.ROCKS;
	}

	@Override
	public int quantityDropped(Random random) {
		return 2 + random.nextInt(3);
	}

	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.BLOCK;
	}
}
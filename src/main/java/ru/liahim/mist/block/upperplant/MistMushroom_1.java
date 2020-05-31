package ru.liahim.mist.block.upperplant;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class MistMushroom_1 extends MistMushroom {

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(TYPE_1).getMetadata();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(TYPE_1, MushroomType_1.byMetadata(meta));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { TYPE_1 });
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		for (int i = 0; i < TYPE_1.getAllowedValues().size(); ++i) {
			list.add(new ItemStack(this, 1, i));
		}
	}

	@Override
	public PropertyEnum getTypeProperty() {
		return MistMushroom.TYPE_1;
	}

	@Override
	public String getTypeName(int meta) {
		return MushroomType_1.byMetadata(meta).getName();
	}

	@Override
	public IFoodProperty getFoodProperty(int meta) {
		return MushroomType_1.byMetadata(meta);
	}

	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(TYPE_1).getMetadata() + 16;
	}
}
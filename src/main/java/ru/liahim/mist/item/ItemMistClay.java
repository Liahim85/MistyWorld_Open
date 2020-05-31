package ru.liahim.mist.item;

import ru.liahim.mist.block.MistClay;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

public class ItemMistClay extends ItemMistGenderNameBlock {

	public ItemMistClay(MistClay block) {
		super(block);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		IBlockState state = ((MistClay)this.block).getStateFromMeta(stack.getItemDamage());
		String typeName = state.getValue(MistClay.TYPE) == MistClay.EnumBlockType.CONTAINER ?
				MistClay.EnumBlockType.CONTAINER.getName() : (state).getValue(MistClay.TYPE).getName();
		return "tile.mist." + (state).getValue(MistClay.VARIANT).getName() + "_" + typeName;
	}
}
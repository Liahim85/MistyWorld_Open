package ru.liahim.mist.item;

import ru.liahim.mist.block.MistSapropel;
import net.minecraft.item.ItemStack;

public class ItemMistSapropel extends ItemMistGenderNameBlock {

	public ItemMistSapropel(MistSapropel block) {
		super(block);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return "tile.mist." + this.block.getUnlocalizedName().substring(5) + "_" +
				((MistSapropel)this.block).getStateFromMeta(stack.getItemDamage()).getValue(MistSapropel.TYPE).getName();
	}
}
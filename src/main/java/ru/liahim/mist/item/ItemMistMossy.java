package ru.liahim.mist.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemMistMossy extends ItemBlock {

	public ItemMistMossy(Block block) {
		super(block);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int damage) {
		return damage;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		String name = super.getUnlocalizedName(stack);
		return stack.getItemDamage() > 0 ? name + "_moss" : name;
	}
}
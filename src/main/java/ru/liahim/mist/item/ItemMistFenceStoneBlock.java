package ru.liahim.mist.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemMistFenceStoneBlock extends ItemBlock {

	public ItemMistFenceStoneBlock(Block block) {
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
		String name = "";
		String blockName = this.block.getUnlocalizedName();
		switch (stack.getItemDamage()) {
		case 0:
			name = "";
			break;
		case 1:
			name = "_moss";
			break;
		}
		return blockName + name;
	}
}
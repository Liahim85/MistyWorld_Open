package ru.liahim.mist.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemMistFenceBlock extends ItemBlock {

	public ItemMistFenceBlock(Block block) {
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
			name = "_4";
			break;
		case 1:
			name = "_4_d";
			break;
		case 2:
			name = "_8";
			break;
		case 3:
			name = "_8_d";
			break;
		}
		return blockName + name;
	}
}
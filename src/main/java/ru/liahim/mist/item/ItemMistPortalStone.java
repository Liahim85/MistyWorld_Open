package ru.liahim.mist.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemMistPortalStone extends ItemBlock {

	public ItemMistPortalStone(Block block) {
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
		String nameBlock = "";
		switch (stack.getItemDamage()) {
		case 0:
			nameBlock = "portal_new_down";
			break;
		case 1:
			nameBlock = "portal_new_up";
			break;
		case 2:
			nameBlock = "portal_old_down";
			break;
		case 3:
			nameBlock = "portal_old_up";
			break;
		}
		return "tile.mist." + nameBlock;
	}
}
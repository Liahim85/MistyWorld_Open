package ru.liahim.mist.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemMistSponge extends ItemBlock {

	public ItemMistSponge(Block block) {
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
		case 13:
			nameBlock = "_clear";
			break;
		case 14:
			nameBlock = "_wet";
			break;
		case 15:
			nameBlock = "_spoiled";
			break;
		}
		return "tile.mist.sponge" + nameBlock;
	}
}
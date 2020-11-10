package ru.liahim.mist.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemMistMinedStone extends ItemBlock {

	public ItemMistMinedStone(Block block) {
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
		String name = "tile.mist.";
		switch (stack.getItemDamage()) {
		case 0:
			name += "stone_mined";
			break;
		case 4:
			name += "stone_mined_moss";
			break;
		case 5:
			name += "stone_chiseled";
			break;
		case 9:
			name += "stone_chiseled_moss";
			break;
		}
		return name;
	}
}
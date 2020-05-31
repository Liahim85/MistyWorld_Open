package ru.liahim.mist.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemMistBranchBlock extends ItemBlock {

	public ItemMistBranchBlock(Block block) {
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
		case 3:
			name = "_4_d";
			break;
		case 6:
			name = "_8";
			break;
		case 9:
			name = "_8_d";
			break;
		}
		return blockName + name;
	}
}
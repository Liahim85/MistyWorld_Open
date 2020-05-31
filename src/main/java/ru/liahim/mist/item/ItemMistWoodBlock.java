package ru.liahim.mist.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemMistWoodBlock extends ItemBlock {

	public ItemMistWoodBlock(Block block) {
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
		blockName = blockName.substring(0, blockName.length() - 5);
		switch (stack.getItemDamage()) {
		case 0:
			name = "log";
			break;
		case 3:
			name = "log_b";
			break;
		case 4:
			name = "log_c";
			break;
		case 7:
			name = "log_n";
			break;
		case 8:
			name = "log_d";
			break;
		case 11:
			name = "log_nd";
			break;
		case 13:
			name = "planks";
			break;
		}
		return blockName + name;
	}
}
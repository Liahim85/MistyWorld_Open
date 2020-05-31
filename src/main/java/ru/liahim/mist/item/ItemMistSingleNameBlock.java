package ru.liahim.mist.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public class ItemMistSingleNameBlock extends ItemBlock {

	public ItemMistSingleNameBlock(Block block) {
		super(block);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int damage) {
		return damage;
	}
}
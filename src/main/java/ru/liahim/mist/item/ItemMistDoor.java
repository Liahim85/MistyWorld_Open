package ru.liahim.mist.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import ru.liahim.mist.common.Mist;

public class ItemMistDoor extends ItemDoor {

	private final Block block;

	public ItemMistDoor(Block block) {
		super(block);
		this.block = block;
		this.setCreativeTab(Mist.mistTab);
	}

	public Block getBlock() {
		return block;
	}

	@Override
	public String getUnlocalizedName() {
		return this.block.getUnlocalizedName();
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return this.block.getUnlocalizedName();
	}
}
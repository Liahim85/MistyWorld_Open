package ru.liahim.mist.item;

import ru.liahim.mist.block.MistTreeSapling;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemMistTreeSapling extends ItemBlock {

	public ItemMistTreeSapling(Block block) {
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
		MistTreeSapling.EnumType type = MistTreeSapling.EnumType.byMeta(stack.getItemDamage());
		return "tile.mist." + type.getName() + "_sapling";
	}
}
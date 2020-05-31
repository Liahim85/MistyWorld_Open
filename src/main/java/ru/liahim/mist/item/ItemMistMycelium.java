package ru.liahim.mist.item;

import ru.liahim.mist.block.upperplant.MistMushroom;
import ru.liahim.mist.block.upperplant.MistMycelium;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.translation.I18n;

public class ItemMistMycelium extends ItemMistGenderNameBlock {

	public ItemMistMycelium(MistMycelium block) {
		super(block);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String name = "";
		NBTTagCompound tag = stack.getSubCompound("BlockEntityTag");
		if (tag != null && tag.hasKey("Mushroom")) {
			tag = tag.getCompoundTag("Mushroom");
			Block block = tag.hasKey("id", 8) ? Block.getBlockFromName(tag.getString("id")) : Blocks.AIR;
			IBlockState state = block.getStateFromMeta(tag.getByte("Damage"));
			if (state.getBlock() instanceof MistMushroom) {
				name = ((MistMushroom)state.getBlock()).getTypeName(state.getBlock().getMetaFromState(state));
				name = " (" + I18n.translateToLocal("tile.mist.mushroom_" + name + ".name") + ")";
			}
		}
		return super.getItemStackDisplayName(stack) + name;
	}
}
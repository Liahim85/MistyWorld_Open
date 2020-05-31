package ru.liahim.mist.common;

import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.init.ModConfig;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MistTab extends CreativeTabs {

	public MistTab(String label) {
		super(label);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ItemStack getTabIconItem() {
		return new ItemStack(MistBlocks.PORTAL_BASE, 1, 3);
	}

	@Override
	public boolean hasSearchBar() {
		return ModConfig.player.enableSearchBar;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getBackgroundImageName() {
		return ModConfig.player.enableSearchBar ? "item_search.png" : "items.png";
	}
}
package ru.liahim.mist.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMistClayBall extends ItemMist {

	public ItemMistClayBall() {
		this.setHasSubtypes(true);
	    this.setMaxDamage(0);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		int i = stack.getMetadata();
		String type = "";
		if (i == 0) type = "_gray";
		else if (i == 1) type = "_red";
		return super.getUnlocalizedName() + type;
    }

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (this.isInCreativeTab(tab)) {
			items.add(new ItemStack(this, 1, 0));
			items.add(new ItemStack(this, 1, 1));
		}
	}
}
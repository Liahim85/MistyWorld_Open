package ru.liahim.mist.api.item;

import net.minecraft.item.ItemStack;

public interface IFilter {

	public default float getFilteringDepth() {
		return 85;
	}

	public static float getDepthOfFilteration(ItemStack stack) {
		if (stack.getItem() instanceof IFilter) return ((IFilter)stack.getItem()).getFilteringDepth();
		return 0;
	}
}
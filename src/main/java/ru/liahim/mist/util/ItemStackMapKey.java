package ru.liahim.mist.util;

import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ItemStackMapKey {

	public final ItemStack itemStack;

	public ItemStackMapKey(ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(itemStack.getItem()).append(itemStack.getItemDamage()).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ItemStackMapKey) return new EqualsBuilder()
				.append(itemStack.getItem(), ((ItemStackMapKey) obj).itemStack.getItem())
				.append(itemStack.getItemDamage(), ((ItemStackMapKey) obj).itemStack.getItemDamage())
				.build();
		else return false;
	}
}
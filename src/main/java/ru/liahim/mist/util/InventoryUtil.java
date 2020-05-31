package ru.liahim.mist.util;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class InventoryUtil {

	/** Collects single items to stacks and places them at random. */
	public static void optimizeInventory(IInventory inventory, Random rand) {
		List<ItemStack> list = Lists.<ItemStack>newLinkedList();
		for (int i = 0; i < inventory.getSizeInventory(); ++i) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (!stack.isEmpty()) {
				if (!list.isEmpty() && stack.isStackable()) {
					boolean check = false;
					for (ItemStack stack1 : list) {
						if (stack1.getItem() == stack.getItem() && (!stack1.getHasSubtypes() || stack1.getMetadata() == stack.getMetadata()) && ItemStack.areItemStackTagsEqual(stack, stack1)) {
							stack1.grow(stack.getCount());
							if (stack1.getCount() > inventory.getInventoryStackLimit()) {
								list.add(rand.nextInt(list.size()), stack1.splitStack(inventory.getInventoryStackLimit()));
							}
							check = true;
							break;
						} 
					}
					if (!check) list.add(stack);
				} else list.add(stack);
			}
		}
		fillInventory(inventory, list, rand);
	}

	/** Collects single items to stacks and places them at random. */
	public static void optimizeAndFillInventory(IInventory inventory, List<ItemStack> listIn, Random rand) {
		List<ItemStack> list = Lists.<ItemStack>newLinkedList();
		for (ItemStack stack : listIn) {
			if (!stack.isEmpty()) {
				if (!list.isEmpty() && stack.isStackable()) {
					boolean check = false;
					for (ItemStack stack1 : list) {
						if (stack1.getItem() == stack.getItem() && (!stack1.getHasSubtypes() || stack1.getMetadata() == stack.getMetadata()) && ItemStack.areItemStackTagsEqual(stack, stack1)) {
							stack1.grow(stack.getCount());
							if (stack1.getCount() > inventory.getInventoryStackLimit()) {
								list.add(rand.nextInt(list.size()), stack1.splitStack(inventory.getInventoryStackLimit()));
							}
							check = true;
							break;
						} 
					}
					if (!check) list.add(stack);
				} else list.add(stack);
			}
		}
		fillInventory(inventory, list, rand);
	}

	/** Places items at random. */
	public static void fillInventory(IInventory inventory, List<ItemStack> list, Random rand) {
		if (!list.isEmpty()) {
			inventory.clear();
			int size = inventory.getSizeInventory() - list.size();
			for (int i = 0; i < size; ++i) list.add(ItemStack.EMPTY);
			ItemStack[] stacks = new ItemStack[list.size()];
			list.toArray(stacks);
			ItemStack temp;
			for (int i = stacks.length; i > 0; --i) {
				size = rand.nextInt(i);
				temp = stacks[size];
				stacks[size] = stacks[i - 1];
				stacks[i - 1] = temp;
			}
			for (int i = 0; i < inventory.getSizeInventory(); ++i) inventory.setInventorySlotContents(i, stacks[i]);
		}
	}

	public static boolean canCombineStack(ItemStack stack1, ItemStack stack2) {
		if (stack1.getItem() != stack2.getItem()) return false;
		else if (stack1.getMetadata() != stack2.getMetadata()) return false;
		else if (stack1.getCount() > stack1.getMaxStackSize()) return false;
		else return ItemStack.areItemStackTagsEqual(stack1, stack2);
	}
}
package ru.liahim.mist.capability.handler;

import ru.liahim.mist.item.food.ItemMistMushroom;
import java.util.HashSet;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class FoodHandler implements IFoodHandler {

	private int[] mushroomStudyList = new int[ItemMistMushroom.MUSHROOMS.length * 16];
	private int[] mushroomCookStudyList = new int[ItemMistMushroom.MUSHROOMS.length * 16];
	private HashSet<String> toxicFoodList = new HashSet<String>();

	@Override
	public int getMushroomStudy(int meta, boolean isCook) {
		if (isCook) { if (meta >= 0 && meta < this.mushroomCookStudyList.length) return this.mushroomCookStudyList[meta]; }
		else { if (meta >= 0 && meta < this.mushroomStudyList.length) return this.mushroomStudyList[meta]; }
		return 0;
	}

	@Override
	public boolean setMushroomStudy(int meta, int study, boolean isCook) {
		if (isCook) {
			if (meta >= 0 && meta < this.mushroomCookStudyList.length) {
				this.mushroomCookStudyList[meta] = study;
				return true;
			}
		} else {
			if (meta >= 0 && meta < this.mushroomStudyList.length) {
				this.mushroomStudyList[meta] = study;
				return true;
			}
		}
		return false;
	}

	@Override
	public int[] getMushroomList(boolean isCook) {
		return isCook ? this.mushroomCookStudyList : this.mushroomStudyList;
	}

	@Override
	public void setMushroomList(int[] list, boolean isCook) {
		if (isCook) this.mushroomCookStudyList = list;
		else this.mushroomStudyList = list;
	}

	@Override
	public boolean mergeMushroomList(int[] list, boolean isCook) {
		boolean change = false;
		if (isCook) {
			for (int i = 0; i < this.mushroomCookStudyList.length && i < list.length; ++i) {
				if (list[i] != 0 && list[i] != this.mushroomCookStudyList[i]) {
					this.mushroomCookStudyList[i] = list[i];
					change = true;
				}
			}
		} else {
			for (int i = 0; i < this.mushroomStudyList.length && i < list.length; ++i) {
				if (list[i] != 0 && list[i] != this.mushroomStudyList[i]) {
					this.mushroomStudyList[i] = list[i];
					change = true;
				}
			}
		}
		return change;
	}

	@Override
	public boolean setFoodStudy(ItemStack stack) {
		return this.setFoodStudy(String.valueOf(stack.getMetadata()) + "_" + stack.getItem().getRegistryName().toString());
	}

	@Override
	public boolean setFoodStudy(String string) {
		return this.toxicFoodList.add(string);
	}

	@Override
	public boolean isFoodStudy(ItemStack stack) {
		return this.toxicFoodList.contains(String.valueOf(stack.getMetadata()) + "_" + stack.getItem().getRegistryName().toString());
	}

	@Override
	public String[] getFoodStudyList() {
		int size = this.toxicFoodList.size();
		String[] ii = new String[this.toxicFoodList.size()];
		if (size > 0) {
			int i = 0;
			for(Object x : this.toxicFoodList.toArray()) {
				ii[i++] = (String)x;
			}
		}
		return ii;
	}

	@Override
	public void setFoodStudyList(String[] array) {
		for(String i : array) this.setFoodStudy(i);
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setIntArray("MushroomsStadyList", this.mushroomStudyList);
		tag.setIntArray("MushroomsCookStadyList", this.mushroomCookStudyList);
		NBTTagList list = new NBTTagList();
		NBTTagCompound temp;
		for (String str : this.getFoodStudyList()) {
			temp = new NBTTagCompound();
			temp.setString("Item", str);
			list.appendTag(temp);
		}
		tag.setTag("ToxicFoodStadyList", list);
		return tag;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		int[] list = nbt.getIntArray("MushroomsStadyList");
		for (int i = 0; i < this.mushroomStudyList.length; ++i) {
			if (i < list.length) this.mushroomStudyList[i] = list[i];
			else this.mushroomStudyList[i] = 0;
		}
		int[] cookList = nbt.getIntArray("MushroomsCookStadyList");
		for (int i = 0; i < this.mushroomCookStudyList.length; ++i) {
			if (i < cookList.length) this.mushroomCookStudyList[i] = cookList[i];
			else this.mushroomCookStudyList[i] = 0;
		}
		NBTTagList str = nbt.getTagList("ToxicFoodStadyList", 10);
		for (int i = 0; i < str.tagCount(); ++i) {
			this.setFoodStudy(str.getCompoundTagAt(i).getString("Item"));
		}
	}
}
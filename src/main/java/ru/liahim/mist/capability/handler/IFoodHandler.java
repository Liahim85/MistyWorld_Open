package ru.liahim.mist.capability.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import ru.liahim.mist.capability.FoodCapability;

public interface IFoodHandler extends INBTSerializable<NBTTagCompound> {

	public int getMushroomStudy(int meta, boolean isCook);
	public int[] getMushroomList(boolean isCook);
	public boolean setMushroomStudy(int meta, int study, boolean isCook);
	public void setMushroomList(int[] list, boolean isCook);
	public boolean mergeMushroomList(int[] list, boolean isCook);
	public boolean setFoodStudy(ItemStack stack);
	public boolean setFoodStudy(String stack);
	public boolean isFoodStudy(ItemStack stack);
	public String[] getFoodStudyList();
	public void setFoodStudyList(String[] array);

	public static IFoodHandler getHandler(EntityPlayer player) {
		IFoodHandler handler = player.getCapability(FoodCapability.CAPABILITY_FOOD, null);
		return handler;
	}
}
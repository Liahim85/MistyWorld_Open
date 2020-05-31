package ru.liahim.mist.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import ru.liahim.mist.capability.handler.IFoodHandler;

public class FoodCapability {

	@CapabilityInject(IFoodHandler.class)
    public static final Capability<IFoodHandler> CAPABILITY_FOOD = null;

	public static class Storage<T extends IFoodHandler> implements IStorage<IFoodHandler> {
		@Override
		public NBTBase writeNBT(Capability<IFoodHandler> capability, IFoodHandler instance, EnumFacing side) {
			return instance.serializeNBT();
		}
		@Override
		public void readNBT(Capability<IFoodHandler> capability, IFoodHandler instance, EnumFacing side, NBTBase nbt) {
			instance.deserializeNBT((NBTTagCompound)nbt);
		}
    }

	public static class Provider implements ICapabilitySerializable<NBTTagCompound> {

		IFoodHandler instance = CAPABILITY_FOOD.getDefaultInstance();

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == CAPABILITY_FOOD;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			return hasCapability(capability, facing) ? CAPABILITY_FOOD.<T>cast(instance) : null;
		}

		@Override
		public NBTTagCompound serializeNBT() {
			return (NBTTagCompound) CAPABILITY_FOOD.getStorage().writeNBT(CAPABILITY_FOOD, instance, null);
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			CAPABILITY_FOOD.getStorage().readNBT(CAPABILITY_FOOD, instance, null, nbt);
		}
	}
}
package ru.liahim.mist.capability;

import ru.liahim.mist.capability.handler.ISkillCapaHandler;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class SkillCapability {

	@CapabilityInject(ISkillCapaHandler.class)
    public static final Capability<ISkillCapaHandler> CAPABILITY_SKILL = null;

	public static class Storage<T extends ISkillCapaHandler> implements IStorage<ISkillCapaHandler> {
		@Override
		public NBTBase writeNBT(Capability<ISkillCapaHandler> capability, ISkillCapaHandler instance, EnumFacing side) {
			return instance.serializeNBT();
		}
		@Override
		public void readNBT(Capability<ISkillCapaHandler> capability, ISkillCapaHandler instance, EnumFacing side, NBTBase nbt) {
			instance.deserializeNBT((NBTTagCompound)nbt);
		}
    }

	public static class Provider implements ICapabilitySerializable<NBTTagCompound> {

		ISkillCapaHandler instance = CAPABILITY_SKILL.getDefaultInstance();

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == CAPABILITY_SKILL;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			return hasCapability(capability, facing) ? CAPABILITY_SKILL.<T>cast(instance) : null;
		}

		@Override
		public NBTTagCompound serializeNBT() {
			return (NBTTagCompound) CAPABILITY_SKILL.getStorage().writeNBT(CAPABILITY_SKILL, instance, null);
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			CAPABILITY_SKILL.getStorage().readNBT(CAPABILITY_SKILL, instance, null, nbt);
		}
	}
}
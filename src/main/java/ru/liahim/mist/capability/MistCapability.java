package ru.liahim.mist.capability;

import ru.liahim.mist.capability.handler.IMistCapaHandler;
import ru.liahim.mist.capability.handler.MistCapaHandler;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.INBTSerializable;

public class MistCapability {

	@CapabilityInject(IMistCapaHandler.class)
    public static final Capability<IMistCapaHandler> CAPABILITY_MIST = null;

	public static class Storage<T extends IMistCapaHandler> implements IStorage<IMistCapaHandler> {
		@Override
		public NBTBase writeNBT(Capability<IMistCapaHandler> capability, IMistCapaHandler instance, EnumFacing side) {
			return null;
		}
		@Override
		public void readNBT(Capability<IMistCapaHandler> capability, IMistCapaHandler instance, EnumFacing side, NBTBase nbt) {}
    }

	public static class Provider implements INBTSerializable<NBTTagCompound>, ICapabilityProvider {

		private final MistCapaHandler handler;

		public Provider(MistCapaHandler handler) {
			this.handler = handler;
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == CAPABILITY_MIST;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			if (capability == CAPABILITY_MIST)
				return (T) this.handler;
			return null;
		}

		@Override
		public NBTTagCompound serializeNBT() {
			return this.handler.serializeNBT();
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			this.handler.deserializeNBT(nbt);
		}
	}
}
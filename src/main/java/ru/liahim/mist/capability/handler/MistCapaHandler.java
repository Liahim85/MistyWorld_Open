package ru.liahim.mist.capability.handler;

import ru.liahim.mist.api.item.IMask;
import ru.liahim.mist.network.PacketHandler;
import ru.liahim.mist.network.PacketToxicSync;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemStackHandler;

public class MistCapaHandler extends ItemStackHandler implements IMistCapaHandler {
	
	private boolean isChange;
	private boolean globalChange;
	private EntityPlayer player;
	private boolean blockEvents = false;
	private int pollution;
	private int toxic;

	public MistCapaHandler() {
        super(1);
    }

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack, EntityPlayer player) {
		return slot == 0 && IMask.canEquip(stack, player);
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		if (stack.isEmpty() || this.isItemValidForSlot(slot, stack, player)) {
			super.setStackInSlot(0, stack);
		}
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (!this.isItemValidForSlot(slot, stack, player)) return stack;
		return super.insertItem(slot, stack, simulate);
	}

	@Override
	protected void onContentsChanged(int slot) {
		super.onContentsChanged(slot);
		this.setMaskChanged(true, true);
	}

	@Override
	public boolean isMaskBlocked() {
		return this.blockEvents;
	}

	@Override
	public void setMaskBlock(boolean blockEvents) {
		this.blockEvents = blockEvents;
	}

	@Override
	public boolean isMaskChanged() {
		return this.isChange;
	}
	
	@Override
	public boolean isGlobalChanged() {
		return this.globalChange;
	}

	@Override
	public void setMaskChanged(boolean changed, boolean global) {
		this.isChange = changed;
		this.globalChange = global;
	}

	@Override
	public void setPlayer(EntityPlayer player) {
		this.player = player;
	}

	////////////////////////////////// Pollution //////////////////////////////////

	private int prevPollution;

	@Override
	public int getPollution() {
		return this.pollution;
	}

	@Override
	public void setPollution(int pollution) {
		if (this.pollution == pollution) return;
		this.pollution = pollution;
		if (!player.world.isRemote && player instanceof EntityPlayerMP) PacketHandler.INSTANCE.sendTo(new PacketToxicSync(this.pollution, HurtType.POLLUTION.getID()), (EntityPlayerMP)player);
	}

	@Override
	public void addPollution(int pollution) {
		if (pollution == 0) return;
		this.prevPollution = this.pollution;
		this.pollution += pollution;
		if (this.pollution < 0) this.pollution = 0;
		else if (this.pollution > 10000) this.pollution = 10000;
		if (this.prevPollution != this.pollution && !player.world.isRemote && player instanceof EntityPlayerMP)
			PacketHandler.INSTANCE.sendTo(new PacketToxicSync(this.pollution, HurtType.POLLUTION.getID()), (EntityPlayerMP)player);
	}

	////////////////////////////////// Intoxication //////////////////////////////////

	private int prevToxic;

	@Override
	public int getToxic() {
		return this.toxic;
	}

	@Override
	public void setToxic(int toxic) {
		if (this.toxic == toxic) return;
		this.toxic = toxic;
		if (!player.world.isRemote && player instanceof EntityPlayerMP) PacketHandler.INSTANCE.sendTo(new PacketToxicSync(this.toxic, HurtType.TOXIC.getID()), (EntityPlayerMP)player);
	}

	@Override
	public void addToxic(int toxic) {
		if (toxic == 0) return;
		this.prevToxic = this.toxic;
		this.toxic += toxic;
		if (this.toxic < 0) this.toxic = 0;
		else if (this.toxic > 10000) this.toxic = 10000;
		if (this.prevToxic != this.toxic && !player.world.isRemote && player instanceof EntityPlayerMP)
			PacketHandler.INSTANCE.sendTo(new PacketToxicSync(this.toxic, HurtType.TOXIC.getID()), (EntityPlayerMP)player);
	}

	////////////////////////////////// NBT //////////////////////////////////

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = super.serializeNBT();
		nbt.setInteger("Pollution", this.pollution);
		nbt.setInteger("Toxic", this.toxic);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		this.pollution = nbt.getInteger("Pollution");
		this.toxic = nbt.getInteger("Toxic");
		super.deserializeNBT(nbt);
	}

	public static enum HurtType {

		TOXIC(0),
		POLLUTION(1);

		private final int id;
		public static Set<String> commands = Sets.newHashSet();

		private HurtType(int id) {
			this.id = id;
		}

		public int getID() {
			return this.id;
		}

		static {
			for (HurtType type : HurtType.values()) {
				commands.add(type.name().toLowerCase());
			}
			commands.add("clear");
		}
	}
}
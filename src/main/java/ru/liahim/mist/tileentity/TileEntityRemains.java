package ru.liahim.mist.tileentity;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import ru.liahim.mist.util.InventoryUtil;
import net.minecraft.tileentity.TileEntityLockableLoot;

public class TileEntityRemains extends TileEntityLockableLoot implements ISidedInventory {

	private int size;
	private NonNullList<ItemStack> contents;
	private int[] slots;
	public int cooldown = -1;

	@Override
	public int getSizeInventory() {
		return contents == null ? 0 : this.contents.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : this.contents) {
			if (!itemstack.isEmpty()) return false;
		}
		return true;
	}

	private boolean isFull() {
		if (this.contents == null) return true;
		for (ItemStack stack : this.contents) {
			if (stack.isEmpty() || stack.getCount() < Math.min(stack.getMaxStackSize(), this.getInventoryStackLimit())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public String getName() {
		return null;
	}

	public void init(int size) {
		this.size = size;
		this.contents = NonNullList.<ItemStack>withSize(this.size, ItemStack.EMPTY);
		this.slots = new int[size];
		for (int i = 0; i < size; ++i) this.slots[i] = i;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.init(compound.getInteger("Size"));
		readItems(compound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("Size", this.size);
		return writeItems(compound);
	}

	public void readItems(NBTTagCompound compound) {
		if (!this.checkLootAndRead(compound)) ItemStackHelper.loadAllItems(compound, this.contents);
	}

	public NBTTagCompound writeItems(NBTTagCompound compound) {
		if (!this.checkLootAndWrite(compound)) ItemStackHelper.saveAllItems(compound, this.contents);
		return compound;
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, this.getBlockMetadata(), this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		handleUpdateTag(pkt.getNbtCompound());
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	@Override
	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer player) {
		this.fillWithLoot(player);
        return null;
	}

	@Override
	public void fillWithLoot(@Nullable EntityPlayer player) {
		if (this.lootTable != null && this.world instanceof WorldServer) {
			LootTable loottable = this.world.getLootTableManager().getLootTableFromLocation(this.lootTable);
			this.lootTable = null;
			Random random;
			if (this.lootTableSeed == 0L) random = new Random();
			else random = new Random(this.lootTableSeed);
			LootContext.Builder builder = new LootContext.Builder((WorldServer) this.world);
			if (player != null) builder.withLuck(player.getLuck()).withPlayer(player);
			loottable.fillInventory(this, random, builder.build());
		}
	}

	public boolean putStack(ItemStack heldItem) {
		if (this.isFull()) return false;
		boolean put = false;
		for (int i = 0; i < this.contents.size(); ++i) {
			ItemStack stack = this.getStackInSlot(i);
			if (stack.isEmpty()) {
				stack = heldItem.splitStack(Math.min(heldItem.getCount(), this.getInventoryStackLimit()));
				if (!stack.isEmpty()) {
					this.setInventorySlotContents(i, stack);
					if (!put) put = true;
				}
                if (heldItem.getCount() == 0) break;
			} else if (InventoryUtil.canCombineStack(stack, heldItem)) {
                int j = Math.min(heldItem.getCount(), Math.min(heldItem.getMaxStackSize(), this.getInventoryStackLimit()) - stack.getCount());
                heldItem.shrink(j);
                stack.grow(j);
                if (!put) put = j > 0;
                if (heldItem.getCount() == 0) break;
			}
		}
		if (put) this.markDirty();
		return put;
	}

	public void pullStack(EntityItem entity) {
		ItemStack stack = entity.getItem().copy();
		putStack(stack);
		if (stack.isEmpty()) entity.setDead();
		else entity.setItem(stack);
	}

	@Override
	public String getGuiID() {
		return null;
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.contents;
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		return this.slots;
	}

	@Override
	public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
		if (direction == EnumFacing.DOWN) return false;
		return index >= 0 && index < this.size;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		return direction == EnumFacing.DOWN && index >= 0 && index < this.size;
	}
}

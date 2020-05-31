package ru.liahim.mist.tileentity;

import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

public class TileEntityCampStick extends TileEntity implements ITickable {

	private EnumFacing facing = EnumFacing.NORTH;
	private boolean isWork = false;
	private ItemStack food = ItemStack.EMPTY;
	private int progress = -1;

	@Override
	public void update() {
		if (this.isWork() && !this.world.isRemote && this.progress >= 0 && !this.getFood().isEmpty()) {
			if (this.progress < TileEntityCampfire.getGrillCookTime() + 20) ++this.progress;
			else {
				this.food = FurnaceRecipes.instance().getSmeltingResult(this.food).copy();
				this.progress = -1;
				this.updateStatus();
			}
		}
	}

	public boolean isWork() {
		return isWork;
	}

	public void setWork(boolean isWork) {
		this.isWork = isWork;
		if (this.progress < 0 && !this.food.isEmpty() && FurnaceRecipes.instance().getSmeltingResult(this.food).getItem() instanceof ItemFood) {
			this.progress = 0;
		}
		this.updateStatus();
	}

	public ItemStack getFood() {
		return food;
	}

	public boolean setFood(ItemStack food) {
		if (!food.isEmpty() && food.getItem() instanceof ItemFood && this.getFood().isEmpty()) {
			ItemStack result = FurnaceRecipes.instance().getSmeltingResult(food);
			if (!result.isEmpty() && result.getItem() instanceof ItemFood) {
				this.food = food.splitStack(1);
				if (isWork()) this.progress = 0;
				this.updateStatus();
				return true;
			}
		}
		return false;
	}

	public void clearFood() {
		this.food = ItemStack.EMPTY;
		this.progress = -1;
	}

	public void setFacing(EnumFacing face) {
		if (face != EnumFacing.UP && face != EnumFacing.DOWN) {
			this.facing = face;
		}
	}

	public EnumFacing getFacing() {
		return this.facing;
	}

	public void updateStatus() {
		this.markDirty();
		this.world.notifyBlockUpdate(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 3);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.setFacing(EnumFacing.getHorizontal(compound.getByte("Facing")));
		this.food = new ItemStack(compound.getCompoundTag("Food"));
		this.progress = compound.getInteger("Progress");
		this.isWork = (compound.getBoolean("IsWork"));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setByte("Facing", (byte)this.getFacing().getHorizontalIndex());
		compound.setTag("Food", this.food.writeToNBT(new NBTTagCompound()));
		compound.setInteger("Progress", this.progress);
		compound.setBoolean("IsWork", this.isWork());
		return compound;
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound tag = getUpdateTag();
		return new SPacketUpdateTileEntity(this.pos, this.getBlockMetadata(), tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		handleUpdateTag(pkt.getNbtCompound());
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound tag = new NBTTagCompound();
		writeToNBT(tag);
		return tag;
	}
}
package ru.liahim.mist.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityLatexPot extends TileEntity {

	private int stage;
	private long lastUpdateTime;
	public boolean isDead;
	public BlockPos root;

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
        BlockPos pos = getPos();
		int i = this.getBlockMetadata();
		if (i == 0) return new AxisAlignedBB(pos.add(0, 0, -1), pos.add(1, 1, 1));
		else if (i == 1) return new AxisAlignedBB(pos.add(0, 0, 0), pos.add(2, 1, 1));
		else if (i == 2) return new AxisAlignedBB(pos.add(0, 0, 0), pos.add(1, 1, 2));
		else return new AxisAlignedBB(pos.add(-1, 0, 0), pos.add(1, 1, 1));
	}

	public long getLastUpdateTime() {
		return this.lastUpdateTime;
	}

	public void updateTime() {
		this.lastUpdateTime = this.world.getTotalWorldTime() + this.world.rand.nextInt(1200) - 600;
	}

	public int getStage() {
		return this.stage;
	}

	public void setStage(int stage) {
		this.stage = stage;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.stage = compound.getInteger("Stage");
		this.lastUpdateTime = compound.getLong("LastUpdateTime");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("Stage", this.stage);
		compound.setLong("LastUpdateTime", this.lastUpdateTime);
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

	public void updateStatus(IBlockState oldState, IBlockState newState) {
		this.markDirty();
		this.world.notifyBlockUpdate(pos, oldState, newState, 3);
	}
}
package ru.liahim.mist.tileentity;

import ru.liahim.mist.block.upperplant.MistMushroom;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityMycelium extends TileEntity {

	private IBlockState mushroom;
	private int[] faceSize = new int[4];
	private int deadTime = 0;

	public IBlockState getMushroomState() {
		return this.mushroom;
	}

	public void setMushroomState(IBlockState state, boolean isNature) {
		if (state.getBlock() instanceof MistMushroom) {
			this.mushroom = state;
			if (isNature) this.faceSize = new int[] {32, 32, 32, 32};
		}
		else this.mushroom = null;
		this.markDirty();
	}

	public int[] getFaceSize() {
		return this.faceSize;
	}

	public int getFaceSize(EnumFacing face) {
		return this.faceSize[face.getHorizontalIndex()];
	}

	public int getMaxSize() {
		int size = 0;
		for (int i : this.faceSize) if (i > size) size = i;
		return size;
	}

	public void setFaceSize(EnumFacing face, int size) {
		this.faceSize[face.getHorizontalIndex()] = size;
		this.markDirty();
	}

	public int getDeadTime() {
		return this.deadTime;
	}

	public void setDeadTime(int deadTime) {
		this.deadTime = deadTime;
		this.markDirty();
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (compound.hasKey("Mushroom")) {
			NBTTagCompound tag = compound.getCompoundTag("Mushroom");
			Block block = tag.hasKey("id", 8) ? Block.getBlockFromName(tag.getString("id")) : Blocks.AIR;
			setMushroomState(block.getStateFromMeta(tag.getByte("Damage")), false);
			this.faceSize = tag.getIntArray("FaceSize");
			this.deadTime = tag.getInteger("DeadTime");
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		NBTTagCompound tag = new NBTTagCompound();
		tag.setIntArray("FaceSize", this.faceSize);
		tag.setInteger("DeadTime", this.deadTime);
		compound.setTag("Mushroom", tag);
		return getMushroomNBT(compound);
	}

	public NBTTagCompound getMushroomNBT(NBTTagCompound compound) {
		if (this.mushroom != null) {
			NBTTagCompound tag = compound.hasKey("Mushroom") ? compound.getCompoundTag("Mushroom") : new NBTTagCompound();
			ResourceLocation res = Block.REGISTRY.getNameForObject(this.mushroom.getBlock());
			tag.setString("id", res == null ? "minecraft:air" : res.toString());
	        tag.setShort("Damage", (short)this.mushroom.getBlock().getMetaFromState(this.mushroom));
			compound.setTag("Mushroom", tag);
		}
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

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
		return oldState.getBlock() != newSate.getBlock();
	}
}
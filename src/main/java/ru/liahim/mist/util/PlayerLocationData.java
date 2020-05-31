package ru.liahim.mist.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

public class PlayerLocationData extends WorldSavedData {

	private static final String DATA_NAME = "MistSpawnLocation";
	private static NBTTagCompound tag = new NBTTagCompound();

	public PlayerLocationData(String identifier) {
		super(identifier);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		if (nbt.hasKey(DATA_NAME)) {
			tag = nbt.getCompoundTag(DATA_NAME);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setTag(DATA_NAME, tag);
		return nbt;
	}

	public void addSpawnPos(EntityPlayer player, int x, int y, int z) {
		tag.setIntArray(String.valueOf(player.getUniqueID()), new int[] {x, y, z});
		this.markDirty();
	}

	public BlockPos getSpawnPos(EntityPlayer player) {
		int[] arr = tag.getIntArray(String.valueOf(player.getUniqueID()));
		if (arr.length == 3) return new BlockPos(arr[0], arr[1], arr[2]);
		else return null;
	}

	public void removeSpawnPos(EntityPlayer player) {
		if (tag.hasKey(String.valueOf(player.getUniqueID()))) {
			tag.removeTag(String.valueOf(player.getUniqueID()));
			this.markDirty();
		}
	}

	public static PlayerLocationData get(World world) {
		MapStorage storage = world.getMapStorage();
		PlayerLocationData instance = (PlayerLocationData)storage.getOrLoadData(PlayerLocationData.class, DATA_NAME);
		if (instance == null) {
			instance = new PlayerLocationData(DATA_NAME);
			storage.setData(DATA_NAME, instance);
		}
		return instance;
	}
}
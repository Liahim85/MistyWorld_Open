package ru.liahim.mist.util;

import ru.liahim.mist.common.MistTime;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

public class TimeData extends WorldSavedData {

	private static final String DATA_NAME = "MistTime";
	private static NBTTagCompound tag = new NBTTagCompound();

	public TimeData(String identifier) {
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

	public void setTime(int day, int month, int year, long offset) {
		tag.setInteger("Day", day);
		tag.setInteger("Month", month);
		tag.setInteger("Year", year);
		tag.setLong("Offset", offset);
		this.markDirty();
	}

	public void loadTime() {
		if (tag.hasKey("Year")) {
			int day = tag.getInteger("Day");
			int month = tag.getInteger("Month");
			int year = tag.getInteger("Year");
			long offset = tag.getLong("Offset");
			if (day >= MistTime.getDayInMonth()) {
				day = MistTime.getDayInMonth() - 1;
				this.setTime(day, month, year, offset);
			}
			MistTime.setTime(day, month, year, offset);
		} else {
			MistTime.setTime(0, 0, 0, 0);
			this.setTime(0, 0, 0, 0);
		}
	}

	public static TimeData get(World world) {
		MapStorage storage = world.getMapStorage();
		TimeData instance = (TimeData)storage.getOrLoadData(TimeData.class, DATA_NAME);
		if (instance == null) {
			instance = new TimeData(DATA_NAME);
			storage.setData(DATA_NAME, instance);
		}
		return instance;
	}
}
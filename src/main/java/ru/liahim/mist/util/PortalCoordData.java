package ru.liahim.mist.util;

import java.util.HashMap;

import javax.vecmath.Vector4f;

import ru.liahim.mist.common.Mist;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PortalCoordData extends WorldSavedData {

	private static final String DATA_NAME = "MistPortalCoords";
	private static NBTTagCompound tag = new NBTTagCompound();
	private static HashMap<String, String> portalCoord = new HashMap<String, String>();
	private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public PortalCoordData(String identifier) {
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

	public void addCoords(int dimIn, BlockPos in, int dimOut, BlockPos out) {
		portalCoord.clear();
		if (tag.hasKey(String.valueOf(dimIn))) {
			portalCoord = gson.fromJson(tag.getString(String.valueOf(dimIn)), HashMap.class);
		}
		portalCoord.put(gson.toJson(in), gson.toJson(new Vector4f(out.getX(), out.getY(), out.getZ(), dimOut)));
		tag.setString(String.valueOf(dimIn), gson.toJson(portalCoord));
		this.markDirty();
	}

	public BlockPos getCoords(int dimIn, BlockPos in) {
		if (tag.hasKey(String.valueOf(dimIn))) {
			portalCoord = gson.fromJson(tag.getString(String.valueOf(dimIn)), HashMap.class);
			return gson.fromJson(portalCoord.get(gson.toJson(in)), BlockPos.class);
		}
		return null;
	}

	public int getDim(int dimIn, BlockPos in) {
		if (tag.hasKey(String.valueOf(dimIn))) {
			portalCoord = gson.fromJson(tag.getString(String.valueOf(dimIn)), HashMap.class);
			try {
				Vector4f vec = gson.fromJson(portalCoord.get(gson.toJson(in)), Vector4f.class);
				if (vec != null) return (int)vec.w;
			} catch (Exception e) {
				return dimIn == Mist.getID() ? 0 : Mist.getID();
			}
		}
		return dimIn == Mist.getID() ? 0 : Mist.getID();
	}

	public void removeCoords(int dimIn, BlockPos in) {
		if (tag.hasKey(String.valueOf(dimIn))) {
			portalCoord = gson.fromJson(tag.getString(String.valueOf(dimIn)), HashMap.class);
			portalCoord.remove(gson.toJson(in));
			if (portalCoord.size() == 0)
				tag.removeTag(String.valueOf(dimIn));
			else tag.setString(String.valueOf(dimIn), gson.toJson(portalCoord));
			this.markDirty();
		}
	}

	public static PortalCoordData get(World world) {
		MapStorage storage = world.getMapStorage();
		PortalCoordData instance = (PortalCoordData)storage.getOrLoadData(PortalCoordData.class, DATA_NAME);
		if (instance == null) {
			instance = new PortalCoordData(DATA_NAME);
			storage.setData(DATA_NAME, instance);
		}
		return instance;
	}
}
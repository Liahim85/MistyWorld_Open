package ru.liahim.mist.api.biome;

import net.minecraft.util.IStringSerializable;

public enum EnumBiomeType implements IStringSerializable {

	Forest("forest"),
	Desert("desert"),
	Cold("cold"),
	Jungle("jungle"),
	Swamp("swamp"),
	Border("border"),
	Down("down");

	private final String name;

	private EnumBiomeType(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public String getName() {
		return this.name;
	}
}
package ru.liahim.mist.api.advancement;

import java.util.HashMap;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.util.IStringSerializable;
import net.minecraft.util.JsonUtils;

public enum PortalType implements IStringSerializable {

	ANY("any"),
	DIMENSION("dimension"),
	IN_VOID("in_void");

	private static final HashMap<String, PortalType> NAME_LOOKUP = new HashMap<String, PortalType>();
	private final String name;

	private PortalType(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public static PortalType getTypeByName(String name) {
		if (NAME_LOOKUP.containsKey(name)) return NAME_LOOKUP.get(name);
		return null;
	}

	static {
		for (PortalType type : values()) {
			NAME_LOOKUP.put(type.getName(), type);
		}
	}

	public boolean test(PortalType type) {
		return (this == ANY && type != IN_VOID) || this == type;
	}

	public static PortalType deserialize(@Nullable JsonElement element) {
		if (element != null && !element.isJsonNull()) {
			JsonObject jsonobject = JsonUtils.getJsonObject(element, "portal");
			String name = JsonUtils.getString(jsonobject, "portal");
			PortalType type = PortalType.getTypeByName(name);
			if (type != null) return type;
			else throw new JsonSyntaxException("Unknown portal type '" + name + "'");
		}
		return PortalType.ANY;
	}
}
package ru.liahim.mist.api.advancement;

import java.util.HashMap;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.JsonUtils;

public class FogDamagePredicate {
	
	public static FogDamagePredicate ANY = new FogDamagePredicate();
	private final FogDamageType damageType;
	private final MinMaxBounds centerDamage;
	private final Boolean mask;
	private final Boolean suit;
	private final Boolean adsorbent;

	public FogDamagePredicate(FogDamageType damageType, MinMaxBounds centerDamage, @Nullable Boolean mask, @Nullable Boolean suit, @Nullable Boolean absorbent) {
		this.damageType = damageType;
		this.centerDamage = centerDamage;
		this.mask = mask;
		this.suit = suit;
		this.adsorbent = absorbent;
	}

	public FogDamagePredicate() {
		this.damageType = FogDamageType.ANY;
		this.centerDamage = MinMaxBounds.UNBOUNDED;
		this.mask = null;
		this.suit = null;
		this.adsorbent = null;
	}

	public boolean test(FogDamageType damageType, float centerDamage, @Nullable Boolean mask, @Nullable Boolean suit, @Nullable Boolean adsorbent) {
		if (!this.damageType.test(damageType)) return false;
		else if (!this.centerDamage.test(centerDamage)) return false;
		else if (this.mask != null && mask != null && this.mask.booleanValue() != mask) return false;
		else if (this.suit != null && suit != null && this.suit.booleanValue() != suit) return false;
		else return this.adsorbent == null || adsorbent == null || this.adsorbent.booleanValue() == adsorbent;
    }

	public static FogDamagePredicate deserialize(@Nullable JsonElement element) {
		if (element != null && !element.isJsonNull()) {
			JsonObject jsonobject = JsonUtils.getJsonObject(element, "damage");
			FogDamageType damageType;
			if (jsonobject.has("type")) {
				String name = JsonUtils.getString(jsonobject, "type");
				damageType = FogDamageType.getTypeByName(name);
				if (damageType == null) {
					damageType = FogDamageType.ANY;
					throw new JsonSyntaxException("Unknown fog damage type '" + name + "'");
				}
			} else damageType = FogDamageType.ANY;
			MinMaxBounds centerDamage = MinMaxBounds.deserialize(jsonobject.get("center_damage"));
			Boolean mask = jsonobject.has("mask") ? JsonUtils.getBoolean(jsonobject, "mask") : null;
			Boolean suit = jsonobject.has("suit") ? JsonUtils.getBoolean(jsonobject, "suit") : null;
			Boolean absorbent = jsonobject.has("absorbent") ? JsonUtils.getBoolean(jsonobject, "absorbent") : null;
			return new FogDamagePredicate(damageType, centerDamage, mask, suit, absorbent);
		}
		return ANY;
	}

	public static enum FogDamageType implements IStringSerializable {

		ANY("any"),
		BY_FOG("by_fog"),
		BY_ACID("by_acid"),
		BY_RAIN("by_rain");

		private static final HashMap<String, FogDamageType> NAME_LOOKUP = new HashMap<String, FogDamageType>();
		private final String name;

		private FogDamageType(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return this.name;
		}

		public static FogDamageType getTypeByName(String name) {
			if (NAME_LOOKUP.containsKey(name)) return NAME_LOOKUP.get(name);
			return null;
		}

		static {
			for (FogDamageType type : values()) {
				NAME_LOOKUP.put(type.getName(), type);
			}
		}
		
		public boolean test(FogDamageType damageType) {
			return this == ANY || this == damageType;
		}
	}
}
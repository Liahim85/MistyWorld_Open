package ru.liahim.mist.api.advancement.criterion;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import ru.liahim.mist.api.advancement.FogDamagePredicate;
import ru.liahim.mist.api.advancement.FogDamagePredicate.FogDamageType;
import ru.liahim.mist.common.Mist;

public class FogDamageTrigger implements ICriterionTrigger<FogDamageTrigger.Instance> {

	private static final ResourceLocation ID = new ResourceLocation(Mist.MODID, "fog_damage");
	private final Map<PlayerAdvancements, FogDamageTrigger.Listeners> listeners = Maps.<PlayerAdvancements, FogDamageTrigger.Listeners> newHashMap();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addListener(PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<FogDamageTrigger.Instance> listener) {
		FogDamageTrigger.Listeners listeners = this.listeners.get(playerAdvancementsIn);

		if (listeners == null) {
			listeners = new FogDamageTrigger.Listeners(playerAdvancementsIn);
			this.listeners.put(playerAdvancementsIn, listeners);
		}

		listeners.add(listener);
	}

	@Override
	public void removeListener(PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<FogDamageTrigger.Instance> listener) {
		FogDamageTrigger.Listeners listeners = this.listeners.get(playerAdvancementsIn);

		if (listeners != null) {
			listeners.remove(listener);

			if (listeners.isEmpty()) {
				this.listeners.remove(playerAdvancementsIn);
			}
		}
	}

	@Override
	public void removeAllListeners(PlayerAdvancements playerAdvancements) {
		this.listeners.remove(playerAdvancements);
	}

	@Override
	public FogDamageTrigger.Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
		LocationPredicate location = LocationPredicate.deserialize(JsonUtils.getJsonObject(json, "location", new JsonObject()));
		FogDamagePredicate fogDamage = FogDamagePredicate.deserialize(JsonUtils.getJsonObject(json, "damage", new JsonObject()));
		return new Instance(location, fogDamage);
	}

	public void trigger(EntityPlayerMP player, World world, BlockPos pos, FogDamageType fogDamage, float centerDamage, @Nullable Boolean mask, @Nullable Boolean suit, @Nullable Boolean adsorbent) {
		FogDamageTrigger.Listeners listeners = this.listeners.get(player.getAdvancements());

		if (listeners != null) {
			listeners.trigger(world, pos, fogDamage, centerDamage, mask, suit, adsorbent);
		}
	}

	public static class Instance extends AbstractCriterionInstance {

		private final LocationPredicate location;
		private final FogDamagePredicate fogDamage;

		public Instance(LocationPredicate location, FogDamagePredicate fogDamage) {
			super(FogDamageTrigger.ID);
			this.location = location;
			this.fogDamage = fogDamage;
		}

		public boolean test(World world, BlockPos pos, FogDamageType damageType, float centerDamage, @Nullable Boolean mask, @Nullable Boolean suit, @Nullable Boolean adsorbent) {
			if (world instanceof WorldServer) {
				boolean loc = this.location == LocationPredicate.ANY || this.location.test((WorldServer)world, pos.getX(), pos.getY(), pos.getZ());
				return loc && this.fogDamage.test(damageType, centerDamage, mask, suit, adsorbent);
			}
			return false;
		}
	}

	static class Listeners {

		private final PlayerAdvancements playerAdvancements;
		private final Set<ICriterionTrigger.Listener<FogDamageTrigger.Instance>> listeners = Sets.<ICriterionTrigger.Listener<FogDamageTrigger.Instance>> newHashSet();

		public Listeners(PlayerAdvancements playerAdvancementsIn) {
			this.playerAdvancements = playerAdvancementsIn;
		}

		public boolean isEmpty() {
			return this.listeners.isEmpty();
		}

		public void add(ICriterionTrigger.Listener<FogDamageTrigger.Instance> listener) {
			this.listeners.add(listener);
		}

		public void remove(ICriterionTrigger.Listener<FogDamageTrigger.Instance> listener) {
			this.listeners.remove(listener);
		}

		public void trigger(World world, BlockPos pos, FogDamageType fogDamage, float centerDamage, @Nullable Boolean mask, @Nullable Boolean suit, @Nullable Boolean adsorbent) {
			List<ICriterionTrigger.Listener<FogDamageTrigger.Instance>> list = null;
			for (ICriterionTrigger.Listener<FogDamageTrigger.Instance> listener : this.listeners) {
				if (listener.getCriterionInstance().test(world, pos, fogDamage, centerDamage, mask, suit, adsorbent)) {
					if (list == null) {
						list = Lists.<ICriterionTrigger.Listener<FogDamageTrigger.Instance>> newArrayList();
					}
					list.add(listener);
				}
			}
			if (list != null) {
				for (ICriterionTrigger.Listener<FogDamageTrigger.Instance> listener1 : list) {
					listener1.grantCriterion(this.playerAdvancements);
				}
			}
		}
	}
}
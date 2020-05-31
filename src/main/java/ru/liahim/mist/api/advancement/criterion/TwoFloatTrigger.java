package ru.liahim.mist.api.advancement.criterion;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import ru.liahim.mist.api.advancement.BaseCriterionTrigger2;
import ru.liahim.mist.api.advancement.ICriterionInstanceTestable2;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;

public class TwoFloatTrigger extends BaseCriterionTrigger2<Float, Float, TwoFloatTrigger.Instance> {

	private final String name1;
	private final String name2;

	public TwoFloatTrigger(ResourceLocation res, String name1, String name2) {
		super(res);
		this.name1 = name1;
		this.name2 = name2;
	}

	@Override
	public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
		LocationPredicate location = LocationPredicate.deserialize(JsonUtils.getJsonObject(json, "location", new JsonObject()));
		MinMaxBounds value1 = MinMaxBounds.deserialize(json.get(this.name1));
		MinMaxBounds value2 = MinMaxBounds.deserialize(json.get(this.name2));
		return new Instance(getId(), location, value1, value2);
	}

	public class Instance extends AbstractCriterionInstance implements ICriterionInstanceTestable2<Float, Float> {

		private final LocationPredicate location;
		private final MinMaxBounds value1;
		private final MinMaxBounds value2;

		public Instance(ResourceLocation criterion, LocationPredicate location, MinMaxBounds value1, MinMaxBounds value2) {
			super(criterion);
			this.location = location;
			this.value1 = value1;
			this.value2 = value2;
		}

		@Override
		public boolean test(EntityPlayerMP player, Float value1, Float value2) {
			if (player.world instanceof WorldServer) {
				boolean loc = this.location.test((WorldServer) player.world, player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
				return loc && this.value1.test(value1) && this.value2.test(value2);
			}
			return false;
		}
	}
}
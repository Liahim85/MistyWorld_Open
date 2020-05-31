package ru.liahim.mist.api.advancement.criterion;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import ru.liahim.mist.api.advancement.BaseCriterionTrigger1;
import ru.liahim.mist.api.advancement.ICriterionInstanceTestable1;

public class AnimalTrigger extends BaseCriterionTrigger1<EntityAnimal, AnimalTrigger.Instance> {

	public AnimalTrigger(ResourceLocation res) {
		super(res);
	}

	@Override
	public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
		return new Instance(getId(), EntityPredicate.deserialize(json.get("entity")));
	}

	public static class Instance extends AbstractCriterionInstance implements ICriterionInstanceTestable1<EntityAnimal> {

		private final EntityPredicate entityPredicate;

		public Instance(ResourceLocation criterion, EntityPredicate entityPredicate) {
			super(criterion);
			this.entityPredicate = entityPredicate;
		}

		@Override
		public boolean test(EntityPlayerMP player, EntityAnimal entity) {
			return this.entityPredicate.test(player, entity);
		}
	}
}
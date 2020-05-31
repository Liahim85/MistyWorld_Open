package ru.liahim.mist.api.advancement.criterion;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import ru.liahim.mist.api.advancement.BaseCriterionTrigger2;
import ru.liahim.mist.api.advancement.ICriterionInstanceTestable2;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ItemStackAndFloatTrigger extends BaseCriterionTrigger2<ItemStack, Float, ItemStackAndFloatTrigger.Instance> {

	public ItemStackAndFloatTrigger(ResourceLocation res) {
		super(res);
	}

	@Override
	public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
		return new Instance(getId(), ItemPredicate.deserialize(json), MinMaxBounds.deserialize(null));
	}

	public static class Instance extends AbstractCriterionInstance implements ICriterionInstanceTestable2<ItemStack, Float> {

		private final ItemPredicate itemPredicate;
		private final MinMaxBounds value;

		public Instance(ResourceLocation criterion, ItemPredicate itemPredicate, MinMaxBounds value) {
			super(criterion);
			this.itemPredicate = itemPredicate;
			this.value = value;
		}

		@Override
		public boolean test(EntityPlayerMP player, ItemStack stack, Float value) {
			return this.itemPredicate.test(stack) && this.value.test(value);
		}
	}
}
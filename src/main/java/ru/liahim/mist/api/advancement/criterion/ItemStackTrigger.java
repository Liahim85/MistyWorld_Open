package ru.liahim.mist.api.advancement.criterion;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import ru.liahim.mist.api.advancement.BaseCriterionTrigger1;
import ru.liahim.mist.api.advancement.ICriterionInstanceTestable1;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ItemStackTrigger extends BaseCriterionTrigger1<ItemStack, ItemStackTrigger.Instance> {

	public ItemStackTrigger(ResourceLocation res) {
		super(res);
	}

	@Override
	public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
		return new Instance(getId(), ItemPredicate.deserialize(json));
	}

	public static class Instance extends AbstractCriterionInstance implements ICriterionInstanceTestable1<ItemStack> {

		private final ItemPredicate itemPredicate;

		public Instance(ResourceLocation criterion, ItemPredicate itemPredicate) {
			super(criterion);
			this.itemPredicate = itemPredicate;
		}

		@Override
		public boolean test(EntityPlayerMP player, ItemStack stack) {
			return this.itemPredicate.test(stack);
		}
	}
}
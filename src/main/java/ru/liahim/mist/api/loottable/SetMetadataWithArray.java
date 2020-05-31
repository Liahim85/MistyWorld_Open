package ru.liahim.mist.api.loottable;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import java.util.List;
import java.util.Random;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import ru.liahim.mist.common.Mist;

public class SetMetadataWithArray extends LootFunction {

	private final int[] array;

	public SetMetadataWithArray(LootCondition[] conditions, int[] array) {
		super(conditions);
		this.array = array;
	}

	@Override
	public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
		if (stack.isItemStackDamageable()) Mist.logger.warn("Couldn't set data of loot item {}", stack);
		else stack.setItemDamage(this.array[rand.nextInt(this.array.length)]);
		return stack;
	}

	public static class Serializer extends LootFunction.Serializer<SetMetadataWithArray> {

		protected Serializer() {
			super(new ResourceLocation(Mist.MODID, "set_array_data"), SetMetadataWithArray.class);
		}

		@Override
		public void serialize(JsonObject object, SetMetadataWithArray functionClazz, JsonSerializationContext serializationContext) {
			object.add("data", serializationContext.serialize(functionClazz.array));
		}

		@Override
		public SetMetadataWithArray deserialize(JsonObject object, JsonDeserializationContext deserializationContext, LootCondition[] conditions) {
			JsonElement jsonElement = object.get("data");
			int[] array;
			if (JsonUtils.isNumber(jsonElement)) {
				array = new int[] {JsonUtils.getInt(jsonElement, "value")};
			} else {
				List<Integer> list = Lists.<Integer>newArrayList();
				for (JsonElement jsonobject : JsonUtils.getJsonArray(jsonElement, "value")) {
					if (JsonUtils.isNumber(jsonobject)) {
						list.add(JsonUtils.getInt(jsonobject, "value"));
					}
				}
				array = new int[list.size()];
				for(int i = 0; i < list.size(); i++) array[i] = list.get(i);
			}
			return new SetMetadataWithArray(conditions, array);
		}
	}
}
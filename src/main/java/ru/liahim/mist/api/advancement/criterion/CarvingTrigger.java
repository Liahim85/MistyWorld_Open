package ru.liahim.mist.api.advancement.criterion;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import ru.liahim.mist.api.advancement.BaseCriterionTrigger3;
import ru.liahim.mist.api.advancement.ICriterionInstanceTestable3;
import ru.liahim.mist.common.Mist;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class CarvingTrigger extends BaseCriterionTrigger3<World, BlockPos, Boolean, CarvingTrigger.Instance> {

	public CarvingTrigger() {
		super(new ResourceLocation(Mist.MODID, "carving"));
	}

	@Override
	public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
		LocationPredicate location = LocationPredicate.deserialize(JsonUtils.getJsonObject(json, "location", new JsonObject()));
		ItemPredicate item = ItemPredicate.deserialize(JsonUtils.getJsonObject(json, "item", new JsonObject()));
		Boolean trunk = json.has("trunk") ? JsonUtils.getBoolean(json, "trunk") : null;
		return new Instance(getId(), location, item, trunk);
	}

	public class Instance extends AbstractCriterionInstance implements ICriterionInstanceTestable3<World, BlockPos, Boolean> {

		private final LocationPredicate location;
		private final ItemPredicate item;
		private final Boolean trunk;

		public Instance(ResourceLocation criterionIn, LocationPredicate location, ItemPredicate item, Boolean trunk) {
			super(criterionIn);
			this.location = location;
			this.item = item;
			this.trunk = trunk;
		}

		@Override
		public boolean test(EntityPlayerMP player, World world, BlockPos pos, Boolean trunk) {
			if (world instanceof WorldServer) {
				if (this.trunk == null || this.trunk == trunk) {
					if (this.location.test((WorldServer)world, pos.getX(), pos.getY(), pos.getZ())) {
						IBlockState state = world.getBlockState(pos);
						return this.item.test(new ItemStack(Item.getItemFromBlock(state.getBlock()), 1, state.getBlock().damageDropped(state)));
					}
				}
			}
			return false;
		}
	}
}
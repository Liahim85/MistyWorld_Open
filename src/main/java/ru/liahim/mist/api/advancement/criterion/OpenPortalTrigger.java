package ru.liahim.mist.api.advancement.criterion;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import ru.liahim.mist.api.advancement.BaseCriterionTrigger3;
import ru.liahim.mist.api.advancement.ICriterionInstanceTestable3;
import ru.liahim.mist.api.advancement.PortalType;
import ru.liahim.mist.common.Mist;

public class OpenPortalTrigger extends BaseCriterionTrigger3<World, BlockPos, PortalType, OpenPortalTrigger.Instance> {

	public OpenPortalTrigger() {
		super(new ResourceLocation(Mist.MODID, "open_portal"));
	}

	@Override
	public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
		LocationPredicate location = LocationPredicate.deserialize(json);
		PortalType type = PortalType.deserialize(json);
		return new Instance(getId(), location, type);
	}

	public class Instance extends AbstractCriterionInstance implements ICriterionInstanceTestable3<World, BlockPos, PortalType> {

		private final LocationPredicate location;
		private final PortalType type;

		public Instance(ResourceLocation criterionIn, LocationPredicate location, PortalType type) {
			super(criterionIn);
			this.location = location;
			this.type = type;
		}

		@Override
		public boolean test(EntityPlayerMP player, World world, BlockPos pos, PortalType type) {
			if (world instanceof WorldServer) {
				boolean loc = this.location.test((WorldServer)world, pos.getX(), pos.getY(), pos.getZ());
				return loc && this.type.test(type);
			}
			return false;
		}
	}
}
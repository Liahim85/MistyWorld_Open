package ru.liahim.mist.api.loottable;

import java.util.Random;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import ru.liahim.mist.capability.handler.ISkillCapaHandler.Skill;
import ru.liahim.mist.common.Mist;

public class MistSkillCutting implements LootCondition {

	private final int skill;
	private final boolean equal;

	public MistSkillCutting(int skill, boolean equal) {
		this.skill = skill;
		this.equal = equal;
	}

	@Override
	public boolean testCondition(Random rand, LootContext context) {
		if (this.skill <= 1) return true;
		else if (context.getKillerPlayer() instanceof EntityPlayer) {
			int i = Skill.getLevel((EntityPlayer) context.getKillerPlayer(), Skill.CUTTING);
			return this.equal ? i == this.skill : i >= this.skill;
		} return false;
	}

	public static class Serializer extends LootCondition.Serializer<MistSkillCutting> {

		protected Serializer() {
			super(new ResourceLocation(Mist.MODID, "skill_cutting"), MistSkillCutting.class);
		}

		@Override
		public void serialize(JsonObject json, MistSkillCutting value, JsonSerializationContext context) {
			json.addProperty("value", value.skill);
		}

		@Override
		public MistSkillCutting deserialize(JsonObject json, JsonDeserializationContext context) {
			return new MistSkillCutting(JsonUtils.getInt(json, "value"), JsonUtils.getBoolean(json, "equal", true));
		}
	}
}
package ru.liahim.mist.api.advancement;

import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;

public interface ICriterionInstanceTestable1<D> extends ICriterionInstance {

	public boolean test(EntityPlayerMP player, D criterionData);
}
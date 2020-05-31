package ru.liahim.mist.api.advancement;

import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;

public interface ICriterionInstanceTestable2<D1, D2> extends ICriterionInstance {

	public boolean test(EntityPlayerMP player, D1 criterionData1, D2 criterionData2);
}
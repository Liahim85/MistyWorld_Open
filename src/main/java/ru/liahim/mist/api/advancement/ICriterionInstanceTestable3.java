package ru.liahim.mist.api.advancement;

import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;

public interface ICriterionInstanceTestable3<D1, D2, D3> extends ICriterionInstance {

	public boolean test(EntityPlayerMP player, D1 criterionData1, D2 criterionData2, D3 criterionData3);
}
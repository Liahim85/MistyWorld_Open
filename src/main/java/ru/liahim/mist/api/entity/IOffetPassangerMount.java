package ru.liahim.mist.api.entity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IOffetPassangerMount {

	@SideOnly(Side.CLIENT)
	default public float[] getPassangerOffset(float[] vec, float limbSwing, float limbSwingAmount) {
		vec[0] = 0;
		vec[1] = 0;
		vec[2] = 0;
		return vec;
	}
}
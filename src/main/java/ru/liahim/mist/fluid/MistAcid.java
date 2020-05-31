package ru.liahim.mist.fluid;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

public class MistAcid extends Fluid {

	public static final String name = "mist.acid";
	public static final MistAcid instance = new MistAcid();

	public MistAcid() {
		super(name, new ResourceLocation("mist:blocks/acid_still"), new ResourceLocation("mist:blocks/acid_flow"));
	}
}
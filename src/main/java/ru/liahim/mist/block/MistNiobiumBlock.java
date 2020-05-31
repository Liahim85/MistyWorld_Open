package ru.liahim.mist.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class MistNiobiumBlock extends MistBlock {

	public MistNiobiumBlock() {
		super(Material.IRON, MapColor.IRON);
		this.setHardness(5.0F);
		this.setResistance(10.0F);
		this.setLightLevel(0.125F);
		this.setSoundType(SoundType.METAL);
	}
}
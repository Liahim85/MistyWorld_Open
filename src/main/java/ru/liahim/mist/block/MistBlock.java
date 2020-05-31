package ru.liahim.mist.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class MistBlock extends Block {

	public MistBlock(Material material) {
		super(material);
	}

	public MistBlock(Material material, MapColor mapColor) {
		super(material, mapColor);
	}

	@Override
	public String getUnlocalizedName() {
		return "tile.mist." + super.getUnlocalizedName().substring(5);
	}
}
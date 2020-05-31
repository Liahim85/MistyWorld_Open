package ru.liahim.mist.block.gizmos;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public abstract class MistBlockContainer extends BlockContainer {

	protected MistBlockContainer(Material material) {
		super(material);
	}

	protected MistBlockContainer(Material material, MapColor color) {
		super(material, color);
	}

	@Override
	public String getUnlocalizedName() {
		return "tile.mist." + super.getUnlocalizedName().substring(5);
	}
}
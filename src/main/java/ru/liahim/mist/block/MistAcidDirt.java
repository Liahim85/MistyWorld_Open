package ru.liahim.mist.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**@author Liahim*/
public class MistAcidDirt extends MistAcidSoil {

	public MistAcidDirt(Material material, float hardness, int waterPerm) {
		super(material, hardness, waterPerm);
	}

	public MistAcidDirt(float hardness, int waterPerm) {
		super(Material.GROUND, hardness, waterPerm);
	}

	@Override
	public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
		return MapColor.SILVER_STAINED_HARDENED_CLAY;
	}
}
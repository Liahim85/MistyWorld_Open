package ru.liahim.mist.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MistAcidSand extends MistBlockWettableFalling {

	public MistAcidSand() {
		super(Material.SAND, 3);
		this.setSoundType(SoundType.SAND);
		this.setHardness(0.5F);
		this.setHarvestLevel("shovel", 0);
	}

	@Override
	public String getUnlocalizedName() {
		return "tile.mist." + super.getUnlocalizedName().substring(5);
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected boolean showPorosityTooltip() { return false; }

	@Override
	public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
		return MapColor.YELLOW_STAINED_HARDENED_CLAY;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getDustColor(IBlockState state) {
		return 0x9B906D;
	}

	@Override
	public int getTopProtectPercent(IBlockState state, boolean isWet) {
		return isWet ? 60 : 145;
	}

	@Override
	public int getSideProtectPercent(IBlockState state, boolean isWet) {
		return isWet ? 30 : 145;
	}

	@Override
	public int getCloseProtectPercent(IBlockState state, boolean isWet) {
		return isWet ? 45 : 120;
	}

	@Override
	public boolean isAcid() {
		return true;
	}
}
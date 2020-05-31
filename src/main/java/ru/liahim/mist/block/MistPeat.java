package ru.liahim.mist.block;

import java.util.Random;

import ru.liahim.mist.world.MistWorld;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MistPeat extends MistBlockWettable {

	public MistPeat() {
		super(Material.GROUND, 2);
		this.setSoundType(SoundType.GROUND);
		this.setHardness(0.7F);
		this.setHarvestLevel("shovel", 0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected boolean showPorosityTooltip() { return false; }

	@Override
	public boolean doIfDry(World world, BlockPos pos, IBlockState state, boolean lava, Random rand) {
		if (rand.nextInt(8) == 0 && world.isAirBlock(pos.up())) {
			int humi = (int) MistWorld.getHumi(world, pos, 0);
			if (humi < 50) {
				humi = MathHelper.clamp(humi, 4, 32);
				if (rand.nextInt(humi) == 0) world.setBlockState(pos.up(), Blocks.FIRE.getDefaultState());
			}
		}
		return false;
	}

	@Override
	public boolean isFireSource(World world, BlockPos pos, EnumFacing side) {
		if (side != EnumFacing.UP || world.getBlockState(pos).getValue(WET)) return false;
		return world.rand.nextInt(16) > 0;
	}

	/*@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return world.getBlockState(pos).getValue(WET) ? 0 : 10;
    }*/

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return world.getBlockState(pos).getValue(WET) ? 0 : 10;
    }

	@Override
	public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction, IPlantable plantable) {
		EnumPlantType plantType = plantable.getPlantType(world, pos.offset(direction));
		switch (plantType) {
		case Plains:
		case Cave:
			return true;
		case Beach:
			return (world.getBlockState(pos.east()).getMaterial() == Material.WATER ||
					world.getBlockState(pos.west()).getMaterial() == Material.WATER ||
					world.getBlockState(pos.north()).getMaterial() == Material.WATER ||
					world.getBlockState(pos.south()).getMaterial() == Material.WATER);
		default:
			return false;
		}
	}

	@Override
	public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
		return MapColor.GRAY_STAINED_HARDENED_CLAY;
	}
}
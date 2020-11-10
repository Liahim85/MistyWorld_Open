package ru.liahim.mist.block;

import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.SoundType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class MistBlockFenceGate extends BlockFenceGate {

	private final int flammability;
	private final int fireSpeed;

	public MistBlockFenceGate(float hardness, int flammability, int fireSpeed) {
		super(BlockPlanks.EnumType.OAK);
		this.setHardness(hardness);
		this.setSoundType(SoundType.WOOD);
        this.flammability = flammability;
		this.fireSpeed = fireSpeed;
	}

	public MistBlockFenceGate(float hardness) {
		this(hardness, 20, 5);
	}

	@Override
	public String getUnlocalizedName() {
		return "tile.mist." + super.getUnlocalizedName().substring(5);
	}

	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return this.flammability;
	}

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return this.fireSpeed;
	}
}
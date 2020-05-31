package ru.liahim.mist.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class FacingHelper {

	/** Facings in D-N-S-W-E order */
	public static final EnumFacing[] NOTUP = new EnumFacing[] {EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST};
	/** Facings in U-N-S-W-E order */
	public static final EnumFacing[] NOTDOWN = new EnumFacing[] {EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST};

	/** Angel offset for BlockPos, including diagonal directions */
	public static BlockPos getAngelOffset(double angle, BlockPos pos) {
		angle %= 360;
		if (angle < 67.5 || angle >= 292.5) pos = pos.south();
		else if (angle >= 112.5 && angle < 247.5) pos = pos.north();
		if (angle >= 22.5 && angle < 157.5) pos = pos.west();
		else if (angle >= 202.5 && angle < 337.5) pos = pos.east();
		return pos;
	}
}
package ru.liahim.mist.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PathNavigateWaterMistUpper extends PathNavigateGroundMistUpper {

	public PathNavigateWaterMistUpper(EntityLiving entity, World world) {
		super(entity, world);
	}

	@Override
	protected PathFinder getPathFinder() {
		this.nodeProcessor = new WalkNodeProcessorMistUpper();
		this.nodeProcessor.setCanEnterDoors(true);
		return new PathFinder(this.nodeProcessor);
	}

	private boolean isSafeToStandAt(int x, int y, int z, int sizeX, int sizeY, int sizeZ, Vec3d vec31, double p_179683_8_, double p_179683_10_) {
		int i = x - sizeX / 2;
		int j = z - sizeZ / 2;
		if (!this.isPositionClear(i, y, j, sizeX, sizeY, sizeZ, vec31, p_179683_8_, p_179683_10_)) {
			return false;
		} else {
			for (int k = i; k < i + sizeX; ++k) {
				for (int l = j; l < j + sizeZ; ++l) {
					double d0 = k + 0.5D - vec31.x;
					double d1 = l + 0.5D - vec31.z;

					if (d0 * p_179683_8_ + d1 * p_179683_10_ >= 0.0D) {
						PathNodeType pathnodetype = this.nodeProcessor.getPathNodeType(this.world, k, y - 1, l, this.entity, sizeX, sizeY, sizeZ, true, true);

						if (pathnodetype == PathNodeType.LAVA) {
							return false;
						}

						if (pathnodetype == PathNodeType.OPEN) {
							return false;
						}

						pathnodetype = this.nodeProcessor.getPathNodeType(this.world, k, y, l, this.entity, sizeX, sizeY, sizeZ, true, true);
						float f = this.entity.getPathPriority(pathnodetype);

						if (f < 0.0F || f >= 8.0F) {
							return false;
						}

						if (pathnodetype == PathNodeType.DAMAGE_FIRE || pathnodetype == PathNodeType.DANGER_FIRE || pathnodetype == PathNodeType.DAMAGE_OTHER) {
							return false;
						}
					}
				}
			}

			return true;
		}
	}

	private boolean isPositionClear(int x, int y, int z, int sizeX, int sizeY, int sizeZ, Vec3d p_179692_7_, double p_179692_8_, double p_179692_10_) {
		for (BlockPos blockpos : BlockPos.getAllInBox(new BlockPos(x, y, z), new BlockPos(x + sizeX - 1, y + sizeY - 1, z + sizeZ - 1))) {
			double d0 = blockpos.getX() + 0.5D - p_179692_7_.x;
			double d1 = blockpos.getZ() + 0.5D - p_179692_7_.z;

			if (d0 * p_179692_8_ + d1 * p_179692_10_ >= 0.0D) {
				Block block = this.world.getBlockState(blockpos).getBlock();

				if (!block.isPassable(this.world, blockpos)) {
					return false;
				}
			}
		}

		return true;
	}

}
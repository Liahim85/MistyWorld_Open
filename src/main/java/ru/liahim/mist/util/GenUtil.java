package ru.liahim.mist.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ru.liahim.mist.common.Mist;

public class GenUtil {

	public final World world;
	public final GenSet set;

	public GenUtil(World world, GenSet set) {
		this.world = world;
		this.set = set;
	}

	public BlockPos getPos(BlockPos pos) {
		return transformedBlockPos(pos, this.set);
	}

	public IBlockState getBlockState(BlockPos pos) {
		return getBlockState(this.world, pos, this.set);
	}

	public TileEntity getTileEntity(BlockPos pos) {
		return getTileEntity(this.world, pos, this.set);
	}

	public boolean setBlockState(BlockPos pos, IBlockState state) {
		return setBlockState(this.world, pos, state, this.set, Mist.FLAG);
	}

	public boolean setBlockState(BlockPos pos, IBlockState state, int flag) {
		return setBlockState(this.world, pos, state, this.set, flag);
	}

	public static boolean setBlockState(World world, BlockPos pos, IBlockState state, GenSet set) {
		return setBlockState(world, pos, state, set, Mist.FLAG);
	}

	public static IBlockState getBlockState(World world, BlockPos pos, GenSet set) {
		pos = transformedBlockPos(pos, set);
		return world.getBlockState(pos);
	}

	public static TileEntity getTileEntity(World world, BlockPos pos, GenSet set) {
		pos = transformedBlockPos(pos, set);
		return world.getTileEntity(pos);
	}

	public static boolean setBlockState(World world, BlockPos pos, IBlockState state, GenSet set, int flag) {
		pos = transformedBlockPos(pos, set);
		state = state.withMirror(set.mirror);
		state = state.withRotation(set.rotation);
		return world.setBlockState(pos, state, flag);
	}

	public static BlockPos transformedBlockPos(BlockPos pos, BlockPos center, Rotation rotation, Mirror mirror) {
		int centerX = center.getX(), centerZ = center.getZ();
		int x = pos.getX() - centerX, z = pos.getZ() - centerZ;
		boolean flag = true;

		switch (mirror) {
			case LEFT_RIGHT: z = -z; break;
			case FRONT_BACK: x = -x; break;
			default: flag = false;
		}

		switch (rotation) {
			case COUNTERCLOCKWISE_90: return new BlockPos(z + centerX, pos.getY(), -x + centerZ);
			case CLOCKWISE_90: return new BlockPos(-z + centerX, pos.getY(), x + centerZ);
			case CLOCKWISE_180: return new BlockPos(-x + centerX, pos.getY(), -z + centerZ);
			default: return flag ? new BlockPos(x + centerX, pos.getY(), z + centerZ) : pos;
		}
	}

	public static BlockPos transformedBlockPos(BlockPos pos, GenSet set) {
		return transformedBlockPos(pos, set.center, set.rotation, set.mirror);
	}

	public GenUtil add(BlockPos center, Rotation rotation, Mirror mirror) {
		Rotation rotationOut = rotation;
		Mirror mirrorOut;
		if (this.set.mirror == Mirror.NONE) mirrorOut = mirror;
		else if (rotation == Rotation.NONE || rotation == Rotation.CLOCKWISE_180) {
			if (mirror == Mirror.NONE) mirrorOut = this.set.mirror;
			else if (mirror == this.set.mirror) mirrorOut = Mirror.NONE;
			else {
				rotationOut = rotation.add(Rotation.CLOCKWISE_180);
				mirrorOut = Mirror.NONE;
			}
		} else {
			if (mirror == Mirror.NONE) mirrorOut = this.set.mirror == Mirror.FRONT_BACK ? Mirror.LEFT_RIGHT : Mirror.FRONT_BACK;
			else if (mirror != this.set.mirror) mirrorOut = Mirror.NONE;
			else {
				rotationOut = rotation.add(Rotation.CLOCKWISE_180);
				mirrorOut = Mirror.NONE;
			}
		}
		return new GenUtil(this.world, new GenSet(this.getPos(center), this.set.rotation.add(rotationOut), mirrorOut));
	}

	public static class GenSet {

		public final BlockPos center;
		public final Rotation rotation;
		public final Mirror mirror;

		public GenSet(BlockPos center, Rotation rotation, Mirror mirror) {
			this.center = center;
			this.rotation = rotation;
			this.mirror = mirror;
		}

		public GenSet(BlockPos center, EnumFacing face, Mirror mirror) {
			this(center, Rotation.values()[face.getHorizontalIndex()], mirror);
		}
	}
}
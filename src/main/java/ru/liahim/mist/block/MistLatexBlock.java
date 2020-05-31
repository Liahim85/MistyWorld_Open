package ru.liahim.mist.block;

import javax.annotation.Nullable;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class MistLatexBlock extends MistBlock {

	protected static final AxisAlignedBB LATEX_AABB = new AxisAlignedBB(0.0625D, 0.0625D, 0.0625D, 0.9375D, 0.9375D, 0.9375D);

	public MistLatexBlock() {
		super(Material.CLAY, MapColor.QUARTZ);
		this.setHardness(0.5F);
		this.setSoundType(SoundType.SLIME);
		this.setHarvestLevel("shovel", 0);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return FULL_BLOCK_AABB;
	}

	@Override
	@Nullable
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return LATEX_AABB;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public void onFallenUpon(World world, BlockPos pos, Entity entity, float fallDistance) {
		entity.fall(fallDistance, 0.3F);
	}

	@Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
		entity.setInWeb();
    }

}
package ru.liahim.mist.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MistRubberBlock extends MistBlock {

	public MistRubberBlock() {
		super(Material.CLAY, MapColor.GRAY);
		this.setHardness(0.6F);
		this.setSoundType(SoundType.CLOTH);
	}

	@Override
	public void onFallenUpon(World world, BlockPos pos, Entity entity, float fallDistance) {
		if (entity.isSneaking()) super.onFallenUpon(world, pos, entity, fallDistance);
		else entity.fall(fallDistance, 0.0F);
	}

	@Override
	public void onLanded(World world, Entity entity) {
		if (entity.isSneaking()) super.onLanded(world, entity);
		else if (entity.motionY < 0.0D) {
			entity.motionY = -entity.motionY;
			if (!(entity instanceof EntityLivingBase)) entity.motionY *= 0.8D;
			if (Math.abs(entity.motionY) < 0.1D) entity.motionY = 0;
		}
	}

	@Override
	public void onEntityWalk(World world, BlockPos pos, Entity entity) {
		if (Math.abs(entity.motionY) < 0.1D && !entity.isSneaking()) {
			double d0 = 0.4D + Math.abs(entity.motionY) * 0.2D;
			entity.motionX *= d0;
			entity.motionZ *= d0;
		}
		super.onEntityWalk(world, pos, entity);
	}
}
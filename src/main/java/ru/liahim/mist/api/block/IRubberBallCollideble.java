package ru.liahim.mist.api.block;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import ru.liahim.mist.entity.EntityRubberBall;

public interface IRubberBallCollideble {

	public boolean isCollide(World world, IBlockState state, EntityRubberBall ball, RayTraceResult result, Random rand);
}
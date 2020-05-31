package ru.liahim.mist.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.world.IBlockAccess;
import ru.liahim.mist.world.MistWorld;

public class WalkNodeProcessorMistUpper extends WalkNodeProcessor {

	@Override
	public PathNodeType getPathNodeType(IBlockAccess world, int x, int y, int z, EntityLiving entity, int xSize, int ySize, int zSize, boolean canBreakDoors, boolean canEnterDoors) {
		if (entity.posY > y && MistWorld.isPosInFog(entity.world, y)) return PathNodeType.OPEN;
		return super.getPathNodeType(world, x, y, z, entity, xSize, ySize, zSize, canBreakDoors, canEnterDoors);
	}
}
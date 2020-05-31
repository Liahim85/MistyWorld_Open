package ru.liahim.mist.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.world.World;

public class PathNavigateGroundMistUpper extends PathNavigateGround {

	public PathNavigateGroundMistUpper(EntityLiving entity, World world) {
		super(entity, world);
	}
	
	@Override
	protected PathFinder getPathFinder() {
        this.nodeProcessor = new WalkNodeProcessorMistUpper();
        this.nodeProcessor.setCanEnterDoors(true);
        return new PathFinder(this.nodeProcessor);
    }
}
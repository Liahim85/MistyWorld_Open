package ru.liahim.mist.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.world.World;

public class PathNavigateGroundMistUpperSwamp extends PathNavigateGround {

	public PathNavigateGroundMistUpperSwamp(EntityLiving entity, World world) {
		super(entity, world);
	}
	
	@Override
	protected PathFinder getPathFinder() {
        this.nodeProcessor = new WalkNodeProcessorMistUpperSwamp();
        this.nodeProcessor.setCanEnterDoors(true);
        return new PathFinder(this.nodeProcessor);
    }
}
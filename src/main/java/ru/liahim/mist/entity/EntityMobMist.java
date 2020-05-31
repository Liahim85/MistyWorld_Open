package ru.liahim.mist.entity;

import net.minecraft.entity.monster.EntityMob;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.world.World;
import ru.liahim.mist.api.entity.IMatWalkable;
import ru.liahim.mist.entity.ai.PathNavigateGroundMistUpper;
import ru.liahim.mist.entity.ai.PathNavigateGroundMistUpperSwamp;

public class EntityMobMist extends EntityMob {

	public EntityMobMist(World world) {
		super(world);
	}

	@Override
	protected PathNavigate createNavigator(World world) {
		return this instanceof IMatWalkable ? new PathNavigateGroundMistUpper(this, world) : new PathNavigateGroundMistUpperSwamp(this, world);
	}
}
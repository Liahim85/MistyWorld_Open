package ru.liahim.mist.entity.ai;

import java.util.List;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIFollowSame extends EntityAIBase {

	EntityLiving entity;
	EntityLiving neighbor;
    double moveSpeed;
    private int delayCounter;
    private final double stopDistance;
    private final double areaSize;

    public EntityAIFollowSame(EntityLiving animal, double speed, double stopDistance, double areaSize) {
        this.entity = animal;
        this.moveSpeed = speed;
        this.stopDistance = stopDistance;
        this.areaSize = areaSize;
    }

    @Override
	public boolean shouldExecute() {
        if (this.entity.isChild()) return false;
        else {
            List<EntityLiving> list = this.entity.world.<EntityLiving>getEntitiesWithinAABB(this.entity.getClass(), this.entity.getEntityBoundingBox().grow(this.areaSize, 8.0D, this.areaSize));
            EntityLiving neighbor = null;
            double d0 = Double.MAX_VALUE;
            for (EntityLiving entity : list) {
                if (entity != this.entity && !entity.isChild()) {
                    double d1 = this.entity.getDistanceSq(entity);
            		if (d1 <= d0) {
                        d0 = d1;
                        neighbor = entity;
                    }
                }
            }

            if (neighbor == null) return false;
            else if (d0 < this.stopDistance * this.stopDistance) return false;
            else {
                this.neighbor = neighbor;
                return true;
            }
        }
    }

    @Override
	public boolean shouldContinueExecuting() {
        if (this.entity.isChild()) return false;
        else if (!this.neighbor.isEntityAlive()) return false;
        else {
            double d0 = this.entity.getDistanceSq(this.neighbor);
            return d0 >= this.stopDistance * this.stopDistance && d0 <= this.areaSize * this.areaSize;
        }
    }

    @Override
	public void startExecuting() {
        this.delayCounter = 0;
    }

    @Override
	public void resetTask() {
        this.neighbor = null;
    }

    @Override
	public void updateTask() {
        if (--this.delayCounter <= 0) {
            this.delayCounter = 10;
            this.entity.getNavigator().tryMoveToEntityLiving(this.neighbor, this.moveSpeed);
        }
    }
}
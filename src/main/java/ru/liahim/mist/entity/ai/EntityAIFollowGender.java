package ru.liahim.mist.entity.ai;

import java.util.List;

import net.minecraft.entity.ai.EntityAIBase;
import ru.liahim.mist.entity.EntityGender;

public class EntityAIFollowGender extends EntityAIBase {

	EntityGender entity;
    EntityGender male;
    double moveSpeed;
    private int delayCounter;

    public EntityAIFollowGender(EntityGender animal, double speed) {
        this.entity = animal;
        this.moveSpeed = speed;
    }

    @Override
	public boolean shouldExecute() {
        if (this.entity.isChild() || !entity.isFemale()) return false;
        else {
            List<EntityGender> list = this.entity.world.<EntityGender>getEntitiesWithinAABB(this.entity.getClass(), this.entity.getEntityBoundingBox().grow(24.0D, 8.0D, 24.0D));
            EntityGender male = null;
            double d0 = Double.MAX_VALUE;
            for (EntityGender entity : list) {
                if (!entity.isChild() && !entity.isFemale()) {
                    double d1 = this.entity.getDistanceSq(entity);
            		if (d1 <= d0) {
                        d0 = d1;
                        male = entity;
                    }
                }
            }

            if (male == null) return false;
            else if (d0 < 256.0D) return false;
            else {
                this.male = male;
                return true;
            }
        }
    }

    @Override
	public boolean shouldContinueExecuting() {
        if (this.entity.isChild()) return false;
        else if (!this.male.isEntityAlive()) return false;
        else {
            double d0 = this.entity.getDistanceSq(this.male);
            return d0 >= 25.0D && d0 <= 576.0D;
        }
    }

    @Override
	public void startExecuting() {
        this.delayCounter = 0;
    }

    @Override
	public void resetTask() {
        this.male = null;
    }

    @Override
	public void updateTask() {
        if (--this.delayCounter <= 0) {
            this.delayCounter = 10;
            this.entity.getNavigator().tryMoveToEntityLiving(this.male, this.moveSpeed);
        }
    }
}
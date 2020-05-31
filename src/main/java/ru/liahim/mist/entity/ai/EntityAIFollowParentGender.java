package ru.liahim.mist.entity.ai;

import java.util.List;

import net.minecraft.entity.ai.EntityAIBase;
import ru.liahim.mist.entity.EntityGender;

public class EntityAIFollowParentGender extends EntityAIBase {

	EntityGender child;
    EntityGender parent;
    double moveSpeed;
    private int delayCounter;

    public EntityAIFollowParentGender(EntityGender animal, double speed) {
        this.child = animal;
        this.moveSpeed = speed;
    }

    @Override
	public boolean shouldExecute() {
        if (!this.child.isChild()) return false;
        else {
            List<EntityGender> list = this.child.world.<EntityGender>getEntitiesWithinAABB(this.child.getClass(), this.child.getEntityBoundingBox().grow(8.0D, 4.0D, 8.0D));
            EntityGender parent = null;
            boolean female = false;
            double d0 = Double.MAX_VALUE;
            for (EntityGender entity : list) {
                if (entity.getGrowingAge() >= 0) {
                	boolean currentFemale = entity.isFemale();
                	if (!female || currentFemale) {
                        double d1 = this.child.getDistanceSq(entity);
                		if (d1 <= d0 || (!female && currentFemale)) {
	                        d0 = d1;
	                        parent = entity;
	                    }
                    }
                	if (!female) female = currentFemale;
                }
            }

            if (parent == null) return false;
            else if (d0 < 9.0D) return false;
            else {
                this.parent = parent;
                return true;
            }
        }
    }

    @Override
	public boolean shouldContinueExecuting() {
        if (!this.child.isChild()) return false;
        else if (!this.parent.isEntityAlive()) return false;
        else {
            double d0 = this.child.getDistanceSq(this.parent);
            return d0 >= 9.0D && d0 <= 256.0D;
        }
    }

    @Override
	public void startExecuting() {
        this.delayCounter = 0;
    }

    @Override
	public void resetTask() {
        this.parent = null;
    }

    @Override
	public void updateTask() {
        if (--this.delayCounter <= 0) {
            this.delayCounter = 10;
            this.child.getNavigator().tryMoveToEntityLiving(this.parent, this.moveSpeed);
        }
    }
}
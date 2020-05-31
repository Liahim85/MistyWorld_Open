package ru.liahim.mist.entity.ai;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigateGround;
import ru.liahim.mist.entity.EntityAnimalMist;

public class EntityAITemptTamed extends EntityAIBase {

    private final EntityAnimalMist temptedEntity;
    private final double speed;
    private final double tamedSpeed;
    private final double distance;
    private double targetX;
    private double targetY;
    private double targetZ;
    private double pitch;
    private double yaw;
    private EntityPlayer temptingPlayer;
    private int delayTemptCounter;
    private boolean isRunning;
    private final Set<ItemStack> temptStacks;
    private final boolean scaredByPlayerMovement;

    public EntityAITemptTamed(EntityAnimalMist temptedEntity, double speed, double tamedSpeed, ItemStack temptStacks, boolean scaredByPlayerMovement) {
        this(temptedEntity, speed, tamedSpeed, scaredByPlayerMovement, Sets.newHashSet(temptStacks));
    }

    public EntityAITemptTamed(EntityAnimalMist temptedEntity, double speed, double tamedSpeed, boolean scaredByPlayerMovement, Set<ItemStack> temptStacks) {
    	this(temptedEntity, speed, tamedSpeed, scaredByPlayerMovement, temptStacks, 10.0D);
    }

    public EntityAITemptTamed(EntityAnimalMist temptedEntity, double speed, double tamedSpeed, boolean scaredByPlayerMovement, Set<ItemStack> temptStacks, double distance) {
        this.temptedEntity = temptedEntity;
        this.speed = speed;
        this.tamedSpeed = tamedSpeed;
        this.distance = distance;
        this.temptStacks = temptStacks;
        this.scaredByPlayerMovement = scaredByPlayerMovement;
        this.setMutexBits(3);
        if (!(temptedEntity.getNavigator() instanceof PathNavigateGround)) {
            throw new IllegalArgumentException("Unsupported mob type for TemptGoal");
        }
    }

    @Override
	public boolean shouldExecute() {
    	if (!this.temptedEntity.canBeTempted()) return false;
        if (this.delayTemptCounter > 0) {
            --this.delayTemptCounter;
            return false;
        } else {
            this.temptingPlayer = this.temptedEntity.world.getClosestPlayerToEntity(this.temptedEntity, this.distance);
            if (this.temptingPlayer == null) return false;
            else return this.temptedEntity.canBeTemptedByEntity(this.temptingPlayer) &&
            		(this.isTempting(this.temptingPlayer.getHeldItemMainhand()) ||
            		this.isTempting(this.temptingPlayer.getHeldItemOffhand())) && this.temptedEntity.childCheck();
        }
    }

    public boolean isTempting(ItemStack stack) {
		for (ItemStack st : this.temptStacks) {
			if (stack.getItem() == st.getItem() && stack.getItemDamage() == st.getItemDamage()) {
				return true;
			}
		}
        return false;
    }

    @Override
	public boolean shouldContinueExecuting() {
        if (this.scaredByPlayerMovement && !this.temptedEntity.isTamedByEntity(this.temptingPlayer)) {
            if (this.temptedEntity.getDistanceSq(this.temptingPlayer) < 36.0D) {
                if (this.temptingPlayer.getDistanceSq(this.targetX, this.targetY, this.targetZ) > 0.01D) return false;
                if (Math.abs(this.temptingPlayer.rotationPitch - this.pitch) > 5.0D || Math.abs(this.temptingPlayer.rotationYaw - this.yaw) > 5.0D) return false;
            } else {
                this.targetX = this.temptingPlayer.posX;
                this.targetY = this.temptingPlayer.posY;
                this.targetZ = this.temptingPlayer.posZ;
            }
            this.pitch = this.temptingPlayer.rotationPitch;
            this.yaw = this.temptingPlayer.rotationYaw;
        }
        return this.shouldExecute();
    }

    @Override
	public void startExecuting() {
        this.targetX = this.temptingPlayer.posX;
        this.targetY = this.temptingPlayer.posY;
        this.targetZ = this.temptingPlayer.posZ;
        this.isRunning = true;
    }

    @Override
	public void resetTask() {
        this.temptingPlayer = null;
        this.temptedEntity.getNavigator().clearPath();
        this.delayTemptCounter = 100;
        this.isRunning = false;
    }

    @Override
	public void updateTask() {
        this.temptedEntity.getLookHelper().setLookPositionWithEntity(this.temptingPlayer, this.temptedEntity.getHorizontalFaceSpeed() + 20, this.temptedEntity.getVerticalFaceSpeed());
        if (this.temptedEntity.getDistanceSq(this.temptingPlayer) < 6.25D) {
            this.temptedEntity.getNavigator().clearPath();
        } else {
            this.temptedEntity.getNavigator().tryMoveToEntityLiving(this.temptingPlayer, this.temptedEntity.isTamed() ? this.tamedSpeed : this.speed);
        }
    }

    public boolean isRunning() {
        return this.isRunning;
    }
}
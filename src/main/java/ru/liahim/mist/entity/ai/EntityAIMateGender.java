package ru.liahim.mist.entity.ai;

import java.util.List;
import java.util.Random;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import ru.liahim.mist.entity.EntityGender;

public class EntityAIMateGender extends EntityAIBase {

	private final EntityGender animal;
	private final Class<? extends EntityGender> mateClass;
	World world;
	private EntityGender targetMate;
	int spawnBabyDelay;
	double moveSpeed;

	public EntityAIMateGender(EntityGender animal, double speed) {
        this(animal, speed, animal.getClass());
    }

	public EntityAIMateGender(EntityGender animal, double speed, Class <? extends EntityGender > mateClass) {
        this.animal = animal;
        this.world = animal.world;
        this.mateClass = mateClass;
        this.moveSpeed = speed;
        this.setMutexBits(3);
    }

	@Override
	public boolean shouldExecute() {
		if (!this.animal.isInLove()) return false;
		else {
			this.targetMate = this.getNearbyMate();
			return this.targetMate != null;
		}
	}

	@Override
	public boolean shouldContinueExecuting() {
		return this.targetMate.isEntityAlive() && this.targetMate.isInLove() && this.spawnBabyDelay < 60;
	}

	@Override
	public void resetTask() {
		this.targetMate = null;
		this.spawnBabyDelay = 0;
	}

	@Override
	public void updateTask() {
		this.animal.getLookHelper().setLookPositionWithEntity(this.targetMate, 10.0F, this.animal.getVerticalFaceSpeed());
		this.animal.getNavigator().tryMoveToEntityLiving(this.targetMate, this.moveSpeed);
		++this.spawnBabyDelay;
		
		if (this.spawnBabyDelay >= 60 && this.animal.getDistanceSq(this.targetMate) < 9.0D) {
			this.spawnBaby();
		}
	}

	private EntityGender getNearbyMate() {
		List<EntityGender> list = this.world.<EntityGender>getEntitiesWithinAABB(this.mateClass, this.animal.getEntityBoundingBox().grow(8.0D));
		double d0 = Double.MAX_VALUE;
		EntityGender entityanimal = null;

		for (EntityGender entityanimal1 : list) {
			if (this.animal.canMateWith(entityanimal1) && this.animal.getDistanceSq(entityanimal1) < d0) {
				entityanimal = entityanimal1;
				d0 = this.animal.getDistanceSq(entityanimal1);
			}
		}

		return entityanimal;
	}

	private void spawnBaby() {
		if (!this.animal.isFemale()) return;
		int count = this.animal.getChildCount();
		if (count <= 0) return;
		EntityAgeable[] childList = new EntityAgeable[count];
		for (int i = 0; i < count; ++i) {
			EntityAgeable child = this.animal.createChild(this.targetMate);
			final BabyEntitySpawnEvent event = new BabyEntitySpawnEvent(animal, targetMate, child);
			final boolean cancelled = MinecraftForge.EVENT_BUS.post(event);
			child = event.getChild();
			if (cancelled || child == null) {
				this.animal.setGrowingAge(6000);
				this.targetMate.setGrowingAge(6000);
				this.animal.resetInLove();
				this.targetMate.resetInLove();
				return;
			}
			childList[i] = child;
		}
		
		EntityPlayerMP player = this.animal.getLoveCause();
		if (player == null && this.targetMate.getLoveCause() != null) {
			player = this.targetMate.getLoveCause();
		}
		if (player != null) {
			player.addStat(StatList.ANIMALS_BRED);
			CriteriaTriggers.BRED_ANIMALS.trigger(player, this.animal, this.targetMate, childList[0]);
		}
		
		NBTTagList tagList = new NBTTagList();
		for (EntityAgeable child : childList) {
			child.setGrowingAge(-24000);
			tagList.appendTag(child.serializeNBT());
		}

		this.animal.setChild(tagList);
		this.animal.setGrowingAge(6000);
		this.targetMate.setGrowingAge(6000);
		this.animal.resetInLove();
		this.targetMate.resetInLove();
		
		Random random = this.animal.getRNG();
		for (int i = 0; i < 7; ++i) {
			double d0 = random.nextGaussian() * 0.02D;
			double d1 = random.nextGaussian() * 0.02D;
			double d2 = random.nextGaussian() * 0.02D;
			double d3 = random.nextDouble() * this.animal.width * 2.0D - this.animal.width;
			double d4 = 0.5D + random.nextDouble() * this.animal.height;
			double d5 = random.nextDouble() * this.animal.width * 2.0D - this.animal.width;
			this.world.spawnParticle(EnumParticleTypes.HEART, this.animal.posX + d3, this.animal.posY + d4, this.animal.posZ + d5, d0, d1, d2);
		}
		if (this.world.getGameRules().getBoolean("doMobLoot")) {
			this.world.spawnEntity(new EntityXPOrb(this.world, this.animal.posX, this.animal.posY, this.animal.posZ, random.nextInt(7) + 1));
		}
	}
}
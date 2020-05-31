package ru.liahim.mist.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityWaterMobMist extends EntityMobMist {

	public EntityWaterMobMist(World world) {
		super(world);
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public boolean getCanSpawnHere() {
		return true;
	}

	@Override
	public boolean isNotColliding() {
		return this.world.checkNoEntityCollision(this.getEntityBoundingBox(), this);
	}

	@Override
	public int getTalkInterval() {
		return 120;
	}

	@Override
	protected boolean canDespawn() {
		return true;
	}

	@Override
	protected int getExperiencePoints(EntityPlayer player) {
		return 1 + this.world.rand.nextInt(3);
	}

	@Override
	public void onEntityUpdate() {
		int i = this.getAir();
		super.onEntityUpdate();
		if (this.isEntityAlive() && !this.isInWater()) {
			--i;
			this.setAir(i);
			if (this.getAir() == -20) {
				this.setAir(0);
				this.attackEntityFrom(DamageSource.DROWN, 0.5F);
			}
		} else this.setAir(this.getAirVolum());
	}

	protected int getAirVolum() {
		return 300;
	}

	@Override
	public boolean isPushedByWater() {
		return false;
	}
}
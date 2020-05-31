package ru.liahim.mist.entity;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public abstract class EntityAlbino extends EntityGender {

	private static final DataParameter<Byte> ALBINO = EntityDataManager.<Byte>createKey(EntityAlbino.class, DataSerializers.BYTE);

	public EntityAlbino(World world) {
		super(world);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(ALBINO, (byte) -1);
	}

	public void setAlbino(int i) {
		if (i < -1) i = -1;
		if (i > 2) i = 2;
		this.dataManager.set(ALBINO, (byte) i);
	}

	public byte getAlbino() {
		return this.dataManager.get(ALBINO);
	}

	public boolean isAlbino() {
		return this.getAlbino() > 0;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setByte("Albino", this.getAlbino());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		this.setAlbino(compound.getByte("Albino"));
	}

	@Override
	@Nullable
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
		if (this.getAlbino() < 0) this.setAlbino(this.rand.nextInt(500) == 0 ? 1 : 0);
		return super.onInitialSpawn(difficulty, livingdata);
	}

	@Override
	@Nullable
	public EntityAgeable createChild(EntityAgeable ageable) {
		if (!(ageable instanceof EntityAlbino)) return null;
		EntityAlbino child = (EntityAlbino)super.createChild(ageable);
		byte i = this.getAlbino();
		byte j = ((EntityAlbino)ageable).getAlbino();
		if (i > 0 && j > 0) child.setAlbino(this.rand.nextInt(5 - i - j) == 0 ? 2 : -1);
		return child;
	}
}
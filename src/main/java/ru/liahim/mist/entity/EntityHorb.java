package ru.liahim.mist.entity;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.api.loottable.LootTables;
import ru.liahim.mist.api.sound.MistSounds;
import ru.liahim.mist.common.MistTime;
import ru.liahim.mist.entity.ai.EntityAIFollowParentGender;
import ru.liahim.mist.entity.ai.EntityAIMateGender;
import ru.liahim.mist.entity.ai.EntityAITemptTamed;

public class EntityHorb extends EntityAlbino {

	private static final Set<ItemStack> TEMPTATION_STACKS = Sets.newHashSet(new ItemStack(MistItems.MUSHROOMS_FOOD, 1, 3), new ItemStack(MistItems.MUSHROOMS_FOOD, 1, 5));
	private static long pregnantTime = MistTime.getDayInMonth() * 32000;
	public final int animShift;

	public EntityHorb(World world) {
		super(world);
		this.animShift = this.rand.nextInt(360);
		this.setSize(1.6F, 2.5F);
	}

	@Override
	protected void initEntityAI() {
		this.aiTempt = new EntityAITemptTamed(this, 0.6D, 1.2D, true, TEMPTATION_STACKS);
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAIPanic(this, 2.0D));
		this.tasks.addTask(2, new EntityAIMateGender(this, 1.0D));
		this.tasks.addTask(3, this.aiTempt);
		this.tasks.addTask(4, new EntityAIFollowParentGender(this, 1.1D));
		this.tasks.addTask(5, new EntityAIAvoidEntity(this, EntityPlayer.class, 16, 1.0D, 2.0D));
		this.tasks.addTask(6, new EntityAIAvoidEntity(this, EntityMonk.class, 17, 1.5D, 2.0D));
		this.tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1.0D));
		this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
		this.tasks.addTask(9, new EntityAILookIdle(this));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
	}

	@Override
	public int getTalkInterval() {
		return 400;
	}

	@Override
	protected long getPregnantTime() {
		return pregnantTime;
	}

	@Override
	public int getChildCount() {
		return this.rand.nextInt(this.rand.nextInt(2) + 1) + 1;
	}

	@Override
	public boolean initFemale() {
		return this.rand.nextInt(3) != 0;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return MistSounds.ENTITY_HORB_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damage) {
		return MistSounds.ENTITY_HORB_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return MistSounds.ENTITY_HORB_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, Block block) {
		this.playSound(SoundEvents.ENTITY_LLAMA_STEP, 0.15F, 1.0F);
	}

	@Override
	protected float getSoundPitch() {
		return this.isChild() ? (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.3F : (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + (this.isFemale() ? 1.0F : 0.8F);
	}

	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand) {
		if (this.revengeTimer > 0 && !player.capabilities.isCreativeMode) return true;
		ItemStack stack = player.getHeldItem(hand);
		if (stack.getItem() == Items.SPAWN_EGG) return super.processInteract(player, hand);
		if (this.isTamed() || player.capabilities.isCreativeMode) {
			return super.processInteract(player, hand);
		}
		return tamedProcess(player, stack);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		boolean flag = super.attackEntityFrom(source, amount);
		Entity entity = source.getTrueSource();
		if (flag && entity != null && entity instanceof EntityPlayer && !((EntityPlayer)entity).capabilities.isCreativeMode) {
			if (this.getHealth() <= 0.0F) this.<EntityHorb>closePanic(EntityHorb.class, (EntityPlayer)entity, this.rand.nextInt(8000) + 8000);
			else this.<EntityHorb>closePanic(EntityHorb.class, (EntityPlayer)entity, this.rand.nextInt(250) + 250);
			this.setRevengeTime(this.rand.nextInt((int)(500 * amount) + 1) + (int)(250 * amount) + 250);
		}
		return flag;
	}

	@Override
	@Nullable
	protected ResourceLocation getLootTable() {
		return LootTables.HORB_LOOT;
	}

	@Override
	protected int getSkillPoint() {
		return 2;
	}

	@Override
	protected void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source) {
		if (this.rand.nextInt(temptationStackDropChance) == 0) {
			this.entityDropItem((ItemStack)TEMPTATION_STACKS.toArray()[this.rand.nextInt(TEMPTATION_STACKS.size())], 0);
		}
		super.dropLoot(wasRecentlyHit, lootingModifier, source);
	}

	@Override
	public float getEyeHeight() {
		return this.isChild() ? this.height : 2;
	}

	@Override
	public boolean childCheck() {
		List<EntityHorb> list = this.world.<EntityHorb>getEntitiesWithinAABB(this.getClass(), this.getEntityBoundingBox().grow(8.0D, 4.0D, 8.0D));
		for (EntityHorb entity : list) {
			if (entity.isChild() && (!entity.canBeTempted() || !entity.isTamed())) return false;
		}
		return true;
	}

	@Override
	protected EntityAlbino getChild() {
		return new EntityHorb(this.world);
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		for (ItemStack st : EntityHorb.TEMPTATION_STACKS) {
			if (stack.getItem() == st.getItem() && stack.getItemDamage() == st.getItemDamage()) return true;
		}
		return false;
	}

	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {
		super.onInitialSpawn(difficulty, livingdata);
		if (livingdata instanceof EntityHorb.GroupData) {
			if (((EntityHorb.GroupData)livingdata).madeParent) {
				this.setGrowingAge(-24000);
			} else {
				this.setFemale(true);
				((EntityHorb.GroupData)livingdata).madeParent = this.rand.nextBoolean();
			}
		} else {
			EntityHorb.GroupData data = new EntityHorb.GroupData();
			data.madeParent = this.isFemale() && this.rand.nextBoolean();
			livingdata = data;
		}
		return livingdata;
	}

	@Override
	public int getTameLevel() {
		return 2;
	}

	static class GroupData implements IEntityLivingData {
		public boolean madeParent;
		private GroupData() {}
	}
}
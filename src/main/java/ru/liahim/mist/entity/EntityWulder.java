package ru.liahim.mist.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
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
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.loottable.LootTables;
import ru.liahim.mist.api.sound.MistSounds;
import ru.liahim.mist.common.MistTime;
import ru.liahim.mist.entity.ai.EntityAIEatMistGrass;
import ru.liahim.mist.entity.ai.EntityAIFollowGender;
import ru.liahim.mist.entity.ai.EntityAIFollowParentGender;
import ru.liahim.mist.entity.ai.EntityAIMateGender;
import ru.liahim.mist.entity.ai.EntityAITemptTamed;

public class EntityWulder extends AbstractMistChestMount implements IShearable {

	private static final DataParameter<Integer> MILK_TIME = EntityDataManager.<Integer>createKey(EntityWulder.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> WOOL_TIME = EntityDataManager.<Integer>createKey(EntityWulder.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> HORN_TYPE = EntityDataManager.<Integer>createKey(EntityWulder.class, DataSerializers.VARINT);
	private static final DataParameter<Boolean> SHEARED = EntityDataManager.<Boolean>createKey(EntityWulder.class, DataSerializers.BOOLEAN);
	private static final Set<ItemStack> TEMPTATION_STACKS = Sets.newHashSet(new ItemStack(MistBlocks.TREE_SAPLING, 1, 7));
	private static long pregnantTime = MistTime.getDayInMonth() * 36000;
	private EntityAIEatMistGrass aiEatGrass;
	private int milkTimer;
	private int woolTimer;
	private int eatTimer;
	private int[] hornArray = new int[] {0, 0, 0, 0, 0, 0};
	protected boolean canGallop = true;

	public EntityWulder(World world) {
		super(world);
		this.setSize(1.825F, 1.9375F);
	}

	@Override
	public double getMountedYOffset() {
		return this.isSheared() ? 1.825D : 1.9375D;
	}

	@Override
	protected void initEntityAI() {
		this.aiTempt = new EntityAITemptTamed(this, 0.6D, 1.0D, true, TEMPTATION_STACKS);
		this.aiEatGrass = new EntityAIEatMistGrass(this, true);
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAIPanic(this, 1.4D));
		this.tasks.addTask(2, new EntityAIMateGender(this, 1.0D));
		this.tasks.addTask(3, this.aiTempt);
		this.tasks.addTask(4, new EntityAIFollowParentGender(this, 1.5D));
		this.tasks.addTask(5, new EntityAIFollowGender(this, 1.0D));
		this.tasks.addTask(6, new EntityAIAvoidEntity(this, EntityPlayer.class, 8, 1.0D, 1.2D));
		this.tasks.addTask(7, this.aiEatGrass);
		this.tasks.addTask(8, new EntityAIWanderAvoidWater(this, 1.0D));
		this.tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
		this.tasks.addTask(10, new EntityAILookIdle(this));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
	}

	@Override
	protected void updateAITasks() {
		if (this.milkTimer > 0) {
			--this.milkTimer;
			if (this.milkTimer == 0) updateMilkTimer();
		}
		if (this.woolTimer > 0) {
			--this.woolTimer;
			if (this.woolTimer == 0) {
				this.setSheared(false);
				updateWoolTimer();
			}
		}
		this.eatTimer = this.aiEatGrass.getEatingGrassTimer();
		super.updateAITasks();
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (this.world.isRemote) {
			if (this.eatTimer > 0) --this.eatTimer;
		}
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
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(HORN_TYPE, -1);
		this.dataManager.register(SHEARED, false);
		this.dataManager.register(MILK_TIME, 0);
		this.dataManager.register(WOOL_TIME, 0);
	}

	public void setSheared(boolean sheared) {
		this.dataManager.set(SHEARED, sheared);
	}

	public boolean isSheared() {
		return this.dataManager.get(SHEARED);
	}

	public void setHornType(int type) {
		this.dataManager.set(HORN_TYPE, type);
	}

	public int getHornType() {
		return this.dataManager.get(HORN_TYPE);
	}

	public void deserializeHornArray() {
		int i = getHornType();
		this.hornArray[0] = i >> 10;
		this.hornArray[1] = i >> 8 & 3;
		this.hornArray[2] = i >> 6 & 3;
		this.hornArray[3] = i >> 4 & 3;
		this.hornArray[4] = i >> 2 & 3;
		this.hornArray[5] = i & 3; 
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		if (MILK_TIME.equals(key) && this.world.isRemote) {
			this.milkTimer = this.dataManager.get(MILK_TIME);
		}
		if (WOOL_TIME.equals(key) && this.world.isRemote) {
			this.woolTimer = this.dataManager.get(WOOL_TIME);
		}
		if (HORN_TYPE.equals(key) && this.world.isRemote) {
			deserializeHornArray();
		}
		super.notifyDataManagerChange(key);
	}

	private void updateMilkTimer() {
		this.dataManager.set(MILK_TIME, this.milkTimer);
	}

	private void updateWoolTimer() {
		this.dataManager.set(WOOL_TIME, this.woolTimer);
	}

	@Override
	public int getTalkInterval() {
		return 400;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setInteger("MilkTime", this.milkTimer);
		compound.setInteger("WoolTime", this.woolTimer);
		compound.setBoolean("Sheared", this.isSheared());
		compound.setInteger("HornType", this.getHornType());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		this.milkTimer = compound.getInteger("MilkTime");
		this.woolTimer = compound.getInteger("WoolTime");
		this.setSheared(compound.getBoolean("Sheared"));
		this.setHornType(compound.getInteger("HornType"));
		updateMilkTimer();
		updateWoolTimer();
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return MistSounds.ENTITY_WULDER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damage) {
		return MistSounds.ENTITY_WULDER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return MistSounds.ENTITY_WULDER_DEATH;
	}

	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand) {
		if (this.revengeTimer > 0 && !player.capabilities.isCreativeMode) return true;
		ItemStack stack = player.getHeldItem(hand);
		if (stack.getItem() == Items.SPAWN_EGG) return super.processInteract(player, hand);
		else if (this.isTamed() || player.capabilities.isCreativeMode) {
			if (player.isSneaking()) {
				if (!this.isChild()) {
					this.openGUI(player);
					return true;
				} else return false;
			} else {
				if (this.isBreedingItem(stack)) {
					if (this.getHealth() < this.getMaxHealth()) {
						this.consumeItemFromStack(player, stack);
						this.heal(5);
					} else return super.processInteract(player, hand);
				} else if (stack.getItem() == Items.BUCKET && this.milkTimer == 0 && this.isFemale()
						&& !player.capabilities.isCreativeMode && !this.isChild()) {
					player.playSound(SoundEvents.ENTITY_COW_MILK, 1.0F, 1.0F);
					stack.shrink(1);
					if (stack.isEmpty()) player.setHeldItem(hand, new ItemStack(Items.MILK_BUCKET));
					else if (!player.inventory.addItemStackToInventory(new ItemStack(Items.MILK_BUCKET))) {
						player.dropItem(new ItemStack(Items.MILK_BUCKET), false);
					}
					this.milkTimer = this.isAlbino() ? 24000 : 30000;
					updateMilkTimer();
					return true;
				} else if (stack.getItem() == Items.NAME_TAG) {
					stack.interactWithEntity(player, this, hand);
					return true;
				} else if (!this.hasChest() && Block.getBlockFromItem(stack.getItem()) instanceof BlockChest) {
					this.setChested(true);
					this.playChestEquipSound();
					this.initHorseChest();
					this.horseChest.setInventorySlotContents(1, stack.splitStack(1));
					if (stack.isEmpty()) player.setHeldItem(hand, ItemStack.EMPTY);
					return true;
				} else if (this.isSaddled() && !this.isBeingRidden() && isRiddingItem(player.getHeldItemMainhand()) && isRiddingItem(player.getHeldItemOffhand())) {
					this.startRiding(player);
					return true;
				} else if (stack.getItem() == Items.SADDLE && this.isTamed() && !this.isSaddled() && !this.isChild()) {
					this.horseChest.setInventorySlotContents(0, stack.splitStack(1));
					if (stack.isEmpty()) player.setHeldItem(hand, ItemStack.EMPTY);
					this.updateHorseSlots();
					return true;
				} else return false;
			}
		}
		return tamedProcess(player, stack);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		boolean flag = super.attackEntityFrom(source, amount);
		Entity entity = source.getTrueSource();
		if (flag && entity != null && entity instanceof EntityPlayer && !((EntityPlayer)entity).capabilities.isCreativeMode) {
			if (this.getHealth() <= 0.0F) this.<EntityWulder>closePanic(EntityWulder.class, (EntityPlayer)entity, this.rand.nextInt(8000) + 8000);
			else this.<EntityWulder>closePanic(EntityWulder.class, (EntityPlayer)entity, this.rand.nextInt(250) + 250);
			this.setRevengeTime(this.rand.nextInt((int)(500 * amount) + 1) + (int)(250 * amount) + 250);
		}
		return flag;
	}

	@Override
	@Nullable
	protected ResourceLocation getLootTable() {
		return LootTables.WULDER_LOOT;
	}

	@Override
	protected int getSkillPoint() {
		return 2;
	}

	@Override
	public void onDeath(DamageSource cause) {
		super.onDeath(cause);
		if (!this.world.isRemote && !this.isSheared()) this.dropWool(cause, this.isAlbino() ? 0 : 12);
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
		return this.isChild() ? this.height : 1.5F;
	}

	@Override
	public boolean childCheck() {
		List<EntityWulder> list = this.world.<EntityWulder>getEntitiesWithinAABB(this.getClass(), this.getEntityBoundingBox().grow(8.0D, 4.0D, 8.0D));
		for (EntityWulder entity : list) {
			if (entity.isChild() && (!entity.canBeTempted() || !entity.isTamed())) return false;
		}
		return true;
	}

	@Override
	protected EntityAlbino getChild() {
		return new EntityWulder(this.world);
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		for (ItemStack st : EntityWulder.TEMPTATION_STACKS) {
			if (stack.getItem() == st.getItem() && stack.getItemDamage() == st.getItemDamage()) return true;
		}
		return false;
	}

	@Override
	@Nullable
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
		if (this.getHornType() < 0) this.setHornType(inicializeHorn());
		super.onInitialSpawn(difficulty, livingdata);
		if (livingdata instanceof EntityWulder.GroupData) {
			if (((EntityWulder.GroupData)livingdata).init) {
				this.setFemale(false);
				((EntityWulder.GroupData)livingdata).init = false;
			} else this.setFemale(true);
		} else livingdata = new EntityWulder.GroupData();;
		return livingdata;
	}

	private int inicializeHorn() {
		if (this.isFemale()) return -1;
		int hL = this.rand.nextInt(5) / 2;
		int hR = hL;
		if (this.rand.nextInt(8) == 0) hR = this.rand.nextInt(5) / 2;
		int temp = hL;
		if (hR < 2 && this.rand.nextInt(32) == 0) hL = 3;
		if (temp < 2 && this.rand.nextInt(32) == 0) hR = 3;
		int zL = this.rand.nextInt(this.rand.nextInt(3) + 1);
		if (zL == 1) zL = 2;
		else if (zL == 2) zL = 1;
		int zR = zL;
		if (this.rand.nextInt(4) == 0) {
			zR = this.rand.nextInt(this.rand.nextInt(3) + 1);
			if (zR == 1) zR = 2;
			else if (zR == 2) zR = 1;
		}
		int yL = this.rand.nextInt(this.rand.nextInt(3) + 1);
		if (yL == 1) yL = 0;
		else if (yL == 0) yL = 1;
		int yR = yL;
		if (this.rand.nextInt(4) == 0) {
			yR = this.rand.nextInt(this.rand.nextInt(3) + 1);
			if (yR == 1) yR = 0;
			else if (yR == 0) yR = 1;
		}
		return (hL << 10) | (zL << 8) | (yL << 6) | (hR << 4) | (zR << 2) | yR;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleStatusUpdate(byte id) {
		if (id == 10) this.eatTimer = 40;
		else super.handleStatusUpdate(id);
	}

	@SideOnly(Side.CLIENT)
	public float getYEatFactor(float partialTickTime) {
		if (this.eatTimer <= 0) return 1.0F;
		else if (this.eatTimer >= 4 && this.eatTimer <= 36) return 0.0F;
		else return this.eatTimer < 4 ? (4 - this.eatTimer + partialTickTime) / 4.0F : (this.eatTimer - partialTickTime - 36) / 4.0F;
	}

	@SideOnly(Side.CLIENT)
	public float getXEatFactor(float partialTickTime) {
		if (this.eatTimer > 4 && this.eatTimer <= 36) {
			float f = (this.eatTimer - 4 - partialTickTime) / 4.0F;
			return MathHelper.sin((float) Math.PI * f);
		} else return 0;
	}

	@Override
	public void eatGrassBonus() {
		if (this.isChild()) this.addGrowth(60);
		this.milkTimer = Math.max(1, this.milkTimer - 1000);
		this.woolTimer = Math.max(1, this.woolTimer - 1000);
		this.heal(5);
	}

	@Override
	public boolean isShearable(ItemStack item, IBlockAccess world, BlockPos pos) {
		return this.isTamed() && this.woolTimer == 0 && !this.isSheared() && !this.isChild();
	}

	@Override
	public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
		this.setSheared(true);
		this.woolTimer = this.isAlbino() ? 30000 : 36000;
		this.updateWoolTimer();
		int i = 1 + this.rand.nextInt(fortune + 2);
		int meta = this.isAlbino() ? 0 : 12;
		List<ItemStack> ret = new ArrayList<ItemStack>();
		for (int j = 0; j < i; ++j) ret.add(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, meta));
		this.playSound(SoundEvents.ENTITY_SHEEP_SHEAR, 1.0F, 1.0F);
		return ret;
	}

	@Override
	public int getTameLevel() {
		return 3;
	}

	static class GroupData implements IEntityLivingData {
		public boolean init = true;
		private GroupData() {}
	}

	/** 0 - LL, 1 - LS, 2 - SS, 3 - S */
	@SideOnly(Side.CLIENT)
	public int[] getHornArray() {
		return this.hornArray;
	}
}
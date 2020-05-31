package ru.liahim.mist.tileentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import ru.liahim.mist.api.item.IMistFood;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.block.gizmos.MistCampfire;
import ru.liahim.mist.block.gizmos.MistCampfire.CookingTool;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.init.ItemColoring;
import ru.liahim.mist.item.food.ItemMistSoup;
import ru.liahim.mist.network.PacketFirePitUpdate;
import ru.liahim.mist.network.PacketHandler;
import ru.liahim.mist.util.ItemStackMapKey;
import ru.liahim.mist.util.MapUtil;
import ru.liahim.mist.world.FogDamage;
import ru.liahim.mist.world.MistWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityCampfire extends TileEntity implements ITickable, ISidedInventory, IFluidTank {

	/** Block parameters */
	private CookingTool cookingTool = CookingTool.NONE;
	private EnumFacing facing = EnumFacing.NORTH;
	public int toxicY = -2;

	/** Animation */
	@SideOnly(Side.CLIENT)
	private final static int RADIUS = 40;
	@SideOnly(Side.CLIENT)
	private final static int ANIMATION_TICKS = 320;
	@SideOnly(Side.CLIENT)
	private static int animationTicks;

	/** Stones */
	private static final HashMap<ItemStack,Vector2f> stoneAndColor = new HashMap<ItemStack,Vector2f>();
	private ItemStack stone = ItemStack.EMPTY;
	private int stoneColor = 0;

	/** Input parameters */
	private byte volume;
	private float milk = 0;

	/** Output food parameters */
	private float finalHealAmaunt = 0;
	private float finalSaturation = 0;
	private float toxic = 0;
	private int toxicTimer;
	private int[] foodColors = new int[4];
	private float[] foodPercent = new float[4];
	private ItemStack[] mainFood = new ItemStack[] {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
	private LinkedHashMap<Integer,Float> foodMap = new LinkedHashMap<Integer,Float>();
	private LinkedHashMap<ItemStackMapKey,Float> mainFoodMap = new LinkedHashMap<ItemStackMapKey,Float>();
	private LinkedHashMap<Integer,Float> colorHelper = new LinkedHashMap<Integer,Float>();
	/** Potions */
	private HashMap<Potion, Vector3f> potions = new HashMap<Potion, Vector3f>();
	private HashMap<Potion, Vector3f> rawPotionsHelper = new HashMap<Potion, Vector3f>();
	private HashMap<Potion, Vector3f> cookPotionsHelper = new HashMap<Potion, Vector3f>();

	/** Slots and stacks */
	private static final int[] SLOTS_FUEL = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
	private static final int[] SLOTS_FOOD = new int[] { 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79 };
	private static final int[] SLOTS_GRILL = new int[] { 16, 17, 18, 19 };
	private static final int SLOTS_FUEL_SIZE = 16;
	private static final int SLOTS_FOOD_SIZE = 64;
	private static final int SLOTS_GRILL_SIZE = 4;
	private NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(SLOTS_FUEL_SIZE + SLOTS_FOOD_SIZE, ItemStack.EMPTY);

	/** Process */
	private int totalBurningTime = 0;
	private int currentBurningTime = 0;
	private int temperature = 0;
	private float totalProgress = 0;
	private float clientProgress = 0;
	private final float[][] progress = new float[SLOTS_FOOD_SIZE][2];
	private int ashTimer = 0;

	@Override
	public void update() {
		if (!this.world.isRemote) {
			if (this.toxicY == -2) this.toxicY = world.provider.getDimension() == Mist.getID() ? pos.getY() : -1;
			if (this.temperature > 0) {
				if (this.currentBurningTime == 0) {
					if (this.totalBurningTime > 0) {
						ItemStack fuel;
						for (int i = 0; i < SLOTS_FUEL_SIZE; ++i) {
							fuel = this.getStackInSlot(i);
							int time = TileEntityFurnace.getItemBurnTime(fuel);
							if (time > 0) {
								fuel.shrink(1);
								this.currentBurningTime += time;
								this.totalBurningTime -= time;
								this.updateStatus();
								break;
							}
						}
					} else {
						--this.temperature;
						if (this.temperature == 0) {
							this.extinguish(false);
						}
					}
				}
				if (this.currentBurningTime > 0) {
					--this.currentBurningTime;
					if (this.ashTimer < 16000) ++this.ashTimer;
					if (this.currentBurningTime >= 20 && this.temperature < 1000) ++this.temperature;
					if (this.currentBurningTime == 0 && this.totalBurningTime <= 0) {
						this.extinguish(true);
					}
				}
				if (this.temperature > 20) this.updateFoodProgress(true);
			}
			if (this.toxicY >= 0 && ++this.toxicTimer > 100 && this.getCookingTool() == CookingTool.POT && this.volume > 0) {
				this.toxicTimer = 0;
				if (MistWorld.isPosInFog(world, this.toxicY)) {
					boolean rain = world.isRaining() && world.canSeeSky(pos);
					float conc = FogDamage.getConcentration(world, pos);
					this.toxic += rain ? (5 + 100 * conc) : (1 + 20 * conc);
					if (this.toxic > 10000) this.toxic = 10000;
				}
			}
		}
	}

	private void updateFoodProgress(boolean plus) {
		if (this.getCookingTool() == CookingTool.POT) {
			if (this.volume > 0) {
				float totalProgress = 0;
				float currentProgress = 0;
				for (int i = 0; i < SLOTS_FOOD_SIZE; ++i) {
					if (!this.getStackInSlot(i + SLOTS_FUEL_SIZE).isEmpty()) {
						if (plus && this.progress[i][0] < this.getPotCookTime()) ++this.progress[i][0];
						++totalProgress;
						currentProgress += this.progress[i][0];
					}
				}
				if (totalProgress > 0) totalProgress = currentProgress / (totalProgress * this.getPotCookTime());
				this.totalProgress = totalProgress;
				if (!plus) this.clientProgress = this.totalProgress;
				if (this.totalProgress > 0.0F && this.totalProgress < 1.0F) {
					if (this.totalProgress - this.clientProgress > 0.1F) {
						this.clientProgress = this.totalProgress;
						this.updateStatus();
					}
				} else if (this.totalProgress == 1.0F && this.clientProgress < 1.0F) {
					this.clientProgress = this.totalProgress;
					this.updateStatus();
				}
			}
		} else if (this.getCookingTool() == CookingTool.GRILL) {
			for (int i = 0; i < SLOTS_GRILL_SIZE; ++i) {
				if (this.progress[i][0] >= 0 && !this.getStackInSlot(i + SLOTS_FUEL_SIZE).isEmpty()) {
					if (this.progress[i][0] < getGrillCookTime()) ++this.progress[i][0];
					else {
						this.setInventorySlotContents(i + SLOTS_FUEL_SIZE, FurnaceRecipes.instance().getSmeltingResult(this.getStackInSlot(i + SLOTS_FUEL_SIZE)).copy());
						this.progress[i][0] = -1;
						this.updateStatus();
					}
				}
			}
		}
	}

	private int getPotCookTime() {
		return this.volume * 400;
	}

	public static final int getGrillCookTime() {
		return 800;
	}

	private void extinguish(boolean wasFuel) {
		((MistCampfire)this.world.getBlockState(this.pos).getBlock()).extinguish(this.world, this.pos, wasFuel);
	}

	private void updateMainFood() {
		ItemStack stack;
		ItemStack cooked;
		this.mainFoodMap.clear();
		for (int i = SLOTS_FUEL_SIZE; i < this.getSizeInventory(); ++i) {
			stack = this.getStackInSlot(i).copy();
			if (!stack.isEmpty()) {
				cooked = FurnaceRecipes.instance().getSmeltingResult(stack);
				float amount;
				if (!cooked.isEmpty() && cooked.getItem() instanceof ItemFood) {
					amount = ((ItemFood)cooked.getItem()).getHealAmount(cooked);
				} else {
					amount = ((ItemFood)stack.getItem()).getHealAmount(stack) * getHealBonus();
				}
				amount = amount * this.progress[i - SLOTS_FUEL_SIZE][1] / foodSize();
				boolean check = false;
				ItemStackMapKey key = new ItemStackMapKey(stack);
				if (this.mainFoodMap.containsKey(key)) {
					this.mainFoodMap.replace(key, this.mainFoodMap.get(key) + amount);
					check = true;
				}
				if (!check) this.mainFoodMap.put(key, amount);
			}
		}
		if (!this.mainFoodMap.isEmpty()) {
			this.mainFoodMap = (LinkedHashMap<ItemStackMapKey,Float>) MapUtil.sortByValue(this.mainFoodMap, true);
			int i = 0;
			for (ItemStackMapKey finalStack : this.mainFoodMap.keySet()) {
				if (i < 4) {
					if (this.mainFoodMap.get(finalStack) >= this.getVolum() * 0.25F) {
						this.mainFood[i] = finalStack.itemStack;
						this.foodPercent[i] = this.mainFoodMap.get(finalStack);
					} else {
						this.mainFood[i] = ItemStack.EMPTY;
						this.foodPercent[i] = 0.0F;
					}
					++i;
				} else break;
			}
		} else {
			this.mainFood = new ItemStack[] {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
			this.foodPercent = new float[4];
		}
	}

	public float getFinalAmount() {
		return this.finalHealAmaunt;
	}

	//////////////////////////////////// ANIMATION  ////////////////////////////////////

	@SideOnly(Side.CLIENT)
	public static void updateFrame() {
		animationTicks = (animationTicks + 1) % ANIMATION_TICKS;
	}

	@SideOnly(Side.CLIENT)
	private float getInterpolatedFrame(float tick) {
		if (tick > 1.0F) tick = 1.0F;
		return animationTicks + tick;
	}

	@SideOnly(Side.CLIENT)
	public double getAnimationPhase(int center, float tick) {
		double i = getInterpolatedFrame(tick);
		if (i < center - RADIUS) i += ANIMATION_TICKS;
		else if (i >= center + RADIUS) i -= ANIMATION_TICKS;
		i = RADIUS - Math.abs(center - i);
		if (i < 0) return 0;
		return (1 - Math.cos(i / RADIUS * Math.PI)) / 2;
	}

	@SideOnly(Side.CLIENT)
	public float getCookProgress() {
		return this.totalProgress;
	}

	@SideOnly(Side.CLIENT)
	public int[] getFoodColors() {
		return this.foodColors;
	}

	@SideOnly(Side.CLIENT)
	public ItemStack[] getMainFood() {
		return this.mainFood;
	}

	@SideOnly(Side.CLIENT)
	public float[] getFoodPercent() {
		return this.foodPercent;
	}

	public void updateFoodColors() {
		int i = 0;
		int count = 0;
		float total = 0;
		int amount;
		ItemStack result;
		foodColors = new int[4];
		for (ItemStack stack : this.mainFood) {
			if (!stack.isEmpty()) {
				result = FurnaceRecipes.instance().getSmeltingResult(stack);
				if (!result.isEmpty() && result.getItem() instanceof ItemFood) {
					amount = ((ItemFood)result.getItem()).getHealAmount(result);
					int red, green, blue;
					int start = ItemColoring.getFoodColor(stack);
					int end = ItemColoring.getFoodColor(result);
					red = (int) (((start >> 16) & 255) * (1.0F - this.getCookProgress()) + ((end >> 16) & 255) * this.getCookProgress());
					green = (int) (((start >> 8) & 255) * (1.0F - this.getCookProgress()) + ((end >> 8) & 255) * this.getCookProgress());
					blue = (int) ((start & 255) * (1.0F - this.getCookProgress()) + (end & 255) * this.getCookProgress());
					this.foodColors[i] = red << 16 | green << 8 | blue;
				} else {
					amount = ((ItemFood)stack.getItem()).getHealAmount(stack);
					this.foodColors[i] = ItemColoring.getFoodColor(stack);
				}
				total += this.foodPercent[i] / amount;
				++count;
			} else {
				this.foodColors[i] = 0;
			}
			++i;
		}
		if (count > 0 && count < 4) {
			total = (float) Math.min(4, Math.ceil(total / 4));
			i = (int) (total - count);
			if (i > 0) {
				if (i == 3 || count == 1) {
					for (int j = 1; j < total; ++j) {
						this.foodColors[j] = this.foodColors[0];
					}
				} else {
					for (int j = 0; j < count; ++j) {
						this.colorHelper.put(this.foodColors[j], this.foodPercent[j]);
					}
					this.colorHelper = (LinkedHashMap<Integer,Float>)MapUtil.sortByValue(this.colorHelper, true);
					if (i == 1) {
						this.foodColors[0] = (int)this.colorHelper.keySet().toArray()[0];
						for (int j = 0; j < count; ++j) {
							this.foodColors[j + 1] = (int)this.colorHelper.keySet().toArray()[j];
						}
					} else if (i == 2) {
						if ((float)(this.colorHelper.values().toArray()[0]) >= (float)(this.colorHelper.values().toArray()[1]) * 2) {
							for (int j = 0; j < 3; ++j) {
								this.foodColors[j] = (int)this.colorHelper.keySet().toArray()[0];
							}
							this.foodColors[3] = (int)this.colorHelper.keySet().toArray()[1];
						} else {
							for (int j = 0; j < 2; ++j) {
								this.foodColors[j] = (int)this.colorHelper.keySet().toArray()[0];
							}
							for (int j = 2; j < 4; ++j) {
								this.foodColors[j] = (int)this.colorHelper.keySet().toArray()[1];
							}
						}
					}
					this.colorHelper.clear();
				}
			}
		}
	}

	////////////////////////////////////  NBT  ////////////////////////////////////

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.stacks = NonNullList.<ItemStack>withSize(SLOTS_FUEL_SIZE + SLOTS_FOOD_SIZE, ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, this.stacks);
        this.totalBurningTime = 0;
		for (int i = 0; i < SLOTS_FUEL_SIZE; ++i) {
			int time = TileEntityFurnace.getItemBurnTime(this.getStackInSlot(i));
			if (time > 0) this.totalBurningTime += time;
		}
		this.currentBurningTime = compound.getInteger("BurnTime");
		this.setStone(new ItemStack(compound.getCompoundTag("Stone")));
		this.setFacing(EnumFacing.getHorizontal(compound.getByte("Facing")));
		this.setCookingTool(CookingTool.fromIndex(compound.getByte("Tool")));
		this.temperature = compound.getInteger("Temperature");
		this.ashTimer = compound.getInteger("AshTimer");
		NBTTagList list = compound.getTagList("Progress", 10);
		for (int i = 0; i < this.progress.length; ++i) {
			NBTTagCompound tag = list.getCompoundTagAt(i);
			if (tag.hasKey("Par_0") && tag.hasKey("Par_1")) {
				this.progress[i][0] = tag.getFloat("Par_0");
				this.progress[i][1] = tag.getFloat("Par_1");
			}
			else this.progress[i] = new float[2];
		}
		if (this.getCookingTool() == CookingTool.POT) {
			this.totalProgress = compound.getFloat("TotalProgress");
			this.clientProgress = this.totalProgress;
			this.volume = compound.getByte("Volum");
			this.milk = compound.getFloat("Milk");
			this.finalHealAmaunt = compound.getFloat("Heal");
			this.finalSaturation = compound.getFloat("Saturation");
			this.toxic = compound.getFloat("Toxic");
			this.updateMainFood();
			if (this.hasWorld() && world.isRemote) this.updateFoodColors();
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setTag("Stone", this.getStone().serializeNBT());
		compound.setByte("Facing", (byte)this.getFacing().getHorizontalIndex());
		compound.setByte("Tool", (byte)this.getCookingTool().getIndex());
		compound.setInteger("BurnTime", (short)this.currentBurningTime);
		compound.setInteger("Temperature", (short)this.temperature);
		compound.setInteger("AshTimer", this.ashTimer);
		NBTTagList list = new NBTTagList();
		for (int i = 0; i < this.progress.length; ++i) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setFloat("Par_0", this.progress[i][0]);
			tag.setFloat("Par_1", this.progress[i][1]);
			list.appendTag(tag);
		}
		compound.setTag("Progress", list);
		ItemStackHelper.saveAllItems(compound, this.stacks);
		if (this.getCookingTool() == CookingTool.POT) {
			compound.setFloat("TotalProgress", this.totalProgress);
			compound.setByte("Volum", this.volume);
			compound.setFloat("Milk", this.milk);
			compound.setFloat("Heal", this.finalHealAmaunt);
			compound.setFloat("Saturation", this.finalSaturation);
			compound.setFloat("Toxic", this.toxic);
		}
		return compound;
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, this.getBlockMetadata(), this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		handleUpdateTag(pkt.getNbtCompound());
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	public void updateStatus() {
		this.updateStatus(this.world.getBlockState(pos));
	}

	public void updateStatus(IBlockState state) {
		this.markDirty();
		this.world.notifyBlockUpdate(pos, this.world.getBlockState(pos), state, 3);
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
		return oldState.getBlock() != newSate.getBlock();
	}

	////////////////////////////////////  SET  ////////////////////////////////////

	public void setFire() {
		if (this.temperature < 20) this.temperature = 20;
		else if (this.currentBurningTime < 20) this.currentBurningTime = 20;
	}

	public void addMilk(int i) {
		this.milk += i;
	}

	public int getVolum() {
		return this.volume;
	}

	public void setVolum(int volume, boolean get) {
		if (this.getCookingTool() == CookingTool.POT && this.volume > 0) {
			if (volume == 0) {
				if (!get) {
					NonNullList<ItemStack> list = NonNullList.create();
					list = this.getFoodDrops(list);
					for (ItemStack stack : list) {
						if (!stack.isEmpty()) InventoryHelper.spawnItemStack(this.world, this.pos.getX(), this.pos.getY() + 1.0D, this.pos.getZ(), stack);
					}
				}
				this.resetFood();
			} else if (volume > this.volume) {
				for (int i = 0; i < this.progress.length; ++i) {
					this.progress[i][0] = (int) this.progress[i][0] * volume / this.volume;
				}
			} else {
				this.finalHealAmaunt = this.finalHealAmaunt * volume / this.volume;
				if (this.finalHealAmaunt < 1) this.resetFood();
				else {
					for (int i = 0; i < this.progress.length; ++i) {
						this.progress[i][1] = this.progress[i][1] * volume / this.volume;
						if (this.progress[i][1] < 1) {
							this.setInventorySlotContents(i + SLOTS_FUEL_SIZE, ItemStack.EMPTY);
						} else this.progress[i][0] = this.progress[i][0] * volume / this.volume;
					}
					this.finalSaturation = this.finalSaturation * volume / this.volume;
					this.toxic = this.toxic * volume / this.volume;
					this.milk = this.milk * volume / this.volume;
					if (this.milk < 0.25) this.milk = 0;
				}
			}
		}
		this.volume = (byte) volume;
		this.updateStatus();
	}

	public void fillWithRain() {
		if (this.getCookingTool() == CookingTool.POT && this.getVolum() < 4) {
			this.setVolum(this.getVolum() + 1, false);
		}
	}

	private void resetFood() {
		for (int i = SLOTS_FUEL_SIZE; i < this.getSizeInventory(); ++i) this.setInventorySlotContents(i, ItemStack.EMPTY);
		for (int i = 0; i < this.progress.length; ++i) this.progress[i] = new float[2];
		this.mainFood = new ItemStack[] {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
		this.foodColors = new int[4];
		this.foodPercent = new float[4];
		this.totalProgress = 0.0F;
		this.clientProgress = 0.0F;
		this.milk = 0;
		this.finalHealAmaunt = 0;
		this.finalSaturation = 0;
		this.toxic = 0;
	}

	public ItemStack getSoup(ItemStack stack, int portion) {
		ItemStack soup = ItemMistSoup.getSoupStack(stack);
		if (!soup.isEmpty()) {
			if (this.getCookingTool() == CookingTool.POT && this.getVolum() > 0 && this.finalHealAmaunt > 0) {
				float foodAmount;
				float foodSaturation;
				float resultAmount;
				float resultSaturation;
				float finalAmount = 0;
				float finalSaturation = 0;
				float toxic = 0;
				float[] progress;
				ItemStack food;
				ItemStack result;
				PotionEffect[] pf_1;
				PotionEffect[] potionEffects;
				Vector3f raw3f;
				Vector3f cook3f;
				for (int i = SLOTS_FUEL_SIZE; i < this.getSizeInventory(); ++i) {
					food = this.getStackInSlot(i);
					if (!food.isEmpty()) {
						progress = this.progress[i - SLOTS_FUEL_SIZE];
						foodAmount = ((ItemFood)food.getItem()).getHealAmount(food);
						foodSaturation = ((ItemFood)food.getItem()).getSaturationModifier(food);
						result = FurnaceRecipes.instance().getSmeltingResult(food);
						boolean resFood = !result.isEmpty() && result.getItem() instanceof ItemFood;
						if (resFood) {
							resultAmount = ((ItemFood)result.getItem()).getHealAmount(result);
							resultSaturation = ((ItemFood)result.getItem()).getSaturationModifier(result);
						} else {
							resultAmount = foodAmount * getHealBonus();
							resultSaturation = foodSaturation * getHealBonus();
						}
						foodAmount = foodAmount + (resultAmount - foodAmount) * progress[0] / this.getPotCookTime();
						foodSaturation = foodSaturation + (resultSaturation - foodSaturation) * progress[0] / this.getPotCookTime();
						foodAmount = foodAmount * progress[1] / foodSize();
						foodSaturation = foodSaturation * progress[1] / foodSize();
						finalAmount += foodAmount;
						finalSaturation += foodSaturation;
						/** Potions */
						boolean pufferfish = ItemFishFood.FishType.byItemStack(food) == ItemFishFood.FishType.PUFFERFISH;
						if (food.getItem() instanceof IMistFood) {
							potionEffects = ((IMistFood)food.getItem()).getPotions(food);
							if (potionEffects != null) {
								float prob = ((IMistFood)food.getItem()).getProbability(food);
								for (PotionEffect pf_2 : potionEffects) {
									this.rawPotionsHelper.put(pf_2.getPotion(), new Vector3f(pf_2.getDuration() * prob, pf_2.getAmplifier(), 0));
								}
							}
						} else {
							if (pufferfish) {
								pf_1 = new PotionEffect[] { new PotionEffect(MobEffects.POISON, 1200, 3), new PotionEffect(MobEffects.HUNGER, 300, 2), new PotionEffect(MobEffects.NAUSEA, 300, 1) };
							} else pf_1 = new PotionEffect[] { ((ItemFood)food.getItem()).potionId };
							float prob = pufferfish ? 1 : ((ItemFood)food.getItem()).potionEffectProbability;
							for (PotionEffect pf_3 : pf_1) {
								if (pf_3 != null) this.rawPotionsHelper.put(pf_3.getPotion(), new Vector3f(pf_3.getDuration() * prob, pf_3.getAmplifier(), 0));
							}
						}
						if (resFood) {
							if (result.getItem() instanceof IMistFood) {
								potionEffects = ((IMistFood)result.getItem()).getPotions(result);
								if (potionEffects != null) {
									float prob = ((IMistFood)result.getItem()).getProbability(result);
									for (PotionEffect pf_2 : potionEffects) {
										this.cookPotionsHelper.put(pf_2.getPotion(), new Vector3f(pf_2.getDuration() * prob, pf_2.getAmplifier(), 0));
									}
								}
							} else {
								if (pufferfish) {
									pf_1 = new PotionEffect[] { new PotionEffect(MobEffects.POISON, 600, 2), new PotionEffect(MobEffects.HUNGER, 300, 1), new PotionEffect(MobEffects.NAUSEA, 300, 1) };
								} else pf_1 = new PotionEffect[] { ((ItemFood)result.getItem()).potionId };
								float prob = pufferfish ? 1 : ((ItemFood)result.getItem()).potionEffectProbability;
								for (PotionEffect pf_3 : pf_1) {
									if (pf_3 != null) this.cookPotionsHelper.put(pf_3.getPotion(), new Vector3f(pf_3.getDuration() * prob, pf_3.getAmplifier(), 0));
								}
							}
							if (!this.rawPotionsHelper.isEmpty()) {
								for (Potion po : this.rawPotionsHelper.keySet()) {
									raw3f = this.rawPotionsHelper.get(po);
									if (this.cookPotionsHelper.containsKey(po)) {
										cook3f = this.cookPotionsHelper.get(po);
										raw3f.x = raw3f.x + (cook3f.x - raw3f.x) * progress[0] / this.getPotCookTime();
										raw3f.y = raw3f.y + (cook3f.y - raw3f.y) * progress[0] / this.getPotCookTime();
									} else {
										raw3f.x = raw3f.x - raw3f.x * progress[0] / this.getPotCookTime();
										raw3f.y = raw3f.y - raw3f.y * progress[0] / this.getPotCookTime();
									}
									this.rawPotionsHelper.replace(po, raw3f);
								}
							}
							if (!this.cookPotionsHelper.isEmpty()) {
								for (Potion po : this.cookPotionsHelper.keySet()) {
									cook3f = this.cookPotionsHelper.get(po);
									if (!this.rawPotionsHelper.containsKey(po)) {
										cook3f.x = cook3f.x * progress[0] / this.getPotCookTime();
										cook3f.y = cook3f.y * progress[0] / this.getPotCookTime();
									}
									this.rawPotionsHelper.put(po, cook3f);
								}
							}
						}
						if (!this.rawPotionsHelper.isEmpty()) {
							for (Potion po : this.rawPotionsHelper.keySet()) {
								raw3f = this.rawPotionsHelper.get(po);
								if (this.potions.containsKey(po)) {
									cook3f = this.potions.get(po);
									cook3f.x += raw3f.x * progress[1] / foodSize();
									cook3f.y += raw3f.y * progress[1] / foodSize();
									cook3f.z += progress[1] / foodSize();
									this.potions.replace(po, cook3f);
								} else {
									raw3f.x = raw3f.x * progress[1] / foodSize();
									raw3f.y = raw3f.y * progress[1] / foodSize();
									raw3f.z = progress[1] / foodSize();
									this.potions.put(po, raw3f);
								}
							}
						}
						this.rawPotionsHelper.clear();
						this.cookPotionsHelper.clear();
					}
				}
				if (portion > this.getVolum()) portion = this.getVolum();
				finalAmount = Math.min(finalAmount / this.getVolum(), 20.0F);
				finalSaturation = Math.min(finalSaturation / this.getVolum(), 1.0F);
				toxic = Math.min(this.toxic / this.getVolum(), 5000.0F);
				if (finalAmount >= 1) {
					this.updateMainFood();
					if (!soup.hasTagCompound()) soup.setTagCompound(new NBTTagCompound());
					int soupPortion = soup.getTagCompound().getInteger("SoupPortion");
					soup.getTagCompound().setInteger("SoupPortion", soupPortion + portion);
					if (soupPortion > 0) {
						finalAmount = (finalAmount * portion + soup.getTagCompound().getFloat("SoupHealAmount") * soupPortion) / (soupPortion + portion);
						finalSaturation = (finalSaturation * portion + soup.getTagCompound().getFloat("SoupSaturation") * soupPortion) / (soupPortion + portion);
						toxic = (toxic * portion + soup.getTagCompound().getFloat("SoupToxic") * soupPortion) / (soupPortion + portion);
					}
					soup.getTagCompound().setFloat("SoupHealAmount", finalAmount);
					soup.getTagCompound().setFloat("SoupSaturation", finalSaturation);
					soup.getTagCompound().setFloat("SoupToxic", toxic);
					// Potions
					NBTTagList potionList = soup.getTagCompound().getTagList("Potions", 10);
					if (potionList.tagCount() > 0) {
						for (int i = 0; i < potionList.tagCount(); ++i) {
							PotionEffect pe = PotionEffect.readCustomPotionEffectFromNBT(potionList.getCompoundTagAt(i));
							this.rawPotionsHelper.put(pe.getPotion(), new Vector3f(pe.getDuration(), pe.getAmplifier(), 0));
						}
					}
					for (Potion po : this.potions.keySet()) {
						cook3f = this.potions.get(po);
						cook3f.x = cook3f.x / this.getVolum();
						cook3f.y /= cook3f.z;
						this.cookPotionsHelper.put(po, cook3f);
					}
					for (Potion po : this.rawPotionsHelper.keySet()) {
						raw3f = this.rawPotionsHelper.get(po);
						if (this.cookPotionsHelper.containsKey(po)) {
							cook3f = this.cookPotionsHelper.get(po);
							cook3f.x = (raw3f.x * soupPortion + cook3f.x * portion) / (soupPortion + portion);
							cook3f.y = (raw3f.y * soupPortion + cook3f.y * portion) / (soupPortion + portion);
							this.cookPotionsHelper.put(po, cook3f);
						} else {
							raw3f.x = raw3f.x * soupPortion / (soupPortion + portion);
							raw3f.y = raw3f.y * soupPortion / (soupPortion + portion);
							this.cookPotionsHelper.put(po, raw3f);
						}
					}
					potionList = new NBTTagList();
					for (Potion po : this.cookPotionsHelper.keySet()) {
						cook3f = this.cookPotionsHelper.get(po);
						if (cook3f.x >= 10) {
							potionList.appendTag(new PotionEffect(po, (int)cook3f.x, (int)cook3f.y).writeCustomPotionEffectToNBT(new NBTTagCompound()));
						}
					}
					this.rawPotionsHelper.clear();
					this.cookPotionsHelper.clear();
					this.potions.clear();
					soup.getTagCompound().setTag("Potions", potionList);
					// End Potions
					NBTTagList tagList = new NBTTagList();
					NBTTagList colorList = new NBTTagList();
					NBTTagList percentList = new NBTTagList();
					NBTTagList currentItems = soup.getTagCompound().getTagList("MainFood", 10);
					NBTTagList currentPercents = soup.getTagCompound().getTagList("FoodPercent", 10);
					this.mainFoodMap.clear();
					if (currentItems.tagCount() > 0) {
						for(int i = 0; i < currentItems.tagCount(); ++i) {
							float p = 0;
							if (i < currentPercents.tagCount()) p = currentPercents.getCompoundTagAt(i).getFloat("Percent");
							this.mainFoodMap.put(new ItemStackMapKey(new ItemStack(currentItems.getCompoundTagAt(i))), p);
						}
					}
					for (int i = 0; i < 4; ++i) {
						ItemStackMapKey st = new ItemStackMapKey(this.mainFood[i]);
						if (this.mainFoodMap.containsKey(st)) {
							float p = this.mainFoodMap.get(st);
							p = (p * soupPortion + this.foodPercent[i] * portion) / (soupPortion + portion);
							this.mainFoodMap.put(st, p);
						} else this.mainFoodMap.put(st, this.foodPercent[i]);
					}
					this.mainFoodMap = (LinkedHashMap<ItemStackMapKey,Float>) MapUtil.sortByValue(this.mainFoodMap, true);
					for (ItemStackMapKey key : this.mainFoodMap.keySet()) {
						ItemStack st = key.itemStack;
						if (!st.isEmpty()) {
							result = FurnaceRecipes.instance().getSmeltingResult(st);
							if (!result.isEmpty() && result.getItem() instanceof ItemFood) {
								colorList.appendTag(result.writeToNBT(new NBTTagCompound()));
							} else colorList.appendTag(st.writeToNBT(new NBTTagCompound()));
							tagList.appendTag(st.writeToNBT(new NBTTagCompound()));
							NBTTagCompound tag = new NBTTagCompound();
							tag.setFloat("Percent", this.mainFoodMap.get(key));
							percentList.appendTag(tag);
						} else break;
					}
					if (!tagList.hasNoTags()) {
						soup.getTagCompound().setTag("MainFood", tagList);
						soup.getTagCompound().setTag("FoodColors", colorList);
						soup.getTagCompound().setTag("FoodPercent", percentList);
					}
					this.setVolum(this.getVolum() - portion, true);
					return soup;
				}
				this.potions.clear();
			}
		}
		return ItemStack.EMPTY;
	}

	public ItemStack addFuel(ItemStack stack) {
		int time = TileEntityFurnace.getItemBurnTime(stack);
		if (time <= 0 || time > 2000 || !this.canSetBurningTime(time)) return stack;
		int i = 0;
		while (!stack.isEmpty()) {
			if (i >= SLOTS_FUEL_SIZE) break;
			if (this.getStackInSlot(i).isEmpty()) {
				if (this.addToBurningTime(time)) {
					this.setInventorySlotContents(i, stack.splitStack(1));
				} else break;
			}
            ++i;
		}
		return stack;
	}

	private int getBurningTime() {
		return this.totalBurningTime;
	}

	private static final int getMaxBurningTime() {
		return 10000;
	}

	private boolean addToBurningTime(int time) {
		if (canSetBurningTime(time)) {
			this.totalBurningTime += time;
			return true;
		}
		return false;
	}

	private boolean canSetBurningTime(int time) {
		return this.totalBurningTime + time < getMaxBurningTime();
	}

	public ArrayList<ItemStack> addFood(ItemStack stack, int slot) {
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		if (!this.hasCookingTool() || stack.isEmpty() || !(stack.getItem() instanceof ItemFood) ||
				(stack.getItem() instanceof IMistFood && !((IMistFood)stack.getItem()).isFood(stack))) {
			list.add(stack);
			return list;
		}
		ItemStack result = FurnaceRecipes.instance().getSmeltingResult(stack);
		if (this.getCookingTool() == CookingTool.POT) {
			if (this.volume == 0) { list.add(stack); return list; }
			ItemStack container = (stack.getItem() instanceof ItemSoup) ? new ItemStack(Items.BOWL) : getFoodContainer(stack);
			float amount;
			float saturation;
			if (!result.isEmpty() && result.getItem() instanceof ItemFood) {
				amount = ((ItemFood)result.getItem()).getHealAmount(result);
				saturation = ((ItemFood)result.getItem()).getSaturationModifier(result);
			} else {
				amount = ((ItemFood)stack.getItem()).getHealAmount(stack) * getHealBonus();
				saturation = ((ItemFood)stack.getItem()).getSaturationModifier(stack) * getHealBonus();
			}
			int count = stack.getCount();
			int i = SLOTS_FUEL_SIZE;
			boolean check = false;
			float toxic = stack.getItem() instanceof IMistFood ? ((IMistFood)stack.getItem()).getToxic(stack) : 0;
			while (!stack.isEmpty()) {
				if (i >= this.getSizeInventory()) break;
				if (this.getStackInSlot(i).isEmpty()) {
					check = true;
					if (this.addToHealAmount(amount, saturation, toxic)) {
						this.setInventorySlotContents(i, stack.splitStack(1));
					} else break;
				}
	            ++i;
			}
			if (!check) {
				ItemStack food;
				float[] aa = new float[SLOTS_FOOD_SIZE];
				float[] ss = new float[SLOTS_FOOD_SIZE];
				float foodAmount = 0;
				float foodSaturation;
				this.foodMap.clear();
				for (i = SLOTS_FUEL_SIZE; i < this.getSizeInventory(); ++i) {
					food = this.getStackInSlot(i);
					if (!food.isEmpty()) {
						result = FurnaceRecipes.instance().getSmeltingResult(food);
						if (!result.isEmpty() && result.getItem() instanceof ItemFood) {
							foodAmount = ((ItemFood)result.getItem()).getHealAmount(result);
							foodSaturation = ((ItemFood)result.getItem()).getSaturationModifier(result);
						} else {
							foodAmount = ((ItemFood)food.getItem()).getHealAmount(food) * getHealBonus();
							foodSaturation = ((ItemFood)food.getItem()).getSaturationModifier(food) * getHealBonus();
						}
						foodAmount = foodAmount * this.progress[i - SLOTS_FUEL_SIZE][1] / foodSize();
						foodSaturation = foodSaturation * this.progress[i - SLOTS_FUEL_SIZE][1] / foodSize();
						aa[i - SLOTS_FUEL_SIZE] = foodAmount;
						ss[i - SLOTS_FUEL_SIZE] = foodSaturation;
					}
					foodMap.put(i - SLOTS_FUEL_SIZE, foodAmount);
				}
				foodMap = (LinkedHashMap<Integer,Float>) MapUtil.sortByValue(foodMap, false);
				for (int j : foodMap.keySet()) {
					if (aa[j] < 0.25F) {
						if (this.canSetHealAmount(amount - aa[j])) {
							this.finalHealAmaunt += amount - aa[j];
							this.finalSaturation += saturation - ss[j];
							this.toxic += toxic;
							if (this.toxic > 10000) this.toxic = 10000;
							this.setInventorySlotContents(j + SLOTS_FUEL_SIZE, stack.splitStack(1));
						} else break;
					} else break;
				}
			}
			this.updateFoodProgress(false);
			list.add(stack);
			if (!container.isEmpty()) {
				count -= stack.getCount();
				if (container.isStackable()) {
					container.setCount(count);
					list.add(container);
				} else {
					for (int l = 0; l < count; ++l) {
						list.add(container);
					}
				}
			}
		} else if (this.getCookingTool() == CookingTool.GRILL) {
			if (slot >= 0 && slot < SLOTS_GRILL_SIZE && !result.isEmpty() && this.getStackInSlot(SLOTS_FUEL_SIZE + slot).isEmpty()) {
				this.setInventorySlotContents(SLOTS_FUEL_SIZE + slot, stack.splitStack(1));
				this.updateStatus();
			}
			list.add(stack);
		}
		return list;
	}

	private static ItemStack getFoodContainer(ItemStack stack) {
		return stack.getItem().getContainerItem(stack);
	}

	public ItemStack getGrillStack(int slot) {
		if (slot >= 0 && slot < SLOTS_GRILL_SIZE) return this.getStackInSlot(SLOTS_FUEL_SIZE + slot);
		return ItemStack.EMPTY;
	}

	public void setGrillStack(int slot, ItemStack stack) {
		if (slot >= 0 && slot < SLOTS_GRILL_SIZE) this.setInventorySlotContents(SLOTS_FUEL_SIZE + slot, stack);
	}

	public boolean isGrillEmpty() {
		if (this.getCookingTool() != CookingTool.GRILL) return true;
		for (int i = SLOTS_FUEL_SIZE; i < SLOTS_FUEL_SIZE + SLOTS_GRILL_SIZE; ++i) {
			if (!this.getStackInSlot(i).isEmpty()) return false;
		}
		return true;
	}

	private int getMaxHealAmaunt() {
		return this.volume * 20;
	}

	private static final float getHealBonus() {
		return 1.25F;
	}

	private static final int foodSize() {
		return 256;
	}

	public int getAshTimer() {
		return ashTimer;
	}

	public void setAshTimer(int timer) {
		this.ashTimer = timer;
	}

	private boolean addToHealAmount(float amount, float saturation, float toxic) {
		if (canSetHealAmount(amount)) {
			this.finalHealAmaunt += amount;
			this.finalSaturation += saturation;
			this.toxic += toxic;
			if (this.toxic > 10000) this.toxic = 10000;
			return true;
		}
		return false;
	}
	
	private boolean canSetHealAmount(float amount) {
		return this.finalHealAmaunt + amount < this.getMaxHealAmaunt();
	}

	public void setStone(ItemStack stack) {
		if (!stack.isEmpty()) {
			for (ItemStack stones : stoneAndColor.keySet()) {
				if (stones.isItemEqual(stack)) {
					this.stone = stones.copy();
					this.stoneColor = (int) stoneAndColor.get(stones).y;
					return;
				}
			}
		}
		this.stone = new ItemStack(MistItems.ROCKS);
		this.stoneColor = 0x9bb6af;
	}

	public static void putStoneAndColorList(Map<ItemStack,Vector2f> stoneAndColor) {
		TileEntityCampfire.stoneAndColor.clear();
		TileEntityCampfire.stoneAndColor.putAll(stoneAndColor);
	}

	public static void updateColorsToClient(EntityPlayerMP player) {
		PacketHandler.INSTANCE.sendTo(new PacketFirePitUpdate(stoneAndColor), player);
	}

	@SideOnly(Side.CLIENT)
	public static void updateColorsFromServer(Map<ItemStack,Vector2f> stoneAndColor) {
		Map<ItemStack,Vector2f> checkMap = new HashMap<ItemStack,Vector2f>();
		boolean check;
		for (ItemStack stonesS : stoneAndColor.keySet()) {
			check = false;
			for (ItemStack stonesC : TileEntityCampfire.stoneAndColor.keySet()) {
				if (stonesS.isItemEqual(stonesC)) {
					checkMap.put(stonesC, new Vector2f(stoneAndColor.get(stonesS).x, TileEntityCampfire.stoneAndColor.get(stonesC).y));
					check = true;
					break;
				}
			}
			if (!check) checkMap.put(stonesS, TileEntityCampfire.stoneAndColor.get(stonesS));
		}
		TileEntityCampfire.stoneAndColor.clear();
		TileEntityCampfire.stoneAndColor.putAll(checkMap);
	}

	public static int getStoneCount(ItemStack stack) {
		if (!stack.isEmpty()) {
			for (ItemStack stones : stoneAndColor.keySet()) {
				if (stones.isItemEqual(stack)) {
					return (int) stoneAndColor.get(stones).x;
				}
			}
		}
		return 0;
	}

	public int getStoneColor() {
		return this.stoneColor;
	}

	public ItemStack getStone() {
		return this.stone;
	}

	public List<ItemStack> getDrops(IBlockState state) {
		NonNullList<ItemStack> ret = NonNullList.create();
		ItemStack stones = getStone();
		int count = getStoneCount(stones);
		int stage = Math.min(state.getValue(MistCampfire.STAGE), 3) + 1;
		count = Math.round(stage * count / 4.0F);
		stones.setCount(count);
		ret.add(stones);
		ItemStack stack;
		for (int i = 0; i < SLOTS_FUEL_SIZE; ++i) {
			stack = this.getStackInSlot(i);
			if (!stack.isEmpty()) ret.add(stack);
		}
		if (this.hasCookingTool()) {
			ret.add(this.getCookingTool().getItem());
		}
		if (this.ashTimer / 1000 > 0) {
			ret.add(new ItemStack(MistItems.ASH, this.ashTimer / 1000));
		}
		ret = this.getFoodDrops(ret);
		return ret;
	}

	private NonNullList<ItemStack> getFoodDrops(NonNullList<ItemStack> list) {
		ItemStack stack;
		if (this.getCookingTool() == CookingTool.POT) {
			for (int i = SLOTS_FUEL_SIZE; i < this.getSizeInventory(); ++i) {
				stack = this.getStackInSlot(i);
				if (!stack.isEmpty() && this.progress[i - SLOTS_FUEL_SIZE][1] == foodSize() &&
						this.progress[i - SLOTS_FUEL_SIZE][0] < this.getPotCookTime()) {
					if (getFoodContainer(stack) == ItemStack.EMPTY && !(stack.getItem() instanceof ItemSoup)) {
						list.add(stack);
					}
				}
			}
		} else if (this.getCookingTool() == CookingTool.GRILL) {
			for (int i = SLOTS_FUEL_SIZE; i < this.getSizeInventory(); ++i) {
				stack = this.getStackInSlot(i);
				if (!stack.isEmpty()) list.add(stack);
			}
		}
		return list;
	}

	public void setFacing(EnumFacing face) {
		if (face != EnumFacing.UP && face != EnumFacing.DOWN) {
			this.facing = face;
		}
	}

	public EnumFacing getFacing() {
		return this.facing;
	}

	public void setCookingTool(CookingTool tool) {
		this.cookingTool = tool;
	}

	public CookingTool getCookingTool() {
		return this.cookingTool;
	}

	public boolean hasCookingTool() {
		return this.cookingTool != CookingTool.NONE;
	}

	//////////////////////////////////// INVENTORY  ////////////////////////////////////

	@Override
	public int getSizeInventory() {
		if (this.hasCookingTool()) {
			if (this.getCookingTool() == CookingTool.POT) {
				return SLOTS_FUEL_SIZE + SLOTS_FOOD_SIZE;
			} else if (this.getCookingTool() == CookingTool.GRILL) {
				return SLOTS_FUEL_SIZE + SLOTS_GRILL_SIZE;
			}
		}
		return SLOTS_FUEL_SIZE;
	}

	@Override
	public boolean isEmpty() {
		if (this.hasCookingTool()) {
			if (this.getCookingTool() == CookingTool.POT) {
				for (ItemStack stack : this.stacks) {
					if (!stack.isEmpty()) {
						return false;
					}
				}
			} else if (this.getCookingTool() == CookingTool.GRILL) {
				for (int i = 0; i < SLOTS_FUEL_SIZE + SLOTS_GRILL_SIZE; ++i) {
					if (!this.stacks.get(i).isEmpty()) {
						return false;
					}
				}
			}
		} else {
			for (int i = 0; i < SLOTS_FUEL_SIZE; ++i) {
				if (!this.stacks.get(i).isEmpty()) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		if (index < SLOTS_FUEL_SIZE || this.getCookingTool() == CookingTool.POT ||
				(this.getCookingTool() == CookingTool.GRILL && index < SLOTS_FUEL_SIZE + SLOTS_GRILL_SIZE)) {
			return this.stacks.get(index);
		}
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		return ItemStackHelper.getAndSplit(this.stacks, index, count);
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		return ItemStackHelper.getAndRemove(this.stacks, index);
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		this.stacks.set(index, stack);
		if (stack.getCount() > this.getInventoryStackLimit()) {
			stack.setCount(this.getInventoryStackLimit());
		}
		if (index >= SLOTS_FUEL_SIZE) {
			if (stack.isEmpty()) this.progress[index - SLOTS_FUEL_SIZE] = new float[2];
			else if (this.getCookingTool() == CookingTool.POT) this.progress[index - SLOTS_FUEL_SIZE][1] = foodSize();
		}
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		if (this.world.getTileEntity(this.pos) != this) return false;
		else return player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D;
	}

	@Override
	public void openInventory(EntityPlayer player) {}

	@Override
	public void closeInventory(EntityPlayer player) {}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if (index < SLOTS_FUEL_SIZE) {
			int time = TileEntityFurnace.getItemBurnTime(stack);
			return time > 0 && time <= 2000;
		} else {
			return stack.getItem() instanceof ItemFood;
		}
	}

	@Override
	public int getField(int id) {
		if (id == 0) return (int) (this.totalProgress * 10000);
		return 0;
	}

	@Override
	public void setField(int id, int value) {
		if (id == 0) this.totalProgress = (float) value / 10000;
	}

	@Override
	public int getFieldCount() {
		return 1;
	}

	@Override
	public void clear() {
		this.stacks.clear();
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		if (side != EnumFacing.DOWN) {
			if (side != EnumFacing.UP) {
				if (this.hasCookingTool()) {
					if (this.getCookingTool() == CookingTool.POT) return SLOTS_FOOD;
					else if (this.getCookingTool() == CookingTool.GRILL) return SLOTS_GRILL;
				}
			} else return SLOTS_FUEL;
		}
		return null;
	}

	@Override
	public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
		return this.isItemValidForSlot(index, stack);
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		return false;
	}

	//////////////////////////////////// TANK  ////////////////////////////////////

	@Override
	public FluidStack getFluid() {
		if (this.getCookingTool() == CookingTool.POT && this.getVolum() > 0 && this.finalHealAmaunt == 0) {
			if (this.milk == this.getVolum()) return new FluidStack(FluidRegistry.getFluid("milk"), this.volume * 250);
			else if (this.milk == 0) return new FluidStack(FluidRegistry.WATER, this.volume * 250);
		}
		return null;
	}

	@Override
	public int getFluidAmount() {
		return this.volume * 250;
	}

	@Override
	public int getCapacity() {
		return 1000;
	}

	@Override
	public FluidTankInfo getInfo() {
		return new FluidTankInfo(getFluid(), getCapacity());
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if (this.getCookingTool() == MistCampfire.CookingTool.POT) {
			int i = 4 - this.volume;
			boolean milk = resource.getFluid().getName().equals("milk");
			if (i > 0 && resource.amount >= 250) {
				if (resource.getFluid() == FluidRegistry.WATER || milk) {
					i = Math.min(i, resource.amount / 250);
					if (doFill) {
						this.setVolum(this.getVolum() + i, false);
						if (milk) this.addMilk(i);
					}
					return i * 250;
				}
			}
		}
		return 0;
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		if (this.getCookingTool() == MistCampfire.CookingTool.POT) {
			if (maxDrain > 250 && this.volume > 0 && this.totalProgress == 0) {
				boolean milk = this.milk == this.getVolum();
				if (this.milk == 0 || milk) {
					int i = Math.min(this.volume, maxDrain / 250);
					if (doDrain) this.setVolum(this.volume - i, false);
					if (milk) return new FluidStack(FluidRegistry.getFluid("milk"), this.volume * 250);
					else return new FluidStack(FluidRegistry.WATER, this.volume * 250);
				}
			}
		}
		return null;
	}
}
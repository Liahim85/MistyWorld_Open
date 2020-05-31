package ru.liahim.mist.tileentity;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.SlotFurnaceFuel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.api.loottable.LootTables;
import ru.liahim.mist.block.gizmos.MistFurnace;
import ru.liahim.mist.inventory.container.ContainerMistFurnace;

public class TileEntityMistFurnace extends TileEntityLockableLoot implements ITickable, ISidedInventory {

	private static final int[] SLOTS_INPUT = new int[] { 0, 1 };
	private static final int[] SLOTS_OUTPUT = new int[] { 0, 1, 2, 3 };
	private static final int[] SLOTS_LEFT = new int[] { 0 };
	private static final int[] SLOTS_RIGHT = new int[] { 1 };
	private NonNullList<ItemStack> furnaceItemStacks = NonNullList.<ItemStack>withSize(4, ItemStack.EMPTY);
	private int[] furnaceBurnTime = new int[] { 0, 0 };
	private int[] currentItemBurnTime = new int[] { 0, 0 };
	private float[] cookTime = new float[] { 0, 0 };
	private int[] totalCookTime = new int[] { 0, 0 };
	public int[] ashProgress = new int[] { 0, 0 };
	private boolean close = false;
	private boolean signal = false;

    protected ResourceLocation lootTableInput;
    protected ResourceLocation lootTableOutput;

	private float[] temperature = new float[11];
	private static final float[] leftFactor = new float[] { 0.84F, 0.91F, 0.96F, 0.99F, 1.0F, 0.99F, 0.96F, 0.91F, 0.84F, 0.74F, 0.63F };
	private static final float[] rightFactor = new float[] { 0.63F, 0.74F, 0.84F, 0.91F, 0.96F, 0.99F, 1.0F, 0.99F, 0.96F, 0.91F, 0.84F };
	private static final int[] leftLimit = new int[] { 2700, 2850, 2950, 3000, 3000, 3000, 2950, 2850, 2700, 2500, 2250 };
	private static final int[] rightLimit = new int[] { 2250, 2500, 2700, 2850, 2950, 3000, 3000, 3000, 2950, 2850, 2700 };
	private static final int[] mainLimit = new int[] { 2700, 2850, 2950, 3000, 3000, 3000, 3000, 3000, 2950, 2850, 2700 };
	
	private static final int radius = 8;
	public static final int ashTime = 1200;	
	public static final int burnTemp = 300;

	public void fire() {
		temperature[2] = Math.max(100, temperature[2]);
		temperature[3] = Math.max(150, temperature[3]);
		temperature[4] = Math.max(250, temperature[4]);
		temperature[5] = Math.max(300, temperature[5]);
		temperature[6] = Math.max(250, temperature[6]);
		temperature[7] = Math.max(150, temperature[7]);
		temperature[8] = Math.max(100, temperature[8]);
	}

	public float getMaxTemperature() {
		float t = 0;
		for (int i = 4; i <= 6; ++i) {
			if (temperature[i] > t) t = temperature[i];
		}
		return t;
	}

	private float getNormalTemperature() {
		return 2500;
	}

	private float getTemperature(int index) {
		return index == 0 ? getLeftTemperature() : getRightTemperature();
	}

	private float getLeftTemperature() {
		float t = 0;
		for (int i = 0; i < radius; ++i) t += temperature[i];
		return t / radius;
	}

	private float getRightTemperature() {
		float t = 0;
		for (int i = temperature.length - radius; i < temperature.length; ++i) t += temperature[i];
		return t / radius;
	}

	private void heatLeft(float amount) {
		for (int i = 0; i < temperature.length; ++i) {
			temperature[i] += amount * leftFactor[i];
			int limit = currentItemBurnTime[1] > 0 ? mainLimit[i] : leftLimit[i];
			if (temperature[i] > limit) temperature[i] = limit;
		}
	}

	private void heatRight(float amount) {
		for (int i = 0; i < temperature.length; ++i) {
			temperature[i] += amount * rightFactor[i];
			int limit = currentItemBurnTime[0] > 0 ? mainLimit[i] : rightLimit[i];
			if (temperature[i] > limit) temperature[i] = limit;
		}
	}
	
	private void heat(int index, float amount) {
		if (index == 0) heatLeft(amount);
		else heatRight(amount);
	}

	private void cool(float amount) {
		for (int i = 0; i < temperature.length; ++i) {
			temperature[i] -= amount;
			if (temperature[i] < 0) temperature[i] = 0;
		}
	}

	@Override
	public int getSizeInventory() {
		return this.furnaceItemStacks.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : this.furnaceItemStacks) {
			if (!itemstack.isEmpty()) return false;
		}
		return true;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		this.fillWithLoot((EntityPlayer)null);
		ItemStack itemstack = this.getItems().get(index);
		boolean flag = !stack.isEmpty() && stack.isItemEqual(itemstack) && ItemStack.areItemStackTagsEqual(stack, itemstack);
		this.getItems().set(index, stack);

		if (stack.getCount() > this.getInventoryStackLimit()) {
			stack.setCount(this.getInventoryStackLimit());
		}

		if (index < 2 && !flag) {
			this.totalCookTime[index] = this.getCookTime(stack);
			this.cookTime[index] = 0;
			this.markDirty();
		}
	}

	@Override
	public String getName() {
		return this.hasCustomName() ? this.customName : "container.furnace";
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.furnaceItemStacks = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
		if (!this.checkLootAndRead(compound)) ItemStackHelper.loadAllItems(compound, this.furnaceItemStacks);
		for (int i : SLOTS_INPUT) {
			this.furnaceBurnTime[i] = compound.getInteger("BurnTime_" + i);
			this.currentItemBurnTime[i] = compound.getInteger("ItemBurnTime_" + i);
			this.cookTime[i] = compound.getFloat("CookTime_" + i);
			this.totalCookTime[i] = compound.getInteger("CookTimeTotal_" + i);
			this.ashProgress[i] = compound.getInteger("AshProgress_" + i);
			this.close = compound.getBoolean("Close");
			this.signal = compound.getBoolean("Signal");
		}
		for (int i = 0; i < this.temperature.length; ++i) {
			this.temperature[i] = compound.getFloat("Temperature_" + i);
		}
		if (compound.hasKey("CustomName", 8)) this.customName = compound.getString("CustomName");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		for (int i : SLOTS_INPUT) {
			compound.setInteger("BurnTime_" + i, this.furnaceBurnTime[i]);
			compound.setInteger("ItemBurnTime_" + i, this.currentItemBurnTime[i]);
			compound.setFloat("CookTime_" + i, this.cookTime[i]);
			compound.setInteger("CookTimeTotal_" + i, this.totalCookTime[i]);
			compound.setInteger("AshProgress_" + i, this.ashProgress[i]);
			compound.setBoolean("Close", this.close);
			compound.setBoolean("Signal", this.signal);
		}
		for (int i = 0; i < this.temperature.length; ++i) {
			compound.setFloat("Temperature_" + i, this.temperature[i]);
		}
		if (!this.checkLootAndWrite(compound)) ItemStackHelper.saveAllItems(compound, this.furnaceItemStacks);
		if (this.hasCustomName()) compound.setString("CustomName", this.customName);
		return compound;
	}

	@Override
	protected boolean checkLootAndRead(NBTTagCompound compound) {
		if (compound.hasKey("LootTableInput", 8) && compound.hasKey("LootTableOutput", 8)) {
			this.lootTableInput = new ResourceLocation(compound.getString("LootTableInput"));
			this.lootTableOutput = new ResourceLocation(compound.getString("LootTableOutput"));
			this.lootTableSeed = compound.getLong("LootTableSeed");
			return true;
		} else return false;
	}

	@Override
	protected boolean checkLootAndWrite(NBTTagCompound compound) {
		if (this.lootTableInput != null && this.lootTableOutput != null) {
			compound.setString("LootTableInput", this.lootTableInput.toString());
			compound.setString("LootTableOutput", this.lootTableOutput.toString());
			if (this.lootTableSeed != 0L) compound.setLong("LootTableSeed", this.lootTableSeed);
			return true;
		} else return false;
	}

	@Override
	public ResourceLocation getLootTable() {
		return this.lootTableInput;
	}

	@Override
	public void setLootTable(ResourceLocation res, long seed) {
		this.lootTableInput = res;
		this.lootTableSeed = seed;
	}

	public void setLootTable(ResourceLocation input, ResourceLocation output, long seed) {
		this.setLootTable(input, seed);
		this.lootTableOutput = output;
	}

	public static void initializeLoot(TileEntity tile, Random rand) {
		if (tile instanceof TileEntityMistFurnace) {
			((TileEntityMistFurnace)tile).setLootTable(LootTables.FURNACE_INPUT_LOOT, LootTables.FURNACE_OUTPUT_LOOT, rand.nextLong());
			((TileEntityMistFurnace)tile).setClose(rand.nextBoolean());
		}
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

	public boolean hasSignal() {
		return signal;
	}

	public void setSignal(boolean signal) {
		this.signal = signal;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	private boolean isLeftBurning() {
		return this.furnaceBurnTime[0] > 0;
	}

	private boolean isRightBurning() {
		return this.furnaceBurnTime[1] > 0;
	}

	public boolean isBurning(int index) {
		return index == 0 ? isLeftBurning() : isRightBurning();
	}

	@SideOnly(Side.CLIENT)
	public static boolean isBurning(IInventory inventory) {
		return inventory.getField(0) > 0 || inventory.getField(1) > 0;
	}

	@SideOnly(Side.CLIENT)
	public static boolean isBurning(IInventory inventory, int index) {
		return inventory.getField(index) > 0;
	}

	@SideOnly(Side.CLIENT)
	public static int getTemp(IInventory inventory) {
		float t = 0;
		for (int i = 4; i <= 6; ++i) {
			if (inventory.getField(i + 10) > t) t = inventory.getField(i + 10);
		}
		return (int)t;
	}

	@SideOnly(Side.CLIENT)
	public int getClientComparatorOutput(IInventory inventory) {
		float t = 0;
		for (int i = 0; i < temperature.length; ++i) {
			t += inventory.getField(i + 10);
		}
		return comparatorMath(t);
	}

	@SideOnly(Side.CLIENT)
	public static int getTemp(IInventory inventory, int index) {
		return inventory.getField(index + 10);
	}

	@SideOnly(Side.CLIENT)
	public static boolean isClose(IInventory inventory) {
		return inventory.getField(21) == 0;
	}

	public boolean isClose() {
		return this.close;
	}

	public void setClose(boolean close) {
		this.close = close;
	}

	public int getComparatorOutput() {
		float t = 0;
		for (int i = 0; i < temperature.length; ++i) {
			t += temperature[i];
		}
		return comparatorMath(t);
	}

	private int comparatorMath(float temp) {
		temp -= 2000;
		return Math.max((int)Math.ceil(temp * 16 / 30000), 0);
	}

	private boolean stateCheck = false;

	@Override
	public void update() {
		if (!this.stateCheck) {
			this.updateStatus();
			this.stateCheck = true;
		}
		if (!this.world.isRemote) {
			boolean burnCheck = this.isLeftBurning() || this.isRightBurning();
			boolean tempCheck = this.getMaxTemperature() >= burnTemp;
			boolean change = false;
			int comp = this.getComparatorOutput();

			for (int i : SLOTS_INPUT) {
				if (this.isBurning(i)) {
					this.furnaceBurnTime[i] -= !this.close && this.ashProgress[i] < ashTime ? 1 : this.currentItemBurnTime[i] / 100;
					++this.ashProgress[i];
					this.heat(i, 3);
				}
				if (this.ashProgress[i] >= ashTime) putAsh(i);
			}
			if (this.getMaxTemperature() >= burnTemp) {
				if (!this.close) {
					for (int i : SLOTS_INPUT) {
						if (!this.isBurning(i) && this.ashProgress[i] < ashTime) {
							ItemStack fuelStack = this.furnaceItemStacks.get(i);
							this.furnaceBurnTime[i] = TileEntityFurnace.getItemBurnTime(fuelStack);
							this.currentItemBurnTime[i] = this.furnaceBurnTime[i];
							if (this.isBurning(i)) {
								change = true;
								if (!fuelStack.isEmpty()) {
									Item fuel = fuelStack.getItem();
									fuelStack.shrink(1);
									if (fuelStack.isEmpty()) this.furnaceItemStacks.set(i, fuel.getContainerItem(fuelStack));
								}
							}
						}
					}
				}
				for (int i : SLOTS_INPUT) {
					if ((this.close || this.currentItemBurnTime[i] == 0) && this.canSmelt(i)) {
						float t = this.getTemperature(i);
						if (t > burnTemp + 10) {
							this.cookTime[i] += t / this.getNormalTemperature();
							if (this.cookTime[i] >= this.totalCookTime[i]) {
								this.cookTime[i] = 0;
								this.totalCookTime[i] = this.getCookTime(this.furnaceItemStacks.get(i));
								this.smeltItem(i);
								change = true;
							}
						}
					} else this.cookTime[i] = 0;
				}
			} else {
				for (int i : SLOTS_INPUT) {
					if (this.cookTime[i] > 0) this.cookTime[i] = MathHelper.clamp(this.cookTime[i] - 2, 0, this.totalCookTime[i]);
				}
			}

			this.cool(this.close || this.isLeftBurning() || this.isRightBurning() ? 1 : 2);

			if ((burnCheck != this.isLeftBurning() || this.isRightBurning()) || tempCheck != (this.getMaxTemperature() >= burnTemp)) {
				change = true;
				updateStatus();
			}
			if (comp != this.getComparatorOutput()) {
				change = true;
				MistFurnace.checkSingal(this.world, this.pos, this);
			}
			if (change) this.markDirty();
		}
	}

	public void updateStatus() {
		boolean burn = this.getMaxTemperature() >= burnTemp;
		int status = this.close ? burn ? 4 : 0 : this.isLeftBurning() || this.isRightBurning() ? 3 : burn ? 2 : 1;
		MistFurnace.setState(status, this.world, this.pos);
	}

	@SideOnly(Side.CLIENT)
	public static int getStatus(IInventory inventory) {
		boolean burn = getTemp(inventory) >= burnTemp;
		return !isClose(inventory) ? burn ? 4 : 0 : isBurning(inventory) ? 3 : burn ? 2 : 1;
	}

	public int getCookTime(ItemStack stack) {
		return 200;
	}

	private boolean canSmelt(int index) {
		if (this.furnaceItemStacks.get(index).isEmpty()) return false;
		else {
			ItemStack stack = FurnaceRecipes.instance().getSmeltingResult(this.furnaceItemStacks.get(index));
			if (stack.isEmpty()) return false;
			else {
				ItemStack outStack = this.furnaceItemStacks.get(index + 2);
				if (outStack.isEmpty()) return true;
				else if (!outStack.isItemEqual(stack)) return false;
				else if (outStack.getCount() + stack.getCount() <= this.getInventoryStackLimit() &&
						outStack.getCount() + stack.getCount() <= outStack.getMaxStackSize()) return true;
				else return outStack.getCount() + stack.getCount() <= stack.getMaxStackSize();
			}
		}
	}

	public void smeltItem(int index) {
		if (this.canSmelt(index)) {
			ItemStack itemstack = this.furnaceItemStacks.get(index);
			ItemStack itemstack1 = FurnaceRecipes.instance().getSmeltingResult(itemstack);
			ItemStack itemstack2 = this.furnaceItemStacks.get(index + 2);
			if (itemstack2.isEmpty()) {
				this.furnaceItemStacks.set(index + 2, itemstack1.copy());
			} else if (itemstack2.getItem() == itemstack1.getItem()) {
				itemstack2.grow(itemstack1.getCount());
			}
			itemstack.shrink(1);
		}
	}

	public void putAsh(int index) {
		ItemStack out = this.furnaceItemStacks.get(index + 2);
		if (out.isEmpty()) {
			this.furnaceItemStacks.set(index + 2, new ItemStack(MistItems.ASH));
			this.ashProgress[index] = 0;
		} else if (out.getItem() == MistItems.ASH && out.getCount() < 16) {
			out.grow(1);
			this.ashProgress[index] = 0;
		} else this.ashProgress[index] = ashTime;
	}

	@Override
	public void openInventory(EntityPlayer player) {}

	@Override
	public void closeInventory(EntityPlayer player) { this.updateStatus(); }

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if (index > 1) return false;
		else {
			ItemStack itemstack = this.furnaceItemStacks.get(1);
			return !FurnaceRecipes.instance().getSmeltingResult(stack).isEmpty() || TileEntityFurnace.isItemFuel(stack);
		}
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		if (side == EnumFacing.DOWN) return SLOTS_OUTPUT;
		else if (side == EnumFacing.UP) return SLOTS_INPUT;
		else {
			EnumFacing face = world.getBlockState(pos).getValue(MistFurnace.FACING);
			if (side == face.rotateY()) return SLOTS_LEFT;
			else if (side == face.rotateYCCW()) return SLOTS_RIGHT;
			else return SLOTS_INPUT;
		}
	}

	@Override
	public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
		return this.isItemValidForSlot(index, stack);
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		return direction == EnumFacing.DOWN && index > 1 || SlotFurnaceFuel.isBucket(stack);
	}

	@Override
	public String getGuiID() {
		return "mist:furnace";
	}

	@Override
	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer player) {
		this.fillWithLoot(player);
		return new ContainerMistFurnace(playerInventory, this);
	}

	@Override
	public void fillWithLoot(@Nullable EntityPlayer player) {
		if (this.lootTableInput != null && this.lootTableOutput != null && this.world instanceof WorldServer) {
			LootTable inputTable = this.world.getLootTableManager().getLootTableFromLocation(this.lootTableInput);
			LootTable outputTable = this.world.getLootTableManager().getLootTableFromLocation(this.lootTableOutput);
			this.lootTableInput = null;
			this.lootTableOutput = null;
			Random random;
			if (this.lootTableSeed == 0L) random = new Random();
			else random = new Random(this.lootTableSeed);
			LootContext.Builder builder = new LootContext.Builder((WorldServer) this.world);
			if (player != null) builder.withLuck(player.getLuck()).withPlayer(player);
			List<ItemStack> inputList = inputTable.generateLootForPools(random, builder.build());
			List<ItemStack> outputList = outputTable.generateLootForPools(random, builder.build());
			if (inputList.size() > 0) {
				if (inputList.size() < 2) inputList.add(this.world.rand.nextInt(2), ItemStack.EMPTY);
				for (int i : SLOTS_INPUT) this.setInventorySlotContents(i, inputList.get(i));
			}
			if (outputList.size() > 0) {
				if (outputList.size() < 2) outputList.add(this.world.rand.nextInt(2), ItemStack.EMPTY);
				for (int i : SLOTS_INPUT) this.setInventorySlotContents(i + 2, outputList.get(i));
			}
		}
	}

	@Override
	public int getField(int id) {
		switch (id) {
		case 0:
			return this.furnaceBurnTime[0];
		case 1:
			return this.furnaceBurnTime[1];
		case 2:
			return this.currentItemBurnTime[0];
		case 3:
			return this.currentItemBurnTime[1];
		case 4:
			return (int)this.cookTime[0];
		case 5:
			return (int)this.cookTime[1];
		case 6:
			return this.totalCookTime[0];
		case 7:
			return this.totalCookTime[1];
		case 8:
			return this.ashProgress[0];
		case 9:
			return this.ashProgress[1];
		case 10:
			return (int)this.temperature[0];
		case 11:
			return (int)this.temperature[1];
		case 12:
			return (int)this.temperature[2];
		case 13:
			return (int)this.temperature[3];
		case 14:
			return (int)this.temperature[4];
		case 15:
			return (int)this.temperature[5];
		case 16:
			return (int)this.temperature[6];
		case 17:
			return (int)this.temperature[7];
		case 18:
			return (int)this.temperature[8];
		case 19:
			return (int)this.temperature[9];
		case 20:
			return (int)this.temperature[10];
		case 21:
			return this.close ? 0 : 1;
		default:
			return 0;
		}
	}

	@Override
	public void setField(int id, int value) {
		switch (id) {
		case 0:
			this.furnaceBurnTime[0] = value; break;
		case 1:
			this.furnaceBurnTime[1] = value; break;
		case 2:
			this.currentItemBurnTime[0] = value; break;
		case 3:
			this.currentItemBurnTime[1] = value; break;
		case 4:
			this.cookTime[0] = value; break;
		case 5:
			this.cookTime[1] = value; break;
		case 6:
			this.totalCookTime[0] = value; break;
		case 7:
			this.totalCookTime[1] = value; break;
		case 8:
			this.ashProgress[0] = value; break;
		case 9:
			this.ashProgress[1] = value; break;
		case 10:
			this.temperature[0] = value; break;
		case 11:
			this.temperature[1] = value; break;
		case 12:
			this.temperature[2] = value; break;
		case 13:
			this.temperature[3] = value; break;
		case 14:
			this.temperature[4] = value; break;
		case 15:
			this.temperature[5] = value; break;
		case 16:
			this.temperature[6] = value; break;
		case 17:
			this.temperature[7] = value; break;
		case 18:
			this.temperature[8] = value; break;
		case 19:
			this.temperature[9] = value; break;
		case 20:
			this.temperature[10] = value; break;
		case 21:
			this.close = value == 0;
		}
	}

	@Override
	public int getFieldCount() {
		return 22;
	}

	IItemHandler[] handler = new SidedInvWrapper[] {
		new SidedInvWrapper(this, EnumFacing.DOWN),
		new SidedInvWrapper(this, EnumFacing.UP),
		new SidedInvWrapper(this, EnumFacing.NORTH),
		new SidedInvWrapper(this, EnumFacing.SOUTH),
		new SidedInvWrapper(this, EnumFacing.WEST),
		new SidedInvWrapper(this, EnumFacing.EAST)
	};

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return (T) handler[facing.getIndex()];
		}
		return super.getCapability(capability, facing);
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.furnaceItemStacks;
	}
}
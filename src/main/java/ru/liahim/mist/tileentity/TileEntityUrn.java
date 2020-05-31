package ru.liahim.mist.tileentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.oredict.OreDictionary;
import ru.liahim.mist.api.biome.EnumBiomeType;
import ru.liahim.mist.api.loottable.LootTables;
import ru.liahim.mist.api.sound.MistSounds;
import ru.liahim.mist.block.gizmos.MistUrn;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.init.ModConfig;
import ru.liahim.mist.inventory.container.ContainerUrn;
import ru.liahim.mist.util.ColorHelper;
import ru.liahim.mist.util.InventoryUtil;

public class TileEntityUrn extends TileEntityLockableLoot implements ITickable {

    private final int size = 9;
    private NonNullList<ItemStack> urnContents = NonNullList.<ItemStack>withSize(size, ItemStack.EMPTY);
    public float lidAngle;
    public float prevLidAngle;
    public int numPlayersUsing;
    /** Server sync counter (once per 20 ticks) */
    private int ticksSinceSync;
    public EnumFacing openSide;
    public static final int clayColor = 0xc0795b;
    public static final int rawColor = 0xbec8d5;
    private UrnType urnType = UrnType.NORMAL;
    private int tintColor = -1;
    private int patina = -1;
    private String tooltip = "";
    private String location = "";
    public boolean bug;

    public UrnType getUrnType() {
        return urnType;
    }

    public int getTintColor() {
        return tintColor < 0 ? clayColor : tintColor;
    }

    public int getPatina() {
        return patina < 0 ? getTintColor() : patina;
    }

    public String getTooltip() {
        return tooltip;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public int getSizeInventory() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : urnContents) {
            if (!itemstack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public int getInventoryStackLimit() {
        return getUrnType().isRare() ? 64 : 16;
    }

    @Override
    public String getName() {
        return hasCustomName() ? customName : getUrnType().isRare() ? "item.mist.urn_rare" : "item.mist.urn";
    }

    @Override
    public boolean hasCustomName() {
        return customName != null && !customName.isEmpty();
    }

    @Override
    public void setCustomName(String name) {
        customName = name;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("CustomName", 8)) customName = compound.getString("CustomName");
        readFromNBTUrn(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (hasCustomName()) compound.setString("CustomName", customName);
        return writeToNBTUrn(compound, true);
    }

    public void readFromNBTUrn(NBTTagCompound compound) {
        urnContents = NonNullList.<ItemStack>withSize(getSizeInventory(), ItemStack.EMPTY);
        if (!checkLootAndRead(compound)) ItemStackHelper.loadAllItems(compound, urnContents);
        urnType = UrnType.byId(compound.getInteger("UrnType"));
        if (compound.hasKey("TintColor")) tintColor = compound.getInteger("TintColor");
        if (compound.hasKey("PatinaColor")) patina = compound.getInteger("PatinaColor");
        tooltip = compound.getString("UrnTooltip");
        location = compound.getString("UrnLocation");
        bug = compound.getBoolean("Bug");
    }

    public NBTTagCompound writeToNBTUrn(NBTTagCompound compound, boolean checkLoot) {
        if (!checkLoot || !checkLootAndWrite(compound)) ItemStackHelper.saveAllItems(compound, urnContents);
        compound.setInteger("UrnType", getUrnType().getId());
        compound.setInteger("TintColor", tintColor);
        compound.setInteger("PatinaColor", patina);
        compound.setString("UrnTooltip", getTooltip());
        compound.setString("UrnLocation", getLocation());
        compound.setBoolean("Bug", bug);
        return compound;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, getBlockMetadata(), getUpdateTag());
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
        updateStatus(world.getBlockState(pos));
    }

    public void updateStatus(IBlockState state) {
        markDirty();
        world.notifyBlockUpdate(pos, world.getBlockState(pos), state, 3);
    }

    @Override
    public void update() {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        ++ticksSinceSync;
        if (!world.isRemote && numPlayersUsing != 0 && (ticksSinceSync + x + y + z) % 200 == 0) {
            numPlayersUsing = 0;
            float f = 5.0F;
            for (EntityPlayer player : world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(x - f, y - f, z - f, x + 1 + f, y + 1 + f, z + 1 + f))) {
                if (player.openContainer instanceof ContainerUrn && ((ContainerUrn) player.openContainer).getUrnInventory() == this) {
                    ++numPlayersUsing;
                }
            }
        }
        prevLidAngle = lidAngle;
        if (numPlayersUsing > 0 && lidAngle == 0.0F) {
            world.playSound((EntityPlayer) null, x + 0.5D, y + 0.5D, z + 0.5D, MistSounds.BLOCK_URN_OPEN, SoundCategory.BLOCKS, 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
        }
        if (numPlayersUsing == 0 && lidAngle > 0.0F || numPlayersUsing > 0 && lidAngle < 1.0F) {
            float speed = 0.1F;

            if (numPlayersUsing > 0) lidAngle += speed;
            else lidAngle -= speed;

            if (lidAngle > 1.0F) lidAngle = 1.0F;

            if (lidAngle < 0.5F && prevLidAngle >= 0.5F) {
                world.playSound((EntityPlayer) null, x + 0.5D, y + 0.5D, z + 0.5D, MistSounds.BLOCK_URN_CLOSE, SoundCategory.BLOCKS, 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
            }
            if (lidAngle < 0.0F) lidAngle = 0.0F;
        }
    }

    @Override
    public boolean receiveClientEvent(int id, int type) {
        if (id == 1) {
            numPlayersUsing = type;
            return true;
        } else if (id == 2) {
            openSide = EnumFacing.getFront(type);
            return true;
        } else return super.receiveClientEvent(id, type);
    }

    @Override
    public void openInventory(EntityPlayer player) {
        if (!player.isSpectator()) {
            if (numPlayersUsing < 0) numPlayersUsing = 0;
            ++numPlayersUsing;
            world.addBlockEvent(pos, getBlockType(), 1, numPlayersUsing);
            if (!world.isRemote)
                world.addBlockEvent(pos, getBlockType(), 2, openSide.getIndex());
        }
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        if (!player.isSpectator() && getBlockType() instanceof MistUrn) {
            --numPlayersUsing;
            world.addBlockEvent(pos, getBlockType(), 1, numPlayersUsing);
        }
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer player) {
        fillWithLoot(player);
        return new ContainerUrn(this, player);

	}

	@Override
	public void fillWithLoot(@Nullable EntityPlayer player) {
		if (this.lootTable != null && this.world instanceof WorldServer) {
			LootTable loottable = this.world.getLootTableManager().getLootTableFromLocation(this.lootTable);
			this.lootTable = null;
			this.bug = false;
			Random random;
			if (this.lootTableSeed == 0L) random = new Random();
			else random = new Random(this.lootTableSeed);
			LootContext.Builder builder = new LootContext.Builder((WorldServer) this.world);
			if (player != null) builder.withLuck(player.getLuck()).withPlayer(player);
			InventoryUtil.optimizeAndFillInventory(this, loottable.generateLootForPools(random, builder.build()), random);
		}
	}

	public boolean isBug() {
		return this.bug && this.lootTable != null && !this.urnType.rare;
	}

    @Override
    public String getGuiID() {
        return "mist:urn";
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return urnContents;
    }

    private static final String urnLocation = "textures/entity/urn/";

	public static enum UrnType {

		NORMAL(0, urnLocation + "urn_normal.png", false, "paper"),
		PATINA(1, urnLocation + "urn_patina.png", false, "mulch"),
		LINE(2, urnLocation + "urn_line.png", false, "string"),
		STRIPE(3, urnLocation + "urn_stripe.png", false, "stickWood"),
		STARS(4, urnLocation + "urn_stars.png", false, "dustSalt", "dustSaltpeter"),
		CLOTH(5, urnLocation + "urn_cloth.png", false, "feather"),
		ZONE(6, urnLocation + "urn_zone.png", false, "bone"),
		RARE_1(16, urnLocation + "urn_rare_1.png", true),
		RARE_2(17, urnLocation + "urn_rare_2.png", true),
		RARE_3(18, urnLocation + "urn_rare_3.png", true),
		RARE_4(19, urnLocation + "urn_rare_4.png", true),
		RARE_5(20, urnLocation + "urn_rare_5.png", true),
		RARE_6(21, urnLocation + "urn_rare_6.png", true),
		RARE_7(22, urnLocation + "urn_rare_7.png", true),
		RARE_8(23, urnLocation + "urn_rare_8.png", true),
		RARE_9(24, urnLocation + "urn_rare_9.png", true),
		RARE_10(25, urnLocation + "urn_rare_10.png", true),
		RARE_11(26, urnLocation + "urn_rare_11.png", true),
		RARE_12(27, urnLocation + "urn_rare_12.png", true);

		private final int id;
		private final ResourceLocation res;
		private final boolean rare;
		private final String[] tool;
		private static final HashMap<Integer, UrnType> ID_LOOKUP = new HashMap<>();
		public static final HashMap<String[], UrnType> TOOL_LOOKUP = new HashMap<>();
		public static final ArrayList<UrnType> RARE_LOOKUP = new ArrayList<>();

		UrnType(int id, String textureLocation, boolean rare, String... tool) {
			this.id = id;
			this.res = new ResourceLocation(Mist.MODID, textureLocation);
			this.rare = rare;
			this.tool = tool;
		}

		public int getId() {
			return this.id;
		}

		public ResourceLocation getTexture() {
			return res;
		}

		public boolean isRare() {
			return this.rare;
		}

		public String[] getTool() {
			return this.tool;
		}

		public static UrnType byId(int id) {
			if (ID_LOOKUP.containsKey(id)) return ID_LOOKUP.get(id);
			return UrnType.NORMAL;
		}

		public static UrnType byTool(ItemStack stack) {
			for (String[] tools : UrnType.TOOL_LOOKUP.keySet()) {
				for (String tool : tools) {
					if (OreDictionary.containsMatch(false, OreDictionary.getOres(tool), stack)) return TOOL_LOOKUP.get(tools);
				}
			}
			return UrnType.NORMAL;
		}

		public static boolean isTool(ItemStack stack) {
			for (String[] tools : UrnType.TOOL_LOOKUP.keySet()) {
				for (String tool : tools) {
					if (OreDictionary.containsMatch(false, OreDictionary.getOres(tool), stack)) return true;
				}
			}
			return false;
		}

		public static NBTTagCompound getTag(ItemStack urn) {
			return urn.getSubCompound("Urn");
		}

		public static UrnType getType(ItemStack urn, NBTTagCompound tag) {
			if (tag == null) tag = getTag(urn);
			return tag != null && tag.hasKey("UrnType") ? UrnType.byId(tag.getInteger("UrnType")) : UrnType.NORMAL;
		}

		public static int getTintColor(ItemStack urn, NBTTagCompound tag) {
			if (tag == null) tag = getTag(urn);
			return tag != null && tag.hasKey("TintColor") ? tag.getInteger("TintColor") : -1;
		}
		
		public static int getPatinaColor(ItemStack urn, NBTTagCompound tag) {
			if (tag == null) tag = getTag(urn);
			return tag != null && tag.hasKey("PatinaColor") ? tag.getInteger("PatinaColor") : -1;
		}

		public static String getTooltip(ItemStack urn, NBTTagCompound tag) {
			if (tag == null) tag = getTag(urn);
			if(tag != null) {
				String tooltip = tag.getString("UrnTooltip");
				if (!tooltip.isEmpty()) {
					tooltip = I18n.format("item.mist.urn.tooltip." + tooltip);
					String location = tag.getString("UrnLocation");
					if (!location.isEmpty()) location = I18n.format("item.mist.urn.location." + location);
					return tooltip + " (" + location + ")";
				}
			}
			return "";
		}

		static { for (UrnType type : values()) ID_LOOKUP.put(type.getId(), type); }
		static { for (UrnType type : values()) if (type.getTool() != null) TOOL_LOOKUP.put(type.getTool(), type); }
		static { for (UrnType type : values()) if (type.rare) RARE_LOOKUP.add(type); }
	}

	private static final ResourceLocation[] SWAMP_TOMB_LOOT = new ResourceLocation[]  { LootTables.URN_FUNERARY_LOOT, LootTables.URN_FUNERARY_LOOT, LootTables.URN_MIXED_LOOT, LootTables.URN_MIXED_LOOT, LootTables.URN_RICHES_LOOT };
	private static final ResourceLocation[] FOREST_TOMB_LOOT = new ResourceLocation[] { LootTables.URN_RICHES_LOOT, LootTables.URN_RICHES_LOOT, LootTables.URN_MIXED_LOOT };
	private static final ResourceLocation[] DESERT_TOMB_LOOT = new ResourceLocation[] { LootTables.URN_FUNERARY_LOOT, LootTables.URN_MIXED_LOOT };
	private static final ResourceLocation[] JUNGLE_TOMB_LOOT = new ResourceLocation[] { LootTables.URN_FUNERARY_LOOT, LootTables.URN_MIXED_LOOT, LootTables.URN_RICHES_LOOT };
	private static final ResourceLocation[] SNOW_TOMB_LOOT = new ResourceLocation[]   { LootTables.URN_RICHES_LOOT, LootTables.URN_MIXED_LOOT, LootTables.URN_FUNERARY_LOOT };

	private static final int forestColor_1 = ColorHelper.mixColor(EnumDyeColor.ORANGE.colorValue, EnumDyeColor.YELLOW.colorValue, EnumDyeColor.SILVER.colorValue, EnumDyeColor.SILVER.colorValue, EnumDyeColor.SILVER.colorValue);
	private static final int forestColor_2 = EnumDyeColor.BROWN.colorValue;
	private static final int desertColor_1 = ColorHelper.mixColor(EnumDyeColor.YELLOW.colorValue, EnumDyeColor.YELLOW.colorValue, EnumDyeColor.YELLOW.colorValue, EnumDyeColor.ORANGE.colorValue, EnumDyeColor.LIME.colorValue, EnumDyeColor.SILVER.colorValue);
	private static final int desertColor_2 = ColorHelper.mixColor(EnumDyeColor.BROWN.colorValue, EnumDyeColor.ORANGE.colorValue, EnumDyeColor.SILVER.colorValue);
	private static final int desertColor_3 = ColorHelper.mixColor(EnumDyeColor.YELLOW.colorValue, EnumDyeColor.SILVER.colorValue, EnumDyeColor.SILVER.colorValue, EnumDyeColor.WHITE.colorValue);
	private static final int desertColor_4 = ColorHelper.mixColor(EnumDyeColor.BLUE.colorValue,EnumDyeColor.BLUE.colorValue, EnumDyeColor.BLUE.colorValue, EnumDyeColor.CYAN.colorValue, EnumDyeColor.YELLOW.colorValue);
	private static final int jungleColor_1 = ColorHelper.mixColor(EnumDyeColor.BLACK.colorValue, EnumDyeColor.RED.colorValue);
	private static final int jungleColor_2 = ColorHelper.mixColor(EnumDyeColor.ORANGE.colorValue,EnumDyeColor.RED.colorValue);
	private static final int snowColor_1 = ColorHelper.mixColor(EnumDyeColor.BLACK.colorValue, EnumDyeColor.SILVER.colorValue, EnumDyeColor.BROWN.colorValue);
	private static final int snowColor_2 = ColorHelper.mixColor(EnumDyeColor.SILVER.colorValue, EnumDyeColor.SILVER.colorValue, EnumDyeColor.BROWN.colorValue);
	private static final int snowColor_3 = ColorHelper.mixColor(EnumDyeColor.ORANGE.colorValue, EnumDyeColor.SILVER.colorValue, EnumDyeColor.SILVER.colorValue);
	private static final int swampColor_1 = ColorHelper.mixColor(EnumDyeColor.GREEN.colorValue, EnumDyeColor.LIGHT_BLUE.colorValue, EnumDyeColor.ORANGE.colorValue);
	private static final int swampColor_2 = ColorHelper.mixColor(EnumDyeColor.GREEN.colorValue, EnumDyeColor.BROWN.colorValue);
	
	public static enum UrnLootType {

		SWAMP_BASEMENTS_1(UrnType.ZONE, ColorHelper.mixColor(EnumDyeColor.LIME.colorValue, EnumDyeColor.LIME.colorValue, EnumDyeColor.BROWN.colorValue), ColorHelper.mixColor(EnumDyeColor.LIME.colorValue, EnumDyeColor.GREEN.colorValue, EnumDyeColor.BROWN.colorValue), "basement", "swamp", LootTables.URN_BASEMENTS_SWAMP_LOOT),
		SWAMP_BASEMENTS_2(UrnType.PATINA, ColorHelper.mixColor(EnumDyeColor.BROWN.colorValue, EnumDyeColor.BROWN.colorValue, EnumDyeColor.LIME.colorValue, EnumDyeColor.CYAN.colorValue, EnumDyeColor.WHITE.colorValue), ColorHelper.mixColor(EnumDyeColor.BROWN.colorValue, EnumDyeColor.BLUE.colorValue, EnumDyeColor.WHITE.colorValue), "basement", "swamp", LootTables.URN_BASEMENTS_SWAMP_LOOT),
		SWAMP_BASEMENTS_3(UrnType.STARS, ColorHelper.mixColor(EnumDyeColor.BROWN.colorValue, EnumDyeColor.LIME.colorValue, EnumDyeColor.SILVER.colorValue), ColorHelper.mixColor(EnumDyeColor.BROWN.colorValue, EnumDyeColor.GREEN.colorValue, EnumDyeColor.GREEN.colorValue), "basement", "swamp", LootTables.URN_BASEMENTS_SWAMP_LOOT),
		SWAMP_WELLS_1(UrnType.PATINA, ColorHelper.mixColor(EnumDyeColor.YELLOW.colorValue, EnumDyeColor.YELLOW.colorValue, EnumDyeColor.SILVER.colorValue, EnumDyeColor.SILVER.colorValue, EnumDyeColor.SILVER.colorValue, EnumDyeColor.LIGHT_BLUE.colorValue), EnumDyeColor.GREEN.colorValue, "well", "swamp", LootTables.URN_WELLS_LOOT),
		SWAMP_WELLS_2(UrnType.PATINA, ColorHelper.mixColor(EnumDyeColor.LIGHT_BLUE.colorValue, EnumDyeColor.LIGHT_BLUE.colorValue, EnumDyeColor.LIGHT_BLUE.colorValue, EnumDyeColor.SILVER.colorValue, EnumDyeColor.SILVER.colorValue, EnumDyeColor.SILVER.colorValue, EnumDyeColor.ORANGE.colorValue, EnumDyeColor.BROWN.colorValue), EnumDyeColor.GREEN.colorValue, "well", "swamp", LootTables.URN_WELLS_LOOT),
		SWAMP_TOMB_1(UrnType.STRIPE, swampColor_1, swampColor_2, "burial", "swamp", SWAMP_TOMB_LOOT),
		SWAMP_TOMB_2(UrnType.ZONE, swampColor_1, swampColor_2, "burial", "swamp", SWAMP_TOMB_LOOT),
		SWAMP_TOMB_3(UrnType.PATINA, swampColor_1, swampColor_2, "burial", "swamp", SWAMP_TOMB_LOOT),
		SWAMP_TOMB_4(UrnType.STARS, swampColor_1, swampColor_2, "burial", "swamp", SWAMP_TOMB_LOOT),
		SWAMP_TOMB_5(UrnType.CLOTH, swampColor_1, swampColor_2, "burial", "swamp", SWAMP_TOMB_LOOT),
		DESERT_ALTAR_1(UrnType.STARS, ColorHelper.mixColor(EnumDyeColor.BLUE.colorValue, EnumDyeColor.CYAN.colorValue, EnumDyeColor.WHITE.colorValue), ColorHelper.mixColor(EnumDyeColor.BLUE.colorValue, EnumDyeColor.LIGHT_BLUE.colorValue, EnumDyeColor.BROWN.colorValue), "altar", "desert", LootTables.URN_ALTARS_DESERT_LOOT),
		DESERT_BASEMENTS_1(UrnType.STRIPE, desertColor_3, desertColor_4, "basement", "desert", LootTables.URN_BASEMENTS_DESERT_LOOT),
		DESERT_BASEMENTS_2(UrnType.PATINA, desertColor_3, desertColor_4, "basement", "desert", LootTables.URN_BASEMENTS_DESERT_LOOT),
		DESERT_BASEMENTS_3(UrnType.LINE, desertColor_3, desertColor_4, "basement", "desert", LootTables.URN_BASEMENTS_DESERT_LOOT),
		DESERT_WELLS_1(UrnType.PATINA, ColorHelper.mixColor(EnumDyeColor.BLUE.colorValue, EnumDyeColor.LIGHT_BLUE.colorValue, EnumDyeColor.YELLOW.colorValue), ColorHelper.mixColor(EnumDyeColor.BLUE.colorValue, EnumDyeColor.LIGHT_BLUE.colorValue, EnumDyeColor.BROWN.colorValue), "well", "desert", LootTables.URN_WELLS_LOOT),
		DESERT_WELLS_2(UrnType.STARS, ColorHelper.mixColor(EnumDyeColor.LIGHT_BLUE.colorValue, EnumDyeColor.LIGHT_BLUE.colorValue, EnumDyeColor.YELLOW.colorValue, EnumDyeColor.ORANGE.colorValue, EnumDyeColor.SILVER.colorValue), ColorHelper.mixColor(EnumDyeColor.BLUE.colorValue, EnumDyeColor.CYAN.colorValue, EnumDyeColor.SILVER.colorValue, EnumDyeColor.SILVER.colorValue), "well", "desert", LootTables.URN_WELLS_LOOT),
		DESERT_TOMB_1(UrnType.ZONE, desertColor_1, desertColor_2, "burial", "desert", DESERT_TOMB_LOOT),
		DESERT_TOMB_2(UrnType.ZONE, desertColor_2, desertColor_1, "burial", "desert", DESERT_TOMB_LOOT),
		DESERT_TOMB_3(UrnType.LINE, desertColor_1, desertColor_2, "burial", "desert", DESERT_TOMB_LOOT),
		DESERT_TOMB_4(UrnType.STARS, desertColor_2, desertColor_1, "burial", "desert", DESERT_TOMB_LOOT),
		DESERT_TOMB_5(UrnType.NORMAL, desertColor_1, desertColor_2, "burial", "desert", DESERT_TOMB_LOOT),
		JUNGLE_BASEMENTS_1(UrnType.PATINA, ColorHelper.mixColor(EnumDyeColor.ORANGE.colorValue, EnumDyeColor.RED.colorValue, EnumDyeColor.SILVER.colorValue, EnumDyeColor.YELLOW.colorValue), ColorHelper.mixColor(EnumDyeColor.ORANGE.colorValue, EnumDyeColor.CYAN.colorValue), "basement", "jungle", LootTables.URN_BASEMENTS_JUNGLE_LOOT),
		JUNGLE_BASEMENTS_2(UrnType.PATINA, ColorHelper.mixColor(EnumDyeColor.ORANGE.colorValue), ColorHelper.mixColor(EnumDyeColor.ORANGE.colorValue, EnumDyeColor.RED.colorValue), "basement", "jungle", LootTables.URN_BASEMENTS_JUNGLE_LOOT),
		JUNGLE_BASEMENTS_3(UrnType.PATINA, -1, ColorHelper.mixColor(EnumDyeColor.ORANGE.colorValue, EnumDyeColor.CYAN.colorValue, EnumDyeColor.CYAN.colorValue), "basement", "jungle", LootTables.URN_BASEMENTS_JUNGLE_LOOT),
		JUNGLE_WELLS_1(UrnType.PATINA, ColorHelper.mixColor(EnumDyeColor.YELLOW.colorValue, EnumDyeColor.ORANGE.colorValue, EnumDyeColor.SILVER.colorValue), ColorHelper.mixColor(EnumDyeColor.GREEN.colorValue, EnumDyeColor.LIME.colorValue), "well", "jungle", LootTables.URN_WELLS_LOOT),
		JUNGLE_WELLS_2(UrnType.PATINA, ColorHelper.mixColor(EnumDyeColor.RED.colorValue, EnumDyeColor.ORANGE.colorValue, EnumDyeColor.SILVER.colorValue, EnumDyeColor.SILVER.colorValue, EnumDyeColor.SILVER.colorValue), ColorHelper.mixColor(EnumDyeColor.GREEN.colorValue, EnumDyeColor.GREEN.colorValue, EnumDyeColor.SILVER.colorValue), "well", "jungle", LootTables.URN_WELLS_LOOT),
		JUNGLE_TOMB_1(UrnType.CLOTH, jungleColor_1, jungleColor_2, "burial", "jungle", JUNGLE_TOMB_LOOT),
		JUNGLE_TOMB_2(UrnType.STRIPE, jungleColor_1, jungleColor_2, "burial", "jungle", JUNGLE_TOMB_LOOT),
		JUNGLE_TOMB_3(UrnType.ZONE, jungleColor_1, jungleColor_2, "burial", "jungle", JUNGLE_TOMB_LOOT),
		JUNGLE_TOMB_4(UrnType.NORMAL, jungleColor_2, jungleColor_1, "burial", "jungle", JUNGLE_TOMB_LOOT),
		SNOW_BASEMENTS_1(UrnType.NORMAL, ColorHelper.mixColor(EnumDyeColor.YELLOW.colorValue, EnumDyeColor.ORANGE.colorValue, EnumDyeColor.SILVER.colorValue), -1, "basement", "taiga", LootTables.URN_BASEMENTS_COLD_LOOT),
		SNOW_BASEMENTS_2(UrnType.NORMAL, ColorHelper.mixColor(EnumDyeColor.BLUE.colorValue, EnumDyeColor.RED.colorValue, EnumDyeColor.LIME.colorValue, EnumDyeColor.WHITE.colorValue), -1, "basement", "taiga", LootTables.URN_BASEMENTS_COLD_LOOT),
		SNOW_BASEMENTS_3(UrnType.NORMAL, ColorHelper.mixColor(EnumDyeColor.BROWN.colorValue, EnumDyeColor.ORANGE.colorValue, EnumDyeColor.WHITE.colorValue, EnumDyeColor.LIGHT_BLUE.colorValue), -1, "basement", "taiga", LootTables.URN_BASEMENTS_COLD_LOOT),
		SNOW_BASEMENTS_4(UrnType.NORMAL, ColorHelper.mixColor(EnumDyeColor.GRAY.colorValue, EnumDyeColor.ORANGE.colorValue, EnumDyeColor.WHITE.colorValue), -1, "basement", "taiga", LootTables.URN_BASEMENTS_COLD_LOOT),
		SNOW_BASEMENTS_5(UrnType.NORMAL, ColorHelper.mixColor(EnumDyeColor.YELLOW.colorValue, EnumDyeColor.BROWN.colorValue, EnumDyeColor.CYAN.colorValue, EnumDyeColor.WHITE.colorValue), -1, "basement", "taiga", LootTables.URN_BASEMENTS_COLD_LOOT),
		SNOW_WELLS_1(UrnType.NORMAL, ColorHelper.mixColor(EnumDyeColor.BROWN.colorValue, EnumDyeColor.LIGHT_BLUE.colorValue, EnumDyeColor.SILVER.colorValue, EnumDyeColor.SILVER.colorValue), -1, "well", "taiga", LootTables.URN_WELLS_LOOT),
		SNOW_WELLS_2(UrnType.STARS, ColorHelper.mixColor(EnumDyeColor.BROWN.colorValue, EnumDyeColor.LIGHT_BLUE.colorValue, EnumDyeColor.SILVER.colorValue, EnumDyeColor.SILVER.colorValue), ColorHelper.mixColor(EnumDyeColor.BROWN.colorValue, EnumDyeColor.BROWN.colorValue, EnumDyeColor.LIGHT_BLUE.colorValue, EnumDyeColor.SILVER.colorValue), "well", "taiga", LootTables.URN_WELLS_LOOT),
		SNOW_WELLS_3(UrnType.STARS, ColorHelper.mixColor(EnumDyeColor.BROWN.colorValue, EnumDyeColor.LIGHT_BLUE.colorValue, EnumDyeColor.SILVER.colorValue, EnumDyeColor.SILVER.colorValue), ColorHelper.mixColor(EnumDyeColor.BROWN.colorValue, EnumDyeColor.BROWN.colorValue, EnumDyeColor.BROWN.colorValue, EnumDyeColor.LIGHT_BLUE.colorValue, EnumDyeColor.SILVER.colorValue), "well", "taiga", LootTables.URN_WELLS_LOOT),
		SNOW_TOMB_1(UrnType.NORMAL, snowColor_1, -1, "burial", "taiga", SNOW_TOMB_LOOT),
		SNOW_TOMB_2(UrnType.LINE, snowColor_1, snowColor_2, "burial", "taiga", SNOW_TOMB_LOOT),
		SNOW_TOMB_3(UrnType.LINE, snowColor_1, snowColor_3, "burial", "taiga", SNOW_TOMB_LOOT),
		SNOW_TOMB_4(UrnType.ZONE, snowColor_1, snowColor_2, "burial", "taiga", SNOW_TOMB_LOOT),
		SNOW_TOMB_5(UrnType.ZONE, snowColor_1, snowColor_3, "burial", "taiga", SNOW_TOMB_LOOT),
		FOREST_BASEMENTS_1(UrnType.ZONE, ColorHelper.mixColor(EnumDyeColor.WHITE.colorValue), ColorHelper.mixColor(EnumDyeColor.RED.colorValue, EnumDyeColor.ORANGE.colorValue, EnumDyeColor.WHITE.colorValue), "basement", "forest", LootTables.URN_BASEMENTS_FOREST_LOOT),
		FOREST_BASEMENTS_2(UrnType.CLOTH, ColorHelper.mixColor(EnumDyeColor.WHITE.colorValue, EnumDyeColor.SILVER.colorValue), ColorHelper.mixColor(EnumDyeColor.BROWN.colorValue, EnumDyeColor.ORANGE.colorValue, EnumDyeColor.LIME.colorValue), "basement", "forest", LootTables.URN_BASEMENTS_FOREST_LOOT),
		FOREST_BASEMENTS_3(UrnType.STRIPE, ColorHelper.mixColor(EnumDyeColor.WHITE.colorValue, EnumDyeColor.SILVER.colorValue, EnumDyeColor.BROWN.colorValue), ColorHelper.mixColor(EnumDyeColor.WHITE.colorValue, EnumDyeColor.BROWN.colorValue), "basement", "forest", LootTables.URN_BASEMENTS_FOREST_LOOT),
		FOREST_WELLS_1(UrnType.PATINA, ColorHelper.mixColor(EnumDyeColor.YELLOW.colorValue, EnumDyeColor.ORANGE.colorValue, EnumDyeColor.GREEN.colorValue), ColorHelper.mixColor(EnumDyeColor.BROWN.colorValue, EnumDyeColor.BROWN.colorValue, EnumDyeColor.BROWN.colorValue, EnumDyeColor.WHITE.colorValue), "well", "forest", LootTables.URN_WELLS_LOOT),
		FOREST_WELLS_2(UrnType.PATINA, ColorHelper.mixColor(EnumDyeColor.BROWN.colorValue, EnumDyeColor.WHITE.colorValue), ColorHelper.mixColor(EnumDyeColor.RED.colorValue, EnumDyeColor.SILVER.colorValue, EnumDyeColor.SILVER.colorValue, EnumDyeColor.SILVER.colorValue), "well", "forest", LootTables.URN_WELLS_LOOT),
		FOREST_TOMB_1(UrnType.NORMAL, forestColor_1, -1, "burial", "forest", FOREST_TOMB_LOOT),
		FOREST_TOMB_2(UrnType.PATINA, forestColor_1, forestColor_2, "burial", "forest", FOREST_TOMB_LOOT),
		FOREST_TOMB_3(UrnType.STRIPE, forestColor_1, forestColor_2, "burial", "forest", FOREST_TOMB_LOOT),
		FOREST_TOMB_4(UrnType.ZONE, forestColor_1, forestColor_2, "burial", "forest", FOREST_TOMB_LOOT);

		private final UrnType type;
		private final int tintColor;
		private final int patina;
		private final String tooltip;
		private final String location;
		public final ResourceLocation[] loot;

		private final static UrnLootType[] forest_basements = new UrnLootType[] { FOREST_BASEMENTS_1, FOREST_BASEMENTS_2, FOREST_BASEMENTS_3 };
		private final static UrnLootType[] forest_wells = new UrnLootType[] { FOREST_WELLS_1, FOREST_WELLS_2 };
		private final static UrnLootType[] forest_tombs = new UrnLootType[] { FOREST_TOMB_1, FOREST_TOMB_2, FOREST_TOMB_3, FOREST_TOMB_4 };
		private final static UrnLootType[] forest_cliffs = new UrnLootType[] { FOREST_TOMB_1, FOREST_TOMB_2, FOREST_TOMB_3, FOREST_TOMB_4 };
		private final static UrnLootType[] swamp_basements = new UrnLootType[] { SWAMP_BASEMENTS_1, SWAMP_BASEMENTS_2, SWAMP_BASEMENTS_3 };
		private final static UrnLootType[] swamp_wells = new UrnLootType[] { SWAMP_WELLS_1, SWAMP_WELLS_2 };
		private final static UrnLootType[] swamp_tombs = new UrnLootType[] { SWAMP_TOMB_1, SWAMP_TOMB_2, SWAMP_TOMB_3, SWAMP_TOMB_4, SWAMP_TOMB_5 };
		private final static UrnLootType[] swamp_cliffs = new UrnLootType[] { SWAMP_TOMB_1, SWAMP_TOMB_2, SWAMP_TOMB_3, SWAMP_TOMB_4, SWAMP_TOMB_5 };
		private final static UrnLootType[] cold_basements = new UrnLootType[] { SNOW_BASEMENTS_1, SNOW_BASEMENTS_2, SNOW_BASEMENTS_3, SNOW_BASEMENTS_4, SNOW_BASEMENTS_5 };
		private final static UrnLootType[] cold_wells = new UrnLootType[] { SNOW_WELLS_1, SNOW_WELLS_2 };
		private final static UrnLootType[] cold_tombs = new UrnLootType[] { SNOW_TOMB_1, SNOW_TOMB_2, SNOW_TOMB_3, SNOW_TOMB_4, SNOW_TOMB_5 };
		private final static UrnLootType[] cold_cliffs = new UrnLootType[] { SNOW_TOMB_1, SNOW_TOMB_2, SNOW_TOMB_3, SNOW_TOMB_4, SNOW_TOMB_5 };
		private final static UrnLootType[] desert_altars = new UrnLootType[] { DESERT_ALTAR_1 };
		private final static UrnLootType[] desert_basements = new UrnLootType[] { DESERT_BASEMENTS_1, DESERT_BASEMENTS_2, DESERT_BASEMENTS_3 };
		private final static UrnLootType[] desert_wells = new UrnLootType[] { DESERT_WELLS_1, DESERT_WELLS_2 };
		private final static UrnLootType[] desert_tombs = new UrnLootType[] { DESERT_TOMB_1, DESERT_TOMB_2, DESERT_TOMB_3, DESERT_TOMB_4, DESERT_TOMB_5 };
		private final static UrnLootType[] desert_cliffs = new UrnLootType[] { DESERT_TOMB_1, DESERT_TOMB_2, DESERT_TOMB_3, DESERT_TOMB_4, DESERT_TOMB_5 };
		private final static UrnLootType[] jungle_basements = new UrnLootType[] { JUNGLE_BASEMENTS_1, JUNGLE_BASEMENTS_2, JUNGLE_BASEMENTS_3 };
		private final static UrnLootType[] jungle_wells = new UrnLootType[] { JUNGLE_WELLS_1, JUNGLE_WELLS_2 };
		private final static UrnLootType[] jungle_tombs = new UrnLootType[] { JUNGLE_TOMB_1, JUNGLE_TOMB_2, JUNGLE_TOMB_3, JUNGLE_TOMB_4 };
		private final static UrnLootType[] jungle_cliffs = new UrnLootType[] { JUNGLE_TOMB_1, JUNGLE_TOMB_2, JUNGLE_TOMB_3, JUNGLE_TOMB_4 };

		UrnLootType(UrnType type, int tintColor, int patina, String tooltip, String location, ResourceLocation... loot) {
			this.type = type;
			this.tintColor = tintColor;
			this.patina = patina;
			this.tooltip = tooltip;
			this.location = location;
			this.loot = loot;
		}

		public void initializeType(TileEntity tile, Random rand) {
			initializeType(tile, 1.0F, -1, rand);
		}

		public void initializeType(TileEntity tile, float chance, Random rand) {
			initializeType(tile, chance, -1, rand);
		}

		public void initializeType(TileEntity tile, float chance, int bug, Random rand) {
			if (tile instanceof TileEntityUrn) {
				TileEntityUrn urn = (TileEntityUrn) tile;
				if (rand.nextFloat() < ModConfig.generation.rareUrnGenerationChance * chance) {
					urn.urnType = UrnType.RARE_LOOKUP.get(rand.nextInt(UrnType.RARE_LOOKUP.size()));
					urn.tintColor = -1;
					urn.patina = -1;
				} else {
					urn.urnType = this.type;
					urn.tintColor = this.tintColor;
					urn.patina = this.patina;
				}
				urn.tooltip = this.tooltip;
				urn.location = this.location;
				urn.bug = bug > 0 || (bug == 0 && rand.nextInt(8) == 0);
				urn.setLootTable(this.loot[rand.nextInt(this.loot.length)], rand.nextLong());
				urn.updateStatus();
			}
		}

		public static void initializeType(TileEntity tile, EnumBiomeType biomeType, UrnLocation loc, Random rand) {
			switch(biomeType) {
				case Forest: {
					switch(loc) {
						case ALTARS: break;
						case BASEMENTS: { if (forest_basements.length > 0) forest_basements[rand.nextInt(forest_basements.length)].initializeType(tile, rand); break; }
						case WELLS: { if (forest_wells.length > 0) forest_wells[rand.nextInt(forest_wells.length)].initializeType(tile, rand); break; }
						case TOMBS: { if (forest_tombs.length > 0) forest_tombs[rand.nextInt(forest_tombs.length)].initializeType(tile, 1.0F, 0, rand); break; }
						case CLIFF: { if (forest_cliffs.length > 0) forest_cliffs[rand.nextInt(forest_cliffs.length)].initializeType(tile, 1.0F, 0, rand); break; }
					}
					break;
				} case Swamp: {
					switch(loc) {
						case ALTARS: break;
						case BASEMENTS: { if (swamp_basements.length > 0) swamp_basements[rand.nextInt(swamp_basements.length)].initializeType(tile, rand); break; }
						case WELLS: { if (swamp_wells.length > 0) swamp_wells[rand.nextInt(swamp_wells.length)].initializeType(tile, rand); break; }
						case TOMBS: { if (swamp_tombs.length > 0) swamp_tombs[rand.nextInt(swamp_tombs.length)].initializeType(tile, 0.7F, 0, rand); break; }
						case CLIFF: { if (swamp_cliffs.length > 0) swamp_cliffs[rand.nextInt(swamp_cliffs.length)].initializeType(tile, 1.0F, 0, rand); break; }
					}
					break;
				} case Cold: {
					switch(loc) {
						case ALTARS: break;
						case BASEMENTS: { if (cold_basements.length > 0) cold_basements[rand.nextInt(cold_basements.length)].initializeType(tile, rand); break; }
						case WELLS: { if (cold_wells.length > 0) cold_wells[rand.nextInt(cold_wells.length)].initializeType(tile, rand); break; }
						case TOMBS: { if (cold_tombs.length > 0) cold_tombs[rand.nextInt(cold_tombs.length)].initializeType(tile, 1.0F, 0, rand); break; }
						case CLIFF: { if (cold_cliffs.length > 0) cold_cliffs[rand.nextInt(cold_cliffs.length)].initializeType(tile, 1.0F, 0, rand); break; }
					}
					break;
				} case Desert: {
					switch(loc) {
						case ALTARS: { if (desert_altars.length > 0) desert_altars[rand.nextInt(desert_altars.length)].initializeType(tile, rand); break; }
						case BASEMENTS: { if (desert_basements.length > 0) desert_basements[rand.nextInt(desert_basements.length)].initializeType(tile, rand); break; }
						case WELLS: { if (desert_wells.length > 0) desert_wells[rand.nextInt(desert_wells.length)].initializeType(tile, rand); break; }
						case TOMBS: { if (desert_tombs.length > 0) desert_tombs[rand.nextInt(desert_tombs.length)].initializeType(tile, 0.2F, 1, rand); break; }
						case CLIFF: { if (desert_cliffs.length > 0) desert_cliffs[rand.nextInt(desert_cliffs.length)].initializeType(tile, 1.0F, 1, rand); break; }
					}
					break;
				} case Jungle: {
					switch(loc) {
						case ALTARS: break;
						case BASEMENTS: { if (jungle_basements.length > 0) jungle_basements[rand.nextInt(jungle_basements.length)].initializeType(tile, rand); break; }
						case WELLS: { if (jungle_wells.length > 0) jungle_wells[rand.nextInt(jungle_wells.length)].initializeType(tile, rand); break; }
						case TOMBS: { if (jungle_tombs.length > 0) jungle_tombs[rand.nextInt(jungle_tombs.length)].initializeType(tile, 0.7F, 0, rand); break; }
						case CLIFF: { if (jungle_cliffs.length > 0) jungle_cliffs[rand.nextInt(jungle_cliffs.length)].initializeType(tile, 1.0F, 1, rand); break; }
					}
					break;
				} case Border: {
					break;
				} case Down: {
					break;
				}
			}
		}
	}

	public static enum UrnLocation {
		ALTARS,
		BASEMENTS,
		WELLS,
		TOMBS,
		CLIFF
	}
}
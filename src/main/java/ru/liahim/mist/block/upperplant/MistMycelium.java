package ru.liahim.mist.block.upperplant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;
import javax.vecmath.Vector2f;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.block.IWettable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.MistBlockWettable;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.init.ModConfig;
import ru.liahim.mist.tileentity.TileEntityMycelium;
import ru.liahim.mist.util.SoilHelper;
import ru.liahim.mist.world.MistWorld;

public class MistMycelium extends MistBlockWettable implements ITileEntityProvider {

	public static final PropertyEnum<SoilType> SOIL = PropertyEnum.<SoilType>create("soil", SoilType.class);

	public MistMycelium() {
		super(Material.GROUND, 2);
		this.setSoundType(SoundType.GROUND);
		this.setDefaultState(this.blockState.getBaseState().withProperty(WET, false));
		this.setHardness(0.5F);
		this.setHarvestLevel("shovel", 0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected boolean showPorosityTooltip() { return false; }

	@Override
	public boolean doIfWet(World world, BlockPos pos, IBlockState state, Vector2f waterType, boolean fog, Random rand) {
		if (!fog) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityMycelium) {
				placeMushroom(world, pos, state, (TileEntityMycelium) te, rand, 1);
			}
		}
		return false;
	}

	@Override
	public boolean doIfDry(World world, BlockPos pos, IBlockState state, boolean lava, Random rand) {
		if (!lava) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityMycelium) {
				if (((TileEntityMycelium)te).getMushroomState() == MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.SAND) ||
						((TileEntityMycelium)te).getMushroomState() == MistBlocks.MUSHROOMS_1.getDefaultState().withProperty(MistMushroom.TYPE_1, MistMushroom.MushroomType_1.TAN)) {
					placeMushroom(world, pos, state, (TileEntityMycelium) te, rand, 1);
				}
			}
		}
		return false;
	}

	protected void placeMushroom(World world, BlockPos pos, IBlockState state, TileEntityMycelium te, Random rand, int count) {
		if (pos.getY() > MistWorld.getFogMaxHight() && te.getMushroomState() != null &&
				MistWorld.getHumi(world, pos, 0) > getNeededHumi(te.getMushroomState())) {
			EnumFacing face;
			BlockPos checkPos;
			IBlockState checkState;
			lab1:
			for (int i = 0; i < count; ++i) {
				face = EnumFacing.HORIZONTALS[rand.nextInt(4)];
				checkPos = pos.offset(face);
				checkState = world.getBlockState(checkPos);
				double distance = 1;
				boolean reverse = false;
				int fuse = 0;
				int maxSize = te.getMaxSize();
				int radius = rand.nextInt(maxSize + 1);
				int faceSize = te.getFaceSize(face);
				int r = 0;
				for (; r < radius; ++r) {
					if (reverse || (checkState.getBlock() instanceof IWettable && !((IWettable)checkState.getBlock()).isAcid() &&
							!(checkState.getBlock() instanceof MistMycelium))) {
						if (reverse) {
							++fuse;
							if (fuse > 3) {
								te.setFaceSize(face, Math.min(r, faceSize));
								break;
							}
							reverse = false;
						} else fuse = 0;
						int j = rand.nextInt(3);
						if (j == 0) face = face.rotateYCCW();
						else if (j == 2) face = face.rotateY();
						if (j != 1 && distance > pos.distanceSq(checkPos.offset(face))) {
							face = rand.nextBoolean() ? face.getOpposite() : j == 0 ? face.rotateY() : face.rotateYCCW();
						}
						checkPos = checkPos.offset(face);
						checkState = world.getBlockState(checkPos);
						distance = pos.distanceSq(checkPos);
					} else {
						boolean air = checkState.getMaterial().isReplaceable();
						checkPos = checkPos.offset(face.getOpposite());
						/** Up */
						checkState = world.getBlockState(checkPos.up());
						if (checkState.getBlock() instanceof IWettable && !((IWettable)checkState.getBlock()).isAcid() &&
								!(checkState.getBlock() instanceof MistMycelium)) {
							checkPos = checkPos.up();
						} /** Down */ else if (air && checkPos.down().getY() > MistWorld.getFogMaxHight()) {
							checkState = world.getBlockState(checkPos.down());
							if (checkState.getBlock() instanceof IWettable && !((IWettable)checkState.getBlock()).isAcid() &&
									!(checkState.getBlock() instanceof MistMycelium)) {
								checkPos = checkPos.down();
							}
						} else {
							checkState = world.getBlockState(checkPos);
							reverse = true;
							--r;
						}
						distance = pos.distanceSq(checkPos);
					}
				}
				if (r >= faceSize * 0.75) te.setFaceSize(face, Math.min(faceSize == maxSize || r <= faceSize ? (faceSize + 1) : r, 32));
				boolean place = checkState.getBlock() == this;
				if (checkState.getBlock() instanceof IWettable && !((IWettable)checkState.getBlock()).isAcid()) {
					checkPos = checkPos.up();
				} else checkPos = checkPos.offset(face.getOpposite());
				checkState = world.getBlockState(checkPos);
				while (checkState.getBlock() instanceof IWettable && !((IWettable)checkState.getBlock()).isAcid() &&
						!(checkState.getBlock() instanceof MistMycelium) && checkState.getBlock() != MistBlocks.CLAY) {
					place = false;
					checkPos = checkPos.up();
					checkState = world.getBlockState(checkPos);
				}
				boolean chance = rand.nextInt(32) < maxSize;
				int hum = SoilHelper.getHumus(world.getBlockState(checkPos.down()));
				if (checkState == Blocks.AIR.getDefaultState() && (!world.canSeeSky(checkPos) ||
						(world.getBlockState(checkPos.down()).getBlock() == this && rand.nextInt(4) == 0)) &&
						(chance || hum > 0) &&
						MistWorld.getHumi(world, checkPos, 0) > getNeededHumi(te.getMushroomState())) {
					world.setBlockState(checkPos, te.getMushroomState());
					if (!chance) SoilHelper.setSoil(world, checkPos.down(), world.getBlockState(checkPos.down()), hum - 1, 2);
					te.setDeadTime(0);
					if (count > 1 && rand.nextBoolean()) break;
				} else if (maxSize >= 16 || maxSize > rand.nextInt(16)) {
					te.setDeadTime(te.getDeadTime() + 1);
					if (te.getDeadTime() == 64) {
						SoilHelper.setSoil(world, pos, ((Block)state.getValue(SOIL).getSoil()).getDefaultState(), 1, state.getValue(WET), 2);
						if (maxSize > 16) {
							boolean gold = ((MistMushroom)te.getMushroomState().getBlock()).getTypeProperty() == MistMushroom.TYPE_1 && te.getMushroomState().getValue(MistMushroom.TYPE_1) == MistMushroom.MushroomType_1.GOLD;
							for (int j = 0; j < maxSize - 16; ++j) {
								checkPos = pos.add(rand.nextInt(64) - 32, 0, rand.nextInt(64) - 32);
								if (generateMycelium(world, checkPos, te.getMushroomState(), rand, gold ? 130 : MistWorld.getFogMaxHight(), false)) {
									break lab1;
								}
							}
						}
						break lab1;
					}
				}
			}
		}
	}

	private int getNeededHumi(IBlockState state) {
		PropertyEnum prop = ((MistMushroom)state.getBlock()).getTypeProperty();
		if (prop == MistMushroom.TYPE_0) {
			switch (state.getValue(MistMushroom.TYPE_0)) {
				case BROWN :
				case RED :
				case ORANGE :
				case PINK : return 77;
				case SAND : return 70;
				default: return 82;
			}
		} else if (prop == MistMushroom.TYPE_1) {
			switch (state.getValue(MistMushroom.TYPE_1)) {
				case BEIGE :
				case COPPER : return 75;
				case TAN : return 70;
				default: return 80;
			}
		}
		return 200;
	}

	protected ItemStack getSilkTouchDrop(IBlockState state, @Nullable TileEntity te) {
		Item item = Item.getItemFromBlock(this);
		ItemStack stack = new ItemStack(item, 1, this.getMetaFromState(state));
		if (te != null && te instanceof TileEntityMycelium) {
			NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
			tag.setTag("BlockEntityTag", ((TileEntityMycelium)te).getMushroomNBT(new NBTTagCompound()));
			stack.setTagCompound(tag);
		}
		return stack;
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
		player.addStat(StatList.getBlockStats(this));
		player.addExhaustion(0.005F);
		if (ModConfig.dimension.myceliumHarvesting || (this.canSilkHarvest(world, pos, state, player) && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0)) {
			List<ItemStack> items = new ArrayList<ItemStack>();
			ItemStack itemstack = this.getSilkTouchDrop(state, te);
			if (!itemstack.isEmpty()) items.add(itemstack);
			ForgeEventFactory.fireBlockHarvesting(items, world, pos, state, 0, 1.0f, true, player);
			for (ItemStack item : items) spawnAsEntity(world, pos, item);
		} else {
			harvesters.set(player);
			int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
			this.dropBlockAsItem(world, pos, state, i);
			harvesters.set(null);
		}
	}

	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		return true;
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock((Block)state.getValue(SOIL).getSoil());
	}

	@Override
	public int damageDropped(IBlockState state) {
		return ((Block)state.getValue(SOIL).getSoil()).getMetaFromState(((Block)state.getValue(SOIL).getSoil()).getDefaultState().withProperty(WET, state.getValue(WET)));
	}

	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.DESTROY;
	}

	@Override
	public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
		return ((Block)state.getValue(SOIL).getSoil()).getMapColor(state, world, pos);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityMycelium();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return (state.getValue(SOIL).getMetadata() << 1) | (state.getValue(WET) ? 0 : 1);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(SOIL, SoilType.byMetadata(meta >> 1)).withProperty(WET, (meta & 1) == 0);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { SOIL, WET });
	}

	@Override
	public int getWaterPerm(IBlockState state) {
		return state.getValue(SOIL).getSoil().getWaterPerm(((Block)state.getValue(SOIL).getSoil()).getDefaultState());
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return getSilkTouchDrop(state, world.getTileEntity(pos));
	}

	public static boolean generateMycelium(World world, BlockPos chunkPos, IBlockState mushroom, Random rand) {
		return generateMycelium(world, chunkPos, mushroom, rand, MistWorld.getFogMaxHight(), true);
	}

	public static boolean generateMycelium(World world, BlockPos chunkPos, IBlockState mushroom, Random rand, int hight) {
		return generateMycelium(world, chunkPos, mushroom, rand, hight, true);
	}

	public static boolean generateMycelium(World world, BlockPos chunkPos, IBlockState mushroom, Random rand, int hight, boolean isNature) {
		BlockPos pos = isNature ? world.getHeight(chunkPos.add(rand.nextInt(16) + 8, 0, rand.nextInt(16) + 8)) : chunkPos;
		while (pos.getY() > hight && !world.isSideSolid(pos, EnumFacing.UP)) {
			pos = pos.down();
		}
		if (world.getBlockState(pos.up()).getMaterial().isLiquid()) return false;
		pos = pos.down();
		if (pos.getY() > hight) {
			IBlockState state = world.getBlockState(pos);
			if (state.getBlock() instanceof IWettable && !((IWettable)state.getBlock()).isAcid() && !(state.getBlock() instanceof MistMycelium)) {
				MistMycelium.SoilType type = MistMycelium.SoilType.bySoil((IWettable)state.getBlock());
				if (type != null) {
					world.setBlockState(pos, MistBlocks.MYCELIUM.getDefaultState().withProperty(MistMycelium.SOIL, type).withProperty(IWettable.WET, state.getValue(IWettable.WET)), isNature ? Mist.FLAG : 3);
					((TileEntityMycelium) world.getTileEntity(pos)).setMushroomState(mushroom, isNature);
					if (isNature) {
						chunkPos = pos;
						for (int i = 0; i < 8 + rand.nextInt(8); ++i) {
							pos = world.getHeight(chunkPos.add(rand.nextInt(64) - 32, 0, rand.nextInt(64) - 32));
							while (pos.getY() > hight && !world.isSideSolid(pos, EnumFacing.UP)) {
								pos = pos.down();
							}
							if (pos.getY() > hight) {
								state = world.getBlockState(pos);
								pos = pos.up();
								if (state.getBlock() instanceof IWettable &&
										!((IWettable)state.getBlock()).isAcid() &&
										state.getBlock() != MistBlocks.CLAY &&
										!world.getBlockState(pos).getMaterial().isLiquid() &&
										world.getBlockState(pos).getMaterial().isReplaceable() &&
										!world.canSeeSky(pos)) {
									world.setBlockState(pos, mushroom, Mist.FLAG);
								}
							}
						}
					}
					return true;
				}
			}
		}
		return false;
	}

	public enum SoilType implements IStringSerializable {

		FOREST(0, "f", MistBlocks.DIRT_F),
		CLAY(1, "c", MistBlocks.DIRT_C),
		SAND(2, "s", MistBlocks.DIRT_S),
		ROCK(3, "r", MistBlocks.DIRT_R),
		TROPIC(4, "t", MistBlocks.DIRT_T);

		private static final SoilType[] META_LOOKUP = new SoilType[values().length];
		private static final HashMap<IWettable,SoilType> SOIL_LOOKUP = new HashMap<IWettable,SoilType>();
		private final int meta;
		private final String name;
		private final IWettable block;

		private SoilType(int meta, String name, IWettable block) {
			this.meta = meta;
			this.name = name;
			this.block = block;
		}

		public int getMetadata() {
			return this.meta;
		}

		public IWettable getSoil() {
			return this.block;
		}

		public static SoilType byMetadata(int meta) {
			if (meta < 0 || meta >= META_LOOKUP.length) meta = 0;
			return META_LOOKUP[meta];
		}

		public static SoilType bySoil(IWettable soil) {
			return SOIL_LOOKUP.get(soil);
		}

		@Override
		public String getName() {
			return this.name;
		}

		static {
			for (SoilType type : values()) {
				META_LOOKUP[type.getMetadata()] = type;
			}
		}

		static {
			for (SoilType type : values()) {
				SOIL_LOOKUP.put(type.getSoil(), type);
			}
		}
	}
}
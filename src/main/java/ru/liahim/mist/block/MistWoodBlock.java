package ru.liahim.mist.block;

import java.util.Random;

import javax.annotation.Nullable;

import ru.liahim.mist.api.block.IDividable;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.handlers.ServerEventHandler;
import ru.liahim.mist.init.ModAdvancements;
import ru.liahim.mist.item.ItemMistChisel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**@author Liahim*/
public class MistWoodBlock extends MistBlock implements IDividable {

	public static final PropertyEnum<EnumAxis> AXIS = PropertyEnum.<EnumAxis>create("axis", EnumAxis.class);
	public static final PropertyEnum<EnumType> TYPE = PropertyEnum.<EnumType>create("type", EnumType.class);
	private final int flammability;
	private final int fireSpeed;
	private Block stepBlock;
	private Block slabBlock;
	private Block wallBlock;
	private Block stairsBlock;

	public MistWoodBlock(float hardness, float resistance, int flammability, int fireSpeed, MapColor color) {
		super(Material.WOOD, color);
		this.setHardness(hardness);
		if (resistance > 0) this.setResistance(resistance);
		this.setSoundType(SoundType.WOOD);
		this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, EnumType.LOG).withProperty(AXIS, EnumAxis.Y));
		this.flammability = flammability;
		this.fireSpeed = fireSpeed;
	}

	public MistWoodBlock(float hardness, float resistance, int flammability, int fireSpeed) {
		this(hardness, resistance, flammability, fireSpeed, Material.WOOD.getMaterialMapColor());
	}

	public MistWoodBlock(float hardness, int flammability, int fireSpeed, MapColor color) {
		this(hardness, -1, flammability, fireSpeed, color);
	}

	public MistWoodBlock(float hardness, int flammability, int fireSpeed) {
		this(hardness, -1, flammability, fireSpeed, Material.WOOD.getMaterialMapColor());
	}

	public MistWoodBlock(float hardness, MapColor color) {
		this(hardness, -1, 20, 5, color);
	}

	public MistWoodBlock(float hardness) {
		this(hardness, -1, 20, 5, Material.WOOD.getMaterialMapColor());
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer) {
		IBlockState state = this.getStateFromMeta(meta);
		if (state.getValue(TYPE) == EnumType.PLANK) {
			switch (facing.getAxis()) {
			case Z:
				return state.withProperty(AXIS, EnumAxis.Z);
			case X:
				return state.withProperty(AXIS, EnumAxis.X);
			case Y:
				if (placer.getHorizontalFacing().getAxis() == EnumFacing.Axis.X)
					return state.withProperty(AXIS, EnumAxis.NONE);
				else return state.withProperty(AXIS, EnumAxis.Y);
			}
		} else if (state.getValue(AXIS) != EnumAxis.NONE) {
			switch (facing.getAxis()) {
			case Z:
				return state.withProperty(AXIS, EnumAxis.Z);
			case X:
				return state.withProperty(AXIS, EnumAxis.X);
			case Y:
				return state.withProperty(AXIS, EnumAxis.Y);
			}
		}
		return state;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		EnumType type = state.getValue(TYPE);
		ItemStack heldItem = player.getHeldItem(hand);
		boolean axe = heldItem.getItem() instanceof ItemAxe;
		boolean chisel = heldItem.getItem() instanceof ItemMistChisel;
		if (type != EnumType.PLANK && type != EnumType.DEBARKING && heldItem != null &&	(axe || (chisel && type != EnumType.CHISELED))) {
			EnumAxis axis = state.getValue(AXIS);
			if (axis == EnumAxis.NONE || EnumAxis.fromFacingAxis(side.getAxis()) != axis) {
				if (!world.isRemote) {
					if (!ServerEventHandler.isMulchDelay(player.getUniqueID())) {
						if (player instanceof EntityPlayerMP) ModAdvancements.CARVING.trigger((EntityPlayerMP) player, world, pos, true);
						if (axe) {
							if (type == EnumType.LOG) {
								if (axis == EnumAxis.NONE) world.setBlockState(pos, state.withProperty(AXIS, EnumAxis.Y));
								else world.setBlockState(pos, state.withProperty(TYPE, EnumType.CHISELED));
							} else if (type == EnumType.CHISELED) {
								world.setBlockState(pos, state.withProperty(TYPE, EnumType.DEBARKING));
							}
						} else if (type == EnumType.LOG && axis != EnumAxis.NONE) {
							world.setBlockState(pos, state.withProperty(TYPE, EnumType.CHISELED));
						} 
						ServerEventHandler.setMulchDelay(player.getUniqueID(), 5);
						heldItem.damageItem(1, player);
						ItemStack stack = new ItemStack(MistItems.MULCH);
						pos = pos.offset(side);
						EntityItem entity_item = new EntityItem(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack);
						entity_item.setDefaultPickupDelay();
						world.spawnEntity(entity_item);
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public int quantityDropped(IBlockState state, int fortune, Random random) {
		return state.getValue(TYPE) == EnumType.PLANK ? 4 : 1;
	}

	@Override
	@Nullable
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return state.getValue(TYPE) == EnumType.PLANK ? Item.getItemFromBlock(this.getStepBlock(state)) : super.getItemDropped(state, rand, fortune);
	}

	@Override
	public int damageDropped(IBlockState state) {
		if (state.getValue(TYPE) == EnumType.PLANK) return 0;
		else if (state.getValue(AXIS) == EnumAxis.NONE) return this.getMetaFromState(state);
		return this.getMetaFromState(state.withProperty(AXIS, EnumAxis.Y));
	}

	@Override
	protected ItemStack getSilkTouchDrop(IBlockState state) {
		if (state.getValue(TYPE) == EnumType.PLANK) return new ItemStack(Item.getItemFromBlock(this), 1, this.getMetaFromState(state.withProperty(AXIS, EnumAxis.X)));
		else if (state.getValue(AXIS) == EnumAxis.Y || state.getValue(AXIS) == EnumAxis.NONE) return super.getSilkTouchDrop(state);
		else return new ItemStack(Item.getItemFromBlock(this), 1, this.getMetaFromState(state.withProperty(AXIS, EnumAxis.Y)));
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		list.add(new ItemStack(this, 1, this.getMetaFromState(this.getDefaultState().withProperty(TYPE, EnumType.LOG).withProperty(AXIS, EnumAxis.Y))));
		list.add(new ItemStack(this, 1, this.getMetaFromState(this.getDefaultState().withProperty(TYPE, EnumType.CHISELED).withProperty(AXIS, EnumAxis.Y))));
		list.add(new ItemStack(this, 1, this.getMetaFromState(this.getDefaultState().withProperty(TYPE, EnumType.DEBARKING).withProperty(AXIS, EnumAxis.Y))));
		list.add(new ItemStack(this, 1, this.getMetaFromState(this.getDefaultState().withProperty(TYPE, EnumType.CHISELED).withProperty(AXIS, EnumAxis.NONE))));
		list.add(new ItemStack(this, 1, this.getMetaFromState(this.getDefaultState().withProperty(TYPE, EnumType.DEBARKING).withProperty(AXIS, EnumAxis.NONE))));
		list.add(new ItemStack(this, 1, this.getMetaFromState(this.getDefaultState().withProperty(TYPE, EnumType.PLANK).withProperty(AXIS, EnumAxis.X))));
	}

	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return this.flammability;
	}

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return this.fireSpeed;
	}

	@Override
	public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
		IBlockState state = world.getBlockState(pos);
		for (IProperty<?> prop : state.getProperties().keySet()) {
			if (prop.getName().equals("axis")) {
				world.setBlockState(pos, state.cycleProperty(prop));
				return true;
			}
		}
		return false;
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		switch (rot) {
		case COUNTERCLOCKWISE_90:
		case CLOCKWISE_90:
			switch (state.getValue(AXIS)) {
			case X:
				return state.withProperty(AXIS, EnumAxis.Z);
			case Z:
				return state.withProperty(AXIS, EnumAxis.X);
			case Y:
				if (state.getValue(TYPE) == EnumType.PLANK) {
					return state.withProperty(AXIS, EnumAxis.NONE);
				} else return state;
			case NONE:
				if (state.getValue(TYPE) == EnumType.PLANK) {
					return state.withProperty(AXIS, EnumAxis.Y);
				} else return state;
			default:
				return state;
			}
		default:
			return state;
		}
	}

	@Override public boolean isWood(IBlockAccess world, BlockPos pos) { return world.getBlockState(pos).getValue(TYPE) != EnumType.PLANK; }

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(AXIS, EnumAxis.byMeta(meta & 3)).withProperty(TYPE, EnumType.byMeta(meta/4));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(AXIS).getMeta() + (state.getValue(TYPE).getMeta() * 4);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { AXIS, TYPE });
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		if (state.getValue(TYPE) == EnumType.PLANK) {
			return new ItemStack(Item.getItemFromBlock(this), 1, 13);
		} else if (state.getValue(AXIS) == EnumAxis.NONE) {
			return new ItemStack(Item.getItemFromBlock(this), 1, this.getMetaFromState(state));
		} else return new ItemStack(Item.getItemFromBlock(this), 1, this.getMetaFromState(state.withProperty(AXIS, EnumAxis.Y)));
	}

	@Override
	public Block getFullBlock() {
		return this;
	}

	@Override
	public Block getStepBlock(IBlockState state) {
		return this.stepBlock;
	}

	@Override
	public IBlockState getSlabBlock(IBlockState state) {
		if (this.slabBlock instanceof MistBlockSlabWood) {
			boolean isRot = state.getBlock() instanceof BlockStairs && state.getValue(BlockStairs.FACING).getAxis() == Axis.X;
			return this.slabBlock.getDefaultState().withProperty(MistBlockSlabWood.ISROT, isRot);
		}
		return this.slabBlock.getDefaultState();
	}

	@Override
	public Block getWallBlock(IBlockState state) {
		return this.wallBlock;
	}

	@Override
	public Block getStairsBlock(IBlockState state) {
		return this.stairsBlock;
	}

	@Override
	public IBlockState getFullState(IBlockState state) {
		if (state.getBlock() instanceof BlockStairs) {
			return this.getDefaultState().withProperty(TYPE, EnumType.PLANK).withProperty(AXIS, EnumAxis.fromFacingAxis(state.getValue(BlockStairs.FACING).rotateY().getAxis()));
		} else if (state.getBlock() instanceof MistBlockSlabWood) {
			return this.getDefaultState().withProperty(TYPE, EnumType.PLANK).withProperty(AXIS, state.getValue(MistBlockSlabWood.ISROT) ? EnumAxis.Z : EnumAxis.X);
		}
		return this.getDefaultState().withProperty(TYPE, EnumType.PLANK);
	}

	public void setStepBlock(Block stepBlock) {
		this.stepBlock = stepBlock;
	}

	public void setWallBlock(Block wallBlock) {
		this.wallBlock = wallBlock;
	}

	public void setSlabBlock(Block slabBlock) {
		this.slabBlock = slabBlock;
	}

	public void setStairsBlock(Block stairBlock) {
		this.stairsBlock = stairBlock;
	}

	public static enum EnumType implements IStringSerializable {

		LOG("log", 0),
		CHISELED("chiseled", 1),
		DEBARKING("debarking", 2),
		PLANK("plank", 3);

		private static final EnumType[] META_LOOKUP = new EnumType[values().length];
		private final String name;
		private final int meta;

		private EnumType(String name, int meta) {
			this.name = name;
			this.meta = meta;
		}

		public int getMeta() {
			return this.meta;
		}

		@Override
		public String toString() {
			return this.name;
		}

		public static EnumType byMeta(int meta) {
			if (meta < 0 || meta >= META_LOOKUP.length) meta = 0;
			return META_LOOKUP[meta];
		}

		@Override
		public String getName() {
			return this.name;
		}

		static {
			for (EnumType type : values()) {
				META_LOOKUP[type.getMeta()] = type;
			}
		}
	}

	public static enum EnumAxis implements IStringSerializable {

		X("x", 1),
		Y("y", 0),
		Z("z", 2),
		NONE("none", 3);

		private static final EnumAxis[] META_LOOKUP = new EnumAxis[values().length];
		private final String name;
		private final int meta;

		private EnumAxis(String name, int meta) {
			this.name = name;
			this.meta = meta;
		}

		public int getMeta() {
			return this.meta;
		}

		@Override
		public String toString() {
			return this.name;
		}

		public static EnumAxis fromFacingAxis(EnumFacing.Axis axis) {
			switch (axis) {
			case X:
				return X;
			case Y:
				return Y;
			case Z:
				return Z;
			default:
				return NONE;
			}
		}

		public static EnumAxis byMeta(int meta) {
			if (meta < 0 || meta >= META_LOOKUP.length) meta = 0;
			return META_LOOKUP[meta];
		}

		@Override
		public String getName() {
			return this.name;
		}

		static {
			for (EnumAxis axis : values()) {
				META_LOOKUP[axis.getMeta()] = axis;
			}
		}
	}
}
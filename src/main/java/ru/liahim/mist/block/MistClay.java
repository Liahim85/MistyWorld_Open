package ru.liahim.mist.block;

import java.util.Random;

import javax.annotation.Nullable;
import ru.liahim.mist.api.block.IWettable;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.entity.EntityDesertFish;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MistClay extends MistBlockWettable {

	public static final PropertyEnum<EnumClayType> VARIANT = PropertyEnum.<EnumClayType>create("variant", EnumClayType.class);
	public static final PropertyEnum<EnumBlockType> TYPE = PropertyEnum.<EnumBlockType>create("type", EnumBlockType.class);

	public MistClay() {
		super(Material.CLAY, 1);
		this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, EnumClayType.CLAY).withProperty(TYPE, EnumBlockType.NATURE).withProperty(WET, true));
		this.setHardness(1.5F);
		this.setHarvestLevel("shovel", 0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected boolean showPorosityTooltip() { return false; }

	@Override
	public SoundType getSoundType(IBlockState state, World world, BlockPos pos, @Nullable Entity entity) {
		return state.getValue(WET) ? SoundType.GROUND : SoundType.STONE;
    }

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
		float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		IBlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
		if (state.getValue(WET) && state.getValue(TYPE) == EnumBlockType.CRACKED) {
			Material upMat = world.getBlockState(pos.up()).getMaterial();
			if (upMat.isSolid() || upMat.isLiquid()) {
				state = state.withProperty(TYPE, EnumBlockType.NATURE);
			}
		}
		return state;
	}

	@Override
	public boolean setWet(World world, BlockPos pos, IBlockState state, int waterDist, Random rand) {
		if (state.getValue(TYPE) == EnumBlockType.CONTAINER) {
			world.destroyBlock(pos, false);
			return true;
		}
		return super.setWet(world, pos, state, waterDist, rand);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if (state.getValue(TYPE) == EnumBlockType.CONTAINER) {
			EntityDesertFish.spawnFish(world, pos, world.rand);
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public int getTopProtectPercent(IBlockState state, boolean isWet) {
		return isWet ? 40 : 120;
	}

	@Override
	public void onFallenUpon(World world, BlockPos pos, Entity entity, float fallDistance) {
		if (!world.isRemote && world.rand.nextFloat() < fallDistance - 0.5F && entity instanceof EntityLivingBase &&
				(entity instanceof EntityPlayer || world.getGameRules().getBoolean("mobGriefing")) &&
				entity.width * entity.width * entity.height > 0.512F) {
			IBlockState state = world.getBlockState(pos);
			if (state.getValue(WET) && state.getValue(TYPE) == EnumBlockType.CRACKED) {
				world.setBlockState(pos, state.withProperty(TYPE, EnumBlockType.NATURE), 2);
			}
		}
		super.onFallenUpon(world, pos, entity, fallDistance);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		super.neighborChanged(state, world, pos, block, fromPos);
		if (state.getValue(WET) && state.getValue(TYPE) == EnumBlockType.CRACKED) {
			Material upMat = world.getBlockState(pos.up()).getMaterial();
			if (upMat.isSolid() || upMat.isLiquid()) {
				world.setBlockState(pos, state.withProperty(TYPE, EnumBlockType.NATURE), 2);
			}
		}
	}

	@Override
	public boolean setDry(World world, BlockPos pos, IBlockState state, Random rand) {
		IBlockState stateUp = world.getBlockState(pos.up());
		if (stateUp.getBlock() instanceof IWettable ? !stateUp.getValue(WET) : true) {
			if (state.getValue(TYPE) == EnumBlockType.BLOCK) {
				return world.setBlockState(pos, state.withProperty(WET, false), 2);
			} else {
				return world.setBlockState(pos, state.withProperty(WET, false)
						.withProperty(TYPE, stateUp.isSideSolid(world, pos.up(), EnumFacing.DOWN) ? EnumBlockType.NATURE : EnumBlockType.CRACKED), 2);
			}
		}
		return false;
	}

	/*@Override
	public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction,
		IPlantable plantable) {
		EnumPlantType plantType = plantable.getPlantType(world, pos.offset(direction));
		switch (plantType) {
		case Desert:
		case Cave:
			return true;
		case Beach:
			return (world.getBlockState(pos.east()).getMaterial() == Material.WATER ||
					world.getBlockState(pos.west()).getMaterial() == Material.WATER ||
					world.getBlockState(pos.north()).getMaterial() == Material.WATER || world.getBlockState(pos.south()).getMaterial() == Material.WATER);
		default:
			return false;
		}
	}*/

	@Override
	public void getDrops(NonNullList ret, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		if (state.getValue(TYPE) != EnumBlockType.CONTAINER) {
			Random rand = world instanceof World ? ((World)world).rand : RANDOM;
			int count = quantityDropped(state, fortune, rand);
			for (int i = 0; i < count; i++) {
				Item item = this.getItemDropped(state, rand, fortune);
				if (item != null) {
					ret.add(new ItemStack(item, 1, this.damageDropped(state)));
				}
			}
			if (state.getValue(WET) && count < 4 && state.getValue(TYPE) != EnumBlockType.BLOCK) {
				int i = rand.nextInt(8 + count * 4);
				if (i < 2) ret.add(new ItemStack(MistItems.ROCKS));
				else if (i == 2) ret.add(new ItemStack(Items.STICK));
				else if (i < 3 + fortune && rand.nextInt(Math.max(1, 4 - fortune)) == 0) ret.add(new ItemStack(Items.COAL, 1, 1));
			}
		}
	}

	@Override
	@Nullable
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return state.getValue(WET) ? MistItems.CLAY_BALL : Item.getItemFromBlock(this);
	}

	@Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
		return state.getValue(WET) ? (state.getValue(TYPE) == EnumBlockType.BLOCK ? 4 : 2 + random.nextInt(3)) : 1;
    }

	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(WET) ? (state.getValue(VARIANT) == EnumClayType.CLAY ? 0 : 1) : getMetaFromState(state);
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		for (int i = 0; i < 6; ++i) {
			list.add(new ItemStack(this, 1, i));
		}
		for (int i = 8; i < 14; ++i) {
			list.add(new ItemStack(this, 1, i));
		}
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return (state.getValue(VARIANT).getMetadata() << 3) | (state.getValue(TYPE).getMetadata() << 1) | (state.getValue(WET) ? 0 : 1);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(VARIANT, meta < 8 ? EnumClayType.CLAY : EnumClayType.RED_CLAY).withProperty(TYPE, EnumBlockType.byMetadata(meta / 2 & 3)).withProperty(WET, (meta & 1) == 0);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { VARIANT, WET, TYPE });
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		return state.getValue(TYPE) == EnumBlockType.CONTAINER ? state.withProperty(TYPE, EnumBlockType.NATURE) : state;
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		if (state.getValue(TYPE) == EnumBlockType.CONTAINER) state = state.withProperty(TYPE, EnumBlockType.NATURE);
		return new ItemStack(this, 1, getMetaFromState(state));
	}

	public static enum EnumClayType implements IStringSerializable {

		CLAY(0, "clay_gray", MapColor.CLAY),
		RED_CLAY(1, "clay_red", MapColor.RED_STAINED_HARDENED_CLAY);

		private static final EnumClayType[] META_LOOKUP = new EnumClayType[values().length];
		private final int meta;
		private final String name;
		private final MapColor mapColor;

		private EnumClayType(int meta, String name, MapColor mapColor) {
			this.meta = meta;
			this.name = name;
			this.mapColor = mapColor;
		}

		public int getMetadata() {
			return this.meta;
		}

		public MapColor getMapColor() {
			return this.mapColor;
		}

		public static EnumClayType byMetadata(int meta) {
			if (meta < 0 || meta >= META_LOOKUP.length) meta = 0;
			return META_LOOKUP[meta];
		}

		@Override
		public String getName() {
			return this.name;
		}

		static {
			for (EnumClayType type : values()) {
				META_LOOKUP[type.getMetadata()] = type;
			}
		}
	}
	
	public static enum EnumBlockType implements IStringSerializable {

		BLOCK(0, "block"),
		NATURE(1, "nature"),
		CRACKED(2, "cracked"),
		CONTAINER(3, "container");

		private static final EnumBlockType[] META_LOOKUP = new EnumBlockType[values().length];
		private final int meta;
		private final String name;

		private EnumBlockType(int meta, String name) {
			this.meta = meta;
			this.name = name;
		}

		public int getMetadata() {
			return this.meta;
		}

		public static EnumBlockType byMetadata(int meta) {
			if (meta < 0 || meta >= META_LOOKUP.length) meta = 0;
			return META_LOOKUP[meta];
		}

		@Override
		public String getName() {
			return this.name;
		}

		static {
			for (EnumBlockType type : values()) {
				META_LOOKUP[type.getMetadata()] = type;
			}
		}
	}
}
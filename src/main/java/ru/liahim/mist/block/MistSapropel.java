package ru.liahim.mist.block;

import java.util.Random;

import javax.annotation.Nullable;

import ru.liahim.mist.api.item.MistItems;
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**@author Liahim*/
public class MistSapropel extends MistBlockWettableFalling {

	public static final PropertyEnum<EnumBlockType> TYPE = PropertyEnum.<EnumBlockType>create("type", EnumBlockType.class);

	public MistSapropel() {
		super(Material.CLAY, 2);
		this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, EnumBlockType.NATURE).withProperty(WET, true));
		this.setHardness(0.5F);
		this.setHarvestLevel("shovel", 0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected boolean showPorosityTooltip() { return false; }

	@Override
	public SoundType getSoundType(IBlockState state, World world, BlockPos pos, @Nullable Entity entity) {
		return state.getValue(WET) ? SoundType.SLIME : SoundType.GROUND;
    }

	@Override
	@Nullable
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return state.getValue(WET) ? new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.875D, 1.0D) : Block.FULL_BLOCK_AABB;
	}

	@Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
		if (state.getValue(WET)) {
			entity.motionX *= 0.4D;
			entity.motionZ *= 0.4D;
		}
    }
	
	@Override
	protected boolean canFall(World world, BlockPos pos, IBlockState state, Random rand) {
		return state.getValue(WET) && state.getValue(TYPE) == EnumBlockType.BLOCK &&
				(world.isAirBlock(pos.down()) || canFallThrough(world.getBlockState(pos.down()))) && pos.getY() >= 0;
	}

	@Override
	public boolean setWet(World world, BlockPos pos, IBlockState state, int waterDist, Random rand) {
		return world.setBlockState(pos, state.withProperty(WET, true));
	}

	@Override
	public boolean setDry(World world, BlockPos pos, IBlockState state, Random rand) {
		return world.setBlockState(pos, state.withProperty(WET, false), 2);
	}

	@Override
	public boolean setAcid(World world, BlockPos pos, IBlockState state, int waterDist, Random rand) {
		return world.destroyBlock(pos, true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {}

	/*@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return world.getBlockState(pos).getValue(WET) ? 0 : 10;
    }*/

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return world.getBlockState(pos).getValue(WET) ? 0 : 10;
    }

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		for (int i = 0; i < 4; ++i) {
			list.add(new ItemStack(this, 1, i));
		}
	}

	@Override
	public void getDrops(NonNullList ret, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		Random rand = world instanceof World ? ((World)world).rand : RANDOM;
		int count = quantityDropped(state, fortune, rand);
		for (int i = 0; i < count; i++) {
			Item item = this.getItemDropped(state, rand, fortune);
			if (item != null) {
				ret.add(new ItemStack(item, 1, this.damageDropped(state)));
			}
		}
		if (count < 3 && state.getValue(TYPE) == EnumBlockType.NATURE && rand.nextInt(3 + count * 2) == 0) {
			if (rand.nextInt(8) == 0) ret.add(new ItemStack(Items.BONE));
			else ret.add(new ItemStack(Items.STICK));
		}
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return state.getValue(WET) ? MistItems.SAPROPEL_BALL : Item.getItemFromBlock(this);
	}

	@Override
	public int quantityDropped(IBlockState state, int fortune, Random rand) {
		return state.getValue(WET) ? (state.getValue(TYPE) == EnumBlockType.BLOCK ? 4 : rand.nextInt(3) + 1) : 1;
	}

	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(WET) ? 0 : getMetaFromState(state);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return (state.getValue(TYPE).getMetadata() << 1) | (state.getValue(WET) ? 0 : 1);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(TYPE, EnumBlockType.byMetadata(meta >> 1)).withProperty(WET, (meta & 1) == 0);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { TYPE, WET });
	}

	@Override
	public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
		return MapColor.BLACK_STAINED_HARDENED_CLAY;
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(this, 1, getMetaFromState(state));
	}

	@Override
	public void setAcidBlock(Block acidBlock) {}

	@Override
	public Block getAcidBlock(IBlockState state) { return null; }

	public static enum EnumBlockType implements IStringSerializable {

		BLOCK(0, "block"),
		NATURE(1, "nature");

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
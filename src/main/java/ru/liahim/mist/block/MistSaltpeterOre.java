package ru.liahim.mist.block;

import ru.liahim.mist.api.block.IMistStoneUpper;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.common.Mist;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class MistSaltpeterOre extends MistOre implements IMistStoneUpper {

	public static final PropertyEnum<SaltType> TYPE = PropertyEnum.<SaltType>create("type", SaltType.class);
	public static Item salt = null;

	public MistSaltpeterOre() {
		super(5, 20, 1, Material.ROCK.getMaterialMapColor());
		this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, SaltType.NORMAL));
	}

	@Override
	public boolean isUpperStone(IBlockState state) {
		return true;
	}

	@Override
	public float getBlockHardness(IBlockState state, World world, BlockPos pos) {
		return state.getValue(TYPE) == SaltType.SALT ? 100 : 5;
	}

	@Override
	public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
		return world.getBlockState(pos).getValue(TYPE) == SaltType.SALT ? 1000 : 20;
	}

	@Override
	public int getHarvestLevel(IBlockState state) {
		return state.getValue(TYPE) == SaltType.SALT ? 2 : 1;
	}

	@Override
	@Nullable
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		if (state.getValue(TYPE) == SaltType.SALT && salt != null) return salt;
		else return MistItems.SALTPETER;
	}

	@Override
	public int quantityDropped(Random random) {
		return 1 + random.nextInt(3);
	}

	@Override
	public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune) {
		Random rand = world instanceof World ? ((World)world).rand : new Random();
		return MathHelper.getInt(rand, 0, 2);
	}

	@Override
	public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
		if (state.getValue(TYPE) == SaltType.SALT && salt == null) state = state.withProperty(TYPE, SaltType.NORMAL);
		return new ItemStack(this, 1, getMetaFromState(state));
	}

	@Override
	public int damageDropped(IBlockState state) {
		return 0;
	}

	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return state.getValue(TYPE) == SaltType.SALT ? EnumPushReaction.BLOCK : EnumPushReaction.NORMAL;
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		list.add(new ItemStack(this, 1, 0));
		if (Mist.saltymod) list.add(new ItemStack(this, 1, 1));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(TYPE).ordinal();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(TYPE, SaltType.values()[meta]);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { TYPE });
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (state.getValue(TYPE) == SaltType.SALT && salt == null) return state.withProperty(TYPE, SaltType.NORMAL);
		return state;
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return this.getItem(world, pos, state);
	}

	@Override
	protected ItemStack getSilkTouchDrop(IBlockState state) {
		int i = state.getValue(TYPE) == SaltType.SALT && Mist.saltymod ? 1 : 0;
		return new ItemStack(this, 1, i);
	}

	public static enum SaltType implements IStringSerializable {

		NORMAL("normal"),
		SALT("salt");

		private final String name;

		private SaltType(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return this.name;
		}
	}
}
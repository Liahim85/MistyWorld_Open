package ru.liahim.mist.block;

import java.util.Random;

import javax.annotation.Nullable;

import ru.liahim.mist.api.block.IFarmland;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.item.MistItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**@author Liahim*/
public class MistFarmland_H extends MistHumus_Dirt implements IFarmland {

	protected static final AxisAlignedBB FARMLAND_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.9375D, 1.0D);

	public MistFarmland_H(float hardness, int waterPerm) {
		super(Material.GROUND, hardness, waterPerm);
		this.setDefaultState(this.blockState.getBaseState().withProperty(WET, true).withProperty(MULCH, false));
		this.setLightOpacity(255);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return state.getValue(MULCH) ? FULL_BLOCK_AABB : FARMLAND_AABB;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return state.getValue(MULCH);
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return state.getValue(MULCH);
	}

	@Override
	public void onFallenUpon(World world, BlockPos pos, Entity entity, float fallDistance) {
		if (!world.isRemote && world.rand.nextFloat() < fallDistance - 0.5F && entity instanceof EntityLivingBase &&
				(entity instanceof EntityPlayer || world.getGameRules().getBoolean("mobGriefing")) &&
				entity.width * entity.width * entity.height > 0.512F) {
			IBlockState state = world.getBlockState(pos);
			if (!state.getValue(MULCH)) world.setBlockState(pos, this.getSoilBlock().getDefaultState().withProperty(WET, state.getValue(WET)));
		}
		super.onFallenUpon(world, pos, entity, fallDistance);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		boolean change = false;
		if (rand.nextInt(50) == 0 && !state.getValue(MULCH)) {
			Block block = world.getBlockState(pos.up()).getBlock();
			change = !(block instanceof IPlantable) || !canSustainPlant(world.getBlockState(pos), world, pos, EnumFacing.UP, (IPlantable)block);
		}
		if (change) world.setBlockState(pos, this.getSoilBlock().getDefaultState().withProperty(WET, state.getValue(WET)));
		else super.updateTick(world, pos, state, rand);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		super.neighborChanged(state, world, pos, block, fromPos);
		Material upMat = world.getBlockState(pos.up()).getMaterial();
		if (upMat.isSolid()) {
			if (!state.getValue(MULCH)) {
				world.setBlockState(pos, this.getSoilBlock().getDefaultState().withProperty(WET, state.getValue(WET)));
			}
		} else if (upMat.isLiquid()) {
			if (state.getValue(MULCH)) Block.spawnAsEntity(world, pos.up(), new ItemStack(MistItems.MULCH));
			world.setBlockState(pos, this.getSoilBlock().getDefaultState().withProperty(WET, state.getValue(WET)));
		}
	}

	@Override
	@Nullable
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		if (state.getValue(WET)) return MistItems.HUMUS;
		return Item.getItemFromBlock(this.getSoilBlock());
	}

	@Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
		if (state.getValue(WET)) return 4;
		return 1;
    }

	@Override
	public int damageDropped(IBlockState state) {
		if (state.getValue(WET)) return 0;
		return getMetaFromState(state) % 2;
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		super.getDrops(drops, world, pos, state, fortune);
		if (state.getValue(MULCH)) drops.add(new ItemStack(MistItems.MULCH));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {}

	@Override
	public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction,
		IPlantable plantable) {
		EnumPlantType plantType = plantable.getPlantType(world, pos.offset(direction));
		switch (plantType) {
		case Plains:
		case Cave:
		case Crop:
			return true;
		default:
			return false;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		switch (side) {
		case UP:
			return true;
		case NORTH:
		case SOUTH:
		case WEST:
		case EAST:
			IBlockState checkState = world.getBlockState(pos.offset(side));
			Block block = checkState.getBlock();
			return !checkState.isOpaqueCube() && block != Blocks.FARMLAND && block != Blocks.GRASS_PATH &&
					!(block instanceof IFarmland) && block != MistBlocks.FLOATING_MAT;
		default:
			return super.shouldSideBeRendered(state, world, pos, side);
		}
	}

	@Override
	public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
		switch (face) {
		case UP:
			return false;
		case NORTH:
		case SOUTH:
		case WEST:
		case EAST:
			IBlockState faceState = world.getBlockState(pos.offset(face));
			Block block = faceState.getBlock();
			return faceState.getMaterial().isLiquid();
		default:
			return false;
		}
	}

	@Override
	public boolean isFertile(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		return state.getValue(WET);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return (state.getValue(MULCH) ? 1 : 0) << 1 | (state.getValue(WET) ? 0 : 1);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(WET, (meta & 1) == 1).withProperty(MULCH, meta > 1);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { MULCH, WET });
	}

	@Override
	public int getTopProtectPercent(IBlockState state, boolean isWet) {
		if (state.getValue(MULCH)) return isWet ? 10 : 140;
		return super.getTopProtectPercent(state, isWet);
	}

	@Override
	public int getCloseProtectPercent(IBlockState state, boolean isWet) {
		if (state.getValue(MULCH)) return isWet ? 10 : 140;
		return super.getCloseProtectPercent(state, isWet);
	}

	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) { return false; }

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos,
		EntityPlayer player) {
		return new ItemStack(this.getSoilBlock(), 1, this.damageDropped(state));
	}
}
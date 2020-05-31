package ru.liahim.mist.block.gizmos;

import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ru.liahim.mist.api.block.IShiftPlaceable;
import ru.liahim.mist.api.block.IWettable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.api.registry.ICompostIngredient;
import ru.liahim.mist.api.registry.MistRegistry;
import ru.liahim.mist.block.MistBlock;
import ru.liahim.mist.block.MistLeaves;
import ru.liahim.mist.block.MistSoil;
import ru.liahim.mist.init.ModAdvancements;
import ru.liahim.mist.world.MistWorld;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**@author Liahim*/
public class MistCompostHeap extends MistBlock implements IShiftPlaceable {

	public static final PropertyInteger STAGE = PropertyInteger.create("stage", 0, 7);
	public static final PropertyBool WORK = PropertyBool.create("work");

	public MistCompostHeap() {
		super(Material.GROUND);
		this.setSoundType(SoundType.GROUND);
		this.setHardness(0.5F);
		this.setDefaultState(this.blockState.getBaseState().withProperty(STAGE, 0).withProperty(WORK, false));
		this.setHarvestLevel("shovel", 0);
		this.setTickRandomly(true);
		this.useNeighborBrightness = true;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		if (state.getValue(WORK))
			return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.9375D, 1.0D);
		return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, (state.getValue(STAGE) + 1) * 0.125D, 1.0D);
	}

	@Override
	@Nullable
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		if (state.getValue(WORK))
			return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.9375D, 1.0D);
		return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, state.getValue(STAGE) * 0.125D + 0.0625D, 1.0D);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (side == EnumFacing.UP) {
			ItemStack heldItem = player.getHeldItem(hand);
			int st = state.getValue(STAGE);
			if (state.getValue(WORK)) {
				if (heldItem != null && heldItem.getItem() == Items.DYE && heldItem.getItemDamage() == 15) {
					IBlockState downState = world.getBlockState(pos.down());
					if (player.capabilities.isCreativeMode || (downState.getBlock() instanceof IWettable && downState.getValue(IWettable.WET)) ||
							world.isRainingAt(pos.up()) || MistWorld.getHumi(world, pos, 0) >= 50 || IWettable.checkWaterDist(world, pos, EnumFacing.UP, 3, 0) >= 0) {
						if (!player.capabilities.isCreativeMode) heldItem.setCount(heldItem.getCount() - 1);
						if (st < 6) world.setBlockState(pos, state.withProperty(STAGE, (st + 2) >> 1 << 1));
						else world.setBlockState(pos, MistBlocks.HUMUS_DIRT.getDefaultState());
						world.playEvent(2005, pos.up(), 0);
						return true;
					}
				}
			} else {
				if (st == 7) {
					if (heldItem != null && heldItem.getItem() == Items.DYE && heldItem.getItemDamage() == 15) {
						if (!player.capabilities.isCreativeMode) heldItem.setCount(heldItem.getCount() - 1);
						world.setBlockState(pos, state.withProperty(WORK, true).withProperty(STAGE, 0));
						world.playEvent(2005, pos.up(), 0);
						return true;
					}
				} else if (isCompostIngredient(heldItem)) {
					if (!player.capabilities.isCreativeMode) heldItem.setCount(heldItem.getCount() - 1);
					world.setBlockState(pos, state.withProperty(STAGE, st + 1));
					world.playSound(null, pos.up(), SoundEvents.BLOCK_GRASS_PLACE, SoundCategory.BLOCKS, 1.0F, 0.8F);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		if (!world.isRemote) {
			if (!world.isSideSolid(pos.down(), EnumFacing.UP)) world.destroyBlock(pos, true);
			else if (!state.getValue(WORK) && block != MistBlocks.FLOATING_MAT) {
				int count = 0;
				for (EnumFacing face : EnumFacing.HORIZONTALS) {
					if (count < 2) {
						if (world.isSideSolid(pos.offset(face), face.getOpposite()) || checkCompostHeap(world.getBlockState(pos.offset(face))))
							++count;
					} else break;
				}
				if (count < 2) world.destroyBlock(pos, true);
			}
		}
	}

	public static boolean checkCompostHeap(IBlockState state) {
		return state.getBlock() == MistBlocks.COMPOST_HEAP && (state.getValue(WORK) || state.getValue(STAGE) == 7);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (!world.isRemote && rand.nextInt(8) == 0) {
			int i = -1;
			if (state.getValue(WORK)) i = 1;
			else if (state.getValue(STAGE) == 7) i = 0;
			if (i >= 0) {
				IBlockState downState = world.getBlockState(pos.down());
				if ((downState.getBlock() instanceof IWettable && downState.getValue(IWettable.WET)) || world.isRainingAt(pos.up()) ||
						MistWorld.getHumi(world, pos, 0) >= 50 || IWettable.checkWaterDist(world, pos, EnumFacing.UP, 3, 0) >= 0) {
					if (i == 0) world.setBlockState(pos, this.getDefaultState().withProperty(WORK, true).withProperty(STAGE, 0));
					else {
						i = state.getValue(STAGE);
						if (i == 7) world.setBlockState(pos, MistBlocks.HUMUS_DIRT.getDefaultState());
						else world.setBlockState(pos, state.withProperty(STAGE, i + 1));
					}
				}
			}
		}
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(STAGE) + (state.getValue(WORK) ? 8 : 0);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(STAGE, meta % 8).withProperty(WORK, meta > 7);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { STAGE, WORK });
	}

	public static boolean isCompostIngredient(ItemStack stack) {
		if (stack == null) return false;
		Item item = stack.getItem();
		if (item instanceof ICompostIngredient) return true;
		if (item instanceof ItemBlock && Block.getBlockFromItem(item) != Blocks.AIR) {
			Block block = Block.getBlockFromItem(item);
			if (block instanceof MistLeaves) return true; //TODO �������� �� ������
			if (block == MistBlocks.TREE_SAPLING) return true;

			//Vanilla blocks
			if (block == Blocks.TALLGRASS) return true;
			//if (block == Blocks.LEAVES) return true;
			//if (block == Blocks.LEAVES2) return true;
			if (block == Blocks.YELLOW_FLOWER) return true;
			if (block == Blocks.RED_FLOWER) return true;
			if (block == Blocks.DOUBLE_PLANT) return true;
			if (block == Blocks.RED_MUSHROOM) return true;
			if (block == Blocks.BROWN_MUSHROOM) return true;
			if (block == Blocks.VINE) return true;
			if (block == Blocks.WATERLILY) return true;
			if (block == Blocks.DEADBUSH) return true;
			if (block == Blocks.SAPLING) return true;
			if (block == Blocks.REEDS) return true;
		}
		//Mod items
		if (item == MistItems.COMPOST) return true;
		if (item == MistItems.MULCH) return true;
		if (item == MistItems.DESERT_COTTON_SEED) return true;
		if (item == MistItems.REMAINS) return true;
		if (item == MistItems.TREE_SEEDS) return true;
		if (item == MistItems.MUSHROOMS_FOOD) return true;
		if (item == MistItems.MUSHROOMS_COOK) return true;
		if (item == MistItems.NIGHTBERRY) return true;
		if (item == MistItems.TINDER_FUNGUS) return true;
		//Vanilla items
		if (item == Items.POISONOUS_POTATO) return true;
		if (item == Items.REEDS) return true;
		if (item == Items.APPLE) return true;
		if (item == Items.POTATO) return true;
		if (item == Items.CARROT) return true;
		if (item == Items.BEETROOT) return true;
		if (item == Items.WHEAT) return true;
		return MistRegistry.isCompostIngredient(stack);
	}

	@Override
	public void getDrops(NonNullList ret, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		boolean work = state.getValue(WORK);
		int st = state.getValue(STAGE);
		int count = work ? 8 : st + 1;
		int humus = work ? (st >> 1 << 1) / 2 : 0;
		for (int i = 0; i < count; i++) {
			if (humus > 0) {
				ret.add(new ItemStack(MistItems.HUMUS, 1));
				--count;
			}
			else ret.add(new ItemStack(MistItems.COMPOST, 1));
			--humus;
		}
	}

	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 20;
    }

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 5;
    }

    @Override
	public boolean isTopSolid(IBlockState state) {
        return isFullCube(state);
    }

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return isFullCube(state);
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return !state.getValue(WORK) && state.getValue(STAGE) == 7;
    }

	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
		return state.getValue(WORK) || state.getValue(STAGE) == 7;
    }

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
		return side == EnumFacing.DOWN ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
	}

	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.DESTROY;
	}

	@Override
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        entityIn.motionX *= 0.4D;
        entityIn.motionZ *= 0.4D;
    }

	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		return false;
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(MistItems.COMPOST, 1);
	}

	@Override
	public boolean onShiftPlacing(World world, BlockPos pos, @Nonnull ItemStack stack, EntityPlayer player, float hitX, float hitY, float hitZ, BlockFaceShape bfs) {
		if (stack.getItem() == MistItems.REMAINS) return false;
		if (bfs != BlockFaceShape.SOLID) return false;
		if (world.getBlockState(pos.down()).getBlock() instanceof MistSoil) {
			int count = 0;
			for (EnumFacing face : EnumFacing.HORIZONTALS) {
				if (count < 2) {
					if (world.isSideSolid(pos.offset(face), face.getOpposite()) ||
							MistCompostHeap.checkCompostHeap(world.getBlockState(pos.offset(face))))
						++count;
				} else break;
			}
			if (count > 1 && MistCompostHeap.isCompostIngredient(stack) && world.setBlockState(pos, this.getDefaultState())) {
				world.playSound(null, pos, SoundEvents.BLOCK_GRASS_PLACE, SoundCategory.BLOCKS, 1.0F, 0.8F);
				if (player instanceof EntityPlayerMP) ModAdvancements.COMPOST.trigger((EntityPlayerMP)player, stack);
				if (!player.capabilities.isCreativeMode) stack.setCount(stack.getCount() - 1);
				return true;
			}
		}
		return false;
	}
}
package ru.liahim.mist.block.upperplant;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.block.IColoredBlock;
import ru.liahim.mist.api.block.IRubberBallCollideble;
import ru.liahim.mist.api.block.ISeasonalChanges;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.block.MistBlock;
import ru.liahim.mist.common.MistTime;
import ru.liahim.mist.entity.EntityRubberBall;
import ru.liahim.mist.init.BlockColoring;
import ru.liahim.mist.world.MistWorld;

public class MistNightberry extends MistBlock implements IColoredBlock, ISeasonalChanges, IRubberBallCollideble {

	@Override
	@SideOnly(Side.CLIENT)
	public IBlockColor getBlockColor() {
		return BlockColoring.GRASS_COLORING_0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IItemColor getItemColor() {
		return BlockColoring.BLOCK_ITEM_COLORING;
	}

	public static final PropertyEnum<EnumAge> AGE = PropertyEnum.<EnumAge>create("age", EnumAge.class);
	public static final AxisAlignedBB AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D);

	public MistNightberry() {
		super(Material.PLANTS);
        this.setSoundType(SoundType.PLANT);
        this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, EnumAge.EMPTY));
        this.setHardness(0.2F);
        this.setTickRandomly(true);
	}

	@Override
	public String getUnlocalizedName() {
		return "item.mist." + super.getUnlocalizedName().substring(5);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return AABB;
	}

	@Override
	@Nullable
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return NULL_AABB;
	}

	@Override
	public int getLightValue(IBlockState state) {
		return state.getValue(AGE) == EnumAge.FRUIT ? 2 : 0;
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (!world.isRemote) {
			if (rand.nextInt(10) == 0 && !(world.getBlockState(pos.down(2)).getBlock() instanceof BlockLiquid)) world.setBlockToAir(pos);
			else {
				IBlockState newState = getSeasonState(world, pos, state, MistTime.getTickOfMonth(world));
				if (newState != null) world.setBlockState(pos, newState);
			}
		}
	}

	@Override
	public IBlockState getSeasonState(World world, BlockPos pos, IBlockState state, long monthTick) {
		EnumAge age = state.getValue(AGE);
		int length = 8000;
		long nightTime = getTimeOfNights(monthTick);
		long range = MistTime.getTickInMonth();
		long startTick = nightTime % range;
		startTick -= startTick % length;
		long endTick = startTick + length;
		long r = MistWorld.getPosRandom(world, pos, 0);
		long rr = r % range;
		if (rr > startTick && rr < endTick) {
			endTick = (nightTime % length) * 2;
			startTick = endTick - length;
			rr = r % length;
			if (rr > startTick && rr < endTick && age == EnumAge.POTENTIAL) {
				return state.withProperty(AGE, EnumAge.FRUIT);
			}
			return null;
		} else return age == EnumAge.POTENTIAL ? null : state.withProperty(AGE, EnumAge.POTENTIAL);
	}

	/** Returns sum of nights ticks (from 14000 to 22000) of Year */
	public static long getTimeOfNights(long monthTick) {
		int space = 16000;
		int shift = 20000;
		long dayTime = (monthTick + shift) % 24000;
		long nightTime = (MistTime.getTickOfYear(monthTick) + shift) % MistTime.getTickInYear();
		nightTime -= (nightTime / 24000) * space;
		if (dayTime < space) nightTime += space - dayTime;
		return nightTime;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (state.getValue(AGE) == EnumAge.FRUIT && side == EnumFacing.UP) {
			if (!world.isRemote) {
				this.spawnFruit(world, pos.offset(side));
				world.setBlockState(pos, state.withProperty(AGE, EnumAge.EMPTY));
			}
			return true;
		}
		return false;
	}

	protected void spawnFruit(World world, BlockPos pos) {
		ItemStack stack = new ItemStack(MistItems.NIGHTBERRY);
		EntityItem entity_item = new EntityItem(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack);
		entity_item.setDefaultPickupDelay();
		world.spawnEntity(entity_item);
		world.playSound(null, pos, SoundEvents.BLOCK_GRASS_PLACE, SoundCategory.BLOCKS, 0.2F, 1.5F);
	}

	@Override
	public boolean isCollide(World world, IBlockState state, EntityRubberBall ball, RayTraceResult result, Random rand) {
		if (!world.isRemote && state.getValue(AGE) == EnumAge.FRUIT && ball.getMotion().lengthSquared() > 0.5F) {
			this.spawnFruit(world, result.getBlockPos().offset(result.sideHit));
			world.setBlockState(result.getBlockPos(), state.withProperty(AGE, EnumAge.EMPTY));
		}
		return true;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		if (world.getBlockState(pos.down()).getBlock() != MistBlocks.FLOATING_MAT) {
			world.destroyBlock(pos, true);
		}
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(AGE).ordinal();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(AGE, EnumAge.values()[meta]);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { AGE });
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}

	public static enum EnumAge implements IStringSerializable {

		EMPTY("empty"),
		POTENTIAL("potential"),
		FRUIT("fruit");

		private final String name;

		private EnumAge(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return this.name;
		}

		@Override
		public String getName() {
			return this.name;
		}
	}
}
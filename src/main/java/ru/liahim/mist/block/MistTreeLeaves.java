package ru.liahim.mist.block;

import java.util.Random;

import ru.liahim.mist.api.block.IRubberBallCollideble;
import ru.liahim.mist.api.block.ISeasonalChanges;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.common.MistTime;
import ru.liahim.mist.entity.EntityRubberBall;
import ru.liahim.mist.util.WorldUtil;
import ru.liahim.mist.world.MistWorld;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**@author Liahim*/
public class MistTreeLeaves extends MistLeaves implements ISeasonalChanges, IRubberBallCollideble {

	public static final PropertyEnum<EnumAge> AGE = PropertyEnum.<EnumAge>create("age", EnumAge.class);
	public static final PropertyBool FAST = PropertyBool.create("fast");

	public MistTreeLeaves(int baseColor, boolean mixColor, int bloomMonth, int spoilMonth) {
		super(baseColor, mixColor, bloomMonth, spoilMonth);
		this.setDefaultState(this.blockState.getBaseState().withProperty(DIR, EnumFacing.DOWN).withProperty(AGE, EnumAge.POTENTIAL).withProperty(FAST, false));
	}

	public MistTreeLeaves(int baseColor, int bloomMonth, int spoilMonth) {
		this(baseColor, true, bloomMonth, spoilMonth);
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		return Blocks.LEAVES.isOpaqueCube(state) ? state.withProperty(FAST, true) : state;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
		float hitZ, int meta, EntityLivingBase placer) {
		if (placer instanceof EntityPlayer && ((EntityPlayer)placer).isCreative() && facing != EnumFacing.DOWN &&
				world.getBlockState(pos.offset(facing.getOpposite())).getBlock() == this.getTrunkBlock())
			return this.getDefaultState().withProperty(DIR, facing).withProperty(AGE, EnumAge.EMPTY);
		else return this.getDefaultState().withProperty(AGE, EnumAge.EMPTY);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (state.getValue(AGE) == EnumAge.FRUIT && side.getHorizontalIndex() >= 0) {
			if (!world.isRemote) {
				this.spawnFruit(world, pos.offset(side));
				world.setBlockState(pos, state.withProperty(AGE, EnumAge.EMPTY));
			}
			return true;
		}
		return false;
	}

	protected void spawnFruit(World world, BlockPos pos) {
		ItemStack stack = new ItemStack(MistItems.TREE_SEEDS, 1, this.trunkBlock.getSapling().getMetadata());
		EntityItem entity_item = new EntityItem(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack);
		entity_item.setDefaultPickupDelay();
		world.spawnEntity(entity_item);
		world.playSound(null, pos, SoundEvents.BLOCK_GRASS_PLACE, SoundCategory.BLOCKS, 0.2F, 1.5F);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		EnumFacing dir = state.getValue(DIR);
		if (dir == EnumFacing.DOWN) return 0;
		EnumAge age = state.getValue(AGE);
		if (age == EnumAge.EMPTY) return dir.getIndex();
		if (age == EnumAge.POTENTIAL) return dir.getIndex() + 5;
		return dir.getIndex() + 10;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		if (meta < 6) return this.getDefaultState().withProperty(DIR, EnumFacing.getFront(meta)).withProperty(AGE, EnumAge.EMPTY);
		if (meta < 11) return this.getDefaultState().withProperty(DIR, EnumFacing.getFront(meta - 5)).withProperty(AGE, EnumAge.POTENTIAL);
		return this.getDefaultState().withProperty(DIR, EnumFacing.getFront(meta - 10)).withProperty(AGE, EnumAge.FRUIT);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { AGE, DIR, FAST });
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (!world.isRemote) {
			EnumFacing dir = state.getValue(DIR);
			if (dir != EnumFacing.DOWN) {
				if (world.getBlockState(pos.offset(dir.getOpposite())).getBlock() != this.getTrunkBlock()) {
					world.setBlockToAir(pos);
				} else {
					IBlockState leaves = getSeasonState(world, pos, state, MistTime.getTickOfMonth(world));
					if (leaves != null) world.setBlockState(pos, leaves);
				}
			}
		}
	}

	@Override
	public void updateLeaves(World world, BlockPos pos, IBlockState state, BlockPos rootPos, IBlockState rootState,
			BlockPos soilPos, IBlockState soil, Random rand) {
		if (rand.nextInt(MistTime.getDayInMonth()) == 0) {
			if (state.getValue(AGE) == EnumAge.EMPTY) {
				if (this.bloomMonth < this.spoilMonth ? MistTime.getMonth() < this.bloomMonth || MistTime.getMonth() > this.spoilMonth :
					MistTime.getMonth() > this.bloomMonth && MistTime.getMonth() < this.spoilMonth) { 
					if (rootState.getValue(MistTreeTrunk.SIZE) == 4 && (rootState.getValue(MistTreeTrunk.DIR) == EnumFacing.EAST ||
							rootState.getValue(MistTreeTrunk.DIR) == EnumFacing.WEST) && world.getLightBrightness(pos) > 0.45) {
						WorldUtil.simpleSetBlock(world, pos, state.withProperty(AGE, EnumAge.POTENTIAL));
					}
				}
			}
		}
	}

	@Override
	public IBlockState getSeasonState(World world, BlockPos pos, IBlockState state, long monthTick) {
		if (state.getValue(AGE) != EnumAge.EMPTY) {
			if (this.bloomMonth < this.spoilMonth ? MistTime.getMonth() >= this.bloomMonth && MistTime.getMonth() < this.spoilMonth :
					MistTime.getMonth() >= this.bloomMonth || MistTime.getMonth() < this.spoilMonth) {
				if (state.getValue(AGE) == EnumAge.POTENTIAL) {
					long r = MistWorld.getPosRandom(world, pos, 0) % (MistTime.getTickInMonth() * 3);
					if (r < MistTime.getTickInMonth()) {
						if (MistTime.getMonth() == this.bloomMonth) {
							if (r < monthTick) return state.withProperty(AGE, EnumAge.FRUIT);
						} else return state.withProperty(AGE, EnumAge.FRUIT);
					}
				}
			} else {
				if (state.getValue(AGE) == EnumAge.FRUIT) {
					if (MistTime.getMonth() == this.spoilMonth) {
						long r = MistWorld.getPosRandom(world, pos, 0) % (MistTime.getTickInMonth() * 3);
						if (r < MistTime.getTickInMonth()) {
							if (r < monthTick) return state.withProperty(AGE, EnumAge.POTENTIAL);
						}
					} else return state.withProperty(AGE, EnumAge.POTENTIAL);
				} else if (state.getValue(AGE) == EnumAge.POTENTIAL) {
					if (MistTime.getMonth() == this.spoilMonth) {
						long r = MistWorld.getPosRandom(world, pos, 0) % (MistTime.getTickInMonth() * 3);
						if (r < MistTime.getTickInMonth()) {
							if (r >= monthTick) return state.withProperty(AGE, EnumAge.FRUIT);
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean isCollide(World world, IBlockState state, EntityRubberBall ball, RayTraceResult result, Random rand) {
		if (!world.isRemote && state.getValue(AGE) == EnumAge.FRUIT && ball.getMotion().lengthSquared() > 0.5F) {
			this.spawnFruit(world, result.getBlockPos().offset(result.sideHit));
			world.setBlockState(result.getBlockPos(), state.withProperty(AGE, EnumAge.EMPTY));
		}
		return true;
	}

	public static enum EnumAge implements IStringSerializable {

		EMPTY("empty"),
		POTENTIAL("potential"),
		BLOOMY("bloomy"),
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
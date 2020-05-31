package ru.liahim.mist.block.gizmos;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.block.ISeasonalChanges;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.block.MistBlock;
import ru.liahim.mist.block.MistTreeTrunk;
import ru.liahim.mist.init.ModAdvancements;
import ru.liahim.mist.init.ModParticle;
import ru.liahim.mist.tileentity.TileEntityLatexPot;
import ru.liahim.mist.util.SoilHelper;

public class MistLatexPot extends MistBlock implements ITileEntityProvider, ISeasonalChanges {

	public static final PropertyDirection DIR = BlockHorizontal.FACING;
	protected static final AxisAlignedBB SOUTH = new AxisAlignedBB(0.3125D, 0.0D, 0.0D, 0.6875D, 0.375D, 0.375D);
	protected static final AxisAlignedBB NORTH = new AxisAlignedBB(0.3125D, 0.0D, 0.625D, 0.6875D, 0.375D, 1.0D);
	protected static final AxisAlignedBB EAST = new AxisAlignedBB(0.0D, 0.0D, 0.3125D, 0.375D, 0.375D, 0.6875D);
	protected static final AxisAlignedBB WEST = new AxisAlignedBB(0.625D, 0.0D, 0.3125D, 1.0D, 0.375D, 0.6875D);
	private static int updateTime = 1200 * 3;

	public MistLatexPot() {
		super(Material.WOOD);
		this.setSoundType(SoundType.WOOD);
		this.setDefaultState(this.blockState.getBaseState().withProperty(DIR, EnumFacing.SOUTH));
		this.setHardness(0.5F);
		this.setTickRandomly(true);
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		if (facing.getHorizontalIndex() < 0) {
			return this.getDefaultState().withProperty(DIR, placer.getHorizontalFacing().getOpposite());
		} else return this.getDefaultState().withProperty(DIR, facing);
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {
		if (side.getHorizontalIndex() >= 0) {
			pos = pos.offset(side.getOpposite());
			IBlockState state = world.getBlockState(pos);
			if (state.getBlock() == MistBlocks.R_TREE_TRUNK && state.getActualState(world, pos).getValue(MistTreeTrunk.SIZE) == 4) {
				for (EnumFacing face : EnumFacing.HORIZONTALS) {
					if (face != side) {
						state = world.getBlockState(pos.offset(face));
						if (state.getBlock() == this && state.getValue(DIR) == face) return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		if (world.getBlockState(pos.offset(state.getValue(DIR).getOpposite())).getBlock() != MistBlocks.R_TREE_TRUNK) {
			world.destroyBlock(pos, true);
		}
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		EnumFacing face = state.getValue(DIR);
		if (face == EnumFacing.SOUTH) return SOUTH;
		else if (face == EnumFacing.NORTH) return NORTH;
		else if (face == EnumFacing.EAST) return EAST;
		else return WEST;
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		if (!world.isRemote) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityLatexPot) ((TileEntityLatexPot)te).updateTime();
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityLatexPot) {
			TileEntityLatexPot pot = (TileEntityLatexPot)te;
			if (pot.getStage() == 6) {
				if (!world.isRemote) {
					ItemStack stack = new ItemStack(MistItems.LATEX);
					pot.setStage(1);
					pot.updateStatus(state, state);
					pot.updateTime();
					world.spawnEntity(new EntityItem(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack));
					if (player instanceof EntityPlayerMP) ModAdvancements.LATEX.trigger((EntityPlayerMP)player, stack);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		this.update(world, pos, state, rand, false);
	}

	public void update(World world, BlockPos pos, IBlockState state, Random rand, boolean seasonalTest) {
		if (!world.isRemote) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityLatexPot) {
				TileEntityLatexPot pot = (TileEntityLatexPot)te;
				BlockPos down;
				if (pot.root == null) {
					pot.root = pos.offset(state.getValue(DIR).getOpposite());
					down = pot.root.down();
					while (world.getBlockState(down).getBlock() == MistBlocks.R_TREE_TRUNK) down = down.down();
					pot.root = down.up();
				}
				down = pot.root.down();
				if (!pot.isDead) pot.isDead = SoilHelper.getHumus(world.getBlockState(down)) == 3;
				if (!pot.isDead) {
					int st = pot.getStage();
					long lastUpdate = pot.getLastUpdateTime();
					if (st < 6) {
						long step = world.getTotalWorldTime() - lastUpdate;
						if (step >= updateTime) {
							step /= updateTime;
							st += step;
							if (st == 1) st = 2;
							else if (st > 6) st = 6;
							pot.setStage(st);
							pot.updateStatus(state, state);
							pot.updateTime();
						}
					}
					if (seasonalTest) {
						long step = world.getTotalWorldTime() - lastUpdate;
						if (step > 1200 * 500) {
							step /= 1200 * 500;
							for (int i = 0; i < step; i++) {
								((MistTreeTrunk)MistBlocks.R_TREE_TRUNK).makeOlder(world, pot.root, world.getBlockState(pot.root), down, world.getBlockState(down), rand, true);
							}
						}
					} else if (rand.nextInt(500) == 0) {
						((MistTreeTrunk)MistBlocks.R_TREE_TRUNK).makeOlder(world, pot.root, world.getBlockState(pot.root), down, world.getBlockState(down), rand, true);
					}
				}
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		if (rand.nextInt(32) == 0) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityLatexPot) {
				TileEntityLatexPot pot = (TileEntityLatexPot)te;
				if (!pot.isDead && pot.getStage() > 0) {
					EnumFacing dir = state.getValue(DIR);
					double x = pos.getX() + 0.5D;
					double y = pos.getY() + 0.37D;
					double z = pos.getZ() + 0.5D;
					if (dir == EnumFacing.SOUTH) world.spawnParticle(ModParticle.MIST_LATEX, x, y, z - 0.37D, 0, 0, 0);
					else if (dir == EnumFacing.NORTH) world.spawnParticle(ModParticle.MIST_LATEX, x, y, z + 0.37D, 0, 0, 0);
					else if (dir == EnumFacing.WEST) world.spawnParticle(ModParticle.MIST_LATEX, x + 0.37D, y, z, 0, 0, 0);
					else world.spawnParticle(ModParticle.MIST_LATEX, x - 0.37D, y, z, 0, 0, 0);
				}
			}
		}
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		drops.add(new ItemStack(this));
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityLatexPot && ((TileEntityLatexPot)te).getStage() == 6) {
			drops.add(new ItemStack(MistItems.LATEX));
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
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityLatexPot();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(DIR).getHorizontalIndex();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(DIR, EnumFacing.getHorizontal(meta));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { DIR });
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		return layer == BlockRenderLayer.CUTOUT;
	}

	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.DESTROY;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public IBlockState getSeasonState(World world, BlockPos pos, IBlockState state, long monthTick) {
		this.update(world, pos, state, world.rand, true);
		return null;
	}
}
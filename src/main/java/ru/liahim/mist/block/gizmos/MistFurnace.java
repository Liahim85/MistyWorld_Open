package ru.liahim.mist.block.gizmos;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.tileentity.TileEntityMistFurnace;

public class MistFurnace extends MistBlockContainer {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	public static final PropertyInteger STATUS = PropertyInteger.create("status", 0, 4);
	private static boolean keepInventory;

	public MistFurnace() {
        super(Material.ROCK);
        this.setHardness(3.5F);
        this.setDefaultState(this.blockState.getBaseState().withProperty(STATUS, 1).withProperty(FACING, EnumFacing.NORTH));
    }

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return MistItems.ROCKS;
	}

	@Override
	public int quantityDropped(IBlockState state, int fortune, Random random) {
		return 8;
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		this.setDefaultFacing(world, pos, state);
		//if (!world.isRemote) TileEntityMistFurnace.initializeLoot(world.getTileEntity(pos), world.rand);
		checkSingal(world, pos);
	}

	public static void checkSingal(World world, BlockPos pos, TileEntityMistFurnace furnace) {
		int power = world.isBlockIndirectlyGettingPowered(pos);
		if (power > 0) {
			if (furnace.isClose()) {
				furnace.setSignal(true);
				furnace.markDirty();
			} else if (furnace.getComparatorOutput() >= power) {
				furnace.setSignal(true);
				furnace.setClose(true);
				furnace.updateStatus();
				furnace.markDirty();
			}
		} else if (furnace.hasSignal()) {
			furnace.setSignal(false);
			furnace.setClose(false);
			furnace.updateStatus();
			furnace.markDirty();
		}
	}

	public static void checkSingal(World world, BlockPos pos) {
		if (!world.isRemote) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityMistFurnace) checkSingal(world, pos, ((TileEntityMistFurnace)te));
		}
	}

	@Override
	public boolean shouldCheckWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return false;
	}

	private void setDefaultFacing(World world, BlockPos pos, IBlockState state) {
		if (!world.isRemote) {
			IBlockState state_n = world.getBlockState(pos.north());
			IBlockState state_s = world.getBlockState(pos.south());
			IBlockState state_w = world.getBlockState(pos.west());
			IBlockState state_e = world.getBlockState(pos.east());
			EnumFacing face = state.getValue(FACING);
			if (face == EnumFacing.NORTH && state_n.isFullBlock() && !state_s.isFullBlock()) face = EnumFacing.SOUTH;
			else if (face == EnumFacing.SOUTH && state_s.isFullBlock() && !state_n.isFullBlock()) face = EnumFacing.NORTH;
			else if (face == EnumFacing.WEST && state_w.isFullBlock() && !state_e.isFullBlock()) face = EnumFacing.EAST;
			else if (face == EnumFacing.EAST && state_e.isFullBlock() && !state_w.isFullBlock()) face = EnumFacing.WEST;
			world.setBlockState(pos, state.withProperty(FACING, face), 2);
		}
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		int i = state.getValue(STATUS);
        return i < 2 ? 0 : i == 3 ? 14 : 7;
    }

	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings("incomplete-switch")
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		int i = state.getValue(STATUS);
		if (i == 3) {
			if (rand.nextInt(24) == 0) {
				world.playSound(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 1.0F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.3F, false);
			}
			if (rand.nextBoolean()) {
				EnumFacing enumfacing = state.getValue(FACING);
				double d0 = pos.getX() + 0.5D;
				double d1 = pos.getY() + rand.nextDouble() * 3.0D / 16.0D + 0.5625D;
				double d2 = pos.getZ() + 0.5D;
				double d3 = 0.52D;
				double d4 = rand.nextDouble() * 0.4D - 0.2D;
				switch (enumfacing) {
				case WEST:
					world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 - 0.52D, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
					break;
				case EAST:
					world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + 0.52D, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
					break;
				case NORTH:
					world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + d4, d1, d2 - 0.52D, 0.0D, 0.0D, 0.0D);
					break;
				case SOUTH:
					world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + d4, d1, d2 + 0.52D, 0.0D, 0.0D, 0.0D);
				}
			}
		} else if ((i == 2 || i == 4) && rand.nextDouble() < 0.1D) {
			world.playSound(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (world.isRemote) return true;
		else {
			TileEntity tileentity = world.getTileEntity(pos);
			if (tileentity instanceof TileEntityMistFurnace) {
				ItemStack heldItem = player.getHeldItem(hand);
				if (heldItem.getItem() instanceof ItemFlintAndSteel && state.getValue(STATUS) == 1) {
					heldItem.damageItem(1, player);
					((TileEntityMistFurnace)tileentity).fire();
					world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
				} else player.openGui(Mist.MODID, 7, world, pos.getX(), pos.getY(), pos.getZ());
			}
			return true;
		}
	}

	public static void setState(int status, World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		if (state.getValue(STATUS) != status) {
			TileEntity te = world.getTileEntity(pos);
			state = state.withProperty(STATUS, status);
			keepInventory = true;
			world.setBlockState(pos, state, 3);
			world.setBlockState(pos, state, 3);
			keepInventory = false;
			if (te != null) {
				te.validate();
				world.setTileEntity(pos, te);
			}
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityMistFurnace();
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		world.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);
		if (stack.hasDisplayName()) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityMistFurnace) {
				((TileEntityMistFurnace)te).setCustomName(stack.getDisplayName());
			}
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if (!keepInventory) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityMistFurnace) {
				InventoryHelper.dropInventoryItems(world, pos, (TileEntityMistFurnace)te);
				int ash = ((TileEntityMistFurnace)te).ashProgress[0] + ((TileEntityMistFurnace)te).ashProgress[1];
				if (ash >= 1000) InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(MistItems.ASH));
				world.updateComparatorOutputLevel(pos, this);
			}
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState blockState, World world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityMistFurnace) return ((TileEntityMistFurnace)te).getComparatorOutput();
		return 0;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		checkSingal(world, pos);
	}

	@Override
	public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
		return new ItemStack(MistBlocks.FURNACE);
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		return state.getValue(STATUS) == 3 ? layer == BlockRenderLayer.CUTOUT_MIPPED : layer == BlockRenderLayer.SOLID;
    }

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta & 3)).withProperty(STATUS, meta >> 2);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int status = state.getValue(STATUS);
		if (status == 4) status = 0;
		return (status << 2) | state.getValue(FACING).getHorizontalIndex();
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirror) {
		return state.withRotation(mirror.toRotation(state.getValue(FACING)));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { STATUS, FACING });
	}
}
package ru.liahim.mist.block.gizmos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.block.IColoredBlock;
import ru.liahim.mist.api.block.IShiftPlaceable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.sound.MistSounds;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntityGraveBug;
import ru.liahim.mist.tileentity.TileEntityUrn;
import ru.liahim.mist.tileentity.TileEntityUrn.UrnType;
import ru.liahim.mist.util.ColorHelper;

public class MistUrn extends MistBlockContainer implements IColoredBlock, IShiftPlaceable {

	@Override
	@SideOnly(Side.CLIENT)
	public IBlockColor getBlockColor() {
		return new IBlockColor() {
			@Override
			public int colorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex) {
				if (world != null && pos != null && tintIndex == 0) {
					if (world.getTileEntity(pos) instanceof TileEntityUrn) {
						return ((TileEntityUrn) world.getTileEntity(pos)).getTintColor();
					}
				}
				return 0xFFFFFFFF;
			}
		};
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IItemColor getItemColor() {
		return new IItemColor() {
			@Override
			public int colorMultiplier(ItemStack stack, int tintIndex) {
				NBTTagCompound tag = stack.getSubCompound("Urn");
				if (tag != null) {
					if (!UrnType.byId(tag.getInteger("UrnType")).isRare()) {
						int tint = tag.hasKey("TintColor") ? tag.getInteger("TintColor") : -1;
						if (tint < 0) tint = TileEntityUrn.clayColor;
						int patina = tag.hasKey("PatinaColor") ? tag.getInteger("PatinaColor") : -1;
						if (patina < 0) patina = tint;
						return tintIndex == 0 ? tint : tintIndex == 1 ? patina : ColorHelper.mixColor(tint, patina);
					} else return 0xFFFFFFFF;
				} else return stack.getItemDamage() == 0 ? TileEntityUrn.clayColor : TileEntityUrn.rawColor;
			}
		};
	}

	protected static final AxisAlignedBB URN_AABB = new AxisAlignedBB(0.3125D, 0.0D, 0.3125D, 0.6875D, 0.5625D, 0.6875D);
	private static final int breakChance = 2;

	public MistUrn() {
		super(Material.CLAY);
		this.setHardness(0.2F);
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
	@SideOnly(Side.CLIENT)
	public boolean hasCustomBreakingProgress(IBlockState state) {
		return true;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return URN_AABB;
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		drops.add(this.getUrnItem(world.getTileEntity(pos)));
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
		if (te instanceof TileEntityUrn) {
			TileEntityUrn urn = (TileEntityUrn)te;
			if ((urn.isBug() && this.spawnBug(world, pos)) ||
					(urn.getUrnType().isRare() && urn.getLootTable() != null && world.rand.nextInt(breakChance) == 0)) {
				this.destroy(world, player, pos, te);
			} else spawnAsEntity(world, pos, this.getUrnItem(te));
		}
	}

	private void destroy(World world, EntityPlayer player, BlockPos pos, TileEntity te) {
		world.playSound(null, pos, MistSounds.BLOCK_URN_BREAK, SoundCategory.BLOCKS, 0.8F, world.rand.nextFloat() * 0.4F + 0.8F);
		world.destroyBlock(pos, false);
		dropInventory(world, pos, player, te);
	}

	@Override
	public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		super.onBlockHarvested(world, pos, state, player);
		if (player.isCreative()) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityUrn && ((TileEntityUrn)te).isBug()) this.spawnBug(world, pos);
			dropInventory(world, pos, player, te);
		}
	}

	private ItemStack getUrnItem(@Nullable TileEntity te) {
		ItemStack stack = new ItemStack(this);
		if (te instanceof TileEntityUrn) {
			((TileEntityUrn)te).fillWithLoot(null);
			NBTTagCompound tag = new NBTTagCompound();
			NBTTagCompound urnTag = ((TileEntityUrn)te).writeToNBTUrn(new NBTTagCompound(), false);
			tag.setTag("Urn", urnTag);
			stack.setTagCompound(tag);
			if (((TileEntityUrn)te).hasCustomName()) stack.setStackDisplayName(((TileEntityUrn)te).getName());
		}
		return stack;
	}

	@Override
	public boolean canDropFromExplosion(Explosion explosion) {
		return false;
	}

	@Override
	public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityUrn && ((TileEntityUrn)te).isBug()) this.spawnBug(world, pos);
		dropInventory(world, pos, null, te);
		world.setBlockToAir(pos);
	}

	private void dropInventory(World world, BlockPos pos, EntityPlayer player, TileEntity urn) {
		if (!world.isRemote) {
			if (urn == null) urn = world.getTileEntity(pos);
			if (urn instanceof TileEntityUrn) {
				((TileEntityUrn)urn).fillWithLoot(player);
				InventoryHelper.dropInventoryItems(world, pos, (TileEntityUrn)urn);
				world.updateComparatorOutputLevel(pos, this);
			}
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (world.isRemote) return true;
		else {
			TileEntityUrn te = (TileEntityUrn)this.getLockableContainer(world, pos);
			if (te != null) {
				if ((te.isBug() && spawnBug(world, pos)) ||
						(!te.getUrnType().isRare() && te.getLootTable() != null && world.rand.nextInt(breakChance) == 0)) {
					this.destroy(world, player, pos, te);
				} else {
					if ((te).lidAngle == 0)	(te).openSide = player.getHorizontalFacing();
					player.openGui(Mist.MODID, 5, world, pos.getX(), pos.getY(), pos.getZ());
				}
			}
			return true;
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		if (!canStay(world.getBlockState(pos.down()).getBlockFaceShape(world, pos.down(), EnumFacing.UP))) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityUrn) {
				TileEntityUrn urn = (TileEntityUrn)te;
				if ((urn.isBug() && this.spawnBug(world, pos)) ||
						(!urn.getUrnType().isRare() || urn.getLootTable() != null && world.rand.nextInt(breakChance) == 0)) {
					this.destroy(world, null, pos, te);
				} else world.destroyBlock(pos, true);
			}
		}
    }

	private boolean spawnBug(World world, BlockPos pos) {
		return EntityGraveBug.spawnBug(world, pos, world.rand);
	}

	private boolean canStay(BlockFaceShape bfs) {
		return bfs != BlockFaceShape.BOWL && bfs != BlockFaceShape.UNDEFINED;
	}

	@Nullable
	public ILockableContainer getLockableContainer(World world, BlockPos pos) {
		return this.getContainer(world, pos, false);
	}

	@Nullable
	public ILockableContainer getContainer(World world, BlockPos pos, boolean allowBlocking) {
		TileEntity tileentity = world.getTileEntity(pos);
		if (!(tileentity instanceof TileEntityUrn)) return null;
		if (!allowBlocking && this.isBlocked(world, pos)) return null;
		return (TileEntityUrn)tileentity;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityUrn();
	}

	private boolean isBlocked(World world, BlockPos pos) {
		return false;
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState blockState, World world, BlockPos pos) {
		return Container.calcRedstoneFromInventory(this.getLockableContainer(world, pos));
	}

	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.DESTROY;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		list.add(new ItemStack(this, 1, 1));
		list.add(new ItemStack(this, 1, 0));
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return getUrnItem(world.getTileEntity(pos));
	}

	@Override
	public boolean onShiftPlacing(World world, BlockPos pos, @Nonnull ItemStack stack, EntityPlayer player, float hitX, float hitY, float hitZ, BlockFaceShape bfs) {
		if (stack.getItemDamage() != 0 || !this.canStay(bfs)) return false;
		if (Block.getBlockFromItem(stack.getItem()) == MistBlocks.URN) {
			if (world.setBlockState(pos, MistBlocks.URN.getDefaultState())) {
				world.playSound(null, pos, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.8F, world.rand.nextFloat() * 0.4F + 0.8F);
				TileEntity te = world.getTileEntity(pos);
				if (te instanceof TileEntityUrn) {
					//((TileEntityUrn)te).urnType = UrnType.RARE_LOOKUP.get(world.rand.nextInt(UrnType.RARE_LOOKUP.size()));
					NBTTagCompound tag = stack.getSubCompound("Urn");
					if (tag != null) ((TileEntityUrn)te).readFromNBTUrn(tag);
					if (stack.hasDisplayName()) ((TileEntityUrn)te).setCustomName(stack.getDisplayName());
				}
				stack.shrink(1);
				return true;
			}
		}
		return false;
	}
}
package ru.liahim.mist.block.gizmos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import ru.liahim.mist.api.block.IColoredBlock;
import ru.liahim.mist.api.block.IShiftPlaceable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.MistSoil;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.init.BlockColoring;
import ru.liahim.mist.item.food.ItemMistSoup;
import ru.liahim.mist.tileentity.TileEntityCampStick;
import ru.liahim.mist.tileentity.TileEntityCampfire;
import ru.liahim.mist.util.SoilHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MistCampfire extends MistBlockContainer implements IColoredBlock, IShiftPlaceable {
	
	@Override
	@SideOnly(Side.CLIENT)
	public IBlockColor getBlockColor() {
		return new IBlockColor() {
			@Override
			public int colorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex) {
				if (world != null && pos != null && tintIndex == 1) {
					if (world.getTileEntity(pos) instanceof TileEntityCampfire) {
						return ((TileEntityCampfire) world.getTileEntity(pos)).getStoneColor();
					}
				}
				return 0xFFFFFFFF;
			}
		};
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IItemColor getItemColor() {
		return BlockColoring.BLOCK_ITEM_COLORING;
	}

	public static final PropertyEnum<CookingTool> TOOL = PropertyEnum.<CookingTool>create("tool", CookingTool.class);
	public static final PropertyInteger STAGE = PropertyInteger.create("stage", 0, 10);
	public static final PropertyDirection DIR = BlockHorizontal.FACING;

	protected static final AxisAlignedBB AABB_FIRE_PIT_0 = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.1875D, 1.0D);
	protected static final AxisAlignedBB AABB_FIRE_PIT_6 = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.375D, 1.0D);
	protected static final AxisAlignedBB AABB_FIRE_PIT_POT = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.8125D, 1.0D);
	protected static final AxisAlignedBB AABB_FIRE_WOOD = new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 0.375D, 0.75D);
	protected static final AxisAlignedBB AABB_POT = new AxisAlignedBB(0.21875D, 0.0D, 0.21875D, 0.78125D, 0.8125D, 0.78125D);
	protected static final AxisAlignedBB AABB_FIRE_GRILL = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5625D, 1.0D);

	public MistCampfire() {
		super(Material.GROUND, MapColor.BROWN);
		this.setSoundType(SoundType.STONE);
		this.setDefaultState(this.blockState.getBaseState().withProperty(STAGE, 7));
		this.setHardness(0.5F);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		state = this.getActualState(state, world, pos);
		if (state.getValue(TOOL) == CookingTool.POT) return AABB_FIRE_PIT_POT;
		else if (state.getValue(TOOL) == CookingTool.GRILL) return AABB_FIRE_GRILL;
		return state.getValue(STAGE) < 6 ? AABB_FIRE_PIT_0 : AABB_FIRE_PIT_6;
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox,
			List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean par_7) {
		if (!par_7) {
			state = this.getActualState(state, world, pos);
		}
		for (AxisAlignedBB axisalignedbb : getCollisionBoxList(state)) {
			addCollisionBoxToList(pos, entityBox, collidingBoxes, axisalignedbb);
		}
	}

	private static List<AxisAlignedBB> getCollisionBoxList(IBlockState state) {
		List<AxisAlignedBB> list = Lists.<AxisAlignedBB> newArrayList();
		if (state.getValue(TOOL) == CookingTool.GRILL) list.add(AABB_FIRE_GRILL);
		else {
			list.add(AABB_FIRE_PIT_0);
			if (state.getValue(TOOL) == CookingTool.POT) list.add(AABB_POT);
			else if (state.getValue(STAGE) > 5) list.add(AABB_FIRE_WOOD);
		}
		return list;
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		int st = state.getValue(STAGE);
        return st == 8 ? 14 : st == 9 ? 10 : 0;
    }

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		state = this.getActualState(state, world, pos);
		ItemStack heldItem = player.getHeldItem(hand);
		TileEntityCampfire te = (TileEntityCampfire) world.getTileEntity(pos);
		if (player.isSneaking()) {
			if (hand == EnumHand.MAIN_HAND) {
				if (te.getCookingTool() == CookingTool.POT) {
					if (heldItem.isEmpty() && te.getVolum() == 0) {
						player.setHeldItem(hand, CookingTool.POT.getItem());
						te.setCookingTool(CookingTool.NONE);
						te.updateStatus(state.withProperty(TOOL, CookingTool.NONE));
						return true;
					}
				} else if (te.getCookingTool() == CookingTool.GRILL) {
					if (te.isGrillEmpty()) {
						if (heldItem.isEmpty()) {
							player.setHeldItem(hand, CookingTool.GRILL.getItem());
							te.setCookingTool(CookingTool.NONE);
							te.updateStatus(state.withProperty(TOOL, CookingTool.NONE));
							return true;
						}
					} else {
						int i = getGrillSlot(hitX, hitZ, te.getFacing());
						ItemStack food = te.getGrillStack(i);
						if (!food.isEmpty() && (heldItem.isEmpty() ||
								(heldItem.isStackable() && heldItem.getCount() < heldItem.getMaxStackSize() && heldItem.isItemEqual(food)))) {
							if (heldItem.isEmpty()) player.setHeldItem(hand, food);
							else heldItem.grow(1);
							world.playSound(null, pos, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, SoundCategory.BLOCKS, 0.25F, 1.2F - world.rand.nextFloat() * 0.4F);
							te.setGrillStack(i, ItemStack.EMPTY);
							te.updateStatus(state);
							return true;
						}
					}
				}
			}
		} else {
			if (!heldItem.isEmpty()) {
				Item item = heldItem.getItem();
				if (state.getValue(TOOL) == CookingTool.POT) {
					if (item instanceof ItemFood && !(item instanceof ItemMistSoup)) {
						ItemStack stack = heldItem.copy();
						stack.setCount(1);
						ArrayList<ItemStack> list = te.addFood(stack, -1);
						if (list.get(0).isEmpty()) {
							heldItem.shrink(1);
							if (list.size() > 1) {
								for (int i = 1; i < list.size(); ++i) {
									if (heldItem.isEmpty()) player.setHeldItem(hand, list.get(i));
									else if (!player.inventory.addItemStackToInventory(list.get(i))) {
										world.spawnEntity(new EntityItem(world, pos.getX() + 0.5D, pos.getY() + 1.5D, pos.getZ() + 0.5D, list.get(i)));
									}
								}
							}
							te.updateStatus(state);
							world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL_LAVA, SoundCategory.BLOCKS, 0.25F, 1.2F - world.rand.nextFloat() * 0.4F);
							return true;
						} else return false;
					} else if (item == Items.BOWL || (item instanceof ItemMistSoup && ((ItemMistSoup)item).getCurrentPortion(heldItem) < ((ItemMistSoup)item).getMaxPortion())) {
						if (!world.isRemote) {
							ItemStack soup = te.getSoup(heldItem, 1);
							if (!soup.isEmpty()) {
								heldItem.shrink(1);
								if (heldItem.isEmpty()) player.setHeldItem(hand, soup);
								else if (!player.inventory.addItemStackToInventory(soup)) {
									world.spawnEntity(new EntityItem(world, pos.getX() + 0.5D, pos.getY() + 1.5D, pos.getZ() + 0.5D, soup));
								}
								world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL_LAVA, SoundCategory.BLOCKS, 0.25F, 1.2F - world.rand.nextFloat() * 0.4F);
								return true;
							}
						} else return false;
					} else if (hand == EnumHand.MAIN_HAND) {
						IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(heldItem);
						if (fluidHandler != null) {
							FluidStack fStack = fluidHandler.drain(Fluid.BUCKET_VOLUME, false);
							if (fStack != null) {
								if (fStack.getFluid() == FluidRegistry.WATER || fStack.getFluid().getName().equals("milk")) {
									player.openGui(Mist.instance, 2, world, pos.getX(), pos.getY(), pos.getZ());
									return true;
								}
							} /*else {
								// TODO Rewrite to different volumes
								fStack = te.drain(Fluid.BUCKET_VOLUME, false);
								if (fStack != null) {
									if (!world.isRemote) {
										fluidHandler.fill(te.drain(Fluid.BUCKET_VOLUME, true), true);
										if (!player.capabilities.isCreativeMode) player.setHeldItem(hand, fluidHandler.getContainer());
									}
									return true;
								}
							}*/
						}
					}
				} else if (state.getValue(TOOL) == CookingTool.GRILL) {
					if (item instanceof ItemFood) {
						ItemStack stack = heldItem.copy();
						stack.setCount(1);
						ArrayList<ItemStack> list = te.addFood(stack, getGrillSlot(hitX, hitZ, te.getFacing()));
						if (list.get(0).isEmpty()) {
							heldItem.shrink(1);
							world.playSound(null, pos, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, SoundCategory.BLOCKS, 0.25F, 1.2F - world.rand.nextFloat() * 0.4F);
							return true;
						} else return false;
					}
				}
				int stage = state.getValue(STAGE);
				if (stage < 3) {
					if (!te.getStone().isEmpty() && item == te.getStone().getItem() && heldItem.getItemDamage() == te.getStone().getItemDamage()) {
						heldItem.shrink(1);
						int count = TileEntityCampfire.getStoneCount(te.getStone());
						count = Math.round(Math.round((stage + 1) * count / 4.0F + 1) * 4.0F / count) - 1;					
						world.setBlockState(pos, state.withProperty(STAGE, count));
						world.playSound(null, pos, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.8F, world.rand.nextFloat() * 0.4F + 0.8F);
						return true;
					}
				} else if (stage < 7) {
					if (item == Items.STICK || Block.getBlockFromItem(item).getDefaultState().getMaterial() == Material.WOOD ||
							(item instanceof ItemTool && "WOOD".equals(((ItemTool)item).getToolMaterialName())) ||
							(item instanceof ItemSword && "WOOD".equals(((ItemSword)item).getToolMaterialName())) ||
							(item instanceof ItemHoe && "WOOD".equals(((ItemHoe)item).getMaterialName()))) {
						ItemStack stack = heldItem.copy();
						stack.setCount(1);
						stack = te.addFuel(stack);
						if (stack.isEmpty()) {
							heldItem.shrink(1);
							world.setBlockState(pos, state.withProperty(STAGE, stage + 1));
							world.playSound(null, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 0.8F, world.rand.nextFloat() * 0.4F + 0.8F);
							return true;
						}
					}
				} else {
					if (item instanceof ItemFlintAndSteel) {
						heldItem.damageItem(1, player);
						world.setBlockState(pos, state.withProperty(STAGE, 8));
						world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
						te.setFire();
						return true;
					} else if (stage > 7 && item != Items.BOWL) {
						ItemStack stack = heldItem.copy();
						stack.setCount(1);
						stack = te.addFuel(stack);
						if (stack.isEmpty()) {
							heldItem.shrink(1);
							if (stage == 9) world.setBlockState(pos, state.withProperty(STAGE, 8));
							world.playSound(null, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 0.8F, world.rand.nextFloat() * 0.4F + 0.8F);
							return true;
						}
					}
				}
				if (stage >= 7 && !te.hasCookingTool()) {
					if (canPlace(world, pos)) {
						for (CookingTool tool : CookingTool.values()) {
							if (tool != CookingTool.NONE && heldItem.isItemEqual(tool.getItem())) {
								heldItem.shrink(1);
								world.playSound(null, pos, SoundEvents.BLOCK_METAL_PLACE, SoundCategory.BLOCKS, 0.8F, world.rand.nextFloat() * 0.4F + 0.8F);
								te.setCookingTool(tool);
								te.updateStatus(state.withProperty(TOOL, tool));
							}
						}
					}
					return true;
				}
			}
			if (hand == EnumHand.MAIN_HAND) {
				if (te.getCookingTool() == CookingTool.POT && te.getVolum() > 0) {
					player.openGui(Mist.instance, 3, world, pos.getX(), pos.getY(), pos.getZ());
					return true;
				}
			}
		}
		return false;
	}

	private boolean canPlace(World world, BlockPos pos) {
		for (EnumFacing face : EnumFacing.HORIZONTALS) {
			TileEntity te = world.getTileEntity(pos.offset(face));
			if (te != null && te instanceof TileEntityCampStick && !((TileEntityCampStick)te).getFood().isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private int getGrillSlot(float hitX, float hitZ, EnumFacing face) {
		int i;
		if (hitZ < 0.5) {
			if (hitX < 0.5) i = 3;
			else i = 2;
		} else {
			if (hitX < 0.5) i = 0;
			else i = 1;
		}
		return (i + face.getHorizontalIndex()) & 3;
	}

	public void extinguish(World world, BlockPos pos, boolean hasFuel) {
		IBlockState state = world.getBlockState(pos).getActualState(world, pos);
		int stage = state.getValue(STAGE);
		if (stage == 8 || stage == 9) world.setBlockState(pos, state.withProperty(STAGE, hasFuel ? 9 : 10));
	}

	@Override
	public void fillWithRain(World world, BlockPos pos) {
		if (!world.isRemote) {
			TileEntity te = world.getTileEntity(pos);
			if (te != null && te instanceof TileEntityCampfire) {
				((TileEntityCampfire)te).fillWithRain();
				int ash = ((TileEntityCampfire)te).getAshTimer();
				if (ash >= 1000) {
					IBlockState soil = world.getBlockState(pos.down());
					if (soil instanceof MistSoil) {
						int hum = SoilHelper.getHumus(soil);
						if (hum < 3) {
							if (SoilHelper.setSoil(world, pos, soil, hum + 1, 2)) {
								((TileEntityCampfire)te).setAshTimer(ash - 1000);
							}
						}
					}
				}
			}
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
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntity te = world.getTileEntity(pos);
		if (!world.isRemote && te != null && te instanceof TileEntityCampfire) {
			for (ItemStack stack : ((TileEntityCampfire)te).getDrops(state)) {
				if (!stack.isEmpty()) {
					InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
				}
			}
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		if (!world.isRemote && world.getBlockState(pos).getValue(STAGE) < 7) {
			world.destroyBlock(pos, true);
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		if (!world.isRemote) {
			if ((fromPos.equals(pos.down()) && !world.isSideSolid(fromPos, EnumFacing.UP)) ||
					world.getBlockState(fromPos).getMaterial().isLiquid()) world.destroyBlock(pos, true);
			else {
				TileEntity te = world.getTileEntity(pos);
				if (te != null && te instanceof TileEntityCampfire) {
					((TileEntityCampfire)te).setMinedStone(world.getBlockState(pos.up()).getBlock() == MistBlocks.STONE_MINED);
				}
			}
		}
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		if (!world.isRemote) {
			TileEntity te = world.getTileEntity(pos);
			if (te != null && te instanceof TileEntityCampfire) {
				((TileEntityCampfire)te).setMinedStone(world.getBlockState(pos.up()).getBlock() == MistBlocks.STONE_MINED);
			}
		}
	}

	@Override
	@Nullable
	public RayTraceResult collisionRayTrace(IBlockState blockState, World world, BlockPos pos, Vec3d start, Vec3d end) {
		List<RayTraceResult> list = Lists.<RayTraceResult> newArrayList();
		for (AxisAlignedBB axisalignedbb : getCollisionBoxList(this.getActualState(blockState, world, pos))) {
			list.add(this.rayTrace(pos, start, end, axisalignedbb));
		}
		RayTraceResult rtresult = null;
		double d1 = 0.0D;
		for (RayTraceResult raytraceresult : list) {
			if (raytraceresult != null) {
				double d0 = raytraceresult.hitVec.squareDistanceTo(end);
				if (d0 > d1) {
					rtresult = raytraceresult;
					d1 = d0;
				}
			}
		}
		return rtresult;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		if (state.getValue(STAGE) == 8) {
			double x = pos.getX() + 0.2D + rand.nextDouble() * 0.6D;
			double y = pos.getY() + rand.nextDouble() * 6.0D / 16.0D + 0.1D;
			double z = pos.getZ() + 0.2D + rand.nextDouble() * 0.6D;
			if (rand.nextDouble() < 0.2D) {
				world.playSound(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
						SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.5F, 1.0F, false);
			}
			world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0.0D, 0.0D, 0.0D);
			world.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(STAGE);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(STAGE, meta);
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntityCampfire te = (TileEntityCampfire) world.getTileEntity(pos);
		if (te != null) {
			state = state.withProperty(DIR, te.getFacing());
			if (te.hasCookingTool()) return state.withProperty(TOOL, te.getCookingTool());
			else return state.withProperty(TOOL, CookingTool.NONE);
		}
		return state;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { DIR, STAGE, TOOL });
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityCampfire();
	}

	@Override
	public boolean onShiftPlacing(World world, BlockPos pos, @Nonnull ItemStack stack, EntityPlayer player, float hitX, float hitY, float hitZ, BlockFaceShape bfs) {
		if (bfs != BlockFaceShape.SOLID) return false;
		int count = TileEntityCampfire.getStoneCount(stack);
		if (count > 0) {
			EnumFacing face = player.getHorizontalFacing().getOpposite();
			if (world.setBlockState(pos, MistBlocks.CAMPFIRE.getDefaultState().withProperty(STAGE, Math.round(4.0F/count) - 1).withProperty(DIR, face))) {
				world.playSound(null, pos, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.8F, world.rand.nextFloat() * 0.4F + 0.8F);
				TileEntityCampfire te = ((TileEntityCampfire)world.getTileEntity(pos));
				te.setStone(stack);
				te.setFacing(face);
				stack.shrink(1);
				return true;
			}
		}
		return false;
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
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	public static enum CookingTool implements IStringSerializable {

		NONE("none", 0, ItemStack.EMPTY),
		POT("pot", 1, new ItemStack(Items.CAULDRON)),
		GRILL("grill", 2, new ItemStack(Blocks.IRON_BARS));

		private final String name;
		private final int index;
		private final ItemStack item;
		private static final CookingTool[] META_LOOKUP = new CookingTool[values().length];

		CookingTool(String name, int index, ItemStack item) {
			this.name = name;
			this.index = index;
			this.item = item;
		}

		@Override
		public String getName() {
			return this.name;
		}

		public int getIndex() {
			return this.index;
		}

		public ItemStack getItem() {
			return this.item.copy();
		}

		public static CookingTool fromIndex(int index) {
			if (index < 0 || index >= META_LOOKUP.length) index = 0;
			return META_LOOKUP[index];
		}

		static {
			for (CookingTool type : values()) {
				META_LOOKUP[type.getIndex()] = type;
			}
		}
	}
}
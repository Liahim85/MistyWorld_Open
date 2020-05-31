package ru.liahim.mist.block.gizmos;

import java.util.Random;

import javax.annotation.Nullable;

import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.block.MistTreeSapling;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MistFlowerPot extends MistBlockContainer {

	public static final PropertyEnum<EnumFlowerType> CONTENTS = PropertyEnum.<EnumFlowerType>create("type", EnumFlowerType.class);
	protected static final AxisAlignedBB FLOWER_POT_AABB = new AxisAlignedBB(0.3125D, 0.0D, 0.3125D, 0.6875D, 0.375D, 0.6875D);

	public MistFlowerPot() {
		super(Material.CIRCUITS);
		this.setDefaultState(this.blockState.getBaseState().withProperty(CONTENTS, EnumFlowerType.EMPTY));
		this.setTickRandomly(true);
	}

	@Override
	public String getLocalizedName() {
		return Blocks.FLOWER_POT.getLocalizedName();
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return FLOWER_POT_AABB;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		TileEntityFlowerPot te = MistFlowerPot.getTileEntity(world, pos);
		if (te == null) return false;
		ItemStack flower = te.getFlowerItemStack();
		if (!flower.isEmpty()) {
			if (stack.isEmpty()) player.setHeldItem(hand, flower);
			else if (!player.addItemStackToInventory(flower)) player.dropItem(flower, false);
			world.setBlockState(pos, Blocks.FLOWER_POT.getDefaultState());
		}
		return true;
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (rand.nextInt(8) == 0) {
			TileEntityFlowerPot te = MistFlowerPot.getTileEntity(world, pos);
			if (te != null && te.getFlowerPotItem() == MistItems.TREE_SEEDS) {
				IBlockState newState;
				boolean grow = false;
				int i = state.getValue(CONTENTS).ordinal();
				if (i < 3) newState = state.withProperty(CONTENTS, EnumFlowerType.values()[i + 1]);
				else {
					newState = this.getDefaultState();
					grow = true;
				}
				world.setBlockState(pos, newState, 3);
				if (grow) {
					te.setItemStack(new ItemStack(MistBlocks.TREE_SAPLING, 1, te.getFlowerPotData()));
					te.markDirty();
					world.notifyBlockUpdate(pos, state, newState, 3);
				}
			}
		}
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		EnumFlowerType type = EnumFlowerType.EMPTY;
		TileEntity te = world instanceof ChunkCache ? ((ChunkCache)world).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) : world.getTileEntity(pos);
		if (te instanceof TileEntityFlowerPot) {
			TileEntityFlowerPot tePot = (TileEntityFlowerPot) te;
			Item item = tePot.getFlowerPotItem();
			int i = tePot.getFlowerPotData();
			if (item instanceof ItemBlock) {
				Block block = Block.getBlockFromItem(item);
				if (block == MistBlocks.TREE_SAPLING) {
					switch (MistTreeSapling.EnumType.byMeta(i)) {
					case ACACIA:
						type = EnumFlowerType.ACACIA_SAPLING;
						break;
					case ASPEN:
						type = EnumFlowerType.ASPEN_SAPLING;
						break;
					case ATREE:
						type = EnumFlowerType.ATREE_SAPLING;
						break;
					case BIRCH:
						type = EnumFlowerType.BIRCH_SAPLING;
						break;
					case OAK:
						type = EnumFlowerType.OAK_SAPLING;
						break;
					case PINE:
						type = EnumFlowerType.PINE_SAPLING;
						break;
					case POPLAR:
						type = EnumFlowerType.POPLAR_SAPLING;
						break;
					case SNOW:
						type = EnumFlowerType.SNOW_SAPLING;
						break;
					case SPRUCE:
						type = EnumFlowerType.SPRUCE_SAPLING;
						break;
					case STREE:
						type = EnumFlowerType.STREE_SAPLING;
						break;
					case TTREE:
						type = EnumFlowerType.TTREE_SAPLING;
						break;
					case WILLOW:
						type = EnumFlowerType.WILLOW_SAPLING;
						break;
					case RTREE:
						type = EnumFlowerType.R_TREE_SAPLING;
						break;
					default:
						type = EnumFlowerType.EMPTY;
					}
				}
			} else {
				if (item == MistItems.TREE_SEEDS) {
					type = EnumFlowerType.SEED_0;
				} else if (item == MistItems.MUSHROOMS_FOOD) {
					switch (i) {
					case 0:
						type = EnumFlowerType.BROWN_MUSHROOM;
						break;
					case 1:
						type = EnumFlowerType.BLACK_MUSHROOM;
						break;
					case 2:
						type = EnumFlowerType.GRAY_MUSHROOM;
						break;
					case 3:
						type = EnumFlowerType.RED_MUSHROOM;
						break;
					case 4:
						type = EnumFlowerType.CORAL_MUSHROOM;
						break;
					case 5:
						type = EnumFlowerType.ORANGE_MUSHROOM;
						break;
					case 6:
						type = EnumFlowerType.YELLOW_MUSHROOM;
						break;
					case 7:
						type = EnumFlowerType.BLUE_MUSHROOM;
						break;
					case 8:
						type = EnumFlowerType.PURPLE_MUSHROOM;
						break;
					case 9:
						type = EnumFlowerType.MARSH_MUSHROOM;
						break;
					case 10:
						type = EnumFlowerType.PINK_MUSHROOM;
						break;
					case 11:
						type = EnumFlowerType.PUFF_MUSHROOM;
						break;
					case 12:
						type = EnumFlowerType.SAND_MUSHROOM;
						break;
					case 16:
						type = EnumFlowerType.SPOT_MUSHROOM;
						break;
					case 17:
						type = EnumFlowerType.CUP_MUSHROOM;
						break;
					case 18:
						type = EnumFlowerType.AZURE_MUSHROOM;
						break;
					case 19:
						type = EnumFlowerType.GREEN_MUSHROOM;
						break;
					case 20:
						type = EnumFlowerType.COPPER_MUSHROOM;
						break;
					case 21:
						type = EnumFlowerType.SILVER_MUSHROOM;
						break;
					case 22:
						type = EnumFlowerType.BEIGE_MUSHROOM;
						break;
					case 23:
						type = EnumFlowerType.GOLD_MUSHROOM;
						break;
					case 24:
						type = EnumFlowerType.WHITE_MUSHROOM;
						break;
					case 25:
						type = EnumFlowerType.VIOLET_MUSHROOM;
						break;
					case 26:
						type = EnumFlowerType.LILAC_MUSHROOM;
						break;
					case 27:
						type = EnumFlowerType.TAN_MUSHROOM;
						break;
					default:
						type = EnumFlowerType.EMPTY;
					}
				} else if (item == MistItems.SPONGE_SPORE) {
					type = EnumFlowerType.SPONGE;
				}
			}
		}
		return state.withProperty(CONTENTS, type);
	}

	public static boolean canBePotted(ItemStack stack) {
		Item item = stack.getItem();
		if (item instanceof ItemBlock) {
			Block block = Block.getBlockFromItem(item);
			return block == MistBlocks.TREE_SAPLING;
		} else {
			return item == MistItems.TREE_SEEDS || item == MistItems.MUSHROOMS_FOOD || item == MistItems.SPONGE_SPORE;
		}
	}

	@Override
	public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
		TileEntityFlowerPot te = MistFlowerPot.getTileEntity(world, pos);
		if (te != null) {
			ItemStack stack = te.getFlowerItemStack();
			if (!stack.isEmpty()) return stack;
		}
		return new ItemStack(Items.FLOWER_POT);
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos) {
		return super.canPlaceBlockAt(world, pos) && world.isSideSolid(pos.down(), EnumFacing.UP);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		if (!world.isSideSolid(pos.down(), EnumFacing.UP)) {
			this.dropBlockAsItem(world, pos, state, 0);
			world.setBlockToAir(pos);
		}
	}

	@Override
	public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		super.onBlockHarvested(world, pos, state, player);
		if (player.capabilities.isCreativeMode) {
			TileEntityFlowerPot tileentityflowerpot = MistFlowerPot.getTileEntity(world, pos);
			if (tileentityflowerpot != null) tileentityflowerpot.setItemStack(ItemStack.EMPTY);
		}
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Items.FLOWER_POT;
	}

	@Nullable
	public static TileEntityFlowerPot getTileEntity(World world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		return te instanceof TileEntityFlowerPot ? (TileEntityFlowerPot) te : null;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityFlowerPot();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { CONTENTS });
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return meta <= 3 ? this.getDefaultState().withProperty(CONTENTS, EnumFlowerType.values()[meta]) : this.getDefaultState();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int i = state.getValue(CONTENTS).ordinal();
		return i <= 3 ? i : 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		super.getDrops(drops, world, pos, state, fortune);
		TileEntityFlowerPot te = world.getTileEntity(pos) instanceof TileEntityFlowerPot ? (TileEntityFlowerPot) world.getTileEntity(pos) : null;
		if (te != null && te.getFlowerPotItem() != null) drops.add(new ItemStack(te.getFlowerPotItem(), 1, te.getFlowerPotData()));
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		if (willHarvest) return true;
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack tool) {
		super.harvestBlock(world, player, pos, state, te, tool);
		world.setBlockToAir(pos);
	}

	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		return false;
	}

	public static enum EnumFlowerType implements IStringSerializable {

		EMPTY("empty"),
		SEED_0("seed"),
		SEED_1("seed_1"),
		SEED_2("seed_2"),
		ACACIA_SAPLING("acacia_sapling"),
		ASPEN_SAPLING("aspen_sapling"),
		ATREE_SAPLING("a_tree_sapling"),
		BIRCH_SAPLING("birch_sapling"),
		OAK_SAPLING("oak_sapling"),
		PINE_SAPLING("pine_sapling"),
		POPLAR_SAPLING("poplar_sapling"),
		SNOW_SAPLING("snow_sapling"),
		SPRUCE_SAPLING("spruce_sapling"),
		STREE_SAPLING("s_tree_sapling"),
		TTREE_SAPLING("t_tree_sapling"),
		WILLOW_SAPLING("willow_sapling"),
		R_TREE_SAPLING("r_tree_sapling"),
		BROWN_MUSHROOM("brown_mushroom"),
		BLACK_MUSHROOM("black_mushroom"),
		GRAY_MUSHROOM("gray_mushroom"),
		RED_MUSHROOM("red_mushroom"),
		CORAL_MUSHROOM("coral_mushroom"),
		ORANGE_MUSHROOM("orange_mushroom"),
		YELLOW_MUSHROOM("yellow_mushroom"),
		BLUE_MUSHROOM("blue_mushroom"),
		PURPLE_MUSHROOM("purple_mushroom"),
		MARSH_MUSHROOM("marsh_mushroom"),
		PINK_MUSHROOM("pink_mushroom"),
		PUFF_MUSHROOM("puff_mushroom"),
		SAND_MUSHROOM("sand_mushroom"),
		SPOT_MUSHROOM("spot_mushroom"),
		CUP_MUSHROOM("cup_mushroom"),
		AZURE_MUSHROOM("azure_mushroom"),
		GREEN_MUSHROOM("green_mushroom"),
		COPPER_MUSHROOM("copper_mushroom"),
		SILVER_MUSHROOM("silver_mushroom"),
		BEIGE_MUSHROOM("beige_mushroom"),
		GOLD_MUSHROOM("gold_mushroom"),
		WHITE_MUSHROOM("white_mushroom"),
		VIOLET_MUSHROOM("violet_mushroom"),
		LILAC_MUSHROOM("lilac_mushroom"),
		TAN_MUSHROOM("tan_mushroom"),
		SPONGE("sponge");

		private final String name;

		private EnumFlowerType(String name) {
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
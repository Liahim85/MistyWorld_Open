package ru.liahim.mist.block;

import ru.liahim.mist.api.block.IColoredBlock;
import ru.liahim.mist.api.block.IDividable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.init.BlockColoring;
import ru.liahim.mist.util.FacingHelper;
import ru.liahim.mist.world.MistWorld;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**@author Liahim*/
public class MistBlockSlabStone extends MistBlockSlab implements IDividable, IColoredBlock {

	@Override
	@SideOnly(Side.CLIENT)
	public IBlockColor getBlockColor() {
		return BlockColoring.GRASS_COLORING_1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IItemColor getItemColor() {
		return BlockColoring.BLOCK_ITEM_COLORING;
	}

	public static final PropertyEnum<EnumType> VARIANT = PropertyEnum.<EnumType>create("variant", EnumType.class);

	public MistBlockSlabStone(Block fullBlock, float hardness, float resistance) {
		super(fullBlock, Material.ROCK, hardness, resistance);
		this.setDefaultState(this.blockState.getBaseState().withProperty(HALF, BlockSlab.EnumBlockHalf.BOTTOM).withProperty(VARIANT, EnumType.NORMAL));
		this.setTickRandomly(true);
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		IBlockState state = this.getDefaultState().withProperty(VARIANT, EnumType.byMetadata(meta));
		return this.isDouble() ? state : (facing != EnumFacing.DOWN && (facing == EnumFacing.UP || hitY <= 0.5D) ? state : state.withProperty(HALF, BlockSlab.EnumBlockHalf.TOP));
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (!world.isRemote) {
			if (state.getValue(VARIANT) == EnumType.MOSSY) {
				if (rand.nextInt(4) == 0 && MistWorld.isPosInFog(world, pos.getY())) {
					world.setBlockState(pos, this.getDefaultState());
				}
			} else if (rand.nextInt(500) == 0 && !MistWorld.isPosInFog(world, pos.getY()) && world.getBiome(pos).getRainfall() >= 0.3F) {
				boolean up = state.getValue(HALF) == EnumBlockHalf.TOP;
				EnumFacing[] faces = up ? FacingHelper.NOTDOWN : EnumFacing.HORIZONTALS;
				for (EnumFacing side : faces) {
					if (world.getBlockState(pos.offset(side)).getBlock() == MistBlocks.ACID_BLOCK) {
						return;
					}
				}
				boolean check = false;
				if (!world.isSideSolid(pos.up(), EnumFacing.DOWN)) {
					for (EnumFacing side : EnumFacing.HORIZONTALS) {
						if (world.getBlockState(pos.offset(side)).getBlock() instanceof MistGrass) {
							check = true;
							break;
						}
					}
				}
				if (!up && !check) {
					for (EnumFacing side : EnumFacing.HORIZONTALS) {
						if (!world.isSideSolid(pos.offset(side), side.getOpposite()) && world.getBlockState(pos.offset(side).down()).getBlock() instanceof MistGrass) {
							check = true;
							break;
						}
					}
				}
				if (check) world.setBlockState(pos, this.getDefaultState().withProperty(VARIANT, EnumType.MOSSY).withProperty(HALF, state.getValue(HALF)));
			}
		}
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		if (!world.isRemote && this == MistBlocks.COBBLESTONE_MOSS_STEP) {
			EnumFacing[] faces = state.getValue(HALF) == EnumBlockHalf.TOP ? FacingHelper.NOTDOWN : EnumFacing.HORIZONTALS;
			for (EnumFacing side : faces) {
				if (world.getBlockState(pos.offset(side)).getBlock() == MistBlocks.ACID_BLOCK) {
					world.setBlockState(pos, MistBlocks.COBBLESTONE_STEP.getDefaultState().withProperty(VARIANT, EnumType.NORMAL).withProperty(HALF, state.getValue(HALF)));
					break;
				}
			}
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		if (!world.isRemote && this == MistBlocks.COBBLESTONE_MOSS_STEP) {
			EnumFacing[] faces = state.getValue(HALF) == EnumBlockHalf.TOP ? FacingHelper.NOTDOWN : EnumFacing.HORIZONTALS;
			for (EnumFacing side : faces) {
				if (world.getBlockState(pos.offset(side)).getBlock() == MistBlocks.ACID_BLOCK) {
					world.setBlockState(pos, MistBlocks.COBBLESTONE_STEP.getDefaultState().withProperty(VARIANT, EnumType.NORMAL).withProperty(HALF, state.getValue(HALF)));
					break;
				}
			}
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(VARIANT, EnumType.byMetadata(meta % 8)).withProperty(HALF, meta < 8 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return (state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP ? 8 : 0) | state.getValue(VARIANT).getMetadata();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {HALF, VARIANT});
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		return layer == BlockRenderLayer.CUTOUT_MIPPED;
    }

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		list.add(new ItemStack(this, 1, 0));
		list.add(new ItemStack(this, 1, 1));
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(state.getBlock(), 1, state.getValue(VARIANT).getMetadata());
	}

	public static enum EnumType implements IStringSerializable {

		NORMAL(0, "normal"),
		MOSSY(1, "moss");

		private static final EnumType[] META_LOOKUP = new EnumType[values().length];
		private final int meta;
		private final String name;

		private EnumType(int meta, String name) {
			this.meta = meta;
			this.name = name;
		}

		public int getMetadata() {
			return this.meta;
		}

		public static EnumType byMetadata(int meta) {
			if (meta < 0 || meta >= META_LOOKUP.length) meta = 0;
			return META_LOOKUP[meta];
		}

		@Override
		public String getName() {
			return this.name;
		}

		static {
			for (EnumType type : values()) {
				META_LOOKUP[type.getMetadata()] = type;
			}
		}
	}
}
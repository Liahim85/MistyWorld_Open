package ru.liahim.mist.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.creativetab.CreativeTabs;
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
import ru.liahim.mist.api.block.IColoredBlock;
import ru.liahim.mist.api.block.IMossable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.init.BlockColoring;
import ru.liahim.mist.util.FacingHelper;
import ru.liahim.mist.world.MistWorld;

public class MistBlockMossy extends MistBlock implements IColoredBlock, IMossable {

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

	public MistBlockMossy() {
		super(Material.ROCK);
		this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, EnumType.NORMAL));
		this.setHardness(3.0F);
		this.setResistance(10.0F);
		this.setTickRandomly(true);
		this.setHarvestLevel("pickaxe", 0);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (!world.isRemote) {
			if (state.getValue(VARIANT) == EnumType.MOSSY) {
				if (rand.nextInt(4) == 0 && MistWorld.isPosInFog(world, pos.getY())) {
					world.setBlockState(pos, this.getDefaultState());
				}
			} else if (rand.nextInt(500) == 0 && !MistWorld.isPosInFog(world, pos.getY()) && world.getBiome(pos).getRainfall() >= 0.3F) {
				for (EnumFacing face : FacingHelper.NOTDOWN) {
					if (world.getBlockState(pos.offset(face)).getBlock() == MistBlocks.ACID_BLOCK) {
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
				if (!check) {
					for (EnumFacing side : EnumFacing.HORIZONTALS) {
						if (!world.isSideSolid(pos.offset(side), side.getOpposite()) && world.getBlockState(pos.offset(side).down()).getBlock() instanceof MistGrass) {
							check = true;
							break;
						}
					}
				}
				if (check) world.setBlockState(pos, this.getDefaultState().withProperty(VARIANT, EnumType.MOSSY));
			}
		}
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		if (!world.isRemote && state.getValue(VARIANT) == EnumType.MOSSY) {
			for (EnumFacing face : FacingHelper.NOTDOWN) {
				if (world.getBlockState(pos.offset(face)).getBlock() == MistBlocks.ACID_BLOCK) {
					world.setBlockState(pos, this.getDefaultState());
					break;
				}
			}
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		if (!world.isRemote && fromPos.getY() >= pos.getY() && state.getValue(VARIANT) == EnumType.MOSSY && world.getBlockState(fromPos).getBlock() == MistBlocks.ACID_BLOCK) {
			world.setBlockState(pos, this.getDefaultState());
		}
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		list.add(new ItemStack(this, 1, 0));
		list.add(new ItemStack(this, 1, 1));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(VARIANT).getMetadata();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(VARIANT, EnumType.byMetadata(meta));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, VARIANT);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		return state.getValue(VARIANT) == EnumType.MOSSY ? layer == BlockRenderLayer.CUTOUT_MIPPED : layer == BlockRenderLayer.SOLID;
    }

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(this, 1, state.getValue(VARIANT).getMetadata());
	}

	@Override
	public boolean setMossy(IBlockState state, World world, BlockPos pos) {
		if (!(state.getBlock() instanceof IMossable) || state.getValue(VARIANT) == EnumType.MOSSY) return false;
		return world.setBlockState(pos, state.withProperty(VARIANT, EnumType.MOSSY));
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
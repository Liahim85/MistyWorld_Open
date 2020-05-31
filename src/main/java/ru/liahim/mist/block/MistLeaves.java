package ru.liahim.mist.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ru.liahim.mist.api.block.IColoredBlock;
import ru.liahim.mist.init.BlockColoring;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**@author Liahim*/
public class MistLeaves extends BlockLeaves implements IColoredBlock {

	@Override
	@SideOnly(Side.CLIENT)
	public IBlockColor getBlockColor() {
		return new IBlockColor() {
			@Override
			public int colorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex) {
				int baseColor = ((MistLeaves)state.getBlock()).getBaseColor();
				return world != null && pos != null ? tintIndex == 0 ? ((MistLeaves)state.getBlock()).getMixColor(world, pos) : 0xFFFFFFFF : baseColor;
			}
		};
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IItemColor getItemColor() {
		return BlockColoring.BLOCK_ITEM_COLORING;
	}

	public static final PropertyDirection DIR = PropertyDirection.create("dir");
	private final int baseColor;
	private final boolean mixColor;
	protected MistTreeTrunk trunkBlock;
	public final int bloomMonth;
	public final int spoilMonth;

	public MistLeaves(int baseColor, boolean mixColor, int bloomMonth, int spoilMonth) {
		super();
		this.baseColor = baseColor;
		this.mixColor = mixColor;
		this.bloomMonth = bloomMonth;
		this.spoilMonth = spoilMonth;
		this.setDefaultState(this.blockState.getBaseState().withProperty(DIR, EnumFacing.DOWN));
		//this.useNeighborBrightness = true;
	}

	@Override
	public String getUnlocalizedName() {
		return "tile.mist." + super.getUnlocalizedName().substring(5);
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
		float hitZ, int meta, EntityLivingBase placer) {
		if (placer instanceof EntityPlayer && ((EntityPlayer)placer).isCreative() && facing != EnumFacing.DOWN &&
				world.getBlockState(pos.offset(facing.getOpposite())).getBlock() == this.trunkBlock)
			return this.getDefaultState().withProperty(DIR, facing);
		else return this.getDefaultState();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(DIR).getIndex();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(DIR, EnumFacing.getFront(meta));
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(DIR, rot.rotate(state.getValue(DIR)));
    }

	@Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(DIR)));
    }

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { DIR });
	}

    public void setTrunkBlock(MistTreeTrunk trunkBlock) {
		this.trunkBlock = trunkBlock;
	}

	public Block getTrunkBlock() {
		return this.trunkBlock;
	}

	private int getBaseColor() {
		return this.baseColor;
	}

	private int getMixColor(IBlockAccess world, BlockPos pos) {
		if (this.mixColor) {
			int biomeColor = BiomeColorHelper.getFoliageColorAtPos(world, pos);				
			int r = (((biomeColor >> 16) & 255) + ((this.baseColor >> 16) & 255) * 2)/3;
			int g = (((biomeColor >> 8) & 255) + ((this.baseColor >> 8) & 255) * 2)/3;
			int b = ((biomeColor & 255) + (this.baseColor & 255) * 2)/3;
			return r << 16 | g << 8 | b;
		} else return this.baseColor;
	}

	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 60;
    }

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 30;
    }

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (!world.isRemote) {
			EnumFacing dir = state.getValue(DIR);
			if (dir != EnumFacing.DOWN && world.getBlockState(pos.offset(dir.getOpposite())).getBlock() != this.trunkBlock)
				world.setBlockToAir(pos);
		}
	}

	public void updateLeaves(World world, BlockPos pos, IBlockState state, BlockPos rootPos, IBlockState rootState,
			BlockPos soilPos, IBlockState soil, Random rand) {}

	@Override
	public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
		List<ItemStack> ret = new ArrayList<ItemStack>();
		ret.add(new ItemStack(this));
		return ret;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	////////////////////////////////////// Overrides //////////////////////////////////////

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return Blocks.LEAVES.getBlockLayer();
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return Blocks.LEAVES.isOpaqueCube(state);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return Blocks.LEAVES.shouldSideBeRendered(state, world, pos, side);
	}

	@Override
	public EnumType getWoodType(int meta) {
		return null;
	}

	@Override public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {}
	@Override public void beginLeavesDecay(IBlockState state, World world, BlockPos pos) {}
	@Override public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {}
}
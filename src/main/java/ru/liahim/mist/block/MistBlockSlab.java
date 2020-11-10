package ru.liahim.mist.block;

import java.util.Random;

import javax.annotation.Nullable;

import ru.liahim.mist.api.block.IDividable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**@author Liahim*/
public class MistBlockSlab extends BlockSlab implements IDividable {

	private final Block fullBlock;

	public MistBlockSlab(Block fullBlock, Material material, float hardness, float resistance) {
		super(material);
		this.setHardness(hardness);
		if (resistance > 0) this.setResistance(resistance);
		this.setDefaultState(this.blockState.getBaseState().withProperty(HALF, BlockSlab.EnumBlockHalf.BOTTOM));
		this.useNeighborBrightness = true;
		this.fullBlock = fullBlock;
	}

	@Override
	public String getUnlocalizedName() {
		return "tile.mist." + super.getUnlocalizedName().substring(5);
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		IBlockState state = this.getDefaultState();
		return this.isDouble() ? state : (facing != EnumFacing.DOWN && (facing == EnumFacing.UP || hitY <= 0.5D) ? state : state.withProperty(HALF, BlockSlab.EnumBlockHalf.TOP));
	}

	@Override
	public int quantityDropped(IBlockState state, int fortune, Random random) {
		return 2;
	}

	@Override
	@Nullable
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return this.getStepBlock(state).getItemDropped(state, rand, fortune);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(HALF, (meta & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP ? 8 : 0;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {HALF});
	}

	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		return true;
	}

	@Override
	public Block getFullBlock() {
		return this.fullBlock;
	}

	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return this.fullBlock.getFlammability(world, pos, face);
	}

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return this.fullBlock.getFireSpreadSpeed(world, pos, face);
	}

	public static IBlockState getClearSlabState(IBlockState state) {
		state = state.withProperty(BlockSlab.HALF, EnumBlockHalf.BOTTOM);
		if (state.getBlock() instanceof MistBlockSlabWood) state = state.withProperty(MistBlockSlabWood.ISROT, false);
		return state;
	}

	//////////////////////////////

	@Override
	public String getUnlocalizedName(int meta) { return null; }

	@Override
	public boolean isDouble() { return false; }

	@Override
	public IProperty<?> getVariantProperty() { return null; }

	@Override
	public Comparable<?> getTypeForItem(ItemStack stack) { return null; }
}
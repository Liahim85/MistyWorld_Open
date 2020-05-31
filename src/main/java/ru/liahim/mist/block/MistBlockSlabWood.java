package ru.liahim.mist.block;

import ru.liahim.mist.api.block.IDividable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**@author Liahim*/
public class MistBlockSlabWood extends MistBlockSlab implements IDividable {

	public static final PropertyBool ISROT = PropertyBool.create("isrot");

	public MistBlockSlabWood(Block fullBlock, float hardness) {
		super(fullBlock, Material.WOOD, hardness, 0);
		this.setDefaultState(this.blockState.getBaseState().withProperty(HALF, BlockSlab.EnumBlockHalf.BOTTOM).withProperty(ISROT, false));
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		IBlockState iblockstate = this.getDefaultState();
		EnumFacing rot = placer.getHorizontalFacing();
		if (rot == EnumFacing.EAST || rot == EnumFacing.WEST) iblockstate = iblockstate.withProperty(ISROT, true);
		return this.isDouble() ? iblockstate : (facing != EnumFacing.DOWN && (facing == EnumFacing.UP || hitY <= 0.5D) ? iblockstate : iblockstate.withProperty(HALF, BlockSlab.EnumBlockHalf.TOP));
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(ISROT, (meta & 1) != 0).withProperty(HALF, (meta & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return (state.getValue(ISROT) ? 1 : 0) + (state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP ? 8 : 0);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {HALF, ISROT});
	}
}
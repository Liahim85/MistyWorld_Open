package ru.liahim.mist.block;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.item.MistItems;

public class MistMasonry extends MistBlockMossy {

	public static final PropertyEnum<Connection> CONNECTION = PropertyEnum.<Connection>create("connect", Connection.class);

	public MistMasonry() {
		super();
		this.setDefaultState(this.blockState.getBaseState().withProperty(CONNECTION, Connection.COMMON).withProperty(VARIANT, EnumType.NORMAL));
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		for (int i = 0; i < 2; i++) {
			drops.add(new ItemStack(MistItems.ROCKS));
			drops.add(new ItemStack(MistItems.BRICK));
		}
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		int count = 0;
		EnumFacing dir = null;
		for (EnumFacing face : EnumFacing.HORIZONTALS) {
			if (isMasonry(world.getBlockState(pos.offset(face)))) {
				++count;
				if (count > 2) return state.withProperty(CONNECTION, Connection.COMMON);
				if (dir == null) dir = face;
				else if (dir == face.getOpposite()) return state.withProperty(CONNECTION, Connection.COMMON);
				if (count == 2 && face == EnumFacing.EAST && dir == EnumFacing.SOUTH) dir = EnumFacing.EAST;
			}
		}
		return state.withProperty(CONNECTION, Connection.values()[count == 0 ? 9 : (--count << 2 | dir.getHorizontalIndex()) + 1]);
	}

	protected boolean isMasonry(IBlockState state) {
		return state.getBlock() == this || state.getBlock() == MistBlocks.COBBLESTONE;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, CONNECTION, VARIANT);
	}

	public static enum Connection implements IStringSerializable {

		COMMON("common"),
		SOUTH("s"),
		WEST("w"),
		NORTH("n"),
		EAST("e"),
		SOUTH_WEST("sw"),
		WEST_NORTH("wn"),
		NORTH_EAST("ne"),
		EAST_SOUTH("es"),
		SINGLE("single");

		private final String name;

		private Connection(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return this.name;
		}
	}
}
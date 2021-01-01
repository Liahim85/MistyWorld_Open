package ru.liahim.mist.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import ru.liahim.mist.api.block.IWettable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.init.ModConfig;
import ru.liahim.mist.world.MistWorld;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class MistTreeSapling extends BlockBush implements IGrowable {

	public static final PropertyEnum<EnumType> TYPE = PropertyEnum.<EnumType>create("type", EnumType.class);
	protected static final AxisAlignedBB SAPLING_AABB = new AxisAlignedBB(0.1D, 0.0D, 0.1D, 0.9D, 0.8D, 0.9D);

	public MistTreeSapling() {
		super(Material.PLANTS);
		this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, EnumType.OAK));
		this.setSoundType(SoundType.PLANT);
        this.setTickRandomly(true);
	}

	@Override
	public String getUnlocalizedName() {
		return "tile.mist." + super.getUnlocalizedName().substring(5);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return SAPLING_AABB;
	}

	@Override
	public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
		return ModConfig.dimension.enableUseBoneMeal && !MistWorld.isPosInFog(world, pos.getY());
	}

	@Override
	public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
		return true;
	}

	@Override
	public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
		this.updateTick(world, pos, state, rand);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (!world.isRemote) {
			MistTreeTrunk tree = state.getValue(TYPE).tree;
			if (rand.nextInt(tree.getGrowthSpeed() * 2) == 0) {
				if (MistWorld.isPosInFog(world, pos)) {
					world.destroyBlock(pos, false);
				} else {
					IBlockState soil = world.getBlockState(pos.down());
					int i = tree.canGrowth(world, pos, tree.getDefaultState(), 0, EnumFacing.UP, new ArrayList<EnumFacing>(),
							true, 1, 0, 0, 0, 0, 1, 5, 5, 10, new ArrayList<Integer>(), new ArrayList<BlockPos>(),
							null, pos, state, pos.down(), soil, rand); 
					if (i > 0) {
						world.setBlockState(pos, tree.getDefaultState());
						world.setBlockState(pos.up(), tree.leaves.getDefaultState().withProperty(MistTreeTrunk.LDIR, EnumFacing.UP).withProperty(MistTreeLeaves.AGE, MistTreeLeaves.EnumAge.EMPTY));
						tree.removeHumus(world, pos.down(), world.getBlockState(pos.down()));
					} else if (tree.isDesertTree()) {
						if (soil instanceof IWettable && !soil.getValue(IWettable.WET) && MistWorld.getHumi(world, pos, 0) >= 100) {
							world.setBlockState(pos.down(), soil.withProperty(IWettable.WET, true));
						}
					} else if (i < 0) world.setBlockToAir(pos);
				}
			} else if (tree.isDesertTree()) {
				IBlockState soil = world.getBlockState(pos.down());
				if (soil instanceof IWettable && !soil.getValue(IWettable.WET) && MistWorld.getHumi(world, pos, 0) >= 100) {
					world.setBlockState(pos.down(), soil.withProperty(IWettable.WET, true));
				}
			}
		}
	}

	@Override
	public int damageDropped(IBlockState state) {
		return getMetaFromState(state);
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		for (EnumType type : EnumType.values()) {
			list.add(new ItemStack(this, 1, type.getMeta()));
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(TYPE, EnumType.byMeta(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(TYPE).getMeta();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {TYPE});
	}

	public static enum EnumType implements IStringSerializable {

		ACACIA("acacia", 0, (MistTreeTrunk)MistBlocks.ACACIA_TRUNK),
		ASPEN("aspen", 1, (MistTreeTrunk)MistBlocks.ASPEN_TRUNK),
		ATREE("a_tree", 2, (MistTreeTrunk)MistBlocks.A_TREE_TRUNK),
		BIRCH("birch", 3, (MistTreeTrunk)MistBlocks.BIRCH_TRUNK),
		OAK("oak", 4, (MistTreeTrunk)MistBlocks.OAK_TRUNK),
		PINE("pine", 5, (MistTreeTrunk)MistBlocks.PINE_TRUNK),
		POPLAR("poplar", 6, (MistTreeTrunk)MistBlocks.POPLAR_TRUNK),
		SNOW("snow", 7, (MistTreeTrunk)MistBlocks.SNOW_TRUNK),
		SPRUCE("spruce", 8, (MistTreeTrunk)MistBlocks.SPRUSE_TRUNK),
		STREE("s_tree", 9, (MistTreeTrunk)MistBlocks.S_TREE_TRUNK),
		TTREE("t_tree", 10, (MistTreeTrunk)MistBlocks.T_TREE_TRUNK),
		WILLOW("willow", 11, (MistTreeTrunk)MistBlocks.WILLOW_TRUNK),
		RTREE("r_tree", 12, (MistTreeTrunk)MistBlocks.R_TREE_TRUNK);

		private static final EnumType[] META_LOOKUP = new EnumType[values().length];
		private static final HashMap<MistTreeTrunk, EnumType> TREE_LOOKUP = new HashMap<MistTreeTrunk, EnumType>();
		private final String name;
		private final int meta;
		private final MistTreeTrunk tree;

		private EnumType(String name, int meta, MistTreeTrunk tree) {
			this.name = name;
			this.meta = meta;
			this.tree = tree;
		}

		public int getMeta() {
			return this.meta;
		}

		public MistTreeTrunk getTree() {
			return this.tree;
		}

		@Override
		public String toString() {
			return this.name;
		}

		public static EnumType byMeta(int meta) {
			if (meta < 0 || meta >= META_LOOKUP.length) meta = 0;
			return META_LOOKUP[meta];
		}

		public static int getMetaByTree(MistTreeTrunk tree) {
			if (TREE_LOOKUP.containsKey(tree)) return TREE_LOOKUP.get(tree).getMeta();
			return 0;
		}

		public static EnumType getTypeByTree(MistTreeTrunk tree) {
			if (TREE_LOOKUP.containsKey(tree)) return TREE_LOOKUP.get(tree);
			return EnumType.OAK;
		}

		@Override
		public String getName() {
			return this.name;
		}

		static {
			for (EnumType type : values()) {
				META_LOOKUP[type.getMeta()] = type;
				TREE_LOOKUP.put(type.getTree(), type);
			}
		}
	}
}
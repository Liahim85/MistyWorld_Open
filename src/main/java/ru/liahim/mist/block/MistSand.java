package ru.liahim.mist.block;

import java.util.Random;

import net.minecraft.block.BlockSand;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MistSand extends MistBlockWettableFalling {

	public static final PropertyEnum<BlockSand.EnumType> VARIANT = BlockSand.VARIANT;

	public MistSand() {
		super(Material.SAND, 3);
		this.setSoundType(SoundType.SAND);
		this.setHardness(0.5F);
		this.setHarvestLevel("shovel", 0);
		this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockSand.EnumType.SAND).withProperty(WET, false));
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected boolean showPorosityTooltip() { return false; }

	@Override
	public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction,
		IPlantable plantable) {
		EnumPlantType plantType = plantable.getPlantType(world, pos.offset(direction));
		switch (plantType) {
		case Desert:
		case Cave:
			return true;
		case Beach:
			return (world.getBlockState(pos.east()).getMaterial() == Material.WATER ||
					world.getBlockState(pos.west()).getMaterial() == Material.WATER ||
					world.getBlockState(pos.north()).getMaterial() == Material.WATER || world.getBlockState(pos.south()).getMaterial() == Material.WATER);
		default:
			return false;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		for (int i = 0; i < 4; ++i) {
			list.add(new ItemStack(this, 1, (i & 1) == 0 ? i + 1 : i - 1));
		}
	}
	
	@Override
	public void getDrops(NonNullList ret, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		Random rand = world instanceof World ? ((World)world).rand : RANDOM;
		if (rand.nextInt(128) == 0) ret.add(new ItemStack(Items.BONE));
		else {
			int count = quantityDropped(state, fortune, rand);
			for (int i = 0; i < count; i++) {
				Item item = this.getItemDropped(state, rand, fortune);
				if (item != null) {
					ret.add(new ItemStack(item, 1, this.damageDropped(state)));
				}
			}
		}
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return (state.getValue(VARIANT).getMetadata() << 1) | (state.getValue(WET) ? 1 : 0);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(VARIANT, BlockSand.EnumType.byMetadata(meta >> 1)).withProperty(WET, (meta & 1) == 1);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { VARIANT, WET });
	}

	@Override
	public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
		return state.getValue(VARIANT).getMapColor();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getDustColor(IBlockState state) {
		return state.getValue(VARIANT) == BlockSand.EnumType.SAND ? -3618641 : -5679071;
	}

	@Override
	public int getTopProtectPercent(IBlockState state, boolean isWet) {
		return isWet ? 60 : 145;
	}

	@Override
	public int getSideProtectPercent(IBlockState state, boolean isWet) {
		return isWet ? 30 : 145;
	}

	@Override
	public int getCloseProtectPercent(IBlockState state, boolean isWet) {
		return isWet ? 45 : 120;
	}
}
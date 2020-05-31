package ru.liahim.mist.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.block.IWettable;
import ru.liahim.mist.api.block.MistBlocks;

/**@author Liahim*/
public class MistBlockWettable extends MistBlock implements IWettable {

	private final int waterPerm;
	private Block acidBlock;
	
	public MistBlockWettable(Material material, int waterPerm) {
		super(material);
		this.waterPerm = MathHelper.clamp(waterPerm, 1, 3);
		this.setDefaultState(this.blockState.getBaseState().withProperty(WET, true));
		this.setTickRandomly(true);
		this.acidBlock = this;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
		String gender = I18n.format(stack.getUnlocalizedName() + ".name");
		if (gender.length() > 2 && gender.substring(gender.length() - 2, gender.length() - 1).equals("_")) {
			gender = gender.substring(gender.length() - 2, gender.length());
		} else gender = "";
		IBlockState state = getStateFromMeta(stack.getItemDamage());
		tooltip.add(I18n.format(state.getValue(WET) ? "tile.mist.block_wet" + gender + ".tooltip" : "tile.mist.block_dry" + gender + ".tooltip"));
		if (showPorosityTooltip()) tooltip.add(I18n.format("tile.mist.soil_porosity_" + getWaterPerm(state) + gender + ".tooltip"));
	}

	@SideOnly(Side.CLIENT)
	protected boolean showPorosityTooltip() { return true; }

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		if (world.isRemote) return;
		IBlockState checkState = world.getBlockState(fromPos);
		if (checkState.getBlock() == MistBlocks.ACID_BLOCK) this.setAcid(world, pos, state, 0, world.rand);
		else if (checkState.getMaterial() == Material.WATER && !state.getValue(WET)) this.setWet(world, pos, state, 0, world.rand);
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		if (!world.isRemote) {
			int i = IWettable.checkFluid(world, pos);
			if (i < 0) this.setAcid(world, pos, state, 0, world.rand);
			else if (i > 0 && !state.getValue(WET)) this.setWet(world, pos, state, 0, world.rand);
		}
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		update(world, pos, state, rand);
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		list.add(new ItemStack(this, 1, 0));
		list.add(new ItemStack(this, 1, 1));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(WET) ? 0 : 1;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(WET, meta == 0);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { WET });
	}

	@Override
	public int damageDropped(IBlockState state) {
		return getMetaFromState(state);
	}

	/** The quantity (1-3) of block water absorption. It affects:
	 *  the speed of wetting and drying of the block;
	 *  the maximum distance of water seepage;
	 *  leaching from the soil humus.*/
	@Override
	public int getWaterPerm(IBlockState state) {
		return waterPerm;
	}

	@Override
	public void setAcidBlock(Block acidBlock) {
		this.acidBlock = acidBlock;
	}

	@Override
	public Block getAcidBlock(IBlockState state) {
		return this.acidBlock;
	}

	@Override
	public boolean isAcid() {
		return false;
	}
}
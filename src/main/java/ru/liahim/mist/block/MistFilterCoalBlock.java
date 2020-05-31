package ru.liahim.mist.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import ru.liahim.mist.api.block.IMistAdsorbent;
import ru.liahim.mist.world.MistWorld;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**@author Liahim*/
public class MistFilterCoalBlock extends MistBlock implements IMistAdsorbent {

	public static final PropertyInteger POLLUTE = PropertyInteger.create("pollute", 0, 15);

	public MistFilterCoalBlock() {
		super(Material.ROCK, MapColor.BLACK);
		this.setDefaultState(this.blockState.getBaseState().withProperty(POLLUTE, 0));
		this.setHardness(5.0F);
		this.setResistance(10.0F);
		this.setTickRandomly(true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
		float pollute = this.getStateFromMeta(stack.getItemDamage()).getValue(POLLUTE) * 6.25F;
		StringBuilder sb = new StringBuilder();
		sb.append(I18n.format("item.mist.filter_damage.tooltip"));
		sb.append(": ");
		if (pollute >= 25) sb.append(pollute < 50 ? TextFormatting.YELLOW : pollute < 75 ? TextFormatting.GOLD : TextFormatting.RED);
		sb.append(String.format("%.2f", pollute));
		sb.append("%");
		tooltip.add(sb.toString());
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (!world.isRemote && rand.nextInt(4) == 0 && MistWorld.isPosInFog(world, pos)) {
			int pollute = state.getValue(POLLUTE);
			if (pollute < 15) world.setBlockState(pos, state.withProperty(POLLUTE, pollute + 1));
			else world.destroyBlock(pos, false);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		list.add(new ItemStack(this, 1, 0));
		list.add(new ItemStack(this, 1, 4));
		list.add(new ItemStack(this, 1, 8));
		list.add(new ItemStack(this, 1, 12));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(POLLUTE);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(POLLUTE, meta);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { POLLUTE });
	}

	@Override
	public int damageDropped(IBlockState state) {
		return getMetaFromState(state);
	}

	@Override
	public boolean isMistAdsorbent(World world, BlockPos pos, IBlockState state) {
		return true;
	}
}
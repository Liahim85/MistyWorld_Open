package ru.liahim.mist.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;
import javax.vecmath.Vector2f;

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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.util.SoilHelper;

/**@author Liahim*/
public class MistDirt extends MistSoil {

	public static final PropertyInteger HUMUS = PropertyInteger.create("humus", 0, 3);
	private final MapColor color;

	public MistDirt(Material material, float hardness, int waterPerm, MapColor color) {
		super(material, hardness, waterPerm);
		this.setDefaultState(this.blockState.getBaseState().withProperty(HUMUS, 0).withProperty(WET, true));
		this.color = color;
	}

	public MistDirt(Material material, float hardness, int waterPerm) {
		this(material, hardness, waterPerm, material.getMaterialMapColor());
	}

	public MistDirt(float hardness, int waterPerm) {
		this(Material.GROUND, hardness, waterPerm, Material.GROUND.getMaterialMapColor());
	}

	public MistDirt(float hardness, int waterPerm, MapColor color) {
		this(Material.GROUND, hardness, waterPerm, color);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
		super.addInformation(stack, player, tooltip, advanced);
		tooltip.add(I18n.format("tile.mist.soil_hum.tooltip") + ": " + (getStateFromMeta(stack.getItemDamage()).getValue(HUMUS) + 1) * 2 + "%");
	}

	@Override
	public boolean doIfWet(World world, BlockPos pos, IBlockState state, Vector2f waterType, boolean fog, Random rand) {
		int waterDist = (int)waterType.getY();
		if (waterDist >= 0 && rand.nextInt(5 - this.getWaterPerm(state)) == 0) {
			IBlockState stateUp = world.getBlockState(pos.up());
			if (stateUp.getBlock() instanceof MistTreeTrunk ? ((MistTreeTrunk)stateUp.getBlock()).getDir(stateUp) != EnumFacing.UP : true) {
				int hum = state.getValue(HUMUS);
				if (hum > waterDist + 3 - getWaterPerm(state) && (waterDist == 0 || hum > getMinHumusLevel(world, pos))) {
					return world.setBlockState(pos, state.withProperty(HUMUS, hum - 1), 2);
				}
			}
		}
		return false;
	}

	public static int getMinHumusLevel(World world, BlockPos pos) {
		int i = 4;
		BlockPos checkPos;
		IBlockState checkState;
		for (EnumFacing face : EnumFacing.VALUES) {
			if (face != EnumFacing.DOWN) {
				checkPos = pos.offset(face);
				if (world.isBlockLoaded(checkPos)) {
					if (world.getBlockState(checkPos).getBlock() instanceof MistSoil) {
						checkState = world.getBlockState(checkPos);
						i = Math.min(i, SoilHelper.getHumus(checkState));
					}
				}
			}
		}
		return i;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		list.add(new ItemStack(this, 1, 0));
		list.add(new ItemStack(this, 1, 4));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		Integer hum = state.getValue(HUMUS);
		return hum + (state.getValue(WET) ? 0 : getHumSize());
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(HUMUS, meta % getHumSize()).withProperty(WET, meta < getHumSize());
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { HUMUS, WET });
	}

	public boolean canFertile(IBlockState state) {
		return false;
	}

	/** Shows the maximum count of stages of soil fertility. DO NOT CHANGE!*/
	protected final int getHumSize() {
		return HUMUS.getAllowedValues().size();
	}

	@Override
	public IBlockState getFarmState(IBlockState state) {
		return this.getFarmBlock().getDefaultState().withProperty(HUMUS, state.getValue(HUMUS)).withProperty(WET, state.getValue(WET));
	}

	@Override
	public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
		return this.color;
	}
}
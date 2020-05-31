package ru.liahim.mist.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;
import javax.vecmath.Vector2f;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.item.MistItems;

/**@author Liahim*/
public class MistHumus_Dirt extends MistSoil {

	public MistHumus_Dirt(Material material, float hardness, int waterPerm) {
		super(material, hardness, waterPerm);
	}

	public MistHumus_Dirt(float hardness, int waterPerm) {
		super(Material.GROUND, hardness, waterPerm);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
		super.addInformation(stack, player, tooltip, advanced);
		tooltip.add(I18n.format("tile.mist.soil_hum.tooltip") + ": " + "10%");
	}

	@Override
	public boolean doIfWet(World world, BlockPos pos, IBlockState state, Vector2f waterType, boolean fog, Random rand) {
		if (waterType.getY() >= 0 && rand.nextInt(5 - this.getWaterPerm(state)) == 0) {
			IBlockState stateUp = world.getBlockState(pos.up());
			if (stateUp.getBlock() instanceof MistTreeTrunk ? ((MistTreeTrunk)stateUp.getBlock()).getDir(stateUp) != EnumFacing.UP : true) {
				if (waterType.getY() == 0 || MistDirt.getMinHumusLevel(world, pos) < 4)
					return world.setBlockState(pos, MistBlocks.DIRT_F.getDefaultState().withProperty(MistDirt.HUMUS, 3), 2);
			}
		}
		return false;
	}

	@Override
	@Nullable
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return state.getValue(WET) ? MistItems.HUMUS : Item.getItemFromBlock(this.getSoilBlock());
	}

	@Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
		return state.getValue(WET) ? 4 : 1;
    }

	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(WET) ? 0 : super.damageDropped(state);
	}

	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		return true;
	}

	@Override
	public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
		return MapColor.GRAY_STAINED_HARDENED_CLAY;
	}
}
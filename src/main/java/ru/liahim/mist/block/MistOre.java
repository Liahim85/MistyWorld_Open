package ru.liahim.mist.block;

import java.util.Random;

import javax.annotation.Nullable;

import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.item.MistItems;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MistOre extends MistBlock {

	public MistOre(float hardness, float resistance, int harvestLevel, MapColor color) {
		super(Material.ROCK, color);
		this.setHardness(hardness);
		this.setResistance(resistance);
		this.setHarvestLevel("pickaxe", harvestLevel);
	}

	public MistOre(float hardness, float resistance, int harvestLevel) {
		this(hardness, resistance, harvestLevel, Material.ROCK.getMaterialMapColor());
	}

	@Override
	@Nullable
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		if (this == MistBlocks.FILTER_COAL_ORE) return MistItems.FILTER_COAL;
		else if (this == MistBlocks.BIO_SHALE_ORE) return MistItems.BIO_SHALE;
		else if (this == MistBlocks.SULFUR_ORE) return MistItems.SULFUR;
		else if (this == MistBlocks.SALTPETER_ORE) return MistItems.SALTPETER;
		else if (this == MistBlocks.LAPIS_ORE) return Items.DYE;
		else return Item.getItemFromBlock(this);
	}

	@Override
	public int quantityDropped(Random random) {
		if (this == MistBlocks.LAPIS_ORE) return 4 + random.nextInt(5);
		else if (this == MistBlocks.SULFUR_ORE) return 1 + random.nextInt(3);
		else if (this == MistBlocks.SALTPETER_ORE) return 1 + random.nextInt(5);
		else return 1;
	}

	@Override
	public int quantityDroppedWithBonus(int fortune, Random random) {
		if (fortune > 0 && Item.getItemFromBlock(this) != this.getItemDropped(this.getBlockState().getValidStates().iterator().next(), random, fortune)) {
			int i = random.nextInt(fortune + 2) - 1;
			if (i < 0) i = 0;
			return this.quantityDropped(random) * (i + 1);
		}
		return this.quantityDropped(random);
	}

	@Override
	public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune) {
		Random rand = world instanceof World ? ((World)world).rand : new Random();
		if (this.getItemDropped(state, rand, fortune) != Item.getItemFromBlock(this)) {
			int i = 0;
			if (this == MistBlocks.FILTER_COAL_ORE) i = MathHelper.getInt(rand, 2, 5);
			else if (this == MistBlocks.BIO_SHALE_ORE) i = MathHelper.getInt(rand, 0, 2);
			else if (this == MistBlocks.SULFUR_ORE) i = MathHelper.getInt(rand, 0, 2);
			else if (this == MistBlocks.SALTPETER_ORE) i = MathHelper.getInt(rand, 0, 2);
			else if (this == MistBlocks.LAPIS_ORE) i = MathHelper.getInt(rand, 2, 5);
			return i;
		}
		return 0;
	}

	@Override
	public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
		return new ItemStack(this);
	}

	@Override
	public int damageDropped(IBlockState state) {
		return this == MistBlocks.LAPIS_ORE ? EnumDyeColor.BLUE.getDyeDamage() : 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		if (this == MistBlocks.NIOBIUM_ORE) return layer == BlockRenderLayer.CUTOUT_MIPPED;
		return layer == BlockRenderLayer.SOLID;
    }
	
}
package ru.liahim.mist.block;

import java.util.Random;

import ru.liahim.mist.api.block.IRubberBallCollideble;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.entity.EntityRubberBall;
import ru.liahim.mist.world.MistWorld;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MistGravel extends BlockFalling implements IRubberBallCollideble {

	public MistGravel() {
		super(Material.GROUND);
		this.setSoundType(SoundType.GROUND);
		this.setHardness(0.6F);
		this.setHarvestLevel("shovel", 0);
	}

	@Override
	public String getUnlocalizedName() {
		return "tile.mist." + super.getUnlocalizedName().substring(5);
	}

	@Override
	public void getDrops(NonNullList ret, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		Random rand = world instanceof World ? ((World)world).rand : RANDOM;
		if (fortune > 3) fortune = 3;
		if (rand.nextInt(20 - fortune * 5) == 0) ret.add(new ItemStack(Items.FLINT));
		else {
			int j = rand.nextInt(96);
			boolean drop = true;
			if (j < 6) {
				ret.add(new ItemStack(MistItems.ROCKS, Math.max(1, rand.nextInt(8) - 5)));
				drop = false;
			} else if (j < 8 + fortune * 4 && world instanceof World) {
				if (MistWorld.isPosInFog((World) world, pos.up(4))) {
					if (j < 2 + fortune * 4 && rand.nextInt(10 - fortune * 3) == 0) {
						ret.add(new ItemStack(rand.nextBoolean() ? Items.GOLD_NUGGET : MistItems.NIOBIUM_NUGGET));
						drop = false;
					}
				} else {
					if (j < 7) {
						ret.add(new ItemStack(Items.BONE));
						drop = false;
					} else if (rand.nextInt(10 - fortune * 3) == 0) {
						ret.add(new ItemStack(Items.IRON_NUGGET));
						drop = false;
					}
				}
			}
			if (drop) {
				int count = quantityDropped(state, fortune, rand);
				for (int i = 0; i < count; i++) {
					Item item = this.getItemDropped(state, rand, fortune);
					if (item != null) {
						ret.add(new ItemStack(item, 1, this.damageDropped(state)));
					}
				}
			}
		}
	}

	@Override
	public void onEntityWalk(World world, BlockPos pos, Entity entity) {
		if (entity.ticksExisted % 10 == 0 && world.rand.nextInt(3) == 0 && world.isAirBlock(pos.down())) {
			world.playSound(null, pos, SoundEvents.BLOCK_GRAVEL_BREAK, SoundCategory.BLOCKS, 0.3F, 1.0F);
			world.scheduleUpdate(pos, this, 0);
		}
	}

	@Override
	public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		return MapColor.STONE;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getDustColor(IBlockState state) {
		return -8878206;
	}

	@Override
	public boolean isCollide(World world, IBlockState state, EntityRubberBall ball, RayTraceResult result, Random rand) {
		world.scheduleUpdate(result.getBlockPos(), this, 0);
		return true;
	}
}
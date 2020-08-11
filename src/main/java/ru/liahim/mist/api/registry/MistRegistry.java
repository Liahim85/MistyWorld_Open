package ru.liahim.mist.api.registry;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import ru.liahim.mist.api.block.IMistStoneBasic;
import ru.liahim.mist.api.block.IMistStoneUpper;
import ru.liahim.mist.api.block.IShiftPlaceable;
import ru.liahim.mist.api.registry.IMistHarvest.HarvestType;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class MistRegistry {

	public static final Set<Item> filterCoalBreakers = Sets.newHashSet();
	public static final Map<Item, int[]> mistStoneBreakers = Maps.newHashMap();
	public static final Map<String, Integer> dimsForSkill = Maps.newHashMap();
	public static final Map<ResourceLocation, Integer> mobsForSkill = Maps.newHashMap();
	public static final Set<String> mobsDimsBlackList = Sets.newHashSet();
	public static final Set<ResourceLocation> mobsBlackList = Sets.newHashSet();
	private static final Map<Block, HarvestType> harvestTypeList = Maps.newHashMap();
	private static final Set<ItemStack> compostIngredients = Sets.newHashSet();
	private static final Set<IShiftPlaceable> shiftPlaceableBlocks = Sets.newHashSet();

	/** Register the ingredients for the Compost Heap. */
	public static void registerCompostIngredient(ItemStack stack) {
		compostIngredients.add(stack);
    }

	/** Register the HarvestType for the block of plants. */
	public static void registerHarvestType(Block block, HarvestType type) {
		if (harvestTypeList.containsKey(block)) harvestTypeList.replace(block, type);
		else harvestTypeList.put(block, type);
    }

	public static boolean isCompostIngredient(ItemStack stack) {
		for (ItemStack compost : compostIngredients) {
			if (stack.getItem() == compost.getItem() && stack.getItemDamage() == compost.getItemDamage())
				return true;
		}
		return false;
	}

	public static HarvestType getHarvestType(Block block) {
		return harvestTypeList.containsKey(block) ? harvestTypeList.get(block) : HarvestType.WP1_3;
	}

	public static boolean addShiftPlaceableBlocks(IShiftPlaceable block) {
		return shiftPlaceableBlocks.add(block);
	}

	public static boolean checkShiftPlacing(World world, BlockPos pos, @Nonnull ItemStack stack, EntityPlayer player, float hitX, float hitY, float hitZ, BlockFaceShape bfs) {
		for (IShiftPlaceable block : shiftPlaceableBlocks) {
			if (block.onShiftPlacing(world, pos, stack, player, hitX, hitY, hitZ, bfs)) {
				return true;
			}
		}
		return false;
	}

	public static float getBreakingSpeed(ItemStack stack, IBlockState state, float originSpeed) {
		if (!mistStoneBreakers.isEmpty() && mistStoneBreakers.containsKey(stack.getItem())) {
			int[] speed = mistStoneBreakers.get(stack.getItem());
			int m = state.getBlock() instanceof IMistStoneBasic ? speed[2] : state.getBlock() instanceof IMistStoneUpper && ((IMistStoneUpper)state.getBlock()).isUpperStone(state) ? speed[1] : speed[0];
			return originSpeed * m;
		}
		return originSpeed;
	}
}
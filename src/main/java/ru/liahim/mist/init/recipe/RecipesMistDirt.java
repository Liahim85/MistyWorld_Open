package ru.liahim.mist.init.recipe;

import javax.annotation.Nullable;

import ru.liahim.mist.api.block.IWettable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.block.MistClay;
import ru.liahim.mist.block.MistDirt;
import ru.liahim.mist.block.MistGravel;
import ru.liahim.mist.block.MistSand;
import ru.liahim.mist.block.MistSapropel;
import ru.liahim.mist.block.MistSoil;
import ru.liahim.mist.item.ItemMistFertilizer;
import ru.liahim.mist.util.SoilHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RecipesMistDirt extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

	private ItemStack resultItem = ItemStack.EMPTY;

	@Override
	public boolean matches(InventoryCrafting inv, World world) {
		this.resultItem = ItemStack.EMPTY;
		ItemStack stack;
		Item item;
		Block block;
		IBlockState state;
		boolean isSoil = false;
		boolean wet = true;
		int blocksCount = 0;
		int counter = 0;
		float humus = 0;
		float sand = 0;
		float ferrum = 0;
		float clay = 0;
		float gravel = 0;
		for (int i = 0; i < inv.getSizeInventory(); ++i) {
			stack = inv.getStackInSlot(i);
			if (!stack.isEmpty()) {
				item = stack.getItem();
				if (item instanceof ItemBlock) {
					block = Block.getBlockFromItem(item);
					if (block instanceof IWettable) {
						state = block.getStateFromMeta(stack.getItemDamage());
						if (!state.getValue(IWettable.WET)) wet = false;
						if (block instanceof MistSoil) {
							isSoil = true;
							humus += (SoilHelper.getHumus(state) + 1) * 2;
							if (block == MistBlocks.DIRT_F || block == MistBlocks.GRASS_F) {
								sand += 0.25F; clay += 0.25F;
							} else if (block == MistBlocks.DIRT_S || block == MistBlocks.GRASS_S) {
								sand += 0.5F;
							} else if (block == MistBlocks.DIRT_T || block == MistBlocks.GRASS_T) {
								sand += 0.25F; clay += 0.25F; ferrum += 0.5F;
							} else if (block == MistBlocks.DIRT_C || block == MistBlocks.GRASS_C) {
								clay += 0.5F;
							} else if (block == MistBlocks.DIRT_R || block == MistBlocks.GRASS_R) {
								gravel += 0.5F;
							}
						} else if (block instanceof MistSand) {
							sand += 1.0F;
							if (state.getValue(BlockSand.VARIANT) == BlockSand.EnumType.RED_SAND) ferrum += 1.0F;
						} else if (block instanceof MistClay) {
							if (state.getValue(MistClay.TYPE) == MistClay.EnumBlockType.BLOCK) {
								clay += 1.0F;
								if (state.getValue(MistClay.VARIANT) == MistClay.EnumClayType.RED_CLAY) ferrum += 1.0F;
							}
						} else if (block instanceof MistSapropel) {
							if (state.getValue(MistSapropel.TYPE) == MistSapropel.EnumBlockType.BLOCK) {
								isSoil = true;
								humus += 10.0F;
							}
						}
					} else if (block instanceof MistGravel) {
						gravel += 1.0F;
					}
					/** Vanila */
					else if (block instanceof BlockSand) {
						wet = false;
						sand += 1.0F;
						state = block.getStateFromMeta(stack.getItemDamage());
						if (state.getValue(BlockSand.VARIANT) == BlockSand.EnumType.RED_SAND) ferrum += 1.0F;
					} else if (block == Blocks.GRAVEL) {
						gravel += 1.0F;
					} else if (block == Blocks.CLAY) {
						clay += 1.0F;
					} else return false;
					++blocksCount;
					++counter;
				} else if (item instanceof ItemMistFertilizer) {
					humus += 2.0F;
					++counter;
				} else if (item == MistItems.CLAY_BALL) {
					clay += 0.25F;
					if (stack.getItemDamage() == 1) ferrum += 0.25F;
					++counter;
				} else if (item == Items.CLAY_BALL) {
					clay += 0.25F;
					++counter;
				} else return false;
			}
		}
		if (!isSoil || counter < 2) return false;
		else {
			humus = (int)(humus/blocksCount) >> 1 << 1;
			if (humus < 2) return false;
			humus = Math.min(humus/2 - 1, 3);
			float summ = sand + clay + gravel;
			if (summ == 0) return false;
			if (ferrum/summ < 0.5F) {
				if ((sand/summ >= 0.75F && sand/blocksCount >= 0.5F) || (clay/blocksCount < 0.15F && gravel/blocksCount < 0.15F)) block = MistBlocks.DIRT_S;
				else if ((clay/summ >= 0.75F && clay/blocksCount >= 0.5F) || (sand/blocksCount < 0.15F && gravel/blocksCount < 0.15F)) block = MistBlocks.DIRT_C;
				else if ((gravel/summ >= 0.75F && gravel/blocksCount >= 0.5F) || (sand/blocksCount < 0.15F && clay/blocksCount < 0.15F)) block = MistBlocks.DIRT_R;
				else if (sand/summ >= 0.375F && clay/summ >= 0.375F && sand/blocksCount >= 0.25F && clay/blocksCount >= 0.25F && gravel/blocksCount < 0.15F) block = MistBlocks.DIRT_F;
				else return false;
			} else if (sand/summ >= 0.375F && clay/summ >= 0.375F && sand/blocksCount >= 0.25F && clay/blocksCount >= 0.25F && gravel/blocksCount < 0.15F) {
				block = MistBlocks.DIRT_T;
			} else return false;
			state = block.getDefaultState().withProperty(IWettable.WET, wet).withProperty(MistDirt.HUMUS, (int)humus);
			this.resultItem = new ItemStack(block, blocksCount, block.getMetaFromState(state));
		}
		return !this.resultItem.isEmpty();
	}

	@Override
	@Nullable
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		return this.resultItem.copy();
	}

	@Override
	@Nullable
	public ItemStack getRecipeOutput() {
		return ItemStack.EMPTY;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
		NonNullList<ItemStack> list = NonNullList.<ItemStack>withSize(inv.getSizeInventory(), ItemStack.EMPTY);
		for (int i = 0; i < list.size(); ++i) {
			ItemStack itemstack = inv.getStackInSlot(i);
			list.set(i, ForgeHooks.getContainerItem(itemstack));
		}
		return list;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}
}
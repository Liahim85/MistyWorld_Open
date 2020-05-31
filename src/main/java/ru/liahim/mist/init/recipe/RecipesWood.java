package ru.liahim.mist.init.recipe;

import ru.liahim.mist.api.block.MistBlocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

public class RecipesWood {

    private final Item[] stepItems = new Item[] {
    		Item.getItemFromBlock(MistBlocks.ACACIA_STEP), Item.getItemFromBlock(MistBlocks.ASPEN_STEP),
    		Item.getItemFromBlock(MistBlocks.A_TREE_STEP), Item.getItemFromBlock(MistBlocks.BIRCH_STEP),
    		Item.getItemFromBlock(MistBlocks.OAK_STEP), Item.getItemFromBlock(MistBlocks.PINE_STEP),
    		Item.getItemFromBlock(MistBlocks.POPLAR_STEP), Item.getItemFromBlock(MistBlocks.SNOW_STEP),
    		Item.getItemFromBlock(MistBlocks.SPRUCE_STEP), Item.getItemFromBlock(MistBlocks.S_TREE_STEP),
    		Item.getItemFromBlock(MistBlocks.T_TREE_STEP), Item.getItemFromBlock(MistBlocks.WILLOW_STEP),
    		Item.getItemFromBlock(MistBlocks.R_TREE_STEP)
		};
    private final Item[] slabItems = new Item[] {
    		Item.getItemFromBlock(MistBlocks.ACACIA_SLAB), Item.getItemFromBlock(MistBlocks.ASPEN_SLAB),
    		Item.getItemFromBlock(MistBlocks.A_TREE_SLAB), Item.getItemFromBlock(MistBlocks.BIRCH_SLAB),
    		Item.getItemFromBlock(MistBlocks.OAK_SLAB), Item.getItemFromBlock(MistBlocks.PINE_SLAB),
    		Item.getItemFromBlock(MistBlocks.POPLAR_SLAB), Item.getItemFromBlock(MistBlocks.SNOW_SLAB),
    		Item.getItemFromBlock(MistBlocks.SPRUCE_SLAB), Item.getItemFromBlock(MistBlocks.S_TREE_SLAB),
    		Item.getItemFromBlock(MistBlocks.T_TREE_SLAB), Item.getItemFromBlock(MistBlocks.WILLOW_SLAB),
    		Item.getItemFromBlock(MistBlocks.R_TREE_SLAB)
		};
    private final Item[] stairsItems = new Item[] {
    		Item.getItemFromBlock(MistBlocks.ACACIA_STAIRS), Item.getItemFromBlock(MistBlocks.ASPEN_STAIRS),
    		Item.getItemFromBlock(MistBlocks.A_TREE_STAIRS), Item.getItemFromBlock(MistBlocks.BIRCH_STAIRS),
    		Item.getItemFromBlock(MistBlocks.OAK_STAIRS), Item.getItemFromBlock(MistBlocks.PINE_STAIRS),
    		Item.getItemFromBlock(MistBlocks.POPLAR_STAIRS), Item.getItemFromBlock(MistBlocks.SNOW_STAIRS),
    		Item.getItemFromBlock(MistBlocks.SPRUCE_STAIRS), Item.getItemFromBlock(MistBlocks.S_TREE_STAIRS),
    		Item.getItemFromBlock(MistBlocks.T_TREE_STAIRS), Item.getItemFromBlock(MistBlocks.WILLOW_STAIRS),
    		Item.getItemFromBlock(MistBlocks.R_TREE_STAIRS)
		};
    private final Item[] logItems = new Item[] {
    		Item.getItemFromBlock(MistBlocks.ACACIA_BLOCK), Item.getItemFromBlock(MistBlocks.ASPEN_BLOCK),
    		Item.getItemFromBlock(MistBlocks.A_TREE_BLOCK), Item.getItemFromBlock(MistBlocks.BIRCH_BLOCK),
    		Item.getItemFromBlock(MistBlocks.OAK_BLOCK), Item.getItemFromBlock(MistBlocks.PINE_BLOCK),
    		Item.getItemFromBlock(MistBlocks.POPLAR_BLOCK), Item.getItemFromBlock(MistBlocks.SNOW_BLOCK),
    		Item.getItemFromBlock(MistBlocks.SPRUCE_BLOCK), Item.getItemFromBlock(MistBlocks.S_TREE_BLOCK),
    		Item.getItemFromBlock(MistBlocks.T_TREE_BLOCK), Item.getItemFromBlock(MistBlocks.WILLOW_BLOCK),
    		Item.getItemFromBlock(MistBlocks.R_TREE_BLOCK)
		};
    private final Item[] branchItems = new Item[] {
    		Item.getItemFromBlock(MistBlocks.ACACIA_BRANCH), Item.getItemFromBlock(MistBlocks.ASPEN_BRANCH),
    		Item.getItemFromBlock(MistBlocks.A_TREE_BRANCH), Item.getItemFromBlock(MistBlocks.BIRCH_BRANCH),
    		Item.getItemFromBlock(MistBlocks.OAK_BRANCH), Item.getItemFromBlock(MistBlocks.PINE_BRANCH),
    		Item.getItemFromBlock(MistBlocks.POPLAR_BRANCH), Item.getItemFromBlock(MistBlocks.SNOW_BRANCH),
    		Item.getItemFromBlock(MistBlocks.SPRUCE_BRANCH), Item.getItemFromBlock(MistBlocks.S_TREE_BRANCH),
    		Item.getItemFromBlock(MistBlocks.T_TREE_BRANCH), Item.getItemFromBlock(MistBlocks.WILLOW_BRANCH),
    		Item.getItemFromBlock(MistBlocks.R_TREE_BRANCH)
		};
    private final int[] logMetas = new int[] { 0, 4, 7, 8, 11 };

    public void addRecipes() {
		Item log;
		Item branch;

		for (int i = 0; i < logItems.length; ++i) {
			log = logItems[i];
			branch = branchItems[i];

			for (int j = 0; j < logMetas.length; ++j) {
				int meta = logMetas[j];
				GameRegistry.addSmelting(new ItemStack(log, 1, meta), new ItemStack(Items.COAL, 1, 1), 0.15F);
				OreDictionary.registerOre("treeWood", new ItemStack(log, 1, meta));
			}

			OreDictionary.registerOre("plankWood", new ItemStack(stepItems[i]));
			OreDictionary.registerOre("slabWood", new ItemStack(slabItems[i]));
			OreDictionary.registerOre("stairWood", new ItemStack(stairsItems[i]));
			OreDictionary.registerOre("stickWood", new ItemStack(branch, 1, 0));
			OreDictionary.registerOre("stickWood", new ItemStack(branch, 1, 3));
		}
	}
}
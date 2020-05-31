package ru.liahim.mist.client.renderer.tileentity;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.block.gizmos.MistChest;
import ru.liahim.mist.tileentity.TileEntityMistChest;

@SideOnly(Side.CLIENT)
public class TEISR extends TileEntityItemStackRenderer {

	private final TileEntityMistChest chestTile = new TileEntityMistChest();

	@Override
	public void renderByItem(ItemStack itemStack, float partialTicks) {
		Block block = Block.getBlockFromItem(itemStack.getItem());
		if (block instanceof MistChest) {
			TileEntityRendererDispatcher.instance.render(chestTile, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks);
		} else ForgeHooksClient.renderTileItem(itemStack.getItem(), itemStack.getMetadata());
	}
}
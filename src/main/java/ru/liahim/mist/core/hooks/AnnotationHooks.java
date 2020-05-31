package ru.liahim.mist.core.hooks;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.MistTags;
import ru.liahim.mist.block.MistBlockStep;
import ru.liahim.mist.common.ClientProxy;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.core.asm.Hook;
import ru.liahim.mist.core.asm.ReturnCondition;
import ru.liahim.mist.core.asm.Hook.ReturnValue;
import ru.liahim.mist.init.ModConfig;
import ru.liahim.mist.init.ModConfig.Graphic;
import ru.liahim.mist.util.FogTexture;
import ru.liahim.mist.util.TextureUtilForHook;

public class AnnotationHooks {

	@Hook(returnCondition = ReturnCondition.ON_TRUE)
	@SideOnly(Side.CLIENT)
	public static boolean addRainParticles(EntityRenderer er) {
		if (Minecraft.getMinecraft().world.provider.getDimension() == Mist.getID()) {
			ClientProxy.RainParticleRenderer.render(0, Minecraft.getMinecraft().world, Minecraft.getMinecraft());
			return true;
		}
		return false;
	}

	@Hook(injectOnExit = true)
	@SideOnly(Side.CLIENT)
	public static void read(Chunk chunk, PacketBuffer buf, int availableSections, boolean groundUpContinuous) {
		if (ModConfig.graphic.advancedFogRenderer && chunk.getWorld().isRemote && chunk.getWorld().provider.getDimension() == Mist.getID()) {
			FogTexture.createChunkTexture(chunk.getWorld(), chunk.x, chunk.z);
		}
	}

	@Hook(injectOnExit = true)
	@SideOnly(Side.CLIENT)
	public static void notifyBlockUpdate(RenderGlobal render, World world, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
		if (ModConfig.graphic.advancedFogRenderer && world != null && world.isRemote && world.provider.getDimension() == Mist.getID()) {
			if (newState.getLightOpacity(world, pos) != oldState.getLightOpacity(world, pos) || newState.getLightValue(world, pos) != oldState.getLightValue(world, pos)) {
				FogTexture.createBlockTexture(world, pos);
			}
		}
	}

	@Hook(returnCondition = ReturnCondition.ON_TRUE, returnAnotherMethod = "blendColorsHook")
	@SideOnly(Side.CLIENT)
	public static boolean blendColors(TextureUtil tu, int color_0, int color_1, int color_2, int color_3, boolean alpha) {
		return Graphic.mipMapOptimization;
	}

	public static int blendColorsHook(TextureUtil tu, int color_0, int color_1, int color_2, int color_3, boolean alpha) {
		return TextureUtilForHook.blendColors(color_0, color_1, color_2, color_3, alpha);
	}

	@Hook(returnCondition = ReturnCondition.ALWAYS)
	public static IRecipe findMatchingRecipe(CraftingManager crManager, InventoryCrafting craftMatrix, World world) {
		for (IRecipe irecipe : CraftingManager.REGISTRY) {
            if (irecipe.matches(craftMatrix, world)) {
            	Item result = irecipe.getCraftingResult(craftMatrix).getItem();
            	if (result == Items.STICK || Block.getBlockFromItem(result) == Blocks.CRAFTING_TABLE ||
            			(Block.getBlockFromItem(result) == Blocks.WOODEN_PRESSURE_PLATE && !irecipe.getRegistryName().getResourceDomain().equals(Mist.MODID))) {
        			boolean check = false;
            		for (int i = 0; i < craftMatrix.getSizeInventory(); ++i) {
        				if (Block.getBlockFromItem(craftMatrix.getStackInSlot(i).getItem()) instanceof MistBlockStep) {
        					check = true;
        					break;
        				}
        			}
            		if (!check) return irecipe;
        		} else return irecipe;
            }
        }
		return null;
	}

	@Hook(injectOnExit = true, returnCondition = ReturnCondition.ON_TRUE)
	public static boolean attemptDamageItem(ItemStack stack, int amount, Random rand, @Nullable EntityPlayerMP player, @ReturnValue boolean returnValue) {
		if (returnValue && player != null && stack.getItem() instanceof ItemArmor) {
			EntityEquipmentSlot slot = ((ItemArmor)stack.getItem()).armorType;
			if (stack == player.getItemStackFromSlot(slot)) {
				NBTTagCompound tag = stack.getSubCompound(MistTags.nbtInnerSuitTag);
				if (tag != null) player.setItemStackToSlot(slot, new ItemStack(tag));
			}
		}
		return returnValue;
	}

	@Hook(injectOnExit = true)
	@SideOnly(Side.CLIENT)
	public static void renderItemOverlayIntoGUI(RenderItem ri, FontRenderer fr, ItemStack stack, int xPosition, int yPosition, @Nullable String text) {
		if (!stack.isEmpty() && stack.getItem() instanceof ItemArmor) {
			NBTTagCompound tag = stack.getSubCompound(MistTags.nbtInnerSuitTag);
			if (tag != null) {
				ItemStack suit = new ItemStack(tag);
				if (suit.getItem().showDurabilityBar(suit)) {
					GlStateManager.disableLighting();
					GlStateManager.disableDepth();
					GlStateManager.disableTexture2D();
					GlStateManager.disableAlpha();
					GlStateManager.disableBlend();
					Tessellator tessellator = Tessellator.getInstance();
					BufferBuilder bufferbuilder = tessellator.getBuffer();
					double health = suit.getItem().getDurabilityForDisplay(suit);
					//int rgbfordisplay = suit.getItem().getRGBDurabilityForDisplay(suit);
					int rgbfordisplay = 0xFFFFFF;
					int i = Math.round(13.0F - (float) health * 13.0F);
					int j = rgbfordisplay;
					ri.draw(bufferbuilder, xPosition + 2, yPosition + 14, 13, 2, 0, 0, 0, 255);
					ri.draw(bufferbuilder, xPosition + 2, yPosition + 14, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255, 255);
					GlStateManager.enableBlend();
					GlStateManager.enableAlpha();
					GlStateManager.enableTexture2D();
					GlStateManager.enableLighting();
					GlStateManager.enableDepth();
				}
			}
		}
	}
}
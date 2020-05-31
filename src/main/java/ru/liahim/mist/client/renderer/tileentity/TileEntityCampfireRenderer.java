package ru.liahim.mist.client.renderer.tileentity;

import org.lwjgl.opengl.GL11;

import ru.liahim.mist.block.gizmos.MistCampfire.CookingTool;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.tileentity.TileEntityCampfire;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;

public class TileEntityCampfireRenderer extends TileEntitySpecialRenderer<TileEntityCampfire> {

	public int[] colors = new int[4];
	private static final ResourceLocation[] SOUP_TEXTURES = new ResourceLocation[] {
		new ResourceLocation(Mist.MODID, "textures/blocks/soup/soup_mask_0.png"),
		new ResourceLocation(Mist.MODID, "textures/blocks/soup/soup_mask_1.png"),
		new ResourceLocation(Mist.MODID, "textures/blocks/soup/soup_mask_2.png"),
		new ResourceLocation(Mist.MODID, "textures/blocks/soup/soup_mask_3.png"),
		new ResourceLocation(Mist.MODID, "textures/blocks/soup/soup_mask_4.png"),
		new ResourceLocation(Mist.MODID, "textures/blocks/soup/soup_mask_5.png"),
		new ResourceLocation(Mist.MODID, "textures/blocks/soup/soup_mask_6.png"),
		new ResourceLocation(Mist.MODID, "textures/blocks/soup/soup_mask_7.png")
	};
	private static final ResourceLocation SOUP_MIX = new ResourceLocation("mist:textures/blocks/soup/soup_mix.png");

	@Override
	public void render(TileEntityCampfire te, double x, double y, double z, float partialTicks, int destroyStage, float alphaIn) {
		if (te.hasCookingTool()) {
			if (te.getCookingTool() == CookingTool.POT) {
				if (te.getVolum() > 0) {
					float h = te.getVolum() * 0.0625F + 0.5F;
					float red = 1.0F, green = 1.0F, blue = 1.0F, alpha = 1.0F;
					Tessellator tessellator = Tessellator.getInstance();
					BufferBuilder vertexbuffer = tessellator.getBuffer();
					GlStateManager.pushMatrix();
				    GlStateManager.translate(x, y, z);
					GlStateManager.color(1.0F, 1.0F, 1.0F);
					GlStateManager.enableBlend();
					GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
					GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
					float progress = te.getCookProgress();
					float index = Math.min(te.getFinalAmount() / te.getVolum() / 6, 1.0F);
					float riches = progress * index;
					colors = te.getFoodColors();
					if (riches < 1.0F) {
						ResourceLocation fluidTexture = FluidRegistry.WATER.getStill();
						TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluidTexture.toString());
						Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
						vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
						vertexbuffer.pos(0.28125, h, 0.28125).tex(sprite.getMinU(), sprite.getMinV()).color(red, green, blue, 1.0F).normal(0, 1, 0).endVertex();
						vertexbuffer.pos(0.28125, h, 0.71825).tex(sprite.getMinU(), sprite.getInterpolatedV(7)).color(red, green, blue, 1.0F).normal(0, 1, 0).endVertex();
						vertexbuffer.pos(0.71825, h, 0.71825).tex(sprite.getInterpolatedU(7), sprite.getInterpolatedV(7)).color(red, green, blue, 1.0F).normal(0, 1, 0).endVertex();
						vertexbuffer.pos(0.71825, h, 0.28125).tex(sprite.getInterpolatedU(7), sprite.getMinV()).color(red, green, blue, 1.0F).normal(0, 1, 0).endVertex();
						tessellator.draw();
					}
					float mix = 0.0F;
					float amount = 0.3F;
					if (riches > 0.0F) {
						int count = 0;
						red = green = blue = 0.0F;
						for (int i = 0; i < colors.length; ++i) {
							if (colors[i] != 0) {
								red += ((colors[i] >> 16) & 255) / 255.0F * 0.7F;
								green += ((colors[i] >> 8) & 255) / 255.0F * 0.7F;
								blue += (colors[i] & 255) / 255.0F * 0.7F;
								++count;
							}
						}
						if (count > 0) {
							red /= count;
							green /= count;
							blue /= count;
							mix = (red + green + blue) / 3;
							red = red * (1.0F - progress * amount) + mix * progress * amount;
							green = green * (1.0F - progress * amount) + mix * progress * amount;
							blue = blue * (1.0F - progress * amount) + mix * progress * amount;
							Minecraft.getMinecraft().getTextureManager().bindTexture(SOUP_MIX);
							vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
							vertexbuffer.pos(0.28125, h, 0.28125).tex(0, 0).color(red, green, blue, riches).normal(0, 1, 0).endVertex();
							vertexbuffer.pos(0.28125, h, 0.71825).tex(0, 1).color(red, green, blue, riches).normal(0, 1, 0).endVertex();
							vertexbuffer.pos(0.71825, h, 0.71825).tex(1, 1).color(red, green, blue, riches).normal(0, 1, 0).endVertex();
							vertexbuffer.pos(0.71825, h, 0.28125).tex(1, 0).color(red, green, blue, riches).normal(0, 1, 0).endVertex();
							tessellator.draw();
						}
					}
					amount = 0.2F;
					for (int i = 0; i < colors.length; ++i) {
						if (colors[i] != 0) {
							red = ((colors[i] >> 16) & 255) / 255.0F * 0.9F;
							green = ((colors[i] >> 8) & 255) / 255.0F * 0.9F;
							blue = (colors[i] & 255) / 255.0F * 0.9F;
							if (mix > 0) {
								red = red * (1.0F - progress * amount) + mix * progress * amount;
								green = green * (1.0F - progress * amount) + mix * progress * amount;
								blue = blue * (1.0F - progress * amount) + mix * progress * amount;
							}
							for (int j = 0; j < SOUP_TEXTURES.length; ++j) {
								Minecraft.getMinecraft().getTextureManager().bindTexture(SOUP_TEXTURES[j]);
								alpha = (float)te.getAnimationPhase((i * 80 + j * 40 + ((i + j) & 3) * 8) % 320, partialTicks) * index;
								if (alpha > 0) {
									vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
									vertexbuffer.pos(0.28125, h, 0.28125).tex(0, 0).color(red, green, blue, alpha).normal(0, 1, 0).endVertex();
									vertexbuffer.pos(0.28125, h, 0.71825).tex(0, 1).color(red, green, blue, alpha).normal(0, 1, 0).endVertex();
									vertexbuffer.pos(0.71825, h, 0.71825).tex(1, 1).color(red, green, blue, alpha).normal(0, 1, 0).endVertex();
									vertexbuffer.pos(0.71825, h, 0.28125).tex(1, 0).color(red, green, blue, alpha).normal(0, 1, 0).endVertex();
									tessellator.draw();
								}
							}
						}
					}
					GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
					GlStateManager.color(1.0F, 1.0F, 1.0F);
					GlStateManager.disableBlend();
					GlStateManager.popMatrix();
				}
			} else if (te.getCookingTool() == CookingTool.GRILL) {
				GlStateManager.pushMatrix();
			    GlStateManager.translate(x + 0.5, y + 0.578125, z + 0.5);
			    GlStateManager.rotate(-90, 1, 0, 0);
			    GlStateManager.rotate(-te.getFacing().getHorizontalAngle(), 0, 0, 1);
			    GlStateManager.scale(0.5, 0.5, 0.5);
			    //Item 1
			    GlStateManager.translate(-0.5, -0.5, 0);
			    Minecraft.getMinecraft().getRenderItem().renderItem(te.getGrillStack(0), TransformType.NONE);
			    //Item 2
			    GlStateManager.translate(1, 0, 0);
			    Minecraft.getMinecraft().getRenderItem().renderItem(te.getGrillStack(1), TransformType.NONE);
			    //Item 3
			    GlStateManager.translate(0, 1, 0);
			    Minecraft.getMinecraft().getRenderItem().renderItem(te.getGrillStack(2), TransformType.NONE);
			    //Item 4
			    GlStateManager.translate(-1, 0, 0);
			   	Minecraft.getMinecraft().getRenderItem().renderItem(te.getGrillStack(3), TransformType.NONE);
				GlStateManager.popMatrix();
			}
		}
	}
}
package ru.liahim.mist.client.renderer;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.IRenderHandler;
import ru.liahim.mist.handlers.FogRenderer;

public class SkyRendererMist extends IRenderHandler {

	private static final ResourceLocation MOON_PHASES_TEXTURES = new ResourceLocation("mist:textures/environment/moon_phases.png");
	private static final ResourceLocation SUN_TEXTURES = new ResourceLocation("textures/environment/sun.png");
	private boolean vboEnabled;
	private final VertexFormat vertexBufferFormat;
	private VertexBuffer starVBO;
	private VertexBuffer skyVBO;
	private VertexBuffer sky2VBO;
	private int starGLCallList = -1;
	private int glSkyList = -1;
	private int glSkyList2 = -1;

	public SkyRendererMist() {
		this.vboEnabled = OpenGlHelper.useVbo();
		this.vertexBufferFormat = new VertexFormat();
		this.vertexBufferFormat.addElement(new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.POSITION, 3));
		this.generateStars();
		this.generateSky();
		this.generateSky2();
	}

	@Override
	public void render(float partialTicks, WorldClient world, Minecraft mc) {
		if (mc.gameSettings.anaglyph) {
			renderSky(partialTicks, world, mc, EntityRenderer.anaglyphField);
		} else {
			renderSky(partialTicks, world, mc, 2);
		}
	}

	public void renderSky(float partialTicks, WorldClient theWorld, Minecraft mc, int pass)
	{
		GlStateManager.disableTexture2D();
		Vec3d vec3d = theWorld.getSkyColor(mc.getRenderViewEntity(), partialTicks);
		float f = (float)vec3d.x;
		float f1 = (float)vec3d.y;
		float f2 = (float)vec3d.z;
		if (pass != 2)
		{
			float f3 = (f * 30.0F + f1 * 59.0F + f2 * 11.0F) / 100.0F;
			float f4 = (f * 30.0F + f1 * 70.0F) / 100.0F;
			float f5 = (f * 30.0F + f2 * 70.0F) / 100.0F;
			f = f3;
			f1 = f4;
			f2 = f5;
		}
		GlStateManager.color(f, f1, f2);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexbuffer = tessellator.getBuffer();
		GlStateManager.depthMask(false);
		GlStateManager.enableFog();
		GlStateManager.color(f, f1, f2);
		if (this.vboEnabled)
		{
			this.skyVBO.bindBuffer();
			GlStateManager.glEnableClientState(32884);
			GlStateManager.glVertexPointer(3, 5126, 12, 0);
			this.skyVBO.drawArrays(7);
			this.skyVBO.unbindBuffer();
			GlStateManager.glDisableClientState(32884);
		}
		else
		{
			GlStateManager.callList(this.glSkyList);
		}
		GlStateManager.disableFog();
		GlStateManager.disableAlpha();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		RenderHelper.disableStandardItemLighting();
		float[] afloat = theWorld.provider.calcSunriseSunsetColors(theWorld.getCelestialAngle(partialTicks), partialTicks);
		if (afloat != null)
		{
			GlStateManager.disableTexture2D();
			GlStateManager.shadeModel(7425);
			GlStateManager.pushMatrix();
			GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(MathHelper.sin(theWorld.getCelestialAngleRadians(partialTicks)) < 0.0F ? 180.0F
				: 0.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
			float f6 = afloat[0];
			float f7 = afloat[1];
			float f8 = afloat[2];
			if (pass != 2)
			{
				float f9 = (f6 * 30.0F + f7 * 59.0F + f8 * 11.0F) / 100.0F;
				float f10 = (f6 * 30.0F + f7 * 70.0F) / 100.0F;
				float f11 = (f6 * 30.0F + f8 * 70.0F) / 100.0F;
				f6 = f9;
				f7 = f10;
				f8 = f11;
			}
			vertexbuffer.begin(6, DefaultVertexFormats.POSITION_COLOR);
			vertexbuffer.pos(0.0D, 100.0D, 0.0D).color(f6, f7, f8, afloat[3]).endVertex();
			int j = 16;
			for (int l = 0; l <= 16; ++l)
			{
				float f21 = l * ((float)Math.PI * 2F) / 16.0F;
				float f12 = MathHelper.sin(f21);
				float f13 = MathHelper.cos(f21);
				vertexbuffer.pos(f12 * 120.0F, f13 * 120.0F, -f13 * 40.0F * afloat[3]).color(afloat[0], afloat[1], afloat[2], 0.0F).endVertex();
			}
			tessellator.draw();
			GlStateManager.popMatrix();
			GlStateManager.shadeModel(7424);
		}
		GlStateManager.enableTexture2D();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.pushMatrix();
		float f16 = 1.0F - theWorld.getRainStrength(partialTicks);
		GlStateManager.color(1.0F, 1.0F, 1.0F, f16);
		GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(theWorld.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);
		float f17 = 30.0F;
		mc.renderEngine.bindTexture(SUN_TEXTURES);
		vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		vertexbuffer.pos((-f17), 100.0D, (-f17)).tex(0.0D, 0.0D).endVertex();
		vertexbuffer.pos(f17, 100.0D, (-f17)).tex(1.0D, 0.0D).endVertex();
		vertexbuffer.pos(f17, 100.0D, f17).tex(1.0D, 1.0D).endVertex();
		vertexbuffer.pos((-f17), 100.0D, f17).tex(0.0D, 1.0D).endVertex();
		tessellator.draw();
		f17 = 20.0F;
		mc.renderEngine.bindTexture(MOON_PHASES_TEXTURES);
		int i = theWorld.getMoonPhase();
		int k = i % 4;
		int i1 = i / 4 % 2;
		float f22 = (k + 0) / 4.0F;
		float f23 = (i1 + 0) / 2.0F;
		float f24 = (k + 1) / 4.0F;
		float f14 = (i1 + 1) / 2.0F;
		vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		vertexbuffer.pos((-f17), -100.0D, f17).tex(f24, f14).endVertex();
		vertexbuffer.pos(f17, -100.0D, f17).tex(f22, f14).endVertex();
		vertexbuffer.pos(f17, -100.0D, (-f17)).tex(f22, f23).endVertex();
		vertexbuffer.pos((-f17), -100.0D, (-f17)).tex(f24, f23).endVertex();
		tessellator.draw();
		GlStateManager.disableTexture2D();
		float f15 = theWorld.getStarBrightness(partialTicks) * f16;
		if (f15 > 0.0F)
		{
			GlStateManager.color(f15, f15, f15, f15);
			if (this.vboEnabled)
			{
				this.starVBO.bindBuffer();
				GlStateManager.glEnableClientState(32884);
				GlStateManager.glVertexPointer(3, 5126, 12, 0);
				this.starVBO.drawArrays(7);
				this.starVBO.unbindBuffer();
				GlStateManager.glDisableClientState(32884);
			}
			else
			{
				GlStateManager.callList(this.starGLCallList);
			}
		}
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
		GlStateManager.pushMatrix();
		GlStateManager.disableTexture2D();
		GlStateManager.enableFog();
		//Vec3d vec = theWorld.getFogColor(partialTicks);
		//Round mask
		float r = FogRenderer.red;
		float g = FogRenderer.green;
		float b = FogRenderer.blue;
		double h = (mc.player.getPositionEyes(partialTicks).y - FogRenderer.fogHeight) * 1.1D;
		double hd = mc.player.getPositionEyes(partialTicks).y;
		double d = (mc.gameSettings.renderDistanceChunks + 2) * 8;
		int a = 16;
		double w = Math.sin(Math.toRadians(180 / a)) * d;
		double u = Math.cos(Math.toRadians(180 / a)) * d;
		for (float j = 0; j < 360; j += 360 / a) {
			GlStateManager.rotate(360 / a, 0.0F, 1.0F, 0.0F);
			vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
			vertexbuffer.pos(-u, -h / 2, -w).color(r, g, b, 0.5F).endVertex();
			vertexbuffer.pos(-u, -h / 2, w).color(r, g, b, 0.5F).endVertex();
			vertexbuffer.pos(-u, -hd, w).color(r, g, b, 0.5F).endVertex();
			vertexbuffer.pos(-u, -hd, -w).color(r, g, b, 1.0F).endVertex();
			tessellator.draw();
		}
		GlStateManager.popMatrix();
		GlStateManager.enableAlpha();
		GlStateManager.color(0.0F, 0.0F, 0.0F);
		double d0 = mc.player.getPositionEyes(partialTicks).y - theWorld.getHorizon();
		if (d0 < 0.0D)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0F, 12.0F, 0.0F);
			if (this.vboEnabled)
			{
				this.sky2VBO.bindBuffer();
				GlStateManager.glEnableClientState(32884);
				GlStateManager.glVertexPointer(3, 5126, 12, 0);
				this.sky2VBO.drawArrays(7);
				this.sky2VBO.unbindBuffer();
				GlStateManager.glDisableClientState(32884);
			}
			else
			{
				GlStateManager.callList(this.glSkyList2);
			}
			GlStateManager.popMatrix();
			float f18 = 1.0F;
			float f19 = -((float)(d0 + 65.0D));
			float f20 = -1.0F;
			vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
			vertexbuffer.pos(-1.0D, f19, 1.0D).color(0, 0, 0, 255).endVertex();
			vertexbuffer.pos(1.0D, f19, 1.0D).color(0, 0, 0, 255).endVertex();
			vertexbuffer.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
			vertexbuffer.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
			vertexbuffer.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
			vertexbuffer.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
			vertexbuffer.pos(1.0D, f19, -1.0D).color(0, 0, 0, 255).endVertex();
			vertexbuffer.pos(-1.0D, f19, -1.0D).color(0, 0, 0, 255).endVertex();
			vertexbuffer.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
			vertexbuffer.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
			vertexbuffer.pos(1.0D, f19, 1.0D).color(0, 0, 0, 255).endVertex();
			vertexbuffer.pos(1.0D, f19, -1.0D).color(0, 0, 0, 255).endVertex();
			vertexbuffer.pos(-1.0D, f19, -1.0D).color(0, 0, 0, 255).endVertex();
			vertexbuffer.pos(-1.0D, f19, 1.0D).color(0, 0, 0, 255).endVertex();
			vertexbuffer.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
			vertexbuffer.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
			vertexbuffer.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
			vertexbuffer.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
			vertexbuffer.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
			vertexbuffer.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
			tessellator.draw();
		}
		if (theWorld.provider.isSkyColored())
		{
			GlStateManager.color(f * 0.2F + 0.04F, f1 * 0.2F + 0.04F, f2 * 0.6F + 0.1F);
		}
		else
		{
			GlStateManager.color(f, f1, f2);
		}
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, -((float)(d0 - 16.0D)), 0.0F);
		GlStateManager.callList(this.glSkyList2);
		GlStateManager.popMatrix();
		GlStateManager.enableTexture2D();
		GlStateManager.depthMask(true);
	}

	private void generateSky2()
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexbuffer = tessellator.getBuffer();
		if (this.sky2VBO != null)
		{
			this.sky2VBO.deleteGlBuffers();
		}
		if (this.glSkyList2 >= 0)
		{
			GLAllocation.deleteDisplayLists(this.glSkyList2);
			this.glSkyList2 = -1;
		}
		if (this.vboEnabled)
		{
			this.sky2VBO = new VertexBuffer(this.vertexBufferFormat);
			this.renderSky(vertexbuffer, -16.0F, true);
			vertexbuffer.finishDrawing();
			vertexbuffer.reset();
			this.sky2VBO.bufferData(vertexbuffer.getByteBuffer());
		}
		else
		{
			this.glSkyList2 = GLAllocation.generateDisplayLists(1);
			GlStateManager.glNewList(this.glSkyList2, 4864);
			this.renderSky(vertexbuffer, -16.0F, true);
			tessellator.draw();
			GlStateManager.glEndList();
		}
	}

	private void generateSky()
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexbuffer = tessellator.getBuffer();
		if (this.skyVBO != null)
		{
			this.skyVBO.deleteGlBuffers();
		}
		if (this.glSkyList >= 0)
		{
			GLAllocation.deleteDisplayLists(this.glSkyList);
			this.glSkyList = -1;
		}
		if (this.vboEnabled)
		{
			this.skyVBO = new VertexBuffer(this.vertexBufferFormat);
			this.renderSky(vertexbuffer, 16.0F, false);
			vertexbuffer.finishDrawing();
			vertexbuffer.reset();
			this.skyVBO.bufferData(vertexbuffer.getByteBuffer());
		}
		else
		{
			this.glSkyList = GLAllocation.generateDisplayLists(1);
			GlStateManager.glNewList(this.glSkyList, 4864);
			this.renderSky(vertexbuffer, 16.0F, false);
			tessellator.draw();
			GlStateManager.glEndList();
		}
	}

	private void renderSky(BufferBuilder worldRendererIn, float posY, boolean reverseX)
	{
		int i = 64;
		int j = 6;
		worldRendererIn.begin(7, DefaultVertexFormats.POSITION);
		for (int k = -384; k <= 384; k += 64)
		{
			for (int l = -384; l <= 384; l += 64)
			{
				float f = k;
				float f1 = k + 64;
				if (reverseX)
				{
					f1 = k;
					f = k + 64;
				}
				worldRendererIn.pos(f, posY, l).endVertex();
				worldRendererIn.pos(f1, posY, l).endVertex();
				worldRendererIn.pos(f1, posY, l + 64).endVertex();
				worldRendererIn.pos(f, posY, l + 64).endVertex();
			}
		}
	}

	private void generateStars()
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexbuffer = tessellator.getBuffer();
		if (this.starVBO != null)
		{
			this.starVBO.deleteGlBuffers();
		}
		if (this.starGLCallList >= 0)
		{
			GLAllocation.deleteDisplayLists(this.starGLCallList);
			this.starGLCallList = -1;
		}
		if (this.vboEnabled)
		{
			this.starVBO = new VertexBuffer(this.vertexBufferFormat);
			this.renderStars(vertexbuffer);
			vertexbuffer.finishDrawing();
			vertexbuffer.reset();
			this.starVBO.bufferData(vertexbuffer.getByteBuffer());
		}
		else
		{
			this.starGLCallList = GLAllocation.generateDisplayLists(1);
			GlStateManager.pushMatrix();
			GlStateManager.glNewList(this.starGLCallList, 4864);
			this.renderStars(vertexbuffer);
			tessellator.draw();
			GlStateManager.glEndList();
			GlStateManager.popMatrix();
		}
	}

	private void renderStars(BufferBuilder worldRendererIn)
	{
		Random random = new Random(10842L);
		worldRendererIn.begin(7, DefaultVertexFormats.POSITION);
		for (int i = 0; i < 1500; ++i)
		{
			double d0 = random.nextFloat() * 2.0F - 1.0F;
			double d1 = random.nextFloat() * 2.0F - 1.0F;
			double d2 = random.nextFloat() * 2.0F - 1.0F;
			double d3 = 0.15F + random.nextFloat() * 0.1F;
			double d4 = d0 * d0 + d1 * d1 + d2 * d2;
			if (d4 < 1.0D && d4 > 0.01D)
			{
				d4 = 1.0D / Math.sqrt(d4);
				d0 = d0 * d4;
				d1 = d1 * d4;
				d2 = d2 * d4;
				double d5 = d0 * 100.0D;
				double d6 = d1 * 100.0D;
				double d7 = d2 * 100.0D;
				double d8 = Math.atan2(d0, d2);
				double d9 = Math.sin(d8);
				double d10 = Math.cos(d8);
				double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
				double d12 = Math.sin(d11);
				double d13 = Math.cos(d11);
				double d14 = random.nextDouble() * Math.PI * 2.0D;
				double d15 = Math.sin(d14);
				double d16 = Math.cos(d14);
				for (int j = 0; j < 4; ++j)
				{
					double d17 = 0.0D;
					double d18 = ((j & 2) - 1) * d3;
					double d19 = ((j + 1 & 2) - 1) * d3;
					double d20 = 0.0D;
					double d21 = d18 * d16 - d19 * d15;
					double d22 = d19 * d16 + d18 * d15;
					double d23 = d21 * d12 + 0.0D * d13;
					double d24 = 0.0D * d12 - d21 * d13;
					double d25 = d24 * d9 - d22 * d10;
					double d26 = d22 * d9 + d24 * d10;
					worldRendererIn.pos(d5 + d25, d6 + d23, d7 + d26).endVertex();
				}
			}
		}
	}
}
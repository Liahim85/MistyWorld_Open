package ru.liahim.mist.handlers;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import ru.liahim.mist.common.Mist;
import ru.liahim.mist.init.ModConfig;
import ru.liahim.mist.init.ModConfig.Graphic;
import ru.liahim.mist.shader.ShaderProgram;
import ru.liahim.mist.util.FogTexture;
import ru.liahim.mist.world.MistWorld;

/**@author Liahim*/
public class FogRenderer {  //�������� ����� � �������!!!

	private static final Minecraft mc = Minecraft.getMinecraft();
	private static final ResourceLocation locationFogPng = new ResourceLocation("mist:textures/environment/fog.png");
	private static final ResourceLocation CLOUDS_TEXTURES = new ResourceLocation("textures/environment/clouds.png");
	public static float fogHeight = MistWorld.getFogMaxHight() + 4;
	public static float depth;
	public static float density;
	public static float red;
	public static float green;
	public static float blue;
	public static float cel;
	private boolean inBlock;
	private int prevPlayerTick;
	private int cloudTickCounter;
	private boolean aboveClouds;
	private float roof;

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderEvent(RenderTickEvent e) {
		if (e.phase == TickEvent.Phase.START && e.side == Side.CLIENT) {
			Entity entity = FogRenderer.mc.getRenderViewEntity();
			if (entity != null) {
				World world = entity.world;
				if (world.isRemote && world.provider.getDimension() == Mist.getID()) {
					float f = MathHelper.cos(world.getCelestialAngle(e.renderTickTime) * ((float)Math.PI * 2F)) * 2.0F + 0.5F;
					FogRenderer.cel = MathHelper.clamp(f, 0.0F, 1.0F);
					this.roof += (getRoof(world, new BlockPos(entity).up().add((int)-(Math.sin(Math.toRadians(entity.rotationYaw)) * 4), 0, (int)(Math.cos(Math.toRadians(entity.rotationYaw)) * 4))) - this.roof) * 0.1F;
					FogRenderer.fogHeight = MistWorld.getFogHight(world, e.renderTickTime) + 4.0F;
					FogRenderer.depth = (float)(FogRenderer.fogHeight - entity.getPositionEyes(e.renderTickTime).y);
					if (this.prevPlayerTick != entity.ticksExisted) {
						++this.cloudTickCounter;
						this.prevPlayerTick = entity.ticksExisted;
					}
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void fogDensity(FogDensity e) {
		Entity entity = e.getEntity();
		World world = entity.world;
		if (world.isRemote && world.provider.getDimension() == Mist.getID()) {
			float tick = (float)e.getRenderPartialTicks();
			float densityUp = Math.max(0.004F, 0.004F * 16 / (FogRenderer.mc.gameSettings.renderDistanceChunks)) + world.getRainStrength(tick) * 0.005F + getMorningFog(world, tick);
			float densityBorder = 0.15F;
			float densityDown = 0.08F;
			float depth = FogRenderer.depth;
			if (depth < 0) {
				FogRenderer.density = densityUp;
				GlStateManager.setFog(GlStateManager.FogMode.EXP2);
			} else if (depth < 4) {
				FogRenderer.density = calculate(densityUp, densityBorder, densityUp, densityBorder, depth, 4);
				GlStateManager.setFog(GlStateManager.FogMode.EXP);
			} else if (depth < 8) {
				FogRenderer.density = calculate(densityBorder, densityDown, densityDown, densityBorder, depth - 4, 4);
				GlStateManager.setFog(GlStateManager.FogMode.EXP);
			} else {
				FogRenderer.density = densityDown;
				GlStateManager.setFog(GlStateManager.FogMode.EXP);
			}
			if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPotionActive(MobEffects.BLINDNESS)) {
				float d1 = 0.3F;
				int i = ((EntityLivingBase)entity).getActivePotionEffect(MobEffects.BLINDNESS).getDuration();
				if (i < 20)
					d1 = FogRenderer.density + (d1 - FogRenderer.density) * i / 20.0F;
				FogRenderer.density = d1;
			} else if (e.getState().getMaterial() == Material.WATER) {
				GlStateManager.setFog(GlStateManager.FogMode.EXP);
				if (entity instanceof EntityLivingBase) {
					if (((EntityLivingBase)entity).isPotionActive(MobEffects.WATER_BREATHING)) {
						FogRenderer.density = Math.max(FogRenderer.density, 0.01F);
					} else {
						FogRenderer.density = Math.max(FogRenderer.density, 0.1F - EnchantmentHelper.getRespirationModifier((EntityLivingBase)entity) * 0.03F);
					}
				} else {
					FogRenderer.density = 0.1F;
				}
			} else if (e.getState().getMaterial() == Material.LAVA) {
				GlStateManager.setFog(GlStateManager.FogMode.EXP);
				FogRenderer.density = 2.0F;
			}
			e.setDensity(FogRenderer.density);
			e.setCanceled(true);
		}
	}

	private float getMorningFog(World world, float tick) {
		float i = ((world.getWorldTime() + 23000) % 24000) + tick;
		i = Math.abs(21000 - i);
		i = 3000 - MathHelper.clamp(i, 0, 3000);
		return i/600000;
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void fogColor(FogColors e) {
		Entity entity = e.getEntity();
		World world = entity.world;
		if (world.isRemote && world.provider.getDimension() == Mist.getID()) {
			if (e.getState().getMaterial() != Material.WATER && e.getState().getMaterial() != Material.LAVA) {
				float tick = (float)e.getRenderPartialTicks();
				Vec3d colUp;
				float rUp;
				float gUp;
				float bUp;
				Vec3d colBorder;
				float rBorder;
				float gBorder;
				float bBorder;
				Vec3d colDown;
				float rDown;
				float gDown;
				float bDown;
				float depth = FogRenderer.depth;
				if (depth < 0) {
					colUp = getFogUpColor(world, tick);
					FogRenderer.red = (float)colUp.x;
					FogRenderer.green = (float)colUp.y;
					FogRenderer.blue = (float)colUp.z;
				} else if (depth < 4) {
					colUp = getFogUpColor(world, tick);
					colBorder = getFogBorderColor(world, tick);
					rUp = (float)colUp.x;
					gUp = (float)colUp.y;
					bUp = (float)colUp.z;
					rBorder = (float)colBorder.x;
					gBorder = (float)colBorder.y;
					bBorder = (float)colBorder.z;
					float r = calculate(rUp, rBorder, Math.min(rUp, rBorder), Math.max(rUp, rBorder), depth, 4);
					float g = calculate(gUp, gBorder, Math.min(gUp, gBorder), Math.max(gUp, gBorder), depth, 4);
					float b = calculate(bUp, bBorder, Math.min(bUp, bBorder), Math.max(bUp, bBorder), depth, 4);
					FogRenderer.red = r;
					FogRenderer.green = g;
					FogRenderer.blue = b;
				} else if (depth < 8) {
					colBorder = getFogBorderColor(world, tick);
					colDown = getFogDownColor(world, tick);
					rBorder = (float)colBorder.x;
					gBorder = (float)colBorder.y;
					bBorder = (float)colBorder.z;
					rDown = (float)colDown.x;
					gDown = (float)colDown.y;
					bDown = (float)colDown.z;
					float r = calculate(rBorder, rDown, Math.min(rBorder, rDown), Math.max(rBorder, rDown), depth - 4, 4);
					float g = calculate(gBorder, gDown, Math.min(gBorder, gDown), Math.max(gBorder, gDown), depth - 4, 4);
					float b = calculate(bBorder, bDown, Math.min(bBorder, bDown), Math.max(bBorder, bDown), depth - 4, 4);
					FogRenderer.red = r;
					FogRenderer.green = g;
					FogRenderer.blue = b;
				} else {
					colDown = getFogDownColor(world, tick);
					FogRenderer.red = (float)colDown.x;
					FogRenderer.green = (float)colDown.y;
					FogRenderer.blue = (float)colDown.z;
				}
				double d1 = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * e.getRenderPartialTicks()) * world.provider.getVoidFogYFactor();
				if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPotionActive(MobEffects.BLINDNESS)) {
					int i = ((EntityLivingBase)entity).getActivePotionEffect(MobEffects.BLINDNESS).getDuration();
					if (i < 20)
						d1 *= 1.0F - i / 20.0F;
					else d1 = 0.0D;
				}
				if (d1 < 1.0D) {
					if (d1 < 0.0D)
						d1 = 0.0D;
					d1 = d1 * d1;
					FogRenderer.red = (float)(FogRenderer.red * d1);
					FogRenderer.green = (float)(FogRenderer.green * d1);
					FogRenderer.blue = (float)(FogRenderer.blue * d1);
				}
				if (FogRenderer.mc.gameSettings.anaglyph) {
					float fr = (FogRenderer.red * 30.0F + FogRenderer.green * 59.0F + FogRenderer.blue * 11.0F) / 100.0F;
					float fg = (FogRenderer.red * 30.0F + FogRenderer.green * 70.0F) / 100.0F;
					float fb = (FogRenderer.red * 30.0F + FogRenderer.blue * 70.0F) / 100.0F;
					FogRenderer.red = fr;
					FogRenderer.green = fg;
					FogRenderer.blue = fb;
				}
			} else {
				if (e.getState().getMaterial() == Material.WATER) {
					float wb = 0.0F;
					if (entity instanceof EntityLivingBase && FogRenderer.depth < 0) {
						wb = EnchantmentHelper.getRespirationModifier((EntityLivingBase)entity) * 0.2F;
						if (((EntityLivingBase)entity).isPotionActive(MobEffects.WATER_BREATHING))
							wb = wb * 0.3F + 0.6F;
					}
					FogRenderer.red = 0.02F + wb;
					FogRenderer.green = 0.02F + wb;
					FogRenderer.blue = 0.2F + wb;
				} else if (e.getState().getMaterial() == Material.LAVA) {
					FogRenderer.red = 0.6F;
					FogRenderer.green = 0.1F;
					FogRenderer.blue = 0.0F;
				}
			}
			e.setRed(FogRenderer.red);
			e.setGreen(FogRenderer.green);
			e.setBlue(FogRenderer.blue);
		}
	}

	@SideOnly(Side.CLIENT)
	private Vec3d getFogUpColor(World world, float partialTicks, float cel) {
		Entity entity = FogRenderer.mc.getRenderViewEntity();
		float d = 0.25F + 0.75F * FogRenderer.mc.gameSettings.renderDistanceChunks / 32.0F;
		d = 1.0F - (float)Math.pow(d, 0.25D);
		Vec3d vec3d = world.getSkyColor(entity, partialTicks);
		float f1 = (float)vec3d.x;
		float f2 = (float)vec3d.y;
		float f3 = (float)vec3d.z;
		float r = 160F / 255;//170
		float g = 210F / 255;
		float b = 210F / 255;//200
		r = r * (cel * 0.82F + 0.18F);
		g = g * (cel * 0.79F + 0.21F);
		b = b * (cel * 0.81F + 0.19F);
		if (FogRenderer.mc.gameSettings.renderDistanceChunks >= 4) {
			double d0 = MathHelper.sin(world.getCelestialAngleRadians(partialTicks)) > 0.0F ? -1.0D : 1.0D;
			Vec3d vec3d2 = new Vec3d(d0, 0.0D, 0.0D);
			float f = (float)entity.getLook(partialTicks).dotProduct(vec3d2);
			if (f < 0.0F)
				f = 0.0F;
			if (f > 0.0F) {
				float[] afloat = world.provider.calcSunriseSunsetColors(world.getCelestialAngle(partialTicks), partialTicks);
				if (afloat != null) {
					f = f * afloat[3];
					r = r * (1.0F - f) + afloat[0] * f;
					g = g * (1.0F - f) + afloat[1] * f;
					b = b * (1.0F - f) + afloat[2] * f;
				}
			}
		}
		r += (f1 - r) * d;
		g += (f2 - g) * d;
		b += (f3 - b) * d;
		float rain = world.getRainStrength(partialTicks);
		if (rain > 0.0F) {
			float f4 = 1.0F - rain * 0.4F;
			float f5 = 1.0F - rain * 0.38F;
			r *= f4;
			g *= f4;
			b *= f5;
		}
		float thunder = world.getThunderStrength(partialTicks);
		if (thunder > 0.0F) {
			float f6 = 1.0F - thunder * 0.58F;
			float f7 = 1.0F - thunder * 0.56F;
			r *= f6;
			g *= f6;
			b *= f7;
		}
		r *= 0.75F + 0.25F * this.roof;
		g *= 0.8F + 0.2F * this.roof;
		b *= 0.85F + 0.15F * this.roof;
		if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPotionActive(MobEffects.NIGHT_VISION)) {
			float f8 = this.getNightVisionBrightness((EntityLivingBase)entity, partialTicks);
			float f9 = 1.0F / r;
			if (f9 > 1.0F / g)
				f9 = 1.0F / g;
			if (f9 > 1.0F / b)
				f9 = 1.0F / b;
			r = r * (1.0F - f8 * 0.5F) + r * f9 * f8 * 0.5F;
			g = g * (1.0F - f8 * 0.4F) + g * f9 * f8 * 0.4F;
			b = b * (1.0F - f8 * 0.65F) + b * f9 * f8 * 0.65F;
		}
		return new Vec3d(r, g, b);
	}

	@SideOnly(Side.CLIENT)
	private Vec3d getFogBorderColor(World world, float partialTicks, float cel) {
		float r = 1.0F;
		float g = 1.0F;
		float b = 1.0F;
		r = r * (cel * 0.62F + 0.38F);
		g = g * (cel * 0.59F + 0.41F);
		b = b * (cel * 0.56F + 0.44F);
		float rain = world.getRainStrength(partialTicks);
		if (rain > 0.0F) {
			float f4 = 1.0F - rain * 0.45F * cel;
			float f5 = 1.0F - rain * 0.38F * cel;
			r *= f4;
			g *= f5;
			b *= f5;
		}
		float thunder = world.getThunderStrength(partialTicks);
		if (thunder > 0.0F) {
			float f6 = 1.0F - thunder * 0.48F * cel;
			float f7 = 1.0F - thunder * 0.42F * cel;
			float f8 = 1.0F - thunder * 0.39F * cel;
			r *= f6;
			g *= f7;
			b *= f8;
		}
		r = r * this.roof + 0.5F * (1.0F - this.roof);
		g = g * this.roof + 0.6F * (1.0F - this.roof);
		b = b * this.roof + 0.7F * (1.0F - this.roof);
		Entity entity = FogRenderer.mc.getRenderViewEntity();
		if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPotionActive(MobEffects.NIGHT_VISION)) {
			float f8 = this.getNightVisionBrightness((EntityLivingBase)entity, partialTicks);
			float f9 = 1.0F / r;
			if (f9 > 1.0F / g)
				f9 = 1.0F / g;
			if (f9 > 1.0F / b)
				f9 = 1.0F / b;
			r = r * (1.0F - f8) + r * f9 * f8;
			g = g * (1.0F - f8 * 0.95F) + g * f9 * f8 * 0.95F;
			b = b * (1.0F - f8) + b * f9 * f8;
		}
		return new Vec3d(r, g, b);
	}

	@SideOnly(Side.CLIENT)
	private Vec3d getFogDownColor(World world, float partialTicks, float cel) {
		float r = 0.63F;
		float g = 0.70F;
		float b = 0.67F;
		r = r * (cel * 0.75F + 0.25F);
		g = g * (cel * 0.70F + 0.30F);
		b = b * (cel * 0.65F + 0.35F);
		float rain = world.getRainStrength(partialTicks);
		if (rain > 0.0F) {
			float f4 = 1.0F - rain * 0.40F * cel;
			float f5 = 1.0F - rain * 0.35F * cel;
			r *= f4;
			g *= f5;
			b *= f5;
		}
		float thunder = world.getThunderStrength(partialTicks);
		if (thunder > 0.0F) {
			float f6 = 1.0F - thunder * 0.50F * cel;
			float f7 = 1.0F - thunder * 0.45F * cel;
			float f8 = 1.0F - thunder * 0.40F * cel;
			r *= f6;
			g *= f7;
			b *= f8;
		}
		if (world.getLastLightningBolt() > 0) {
			float f10 = world.getLastLightningBolt() - partialTicks;
			if (f10 > 1.0F)
				f10 = 1.0F;
			f10 = f10 * 0.25F;
			r = r * (1.0F - f10) + 0.8F * f10;
			g = g * (1.0F - f10) + 0.8F * f10;
			b = b * (1.0F - f10) + 1.0F * f10;
		}
		r = r * this.roof + 0.1F * (1.0F - this.roof);
		g = g * this.roof + 0.12F * (1.0F - this.roof);
		b = b * this.roof + 0.15F * (1.0F - this.roof);
		Entity entity = FogRenderer.mc.getRenderViewEntity();
		if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPotionActive(MobEffects.NIGHT_VISION)) {
			float f8 = this.getNightVisionBrightness((EntityLivingBase)entity, partialTicks);
			float f9 = 1.0F / r;
			if (f9 > 1.0F / g)
				f9 = 1.0F / g;
			if (f9 > 1.0F / b)
				f9 = 1.0F / b;
			r = r * (1.0F - f8 * (0.85F + 0.1F * this.roof)) + r * f9 * f8 * (0.85F + 0.1F * this.roof);
			g = g * (1.0F - f8 * 0.85F) + g * f9 * f8 * 0.85F;
			b = b * (1.0F - f8 * (1.0F - 0.1F * this.roof)) + b * f9 * f8 * (1.0F - 0.1F * this.roof);
		}
		return new Vec3d(r, g, b);
	}

	@SideOnly(Side.CLIENT)
	private Vec3d getFogLayerColor(World world, float partialTicks, float cel) {
		float r = 1.0F;
		float g = 1.0F;
		float b = 1.0F;
		r = r * (cel * 0.85F + 0.15F);
		g = g * (cel * 0.82F + 0.18F);
		b = b * (cel * 0.80F + 0.20F);
		float rain = world.getRainStrength(partialTicks);
		if (rain > 0.0F) {
			float f4 = 1.0F - rain * 0.4F * cel;
			float f5 = 1.0F - rain * 0.38F * cel;
			float f6 = 1.0F - rain * 0.35F * cel;
			r *= f4;
			g *= f5;
			b *= f6;
		}
		float thunder = world.getThunderStrength(partialTicks);
		if (thunder > 0.0F) {
			float f7 = 1.0F - thunder * 0.55F * cel;
			float f8 = 1.0F - thunder * 0.50F * cel;
			float f9 = 1.0F - thunder * 0.45F * cel;
			r *= f7;
			g *= f8;
			b *= f9;
		}
		r *= 0.75F + 0.25F * this.roof;
		g *= 0.8F + 0.2F * this.roof;
		b *= 0.85F + 0.15F * this.roof;
		Entity entity = FogRenderer.mc.getRenderViewEntity();
		if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPotionActive(MobEffects.NIGHT_VISION)) {
			float f8 = this.getNightVisionBrightness((EntityLivingBase)entity, partialTicks);
			float f9 = 1.0F / r;
			if (f9 > 1.0F / g)
				f9 = 1.0F / g;
			if (f9 > 1.0F / b)
				f9 = 1.0F / b;
			r = r * (1.0F - f8) + r * f9 * f8;
			g = g * (1.0F - f8) + g * f9 * f8;
			b = b * (1.0F - f8) + b * f9 * f8;
		}
		return new Vec3d(r, g, b);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderEvent(RenderWorldLastEvent e) {
		World world = FogRenderer.mc.player.world;
		if (world.isRemote && world.provider.getDimension() == Mist.getID()) {
			if (this.inBlock || this.aboveClouds) {
				if (this.inBlock) {
					if (ModConfig.graphic.advancedFogRenderer) {
						fogRender(e.getPartialTicks(), (WorldClient)world, FogRenderer.mc);
					} else fogRenderOld(e.getPartialTicks(), (WorldClient)world, FogRenderer.mc);
				}
				this.renderCloudsCheck(e.getPartialTicks());
				GlStateManager.disableFog();
			}
			this.inBlock = true;
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderEvent(DrawBlockHighlightEvent e) {
		World world = FogRenderer.mc.player.world;
		if (world.isRemote && world.provider.getDimension() == Mist.getID()) {
			Entity entity = FogRenderer.mc.getRenderViewEntity();
			e.getContext().drawSelectionBox(mc.player, mc.objectMouseOver, 0, e.getPartialTicks());
			if (ModConfig.graphic.advancedFogRenderer) {
				fogRender(e.getPartialTicks(), (WorldClient)world, FogRenderer.mc);
			} else fogRenderOld(e.getPartialTicks(), (WorldClient)world, FogRenderer.mc);
			if (entity.posY + entity.getEyeHeight() < FogRenderer.mc.world.provider.getCloudHeight()) {
				GlStateManager.enableAlpha();
				this.renderCloudsCheck(e.getPartialTicks());
				GlStateManager.disableAlpha();
				this.aboveClouds = false;
			}
			else this.aboveClouds = true;
			this.inBlock = false;
		}
	}

	private void fogRender(float partialTicks, WorldClient world, Minecraft mc) {
		Entity entity = mc.getRenderViewEntity();
		float cameraHeight = mc.getRenderViewEntity().getEyeHeight();
		float playerHeight = (float)(entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks);
		double playerX = (entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks);
		double playerZ = (entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks);
		int cX = MathHelper.floor(playerX / 16.0D);
		int cZ = MathHelper.floor(playerZ / 16.0D);
		int temp = FogRenderer.depth < 4 ? MathHelper.floor(FogRenderer.fogHeight - 0.0025F) : MathHelper.ceil(FogRenderer.fogHeight - 9 + 0.0025F);
		if (FogTexture.chunkX != cX || FogTexture.chunkZ != cZ || FogTexture.fogHeight != temp) {
			FogTexture.chunkX = cX;
			FogTexture.chunkZ = cZ;
			FogTexture.fogHeight = temp;
			FogTexture.createFogTexture(world, cX, cZ);
		}
		if (FogTexture.offset != mc.gameSettings.renderDistanceChunks + 2) {
			FogTexture.offset = mc.gameSettings.renderDistanceChunks + 2;
			FogTexture.allocateTexture();
			FogTexture.createFogTexture(world, cX, cZ);
		}
		byte i = 4; //Segment count
		int r = FogTexture.offset * 16 + 8;
		int e = r / i; //Segment size
		int d = r * 2;
		playerX = playerX - cX * 16 - 8;
		playerZ = playerZ - cZ * 16 - 8;

		float f2;
		float f3;
		float f4;
		if (FogRenderer.depth < 4) {
			Vec3d vecLayer = getFogLayerColor(world, partialTicks);
			f2 = (float)vecLayer.x;
			f3 = (float)vecLayer.y;
			f4 = (float)vecLayer.z;
		} else {
			Vec3d vecDown = getFogDownColor(world, partialTicks);
			f2 = (float)vecDown.x;
			f3 = (float)vecDown.y;
			f4 = (float)vecDown.z;
		}
		float colorOffset;
		float layerOffset = -0.0025F;
		float height;

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexbuffer = tessellator.getBuffer();
		FogTexture.bindTexture();
		GlStateManager.enableBlend();
		GlStateManager.enableFog();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		ShaderProgram.useShader(ShaderProgram.fog);
		ShaderProgram.setUniform2f("center", (float)(playerX + 8 + FogTexture.offset * 16) / d, (float)(playerZ + 8 + FogTexture.offset * 16) / d);
		ShaderProgram.setUniform3f("main_color", f2, f3, f4);
		ShaderProgram.setUniform1f("fog_smooth", Graphic.smoothFogTexture ? 1 : 0);
		for (int n = 0; n < 64; n++) {
			height = FogRenderer.fogHeight - 4 - playerHeight + layerOffset;
			colorOffset = 0.63F - n * 0.01F;
			ShaderProgram.setUniform1f("offset", colorOffset);
			if (height - cameraHeight < -0.1) {
				vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
				ShaderProgram.setUniform1f("alpha", 0.075F);
				ShaderProgram.setUniform1f("deep", FogRenderer.fogHeight - 4 + layerOffset - MathHelper.floor(FogRenderer.fogHeight - 0.0025f));
				for (int x = -e * i; x < e * i; x += e) {
					for (int z = -e * i; z < e * i; z += e) {
						vertexbuffer.pos(x - playerX, height, z - playerZ).tex((float)(r + x) / d, (float)(r + z) / d).endVertex();
						vertexbuffer.pos(x - playerX, height, z + e - playerZ).tex((float)(r + x) / d, (float)(r + z + e) / d).endVertex();
						vertexbuffer.pos(x + e - playerX, height, z + e - playerZ).tex((float)(r + x + e) / d, (float)(r + z + e) / d).endVertex();
						vertexbuffer.pos(x + e - playerX, height, z - playerZ).tex((float)(r + x + e) / d, (float)(r + z) / d).endVertex();
					}
				}
				tessellator.draw();
			}
			height = FogRenderer.fogHeight - 4 - playerHeight - layerOffset;
			if (height - cameraHeight > 0.1) {
				vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
				ShaderProgram.setUniform1f("alpha", Math.min(0.5F, 0.075F + Math.max(0, (height) / 100)));
				ShaderProgram.setUniform1f("deep", MathHelper.ceil(FogRenderer.fogHeight - 8 + 0.0025f) - FogRenderer.fogHeight + 4 + layerOffset);
				for (int x = -e * i; x < e * i; x += e) {
					for (int z = -e * i; z < e * i; z += e) {
						vertexbuffer.pos(x - playerX, height, z - playerZ).tex((float)(r + x) / d, (float)(r + z) / d).endVertex();
						vertexbuffer.pos(x + e - playerX, height, z - playerZ).tex((float)(r + x + e) / d, (float)(r + z) / d).endVertex();
						vertexbuffer.pos(x + e - playerX, height, z + e - playerZ).tex((float)(r + x + e) / d, (float)(r + z + e) / d).endVertex();
						vertexbuffer.pos(x - playerX, height, z + e - playerZ).tex((float)(r + x) / d, (float)(r + z + e) / d).endVertex();
					}
				}
				tessellator.draw();
			}
			layerOffset += 0.0625F;
		}
		ShaderProgram.releaseShader();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.disableBlend();
	}
	
	private void fogRenderOld(float partialTicks, WorldClient world, Minecraft mc) {
		Entity entity = mc.getRenderViewEntity();
		float cameraHeight = mc.getRenderViewEntity().getEyeHeight();
		float playerHeight = (float)(entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks);
		byte i = 4; //Segment count
		int r = (mc.gameSettings.renderDistanceChunks + 2) * 16;
		int e = r / i; //Segment size
		int d = r * 2;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexbuffer = tessellator.getBuffer();
		mc.getTextureManager().bindTexture(locationFogPng);
		GlStateManager.enableBlend();
		GlStateManager.enableFog();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		float f2;
		float f3;
		float f4;
		if (FogRenderer.depth < 4) {
			Vec3d vecLayer = getFogLayerColor(world, partialTicks);
			f2 = (float)vecLayer.x;
			f3 = (float)vecLayer.y;
			f4 = (float)vecLayer.z;
		} else {
			Vec3d vecDown = getFogDownColor(world, partialTicks);
			f2 = (float)vecDown.x;
			f3 = (float)vecDown.y;
			f4 = (float)vecDown.z;
		}
		float colorOffset;
		float layerOffset = -0.0025F;
		float height; float red; float green; float blue; float alpha;
		for (int n = 0; n < 64; n++) {
			height = FogRenderer.fogHeight - 4 - playerHeight + layerOffset;
			colorOffset = 0.63F - n * 0.01F;
			if (height - cameraHeight < -0.1) {
				vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
				red = Math.min(1, f2 + colorOffset);
				green = Math.min(1, f3 + colorOffset);
				blue = Math.min(1, f4 + colorOffset);
				alpha = 0.07F;
				for (int x = -e * i; x < e * i; x += e) {
					for (int z = -e * i; z < e * i; z += e) {
						vertexbuffer.pos(x, height, z).tex((float)(r + z) / d, (float)(r - x) / d).color(red, green, blue, alpha).endVertex();
						vertexbuffer.pos(x, height, z + e).tex((float)(r + z + e) / d, (float)(r - x) / d).color(red, green, blue, alpha).endVertex();
						vertexbuffer.pos(x + e, height, z + e).tex((float)(r + z + e) / d, (float)(r - x - e) / d).color(red, green, blue, alpha).endVertex();
						vertexbuffer.pos(x + e, height, z).tex((float)(r + z) / d, (float)(r - x - e) / d).color(red, green, blue, alpha).endVertex();
					}
				}
				tessellator.draw();
			}
			height = FogRenderer.fogHeight - 4 - playerHeight - layerOffset;
			if (height - cameraHeight > 0.1) {
				vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
				red = Math.min(1, f2 + colorOffset);
				green = Math.min(1, f3 + colorOffset);
				blue = Math.min(1, f4 + colorOffset);
				alpha = Math.min(0.5F, 0.07F + Math.max(0, (height) / 100));
				for (int x = -e * i; x < e * i; x += e) {
					for (int z = -e * i; z < e * i; z += e) {
						vertexbuffer.pos(x, height, z).tex((float)(r + z) / d, (float)(r - x) / d).color(red, green, blue, alpha).endVertex();
						vertexbuffer.pos(x + e, height, z).tex((float)(r + z) / d, (float)(r - x - e) / d).color(red, green, blue, alpha).endVertex();
						vertexbuffer.pos(x + e, height, z + e).tex((float)(r + z + e) / d, (float)(r - x - e) / d).color(red, green, blue, alpha).endVertex();
						vertexbuffer.pos(x, height, z + e).tex((float)(r + z + e) / d, (float)(r - x) / d).color(red, green, blue, alpha).endVertex();
					}
				}
				tessellator.draw();
			}
			layerOffset += 0.0625F;
		}
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.disableBlend();
	}

	private void renderCloudsCheck(float partialTicks) {
		Entity entity = FogRenderer.mc.getRenderViewEntity();
		if (FogRenderer.mc.gameSettings.shouldRenderClouds() != 0 && entity.posY + entity.getEyeHeight() > FogRenderer.fogHeight - 4) {
			if (FogRenderer.mc.gameSettings.anaglyph) {
				this.renderClouds(partialTicks, EntityRenderer.anaglyphField);
			} else {
				this.renderClouds(partialTicks, 2);
			}
		}
	}

	private void renderClouds(float partialTicks, int pass) {
		if (FogRenderer.mc.gameSettings.shouldRenderClouds() == 2)
			this.renderCloudsFancy(partialTicks, pass);
		else {
			GlStateManager.disableCull();
			Entity entity = FogRenderer.mc.getRenderViewEntity();
			float f = (float)(entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks);
			int i = 32;
			int j = 8;
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder vertexbuffer = tessellator.getBuffer();
			FogRenderer.mc.renderEngine.bindTexture(CLOUDS_TEXTURES);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			Vec3d vec3d = FogRenderer.mc.world.getCloudColour(partialTicks);
			float f1 = (float)vec3d.x;
			float f2 = (float)vec3d.y;
			float f3 = (float)vec3d.z;
			if (pass != 2) {
				float f4 = (f1 * 30.0F + f2 * 59.0F + f3 * 11.0F) / 100.0F;
				float f5 = (f1 * 30.0F + f2 * 70.0F) / 100.0F;
				float f6 = (f1 * 30.0F + f3 * 70.0F) / 100.0F;
				f1 = f4;
				f2 = f5;
				f3 = f6;
			}
			float f10 = 4.8828125E-4F;
			double d2 = (this.cloudTickCounter + partialTicks);
			double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks + d2 * 0.029999999329447746D;
			double d1 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks;
			int k = MathHelper.floor(d0 / 2048.0D);
			int l = MathHelper.floor(d1 / 2048.0D);
			d0 = d0 - k * 2048;
			d1 = d1 - l * 2048;
			float f7 = FogRenderer.mc.world.provider.getCloudHeight() - f + 0.33F;
			float f8 = (float)(d0 * 4.8828125E-4D);
			float f9 = (float)(d1 * 4.8828125E-4D);
			vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
			for (int i1 = -256; i1 < 256; i1 += 32) {
				for (int j1 = -256; j1 < 256; j1 += 32) {
					vertexbuffer.pos(i1 + 0, f7, j1 + 32).tex((i1 + 0) * 4.8828125E-4F + f8, (j1 + 32) * 4.8828125E-4F + f9).color(f1, f2, f3, 0.8F).endVertex();
					vertexbuffer.pos(i1 + 32, f7, j1 + 32).tex((i1 + 32) * 4.8828125E-4F + f8, (j1 + 32) * 4.8828125E-4F + f9).color(f1, f2, f3, 0.8F).endVertex();
					vertexbuffer.pos(i1 + 32, f7, j1 + 0).tex((i1 + 32) * 4.8828125E-4F + f8, (j1 + 0) * 4.8828125E-4F + f9).color(f1, f2, f3, 0.8F).endVertex();
					vertexbuffer.pos(i1 + 0, f7, j1 + 0).tex((i1 + 0) * 4.8828125E-4F + f8, (j1 + 0) * 4.8828125E-4F + f9).color(f1, f2, f3, 0.8F).endVertex();
				}
			}
			tessellator.draw();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.disableBlend();
			GlStateManager.enableCull();
		}
	}

	private void renderCloudsFancy(float partialTicks, int pass) {
		GlStateManager.disableCull();
		Entity entity = FogRenderer.mc.getRenderViewEntity();
		float f = (float)(entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexbuffer = tessellator.getBuffer();
		float f1 = 12.0F;
		float f2 = 4.0F;
		double d0 = this.cloudTickCounter + partialTicks;
		double d1 = (entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks + d0 * 0.029999999329447746D) / 12.0D;
		double d2 = (entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks) / 12.0D + 0.33000001311302185D;
		float f3 = FogRenderer.mc.world.provider.getCloudHeight() - f + 0.33F;
		int i = MathHelper.floor(d1 / 2048.0D);
		int j = MathHelper.floor(d2 / 2048.0D);
		d1 = d1 - i * 2048;
		d2 = d2 - j * 2048;
		FogRenderer.mc.renderEngine.bindTexture(CLOUDS_TEXTURES);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		Vec3d vec3d = FogRenderer.mc.world.getCloudColour(partialTicks);
		float f4 = (float)vec3d.x;
		float f5 = (float)vec3d.y;
		float f6 = (float)vec3d.z;
		if (pass != 2) {
			float f7 = (f4 * 30.0F + f5 * 59.0F + f6 * 11.0F) / 100.0F;
			float f8 = (f4 * 30.0F + f5 * 70.0F) / 100.0F;
			float f9 = (f4 * 30.0F + f6 * 70.0F) / 100.0F;
			f4 = f7;
			f5 = f8;
			f6 = f9;
		}
		float f26 = f4 * 0.9F;
		float f27 = f5 * 0.9F;
		float f28 = f6 * 0.9F;
		float f10 = f4 * 0.7F;
		float f11 = f5 * 0.7F;
		float f12 = f6 * 0.7F;
		float f13 = f4 * 0.8F;
		float f14 = f5 * 0.8F;
		float f15 = f6 * 0.8F;
		float f16 = 0.00390625F;
		float f17 = MathHelper.floor(d1) * 0.00390625F;
		float f18 = MathHelper.floor(d2) * 0.00390625F;
		float f19 = (float)(d1 - MathHelper.floor(d1));
		float f20 = (float)(d2 - MathHelper.floor(d2));
		int k = 8;
		int l = 4;
		float f21 = 9.765625E-4F;
		GlStateManager.scale(12.0F, 1.0F, 12.0F);
		for (int i1 = 0; i1 < 2; ++i1) {
			if (i1 == 0)
				GlStateManager.colorMask(false, false, false, false);
			else {
				switch (pass) {
				case 0:
					GlStateManager.colorMask(false, true, true, true);
					break;
				case 1:
					GlStateManager.colorMask(true, false, false, true);
					break;
				case 2:
					GlStateManager.colorMask(true, true, true, true);
				}
			}
			for (int j1 = -3; j1 <= 4; ++j1) {
				for (int k1 = -3; k1 <= 4; ++k1) {
					vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
					float f22 = j1 * 8;
					float f23 = k1 * 8;
					float f24 = f22 - f19;
					float f25 = f23 - f20;
					if (f3 > -5.0F) {
						vertexbuffer.pos(f24 + 0.0F, f3 + 0.0F, f25 + 8.0F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
						vertexbuffer.pos(f24 + 8.0F, f3 + 0.0F, f25 + 8.0F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
						vertexbuffer.pos(f24 + 8.0F, f3 + 0.0F, f25 + 0.0F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
						vertexbuffer.pos(f24 + 0.0F, f3 + 0.0F, f25 + 0.0F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
					}
					if (f3 <= 5.0F) {
						vertexbuffer.pos(f24 + 0.0F, f3 + 4.0F - 9.765625E-4F, f25 + 8.0F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
						vertexbuffer.pos(f24 + 8.0F, f3 + 4.0F - 9.765625E-4F, f25 + 8.0F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
						vertexbuffer.pos(f24 + 8.0F, f3 + 4.0F - 9.765625E-4F, f25 + 0.0F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
						vertexbuffer.pos(f24 + 0.0F, f3 + 4.0F - 9.765625E-4F, f25 + 0.0F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
					}
					if (j1 > -1) {
						for (int l1 = 0; l1 < 8; ++l1) {
							vertexbuffer.pos(f24 + l1 + 0.0F, f3 + 0.0F, f25 + 8.0F).tex((f22 + l1 + 0.5F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
							vertexbuffer.pos(f24 + l1 + 0.0F, f3 + 4.0F, f25 + 8.0F).tex((f22 + l1 + 0.5F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
							vertexbuffer.pos(f24 + l1 + 0.0F, f3 + 4.0F, f25 + 0.0F).tex((f22 + l1 + 0.5F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
							vertexbuffer.pos(f24 + l1 + 0.0F, f3 + 0.0F, f25 + 0.0F).tex((f22 + l1 + 0.5F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
						}
					}
					if (j1 <= 1) {
						for (int i2 = 0; i2 < 8; ++i2) {
							vertexbuffer.pos(f24 + i2 + 1.0F - 9.765625E-4F, f3 + 0.0F, f25 + 8.0F).tex((f22 + i2 + 0.5F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
							vertexbuffer.pos(f24 + i2 + 1.0F - 9.765625E-4F, f3 + 4.0F, f25 + 8.0F).tex((f22 + i2 + 0.5F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
							vertexbuffer.pos(f24 + i2 + 1.0F - 9.765625E-4F, f3 + 4.0F, f25 + 0.0F).tex((f22 + i2 + 0.5F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
							vertexbuffer.pos(f24 + i2 + 1.0F - 9.765625E-4F, f3 + 0.0F, f25 + 0.0F).tex((f22 + i2 + 0.5F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
						}
					}
					if (k1 > -1) {
						for (int j2 = 0; j2 < 8; ++j2) {
							vertexbuffer.pos(f24 + 0.0F, f3 + 4.0F, f25 + j2 + 0.0F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + j2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
							vertexbuffer.pos(f24 + 8.0F, f3 + 4.0F, f25 + j2 + 0.0F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + j2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
							vertexbuffer.pos(f24 + 8.0F, f3 + 0.0F, f25 + j2 + 0.0F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + j2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
							vertexbuffer.pos(f24 + 0.0F, f3 + 0.0F, f25 + j2 + 0.0F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + j2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
						}
					}
					if (k1 <= 1) {
						for (int k2 = 0; k2 < 8; ++k2) {
							vertexbuffer.pos(f24 + 0.0F, f3 + 4.0F, f25 + k2 + 1.0F - 9.765625E-4F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + k2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
							vertexbuffer.pos(f24 + 8.0F, f3 + 4.0F, f25 + k2 + 1.0F - 9.765625E-4F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + k2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
							vertexbuffer.pos(f24 + 8.0F, f3 + 0.0F, f25 + k2 + 1.0F - 9.765625E-4F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + k2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
							vertexbuffer.pos(f24 + 0.0F, f3 + 0.0F, f25 + k2 + 1.0F - 9.765625E-4F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + k2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
						}
					}
					tessellator.draw();
				}
			}
		}
		GlStateManager.scale(1.0F / 12, 1.0F, 1.0F / 12);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableBlend();
		GlStateManager.enableCull();
	}

	private static int viewX, viewY, viewZ;
	private static boolean shadowInit;
	private static float shadowMultiplier;

	private float getRoof(World world, BlockPos pos) {
		if (pos.getX() == viewX && pos.getY() == viewY && pos.getZ() == viewZ && shadowInit)
			return shadowMultiplier;
		if (world.getChunkFromBlockCoords(pos).isLoaded()) shadowInit = true;
        
		float i = 0.0F;
		int count = 1;
		int r = 6;
		for (int x = -r; x <= r; x++) {
			for (int z = -r; z <= r; z++) {
				if (!world.isBlockFullCube(pos.add(x, 0, z))) {
					count += 1;
					i += world.getLightFor(EnumSkyBlock.SKY, pos.add(x, 0, z));
					/*if (world.canBlockSeeSky(pos.add(x, 0, z))) {
						i += 1;
					}*/
				}
			}
		}

		viewX = pos.getX();
		viewY = pos.getY();
		viewZ = pos.getZ();
		shadowMultiplier = (i / count) / 15;
		return shadowMultiplier;
	}

	private float calculate(float up, float down, float min, float max, float depth, float layerHeight) {
		return MathHelper.clamp(up + (down - up) * depth / layerHeight, min, max);
	}

	private float getNightVisionBrightness(EntityLivingBase entitylivingbaseIn, float partialTicks) {
		int i = entitylivingbaseIn.getActivePotionEffect(MobEffects.NIGHT_VISION).getDuration();
		return i > 200 ? 1.0F : 0.7F + MathHelper.sin((i - partialTicks) * (float)Math.PI * 0.2F) * 0.3F;
	}

	@SideOnly(Side.CLIENT)
	private Vec3d getFogUpColor(World world, float partialTicks) {
		return getFogUpColor(world, partialTicks, FogRenderer.cel);
	}

	@SideOnly(Side.CLIENT)
	private Vec3d getFogBorderColor(World world, float partialTicks) {
		return getFogBorderColor(world, partialTicks, FogRenderer.cel);
	}

	@SideOnly(Side.CLIENT)
	private Vec3d getFogDownColor(World world, float partialTicks) {
		return getFogDownColor(world, partialTicks, FogRenderer.cel);
	}

	@SideOnly(Side.CLIENT)
	private Vec3d getFogLayerColor(World world, float partialTicks) {
		return getFogLayerColor(world, partialTicks, FogRenderer.cel);
	}
}
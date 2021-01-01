package ru.liahim.mist.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.MistTags;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.entity.IOffetPassangerMount;
import ru.liahim.mist.api.item.IFilter;
import ru.liahim.mist.api.item.IMask;
import ru.liahim.mist.api.item.ISuit;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.capability.handler.IMistCapaHandler;
import ru.liahim.mist.client.model.MistModelLoader;
import ru.liahim.mist.client.model.animation.SimpleIK;
import ru.liahim.mist.common.ClientProxy;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.common.MistTime;
import ru.liahim.mist.entity.AbstractMistMount;
import ru.liahim.mist.entity.EntityGender;
import ru.liahim.mist.init.ItemColoring;
import ru.liahim.mist.init.ModConfig;
import ru.liahim.mist.inventory.gui.GuiMask;
import ru.liahim.mist.inventory.gui.GuiMaskButton;
import ru.liahim.mist.inventory.gui.GuiRespirator;
import ru.liahim.mist.inventory.gui.GuiSkills;
import ru.liahim.mist.item.ItemMistMap;
import ru.liahim.mist.network.PacketHandler;
import ru.liahim.mist.network.PacketOpenMaskInventory;
import ru.liahim.mist.network.PacketOpenNormalInventory;
import ru.liahim.mist.sound.MistMusic;
import ru.liahim.mist.tileentity.TileEntityCampfire;
import ru.liahim.mist.util.ItemStackMapKey;
import ru.liahim.mist.world.WorldProviderMist;

public class ClientEventHandler {

	private static final Minecraft mc = Minecraft.getMinecraft();
	private int prevPlayerTick;
	private boolean notSleep;
	private int prevSleepTimer;
	private int filterTimer;
	private int xPos = mc.gameSettings.mainHand == EnumHandSide.RIGHT ? 189 : -29;
	public static ISound currentSound;
	public static int fadeOut = 0;

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderGameOverlay(RenderGameOverlayEvent.Post event) {
		//Sleeping text
		if (event.getType() == ElementType.TEXT) {
			EntityPlayerSP player = mc.player;
			int dim = player.world.provider.getDimension();
			if (dim == 0 || dim == Mist.getID()) {
				int time = player.getSleepTimer();
				if (this.prevSleepTimer > time) this.notSleep = true;
				if (this.notSleep && time > 95) this.notSleep = false;
				if (this.notSleep) {
					String text = I18n.format("gui.mist.bad_sleep", new Object[0]);
					String text2 = I18n.format("gui.mist.bad_sleep.line_" + (dim == 0 ? "1" : "2"), new Object[0]);
					FontRenderer fontRenderer = mc.fontRenderer;
					ScaledResolution res = event.getResolution();
					int width = res.getScaledWidth();
					int height = res.getScaledHeight();
					fontRenderer.drawStringWithShadow(text, width/2 - fontRenderer.getStringWidth(text)/2, height/4, 0xFFFFFF);
					fontRenderer.drawStringWithShadow(text2, width/2 - fontRenderer.getStringWidth(text2)/2, height/4 + fontRenderer.FONT_HEIGHT, 0xFFFFFF);
				}
				if (time <= 95) {
					if (this.prevSleepTimer != time) this.prevSleepTimer = time;
				}
				else if (this.prevSleepTimer != 0) this.prevSleepTimer = 0;
			}
		} else if (event.getType() == ElementType.HOTBAR) {
			Entity entity = mc.getRenderViewEntity();
			if (entity != null && entity instanceof EntityPlayer && !((EntityPlayer)entity).isSpectator()) {
				EntityPlayer player = (EntityPlayer)entity;
				IMistCapaHandler mistCapa = IMistCapaHandler.getHandler(player);
				ItemStack mask = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
				if (!IMask.isMask(mask)) mask = mistCapa.getMask();
				if (IMask.isMask(mask) && IMask.isRespirator(mask)) {
					ItemStack filter = IMask.getFilter(mask);
					mc.getTextureManager().bindTexture(GuiRespirator.guiTextures);
					GlStateManager.enableBlend();
					ScaledResolution res = event.getResolution();
					int width = res.getScaledWidth();
					int height = res.getScaledHeight();
					int damageShift = (mask.getMaxDamage() - mask.getItemDamage() < 20) ? 44 : 0;
					if (mc.inGameHasFocus) xPos = mc.gameSettings.mainHand == EnumHandSide.RIGHT ? 189 : -29;
					if (!filter.isEmpty()) {
						if (IMask.getFilterDurability(filter) - mask.getTagCompound().getInteger(MistTags.nbtFilterDurabilityTag) < 150) {
							if (filterTimer == 0) filterTimer = -20;
							else if (filterTimer == -1) filterTimer = 20;
							if (this.prevPlayerTick != player.ticksExisted) {
								if (filterTimer > 0 ) --filterTimer;
								else if (filterTimer < -1) ++filterTimer;
								this.prevPlayerTick = player.ticksExisted;
							}
						} else filterTimer = 0;
						Gui.drawModalRectWithCustomSizedTexture((width - 176)/2 + xPos - 3, height - 22, 0, (filterTimer >= 0 ? 166 : 188) + damageShift, 22, 22, 256, 256);
						mc.getRenderItem().renderItemAndEffectIntoGUI(filter, (width - 176)/2 + xPos, height - 19);
						mc.getRenderItem().renderItemOverlays(mc.fontRenderer, filter, (width - 176)/2 + xPos, height - 19);
						GlStateManager.disableLighting();
					} else Gui.drawModalRectWithCustomSizedTexture((width - 176)/2 + xPos - 3, height - 22, 0, 166 + damageShift, 22, 22, 256, 256);
				}
				GlStateManager.enableBlend();
				// Toxic
				int shift = 8;
				int percentX = 21;
				if (mistCapa.getToxic() > 0) {
					mc.getTextureManager().bindTexture(GuiSkills.guiTextures);
					Gui.drawModalRectWithCustomSizedTexture(8, shift, 176, 0, 9, 10, 256, 256);
					if (ModConfig.player.showEffectsBar) {
						Gui.drawModalRectWithCustomSizedTexture(20, shift + 3, 176, 10, 33, 4, 256, 256);
						Gui.drawModalRectWithCustomSizedTexture(20, shift + 3, 176, 14, 2 + mistCapa.getToxic() * 30 / 10000, 4, 256, 256);
						percentX = 56;
					}
					if (ModConfig.player.showEffectsPercent) mc.fontRenderer.drawStringWithShadow(String.format("%.2f", mistCapa.getToxic()/100F) + "%", percentX, shift, 0xFFFFFF);
					shift = 22;
				}
				// Pollution
				if (mistCapa.getPollution() > 0) {
					mc.getTextureManager().bindTexture(GuiSkills.guiTextures);
					Gui.drawModalRectWithCustomSizedTexture(8, shift, 185, 0, 9, 10, 256, 256);
					if (ModConfig.player.showEffectsBar) {
						Gui.drawModalRectWithCustomSizedTexture(20, shift + 3, 176, 10, 33, 4, 256, 256);
						Gui.drawModalRectWithCustomSizedTexture(20, shift + 3, 176, 18, 2 + mistCapa.getPollution() * 30 / 10000, 4, 256, 256);
						percentX = 56;
					}
					if (ModConfig.player.showEffectsPercent) mc.fontRenderer.drawStringWithShadow(String.format("%.2f", mistCapa.getPollution()/100F) + "%", percentX, shift, 0xFFFFFF);
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderGameOverlay(RenderGameOverlayEvent.Pre event) {
		if (event.getType() == ElementType.JUMPBAR && mc.player.isRiding() && mc.player.getRidingEntity() instanceof AbstractMistMount) {
			if (!mc.player.isCreative()) mc.ingameGUI.renderExpBar(event.getResolution(), event.getResolution().getScaledWidth() / 2 - 91);
			event.setCanceled(true);
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void guiPostInit(GuiScreenEvent.InitGuiEvent.Post event) {
		if (event.getGui() instanceof GuiInventory || event.getGui() instanceof GuiMask) {
			GuiContainer gui = (GuiContainer)event.getGui();
			int y; boolean isMask;
			if (gui instanceof GuiInventory) { y = 1; isMask = false;} else { y = -23; isMask = true;}
			event.getButtonList().add(new GuiMaskButton(69, gui.getGuiLeft() + 8, gui.getGuiTop() + y, 16, 6, isMask, gui, ""));
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void guiPostAction(GuiScreenEvent.ActionPerformedEvent.Post event) {
		if (event.getGui() instanceof GuiInventory) {
			if (event.getButton().id == 69) {
				PacketHandler.INSTANCE.sendToServer(new PacketOpenMaskInventory(ItemStack.EMPTY));
			}
		}
		if (event.getGui() instanceof GuiMask) {
			if (event.getButton().id == 69) {
				event.getGui().mc.displayGuiScreen(new GuiInventory(event.getGui().mc.player));
				PacketHandler.INSTANCE.sendToServer(new PacketOpenNormalInventory());
			}
		}
	}

	private boolean close = false;
	private float[] vec = new float[3];

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderPlayer(RenderPlayerEvent.Pre event) {
		Entity entity = event.getEntityPlayer().getRidingEntity();
		if (entity instanceof IOffetPassangerMount && entity instanceof EntityLivingBase) {
			EntityLivingBase living = (EntityLivingBase)entity;
			float ang = -(float) Math.toRadians(living.renderYawOffset);
			vec = ((IOffetPassangerMount)living).getPassangerOffset(vec, living.limbSwing, living.limbSwingAmount);
			vec = SimpleIK.rotateY(vec, ang);
			GlStateManager.pushMatrix();
			GlStateManager.translate(vec[0], 0, vec[2]);
			close = true;
		} else close = false;
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderPlayer(RenderPlayerEvent.Post event) {
		if (close) GlStateManager.popMatrix();
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void swingArm(RenderSpecificHandEvent event) {
		ItemStack stack = event.getItemStack();
		if (stack.getItem() == MistItems.SOAP) {
			EntityPlayerSP player = mc.player;
			if (player.isHandActive() && player.getItemInUseCount() > 0 && player.getActiveHand() == event.getHand()) {
				EnumHandSide handSide = event.getHand() == EnumHand.MAIN_HAND ? player.getPrimaryHand() : player.getPrimaryHand().opposite();
				GlStateManager.pushMatrix();
				boolean leftHand = handSide == EnumHandSide.LEFT;
				transformEatFirstPerson(event.getPartialTicks(), handSide, stack);
				mc.getItemRenderer().transformSideFirstPerson(handSide, event.getEquipProgress());
				mc.getItemRenderer().renderItemSide(player, stack, leftHand ? ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND : ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, leftHand);
				GlStateManager.popMatrix();
				event.setCanceled(true);
			}
		}
	}

	public void transformEatFirstPerson(float partialTicks, EnumHandSide hand, ItemStack stack) {
		float f = ClientEventHandler.mc.player.getItemInUseCount() - partialTicks + 1.0F;
		float f1 = f / stack.getMaxItemUseDuration();
		if (f1 < 0.8F) {
			float f2 = MathHelper.abs(MathHelper.cos(f / 4.0F * (float) Math.PI) * 0.5F);
			GlStateManager.translate(0.0F, f2, 0.0F);
		}
		float f3 = 1.0F - (float) Math.pow(f1, 27.0D);
		int i = hand == EnumHandSide.RIGHT ? 1 : -1;
		GlStateManager.translate(f3 * 0.6F * i, f3 * -0.7F, f3 * 0.0F);
		GlStateManager.rotate(i * f3 * 90.0F, 0, 1, 0);
		GlStateManager.rotate(f3 * 10.0F, 1, 0, 0);
		GlStateManager.rotate(i * f3 * 30.0F, 0, 0, 1);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void tooltipEvent(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		if (!stack.isEmpty()) {
			//Remove mask armor tooltip
			if (IMask.isMask(stack)) {
				for (int i = 1; i < event.getToolTip().size(); ++i) {
					if (event.getToolTip().get(i).contains(I18n.format("item.modifiers." + ((ItemArmor)stack.getItem()).getEquipmentSlot().getName()))) {
						event.getToolTip().remove(i);
						event.getToolTip().remove(i - 1);
					}
				}
				if (!(stack.getItem() instanceof IMask) && IMask.respirators.containsKey(new ItemStackMapKey(stack))) {
					ItemStack filter = IMask.getFilter(stack);
					StringBuilder sb = new StringBuilder();
					if (!filter.isEmpty()) {
						sb.append(filter.getItem().getUnlocalizedName());
						sb.append(".name");
						String name = sb.toString();
						sb.delete(0, sb.length());

						sb.append(I18n.format("item.mist.filter.tooltip"));
						sb.append(": ");
						sb.append(I18n.format(name));
						event.getToolTip().add(sb.toString());
						sb.delete(0, sb.length());

						sb.append(I18n.format("item.mist.respirator_efficiency.tooltip"));
						sb.append(": ");
						sb.append(TextFormatting.GREEN);
						sb.append(String.format("%.2f", IMask.getImpermeability(stack)*IFilter.getDepthOfFilteration(filter)/100));
						sb.append("%");
						event.getToolTip().add(sb.toString());
						sb.delete(0, sb.length());

						float d = (float)stack.getTagCompound().getInteger(MistTags.nbtFilterDurabilityTag)/IMask.getFilterDurability(filter)*100;
						sb.append(I18n.format("item.mist.filter_damage.tooltip"));
						sb.append(": ");
						if (d >= 25) sb.append(d < 50 ? TextFormatting.YELLOW : d < 75 ? TextFormatting.GOLD : TextFormatting.RED);
						sb.append(String.format("%.2f", d));
						sb.append("%");
						event.getToolTip().add(sb.toString());
					} else {
						sb.append(I18n.format("item.mist.respirator_impermeability.tooltip"));
						sb.append(": ");
						sb.append(TextFormatting.GREEN);
						sb.append(String.format("%.2f", IMask.getImpermeability(stack)));
						sb.append("%");
						event.getToolTip().add(sb.toString());
						sb.delete(0, sb.length());
						event.getToolTip().add(I18n.format("item.mist.filter_empty.tooltip"));
					}
				}
			} else if (stack.getItem() instanceof ISuit) {
				for (int i = 1; i < event.getToolTip().size(); ++i) {
					if (event.getToolTip().get(i).contains(I18n.format("item.modifiers." + ((ItemArmor)stack.getItem()).getEquipmentSlot().getName()))) {
						event.getToolTip().remove(i);
						event.getToolTip().remove(i - 1);
					}
				}
			} else if (stack.getItem() instanceof ItemArmor) {
				NBTTagCompound tag = stack.getSubCompound(MistTags.nbtInnerSuitTag);
				if (tag != null) {
					ItemStack suit = new ItemStack(tag);
					if (suit.getItem() instanceof ISuit) {
						StringBuilder sb = new StringBuilder();
						sb.append(suit.getItem().getUnlocalizedName(suit));
						sb.append(".name");
						String name = I18n.format(sb.toString());
						sb.delete(0, sb.length());
						sb.append(I18n.format("item.mist.suit_inner.tooltip"));
						sb.append(": ");
						sb.append(name);
						event.getToolTip().add(1, sb.toString());
						sb.delete(0, sb.length());
						sb.append(I18n.format("item.mist.suit_protection.tooltip"));
						sb.append(": ");
						sb.append(TextFormatting.GREEN);
						sb.append(String.format("%.2f", ((ISuit)suit.getItem()).getPollutionProtection()));
						sb.append("%");
						event.getToolTip().add(2, sb.toString());
					}
				}
			} else if (stack.getItem() instanceof UniversalBucket) {
				FluidStack fs = FluidStack.loadFluidStackFromNBT(stack.getTagCompound());
				if (fs != null && fs.getFluid() == MistBlocks.ACID) {
					event.getToolTip().set(0, I18n.format("item.mist.acid_bucket.name"));
				}
			} else if (stack.getItem() instanceof ItemMistMap) {
				if (!stack.hasDisplayName()) {
					event.getToolTip().set(0, stack.getDisplayName() + " #" + stack.getItemDamage());
				}
			} else if (stack.getItem() == Items.CLOCK) {
				if (event.getEntityPlayer() != null && event.getEntityPlayer().world.provider.getDimension() == Mist.getID()) {
					event.getToolTip().add(MistTime.getDate());
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void textureStitchEvent(TextureStitchEvent.Pre event) {
		event.getMap().registerSprite(new ResourceLocation(Mist.MODID, "items/empty_mask"));
		event.getMap().registerSprite(new ResourceLocation(Mist.MODID, "particle/rain"));
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void textureStitchEvent(TextureStitchEvent.Post event) {
		if (Minecraft.getMinecraft().getRenderItem() != null) {
			ItemColoring.createFoodColorList();
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderCreatureName(RenderLivingEvent.Specials.Post event) {
		EntityLivingBase entity = event.getEntity();
		if (entity instanceof EntityGender) {
			String name = ((EntityGender)entity).getGenderTag();
			if (!name.isEmpty()) {
				double d0 = entity.getDistanceSq(mc.getRenderViewEntity());
		        if (d0 < 36) {
		        	GlStateManager.alphaFunc(516, 0.1F);
		        	event.getRenderer().renderEntityName(entity, event.getX(), event.getY() + 0.25, event.getZ(), name, d0);
		        }
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void playerTick(PlayerTickEvent event) {
		if (event.side == Side.CLIENT && event.phase == Phase.START ) {
			if (ClientProxy.maskKey.isPressed() && mc.inGameHasFocus) {
				PacketHandler.INSTANCE.sendToServer(new PacketOpenMaskInventory(ItemStack.EMPTY));
			} else if (ClientProxy.skillKey.isPressed() && mc.inGameHasFocus) {
				event.player.openGui(Mist.instance, 4, event.player.world, 0, 0, 0);
			}
			TileEntityCampfire.updateFrame();
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void modelRegistry(ModelRegistryEvent event) {
		ModelLoaderRegistry.registerLoader(new MistModelLoader());
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onSoundPlayed(PlaySoundEvent event) {
		if (event.getSound().getCategory() == SoundCategory.MUSIC && WorldProviderMist.canPlayMusic(mc.player)) {
			if (currentSound == null) {
				ISound sound = event.getSound();
				MistMusic s = new MistMusic(sound.getSoundLocation(), sound.getCategory());
				currentSound = s;
				event.setResultSound(s);
			} else event.setResultSound(null);
		}
	}

	/*private static final MistWind wind = new MistWind(MistSounds.BLOCK_WIND, SoundCategory.AMBIENT);

	@SubscribeEvent
	public void onClientWorldTick(ClientTickEvent event) {
		if (event.phase == Phase.START && mc.inGameHasFocus && mc.player != null && mc.player.world.provider.getDimension() == Mist.getID()) {
			checkSoundPlace();
			if (!mc.getSoundHandler().isSoundPlaying(wind)) mc.getSoundHandler().playSound(wind);
		}
	}

	public static float lightVolume;
	public static boolean isUpBiome;

	private void checkSoundPlace() {
		EntityPlayerSP player = mc.player;
		BlockPos pos = new BlockPos(Math.floor(player.posX), Math.floor(player.posY) + 1, Math.floor(player.posZ));
		isUpBiome = player.world.getBiome(pos) instanceof BiomeMistUp;
		lightVolume = ((float)player.world.getLightFor(EnumSkyBlock.SKY, pos))/16F;
	}*/

	/*@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onChunkWatch(ChunkWatchEvent.Watch event) {
		if (ModConfig.advancedFogRenderer) {
			Chunk chunk = event.getChunkInstance();
			if (chunk.getWorld().isRemote && chunk.getWorld().provider.getDimension() == Mist.dimensionID) {
				FogTexture.createChunkTexture(chunk.getWorld(), chunk.x, chunk.z);
			}
		}
	}*/
	
	/*public static void StartMistAmbient() {
		if (!mc.getSoundHandler().isSoundPlaying(psr)) {
			mc.getSoundHandler().stop("", SoundCategory.MUSIC);
			mc.getSoundHandler().playSound(psr);
		}
	}*/
}
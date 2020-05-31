package ru.liahim.mist.client.renderer.layers;

import ru.liahim.mist.api.MistTags;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerSuit extends LayerArmorBase<ModelBiped> {

	private final RenderLivingBase<?> renderer;
	private boolean skipRenderGlint;

	public LayerSuit(RenderLivingBase<?> renderer) {
		super(renderer);
		this.renderer = renderer;
	}

	@Override
	protected void initArmor() {}

	@Override
	public void doRenderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		this.renderArmorLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.CHEST);
		this.renderArmorLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.LEGS);
		this.renderArmorLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.FEET);
		this.renderArmorLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.HEAD);
	}

	public void renderArmorLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, EntityEquipmentSlot slot) {
		ItemStack stack = entity.getItemStackFromSlot(slot);
		if (!stack.isEmpty() && stack.getItem() instanceof ItemArmor && ((ItemArmor) stack.getItem()).getEquipmentSlot() == slot) {
			NBTTagCompound tag = stack.getSubCompound(MistTags.nbtInnerSuitTag);
			if (tag != null) {
				stack = new ItemStack(tag);
				if (stack.getItem() instanceof ItemArmor) {
					ItemArmor suit = (ItemArmor) stack.getItem();
					ModelBiped model = suit.getArmorModel(entity, stack, slot, null);
					model.setModelAttributes(this.renderer.getMainModel());
					model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
					this.setModelSlotVisible(model, slot);
					this.renderer.bindTexture(this.getArmorResource(entity, stack, slot, null));
					GlStateManager.enableNormalize();

					if (suit.hasOverlay(stack)) { // Allow this for anything, not only cloth
						int i = suit.getColor(stack);
						float f = (i >> 16 & 255) / 255.0F;
						float f1 = (i >> 8 & 255) / 255.0F;
						float f2 = (i & 255) / 255.0F;
						GlStateManager.color(f, f1, f2, 1.0F);
						model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
						this.renderer.bindTexture(this.getArmorResource(entity, stack, slot, "overlay"));
					}
					{ // Non-colored
						GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
						model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
					} // Default
					if (!this.skipRenderGlint && stack.hasEffect()) {
						renderEnchantedGlint(this.renderer, entity, model, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
					}
				}
			}
		}
	}

	@Override
	protected void setModelSlotVisible(ModelBiped model, EntityEquipmentSlot slot) {
		model.setVisible(false);
		switch (slot) {
		case HEAD:
			model.bipedHead.showModel = true;
			model.bipedHeadwear.showModel = true;
			break;
		case CHEST:
			model.bipedBody.showModel = true;
			model.bipedRightArm.showModel = true;
			model.bipedLeftArm.showModel = true;
			break;
		case LEGS:
			model.bipedBody.showModel = true;
			model.bipedRightLeg.showModel = true;
			model.bipedLeftLeg.showModel = true;
			break;
		case FEET:
			model.bipedRightLeg.showModel = true;
			model.bipedLeftLeg.showModel = true;
		default:
			break;
		}
	}
}
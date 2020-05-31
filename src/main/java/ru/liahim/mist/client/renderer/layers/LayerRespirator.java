package ru.liahim.mist.client.renderer.layers;

import ru.liahim.mist.api.item.IMask;
import ru.liahim.mist.capability.handler.IMistCapaHandler;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerRespirator extends LayerArmorBase<ModelBiped> {

	private final RenderLivingBase<?> renderer;
    private boolean skipRenderGlint;

	public LayerRespirator(RenderLivingBase<?> renderer) {
		super(renderer);
		this.renderer = renderer;
	}

	@Override
	protected void initArmor() {}

	@Override
	public void doRenderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		ItemStack stack = getMask(entity);
		if (!stack.isEmpty() && stack.getItem() instanceof ItemArmor) {
			ItemArmor mask = (ItemArmor)stack.getItem();
			ModelBiped model = mask.getArmorModel(entity, stack, EntityEquipmentSlot.HEAD, null);
			model.setModelAttributes(this.renderer.getMainModel());
			model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
			this.setModelSlotVisible(model, EntityEquipmentSlot.HEAD);
			this.renderer.bindTexture(this.getArmorResource(entity, stack, EntityEquipmentSlot.HEAD, null));
			GlStateManager.enableNormalize();

			if (mask.hasOverlay(stack)) { // Allow this for anything, not only cloth
				int i = mask.getColor(stack);
				float f = (i >> 16 & 255) / 255.0F;
				float f1 = (i >> 8 & 255) / 255.0F;
				float f2 = (i & 255) / 255.0F;
				GlStateManager.color(f, f1, f2, 1.0F);
				model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
				this.renderer.bindTexture(this.getArmorResource(entity, stack, EntityEquipmentSlot.HEAD, "overlay"));
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

	public ItemStack getMask(EntityLivingBase living) {
		if (living instanceof EntityPlayer) {
			ItemStack mask = IMistCapaHandler.getHandler((EntityPlayer)living).getMask();
			if (!mask.isEmpty() && mask.getItem() instanceof IMask) return mask;
		}
		return ItemStack.EMPTY;
	}

	@Override
	protected void setModelSlotVisible(ModelBiped model, EntityEquipmentSlot slotIn) {
		model.setVisible(false);
	}
}
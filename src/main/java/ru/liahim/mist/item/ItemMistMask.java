package ru.liahim.mist.item;

import java.util.List;

import javax.annotation.Nullable;

import ru.liahim.mist.api.item.IColoredItem;
import ru.liahim.mist.api.item.IFilter;
import ru.liahim.mist.api.item.IMask;
import ru.liahim.mist.capability.handler.IMistCapaHandler;
import ru.liahim.mist.client.model.ModelRespirator;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.init.ItemColoring;
import ru.liahim.mist.init.ModItems;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMistMask extends ItemMistArmor implements IMask, IColoredItem {

	@Override
	@SideOnly(Side.CLIENT)
	public IItemColor getItemColor() {
		return ItemColoring.ITEM_ARMOR_COLORING;
	}
	private final boolean isRespirator;
	private final boolean canEat;
	private final float impermeability;
	private final String texture;
	private final String overlay;

	public ItemMistMask(ArmorMaterial material, boolean isRespirator, float impermeability, boolean canEat) {
		super(material, 0, EntityEquipmentSlot.HEAD);
		this.isRespirator = isRespirator;
		this.canEat = canEat;
		this.impermeability = impermeability;
		this.texture = canEat ? "mist:textures/models/armor/respirator_open.png" : "mist:textures/models/armor/respirator.png";
		this.overlay = "mist:textures/models/armor/respirator_overlay.png";
	}
	
	public ItemMistMask(ArmorMaterial material, float impermeability, boolean canEat) {
		this(material, true, impermeability, canEat);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
		if (stack.getItem() instanceof IMask && ((IMask)stack.getItem()).isRespirator()) {
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
				tooltip.add(sb.toString());
				sb.delete(0, sb.length());

				sb.append(I18n.format("item.mist.respirator_efficiency.tooltip"));
				sb.append(": ");
				sb.append(TextFormatting.GREEN);
				sb.append(String.format("%.2f", IMask.getImpermeability(stack)*IFilter.getDepthOfFilteration(filter)/100));
				sb.append("%");
				tooltip.add(sb.toString());
				sb.delete(0, sb.length());

				float d = (float)filter.getItemDamage()/filter.getMaxDamage()*100;
				sb.append(I18n.format("item.mist.filter_damage.tooltip"));
				sb.append(": ");
				if (d >= 25) sb.append(d < 50 ? TextFormatting.YELLOW : d < 75 ? TextFormatting.GOLD : TextFormatting.RED);
				sb.append(String.format("%.2f", d));
				sb.append("%");
				tooltip.add(sb.toString());
			} else {
				sb.append(I18n.format("item.mist.respirator_impermeability.tooltip"));
				sb.append(": ");
				sb.append(TextFormatting.GREEN);
				sb.append(String.format("%.2f", IMask.getImpermeability(stack)));
				sb.append("%");
				tooltip.add(sb.toString());
				sb.delete(0, sb.length());
				tooltip.add(I18n.format("item.mist.filter_empty.tooltip"));
			}
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (player.isSneaking()) {
			if (!world.isRemote && hand == EnumHand.MAIN_HAND) player.openGui(Mist.instance, 1, world, MathHelper.floor(player.posX), MathHelper.floor(player.posY), MathHelper.floor(player.posZ));
			return new ActionResult(EnumActionResult.PASS, stack);
		} else {
			IMistCapaHandler mistCapa = IMistCapaHandler.getHandler(player);
			if (mistCapa == null || mistCapa.getMask().isEmpty()) {
				ItemStack armor = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
				if (armor.isEmpty()) {
					player.setItemStackToSlot(EntityEquipmentSlot.HEAD, stack.copy());
					stack.setCount(0);
					return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
				} else if (mistCapa != null && !(armor.getItem() instanceof IMask)) {
					mistCapa.setStackInSlot(0, stack.copy());
					//mistCapa.setMaskChanged(true, true);
					stack.setCount(0);
					player.playSound(this.getArmorMaterial().getSoundEvent(), 1, 1);
					return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
				}
			}
			return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
		}
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		if (this.hasColor(stack)) {
			IBlockState state = world.getBlockState(pos);
			if (state.getBlock() instanceof BlockCauldron) {
				int level = state.getValue(BlockCauldron.LEVEL);
				if (level > 0) {
					if (!world.isRemote) {
						this.removeColor(stack);
						Blocks.CAULDRON.setWaterLevel(world, pos, state, level - 1);
						player.addStat(StatList.ARMOR_CLEANED);
					}
					return EnumActionResult.SUCCESS;
				}
			}
		}
		return EnumActionResult.PASS;
	}

	@Override
	public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot armorType, Entity entity) {
		if (IMask.isMaskInSlot(entity)) return false;
        return super.isValidArmor(stack, armorType, entity);
    }

	@Override
	public boolean isRespirator() {
		return this.isRespirator;
	}

	@Override
	public boolean canEat() {
		return this.canEat;
	}

	@Override
	public float getImpermeability() {
		return this.impermeability;
	}

	@Override
	public boolean hasOverlay(ItemStack stack) {
		return this.getArmorMaterial() == ModItems.LEATHER_MASK_MATERIAL ||
				this.getArmorMaterial() == ModItems.RUBBER_MATERIAL || getColor(stack) != 0x00FFFFFF;
	}

	@Override
	public boolean hasColor(ItemStack stack) {
		if (this.getArmorMaterial() != ModItems.LEATHER_MASK_MATERIAL &&
				this.getArmorMaterial() != ModItems.RUBBER_MATERIAL) return false;
		else {
			NBTTagCompound tag = stack.getTagCompound();
			return tag != null && tag.hasKey("display", 10) ? tag.getCompoundTag("display").hasKey("color", 3) : false;
		}
	}

	@Override
	public int getColor(ItemStack stack) {
		if (!this.hasColor(stack)) {
			if (this.getArmorMaterial() == ModItems.LEATHER_MASK_MATERIAL) return 0xA06540;
			else if (this.getArmorMaterial() == ModItems.RUBBER_MATERIAL) return 0x726d61;
			else return 0xFFFFFF;
		} else return stack.getTagCompound().getCompoundTag("display").getInteger("color");
	}

	@Override
	public void removeColor(ItemStack stack) {
		if (this.getArmorMaterial() == ModItems.LEATHER_MASK_MATERIAL ||
				this.getArmorMaterial() == ModItems.RUBBER_MATERIAL) {
			NBTTagCompound tag = stack.getTagCompound();
			if (tag != null) {
				NBTTagCompound tag1 = tag.getCompoundTag("display");
				if (tag1.hasKey("color")) {
					tag1.removeTag("color");
				}
			}
		}
	}

	@Override
	public void setColor(ItemStack stack, int color) {
		if (this.getArmorMaterial() != ModItems.LEATHER_MASK_MATERIAL &&
				this.getArmorMaterial() != ModItems.RUBBER_MATERIAL) {
			throw new UnsupportedOperationException("Can\'t dye non-leather or non-rubber!");
		} else {		
			NBTTagCompound tab = stack.getTagCompound();
			if (tab == null) {
				tab = new NBTTagCompound();
				stack.setTagCompound(tab);
			}
			NBTTagCompound tab1 = tab.getCompoundTag("display");
			if (!tab.hasKey("display", 10)) {
				tab.setTag("display", tab1);
			}
			tab1.setInteger("color", color);
		}
	}

	@Override
	@Nullable
	@SideOnly(Side.CLIENT)
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		if (entity instanceof AbstractClientPlayer) {
			if (slot == EntityEquipmentSlot.HEAD) return type == null ? texture : overlay;
		}
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped model) {
		if (armorSlot == EntityEquipmentSlot.HEAD) return ModelRespirator.respirator;
		return null;
	}
}
package ru.liahim.mist.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.tileentity.TileEntityUrn.UrnType;

public class ItemMistUrn extends ItemBlock {

	public ItemMistUrn(Block block) {
		super(block);
		this.addPropertyOverride(new ResourceLocation("type"), new IItemPropertyGetter() {
			@Override
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
				return UrnType.getType(stack, null).getId();
			}
		});
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
		String tip = UrnType.getTooltip(stack, null);
		if (!tip.isEmpty()) tooltip.add(tip);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		return I18n.translateToLocal(stack.getItemDamage() == 1 ? "item.mist.urn_raw" : UrnType.getType(stack, null).isRare() ? "item.mist.urn_rare" : "item.mist.urn");
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return stack.getItemDamage() == 0 ? 1 : 4;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (stack.getItemDamage() != 0) return new ActionResult(EnumActionResult.FAIL, stack);
		if (!world.isRemote && hand == EnumHand.MAIN_HAND) player.openGui(Mist.instance, 6, world, MathHelper.floor(player.posX), MathHelper.floor(player.posY), MathHelper.floor(player.posZ));
		return new ActionResult(EnumActionResult.PASS, stack);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		return EnumActionResult.FAIL;
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {
	    return UrnType.getType(stack, null).isRare() ? EnumRarity.RARE : EnumRarity.COMMON;
	}
}
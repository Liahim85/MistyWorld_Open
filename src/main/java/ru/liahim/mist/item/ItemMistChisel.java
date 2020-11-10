package ru.liahim.mist.item;

import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.block.IDividable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.block.MistStoneMined;
import ru.liahim.mist.capability.handler.ISkillCapaHandler;
import ru.liahim.mist.capability.handler.ISkillCapaHandler.Skill;
import ru.liahim.mist.init.ModAdvancements;

public class ItemMistChisel extends ItemMist {

	private final float speed;
	protected ToolMaterial toolMaterial;

	public ItemMistChisel(ToolMaterial material) {
		this.toolMaterial = material;
		this.maxStackSize = 1;
		this.setMaxDamage(material.getMaxUses());
		this.speed = material.getAttackDamage() + 1.0F;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		if (!player.canPlayerEdit(pos.offset(facing), facing, stack)) return EnumActionResult.FAIL;
		else {
			IBlockState state = world.getBlockState(pos);
			Block block = state.getBlock();
			if (block == MistBlocks.STONE_MINED) {
				if (state.getValue(MistStoneMined.TYPE) == MistStoneMined.EnumStoneType.MINED && Skill.getLevel(player, Skill.MASON) > 1) {
					ISkillCapaHandler.getHandler(player).addSkill(Skill.MASON, 1);
					if (state.getValue(MistStoneMined.STAGE) == MistStoneMined.EnumStoneStage.MOSS) state = state.withProperty(MistStoneMined.STAGE, MistStoneMined.EnumStoneStage.NORMAL);
					this.setBlock(stack, player, world, pos, state.withProperty(MistStoneMined.TYPE, MistStoneMined.EnumStoneType.CHISELED));
					if (player instanceof EntityPlayerMP) ModAdvancements.STONE_MINED.trigger((EntityPlayerMP)player, new ItemStack(MistItems.NIOBIUM_CHISEL));
					return EnumActionResult.SUCCESS;
				}
			} else if (block instanceof IDividable) {
				return ((IDividable)block).chiselBlock(player, stack, world, pos, state, hand, facing, hitX, hitY, hitZ);
			}
			return EnumActionResult.PASS;
		}
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
		stack.damageItem(1, attacker);
		return true;
	}

	protected void setBlock(ItemStack stack, EntityPlayer player, World world, BlockPos pos, IBlockState state) {
		world.playSound(player, pos, SoundEvents.BLOCK_STONE_HIT, SoundCategory.BLOCKS, 1.0F, 1.0F);
		if (!world.isRemote) {
			world.setBlockState(pos, state, 11);
			stack.damageItem(1, player);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isFull3D() {
		return true;
	}

	public String getMaterialName() {
		return this.toolMaterial.toString();
	}

	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
		Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);
		if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", 0.0D, 0));
			multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", this.speed - 4, 0));
		}
		return multimap;
	}
}
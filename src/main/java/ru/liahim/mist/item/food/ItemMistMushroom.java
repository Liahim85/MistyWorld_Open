package ru.liahim.mist.item.food;

import java.util.List;

import javax.annotation.Nullable;

import ru.liahim.mist.api.MistTags;
import ru.liahim.mist.api.block.IWettable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.item.IMistFood;
import ru.liahim.mist.block.upperplant.MistMushroom;
import ru.liahim.mist.block.upperplant.MistMycelium;
import ru.liahim.mist.block.upperplant.MistMushroom.IFoodProperty;
import ru.liahim.mist.capability.handler.IFoodHandler;
import ru.liahim.mist.capability.handler.IMistCapaHandler;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.init.ModAdvancements;
import ru.liahim.mist.tileentity.TileEntityMycelium;
import ru.liahim.mist.util.SoilHelper;
import ru.liahim.mist.world.MistWorld;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMistMushroom extends ItemFood implements IMistFood {

	public static final MistMushroom[] MUSHROOMS = new MistMushroom[] {MistBlocks.MUSHROOMS_0, MistBlocks.MUSHROOMS_1};
	private final boolean isCook;

	public ItemMistMushroom(boolean isCook) {
		super(0, 0.0F, false);
		this.isCook = isCook;
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
		this.addPropertyOverride(new ResourceLocation("mist:salt"), new IItemPropertyGetter() {
			@Override
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
				return IMistFood.hasSalt(stack) ? 1 : 0;
			}
		});
	}

	@Override
	public int getMetadata(int damage) {
		return damage;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		if (player != null) {
			IFoodHandler mCapa = IFoodHandler.getHandler(player);
			int i = mCapa.getMushroomStudy(stack.getMetadata(), this.isCook);
			float toxic = this.getToxic(stack);
			if (toxic != 0 && mCapa.isFoodStudy(stack)) {
				if (toxic > 0) tooltip.add(TextFormatting.DARK_RED + I18n.translateToLocal("item.mist.food_toxic.tooltip"));
				else tooltip.add(TextFormatting.DARK_GREEN + I18n.translateToLocal("item.mist.food_antitoxic.tooltip"));
			}
			if (i != 0) {
				if (i == -1) tooltip.add(TextFormatting.DARK_RED + I18n.translateToLocal("item.mist.food_inedible.tooltip"));
				else if (i == 1) tooltip.add(I18n.translateToLocal("item.mist.food_unknown.tooltip"));
				else if (i == 2) tooltip.add(TextFormatting.DARK_GREEN + I18n.translateToLocal("item.mist.food_edible.tooltip"));
			}
			if (!this.isCook && player != null && player.isCreative()) tooltip.add(I18n.translateToLocal("tile.mist.mushroom.tooltip"));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		if (!stack.isEmpty() && stack.getItem() == this) {
			int i = MathHelper.clamp(stack.getMetadata(), 0, MUSHROOMS.length * 16 - 1);
			return "tile.mist.mushroom_" + MUSHROOMS[i/16].getTypeName(i % 16) + (this.isCook ? "_c" : "");
		}
		return "";
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String salt = IMistFood.hasSalt(stack) ? " " + I18n.translateToLocal("item.mist.food_salt.tooltip") : "";
		return super.getItemStackDisplayName(stack) + salt;
	}

	@Override
	public int getHealAmount(ItemStack stack) {
		if (!stack.isEmpty() && stack.getItem() == this) {
			int salt = IMistFood.hasSalt(stack) ? 1 : 0;
			int i = MathHelper.clamp(stack.getMetadata(), 0, MUSHROOMS.length * 16 - 1);
			return MUSHROOMS[i/16].getFoodProperty(i % 16).getHealAmount(this.isCook) + salt;
		}
		return 0;
	}

	@Override
	public float getSaturationModifier(ItemStack stack) {
		if (!stack.isEmpty() && stack.getItem() == this) {
			float salt = IMistFood.hasSalt(stack) ? 0.1F : 0;
			int i = MathHelper.clamp(stack.getMetadata(), 0, MUSHROOMS.length * 16 - 1);
			return MUSHROOMS[i/16].getFoodProperty(i % 16).getSaturationModifier(this.isCook) + salt;
		}
		return 0;
	}

	@Override
	protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
		if (stack.getItem() == this) {
			int i = MathHelper.clamp(stack.getMetadata(), 0, MUSHROOMS.length * 16 - 1);
			IFoodProperty prop = MUSHROOMS[i/16].getFoodProperty(i % 16);
			PotionEffect[] pes = prop.getPotionEffect(this.isCook);
			IFoodHandler mCapa = IFoodHandler.getHandler(player);
			if (pes != null && world.rand.nextFloat() < prop.getProbability(this.isCook)) {
				for (PotionEffect pe : pes) {
					player.addPotionEffect(new PotionEffect(pe));
				}
				if (prop.isEdable(this.isCook) ? mCapa.getMushroomStudy(i, this.isCook) != 2 : mCapa.getMushroomStudy(i, this.isCook) != -1) {
					mCapa.setMushroomStudy(i, prop.isEdable(this.isCook) ? 1 : -1, this.isCook);
				}
			} else if (mCapa.getMushroomStudy(i, this.isCook) != 2) {
				mCapa.setMushroomStudy(i, 2, this.isCook);
			}
			int toxic = (int) this.getToxic(stack);
			if (toxic != 0) {
				IMistCapaHandler.getHandler(player).addToxic(toxic);
				IFoodHandler.getHandler(player).setFoodStudy(stack);
				if (player instanceof EntityPlayerMP) ModAdvancements.CONSUME_TOXIC.trigger((EntityPlayerMP) player, stack, Float.valueOf(toxic));
			}
		}
	}

	@SuppressWarnings("unused")
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (this.isInCreativeTab(tab)) {
			for (int i = 0; i < MUSHROOMS.length; ++i) {
				for (int j = 0; j < MUSHROOMS[i].getTypeProperty().getAllowedValues().size(); ++j) {
					items.add(new ItemStack(this, 1, j + i * 16));
				}
			}
			if (showSaltyFood && Mist.saltymod && this.isCook) {
				for (int i = 0; i < MUSHROOMS.length; ++i) {
					for (int j = 0; j < MUSHROOMS[i].getTypeProperty().getAllowedValues().size(); ++j) {
						ItemStack stack = new ItemStack(this, 1, j + i * 16);
						stack = stack.copy();
						stack.setTagCompound(new NBTTagCompound());
						stack.getTagCompound().setBoolean(MistTags.saltTag, true);
						items.add(stack);
					}
				}
			}
		}
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!this.isCook) {
			if (player.isSneaking()) {
				if (facing == EnumFacing.UP) {
					if (!world.getBlockState(pos).getBlock().isReplaceable(world, pos)) pos = pos.offset(facing);
						ItemStack stack = player.getHeldItem(hand);
						int meta = this.getMetadata(stack.getMetadata());
						if (!stack.isEmpty() && player.canPlayerEdit(pos, facing, stack) && world.mayPlace(ItemMistMushroom.MUSHROOMS[meta/16], pos, false, facing, (Entity) null)) {
							IBlockState mushroom = MUSHROOMS[meta/16].getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta % 16, player, hand);
							IBlockState soil = world.getBlockState(pos.down());
							if (soil.getBlock() instanceof IWettable && !((IWettable)soil.getBlock()).isAcid() && !MistWorld.isPosInFog(world, pos) &&
									(SoilHelper.getHumus(soil) >= 2 || MistMushroom.isPair(mushroom, world.getTileEntity(pos.down())))) {
							if (placeBlockAt(stack, player, world, pos, facing, hitX, hitY, hitZ, mushroom, meta)) {
								mushroom = world.getBlockState(pos);
								SoundType soundtype = mushroom.getBlock().getSoundType(mushroom, world, pos, player);
								world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
								stack.shrink(1);
							}
							return EnumActionResult.SUCCESS;
						}
					}
				}
			} else if (player.isCreative()) {
				IBlockState state = world.getBlockState(pos);
				if (state.getBlock() instanceof IWettable) {
					MistMycelium.SoilType type = MistMycelium.SoilType.bySoil((IWettable)state.getBlock());
					if (type != null) {
						world.setBlockState(pos, MistBlocks.MYCELIUM.getDefaultState().withProperty(MistMycelium.SOIL, type).withProperty(IWettable.WET, state.getValue(IWettable.WET)));
						int meta = this.getMetadata(player.getHeldItem(hand).getMetadata());
						IBlockState state1 = MUSHROOMS[meta/16].getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta % 16, player, hand);
						((TileEntityMycelium)world.getTileEntity(pos)).setMushroomState(state1, false);
					}
				}
			}
		}
		return EnumActionResult.FAIL;
	}

	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState, int meta) {
		if (!world.setBlockState(pos, newState, 11)) return false;
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() == MUSHROOMS[meta/16]) {
			ItemMistMushroom.MUSHROOMS[meta/16].onBlockPlacedBy(world, pos, state, player, stack);
			if (player instanceof EntityPlayerMP) CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos, stack);
		}
		return true;
	}

	@Override
	public PotionEffect[] getPotions(ItemStack stack) {
		if (!stack.isEmpty() && stack.getItem() == this) {
			int i = MathHelper.clamp(stack.getMetadata(), 0, MUSHROOMS.length * 16 - 1);
			if (IMistFood.hasSalt(stack)) {
				PotionEffect[] list = MUSHROOMS[i/16].getFoodProperty(i % 16).getPotionEffect(this.isCook);
				PotionEffect[] temp = new PotionEffect[list.length];
				int j = 0;
				for (PotionEffect pe : list) {
					if (pe.getPotion().isBadEffect()) {
						temp[j] = new PotionEffect(pe.getPotion(), pe.getDuration()/2, Math.max(pe.getAmplifier() - 1, 0), false, false);
					} else temp[j] = pe;
					++j;
				}
				return temp;
			}
			return MUSHROOMS[i/16].getFoodProperty(i % 16).getPotionEffect(this.isCook);
		}
		return null;
	}

	@Override
	public float getProbability(ItemStack stack) {
		if (!stack.isEmpty() && stack.getItem() == this) {
			int i = MathHelper.clamp(stack.getMetadata(), 0, MUSHROOMS.length * 16 - 1);
			return MUSHROOMS[i/16].getFoodProperty(i % 16).getProbability(this.isCook);
		}
		return 0;
	}

	@Override
	public boolean isEdible(ItemStack stack) {
		int i = MathHelper.clamp(stack.getMetadata(), 0, MUSHROOMS.length * 16 - 1);
		return MUSHROOMS[i/16].getFoodProperty(i % 16).isEdable(this.isCook);
	}

	@Override
	public float getToxic(ItemStack stack) {
		if (!this.isCook) {
			int i = stack.getMetadata();
			if (i == 21) return -350;		// silver
			else if (i == 23) return -200;	// gold
			else if (i == 25) return -350;	// violet
		}
		return 0;
	}
}
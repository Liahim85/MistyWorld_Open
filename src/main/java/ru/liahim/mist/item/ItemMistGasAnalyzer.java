package ru.liahim.mist.item;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.sound.MistSounds;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.world.FogDamage;
import ru.liahim.mist.world.MistWorld;

public class ItemMistGasAnalyzer extends ItemMist {

	public ItemMistGasAnalyzer() {
		this.setMaxStackSize(1);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
		this.addPropertyOverride(new ResourceLocation("mist:value"), new IItemPropertyGetter() {

			private float lastToxic = -1;
			
			@Override
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entityIn) {
				boolean frame = stack.isOnItemFrame();
				if (entityIn != null || frame) {
					float toxic = 0;
					boolean flag = entityIn != null;
					Entity entity = flag ? entityIn : stack.getItemFrame();
					if (world == null) world = entity.world;
					if (world.provider.getDimension() == Mist.getID()) {
						float y = (float) (entity.posY + (flag ? 1 : 0));
						PooledMutableBlockPos pos = PooledMutableBlockPos.retain();
						pos.setPos(MathHelper.floor(entity.posX), y, MathHelper.floor(entity.posZ));
						if (MistWorld.isPosInFog(world, pos)) {
							float depth = 0;
							if (y < MistWorld.getFogMinHight()) depth = 4;
							else depth = Math.min(4, MistWorld.getFogHight(world, 0) + 4 - y);
							if (depth > 0) {
								float concentration = FogDamage.getConcentration(world, pos, true);
								toxic = FogDamage.getFogToxic(concentration);
								if (FogDamage.isAdsorbentNear(world, pos)) toxic *= (1 - FogDamage.getFinalEfficiency(60, concentration));
								toxic = toxic*depth/4;
								if (toxic <= 40) toxic = toxic*10/40;
								else if (toxic <= 70) toxic = (toxic-40)*10/30 + 10;
								else toxic = (toxic-70)*10/30 + 20;
							}
						}
						pos.release();
					}
					if (frame) return toxic;
					else {
						if (lastToxic < 0) lastToxic = toxic;
						else {
							int last = Math.round(lastToxic);
							lastToxic += (toxic - lastToxic) * 0.003F;
							if (last != Math.round(lastToxic) && entityIn instanceof EntityPlayer) world.playSound((EntityPlayer)entityIn, entityIn.getPosition(), MistSounds.PLAYER_GAS_ANALYZER, SoundCategory.PLAYERS, 1, 1);
						}
						return Math.round(lastToxic);
					}
				}
				return 0;
			}
		});
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {
		return EnumRarity.RARE;
	}
}
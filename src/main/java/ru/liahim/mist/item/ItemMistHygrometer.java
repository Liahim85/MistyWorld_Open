package ru.liahim.mist.item;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.world.MistWorld;

public class ItemMistHygrometer extends ItemMist {

	public ItemMistHygrometer() {
		this.addPropertyOverride(new ResourceLocation("mist:humi"), new IItemPropertyGetter() {

			private float lastHygro = -1;

			@Override
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entityIn) {
				boolean frame = stack.isOnItemFrame();
				if (entityIn != null || frame) {
					Entity entity = entityIn != null ? entityIn : stack.getItemFrame();
					if (world == null) world = entity.world;
					BlockPos pos = new BlockPos(MathHelper.floor(entity.posX), entity.posY, MathHelper.floor(entity.posZ));
					if (frame) return Math.max(0, MistWorld.getHumi(world, pos, 0));
					else {
						float hygro = MistWorld.getHumi(world, pos.up(), 0);
						if (lastHygro < 0) lastHygro = hygro;
						else lastHygro += (hygro - lastHygro) * 0.001F;
						return Math.max(0, lastHygro);
					}
				}
				return 50;
			}
		});
	}
}
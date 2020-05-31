package ru.liahim.mist.network;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Vector2f;

import ru.liahim.mist.tileentity.TileEntityCampfire;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketFirePitUpdate implements IMessage {

	Map<ItemStack,Vector2f> stoneAndColor = new HashMap<ItemStack,Vector2f>();

	public PacketFirePitUpdate() {}

	public PacketFirePitUpdate(Map<ItemStack,Vector2f> stoneAndColor) {
		this.stoneAndColor.putAll(stoneAndColor);
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		NBTTagList tagList = new NBTTagList();
		for (ItemStack stones : stoneAndColor.keySet()) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger("Color", (int) stoneAndColor.get(stones).y);
			new ItemStack(stones.getItem(), (int) stoneAndColor.get(stones).x, stones.getItemDamage()).writeToNBT(tag);
			tagList.appendTag(tag);
		}
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag("Stones", tagList);
		ByteBufUtils.writeTag(buffer, nbt);
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		NBTTagCompound nbt = ByteBufUtils.readTag(buffer);
		NBTTagList tagList = nbt.getTagList("Stones", 10);
		for (int i = 0; i < tagList.tagCount(); ++i) {
			NBTTagCompound tag = tagList.getCompoundTagAt(i);
			ItemStack stack = new ItemStack(tag);
			stoneAndColor.put(new ItemStack(stack.getItem(), 1, stack.getItemDamage()), new Vector2f(stack.getCount(), tag.getInteger("Color")));
		}
	}	

	public static class Handler implements IMessageHandler<PacketFirePitUpdate, IMessage> {
		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(final PacketFirePitUpdate message, final MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(new Runnable() {
				@Override
				public void run() {
					TileEntityCampfire.updateColorsFromServer(message.stoneAndColor);			
			}});
			return null;
		}
	}
}
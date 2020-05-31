package ru.liahim.mist.network;

import io.netty.buffer.ByteBuf;
import ru.liahim.mist.capability.handler.IFoodHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketToxicFoodSync implements IMessage {

	String[] list;

	public PacketToxicFoodSync() {}

	public PacketToxicFoodSync(String[] list) {
		this.list = list;
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeInt(this.list.length);
		for (int i = 0; i < this.list.length; i++) {
			String str = this.list[i];
			int s = str.indexOf("_");
			buffer.writeInt(Integer.valueOf(str.substring(0, s)));
			buffer.writeInt(Item.getIdFromItem(Item.getByNameOrId(str.substring(s + 1))));
		}
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		this.list = new String[buffer.readInt()];
		for (int i = 0; i < this.list.length; i++) {
			this.list[i] = buffer.readInt() + "_" + Item.getItemById(buffer.readInt()).getRegistryName().toString();
		}
	}

	public static class Handler implements IMessageHandler<PacketToxicFoodSync, IMessage> {
		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(final PacketToxicFoodSync message, final MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(new Runnable() {
				@Override
				public void run() {
					IFoodHandler mCapa = IFoodHandler.getHandler(Minecraft.getMinecraft().player);
					mCapa.setFoodStudyList(message.list);
			}});
			return null;
		}
	}
}
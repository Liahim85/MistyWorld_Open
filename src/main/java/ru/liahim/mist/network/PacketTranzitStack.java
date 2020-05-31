package ru.liahim.mist.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketTranzitStack implements IMessage {

	ItemStack mask = ItemStack.EMPTY;

	public PacketTranzitStack() {}

	public PacketTranzitStack(ItemStack mask) {
		this.mask = mask;
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		ByteBufUtils.writeItemStack(buffer, mask);
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		mask = ByteBufUtils.readItemStack(buffer);
	}	

	public static class Handler implements IMessageHandler<PacketTranzitStack, IMessage> {
		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(final PacketTranzitStack message, final MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(new Runnable() {
				@Override
				public void run() {			
					Minecraft.getMinecraft().player.inventory.setItemStack(message.mask);
			}});
			return null;
		}
	}
}
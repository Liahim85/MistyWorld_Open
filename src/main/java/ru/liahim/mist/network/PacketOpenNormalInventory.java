package ru.liahim.mist.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketOpenNormalInventory implements IMessage {

	public PacketOpenNormalInventory() {}

	@Override
	public void toBytes(ByteBuf buffer) {}

	@Override
	public void fromBytes(ByteBuf buffer) {}

	public static class Handler implements IMessageHandler<PacketOpenNormalInventory, IMessage> {
		@Override
		public IMessage onMessage(final PacketOpenNormalInventory message, final MessageContext ctx) {
			IThreadListener mainThread = (WorldServer)ctx.getServerHandler().player.world; 
	        mainThread.addScheduledTask(new Runnable(){
	        @Override
			public void run() { 			
				ctx.getServerHandler().player.openContainer.onContainerClosed(ctx.getServerHandler().player);		
				ctx.getServerHandler().player.openContainer = ctx.getServerHandler().player.inventoryContainer;
			}});
			return null;
		}
	}
}
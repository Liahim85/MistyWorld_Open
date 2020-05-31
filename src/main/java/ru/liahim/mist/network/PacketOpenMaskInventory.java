package ru.liahim.mist.network;

import ru.liahim.mist.common.Mist;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketOpenMaskInventory implements IMessage {

	ItemStack mask = ItemStack.EMPTY;

	public PacketOpenMaskInventory() {}

	public PacketOpenMaskInventory(ItemStack mask) {
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

	public static class Handler implements IMessageHandler<PacketOpenMaskInventory, IMessage> {
		@Override
		public IMessage onMessage(final PacketOpenMaskInventory message, final MessageContext ctx) {
			IThreadListener mainThread = (WorldServer)ctx.getServerHandler().player.world; 
	        mainThread.addScheduledTask(new Runnable(){
	        @Override
			public void run() {
	        	if (message.mask.isEmpty()) {
	        		ctx.getServerHandler().player.openGui(Mist.instance, 0, ctx.getServerHandler().player.world, (int)ctx.getServerHandler().player.posX, (int)ctx.getServerHandler().player.posY, (int)ctx.getServerHandler().player.posZ);
	        	} else {
		        	ctx.getServerHandler().player.inventory.setItemStack(ItemStack.EMPTY);
					ctx.getServerHandler().player.openGui(Mist.instance, 0, ctx.getServerHandler().player.world, (int)ctx.getServerHandler().player.posX, (int)ctx.getServerHandler().player.posY, (int)ctx.getServerHandler().player.posZ);
					ctx.getServerHandler().player.inventory.setItemStack(message.mask);
					try {
						PacketHandler.INSTANCE.sendTo(new PacketTranzitStack(message.mask), ctx.getServerHandler().player);
					} catch (Exception e) {}
	        	}
			}});
			return null;
		}
	}
}
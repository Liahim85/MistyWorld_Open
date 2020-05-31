package ru.liahim.mist.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import ru.liahim.mist.block.gizmos.MistFurnace;
import ru.liahim.mist.tileentity.TileEntityMistFurnace;

public class PacketFurnaceClose implements IMessage {

	int x, y, z, status;

	public PacketFurnaceClose() {}

	public PacketFurnaceClose(BlockPos pos, int status) {
		this.x = pos.getX();
		this.y = pos.getY();
		this.z = pos.getZ();
		this.status = status;
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeInt(x);
		buffer.writeInt(y);
		buffer.writeInt(z);
		buffer.writeInt(status);
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		x = buffer.readInt();
		y = buffer.readInt();
		z = buffer.readInt();
		status = buffer.readInt();
	}

	public static class Handler implements IMessageHandler<PacketFurnaceClose, IMessage> {
		@Override
		public IMessage onMessage(final PacketFurnaceClose message, final MessageContext ctx) {
			EntityPlayer player = ctx.getServerHandler().player;
			IThreadListener mainThread = (WorldServer)player.world; 
	        mainThread.addScheduledTask(new Runnable() {
	        @Override
			public void run() {
	        	BlockPos pos = new BlockPos(message.x, message.y, message.z);
	        	World world = ctx.getServerHandler().player.world;
	        	TileEntity te = world.getTileEntity(pos);
				if (te instanceof TileEntityMistFurnace) {
					((TileEntityMistFurnace)te).setClose(message.status == 0 || message.status == 4);
					if (world.isBlockIndirectlyGettingPowered(pos) > 0) ((TileEntityMistFurnace)te).setSignal(true);
					MistFurnace.setState(message.status, world, pos);
				}
			}});
			return null;
		}
	}
}
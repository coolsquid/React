package coolsquid.react.network;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import coolsquid.react.event.InternalEventManager;
import coolsquid.react.util.Log;

import io.netty.buffer.ByteBuf;

public class PacketReload implements IMessage {

	@Override
	public void fromBytes(ByteBuf buf) {

	}

	@Override
	public void toBytes(ByteBuf buf) {

	}

	public static class Handler implements IMessageHandler<PacketReload, IMessage> {

		@Override
		public IMessage onMessage(PacketReload message, MessageContext ctx) {
			if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
				Log.REACT.info("Received configs from server.");
				InternalEventManager.setupEvents();
			}
			return null;
		}
	}
}
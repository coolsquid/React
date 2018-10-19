package coolsquid.react.network;

import coolsquid.react.event.InternalEventManager;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketClean implements IMessage {

	@Override
	public void fromBytes(ByteBuf buf) {

	}

	@Override
	public void toBytes(ByteBuf buf) {

	}

	public static class Handler implements IMessageHandler<PacketClean, IMessage> {

		@Override
		public IMessage onMessage(PacketClean message, MessageContext ctx) {
			if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
				InternalEventManager.LISTENERS.clear();
			}
			return null;
		}
	}
}
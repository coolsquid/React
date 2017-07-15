package coolsquid.react.network;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import coolsquid.react.ConfigManager;

import com.typesafe.config.ConfigFactory;

import io.netty.buffer.ByteBuf;

public class PacketConfig implements IMessage {

	byte[] config;

	public PacketConfig() {

	}

	public PacketConfig(byte[] config) {
		this.config = config;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.config = new byte[buf.readableBytes()];
		buf.readBytes(this.config);
		buf.release();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBytes(this.config);
	}

	public static class Handler implements IMessageHandler<PacketConfig, IMessage> {

		private static boolean hasServerConfigs;

		@Override
		public IMessage onMessage(PacketConfig message, MessageContext ctx) {
			if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
				ConfigManager.load(ConfigFactory.parseString(new String(message.config)));
				hasServerConfigs = true;
			}
			return null;
		}

		public static boolean hasServerConfigs() {
			return hasServerConfigs;
		}
	}
}
package coolsquid.react.network;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import coolsquid.react.React;
import coolsquid.react.config.ConfigManager;
import coolsquid.react.util.Log;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketManager {

	private static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(React.MODID);

	public static void load() {
		INSTANCE.registerMessage(PacketConfig.Handler.class, PacketConfig.class, 0, Side.CLIENT);
		INSTANCE.registerMessage(PacketClean.Handler.class, PacketClean.class, 1, Side.CLIENT);
		INSTANCE.registerMessage(PacketReload.Handler.class, PacketReload.class, 2, Side.CLIENT);
	}

	public static void sendConfigsToClient(EntityPlayer player) {
		sendConfigsToClient(player, ConfigManager.CONFIG_DIRECTORY);
	}

	public static void sendConfigsToClient(EntityPlayer player, File... directories) {
		if (FMLCommonHandler.instance().getSide() != Side.SERVER || !ConfigManager.syncConfigs) {
			return;
		}
		if (player == null) {
			INSTANCE.sendToAll(new PacketClean());
		} else {
			INSTANCE.sendTo(new PacketClean(), (EntityPlayerMP) player);
		}
		for (File configDir : directories) {
			if (player == null) {
				Log.REACT.info("Sending configurations to %s clients",
						FMLCommonHandler.instance().getMinecraftServerInstance().getCurrentPlayerCount());
				for (File file : configDir.listFiles(ConfigManager.CONFIG_FILE_FILTER)) {
					try {
						INSTANCE.sendToAll(new PacketConfig(FileUtils.readFileToByteArray(file)));
					} catch (IOException e) {
						Log.REACT.error("Exception while reading file %s", file.getName());
						Log.REACT.catching(e);
					}
				}
				INSTANCE.sendToAll(new PacketReload());
			} else {
				Log.REACT.info("Sending configurations to player %s (%s)", player.getName(),
						player.getCachedUniqueIdString());
				for (File file : configDir.listFiles(ConfigManager.CONFIG_FILE_FILTER)) {
					try {
						INSTANCE.sendTo(new PacketConfig(FileUtils.readFileToByteArray(file)), (EntityPlayerMP) player);
					} catch (IOException e) {
						Log.REACT.error("Exception while reading file %s", file.getName());
						Log.REACT.catching(e);
					}
				}
				INSTANCE.sendTo(new PacketReload(), (EntityPlayerMP) player);
			}
		}
	}
}
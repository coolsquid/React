
package coolsquid.react;

import java.util.Map;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import coolsquid.react.base.Actions;
import coolsquid.react.base.Conditions;
import coolsquid.react.base.Events;
import coolsquid.react.base.Targets;
import coolsquid.react.config.ConfigManager;
import coolsquid.react.network.PacketConfig;
import coolsquid.react.network.PacketManager;
import coolsquid.react.util.CommandReact;
import coolsquid.react.util.Log;

import org.apache.logging.log4j.Level;

@Mod(modid = React.MODID, name = React.NAME, version = React.VERSION, dependencies = React.DEPENDENCIES, updateJSON = React.UPDATE_JSON_URL)
public class React {

	public static final String MODID = "react";
	public static final String NAME = "React";
	public static final String VERSION = "1.1.0";
	public static final String DEPENDENCIES = "required-after:forge@[14.21.1.2387,)";
	public static final String UPDATE_JSON_URL = "https://coolsquid.me/api/version/react.json";
	public static final String ISSUE_TRACKER_URL = "https://github.com/coolsquid/React/issues";
	public static final String CERTIFICATE_FINGERPRINT = "@FINGERPRINT@";

	@Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event) {
		Log.REACT.info("Running React v%s.", VERSION);
	}

	@Mod.EventHandler
	public void onInit(FMLInitializationEvent event) {
		Actions.register();
		Conditions.register();
		Events.register();
		Targets.register();

		MinecraftForge.EVENT_BUS.register(this);
		PacketManager.load();
	}

	@Mod.EventHandler
	public void onPostInit(FMLPostInitializationEvent event) {
		ConfigManager.load();
	}

	@Mod.EventHandler
	public void onInit(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandReact());
	}

	@SideOnly(Side.SERVER)
	@SubscribeEvent
	public void onPlayerLogIn(PlayerLoggedInEvent event) {
		PacketManager.sendConfigsToClient(event.player);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onDisconnect(ClientDisconnectionFromServerEvent event) {
		if (PacketConfig.Handler.hasServerConfigs()) {
			ConfigManager.load();
		}
	}

	@NetworkCheckHandler
	public boolean networkCheck(Map<String, String> versionMap, Side remote) {
		if (remote == Side.CLIENT && ConfigManager.syncConfigs) {
			return VERSION.equals(versionMap.get(MODID)); // if sync is enabled, connecting clients must use the same
															// React version as the server
		}
		return true;
	}

	//@Mod.EventHandler
	public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
		Log.REACT.log(false, Level.WARN,
				"The mod React is expecting signature %s for source %s, however there is no signature matching that description.",
				event.getExpectedFingerprint(), event.getSource().getName());
	}
}

package coolsquid.react.integration;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import coolsquid.react.config.ConfigManager;
import coolsquid.react.network.PacketManager;

import com.feed_the_beast.ftbl.api.events.ReloadEvent;

public class FTBLibIntegration implements Integration {

	public static final String MODID = "ftbl";

	@Optional.Method(modid = MODID)
	@SubscribeEvent
	public void onReload(ReloadEvent event) {
		ConfigManager.load();
		ConfigManager.load(event.getPackModeFile("react"), false);
		PacketManager.sendConfigsToClient(null, ConfigManager.CONFIG_DIRECTORY, event.getPackModeFile("react"));
	}

	@Override
	public void enable() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void disable() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@Override
	public String getModID() {
		return MODID;
	}
}
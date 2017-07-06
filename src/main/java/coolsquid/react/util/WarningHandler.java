package coolsquid.react.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WarningHandler {

	private final int errorCount;

	private WarningHandler(int errorCount) {
		this.errorCount = errorCount;
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
		if (Minecraft.getMinecraft().player != null) {
			this.sendWarning(Minecraft.getMinecraft().player);
			MinecraftForge.EVENT_BUS.unregister(this);
		}
	}

	private void sendWarning(EntityPlayer player) {
		player.sendMessage(new TextComponentString("<React> " + this.getErrorMessage()));
	}

	private String getErrorMessage() {
		if (this.errorCount == 1) {
			return "There was 1 error during loading. See the React log for more information.";
		} else {
			return "There were " + this.errorCount + " errors during loading. See the React log for more information.";
		}
	}

	@SideOnly(Side.CLIENT)
	public static void registerWarning(int errorCount) {
		WarningHandler warning = new WarningHandler(errorCount);
		if (Minecraft.getMinecraft().player == null) {
			MinecraftForge.EVENT_BUS.register(warning);
		} else {
			warning.sendWarning(Minecraft.getMinecraft().player);
		}
	}
}
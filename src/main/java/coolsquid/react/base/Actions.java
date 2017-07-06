
package coolsquid.react.base;

import static coolsquid.react.api.event.EventManager.registerAction;

import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;

import coolsquid.react.api.event.Action;
import coolsquid.react.util.Log;
import coolsquid.react.util.Util;

public class Actions {

	public static void register() {
		registerAction("send_chat", (Action<EntityPlayer>) (event, target, parameters) -> target
				.sendMessage(new TextComponentString((String) parameters.get("message"))), "message");
		registerAction("feed",
				(Action<EntityPlayer>) (event, target, parameters) -> target.getFoodStats().setFoodLevel(
						target.getFoodStats().getFoodLevel() + ((Number) parameters.get("amount")).intValue()),
				"amount");

		registerAction("kill", (Action<EntityLivingBase>) (event, target, parameters) -> target
				.attackEntityFrom(Util.getDamageSource(parameters.get("damage_source")), Float.MAX_VALUE));
		registerAction("damage",
				(Action<EntityLivingBase>) (event, target, parameters) -> target.attackEntityFrom(
						Util.getDamageSource(parameters.get("damage_source")),
						((Number) parameters.get("amount")).floatValue()),
				"amount");
		registerAction("heal", (Action<EntityLivingBase>) (event, target, parameters) -> target
				.heal(((Number) parameters.get("amount")).floatValue()), "amount");
		registerAction("set_time", (Action<World>) (event, target, parameters) -> {
			Object time = parameters.get("time");
			if (time instanceof String) {
				if (time.equals("day")) {
					time = 1000;
				} else if (time.equals("night")) {
					time = 13000;
				}
			}
			target.setWorldTime(((Number) time).longValue());
		}, "time");
		registerAction("change_weather", (Action<World>) (event, target, parameters) -> {
			int i = (300 + new Random().nextInt(600)) * 20;
			WorldInfo worldinfo = target.getWorldInfo();
			Boolean rain = (Boolean) parameters.get("rain");
			Boolean thunder = (Boolean) parameters.get("thunder");
			if (thunder == true) {
				worldinfo.setCleanWeatherTime(0);
				worldinfo.setRainTime(i);
				worldinfo.setThunderTime(i);
				worldinfo.setRaining(true);
				worldinfo.setThundering(true);
			} else if (rain == true) {
				worldinfo.setCleanWeatherTime(0);
				worldinfo.setRainTime(i);
				worldinfo.setThunderTime(i);
				worldinfo.setRaining(true);
			} else if (rain == false && thunder == false) {
				worldinfo.setCleanWeatherTime(i);
				worldinfo.setRainTime(0);
				worldinfo.setThunderTime(0);
				worldinfo.setRaining(false);
				worldinfo.setThundering(false);
			}
		});
		registerAction("burn", (Action<EntityLivingBase>) (event, target, parameters) -> target
				.setFire(((Number) parameters.get("duration")).intValue()), "duration");
		registerAction("extinguish", (Action<EntityLivingBase>) (event, target, parameters) -> target.extinguish());
		registerAction("add_potion_effect", (Action<EntityLivingBase>) (event, target, parameters) -> {
			Potion potion = Potion.REGISTRY.getObject(new ResourceLocation((String) parameters.get("potion")));
			if (potion == null) {
				throw new IllegalArgumentException("No such potion " + parameters.get("potion"));
			}
			Number amplifier = (Number) parameters.get("amplifier");
			Number duration = (Number) parameters.get("duration");
			target.addPotionEffect(new PotionEffect(potion, duration == null ? 0 : duration.intValue(),
					amplifier == null ? 0 : amplifier.intValue()));
		}, "potion");
		registerAction("remove_potion_effect",
				(Action<EntityLivingBase>) (event, target, parameters) -> target.removeActivePotionEffect(
						Potion.REGISTRY.getObject(new ResourceLocation((String) parameters.get("potion")))),
				"potion");
		registerAction("set_invulnerable", (Action<Entity>) (event, target, parameters) -> target
				.setEntityInvulnerable((boolean) parameters.get("invulnerable")), "invulnerable");
		registerAction("set_position", (Action<Entity>) (event, target, parameters) -> {
			double x = Util.getRelativeNumber(parameters.get("x"), target.posX);
			double y = Util.getRelativeNumber(parameters.get("y"), target.posY);
			double z = Util.getRelativeNumber(parameters.get("z"), target.posZ);
			target.setPosition(x, y, z);
		}, "x", "y", "z");

		registerAction("cancel", (event, target, parameters) -> {
			boolean success = false;
			if (event.isCancelable()) {
				event.setCanceled(true);
				success = true;
			}
			if (event.hasResult()) {
				event.setResult(Result.DENY);
				success = true;
			}
			if (event instanceof PlayerSleepInBedEvent) {
				((PlayerSleepInBedEvent) event).setResult(SleepResult.OTHER_PROBLEM);
				success = true;
			}
			if (!success) {
				Log.error("Tried to cancel uncancelable event %s", event.getClass().getName());
			}
		});
		registerAction("log", (event, target, parameters) -> Log.info((String) parameters.get("message")), "message");
	}
}
package coolsquid.react.base;

import static coolsquid.react.api.event.EventManager.registerCondition;

import net.minecraft.command.ICommand;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class Conditions {

	public static void register() {
		registerCondition("message",
				(event, variables, value) -> ((String) variables.get("message")).matches((String) value), "message");
		registerCondition("min_damage_amount",
				(event, variables, value) -> (float) variables.get("damage_amount") >= ((Number) value).floatValue(),
				"damage_amount");
		registerCondition("max_damage_amount",
				(event, variables, value) -> (float) variables.get("damage_amount") <= ((Number) value).floatValue(),
				"damage_amount");
		registerCondition("damage_source",
				(event, variables, value) -> value.equals(((DamageSource) variables.get("damage_source")).damageType),
				"damage_source");
		registerCondition("side",
				(event, variables, value) -> FMLCommonHandler.instance().getSide().name().toLowerCase().equals(value));
		registerCondition("hand",
				(event, variables, value) -> value.equals(variables.get("hand").toString().toLowerCase()), "hand");

		registerCondition("command",
				(event, variables, value) -> value.equals(((ICommand) variables.get("command")).getName()), "command");
		registerCondition("command_arguments", (event, variables, value) -> {
			String[] args = (String[]) variables.get("command_arguments");
			return String.join(" ", args).matches((String) value);
		}, "command_arguments");

		registerCondition("has_variables", (event, variables, value) -> {
			for (String s : (Iterable<String>) value) {
				if (!variables.containsKey(s)) {
					return false;
				}
			}
			return true;
		});

		registerCondition("remote_world",
				(event, variables, value) -> ((World) variables.get("world")).isRemote == (boolean) value, "world");
		registerCondition("mover_type", (event, variables, value) -> value.equals(variables.get("mover_type")),
				"mover_type");
	}

}
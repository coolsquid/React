package coolsquid.react.base;

import static coolsquid.react.api.event.EventManager.registerTarget;
import static coolsquid.react.api.event.EventManager.registerTargetCondition;
import static coolsquid.react.api.event.EventManager.registerTargetProperty;

import java.util.Collections;
import java.util.Objects;

import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.command.ICommand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import coolsquid.react.api.event.TargetCondition;
import coolsquid.react.api.event.TargetProperty;
import coolsquid.react.util.BlockWrapper;
import coolsquid.react.util.Util;

public class Targets {

	public static void registerTargets() {
		registerTarget("mob", (event, variables, list) -> list.add(variables.get("mob")), "mob");
		registerTarget("player", (event, variables, list) -> {
			Object mob = variables.get("mob");
			if (mob instanceof EntityPlayer) {
				list.add(variables.get("mob"));
			}
		}, "mob");
		registerTarget("world", (event, variables, list) -> list.add(variables.get("world")), "world");
		registerTarget("every_player", (event, variables, list) -> {
			if (FMLCommonHandler.instance().getMinecraftServerInstance() != null) {
				list.addAll(FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers());
			}
		});
		registerTarget("every_world", (event, variables, list) -> {
			if (FMLCommonHandler.instance().getMinecraftServerInstance() != null) {
				Collections.addAll(list, FMLCommonHandler.instance().getMinecraftServerInstance().worlds);
			}
		});
		registerTarget("block", (event, variables, list) -> list.add(variables.get("block")), "block");

		registerTarget("attacker", (event, variables, list) -> {
			Object attacker = variables.get("attacker");
			if (attacker != null) {
				list.add(attacker);
			}
		}, "attacker");
		registerTarget("sender", (event, variables, list) -> {
			Object sender = variables.get("sender");
			if (sender != null) {
				list.add(sender);
			}
		}, "sender");
		registerTarget("target_dimension", (event, variables, list) -> {
			Object targetDimension = variables.get("target_dimension");
			if (targetDimension != null) {
				list.add(targetDimension);
			}
		}, "target_dimension");
		registerTarget("tamer", (event, variables, list) -> {
			Object tamer = variables.get("tamer");
			if (tamer != null) {
				list.add(tamer);
			}
		}, "tamer");
		registerTarget("mounted_mob", (event, variables, list) -> {
			Object mounted = variables.get("mounted_mob");
			if (mounted != null) {
				list.add(mounted);
			}
		}, "mounted_mob");
		registerTarget("interaction_target", (event, variables, list) -> {
			Object target = variables.get("interaction_target");
			if (target instanceof EntityLivingBase) {
				list.add(target);
			}
		}, "interaction_target");
		registerTarget("explosion", (event, variables, list) -> list.add(variables.get("explosion")), "explosion");
		registerTarget("exploder", (event, variables, list) -> {
			Object target = variables.get("exploder");
			if (target instanceof EntityLivingBase) {
				list.add(target);
			}
		}, "exploder");
	}

	public static void registerTargetConditions() {
		registerTargetCondition("type", (TargetCondition<EntityLivingBase, String>) (target, expected) -> EntityList
				.isMatchingName(target, new ResourceLocation(expected)), EntityLivingBase.class);
		registerTargetCondition("name",
				(TargetCondition<Entity, String>) (target, expected) -> target.getName().matches(expected),
				Entity.class);
		registerTargetCondition("id", (TargetCondition<EntityLivingBase, String>) (target, expected) -> {
			Entity e = target.getCommandSenderEntity();
			return e != null && e.getCachedUniqueIdString().equals(expected);
		}, EntityLivingBase.class);
		registerTargetCondition("held_item", (TargetCondition<EntityLivingBase, String>) (target, expected) -> {
			if (target.getActiveHand() == null) {
				return expected.equals("none");
			}
			ItemStack heldItem = target.getHeldItem(target.getActiveHand());
			if (heldItem == null || heldItem.isEmpty()) {
				return expected.equals("none");
			} else {
				return heldItem.getItem().getRegistryName().toString().matches(expected);
			}
		}, EntityLivingBase.class);
		registerTargetCondition("active_potion", (TargetCondition<EntityLivingBase, String>) (target, expected) -> {
			return target.isPotionActive(Potion.REGISTRY.getObject(new ResourceLocation(expected)));
		}, EntityLivingBase.class);
		registerTargetCondition("active_hand",
				(target, expected) -> expected.equals(target.getActiveHand().toString().toLowerCase()),
				EntityLivingBase.class);
		registerTargetCondition("facing_direction", (TargetCondition<EntityLivingBase, String>) (target,
				expected) -> expected.equals(target.getHorizontalFacing().toString().toLowerCase()),
				EntityLivingBase.class);

		registerTargetCondition("time", (target, expected) -> {
			if (expected instanceof String) {
				return target.isDaytime() ? expected.equals("day") : expected.equals("night");
			} else {
				return target.getWorldTime() == ((Number) expected).longValue();
			}
		}, World.class);
		registerTargetCondition("min_time", (target, expected) -> {
			return target.getWorldTime() >= ((Number) expected).longValue();
		}, World.class);
		registerTargetCondition("max_time", (target, expected) -> {
			return target.getWorldTime() <= ((Number) expected).longValue();
		}, World.class);
		registerTargetCondition("difficulty",
				(target, expected) -> expected.equals(target.getWorldInfo().getDifficulty().toString().toLowerCase()),
				World.class);
		registerTargetCondition("gamemode", (target, expected) -> {
			World world = target;
			if (world.getWorldInfo().isHardcoreModeEnabled()) {
				return expected.equals("hardcore");
			} else {
				return expected.equals(world.getWorldInfo().getGameType().getName());
			}
		}, World.class);
		registerTargetCondition("id",
				(target, expected) -> ((Number) expected).intValue() == target.provider.getDimension(), World.class);

		registerTargetCondition("id",
				(target, expected) -> expected.equals(target.state.getBlock().getRegistryName().toString()),
				BlockWrapper.class);

		registerTargetCondition("min_xp_level",
				(target, expected) -> target.experienceLevel >= ((Number) expected).intValue(), EntityPlayer.class);
		registerTargetCondition("max_xp_level",
				(target, expected) -> target.experienceLevel <= ((Number) expected).intValue(), EntityPlayer.class);
		registerTargetCondition("min_xp", (target, expected) -> target.experience >= ((Number) expected).floatValue(),
				EntityPlayer.class);
		registerTargetCondition("max_xp", (target, expected) -> target.experience <= ((Number) expected).floatValue(),
				EntityPlayer.class);
		registerTargetCondition("min_total_xp",
				(target, expected) -> target.experienceTotal >= ((Number) expected).floatValue(), EntityPlayer.class);
		registerTargetCondition("max_total_xp",
				(target, expected) -> target.experienceTotal <= ((Number) expected).floatValue(), EntityPlayer.class);
		registerTargetCondition("min_food_level",
				(target, expected) -> target.getFoodStats().getFoodLevel() >= ((Number) expected).intValue(),
				EntityPlayer.class);
		registerTargetCondition("max_food_level",
				(target, expected) -> target.getFoodStats().getFoodLevel() <= ((Number) expected).intValue(),
				EntityPlayer.class);
		registerTargetCondition("min_saturation",
				(target, expected) -> target.getFoodStats().getSaturationLevel() >= ((Number) expected).intValue(),
				EntityPlayer.class);
		registerTargetCondition("max_saturation",
				(target, expected) -> target.getFoodStats().getSaturationLevel() <= ((Number) expected).intValue(),
				EntityPlayer.class);

		registerTargetCondition("can_use_command", (target, expected) -> {
			EntityPlayer player = target;
			ICommand command = player.getServer().getCommandManager().getCommands()
					.get(((String) expected).split(" ")[0]);
			if (command == null || !command.checkPermission(player.getServer(), player)) {
				return false;
			}
			return true;
		}, EntityPlayer.class);

		if (Loader.isModLoaded("gamestages")) {
			registerTargetCondition("has_game_stage", (TargetCondition<EntityPlayer, String>) (target,
					expected) -> GameStageHelper.getPlayerData(target).hasStage(expected), EntityPlayer.class);
		}
	}

	public static void registerTargetProperties() {
		registerTargetProperty("name", EntityLivingBase.class,
				(TargetProperty<EntityLivingBase>) (variable) -> variable.getName());
		registerTargetProperty("uuid", EntityLivingBase.class,
				(TargetProperty<EntityLivingBase>) (variable) -> variable.getCachedUniqueIdString());
		registerTargetProperty("position", EntityLivingBase.class,
				(TargetProperty<EntityLivingBase>) (variable) -> "[x=" + variable.posX + ",y=" + variable.posY + ",z="
						+ variable.posZ + "]");
		registerTargetProperty("type", EntityLivingBase.class,
				(variable) -> variable instanceof EntityPlayer ? "minecraft:player"
						: Objects.toString(EntityList.getKey(variable)));
		registerTargetProperty("x_position", EntityLivingBase.class, (variable) -> String.valueOf((int) variable.posX));
		registerTargetProperty("y_position", EntityLivingBase.class, (variable) -> String.valueOf((int) variable.posY));
		registerTargetProperty("z_position", EntityLivingBase.class, (variable) -> String.valueOf((int) variable.posZ));
		registerTargetProperty("health", EntityLivingBase.class,
				(TargetProperty<EntityLivingBase>) (variable) -> String.valueOf(variable.getHealth()));
		registerTargetProperty("helmet", EntityLivingBase.class, (TargetProperty<EntityLivingBase>) (variable) -> Util
				.getEquipmentName(EntityEquipmentSlot.HEAD, variable));
		registerTargetProperty("chestplate", EntityLivingBase.class,
				(TargetProperty<EntityLivingBase>) (variable) -> Util.getEquipmentName(EntityEquipmentSlot.CHEST,
						variable));
		registerTargetProperty("leggings", EntityLivingBase.class, (TargetProperty<EntityLivingBase>) (variable) -> Util
				.getEquipmentName(EntityEquipmentSlot.LEGS, variable));
		registerTargetProperty("boots", EntityLivingBase.class, (TargetProperty<EntityLivingBase>) (variable) -> Util
				.getEquipmentName(EntityEquipmentSlot.FEET, variable));
		registerTargetProperty("mainhand_item", EntityLivingBase.class,
				(TargetProperty<EntityLivingBase>) (variable) -> Util.getEquipmentName(EntityEquipmentSlot.MAINHAND,
						variable));
		registerTargetProperty("offhand_item", EntityLivingBase.class,
				(TargetProperty<EntityLivingBase>) (variable) -> Util.getEquipmentName(EntityEquipmentSlot.OFFHAND,
						variable));
		registerTargetProperty("current_item", EntityLivingBase.class,
				(TargetProperty<EntityLivingBase>) (variable) -> {
					Item item = variable.getHeldItem(variable.getActiveHand()).getItem();
					if (item == Items.AIR) {
						return null;
					}
					return item.getRegistryName().toString();
				});

		registerTargetProperty("ip", EntityPlayerMP.class,
				(TargetProperty<EntityPlayerMP>) (variable) -> variable.getPlayerIP());

		registerTargetProperty("id", BlockWrapper.class,
				(variable) -> variable.state.getBlock().getRegistryName().toString());
		registerTargetProperty("position", BlockWrapper.class, (variable) -> "[x=" + variable.pos.getX() + ",y="
				+ variable.pos.getY() + ",z=" + variable.pos.getZ() + "]");
		registerTargetProperty("x_position", BlockWrapper.class, (variable) -> String.valueOf(variable.pos.getX()));
		registerTargetProperty("y_position", BlockWrapper.class, (variable) -> String.valueOf(variable.pos.getY()));
		registerTargetProperty("z_position", BlockWrapper.class, (variable) -> String.valueOf(variable.pos.getZ()));

		registerTargetProperty("name", DamageSource.class,
				(TargetProperty<DamageSource>) (variable) -> variable.damageType);
		registerTargetProperty("name", ICommand.class, (TargetProperty<ICommand>) (variable) -> variable.getName());
		registerTargetProperty("list", String[].class,
				(TargetProperty<String[]>) (variable) -> String.join(", ", variable));

		registerTargetProperty("x_position", Explosion.class,
				(variable) -> String.valueOf((int) variable.getPosition().x));
		registerTargetProperty("y_position", Explosion.class,
				(variable) -> String.valueOf((int) variable.getPosition().y));
		registerTargetProperty("z_position", Explosion.class,
				(variable) -> String.valueOf((int) variable.getPosition().z));

		registerTargetProperty("id", World.class, (variable) -> String.valueOf(variable.provider.getDimension()));
		registerTargetProperty("seed", World.class, (variable) -> String.valueOf(variable.getSeed()));
		registerTargetProperty("time", World.class, (variable) -> String.valueOf(variable.getWorldTime()));

	}

	public static void register() {
		registerTargets();
		registerTargetConditions();
		registerTargetProperties();
	}
}
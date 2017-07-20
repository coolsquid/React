package coolsquid.react.base;

import static coolsquid.react.api.event.EventManager.registerTarget;
import static coolsquid.react.api.event.EventManager.registerTargetCondition;

import java.util.Collections;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import coolsquid.react.api.event.TargetCondition;

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
			ItemStack heldItem = target.getActiveItemStack();
			if (heldItem.isEmpty()) {
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

		registerTargetCondition("time",
				(target, expected) -> target.isDaytime() ? expected.equals("day") : expected.equals("night"),
				World.class);
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

		registerTargetCondition("name", (target, expected) -> expected.equals(target.getRegistryName().toString()),
				Block.class);

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
	}

	public static void register() {
		registerTargets();
		registerTargetConditions();
	}
}
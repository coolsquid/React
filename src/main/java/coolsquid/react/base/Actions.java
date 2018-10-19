
package coolsquid.react.base;

import static coolsquid.react.api.event.EventManager.registerAction;

import java.util.EnumSet;
import java.util.Random;

import com.typesafe.config.Config;

import coolsquid.react.api.event.Action;
import coolsquid.react.util.Log;
import coolsquid.react.util.Util;
import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.gamestages.GameStages;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.Event.Result;

public class Actions {

	public static void register() {
		registerAction("send_chat", (Action<EntityPlayer>) (event, target, parameters, variables) -> target
				.sendMessage(new TextComponentString((String) parameters.get("message"))), "message");
		registerAction("feed",
				(Action<EntityPlayer>) (event, target, parameters, variables) -> target.getFoodStats().setFoodLevel(
						target.getFoodStats().getFoodLevel() + ((Number) parameters.get("amount")).intValue()),
				"amount");

		registerAction("kill", (Action<EntityLivingBase>) (event, target, parameters, variables) -> target
				.attackEntityFrom(Util.getDamageSource(parameters.get("damage_source")), Float.MAX_VALUE));
		registerAction("damage",
				(Action<EntityLivingBase>) (event, target, parameters, variables) -> target.attackEntityFrom(
						Util.getDamageSource(parameters.get("damage_source")),
						((Number) parameters.get("amount")).floatValue()),
				"amount");
		registerAction("heal", (Action<EntityLivingBase>) (event, target, parameters, variables) -> target
				.heal(((Number) parameters.get("amount")).floatValue()), "amount");
		registerAction("set_time", (Action<World>) (event, target, parameters, variables) -> {
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
		registerAction("change_weather", (Action<World>) (event, target, parameters, variables) -> {
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
		registerAction("burn", (Action<EntityLivingBase>) (event, target, parameters, variables) -> target
				.setFire(((Number) parameters.get("duration")).intValue()), "duration");
		registerAction("extinguish",
				(Action<EntityLivingBase>) (event, target, parameters, variables) -> target.extinguish());
		registerAction("add_potion_effect", (Action<EntityLivingBase>) (event, target, parameters, variables) -> {
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
				(Action<EntityLivingBase>) (event, target, parameters, variables) -> target.removeActivePotionEffect(
						Potion.REGISTRY.getObject(new ResourceLocation((String) parameters.get("potion")))),
				"potion");
		registerAction("set_invulnerable", (Action<Entity>) (event, target, parameters, variables) -> target
				.setEntityInvulnerable((boolean) parameters.get("invulnerable")), "invulnerable");
		registerAction("set_position", (Action<Entity>) (event, target, parameters, variables) -> {
			double x = Util.getRelativeNumber(parameters.get("x"), target.posX);
			double y = Util.getRelativeNumber(parameters.get("y"), target.posY);
			double z = Util.getRelativeNumber(parameters.get("z"), target.posZ);
			if (target instanceof EntityPlayerMP) {
				((EntityPlayerMP) target).connection.setPlayerLocation(x, y, z, target.rotationYaw,
						target.rotationPitch, EnumSet.noneOf(SPacketPlayerPosLook.EnumFlags.class));
			} else {
				target.setPosition(x, y, z);
			}
			if (!(target instanceof EntityLivingBase) || !((EntityLivingBase) target).isElytraFlying()) {
				target.motionY = 0;
				target.onGround = true;
			}
		}, "x", "y", "z");

		registerAction("cancel", (event, target, parameters, variables) -> {
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
			} else if (event instanceof PlayerInteractEvent) {
				((PlayerInteractEvent) event).setCancellationResult(EnumActionResult.SUCCESS);
			}
			if (!success) {
				Log.REACT.error("Tried to cancel uncancelable event %s", event.getClass().getName());
			}
		});
		registerAction("log", (event, target, parameters, variables) -> {
			String logName = (String) parameters.get("log_name");
			Integer numToRetain = (Integer) parameters.get("maximum_backups");
			boolean compact = parameters.containsKey("compact") ? (boolean) parameters.get("compact") : false;
			if (numToRetain == null) {
				numToRetain = 5;
			}
			if (logName == null) {
				if (compact) {
					Log.REACT.logCompactly((String) parameters.get("message"));
				} else {
					Log.REACT.info((String) parameters.get("message"));
				}
			} else {
				Log log = Log.getLog(logName);
				if (log == null) {
					log = new Log(logName, "logs/react/" + logName, false, numToRetain);
				}
				if (compact) {
					log.logCompactly((String) parameters.get("message"));
				} else {
					log.info((String) parameters.get("message"));
				}
			}
		}, "message");
		registerAction("command",
				(event, target, parameters, variables) -> FMLCommonHandler.instance().getMinecraftServerInstance()
						.getCommandManager()
						.executeCommand((ICommandSender) target, (String) parameters.get("command")),
				"command");
		registerAction("server_command",
				(event, target, parameters, variables) -> FMLCommonHandler.instance().getMinecraftServerInstance()
						.getCommandManager().executeCommand(FMLCommonHandler.instance().getMinecraftServerInstance(),
								(String) parameters.get("command")),
				"command");
		registerAction("explode", (event, target, parameters, variables) -> {
			final World world = (World) variables.get("world");
			final boolean isFlaming = (boolean) parameters.get("flames");
			final boolean isSmoking = true; // (boolean) parameters.get("smoke"); // If false, nothing seems to explode
			final float strength = ((Number) parameters.get("strength")).floatValue();
			Entity entity = target instanceof Entity ? (Entity) target : null;
			Vec3d pos = Util.getCoordFromTarget(target, parameters);
			world.newExplosion(entity, pos.x, pos.y, pos.z, strength, isFlaming, isSmoking);
		}, "flames", "strength");
		registerAction("set_block", (event, target, parameters, variables) -> {
			final World world = (World) variables.get("world");
			final IBlockState newState = Block.getBlockFromName((String) parameters.get("block"))
					.getStateFromMeta(parameters.containsKey("meta") ? (int) parameters.get("meta") : 0);
			world.setBlockState(new BlockPos(Util.getCoordFromTarget(target, parameters)), newState);
		}, "block");
		registerAction("destroy_block", (event, target, parameters, variables) -> {
			((World) variables.get("world")).destroyBlock(new BlockPos(Util.getCoordFromTarget(target, parameters)),
					(boolean) parameters.get("drop"));
		}, "drop");
		registerAction("spawn_mob", (event, target, parameters, variables) -> {
			World world = ((World) variables.get("world"));
			Entity entity = EntityList
					.createEntityByIDFromName(new ResourceLocation((String) parameters.get("mob_type")), world);
			Vec3d loc = Util.getCoordFromTarget(target, parameters);
			entity.setPosition(loc.x, loc.y, loc.z);
			world.spawnEntity(entity);
		}, "mob_type");
		registerAction("spawn_item", (event, target, parameters, variables) -> {
			World world = ((World) variables.get("world"));
			Vec3d loc = Util.getCoordFromTarget(target, parameters);
			ItemStack stack = new ItemStack(
					Item.REGISTRY.getObject(new ResourceLocation((String) parameters.get("item"))), 1,
					parameters.containsKey("meta") ? ((Integer) parameters.get("meta")).intValue() : 0);
			if (parameters.containsKey("nbt")) {
				stack.setTagCompound(Util.createNBT((Config) parameters.get("nbt")));
			}
			EntityItem entity = new EntityItem(world, loc.x, loc.y, loc.z, stack);
			world.spawnEntity(entity);
		}, "item");

		if (Loader.isModLoaded("gamestages")) {
			registerAction("add_game_stage", (Action<EntityPlayer>) (event, target, parameters, variables) -> {
				GameStageHelper.getPlayerData(target).addStage((String) parameters.get("stage"));
			}, "stage");
			registerAction("remove_game_stage", (Action<EntityPlayer>) (event, target, parameters, variables) -> {
				GameStageHelper.getPlayerData(target).removeStage((String) parameters.get("stage"));
			}, "stage");
			registerAction("clear_game_stages", (Action<EntityPlayer>) (event, target, parameters, variables) -> {
				GameStageHelper.getPlayerData(target).clear();
			});
		}
	}
}
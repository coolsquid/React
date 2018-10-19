
package coolsquid.react.base;

import static coolsquid.react.api.event.EventManager.registerEvent;
import static coolsquid.react.api.event.EventManager.registerVariable;

import java.util.List;

import com.google.common.collect.Lists;

import coolsquid.react.api.event.EntityMoveEvent;
import coolsquid.react.util.BlockWrapper;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.stats.StatList;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.AnimalTameEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.CropGrowEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class Events {

	public static void register() {
		registerEvent("server_chat", ServerChatEvent.class);
		registerVariable(ServerChatEvent.class, "mob", (event) -> event.getPlayer());
		registerVariable(ServerChatEvent.class, "world", (event) -> event.getPlayer().world);
		registerVariable(ServerChatEvent.class, "message", (event) -> event.getMessage());
		registerVariable(ServerChatEvent.class, "sender", (event) -> event.getPlayer());

		registerEvent("client_chat", ClientChatReceivedEvent.class);
		registerVariable(ClientChatReceivedEvent.class, "message", (event) -> event.getMessage().getUnformattedText());

		registerVariable(EntityEvent.class, "mob",
				(event) -> event.getEntity() instanceof EntityLivingBase ? event.getEntity() : null);
		registerVariable(EntityEvent.class, "world", (event) -> event.getEntity().world);

		registerEvent("mob_hurt", LivingHurtEvent.class);
		registerEvent("mob_attacked", LivingAttackEvent.class);
		registerEvent("mob_death", LivingDeathEvent.class);
		registerVariable(LivingHurtEvent.class, "attacker", (event) -> event.getSource().getTrueSource());
		registerVariable(LivingAttackEvent.class, "attacker", (event) -> event.getSource().getTrueSource());
		registerVariable(LivingDeathEvent.class, "attacker", (event) -> event.getSource().getTrueSource());

		registerEvent("animal_tame", AnimalTameEvent.class);
		registerVariable(AnimalTameEvent.class, "tamer", (event) -> event.getTamer());

		registerEvent("mob_fall", LivingFallEvent.class);
		registerEvent("mob_heal", LivingHealEvent.class);
		registerEvent("mob_jump", LivingJumpEvent.class);

		registerEvent("mob_mount", EntityMountEvent.class, (event) -> event.isMounting());
		registerEvent("mob_dismount", EntityMountEvent.class, (event) -> event.isDismounting());
		registerVariable(EntityMountEvent.class, "mounted_mob", (event) -> event.getEntityBeingMounted());

		registerEvent("mob_spawn", LivingSpawnEvent.SpecialSpawn.class);

		registerEvent("mob_update", LivingUpdateEvent.class);

		registerVariable(net.minecraftforge.fml.common.gameevent.PlayerEvent.class, "mob", (event) -> event.player);
		registerVariable(net.minecraftforge.fml.common.gameevent.PlayerEvent.class, "player", (event) -> event.player);
		registerVariable(net.minecraftforge.fml.common.gameevent.PlayerEvent.class, "world",
				(event) -> event.player.world);

		registerEvent("player_log_in", PlayerLoggedInEvent.class);
		registerEvent("player_log_out", PlayerLoggedOutEvent.class);
		registerEvent("player_respawn", PlayerRespawnEvent.class, (event) -> !event.isEndConquered());

		registerEvent("interact_with_entity", PlayerInteractEvent.EntityInteractSpecific.class);
		registerVariable(PlayerInteractEvent.EntityInteractSpecific.class, "interaction_target",
				(event) -> event.getTarget());

		registerEvent("left_click_block", PlayerInteractEvent.LeftClickBlock.class);
		registerEvent("right_click_block", PlayerInteractEvent.RightClickBlock.class);
		registerEvent("right_click_item", PlayerInteractEvent.RightClickItem.class);

		registerVariable(EntityJoinWorldEvent.class, "mob", (event) -> {
			if (event.getEntity() instanceof EntityLivingBase) {
				return event.getEntity();
			}
			return null;
		});
		registerVariable(EntityJoinWorldEvent.class, "player", (event) -> {
			if (event.getEntity() instanceof EntityPlayer) {
				return event.getEntity();
			}
			return null;
		});
		registerVariable(EntityJoinWorldEvent.class, "world", (event) -> event.getEntity().world);
		registerEvent("player_spawn", PlayerLoggedInEvent.class,
				(event) -> FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList()
						.getPlayerStatsFile(event.player).readStat(StatList.LEAVE_GAME) == 0);

		registerEvent("player_wake_up", PlayerWakeUpEvent.class);
		registerEvent("player_sleep", PlayerSleepInBedEvent.class);
		registerVariable(PlayerSleepInBedEvent.class, "block",
				(event) -> new BlockWrapper(event.getEntityPlayer().world.getBlockState(event.getPos()),
						event.getPos()));
		registerEvent("fill_bucket", FillBucketEvent.class);

		registerEvent("render_player", RenderPlayerEvent.Pre.class, Side.CLIENT);
		registerEvent("render_hand", RenderSpecificHandEvent.class, Side.CLIENT);
		registerVariable(RenderSpecificHandEvent.class, "hand", (event) -> event.getHand());
		registerEvent("render_mob", RenderLivingEvent.Pre.class,
				(event) -> !(event.getEntity() instanceof EntityPlayer), Side.CLIENT);
		registerVariable(RenderLivingEvent.class, "mob", (event) -> event.getEntity());
		registerVariable(RenderLivingEvent.class, "world", (event) -> event.getEntity().world);

		registerVariable(LivingHurtEvent.class, "damage_amount", (event) -> event.getAmount());
		registerVariable(LivingAttackEvent.class, "damage_amount", (event) -> event.getAmount());

		registerVariable(LivingHurtEvent.class, "damage_source", (event) -> event.getSource());
		registerVariable(LivingAttackEvent.class, "damage_source", (event) -> event.getSource());
		registerVariable(LivingDeathEvent.class, "damage_source", (event) -> event.getSource());

		registerEvent("command", CommandEvent.class);
		registerVariable(CommandEvent.class, "command", (event) -> event.getCommand());
		registerVariable(CommandEvent.class, "command_arguments", (event) -> event.getParameters());
		registerVariable(CommandEvent.class, "sender", (event) -> event.getSender());
		registerVariable(CommandEvent.class, "world", (event) -> event.getSender().getEntityWorld());

		registerEvent("dimension_travel", EntityTravelToDimensionEvent.class);
		registerVariable(EntityTravelToDimensionEvent.class, "target_dimension",
				(event) -> DimensionManager.getWorld(event.getDimension()));

		registerVariable(WorldEvent.class, "world", (event) -> event.getWorld());
		registerEvent("sapling_grow", SaplingGrowTreeEvent.class);
		//registerVariable(SaplingGrowTreeEvent.class, "block",
		//		(event) -> event.getWorld().getBlockState(event.getPos()));
		registerVariable(SaplingGrowTreeEvent.class, "block",
				(event) -> new BlockWrapper(event.getWorld().getBlockState(event.getPos()), event.getPos()));

		registerVariable(BlockEvent.class, "world", (event) -> event.getWorld());
		//registerVariable(BlockEvent.class, "block", (event) -> event.getState());
		registerVariable(BlockEvent.class, "block", (event) -> new BlockWrapper(event.getState(), event.getPos()));
		registerVariable(BlockEvent.BreakEvent.class, "mob", (event) -> event.getPlayer());
		registerVariable(BlockEvent.PlaceEvent.class, "mob", (event) -> event.getPlayer());
		registerEvent("block_break", BlockEvent.BreakEvent.class);
		registerEvent("block_place", BlockEvent.PlaceEvent.class);
		registerEvent("crop_grow", CropGrowEvent.Pre.class);

		registerVariable(TickEvent.WorldTickEvent.class, "world", (event) -> event.world);
		registerEvent("world_tick", TickEvent.WorldTickEvent.class);

		registerEvent("pre_explosion", ExplosionEvent.Start.class);
		registerVariable(ExplosionEvent.class, "world", (event) -> event.getWorld());
		registerVariable(ExplosionEvent.class, "explosion", (event) -> event.getExplosion());
		registerVariable(ExplosionEvent.class, "exploder", (event) -> event.getExplosion().getExplosivePlacedBy());

		registerEvent("explosion", ExplosionEvent.Detonate.class);
		registerVariable(ExplosionEvent.Detonate.class, "exploding_blocks", (event) -> {
			List<String> list = Lists.transform(event.getAffectedBlocks(), (e) -> {
				Block block = event.getWorld().getBlockState(e).getBlock();
				if (block == Blocks.AIR) {
					return null;
				}
				return "[" + block.getRegistryName().toString() + " at " + String.valueOf(e.getX()) + ","
						+ String.valueOf(e.getY()) + "," + String.valueOf(e.getZ()) + "]";
			});
			list.removeIf((e) -> e == null);
			return list;
		});
		registerVariable(ExplosionEvent.Detonate.class, "exploding_mobs", (event) -> {
			List<String> list = Lists.transform(event.getAffectedEntities(), (e) -> {
				if (e instanceof EntityLivingBase) {
					return "[" + e.getName() + " at " + String.valueOf(e.posX) + "," + String.valueOf(e.posY) + ","
							+ String.valueOf(e.posZ) + "]";
				}
				return null;
			});
			list.removeIf((e) -> e == null);
			return list;
		});
		
		registerEvent("mob_move", EntityMoveEvent.class, (event) -> event.getEntity() instanceof EntityLivingBase);
		registerVariable(EntityMoveEvent.class, "mover_type", (event) -> event.getMoverType().toString().toLowerCase());
	}
}
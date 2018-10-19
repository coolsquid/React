package coolsquid.react.asm;

import coolsquid.react.api.event.EntityMoveEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraftforge.common.MinecraftForge;

public class Hooks {

	public static boolean fireMoveEvent(Entity entity, MoverType type, double x, double y, double z) {
		return MinecraftForge.EVENT_BUS.post(new EntityMoveEvent(entity, type, x, y, z));
	}
}
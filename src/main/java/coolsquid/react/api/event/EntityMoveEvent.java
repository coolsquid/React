package coolsquid.react.api.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraftforge.event.entity.EntityEvent;

public class EntityMoveEvent extends EntityEvent {

	private final MoverType type;
	private final double x, y, z;

	public EntityMoveEvent(Entity entity, MoverType type, double x, double y, double z) {
		super(entity);
		this.type = type;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public MoverType getMoverType() {
		return this.type;
	}

	public double getX() {
		return this.x;
	}

	public double getY() {
		return this.y;
	}

	public double getZ() {
		return this.z;
	}

	@Override
	public boolean isCancelable() {
		return true;
	}
}
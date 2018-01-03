
package coolsquid.react.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;

public class Util {

	public static final Map<String, DamageSource> STATIC_DAMAGE_SOURCES = new HashMap<>();

	public static Vec3d getCoordFromTarget(Object target, Map<String, Object> parameters) {
		double x;
		double y;
		double z;
		if (target instanceof EntityLivingBase) {
			x = ((EntityLivingBase) target).posX;
			y = ((EntityLivingBase) target).posY;
			z = ((EntityLivingBase) target).posZ;
		} else if (target instanceof BlockWrapper) {
			x = ((BlockWrapper) target).pos.getX();
			y = ((BlockWrapper) target).pos.getY();
			z = ((BlockWrapper) target).pos.getZ();
		} else if (target == null) {
			x = ((Number) parameters.get("x")).doubleValue();
			y = ((Number) parameters.get("y")).doubleValue();
			z = ((Number) parameters.get("z")).doubleValue();
		} else {
			throw new RuntimeException(
					"Target type " + target.getClass().getName() + " cannot be used with this action");
		}
		return new Vec3d(getRelativeNumber(parameters.get("x"), x), getRelativeNumber(parameters.get("y"), y),
				getRelativeNumber(parameters.get("z"), z));
	}

	public static double getRelativeNumber(Object number, double oldNumber) {
		if (number instanceof String) {
			String s = ((String) number).trim();
			if (s.startsWith("~")) {
				if (s.length() > 1) {
					return oldNumber + Double.parseDouble(s.substring(1));
				} else {
					return oldNumber;
				}
			}
		} else if (number instanceof Number) {
			return ((Number) number).doubleValue();
		} else if (number == null) {
			return oldNumber;
		}
		Log.REACT.error("Failed to parse parameter: %s", number);
		throw new RuntimeException("Failed to parse parameter");
	}

	public static DamageSource getDamageSource(Object name) {
		String sourceName = (String) name;
		DamageSource source;
		if (sourceName == null) {
			source = DamageSource.MAGIC;
		} else {
			source = Util.STATIC_DAMAGE_SOURCES.get(sourceName);
			if (source == null) {
				Log.REACT.error("Damage source %s was not found. Defaulting to magic.", sourceName);
				source = DamageSource.MAGIC;
			}
		}
		return source;
	}

	public static String getEquipmentName(EntityEquipmentSlot slot, EntityLivingBase entity) {
		Item item = entity.getItemStackFromSlot(slot).getItem();
		if (item == Items.AIR) {
			return null;
		}
		return item.getRegistryName().toString();
	}

	static {
		for (Field field : DamageSource.class.getFields()) {
			if (DamageSource.class.isAssignableFrom(field.getType()) && Modifier.isStatic(field.getModifiers())
					&& Modifier.isFinal(field.getModifiers())) {
				try {
					DamageSource source = (DamageSource) field.get(null);
					if (source != null && source.damageType != null) {
						STATIC_DAMAGE_SOURCES.put(source.damageType, source);
					}
				} catch (Exception e) {
					Log.REACT.error("Could not retrieve damage source %s", field.getName());
					Log.REACT.catching(e);
				}
			}
		}
	}
}
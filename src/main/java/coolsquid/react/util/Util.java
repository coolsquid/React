
package coolsquid.react.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.DamageSource;

public class Util {

	public static final Map<String, DamageSource> STATIC_DAMAGE_SOURCES = new HashMap<>();

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
		}
		Log.error("Failed to parse parameter: %s", number);
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
				Log.error("Damage source %s was not found. Defaulting to magic.", sourceName);
				source = DamageSource.MAGIC;
			}
		}
		return source;
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
					Log.error("Could not retrieve damage source %s", field.getName());
					Log.catching(e);
				}
			}
		}
	}
}
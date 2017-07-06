package coolsquid.react.api;

import net.minecraftforge.fml.common.eventhandler.Event;

@FunctionalInterface
public interface Variable<V, E extends Event> {

	V get(E event);
}
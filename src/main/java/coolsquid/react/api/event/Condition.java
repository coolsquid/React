package coolsquid.react.api.event;

import java.util.Map;

import net.minecraftforge.fml.common.eventhandler.Event;

@FunctionalInterface
public interface Condition<E> {

	boolean allow(Event event, Map<String, Object> variables, E expectedValue);
}
package coolsquid.react.api;

import java.util.Map;

import net.minecraftforge.fml.common.eventhandler.Event;

@FunctionalInterface
public interface Action<T> {

	void execute(Event event, T target, Map<String, Object> parameters);
}
package coolsquid.react.api;

import java.util.List;
import java.util.Map;

import net.minecraftforge.fml.common.eventhandler.Event;

@FunctionalInterface
public interface Target<T> {

	void get(Event event, Map<String, Object> variables, List<T> list);
}
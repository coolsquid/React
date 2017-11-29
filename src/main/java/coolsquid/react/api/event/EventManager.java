package coolsquid.react.api.event;

import java.util.function.Predicate;

import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;

import coolsquid.react.event.InternalEventManager;

public class EventManager {

	public static void registerEvent(String name, Class<? extends Event> eventClass) {
		InternalEventManager.registerEvent(name, eventClass, null, null);
	}

	public static <E extends Event> void registerEvent(String name, Class<E> eventClass, Predicate<E> shouldFire) {
		InternalEventManager.registerEvent(name, eventClass, shouldFire, null);
	}

	public static void registerEvent(String name, Class<? extends Event> eventClass, Side side) {
		InternalEventManager.registerEvent(name, eventClass, null, side);
	}

	public static <E extends Event> void registerEvent(String name, Class<E> eventClass, Predicate<E> shouldFire,
			Side side) {
		InternalEventManager.registerEvent(name, eventClass, shouldFire, side);
	}

	public static void registerAction(String name, Action<?> action, String... requiredParameters) {
		InternalEventManager.registerAction(name, action, requiredParameters);
	}

	public static void registerTarget(String name, Target<?> target, String... requiredVariables) {
		InternalEventManager.registerTarget(name, target, requiredVariables);
	}

	public static <T> void registerTargetCondition(String name, TargetCondition<T, ?> targetCondition,
			Class<T> targetType) {
		InternalEventManager.registerTargetCondition(name, targetCondition, targetType);
	}

	public static void registerCondition(String name, Condition<?> condition, String... requiredVariables) {
		InternalEventManager.registerCondition(name, condition, requiredVariables);
	}

	public static <E extends Event> void registerVariable(Class<E> eventType, String name, Variable<?, E> variable) {
		InternalEventManager.registerVariable(eventType, name, variable);
	}

	public static <V> void registerTargetProperty(String property, Class<V> variableType, TargetProperty<V> text) {
		InternalEventManager.registerTargetProperty(property, variableType, text);
	}
}
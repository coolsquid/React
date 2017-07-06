package coolsquid.react.api.event;

@FunctionalInterface
public interface TargetCondition<T, E> {

	boolean allow(T target, E expected);
}
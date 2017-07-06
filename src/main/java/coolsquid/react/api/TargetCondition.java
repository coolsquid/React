package coolsquid.react.api;

@FunctionalInterface
public interface TargetCondition<T, E> {

	boolean allow(T target, E expected);
}
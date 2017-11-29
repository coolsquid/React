package coolsquid.react.api.event;

@FunctionalInterface
public interface TargetProperty<V> {

	String getText(V variable);
}
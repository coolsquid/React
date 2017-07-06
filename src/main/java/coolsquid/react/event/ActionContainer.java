package coolsquid.react.event;

import java.util.Map;

import coolsquid.react.api.event.Action;

public class ActionContainer {

	public final Action<?> action;
	public final String target;
	public final Map<String, Object> parameters, conditions, targetConditions;

	public ActionContainer(Action<?> action, String target, Map<String, Object> parameters,
			Map<String, Object> conditions, Map<String, Object> targetConditions) {
		this.action = action;
		this.target = target;
		this.parameters = parameters;
		this.conditions = conditions;
		this.targetConditions = targetConditions;
	}
}
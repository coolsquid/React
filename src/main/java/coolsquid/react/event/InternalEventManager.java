
package coolsquid.react.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;

import coolsquid.react.React;
import coolsquid.react.api.event.Action;
import coolsquid.react.api.event.Condition;
import coolsquid.react.api.event.Target;
import coolsquid.react.api.event.TargetCondition;
import coolsquid.react.api.event.TargetProperty;
import coolsquid.react.api.event.Variable;
import coolsquid.react.config.ConfigManager;
import coolsquid.react.util.Log;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;

public class InternalEventManager {

	private static final InternalEventManager INSTANCE = new InternalEventManager();

	public static final ListMultimap<Class<? extends Event>, ActionContainer> LISTENERS = ArrayListMultimap.create();

	public static final Map<String, Class<? extends Event>> LISTENER_TYPES = new HashMap<>();
	public static final Map<Class<? extends Event>, Predicate<? extends Event>> EVENT_PREDICATES = new HashMap<>();

	public static final Map<Class<? extends Event>, Map<String, Variable<?, ?>>> VARIABLES = new HashMap<>();
	public static final Map<String, Action<?>> ACTIONS = new HashMap<>();
	public static final Map<String, Target<?>> TARGETS = new HashMap<>();
	public static final Map<String, Condition<?>> CONDITIONS = new HashMap<>();
	public static final Map<Class<?>, Map<String, TargetCondition<?, ?>>> TARGET_CONDITIONS = new HashMap<>();

	public static final SetMultimap<String, String> CONDITIONS_REQUIRED_VARS = HashMultimap.create();
	public static final SetMultimap<String, String> TARGETS_REQUIRED_VARS = HashMultimap.create();
	public static final SetMultimap<String, String> ACTIONS_REQUIRED_PARAMS = HashMultimap.create();

	public static final Map<Class<?>, Map<String, TargetProperty<?>>> TARGET_PROPERTIES = new HashMap<>();

	private static final Pattern VARIABLE_PATTERN = Pattern.compile("%%%(\\w|\\.)+%%%");

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onEvent(Event event) {
		List<ActionContainer> actionContainers = LISTENERS.get(event.getClass());
		if (ConfigManager.debug) {
			Log.REACT.debug("Received event %s", event.getClass().getName());
			Log.REACT.debug("Found %s action containers", actionContainers.size());
		}
		if (!actionContainers.isEmpty()) {
			Predicate eventPredicate = EVENT_PREDICATES.get(event.getClass());
			if (eventPredicate != null && !eventPredicate.test(event)) {
				if (ConfigManager.debug) {
					Log.REACT.debug("Actions will not fire, as the internal requirements have not been met");
				}
				return;
			}
			actionLoop: for (ActionContainer actionContainer : actionContainers) {
				Action action = actionContainer.action;
				if (ConfigManager.debug) {
					Log.REACT.debug("Preparing action %s", actionContainer.action);
					Log.REACT.debug("Constructing variables");
				}
				Map<String, Object> variables = new HashMap<>();
				for (Class c = event.getClass(); c != Event.class; c = c.getSuperclass()) {
					Map<String, Variable<?, ?>> map = VARIABLES.get(c);
					if (map != null) {
						map.forEach((key, value) -> {
							Object value2 = ((Variable) value).get(event);
							if (value2 != null) {
								variables.put(key, value2);
							}
						});
					}
				}
				if (ConfigManager.debug) {
					Log.REACT.debug("Checking conditions");
				}
				conditionLoop: for (Entry<String, Object> conditionEntry : actionContainer.conditions.entrySet()) {
					if (ConfigManager.debug) {
						Log.REACT.debug("Checking condition %s", conditionEntry.getKey());
					}
					if (conditionEntry.getKey().contains(".")) {
						String[] conditionParts = conditionEntry.getKey().split("\\.");
						String conditionTargetKey = conditionParts[0];
						String conditionKey = conditionParts[1];
						boolean negative = conditionKey.startsWith(ConfigManager.NEGATIVE_PREFIX);
						if (negative) {
							conditionKey = conditionKey.substring(1);
						}
						for (String requiredVar : TARGETS_REQUIRED_VARS.get(conditionTargetKey)) {
							if (!variables.containsKey(requiredVar)) {
								if (ConfigManager.debug) {
									Log.REACT.debug(
											"Variable %s, required by condition %s, is missing. The condition will be ignored.");
								}
								continue conditionLoop;
							}
						}
						Target conditionTarget = TARGETS.get(conditionTargetKey);
						List conditionTargetList = new ArrayList<>();
						conditionTarget.get(event, variables, conditionTargetList);
						if (conditionTargetList.isEmpty()) {
							if (ConfigManager.debug) {
								Log.REACT.debug("No target objects for target %s found, skipping");
							}
							continue actionLoop;
						}
						for (Object conditionTargetObj : conditionTargetList) {
							for (Class<?> c = conditionTargetObj.getClass(); c != Object.class; c = c.getSuperclass()) {
								Map<String, TargetCondition<?, ?>> map = TARGET_CONDITIONS.get(c);
								if (map == null) {
									if (ConfigManager.debug) {
										Log.REACT.debug("No such target condition %s, continuing", c.getName());
									}
									continue;
								}
								TargetCondition tc = map.get(conditionKey);
								if (tc != null) {
									boolean passed = tc.allow(conditionTargetObj, conditionEntry.getValue());
									if (passed && negative) {
										if (ConfigManager.debug) {
											Log.REACT.debug("Condition %s not met, skipping", conditionEntry.getKey());
										}
										continue actionLoop;
									} else if (!passed && !negative) {
										if (ConfigManager.debug) {
											Log.REACT.debug("Condition %s not met, skipping", conditionEntry.getKey());
										}
										continue actionLoop;
									}
								}
							}
						}
					} else {
						String conditionKey = conditionEntry.getKey();
						boolean negative = conditionKey.startsWith(ConfigManager.NEGATIVE_PREFIX);
						if (negative) {
							conditionKey = conditionKey.substring(1);
						}
						for (String requiredVar : CONDITIONS_REQUIRED_VARS.get(conditionKey)) {
							if (!variables.containsKey(requiredVar)) {
								if (ConfigManager.debug) {
									Log.REACT.debug("Variable %s, required by condition %s, is missing. Skipping.",
											conditionEntry.getKey());
								}
								continue actionLoop;
							}
						}
						Condition condition = CONDITIONS.get(conditionKey);
						boolean passed = condition.allow(event, variables, conditionEntry.getValue());
						if (passed && negative) {
							if (ConfigManager.debug) {
								Log.REACT.debug("Condition %s not met, skipping", conditionEntry.getKey());
							}
							continue actionLoop;
						} else if (!passed && !negative) {
							if (ConfigManager.debug) {
								Log.REACT.debug("Condition %s not met, skipping", conditionEntry.getKey());
							}
							continue actionLoop;
						}
					}
				}
				if (ConfigManager.debug) {
					Log.REACT.debug("Inserting variables");
				}
				Map<String, Object> parameters = new HashMap<>(actionContainer.parameters);
				insertVariables(parameters, variables, event);
				if (!actionContainer.target.isEmpty()) {
					if (ConfigManager.debug) {
						Log.REACT.debug("Found target %s", actionContainer.target);
					}
					for (String requiredVar : TARGETS_REQUIRED_VARS.get(actionContainer.target)) {
						if (!variables.containsKey(requiredVar)) {
							if (ConfigManager.debug) {
								Log.REACT.debug("Missing variable %s for target, skipping", requiredVar);
							}
							continue actionLoop;
						}
					}
					Target target = TARGETS.get(actionContainer.target);
					if (target == null) {
						if (ConfigManager.debug) {
							Log.REACT.debug("Target is null, skipping");
						}
						continue actionLoop;
					}
					List targetList = new ArrayList<>();
					target.get(event, variables, targetList);
					Iterator<?> i = targetList.iterator();
					l2: while (i.hasNext()) {
						Object in = i.next();
						for (Class<?> c = in.getClass(); c != Object.class; c = c.getSuperclass()) {
							Map<String, TargetCondition<?, ?>> map = TARGET_CONDITIONS.get(c);
							if (map == null) {
								continue;
							}
							for (Entry<String, Object> tcm : actionContainer.targetConditions.entrySet()) {
								String key = tcm.getKey();
								boolean negative = key.startsWith(ConfigManager.NEGATIVE_PREFIX);
								if (negative) {
									key = key.substring(1);
								}
								TargetCondition tc = map.get(key);
								if (tc != null) {
									boolean passed = tc.allow(in, tcm.getValue());
									if (passed && negative) {
										i.remove();
										if (ConfigManager.debug) {
											Log.REACT.debug("Target object %s was removed by filter %s", in, key);
										}
										continue l2;
									}
									if (!passed && !negative) {
										i.remove();
										if (ConfigManager.debug) {
											Log.REACT.debug("Target object %s was removed by filter %s", in, key);
										}
										continue l2;
									}
								}
							}
						}
					}
					for (Object x : targetList) {
						if (ConfigManager.debug) {
							Log.REACT.debug("Executing action on target object %s", x);
						}
						try {
							action.execute(event, x, parameters, variables);
						} catch (Exception e1) {
							Log.REACT.error("An exception occured while executing script");
							Log.REACT.catching(e1);
						}
					}
				} else {
					if (ConfigManager.debug) {
						Log.REACT.debug("No target, going straight to execution");
						Log.REACT.debug("Executing action");
					}
					try {
						action.execute(event, null, parameters, variables);
					} catch (Exception e1) {
						Log.REACT.error("An exception occured while executing script");
						Log.REACT.catching(e1);
					}
				}
			}
		}
	}

	private static void insertVariables(Map<String, Object> input, Map<String, Object> variables, Event event) {
		input.replaceAll((key, value) -> {
			if (value instanceof String) {
				String string = (String) value;
				Matcher matcher = VARIABLE_PATTERN.matcher(string);
				while (matcher.find()) {
					String group = matcher.group();
					String toReplace = group.replace("%%%", "").replace("%%%", "");
					if (toReplace.contains(".")) {
						String[] parts = toReplace.split("\\.");
						Target t = TARGETS.get(parts[0]);
						if (t == null) {
							Object obj = variables.get(parts[0]);
							if (obj == null) {
								Log.REACT.error("No such target or variable %s", parts[0]);
							}
							List<String> st = new ArrayList<>();
							for (Class varClass = obj.getClass(); varClass != Object.class; varClass = varClass
									.getSuperclass()) {
								Map<String, TargetProperty<?>> map = TARGET_PROPERTIES.get(varClass);
								if (map != null) {
									TargetProperty varText = map.get(parts[1]);
									if (varText != null) {
										st.add(varText.getText(obj));
									}
								} else {
									for (Class intClass : varClass.getInterfaces()) {
										map = TARGET_PROPERTIES.get(intClass);
										if (map != null) {
											TargetProperty varText = map.get(parts[1]);
											if (varText != null) {
												st.add(varText.getText(obj));
											}
										}
									}
								}
							}
							if (st.isEmpty()) {
								Log.REACT.error("Property %s for variable %s was not found", parts[1], parts[0]);
							} else if (st.size() == 1) {
								string = string.replace(group, st.get(0));
							} else {
								string = string.replace(group, "[" + String.join(", ", st) + "]");
							}
						} else {
							List<?> list = new ArrayList<>();
							t.get(event, variables, list);
							List<String> st = new ArrayList<>();
							for (Object obj : list) {
								for (Class varClass = obj.getClass(); varClass != Object.class; varClass = varClass
										.getSuperclass()) {
									Map<String, TargetProperty<?>> map = TARGET_PROPERTIES.get(varClass);
									if (map != null) {
										TargetProperty varText = map.get(parts[1]);
										if (varText != null) {
											st.add(varText.getText(obj));
										}
									} else {
										for (Class intClass : varClass.getInterfaces()) {
											map = TARGET_PROPERTIES.get(intClass);
											if (map != null) {
												TargetProperty varText = map.get(parts[1]);
												if (varText != null) {
													st.add(varText.getText(obj));
												}
											}
										}
									}
								}
							}
							if (st.isEmpty()) {
								Log.REACT.error("Property %s for target %s was not found", parts[1], parts[0]);
								string = string.replace(group, "null");
							} else if (st.size() == 1) {
								String str = st.get(0);
								if (str == null) {
									string = string.replace(group, "null");
								} else {
									string = string.replace(group, str);
								}
							} else {
								string = string.replace(group, "[" + String.join(", ", st) + "]");
							}
						}
						/*Object var = variables.get(parts[0]);
						if (var == null) {
							continue l1;
						}
						for (Class varClass = var.getClass(); varClass != Object.class; varClass = varClass
								.getSuperclass()) {
							Map<String, VariableText<?>> map = VARIABLE_TEXT.get(varClass);
							if (map != null) {
								VariableText varText = map.get(parts[1]);
								if (varText != null) {
									string = string.replace(group, varText.getText(var));
									continue l1;
								}
							}
						}
						Log.REACT.error("Property %s for variable %s was not found", parts[1], parts[0]);*/
					} else {
						Object var = variables.get(toReplace);
						if (var == null) {
							Log.REACT.error("Variable %s was not found", toReplace);
						} else {
							string = string.replace(group, String.valueOf(var));
						}
					}
				}
				return string;
			} else {
				return value;
			}
		});
	}

	public static void setupEvents() {
		MinecraftForge.EVENT_BUS.unregister(INSTANCE);
		try {
			Method method = EventBus.class.getDeclaredMethod("register", Class.class, Object.class, Method.class,
					ModContainer.class);
			method.setAccessible(true);
			Method targetMethod = InternalEventManager.class.getMethod("onEvent", Event.class);
			for (Class<? extends Event> e : LISTENERS.keySet()) {
				if (e != NeverFiredEvent.class) {
					method.invoke(MinecraftForge.EVENT_BUS, e, INSTANCE, targetMethod,
							Loader.instance().activeModContainer());
				}
			}
			Map<Object, ModContainer> listenerOwners = ReflectionHelper.getPrivateValue(EventBus.class,
					MinecraftForge.EVENT_BUS, "listenerOwners");
			listenerOwners.put(INSTANCE, Loader.instance().getIndexedModList().get(React.MODID));
		} catch (Exception e) {
			Log.REACT.error("An error occured while hacking the Forge event bus. Registering normally instead.");
			Log.REACT.error("Please report this at %s. Include both the FML log and the React log.",
					React.ISSUE_TRACKER_URL);
			Log.REACT.catching(e);
			MinecraftForge.EVENT_BUS.unregister(INSTANCE);
			MinecraftForge.EVENT_BUS.register(INSTANCE);
		}
	}

	public static void registerListener(Class<? extends Event> eventType, Action<?> action, String target,
			Map<String, Object> parameters, Map<String, Object> conditions, Map<String, Object> targetConditions) {
		if (ConfigManager.debug) {
			Log.REACT.debug("Registering listener for event %s", eventType.getName());
		}
		LISTENERS.put(eventType, new ActionContainer(action, target, parameters, conditions, targetConditions));
	}

	public static void registerEvent(String name, Class<? extends Event> eventClass,
			Predicate<? extends Event> shouldFire, Side side) {
		if (side != null && side != FMLCommonHandler.instance().getSide()) {
			eventClass = NeverFiredEvent.class;
		}
		Class<? extends Event> event = LISTENER_TYPES.get(name);
		if (event != null) {
			Log.REACT.warn("Duplicate event %s (%s) registered. This will override the previous entry (%s).", name,
					eventClass.getName(), event.getName());
		}
		LISTENER_TYPES.put(name, eventClass);
		if (shouldFire != null) {
			EVENT_PREDICATES.put(eventClass, shouldFire);
		}
	}

	public static void registerAction(String name, Action<?> action, String... requiredParameters) {
		Action<?> previousAction = ACTIONS.get(name);
		if (previousAction != null) {
			Log.REACT.warn("Duplicate action %s (%s) registered. This will override the previous entry (%s).", name,
					action.getClass().getName(), previousAction.getClass().getName());
		}
		ACTIONS.put(name, action);
		for (String requiredParameter : requiredParameters) {
			ACTIONS_REQUIRED_PARAMS.put(name, requiredParameter);
		}
	}

	public static <T> void registerTarget(String name, Target<T> target, String... requiredVariables) {
		Target<?> previousTarget = TARGETS.get(name);
		if (previousTarget != null) {
			Log.REACT.warn("Duplicate target %s (%s) registered. This will override the previous entry (%s).", name,
					target.getClass().getName(), previousTarget.getClass().getName());
		}
		TARGETS.put(name, target);
		for (String requiredVariable : requiredVariables) {
			TARGETS_REQUIRED_VARS.put(name, requiredVariable);
		}
	}

	public static <T> void registerTargetCondition(String name, TargetCondition<T, ?> targetCondition,
			Class<T> targetType) {
		Map<String, TargetCondition<?, ?>> map = TARGET_CONDITIONS.get(targetType);
		if (map == null) {
			map = new HashMap<>();
			TARGET_CONDITIONS.put(targetType, map);
		}
		TargetCondition<?, ?> previousTargetCondition = map.get(name);
		if (previousTargetCondition != null) {
			Log.REACT.warn(
					"Duplicate target condition %s (%s) registered for target type %s. This will override the previous entry (%s).",
					name, targetCondition.getClass().getName(), targetType.getName(),
					previousTargetCondition.getClass().getName());
		}
		map.put(name, targetCondition);
	}

	public static void registerCondition(String name, Condition<?> condition, String... requiredVariables) {
		Condition<?> previousCondition = CONDITIONS.get(name);
		if (previousCondition != null) {
			Log.REACT.warn("Duplicate condition %s (%s) registered. This will override the previous entry (%s).", name,
					condition.getClass().getName(), previousCondition.getClass().getName());
		}
		CONDITIONS.put(name, condition);
		for (String requiredVariable : requiredVariables) {
			CONDITIONS_REQUIRED_VARS.put(name, requiredVariable);
		}

	}

	public static <E extends Event> void registerVariable(Class<E> eventType, String name, Variable<?, E> variable) {
		Map<String, Variable<?, ?>> map = VARIABLES.get(eventType);
		if (map == null) {
			map = new HashMap<>();
			VARIABLES.put(eventType, map);
		}
		Variable<?, ?> previousVariable = map.get(name);
		if (previousVariable != null) {
			Log.REACT.warn(
					"Duplicate variables %s (%s) registered for event type %s. This will override the previous entry (%s).",
					name, variable.getClass().getName(), eventType.getName(), previousVariable.getClass().getName());
		}
		map.put(name, variable);
	}

	public static <V> void registerTargetProperty(String property, Class<V> targetType, TargetProperty<V> text) {
		Map<String, TargetProperty<?>> map = TARGET_PROPERTIES.get(targetType);
		if (map == null) {
			map = new HashMap<>();
			TARGET_PROPERTIES.put(targetType, map);
		}
		TargetProperty<?> previousVariable = map.get(property);
		if (previousVariable != null) {
			Log.REACT.warn(
					"Duplicate property %s (%s) registered for variable %s. This will override the previous entry (%s).",
					property, text.getClass().getName(), targetType.getName(), previousVariable.getClass().getName());
		}
		map.put(property, text);
	}
}
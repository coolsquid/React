package coolsquid.react.config;

import java.io.File;
import java.io.FilenameFilter;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigOrigin;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;

import coolsquid.react.api.event.Action;
import coolsquid.react.api.event.Variable;
import coolsquid.react.event.InternalEventManager;
import coolsquid.react.util.Log;
import coolsquid.react.util.WarningHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;

public class ConfigManager {

	public static final File CONFIG_DIRECTORY = new File(Loader.instance().getConfigDir(), "react");
	public static final File MOD_CONFIG_FILE = new File(CONFIG_DIRECTORY, "react.conf");
	public static final FilenameFilter CONFIG_FILE_FILTER = (file,
			name) -> (file.isDirectory() || name.endsWith(".conf")) && !file.equals(MOD_CONFIG_FILE);

	public static final String NEGATIVE_PREFIX = "-";

	public static boolean syncConfigs = true;
	public static boolean debug = false;

	private static int errorCount = 0;

	public static void load() {
		load(CONFIG_DIRECTORY, true);
	}

	public static int load(File configDir, boolean reset) {
		if (reset) {
			InternalEventManager.LISTENERS.clear();
		}
		if (!configDir.exists()) {
			configDir.mkdirs();
		}
		if (!configDir.isDirectory()) {
			Log.REACT.error("The React config directory is not a directory!");
			errorCount++;
			return errorCount;
		} else if (!configDir.canRead()) {
			Log.REACT.error("The React config directory is not readable!");
			errorCount++;
			return errorCount;
		}
		try {
			ConfigWrapper config = new ConfigWrapper(MOD_CONFIG_FILE);
			syncConfigs = config.getBoolean("sync", true);
			debug = config.getBoolean("debug", false);
			config.save();
			loadFile(configDir);
		} catch (Exception e) {
			Log.REACT.error("Error while loading configs");
			Log.REACT.catching(e);
			errorCount++;
			return errorCount;
		}
		if (errorCount > 0 && FMLCommonHandler.instance().getSide() == Side.CLIENT) {
			WarningHandler.registerWarning(errorCount);
		}
		InternalEventManager.setupEvents();
		return errorCount + (errorCount = 0);
	}

	public static void loadFile(File file) {
		if (file.isDirectory()) {
			for (File file2 : file.listFiles((f, n) -> n.endsWith(".conf") || f.isDirectory())) {
				loadFile(file2);
			}
		} else if (CONFIG_FILE_FILTER.accept(file, file.getName())) {
			Log.REACT.info("Loading scripts from file %s", file.getName());
			try {
				long time = System.nanoTime();
				int errorCount2 = errorCount;
				loadConfig(ConfigFactory.parseFile(file));
				if (errorCount2 == errorCount) {
					Log.REACT.info("Successfully loaded scripts from file %s. Loading took %s seconds.", file.getName(),
							NumberFormat.getNumberInstance(Locale.UK).format((System.nanoTime() - time) / 1000000000D));
				} else {
					Log.REACT.info("Loaded scripts from file %s with %s errors. Loading took %s seconds.",
							file.getName(), errorCount - errorCount2,
							NumberFormat.getNumberInstance(Locale.UK).format((System.nanoTime() - time) / 1000000000D));
				}
			} catch (ConfigException e) {
				Log.REACT.error("Exception while parsing script %s", file.getName());
				Log.REACT.catching(e);
				errorCount++;
				return;
			}
		}
	}

	public static void loadConfig(Config root) {
		if (root.hasPath("disable") && root.getBoolean("disable")) {
			return;
		}
		for (Entry<String, ConfigValue> e : root.entrySet()) {
			if (!InternalEventManager.LISTENER_TYPES.containsKey(e.getKey())
					&& e.getValue().valueType() == ConfigValueType.LIST) {
				addError(e.getValue().origin(), "No such event %s", e.getKey());
			}
		}
		for (Entry<String, Class<? extends Event>> eventType : InternalEventManager.LISTENER_TYPES.entrySet()) {
			if (root.hasPath(eventType.getKey())) {
				List<? extends Config> actions = root.getConfigList(eventType.getKey());
				for (int i = 0; i < actions.size(); i++) {
					Config action = actions.get(i);
					try {
						Action<?> type = getType(action, eventType);
						if (type == null) {
							continue;
						}
						String target = getTarget(action, eventType);
						if (target == null) {
							continue;
						}
						Map<String, Object> parameters = getParameters(action, eventType);
						Map<String, Object> conditions = getConditions(action, eventType);
						Map<String, Object> targetConditions = getTargetConditions(action, target, eventType);
						String actionName = action.getString("action");
						for (String requiredParameter : InternalEventManager.ACTIONS_REQUIRED_PARAMS.get(actionName)) {
							if (!parameters.containsKey(requiredParameter)) {
								addError(
										action.hasPath("parameters") ? action.getValue("parameters").origin()
												: action.origin(),
										"Missing required parameter %s for action %s", requiredParameter, actionName);
							}
						}
						if (errorCount == 0) {
							InternalEventManager.registerListener(eventType.getValue(), type, target, parameters,
									conditions, targetConditions);
						}
					} catch (Exception e) {
						Log.REACT.error("Exception while loading script %s section %s.%s:", root.origin().filename(),
								eventType.getKey(), i + 1);
						Log.REACT.catching(e);
						errorCount++;
					}
				}
			}
		}
	}

	private static Map<String, Object> getConditions(Config action, Entry<String, Class<? extends Event>> eventType) {
		if (!action.hasPath("conditions")) {
			return Collections.emptyMap();
		}
		Config conditions = action.getConfig("conditions");
		Map<String, Object> compiledConditions = new HashMap<>();
		l1: for (Entry<String, ConfigValue> condition : conditions.entrySet()) {
			String key = noQuotes(condition.getKey());
			String[] parts = key.split("\\.");
			for (int i = 0; i < parts.length; i++) {
				parts[i] = noQuotes(parts[i]);
			}
			key = String.join(".", parts);
			String baseKey = key;
			if (key.startsWith(NEGATIVE_PREFIX)) {
				key = key.substring(1);
			}
			if (parts.length > 1) {
				if (!InternalEventManager.TARGETS.containsKey(parts[0])) {
					addError(condition.getValue().origin(), "No such target: %s", parts[0]);
					continue l1;
				}
			} else if (!InternalEventManager.CONDITIONS.containsKey(key)) {
				addError(condition.getValue().origin(), "No such condition: %s", key);
				continue l1;
			}
			l2: for (String s : InternalEventManager.CONDITIONS_REQUIRED_VARS.get(key)) {
				for (Class<?> c = eventType.getValue(); c != Object.class; c = c.getSuperclass()) {
					Map<String, Variable<?, ?>> map = InternalEventManager.VARIABLES.get(c);
					if (map != null && map.containsKey(s)) {
						continue l2;
					}
				}
				addError(condition.getValue().origin(), "Event %s does not support condition %s", eventType.getKey(),
						key);
				continue l1;
			}
			compiledConditions.put(baseKey, condition.getValue().unwrapped());
		}
		return compiledConditions;
	}

	private static Map<String, Object> getParameters(Config action, Entry<String, Class<? extends Event>> eventType) {
		if (!action.hasPath("parameters")) {
			return Collections.emptyMap();
		}
		Config parameters = action.getConfig("parameters");
		Map<String, Object> compiledParameters = new HashMap<>();
		for (Entry<String, ConfigValue> parameter : parameters.root().entrySet()) {
			String key = noQuotes(parameter.getKey());
			if (parameter.getValue().valueType() == ConfigValueType.OBJECT) {
				compiledParameters.put(key, parameters.getConfig(key));
			} else {
				compiledParameters.put(key, parameter.getValue().unwrapped());
			}
		}
		return compiledParameters;
	}

	private static String getTarget(Config action, Entry<String, Class<? extends Event>> eventType) {
		if (!action.hasPath("target")) {
			return "";
		}
		ConfigValue target = action.getValue("target");
		if (!((String) target.unwrapped()).isEmpty() && !InternalEventManager.TARGETS.containsKey(target.unwrapped())) {
			addError(target.origin(), "No such target type for event %s: %s", eventType.getKey(), target.unwrapped());
			return null;
		}
		l1: for (String s : InternalEventManager.TARGETS_REQUIRED_VARS.get((String) target.unwrapped())) {
			for (Class<?> c = eventType.getValue(); c != Object.class; c = c.getSuperclass()) {
				Map<String, Variable<?, ?>> map = InternalEventManager.VARIABLES.get(c);
				if (map != null && map.containsKey(s)) {
					continue l1;
				}
			}
			addError(target.origin(), "Event %s does not support target %s", eventType.getKey(), target.unwrapped());
			return null;
		}
		return (String) target.unwrapped();
	}

	private static Map<String, Object> getTargetConditions(Config action, String target,
			Entry<String, Class<? extends Event>> eventType) {
		if (target.isEmpty() || !action.hasPath("target_filters")) {
			return Collections.emptyMap();
		}
		Config targetConditions = action.getConfig("target_filters");
		Map<String, Object> compiledTargetConditions = new HashMap<>();
		for (Entry<String, ConfigValue> targetCondition : targetConditions.entrySet()) {
			compiledTargetConditions.put(noQuotes(targetCondition.getKey()), targetCondition.getValue().unwrapped());
		}
		return compiledTargetConditions;
	}

	private static Action<?> getType(Config action, Entry<String, Class<? extends Event>> eventType) {
		ConfigValue type = action.getValue("action");
		Action<?> action2 = InternalEventManager.ACTIONS.get(type.unwrapped());
		if (action2 == null) {
			ConfigOrigin debug = type.origin();
			addError(debug, "No such action type: %s", type.unwrapped());
			return null;
		}
		return action2;
	}

	private static void addError(ConfigOrigin debug, String error, Object... args) {
		Object[] args2 = new Object[args.length + 2];
		args2[0] = debug.filename();
		args2[1] = debug.lineNumber();
		for (int i = 0; i < args.length; i++) {
			args2[i + 2] = args[i];
		}
		errorCount++;
		Log.REACT.error("Error in file %s at line %s:\n" + error, args2);
	}

	private static String noQuotes(String string) {
		if (string.indexOf('"') == 0 && string.lastIndexOf('"') == string.length() - 1) {
			return string.substring(1, string.length() - 1);
		} else {
			return string;
		}
	}
}
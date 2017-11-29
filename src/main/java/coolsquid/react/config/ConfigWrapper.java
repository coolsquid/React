package coolsquid.react.config;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import coolsquid.react.util.Log;

import org.apache.commons.io.FileUtils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigValueFactory;

public class ConfigWrapper {

	private File file;
	private final int originalHash;
	private Config config;
	private String path;
	private ConfigWrapper parent;

	public ConfigWrapper(File file) {
		this(file, ConfigFactory.parseFile(file));
	}

	public ConfigWrapper(File file, Config config) {
		this(file, config, null, null);
	}

	private ConfigWrapper(File file, Config config, ConfigWrapper parent, String path) {
		this.file = file;
		this.originalHash = config.hashCode();
		this.config = config;
		this.parent = parent;
		this.path = path;
	}

	public boolean getBoolean(String path, boolean defaultValue) {
		if (!this.config.hasPath(path)) {
			this.addValue(path, defaultValue);
			return defaultValue;
		}
		return this.config.getBoolean(path);
	}

	public Number getNumber(String path, Number defaultValue) {
		if (!this.config.hasPath(path)) {
			this.addValue(path, defaultValue);
			return defaultValue;
		}
		return this.config.getNumber(path);
	}

	public int getInt(String path, int defaultValue) {
		if (!this.config.hasPath(path)) {
			this.addValue(path, defaultValue);
			return defaultValue;
		}
		return this.config.getInt(path);
	}

	public long getLong(String path, long defaultValue) {
		if (!this.config.hasPath(path)) {
			this.addValue(path, defaultValue);
			return defaultValue;
		}
		return this.config.getLong(path);
	}

	public double getDouble(String path, double defaultValue) {
		if (!this.config.hasPath(path)) {
			this.addValue(path, defaultValue);
			return defaultValue;
		}
		return this.config.getDouble(path);
	}

	public String getString(String path, String defaultValue) {
		if (!this.config.hasPath(path)) {
			this.addValue(path, defaultValue);
			return defaultValue;
		}
		return this.config.getString(path);
	}

	public ConfigWrapper getConfig(String path) {
		if (!this.config.hasPath(path)) {
			Config config = ConfigFactory.empty();
			this.addValue(path, config.root());
			return new ConfigWrapper(null, config, this, path);
		}
		return new ConfigWrapper(null, this.config.getConfig(path), this, path);
	}

	public List<Boolean> getBooleanList(String path, List<Boolean> defaultValue) {
		if (!this.config.hasPath(path)) {
			this.addValue(path, defaultValue);
			return defaultValue;
		}
		return this.config.getBooleanList(path);
	}

	public List<Number> getNumberList(String path, List<Number> defaultValue) {
		if (!this.config.hasPath(path)) {
			this.addValue(path, defaultValue);
			return defaultValue;
		}
		return this.config.getNumberList(path);
	}

	public List<Integer> getIntList(String path, List<Integer> defaultValue) {
		if (!this.config.hasPath(path)) {
			this.addValue(path, defaultValue);
			return defaultValue;
		}
		return this.config.getIntList(path);
	}

	public List<Long> getLongList(String path, List<Long> defaultValue) {
		if (!this.config.hasPath(path)) {
			this.addValue(path, defaultValue);
			return defaultValue;
		}
		return this.config.getLongList(path);
	}

	public List<Double> getDoubleList(String path, List<Double> defaultValue) {
		if (!this.config.hasPath(path)) {
			this.addValue(path, defaultValue);
			return defaultValue;
		}
		return this.config.getDoubleList(path);
	}

	public List<String> getStringList(String path, List<String> defaultValue) {
		if (!this.config.hasPath(path)) {
			this.addValue(path, defaultValue);
			return defaultValue;
		}
		return this.config.getStringList(path);
	}

	public void save() {
		if (this.file == null) {
			throw new IllegalStateException();
		}
		if (this.originalHash != this.config.hashCode()) {
			try {
				FileUtils.write(this.file,
						this.config.root()
								.render(ConfigRenderOptions.defaults().setJson(false).setOriginComments(false)),
						StandardCharsets.UTF_8);
			} catch (IOException e) {
				Log.REACT.error("Could not write config to file");
				Log.REACT.catching(e);
			}
		}
	}

	private void addValue(String path, Object value) {
		this.config = this.config.withValue(path, ConfigValueFactory.fromAnyRef(value));
		this.updateParent();
	}

	private void updateParent() {
		if (this.parent != null) {
			this.parent.config = this.parent.config.withValue(this.path, this.config.root());
			this.parent.updateParent();
		}
	}
}
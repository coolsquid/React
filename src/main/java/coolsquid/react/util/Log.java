
package coolsquid.react.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log {

	private static final Map<String, Log> LOGS = new HashMap<>();
	public static final Log REACT = new Log("React", "logs/react", true, 0);

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
	private final String file;
	private final int numToRetain;
	private final Logger log4j;
	private final BufferedWriter log;

	public Log(String name, String filePath, boolean mainLog, int numToRetain) {
		this.file = filePath;
		this.numToRetain = numToRetain;
		if (mainLog) {
			this.log4j = LogManager.getLogger(name);
		} else {
			this.log4j = null;
		}
		BufferedWriter log = null;
		try {
			File file = new File(filePath + ".log");
			this.moveLog(file, 1);
			log = new BufferedWriter(new OutputStreamWriter(FileUtils.openOutputStream(file)));
		} catch (IOException e) {
			if (this.log4j == null) {
				LogManager.getLogger("React").catching(e);
			} else {
				this.log4j.catching(e);
			}
			log = null;
		}
		this.log = log;
		LOGS.put(name, this);
	}

	private void moveLog(File file, int num) {
		if (file.exists()) {
			if (num > this.numToRetain) {
				file.delete();
			} else {
				File newFile = new File(this.file + "-" + num + ".log");
				if (newFile.exists()) {
					this.moveLog(newFile, num + 1);
				}
				file.renameTo(newFile);
			}
		}
	}

	public void error(String message, Object... args) {
		this.log(true, Level.ERROR, message, args);
	}

	public void warn(String message, Object... args) {
		this.log(true, Level.WARN, message, args);
	}

	public void info(String message, Object... args) {
		this.log(true, Level.INFO, message, args);
	}

	public void debug(String message, Object... args) {
		this.log(true, Level.DEBUG, message, args);
	}

	public void catching(Throwable t) {
		if (this.log4j != null) {
			this.log4j.catching(t);
		}
		if (this.log != null) {
			t.printStackTrace(new PrintWriter(this.log));
			try {
				this.log.newLine();
				this.log.flush();
			} catch (IOException e) {
				this.log4j.catching(e);
			}
		}
	}

	public void log(boolean log4j, Level level, String message, Object... args) {
		String formattedMessage = args.length > 0 ? String.format(message, args) : message;
		if (log4j && this.log4j != null) {
			this.log4j.log(level, formattedMessage);
		}
		if (this.log != null) {
			try {
				this.log.write("[");
				this.log.write(DATE_FORMAT.format(new Date()));
				this.log.write("] [");
				this.log.write(level.name());
				this.log.write("]: ");
				this.log.write(formattedMessage);
				this.log.newLine();
				this.log.flush();
			} catch (IOException e) {
				this.log4j.catching(e);
			}
		}
	}

	public static Log getLog(String name) {
		return LOGS.get(name);
	}
}
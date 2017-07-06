package coolsquid.react.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
	private static final Logger LOG4J = LogManager.getLogger("React");
	private static final BufferedWriter LOG = open();

	private static BufferedWriter open() {
		try {
			return new BufferedWriter(new OutputStreamWriter(FileUtils.openOutputStream(new File("logs/react.log"))));
		} catch (IOException e) {
			LOG4J.catching(e);
			return null;
		}
	}

	public static void error(String message, Object... args) {
		log(Level.ERROR, message, args);
	}

	public static void warn(String message, Object... args) {
		log(Level.WARN, message, args);
	}

	public static void info(String message, Object... args) {
		log(Level.INFO, message, args);
	}

	public static void debug(String message, Object... args) {
		log(Level.DEBUG, message, args);
	}

	public static void catching(Throwable t) {
		LOG4J.catching(t);
		if (LOG != null) {
			t.printStackTrace(new PrintWriter(LOG));
			try {
				LOG.newLine();
				LOG.flush();
			} catch (IOException e) {
				LOG4J.catching(e);
			}
		}
	}

	public static void clear() {
		try {
			LOG.flush();
		} catch (IOException e) {

		}
	}

	private static void log(Level level, String message, Object[] args) {
		String formattedMessage = args.length > 0 ? String.format(message, args) : message;
		LOG4J.log(level, formattedMessage);
		if (LOG != null) {
			try {
				LOG.write("[");
				LOG.write(DATE_FORMAT.format(new Date()));
				LOG.write("] [");
				LOG.write(level.name());
				LOG.write("]: ");
				LOG.write(formattedMessage);
				LOG.newLine();
				LOG.flush();
			} catch (IOException e) {
				LOG4J.catching(e);
			}
		}
	}
}
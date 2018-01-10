
package coolsquid.react.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log {

	private static final Map<String, Log> LOGS = new HashMap<>();
	public static final Log REACT = new Log("React", "logs/react", true, 0);

	private static final long LOG_SIZE = 1024L * 1024L * 20L;

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
	private static final SimpleDateFormat COMPACT_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
	private static final SimpleDateFormat COMPACT_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
	private String filePath;
	private File file;
	private final int numToRetain;
	private final Logger log4j;
	private BufferedWriter log;

	private String lastCompactDate;
	private String lastCompactTime;

	private boolean inUse;

	public Log(String name, String filePath, boolean mainLog, int numToRetain) {
		this.filePath = filePath;
		this.numToRetain = numToRetain;
		if (mainLog) {
			this.log4j = LogManager.getLogger(name);
		} else {
			this.log4j = null;
		}
		this.initIO();
		LOGS.put(name, this);
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

	public void catching(Throwable t, String message, Object... args) {
		this.error(message, args);
		this.catching(t);
	}

	public void catching(Throwable t) {
		synchronized (this) {
			while (this.inUse) {
				try {
					this.wait();
				} catch (InterruptedException e) {

				}
			}
			this.inUse = true;
			if (this.log != null) {
				t.printStackTrace(new PrintWriter(this.log));
				try {
					this.log.newLine();
					this.log.flush();
				} catch (IOException e) {
					this.log4j.catching(e);
				}
			}
			this.inUse = false;
			this.notify();
		}
		if (this.log4j != null) {
			this.log4j.catching(t);
		}
	}

	public void log(boolean log4j, Level level, String message, Object... args) {
		String formattedMessage = args.length > 0 ? String.format(message, args) : message;
		if (log4j && this.log4j != null) {
			this.log4j.log(level, formattedMessage);
		}
		synchronized (this) {
			while (this.inUse) {
				try {
					this.wait();
				} catch (InterruptedException e) {

				}
			}
			this.inUse = true;
			this.updateFile();
			this.lastCompactDate = null;
			this.lastCompactTime = null;
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
			this.inUse = false;
			this.notify();
		}
	}

	public void logCompactly(String message, Object... args) {
		String formattedMessage = args.length > 0 ? String.format(message, args) : message;
		Date d = new Date();
		String date = COMPACT_DATE_FORMAT.format(d);
		String time = COMPACT_TIME_FORMAT.format(d);
		synchronized (this) {
			while (this.inUse) {
				try {
					this.wait();
				} catch (InterruptedException e) {

				}
			}
			this.inUse = true;
			this.updateFile();
			if (this.log != null) {
				try {
					if (this.lastCompactDate == null || !date.equals(this.lastCompactDate)) {
						this.log.write(date);
						this.log.write(":");
						this.log.newLine();
						this.lastCompactDate = date;
					}
					if (this.lastCompactTime == null || !time.equals(this.lastCompactTime)) {
						this.log.write(time);
						this.log.write(":");
						this.log.newLine();
						this.lastCompactTime = time;
					}
					this.log.write(formattedMessage);
					this.log.newLine();
					this.log.flush();
				} catch (IOException e) {
					this.log4j.catching(e);
				}
			}
			this.inUse = false;
			this.notify();
		}
	}

	private synchronized void updateFile() {
		if (this.file.length() >= LOG_SIZE) {
			this.initIO();
		}
	}

	private synchronized void initIO() {
		if (this.log != null) {
			try {
				this.log.close();
			} catch (IOException e) {
				if (this.log4j == null) {
					LogManager.getLogger("React").catching(e);
				} else {
					this.log4j.catching(e);
				}
			}
		}
		BufferedWriter log = null;
		File file = new File(this.filePath + ".log");
		this.file = file;
		try {
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
	}

	private synchronized void moveLog(File file, int num) {
		if (file.exists()) {
			if (num > this.numToRetain) {
				file.delete();
			} else {
				File newFile = new File(this.filePath + "-" + num + ".zip");
				if (newFile.exists()) {
					this.moveLog(newFile, num + 1);
				}
				if (file.getName().endsWith(".zip")) {
					file.renameTo(newFile);
				} else {
					try (InputStream in = FileUtils.openInputStream(file);
							ZipArchiveOutputStream out = new ZipArchiveOutputStream(newFile)) {
						out.putArchiveEntry(out.createArchiveEntry(file, file.getName()));
						IOUtils.copy(in, out);
						out.closeArchiveEntry();
					} catch (IOException e) {
						Log.REACT.catching(e);
						file.renameTo(newFile);
					}
				}
			}
		}
	}

	public static Log getLog(String name) {
		return LOGS.get(name);
	}
}
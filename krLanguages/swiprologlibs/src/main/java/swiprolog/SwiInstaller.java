package swiprolog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import org.jpl7.JPL;

import net.harawata.appdirs.AppDirsFactory;

/**
 * call init() once to install the libraries and prepare SWI for use.
 *
 * @author W.Pasman 1dec2014
 */
public final class SwiInstaller {
	private final static SupportedSystem system = SupportedSystem.getSystem();
	private static String override = null;
	private static File SwiPath;
	private static boolean initialized = false;
	private final static Logger logger = Logger.getLogger("KRLogger");

	/**
	 * This is a utility class. Just call init().
	 */
	private SwiInstaller() {
	}

	/**
	 * Overrides the installation directory. Useful if applicationdata can not be
	 * used by the application.
	 * 
	 * @param dir the new target directory.
	 */
	public static void overrideDirectory(String dir) {
		override = dir;
	}

	/**
	 * see {@link #init(boolean)} where boolean=false.
	 */
	public static void init() {
		init(false);
	}

	/**
	 * initialize SWI prolog for use. Unzips dlls and connects them to the system.
	 * This static function needs to be called once, to get SWI hooked up to the
	 * java system.
	 *
	 * This call will unzip required system dynamic link libraries to a temp folder,
	 * pre-load them, and set the paths such that SWI can find its files.
	 *
	 * The temp folder will be removed automatically if the JVM exits normally.
	 *
	 * @throws RuntimeException if initialization failed (see nested exception).
	 */
	public static synchronized void init(boolean force) {
		if (initialized && !force) {
			return;
		}

		unzipSWI(force);
		loadDependencies();

		JPL.setNativeLibraryDir(SwiPath.getAbsolutePath());
		
		// Don't Tell Me Mode needs to be false as it ensures that variables
		// with initial '_' are treated as regular variables.
		JPL.setDTMMode(false);
		// Let JPL know which SWI_HOME_DIR we're using; this negates the need
		// for a SWI_HOME_DIR environment var
		JPL.init(new String[] { "pl", "--home=" + SwiPath, "--quiet", "--nosignals", "--nodebug" });
		new org.jpl7.Query("set_prolog_flag(debug_on_error,false).").allSolutions();

		// Finished
		initialized = true;
	}

	public static void unzipSWI(boolean force) throws RuntimeException {
		File basedir;
		try {
			basedir = unzip(system + ".zip", force);
		} catch (URISyntaxException | IOException e) {
			throw new RuntimeException("failed to install SWI: ", e);
		}
		SwiPath = new File(basedir, system.toString());
	}

	/**
	 * Loads all dynamic load libraries for the selected system.
	 */
	private static void loadDependencies() {
		// Dirty system-dependent stuff...
		switch (system) {
		case linux:
			load("libswipl.so.8.0.3");
			load("libjpl.so");
			break;
		case mac:
			load("libreadline.8.dylib");
			load("libgmp.10.dylib");
			load("libswipl.dylib");
			load("libjpl.dylib");
			break;
		case win32:
			load("libwinpthread-1.dll");
			load("libgcc_s_sjlj-1.dll");
			load("libgmp-10.dll");
			load("libswipl.dll");
			load("jpl.dll");
			break;
		case win64:
			load("libwinpthread-1.dll");
			load("libgcc_s_seh-1.dll");
			load("libgmp-10.dll");
			load("libswipl.dll");
			load("jpl.dll");
			break;
		}
	}

	/**
	 * pre-loads a system dynamic library.
	 *
	 * @param libname
	 */
	private static void load(String libname) {
		System.load(new File(SwiPath, libname).getAbsolutePath());
	}

	/**
	 * Unzip a given zip file
	 *
	 * @param zipfilename
	 * @return temp directory where swi files are contained.
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws ZipException
	 */
	private static File unzip(String zipfilename, boolean force) throws URISyntaxException, ZipException, IOException {
		String appDataDir = AppDirsFactory.getInstance().getUserDataDir("swilibs", getVersion(), "GOAL");
		Path path = (override == null) ? Paths.get(appDataDir) : Paths.get(override);
		File base = path.toFile();
		if (base.exists()) {
			if (force) {
				deleteFolder(base);
			} else {
				return base;
			}
		}

		logger.log(Level.INFO, "unzipping SWI prolog libraries (" + zipfilename + ") to " + base);
		base.mkdir();

		InputStream fis = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("swiprolog/" + zipfilename);
		BufferedInputStream bis = new BufferedInputStream(fis);
		ZipInputStream zis = new ZipInputStream(bis);
		ZipEntry entry = null;
		byte[] buffer = new byte[2048];
		while ((entry = zis.getNextEntry()) != null) {
			File fileInDir = new File(base, entry.getName());
			if (entry.isDirectory()) {
				fileInDir.mkdirs();
			} else if (!fileInDir.canRead()) {
				FileOutputStream fOutput = new FileOutputStream(fileInDir);
				int count = 0;
				while ((count = zis.read(buffer)) > 0) {
					// write 'count' bytes to the file output stream
					fOutput.write(buffer, 0, count);
				}
				fOutput.close();
			}
			zis.closeEntry();
		}
		zis.close();
		fis.close();

		return base;
	}

	/**
	 * @return a unique number for the current source code, that changes when the
	 *         GOAL version changes. the maven version number of this SWI installer,
	 *         or the modification date of this class if no maven info is
	 *         available..
	 * @throws UnsupportedEncodingException
	 */
	private static String getVersion() throws UnsupportedEncodingException {
		String version = null;

		// try to load from maven properties first
		try {
			Properties p = new Properties();
			InputStream is = SwiInstaller.class.getResourceAsStream(
					"/META-INF/maven/org.bitbucket.goalhub.krTools.krLanguages/swiPrologEnabler/pom.properties");
			if (is != null) {
				p.load(is);
				version = p.getProperty("version", "");
			}
		} catch (Exception ignore) {
		}
		// fallback to using Java API
		if (version == null || version.isEmpty()) {
			String srcpath1 = SwiInstaller.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			String srcpath = URLDecoder.decode(srcpath1, "UTF-8");
			File srcfile = new File(srcpath);
			version = Long.toString(srcfile.lastModified());
		}

		return version;
	}

	private static void deleteFolder(File folder) {
		File[] files = folder.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					deleteFolder(f);
				} else {
					f.delete();
				}
			}
		}
		folder.delete();
	}
}
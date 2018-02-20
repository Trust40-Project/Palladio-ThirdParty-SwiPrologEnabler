package swiprolog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import org.jpl7.JPL;

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

	/**
	 * This is a utility class. Just call init().
	 */
	private SwiInstaller() {
	}

	public static void overrideDirectory(String dir) {
		override = dir;
	}

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
	 * @throws RuntimeException
	 *             if initialization failed (see nested exception).
	 */
	public static void init(boolean force) {
		if (initialized && !force) {
			return;
		}

		makeSwiPath(force);
		preLoadDependencies();

		try {
			addFolderToLibraryPath(SwiPath.getAbsolutePath());
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException("failed to install SWI: ", e);
		}

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

	private static void makeSwiPath(boolean force) throws RuntimeException {
		File basedir;
		try {
			basedir = unzipToTmp(system + ".zip", force);
		} catch (URISyntaxException | IOException e) {
			throw new RuntimeException("failed to install SWI: ", e);
		}
		SwiPath = new File(basedir, system.toString());
	}

	/**
	 * Pre-loads all dynamic load libraries for the selected system.
	 */
	private static void preLoadDependencies() {
		// Dirty system-dependent stuff...
		switch (system) {
		case linux:
			load("libswipl.so.7.6.4");
			load("libjpl.so");
			break;
		case mac:
			load("libncurses.6.dylib");
			load("libreadline.6.dylib");
			load("libgmp.10.dylib");
			load("libswipl.dylib");
			load("libjpl.dylib");
			break;
		case win32:
			load("libwinpthread-1.dll");
			load("libgcc_s_sjlj-1.dll");
			load("libdwarf.dll");
			load("libgmp-10.dll");
			load("libswipl.dll");
			load("jpl.dll");
			break;
		case win64:
			load("libwinpthread-1.dll");
			load("libgcc_s_seh-1.dll");
			load("libdwarf.dll");
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
	 * Adds given folder to java.library.path
	 *
	 * @param s
	 *            the path to be added (as string)
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	private static void addFolderToLibraryPath(final String s)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		final String path = s + File.pathSeparator + System.getProperty("java.library.path");
		System.setProperty("java.library.path", path);
		final Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
		fieldSysPath.setAccessible(true);
		fieldSysPath.set(null, null);
	}

	/**
	 * Unzip a given zip file to /tmp.
	 *
	 * @param zipfilename
	 * @return temp directory where swi files are contained.
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws ZipException
	 */
	private static File unzipToTmp(String zipfilename, boolean force)
			throws URISyntaxException, ZipException, IOException {
		Path path = (override == null) ? Paths.get(System.getProperty("java.io.tmpdir"), "swilibs" + getSourceNumber())
				: Paths.get(override);
		File base = path.toFile();
		if (base.exists()) {
			if (force) {
				deleteFolder(base);
			} else {
				return base;
			}
		}

		System.out.println("unzipping SWI prolog libraries (" + zipfilename + ") to " + base);
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
				// Assume directories are stored parents first then children.
				// System.err.println("Extracting dir: " + entry.getName());
				fileInDir.mkdir();
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
	 *         GOAL version changes. Actually this number is the modification date
	 *         of this class.
	 * @throws UnsupportedEncodingException
	 */
	private static long getSourceNumber() throws UnsupportedEncodingException {
		String srcpath1 = SwiInstaller.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String srcpath = URLDecoder.decode(srcpath1, "UTF-8");
		File srcfile = new File(srcpath);
		return srcfile.lastModified();
	}

	public static void deleteFolder(File folder) {
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
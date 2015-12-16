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

import jpl.JPL;

/**
 * call init() once to install the libraries and prepare SWI for use.
 *
 * @author W.Pasman 1dec2014
 */
public final class SwiInstaller {
	private static SupportedSystem system = SupportedSystem.getSystem();
	private static File SwiPath;
	private static boolean initialized = false;

	/**
	 * This is a utility class. Just call init() once.
	 */
	private SwiInstaller() {
	}

	/**
	 * initialize SWI prolog for use. Unzips dlls and connects them to the
	 * system. This static function needs to be called once, to get SWI hooked
	 * up to the java system.
	 *
	 * This call will unzip required system dynamic link libraries to a temp
	 * folder, pre-load them, and set the paths such that SWI can find its
	 * files.
	 *
	 * The temp folder will be removed automatically if the JVM exits normally.
	 *
	 * @throws IllegalStateException
	 *             , NoSuchFieldException, IllegalAccessException,
	 *             SecurityException if initialization failed. These are runtime
	 *             exceptions and therefore not declared.
	 */
	public synchronized static void init() {
		if (initialized) {
			return;
		}

		makeSwiPath();
		preLoadDependencies();
		try {
			addFolderToLibraryPath(SwiPath.getAbsolutePath());
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new IllegalStateException("Failed to initialize SWI Prolog",
					e);
		}

		/*
		 * Let JPL know which SWI_HOME_DIR we're using; this negates the need
		 * for a SWI_HOME_DIR environment var
		 */
		JPL.setDefaultInitArgs(new String[] { "pl", "--home=" + SwiPath,
				"--quiet", "--nosignals" });

		initialized = true;
	}

	private static void makeSwiPath() throws RuntimeException {
		File basedir;
		try {
			basedir = unzipToTmp(system + ".zip");
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
			load("libncurses.so.5");
			load("libreadline.so.6");
			load("libswipl.so.6.0.2");
			load("libjpl.so");
			load("libforeign.so");
			break;
		case mac:
			load("libncurses.5.4.dylib");
			load("libreadline.6.1.dylib");
			load("libswipl.dylib");
			load("libjpl.dylib");
			load("libforeign.jnilib");
			break;
		case win32:
			load("pthreadVC.dll");
			load("swipl.dll");
			load("jpl.dll");
			load("foreign.dll");
			break;
		case win64:
			load("pthreadVC2.dll");
			load("swipl.dll");
			load("jpl.dll");
			load("foreign.dll");
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
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	private static void addFolderToLibraryPath(final String s)
			throws NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException {
		final Field field = ClassLoader.class.getDeclaredField("usr_paths");
		field.setAccessible(true);
		final String[] paths = (String[]) field.get(null);
		for (final String path : paths) {
			if (s.equalsIgnoreCase(path)) {
				return;
			}
		}
		final String[] tmp = new String[paths.length + 1];
		System.arraycopy(paths, 0, tmp, 0, paths.length);
		tmp[paths.length] = s;
		field.set(null, tmp);
		final String path = s + File.pathSeparator
				+ System.getProperty("java.library.path");
		System.setProperty("java.library.path", path);
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
	private static File unzipToTmp(String zipfilename)
			throws URISyntaxException, ZipException, IOException {
		String tmpdir = System.getProperty("java.io.tmpdir");
		Path path = Paths.get(tmpdir, "swilibs" + getSourceNumber());
		File base = path.toFile();

		if (base.exists()) {
			return base;
		}

		if (!base.mkdir()) {
			throw new IOException("Can't create tmp directory for SWI at "
					+ base);
		}

		System.out.println("unzipping SWI prolog libraries (" + zipfilename
				+ ") to " + base);

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
			} else {
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
	 * @return a unique number for the current source code, that changes when
	 *         the GOAL version changes. Actually this number is the
	 *         modification date of this class.
	 * @throws UnsupportedEncodingException
	 */
	private static long getSourceNumber() throws UnsupportedEncodingException {
		String srcpath1 = SwiInstaller.class.getProtectionDomain()
				.getCodeSource().getLocation().getPath();
		String srcpath = URLDecoder.decode(srcpath1, "UTF-8");
		File srcfile = new File(srcpath);
		return srcfile.lastModified();
	}

}
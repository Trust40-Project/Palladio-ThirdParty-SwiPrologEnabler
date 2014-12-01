package swiprolog;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import jpl.JPL;

/**
 * 
 * call init() once to install the libraries and prepare SWI for use.
 * 
 * @author W.Pasman 1dec2014
 *
 */
public final class SwiInstaller {

	static SupportedSystem system = SupportedSystem.getSystem();
	static File SwiPath;
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
	 */
	public synchronized static void init() {
		if (initialized) {
			return;
		}

		makeSwiPath();
		preLoadDependencies();
		try {
			addFolderToLibraryPath(SwiPath.getAbsolutePath());
		} catch (NoSuchFieldException | SecurityException
				| IllegalArgumentException | IllegalAccessException e) {
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

		File base = File.createTempFile("swilibs",
				Long.toString(System.currentTimeMillis()));
		if (!base.delete()) {
			throw new IOException("Could not delete temp file: " + base);
		}
		if (!base.mkdir()) {
			throw new IOException("Can't create tmp directory for SWI at "
					+ base);
		}
		deleteOnExit(base);

		System.out.println("unzipping SWI prolog libraries to " + base);

		URL sourceDirUrl = ClassLoader.getSystemResource("swiprolog/lib/"
				+ zipfilename);
		File sourceFile = new File(sourceDirUrl.toURI());
		ZipFile zipFile = new ZipFile(sourceFile);

		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			File fileInDir = new File(base, entry.getName());

			if (entry.isDirectory()) {
				// Assume directories are stored parents first then children.
				// System.err.println("Extracting dir: " + entry.getName());
				fileInDir.mkdir();
			} else {
				// System.err.println("Extracting file: " + entry.getName());
				copyInputStream(zipFile.getInputStream(entry),
						new BufferedOutputStream(
								new FileOutputStream(fileInDir)));
			}

			deleteOnExit(fileInDir);
		}
		zipFile.close();

		return base;
	}

	/**
	 * Central deleteOnExit, convenient for debugging.
	 */
	private static void deleteOnExit(File file) {
		file.deleteOnExit();
	}

	/**
	 * Copy all bytes from inputstream to output stream.
	 * 
	 * @param in
	 *            the {@link InputStream}
	 * @param out
	 *            the {@link OutputStream}
	 * @throws IOException
	 */
	private static final void copyInputStream(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[1024];
		int len;
		while ((len = in.read(buffer)) >= 0)
			out.write(buffer, 0, len);
		in.close();
		out.close();
	}

}
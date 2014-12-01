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
 * call init() to install the libraries and prepare SWI for use.
 * 
 * @author W.Pasman 1dec2014
 *
 */
public final class SwiInstaller {
	public final static String JARpath = "";
	public final static String SWIpath = "";

	static {
		String JARpathT = "";
		// final Bundle bundle = Activator.getDefault().getBundle();
		// final URL found1 = FileLocator.find(bundle, new Path("lib"), null);
		// final String found2 = FileLocator.resolve(found1).toString()
		// .replaceAll("\\s", "%20");
		// final URI found3 = new URI("aap");
		// JARpathT = new File(found3).getPath();
		// JARpath = JARpathT;
		// String SWIpathT = JARpath + File.separator;
		// final String os = System.getProperty("os.name").toLowerCase(); //
		// System-dependent
		// if (os.contains("win")) {
		// if (System.getProperty("os.version").contains("64")) {
		// SWIpathT += "win64";
		// } else {
		// SWIpathT += "win32";
		// }
		// } else if (os.contains("mac")) {
		// SWIpathT += "mac";
		// } else {
		// SWIpathT += "linux";
		// }
		// SWIpath = SWIpathT;
	}

	public static void initializePrologLink() throws Exception {
		// Dirty system-dependent stuff...
		addFolderToLibraryPath(SWIpath);
		if (SWIpath.endsWith("linux")) {
			System.load(SWIpath + File.separator + "libncurses.so.5");
			System.load(SWIpath + File.separator + "libreadline.so.6");
			System.load(SWIpath + File.separator + "libswipl.so.6.0.2");
			System.load(SWIpath + File.separator + "libjpl.so");
			System.load(SWIpath + File.separator + "libforeign.so");
		} else if (SWIpath.endsWith("mac")) {
			System.load(SWIpath + File.separator + "libncurses.5.4.dylib");
			System.load(SWIpath + File.separator + "libreadline.6.1.dylib");
			System.load(SWIpath + File.separator + "libswipl.dylib");
			System.load(SWIpath + File.separator + "libjpl.dylib");
			System.load(SWIpath + File.separator + "libforeign.jnilib");
		} else if (SWIpath.endsWith("win32")) {
			System.load(SWIpath + File.separator + "pthreadVC.dll");
			System.load(SWIpath + File.separator + "swipl.dll");
			System.load(SWIpath + File.separator + "jpl.dll");
			System.load(SWIpath + File.separator + "foreign.dll");
		} else if (SWIpath.endsWith("win64")) {
			System.load(SWIpath + File.separator + "pthreadVC2.dll");
			System.load(SWIpath + File.separator + "swipl.dll");
			System.load(SWIpath + File.separator + "jpl.dll");
			System.load(SWIpath + File.separator + "foreign.dll");
		}
		JPL.setDefaultInitArgs(new String[] {
				// Let JPL know which SWI_HOME_DIR we're using;
				// this negates the need for a SWI_HOME_DIR environment var
				"pl", "--home=" + SWIpath, "--quiet", "--nosignals" });
	}

	private static void addFolderToLibraryPath(final String s) throws Exception {
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
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws ZipException
	 */
	private static void unzipToTmp(String zipfilename)
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
		base.deleteOnExit();

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

			fileInDir.deleteOnExit();
		}
		zipFile.close();
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
	public static final void copyInputStream(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[1024];
		int len;
		while ((len = in.read(buffer)) >= 0)
			out.write(buffer, 0, len);
		in.close();
		out.close();
	}

	public static void main(String[] args) throws ZipException,
			URISyntaxException, IOException {
		unzipToTmp("mac.zip");
	}

}
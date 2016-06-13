package swiprolog;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Contains results of a check if any path (file/directory) changed. Checks
 * recursively. The check is done the moment this object is created. immutable
 * object.
 * 
 * * A path is considered changed if
 * <ul>
 * <li>the path does not exist anymore (it has been deleted)
 * <li>the difference between creation and modification time is > T
 * <li>it contains any children (files/directories) that are considered changed
 * </ul>
 * 
 * @author W.Pasman
 *
 */
class ChangeCheck implements FileVisitor<Path> {
	/**
	 * T (milliseconds) determines maximum difference in start and change time
	 * of files. Should be set such that all files in the directory can be
	 * created in all cases within this time.
	 */

	private final static int T = 60000;
	private Path changedFile = null;

	/**
	 * @param path
	 *            the start path for the check
	 * @throws IOException
	 */
	ChangeCheck(Path path) throws IOException {
		Files.walkFileTree(path, this);
	}

	/**
	 * @return a (not specified which) file that was changed, or null if no file
	 *         has changed.
	 * @throws IOException
	 */
	public Path getFile() {
		return changedFile;
	}

	/************* implements FileVisitor ***************/
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
			throws IOException {
		return check(file);
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc)
			throws IOException {
		changedFile = file;
		return FileVisitResult.TERMINATE;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
			throws IOException {
		return check(dir);
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc)
			throws IOException {
		return FileVisitResult.CONTINUE;
	}

	/************** SUPPORT FUNCS *********************/

	private FileVisitResult check(Path path) throws IOException {
		if (isFileChanged(path)) {
			changedFile = path;
			return FileVisitResult.TERMINATE;
		}
		return FileVisitResult.CONTINUE;
	}

	/**
	 * * A file/directory is considered changed if
	 * <ul>
	 * <li>the file does not exist anymore (it has been deleted)
	 * <li>the difference between creation and modification time is > T
	 * 
	 * @param file
	 *            path to some file or directory .
	 * @return true iff the file changed
	 * @throws IOException
	 */
	private boolean isFileChanged(Path file) throws IOException {
		if (!Files.exists(file)) {
			return true;
		}
		BasicFileAttributes attri = Files.readAttributes(file,
				BasicFileAttributes.class);
		if (attri.lastModifiedTime().toMillis()
				- attri.creationTime().toMillis() > T) {
			return true;
		}
		return false;
	}

}
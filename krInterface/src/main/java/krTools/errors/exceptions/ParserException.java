/**
 * Knowledge Representation Tools. Copyright (C) 2014 Koen Hindriks.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package krTools.errors.exceptions;

import java.io.File;

import krTools.parser.SourceInfo;

/**
 * Exception to catch exceptions thrown by a parser.
 * 
 * <p>A parser exception should be thrown in case anything went wrong during initialization
 * of the parser, e.g., due to a problem with the source, or if parsing was interrupted for
 * some reason.
 * </p>
 */
public class ParserException extends Exception implements SourceInfo {

	private static final long serialVersionUID = 8224464835000074458L;

	/**
	 * The source of this error. Will not be printed when null.
	 */
	private SourceInfo info = null;

	/**
	 * Creates a new {@link ParserException} using the message provided.
	 * 
	 * @param msg Informative message about the exception.
	 */
	public ParserException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new {@link ParserException} using the message, input stream
	 * position, and source that are provided.
	 * 
	 * @param msg Informative message about the exception.
	 * @param info The source that was being parsed.
	 */
	public ParserException(String msg, SourceInfo info) {
		super(msg);
		this.info = info;
	}
	
	/**
	 * Creates a new {@link ParserException} using the message and throwable.
	 * 
	 * @param msg Informative message about the exception.
	 * @param e cause.
	 */
	public ParserException(String msg, Throwable e) {
		super(msg, e);
	}
	
	/**
	 * @return {@code true} if source info is available, {@code false} otherwise.
	 */
	public boolean hasSourceInfo() {
		return info != null;
	}

	/**
	 * @return The source of this exception, or {@code null} if no source is available.
	 */
	public File getSource() {
		if (hasSourceInfo()) {
			return info.getSource();
		} else {
			return null;
		}
	}

	/**
	 * @return Line number where exception occurred, or {@code null} if no line nr is available.
	 */
	@Override
	public int getLineNumber() {
		if (hasSourceInfo()) {
			return info.getLineNumber();
		} else {
			return -1;
		}
	}

	/**
	 * @return Character position where exception occurred, or {@code null} if no position is available.
	 */
	@Override
	public int getCharacterPosition() {
		if (hasSourceInfo()) {
			return info.getCharacterPosition();
		} else {
			return -1;
		}
	}

}

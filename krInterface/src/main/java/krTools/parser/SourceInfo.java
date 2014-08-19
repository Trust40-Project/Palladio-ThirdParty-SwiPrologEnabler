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

package krTools.parser;

import java.io.File;

/**
 * Information about a specific position in the source. Provides the position
 * in terms of a line and character position number.
 */
public interface SourceInfo {
	
	/**
	 * @return The source that this info is associated with.
	 * 
	 * TODO: change File to Reader?
	 */
	public File getSource();

	/**
	 * @return The line number of the line that this info is about.
	 */
	public int getLineNumber();

	/**
	 * @return The index of the character in the line that this info is about.
	 */
	public int getCharacterPosition();
	/**
	 * @return An info message.
	 */
	public String getMessage();

}

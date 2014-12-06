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

/**
 * Generic exception for throwing interface exceptions.
 */
public class KRException extends Exception {

	private static final long serialVersionUID = 3963559073032366579L;

	/**
	 * Creates ...
	 *
	 * @param message
	 */
	public KRException(String message) {
		super(message);
	}

	/**
	 * Creates ...
	 *
	 * @param message
	 * @param cause
	 */
	public KRException(String message, Throwable cause) {
		super(message, cause);
	}

	@Override
	public String toString() {
		return "<" + getClass().getSimpleName() + ": " + getMessage() + ", "
				+ getCause() + ">";
	}

}

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

import krTools.KRInterface;

/**
 * A KR initialization exception should be thrown if the initialization of
 * the interface failed for some reason. See {@link KRInterface#initialize()}.
 */
public class KRInitFailedException extends KRException {

	private static final long serialVersionUID = 4881505555016007638L;

	/**
	 * Creates ...
	 * 
	 * @param message
	 */
	public KRInitFailedException(String message) {
		super(message);
		assert message != null;
	}

	/**
	 * Creates ...
	 * 
	 * @param message
	 * @param cause
	 */
	public KRInitFailedException(String message, Throwable cause) {
		super(message,cause);
		assert message != null;
	}

}

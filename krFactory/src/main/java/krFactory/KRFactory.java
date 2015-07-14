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

package krFactory;

import java.util.HashMap;
import java.util.Map;

import krTools.KRInterface;
import krTools.exceptions.KRInitFailedException;
import krTools.exceptions.KRInterfaceNotSupportedException;
import owlrepo.OWLRepoKRInterface;
import swiprolog.SWIPrologInterface;

/**
 * Factory of KRIs. Currently, the factory supports:
 * <ul>
 * <li>SWI Prolog v6.0.2</li>
 * <li> OWL - API v. 4.0 </li>
 * </ul>
 */
public class KRFactory {
	// The names of the supported KRIs
	public static String SWI_PROLOG = "SWI Prolog";
	public static String OWL_REPO = "OWL Repo";

	/**
	 * A map of names to the {@link KRInterface}s that are supported.
	 */
	private static Map<String, Class<? extends KRInterface>> kr = new HashMap<>();
	static {
		kr.put(SWI_PROLOG,SWIPrologInterface.class);
		kr.put(OWL_REPO,OWLRepoKRInterface.class);
	}

	/**
	 * Utility class; constructor is hidden.
	 */
	private KRFactory() {
	}

	/**
	 * Provides an available knowledge representation
	 * technology. See the public static strings of this class for the supported KRTs.
	 *
	 * @param name
	 *            The name of the KRT. 
	 * @return A KR interface implementation.
	 * @throws KRInterfaceNotSupportedException
	 *             If the name does not map to a known interface.
	 * @throws KRInitFailedException
	 * 			   If the creation of the requested implementation failed.
	 */
	public static KRInterface getKR(String name)
			throws KRInterfaceNotSupportedException, KRInitFailedException {
		try
		{
			KRInterface krInterface = kr.containsKey(name) ? kr.get(name).newInstance() : null;
			if (krInterface == null) {
				throw new KRInterfaceNotSupportedException(
						"Could not find KRT " + name
								+ " as these are available: "
								+ kr.keySet());
			} else {
				return krInterface;
			}
		} catch( IllegalAccessException | InstantiationException e ){
			throw new KRInitFailedException("Failed to initialize " + name, e);
		}
	}
	
	public static String getName(KRInterface kri){
		for( final String name : kr.keySet()){
			final Class<? extends KRInterface> defined = kr.get(name);
			if(defined.equals(kri)){
				return name;
			}
		}
		return null;
	}
}
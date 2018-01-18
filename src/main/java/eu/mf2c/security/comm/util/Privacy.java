/**
 Copyright 2018 UKRI Science and Technology Facilities Council

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License 
 */
package eu.mf2c.security.comm.util;

/**
 * An {@link java.lang.Enum Enum <em>Enum<em>} of GDPR &#40;General Data Protection Regulation&#41; 
 * related data privacy categories. Based on a prototype developed by Cheney Ketley. 
 * <p> 
 * @author Shirley Crompton
 * @email  shirley.crompton@stfc.ac.uk
 * @org Data Science and Technology Group,
 *      UKRI Science and Technology Council
 * @Created 9 Jan 2018
 *
 */
public enum Privacy {
	
	//:TODO the categories need to be realigned as there are overlaps.
	//Privacy is not in scope for IT1
	
	/** GDRP applicable to data, message content needs to be processed according to GDPR*/
	GDPR,
	/** GDPR not applicable to data */
	NOT_GDPR,
	/** Data not personal sensitive */
	NOTPII;
	/** Data privacy requirement unknown
	UNKNOWN; */

}

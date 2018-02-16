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
 * An {@link java.lang.Enum Enum <em>Enum<em>} of message delivery quality of 
 * service.
 * <ul>
 * <li>ATMOSTONCE &#58; message delivered at most once.</li>
 * <li>ATMOSTONCE &#58; message delivered at least once.</li>
 * <li>ATMOSTONCE &#58; message delivered exactly once.</li>
 * </ul>
 * <p>
 * @author Shirley Crompton
 * @email  shirley.crompton@stfc.ac.uk
 * @org Data Science and Technology Group,
 *      UKRI Science and Technology Council
 * @Created 9 Jan 2018
 *
 */
public enum QoS {
	
	/** Message to be sent at most once. Use this flag for asynchronous communication.  
	 * Messages will not be stored nor re-sent.  Not acknowledged by recipients */	
	ATMOSTONCE,
	/** Message to be sent at least once, but may be duplicated */
    ATLEASTONCE,
    /** Message to be sent exactly once.  Most overhead.*/
    EXACTLYONCE;
    /** QoS not defined 
    UNDEFINED ;*/
}

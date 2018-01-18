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
package eu.mf2c.security.comm.protocol;

import eu.mf2c.security.comm.util.Protocol;

/**
 * 
 * Abstract class for handling different messaging
 * protocols.
 * <p>
 * @author Shirley Crompton
 * @email  shirley.crompton@stfc.ac.uk
 * @org Data Science and Technology Group,
 *      UKRI Science and Technology Council
 * @Created 16 Jan 2018
 *
 */
public abstract class ProtocolHandler {
	
	//external services such as MQTT broker, remote key issuance service need to be discovered through NDP protocol

	/** {@link Protocol <em>Protocol</em>} attribute */
	private Protocol protocol;
	/** Destination of the communication channel */
	protected String destination;
	
	/**
	 * Getter for {@link Protocol <em>Protocol</em>} type handled 
	 */
	public Protocol getProtocol(){
		return this.protocol;
	}
	
	
	
	
	
	
	
}
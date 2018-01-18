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
import eu.mf2c.security.exception.ProtocolHandlerException;

/**
 * A Factory class to return the correct instance of {@link ProtocolHandler <em>ProtocolHandler</em>}
 * at run time.
 * <p>
 * @author Shirley Crompton
 * @email  shirley.crompton@stfc.ac.uk
 * @org Data Science and Technology Group,
 *      UKRI Science and Technology Council
 * @Created 18 Jan 2018
 *
 */
public class ProtocolHandlerFactory {
	/**
	 * @param protocol	The {@link Protocol <em>Protocol</em>} requirement for determining which
	 *                  concrete {@link ProtocolHandler <em>ProtocolHandler</em>} subclass to instantiate.
	 * @return 			The appropriate instance of {@link ProtocolHandler <em>ProtocolHandler</em>}
	 * @throws ProtocolHandlerException		If the protocol is not supported.
	 */
	public static ProtocolHandler getProtocolHandler(Protocol protocol) throws ProtocolHandlerException{		
		//determine which type of handler to instantiate		
		try{
			switch(protocol){
				case BLE: return new BLEHandler();
				case MQTT: return new MQTTHandler();
				case HTTP: return new HTTPHandler();
				//18Jan2018 only supports BLE, MQTT and HTTP in iteration 1
				default: throw new ProtocolHandlerException("Unsupported protocol: " + protocol);  	
				//ideally we should use a separate ProtocolHandlerFactoryException, but we want to have a compact application.
			}
		}catch(Exception ex){
			//delegate error logging to the caller
			throw new ProtocolHandlerException("Error instantiating protocolHandler for " + protocol);
		}
	}
}

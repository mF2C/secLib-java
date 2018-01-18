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
package eu.mf2c.security.data;

import org.apache.log4j.Logger;

import eu.mf2c.security.comm.Channel;

/**
 * Identity of the mF2C agent or client application associated with the {@link Channel <em>Channel</em>} instance.
 *   
 * <p>  
 * @author Shirley Crompton
 * @email  shirley.crompton@stfc.ac.uk
 * @org Data Science and Technology Group,
 *      UKRI Science and Technology Council
 * @Created 9 Jan 2018
 *
 */
public class Identity {
	//??? do we need it to be serializable? the getInstance approach needs to be amended to guarantee threadsafe....
	/** Message logger attribute */
	private final static Logger LOGGER = Logger
			.getLogger(Identity.class.getName());
	/** Device Id attribute */
	private byte deviceId; //32 bits
	
	
	
	/**
	 * Private constructor.
	 */
	private Identity(){
		LOGGER.debug("Creating an instance.");
		//TBD
		//get DeviceId (compute or get from cert)
		//bootstrap id (read from environment)
	}
	/**
	 * Inner helper class to create the singleton instance of
	 * {@link eu.mf2c.security.data.Identity <em>Identity </em>}
	 *<p>
	 * @author Shirley Crompton
	 * @email  shirley.crompton@stfc.ac.uk
	 * @org Data Science and Technology Group,
	 *      UKRI Science and Technology Council
	 * @Created 12 Jan 2018
	 *
	 */
	private static class SingletonHelper{
		//lazy loading, guarantee single INSTANCE per class loader
		private static final Identity INSTANCE = new Identity();
	}
	
	
	/** 
	 * Get an instance 
	 * <p>
	 * return 	An {@link @Identity <em>Identity</em>} instance	 * 
	 * 
	 * */
	public static Identity getInstance(){
		LOGGER.debug("About to return an instance.");
		return SingletonHelper.INSTANCE;
	}
	/** 
	 * Getter for the {@link #deviceId} attribute
	 * @return    the {@link #deviceId} attribute
	 * */
	public byte getDeviceId(){
		//TODO need to clarify how DeviceId is represented....
		
		return this.deviceId;
	}
}

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
package eu.mf2c.security.comm;

import java.util.Set;

import eu.mf2c.security.data.Message;
import eu.mf2c.security.exception.ChannelException;

/**
 * mF2C secure communication API for establishing secure
 * control and data communication via various communication
 * protocols &#40;Iteration 1 suports MQTT, HTTP, BLE only.&#41;  
 * <p>
 * @author Shirley Crompton
 * @email  shirley.crompton@stfc.ac.uk
 * @org Data Science and Technology Group,
 *      UKRI Science and Technology Council
 * @Created 9 Jan 2018
 *
 */
public interface Channelable {
	/** current version of mF2C security libarary */
	public String version = "0.1.0-alpha"; //increment this for each release
	
	/**
	 * Send a message
	 * <p>
	 * @param message	The {@link Message <em>Message</em>} object
	 * @param flagHM	A {@link java.util.Set <em>Set</em>} of  
	 * 						{@link java.lang.Enum <em>Enum</em>} flags specifying
	 * 						the security, privacy and quality of service requirements  
	 * @throws ChannelException	on processing errors
	 */
	public void send(Message message, Set<Enum<?>> flags) throws ChannelException;
	
	/** flush the message buffers */
	public void flush();
	
	/** 
	 * terminate channel and tidy up, this includes
	 * flushes all buffers, closes all live connections 
	 * gracefully. 
	 */
	public void destruct();	
	/**
	 * Poll whether a message is available.  This is a non-blocking operation.
	 * <p>
	 * @return true if there is a message, else false.
	 */
	public boolean poll();
	
	/**
	 * Pops a message off the message queue.  This is a non-blocking mehod.
	 * <p>
	 * @return  a {@link ReceivedMessage <em>ReceivedMessage</em>} object containg 
	 * 			the sender channel&#39;friendlyname, flags, sender#95;id, the message
	 * 			content plus the received and sent timestamps.  A null or empty string
	 * 			is returned if there is no messages, dependent on behaviour of the 
	 * 			specific protocol.
	 */
	public Message pop();

	//Channelable is implemented by Channel, user can instantiate the interface to use its methods??? 
	//or the concrete Channel 
	//we need a separate interface for the user to registered as an Observer
	
}

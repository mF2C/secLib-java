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

import java.util.List;

import eu.mf2c.security.data.Message;
import eu.mf2c.security.data.ReceivedMessage;

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
	
	//variables in Channel class as we need them to be protected
	
	/** send a message */
	public void send(Message message, List<Enum> flags);
	
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
	public ReceivedMessage pop();

	//Channelable is implemented by Channel, user can instantiate the interface to use its methods??? 
	//or the concrete Channel 
	//we need a separate interface for the user to registered as an Observer
	
}

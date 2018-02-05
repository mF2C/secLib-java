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

import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

import eu.mf2c.security.comm.Receiver;
import eu.mf2c.security.comm.util.Protocol;
import eu.mf2c.security.data.Message;
import eu.mf2c.security.data.ReceivedMessage;

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
	//
	//external services such as MQTT broker, remote key issuance service need to be discovered through NDP protocol

	/** Destination of the communication channel */
	protected String destination;
	/** {@link Protocol <em>Protocol</em>} attribute */
	private Protocol protocol;
	/** Connection status flag */
	private boolean connack = false; 
	/** The ping acknowledgement queue attribute */ 
	protected ConcurrentLinkedQueue<ReceivedMessage> pingAckQ = new ConcurrentLinkedQueue<ReceivedMessage>(); //may need to block until something is in the buffer
	/** The ping request queue attribute */
	protected ConcurrentLinkedQueue<ReceivedMessage> pingReqQ = new ConcurrentLinkedQueue<ReceivedMessage>(); //may need to block until something is in the buffer
	/** The incoming message queue attribute */
	protected ConcurrentLinkedQueue<ReceivedMessage> msgQ = new ConcurrentLinkedQueue<ReceivedMessage>(); //may need to block until something is in the buffer
    //also need to check the removeAll operation is threadsafe
	/** Buffer for outgoing messages attribute  */
	protected ConcurrentLinkedQueue<Message> outMsgBuffer = new ConcurrentLinkedQueue<Message>();
	
	
	
	
	/**
	 * @return the {@link #connack <em>connack</em>} attribute 
	 */
	public boolean isConnack() {
		return connack;
	}

	/**
	 * Getter for {@link Protocol <em>Protocol</em>} type handled 
	 */
	public Protocol getProtocol(){
		return protocol;
	}
	
	private void setup(Properties properties, Receiver receiver){
		//to be defined by the concrete classes		
	}
	
	
	
	
	
	
}

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

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Signature;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.crypto.Cipher;

import org.apache.log4j.Logger;

import eu.mf2c.security.comm.Receiver;
import eu.mf2c.security.comm.util.Privacy;
import eu.mf2c.security.comm.util.Protocol;
import eu.mf2c.security.comm.util.QoS;
import eu.mf2c.security.comm.util.Security;
import eu.mf2c.security.data.Identity;
import eu.mf2c.security.data.Message;
import eu.mf2c.security.exception.IdentityException;
import eu.mf2c.security.exception.ProtocolHandlerException;

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
	/** using an abstract class as we need instance variables **/
	/** message logger */
	private static Logger LOGGER = Logger.getLogger(ProtocolHandler.class.getName());
	//
	/** friendy name of this Channel*/
	protected static String friendyName;
	//external services such as MQTT broker, remote key issuance service need to be discovered through NDP protocol
	/** broker address */ //this should be discovered in the bootstrapping process	
	protected String broker;
	/** Destination &#40;recipient&#41; of the communication channel */
	protected static String destination;	
	/** Target recipient&#39;s public key, it is obtained in the initial handshade operation*/
	protected PublicKey destPK = null;
	
	/** 
	 * Keepalive interval, the maximum number of seconds allowed between communications
	 * with the broker.  If no other messages are sent, the client will send a ping request
	 * at this interval 
	 * */
	protected int keepAlive;
	/** The ping acknowledgement queue attribute */ 
	protected ConcurrentLinkedQueue<Message> pingAckQ = new ConcurrentLinkedQueue<Message>(); //may need to block until something is in the buffer
	/** The ping request queue attribute */
	protected ConcurrentLinkedQueue<Message> pingReqQ = new ConcurrentLinkedQueue<Message>(); //may need to block until something is in the buffer
	/** The incoming message queue attribute */
	protected ConcurrentLinkedQueue<Message> msgQ = new ConcurrentLinkedQueue<Message>(); //may need to block until something is in the buffer
    //also need to check the removeAll operation is threadsafe
	/** Buffer for outgoing messages attribute  */
	protected ConcurrentLinkedQueue<Message> outMsgBuffer = new ConcurrentLinkedQueue<Message>();

	/** {@link Protocol <em>Protocol</em>} attribute */
	protected Protocol protocol;
	/** Connection status flag */
	protected boolean connack = false; 
	/** Time out attribute in milliseconds */
	protected long timeOut;
	
	/**
	 * Default constructor
	 */
	public ProtocolHandler(){
		
	}
	/**
	 * Getter for the the recipient&#39;s public key attribute
	 * <p>
	 * @param pk  the destination recipient&#39;s public key 
	 */
	public PublicKey getDestPK(){
		return destPK;
	}
	
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
	/**
	 * Check if there are any messages in the incoming message queue.
	 * <p>
	 * @return true if there the queue is not empty, else false.
	 */
	public boolean poll(){
		return !msgQ.isEmpty();
	}
	/**
	 * Pop an incoming message from the head of the incoming message queue. 
	 * Use this in a loop to get more than one message&#58;
	 * <pre>
	 * 	while ((message = msgQ.poll())  != null){ //do your processing.....}
	 * </pre>
	 * <p>
	 * @return the oldest {@link Java.util.HashMap <em>HashMap</em>} 
	 * 		object in the queue or NULL if the head of the queue is empty
	 */
	public Message pop() { //maintain the interface method which has no arguments
		//
		return msgQ.poll();
	}
	/**
	 * Pop a ping request message from the head of the ping request message queue. 
	 * Use this in a loop to get more than one message&#58;
	 * <pre>
	 * 	while ((message = msgQ.poll())  != null){ //do your processing.....}
	 * </pre>
	 * <p>
	 * @return the oldest {@link Java.util.HashMap <em>HashMap</em>} 
	 * 		object in the queue or NULL if the head of the queue is empty
	 */
	public Message popPingRequest(){		
		return pingReqQ.poll();		
	}
	/**
	 * Pop a ping acknowledge message from the head of the ping acknowledgement message queue. 
	 * Use this in a loop to get more than one message&#58;
	 * <pre>
	 * 	while ((message = msgQ.poll())  != null){ //do your processing.....}
	 * </pre>
	 * <p>
	 * @return the oldest {@link Java.util.HashMap <em>HashMap</em>} 
	 * 		object in the queue or NULL if the head of the queue is empty
	 */
	public Message popPingAck(){		
		return pingAckQ.poll();		
	}
	
	/**		
	 * Assembly contents for the status message.   Status messages are public by default.
	 * <p>
	 * @param status
	 * @return	a {@link java.util.HashMap <em>HashMap</em>} of key values for input into the communication payload.
	 * @throws ProtocolHandlerException on error access the owner&#39;s public key
	 */
	public abstract  HashMap<String, Object> getStatusMessage(String status) throws ProtocolHandlerException;
	/**
	 * Generate a ping request or acknowledgement message payload.
	 * <p>
	 * @param reqTS	the source ping request publication time in epoch seconds.  If null, this would
	 * 		  		be a ping request message.
	 * @return	{@link java.util.HashMap <em>HashMap</em>} representation of the message payload.
	 * @throws ProtocolHandlerException on error 
	 */
	public abstract HashMap<String, Object>  getPingMessage(Object reqTS) throws ProtocolHandlerException;
	
	/**
	 * Compile the target ping request topic name.
	 * <p>
	 * @return			The topic name.
	 */
	public abstract String getPingRequestDest();
	/**
	 * Compile the target ping acknowledgement topic name.
	 * <p>
	 * @param target 	The target friendly name.
	 * @return			The topic name.
	 */
	public abstract String getPingAckDest(String target);
	
	/** Compile the target destination 
	 * <p>
	 * @param sec	The {@link Security <em>Security</em>} flag
	 * @return			the destination {@link java.lang.String <em>String</em>}
	 */
	public abstract String getDestination(Security sec); //destination is already populated
	
	
	/**
	 * Initialise the channel, this includes subscribing#47;listening to all the correct inboxes,
	 * including the default built&#45;in ones according to the structure illustrated below&#58;
	 * <pre>
	 *  mf2c/[friendyName]/public
	 *	mf2c/[friendyName]/protected
	 *	mf2c/[friendyName]/private
	 *	mf2c/[friendyName]/handshake
	 *	mf2c/[friendyName]/public/pingreq
	 *	mf2c/[friendyName]/public/pingack
	 * </pre>
	 * It will also set up a pinger to periodically check if the mF2C destination is listening and alive.
	 * <p>
	 * @param properties	a {@link java.util.HashMap <em>HashMap</em>} of configuration key value pairs
	 * @throws {@link ProtocolHandlerException <em>ProtocolHandlerException</em>} on set up errors
	 */
	public void setup(HashMap<String, String> properties) throws ProtocolHandlerException{
		//protocol specific operations to be defined by the concrete classes
		friendyName = properties.get("friendyName");
		broker = properties.get("broker");
		//This is a botch to ensure the application works w/o PKI and will be updated in a later version
		if((properties.get("destination") != null) || (!properties.get("destination").isEmpty())){
			destination = properties.get("destination");			
		}else{			
			LOGGER.debug("no destination, this is just a subscriber!");
		}
		
		keepAlive = Integer.valueOf(properties.get("keepAlive"));
		timeOut = Long.valueOf(properties.get("timeOut"));
		
		//!!!rest of processing MUST be implemented by the specific protocol handler
	}
	/**
	 * Clean up and disconnect the client.
	 */
	public abstract void disconnect();
	
	/**
	 * Complete the metadata and publish a message.  If the {@link QoS <en><QoS/em>} flag is set to ATLEASTONCE, the message is published
	 * asynchronously, i.e. send and forget.  Otherwise, the message is published synchronously.
	 * <p>
	 * @param topicName	the destination topic
	 * @param qos		the {@link QoS <em>QoS</em>} flag
	 * @param payload	the message payload represented as a {@link java.util.HashMap <em>HashMap</em>} of 
	 * 					metadata and processed payload
	 * @throws ProtocolHandlerException on all processing errors
	 */
	public abstract void publish(String topicName, QoS qos, HashMap<String, Object> payload) throws ProtocolHandlerException;
	
	/**
	 * Flush the cached outgoing messages that have not yet been sent.
	 * <p>
	 * @throws ProtocolHandlerException on all processing errors
	 */
	public abstract void flush() throws ProtocolHandlerException; //incoming message queues are cleared by the parent channel object
	

}

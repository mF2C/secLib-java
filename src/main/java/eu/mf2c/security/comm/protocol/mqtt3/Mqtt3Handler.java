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
package eu.mf2c.security.comm.protocol.mqtt3;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import eu.mf2c.security.comm.Channel;
import eu.mf2c.security.comm.Receiver;
import eu.mf2c.security.comm.protocol.ProtocolHandler;
import eu.mf2c.security.comm.util.Message;
import eu.mf2c.security.comm.util.QoS;
import eu.mf2c.security.comm.util.Security;
import eu.mf2c.security.data.Identity;
import eu.mf2c.security.data.ReceivedMessage;
import eu.mf2c.security.exception.IdentityException;
import eu.mf2c.security.exception.ProtocolHandlerException;

/**
 * Mqtt3 messaging protocol handler.  The {@link Channel <em>Channel</em>} object
 * must set the three message queues to enable call backs.  The handler can be
 * used as both a publisher and subscriber.  Native Mqtt3 capability 
 * for buffering outgoing messages are used here.   
 * <p> * 
 * @author Shirley Crompton
 * @email  shirley.crompton@stfc.ac.uk
 * @org Data Science and Technology Group,
 *      UKRI Science and Technology Council
 * @Created 16 Jan 2018
 *
 */
public class Mqtt3Handler extends ProtocolHandler implements MqttCallback {
	//need to base this on the asynchronous mqtt client
	//think about concurrency
	//these queue needs to have a message type!!!!
	//!!! be careful of thread hanging....
		
	/** message logger */
	private static Logger LOGGER = Logger.getLogger(Mqtt3Handler.class.getName());
	private static String dest;
	
	/***************Constants**********************/
	/** Constant name for the public destination topic */
	private static final String TOPIC_PUBLIC = "mf2c/" + dest + "/public";
	/** Constant name for the private destination topic */
	private static final String TOPIC_PRIVATE = "mf2c/" + dest + "/private";
	/** Constant name for the protected destination topic */
	private static final String TOPIC_PROTECTED = "mf2c/" + dest + "/protected";
	/** Constant name for the ping request topic */
	private static final String TOPIC_PINGREQ = "mf2c/public/pingreq";
	/** Constant name for the ping acknowledgement topic */
	private static final String TOPIC_PINGACK = "mf2c/public/pingack";
	/** Constant name for the broker service status topic */
	private static final String TOPIC_STATUS = "mf2c/broker_services/status";
	/** Constant name for the handshake topic */
	private static final String TOPIC_HANDSHAKE = "mf2c/" + dest + "/public/handshake";
	/** Constant for connected connection status */
	private static final String STATUS_CONNECTED = "C";
	/** Constant for gracefully disconnected connection status */
	private static final String STATUS_GRACE_DISCONNECT = "DG";
	/** Constant for ungraceful disconnected connection status */
	private static final String STATUS_UG_DISCONNECT = "DU";
	
	
	/** the Mqtt client */
	private MqttClient client;
	
	/**
	 * Construct an instant
	 */
	public Mqtt3Handler() {
		super();
		dest = super.destination;
	}	
	/**
	 * Setter for the {@link #pingAckQ <em>pingAckQ</em>} attribute
	 * <p>
	 * @param pingAckQ the ping acknowledgement queue to set
	 
	public void setPingAckQ(ConcurrentLinkedQueue pingAckQ) {
		this.pingAckQ = pingAckQ;
	}

	/**
	 * Setter for the {@link #pingReqQ <em>pingReqQ</em>} attribute
	 * <p>
	 * @param pingReqQ the ping request queue to set
	
	public void setPingReqQ(ConcurrentLinkedQueue pingReqQ) {
		this.pingReqQ = pingReqQ;
	}

	/**
	 * Setter for the {@link #msgQ <em>msgQ</em>} attribute
	 * <p>
	 * @param msgQ the message queue to set
	
	public void setMsgQ(ConcurrentLinkedQueue msgQ) {
		this.msgQ = msgQ;
	}*/
	
	/*********************************** Mqtt3 client set up ********************************************************/
	public void setup(Properties properties, Receiver receiver) throws ProtocolHandlerException {
		super.setup(properties, receiver);
		//protocol specific
		try{
			MqttConnectOptions connOpt = new MqttConnectOptions();		
			connOpt.setCleanSession(false); //subscription info and queued messages are retained after client disconnect
			connOpt.setKeepAliveInterval(keepAlive);
			//payload is a base64 encoded version of the key:value
			connOpt.setWill(TOPIC_STATUS, super.generatePayload(getStatusMessage(STATUS_UG_DISCONNECT),Security.PUBLIC), QoS.ATLEASTONCE.ordinal(), true);
			//
			client = new MqttClient(broker, friendlyName);
			client.connect(connOpt);
			
			connack = client.isConnected();
			
			//			
			handshake();
				
			
			
			
		}catch(MqttException me){
			LOGGER.error("Mqtt exception on setting up mqtt3 protocol handler: " + me.getMessage());
			throw new ProtocolHandlerException(me.getMessage());
		}catch(ProtocolHandlerException pe){
			throw pe;
		}catch(Exception e){
			LOGGER.error("Failed to set up mqtt3 protocol handler: " + e.getMessage());
			throw new ProtocolHandlerException(e);
		}
		
		
		
		
		
	}
	/**
	 * Connect to broker and carry out the handshake process.
	 */
	public void handshake() throws MqttException{
		
		
		
		
		
		
		
		
	}
	
	
	
	/*********************************** Mqtt3 Publisher handling *************************************************/
	public void publish(List<String> destinations, Message msg){ //assuming qos, security flag already embedded in the Message object
		
		//add timestamp to message
		
		//check if sender's id is in message
		//make sure message is signed for sec=prot || priv and there is a public key
		//
		
		
		
		
	}
	
	
	
	
	/*********************************** Mqtt3 Subscriber handling *************************************************/
	
	
	
	
	
	
	/************************************ Mqtt3 callback handling *************************************************/
	/**  Note the method javadocs are copied from the mqtt3 library */	
	
	/**
	 * This method is called when the connection to the server is lost.
	 * <p>
	 * @param t the reason behind the loss of connection.
	 */
	@Override
	public void connectionLost(Throwable t) {
		//we are tolerating short term lost of connection, the Logger will provide the time stamp for the event 
		LOGGER.warn("Lost connection to MQTT broker :  " + t.getMessage());
		//what else do we need to do?  We will use mqtt3's capability to buffer messages while disconnected and to regain connection......
		
	}
	/**
	 * Called when delivery for a message has been completed, and all
	 * acknowledgments have been received. For QoS 0 messages it is
	 * called once the message has been handed to the network for
	 * delivery. For QoS 1 it is called when PUBACK is received and
	 * for QoS 2 when PUBCOMP is received. The token will be the same
	 * token as that returned when the message was published.
	 *
	 * @param dq the delivery token associated with the message.
	 */
	@Override
	public void deliveryComplete(IMqttDeliveryToken dq) {
		// 
		LOGGER.info("Message(" + dq.getMessageId() + ") delivered");
		//what else?
	}
	/**
	 * This method is called when a message arrives from the server.	 *
	 * <p>
	 * This method is invoked synchronously by the MQTT client. An
	 * acknowledgment is not sent back to the server until this
	 * method returns cleanly.</p>
	 * <p>
	 * If an implementation of this method throws an <code>Exception</code>, then the
	 * client will be shut down.  When the client is next re-connected, any QoS
	 * 1 or 2 messages will be redelivered by the server.</p>
	 * <p>
	 * Any additional messages which arrive while an
	 * implementation of this method is running, will build up in memory, and
	 * will then back up on the network.</p>
	 * <p>
	 * If an application needs to persist data, then it
	 * should ensure the data is persisted prior to returning from this method, as
	 * after returning from this method, the message is considered to have been
	 * delivered, and will not be reproducible.</p>
	 * <p>
	 * It is possible to send a new message within an implementation of this callback
	 * (for example, a response to this message), but the implementation must not
	 * disconnect the client, as it will be impossible to send an acknowledgment for
	 * the message being processed, and a deadlock will occur.
	 * <p>
	 * @param topic name of the topic on the message was published to
	 * @param msg the actual message.
	 * @throws Exception if a terminal error has occurred, and the client should be
	 * shut down.
	 */
	@Override
	public void messageArrived(String topic, MqttMessage msg) throws Exception {
		//we are assuming that the pingack and pingreq have their own topics, and the rest are mF2C application/infrastructure messages  
		ReceivedMessage rm = new ReceivedMessage(); //temp, we need to transform the message - may be different processing for different type of msg
		/*will need to repack messages!!!!!!!!!!!!!
				
		 * 
		 */	
		if(topic.equals(Message.PINGACK)){
			this.pingAckQ.offer(rm);
		}else if(topic.equals(Message.PINGREQ)){
			this.pingReqQ.offer(rm);
		}else{
			this.msgQ.offer(rm);
		}		
		LOGGER.debug("Offered message(" +  msg.getId()+ ") to " + topic + " queue");
	}
	
	/************************************** methods for Channel to poll and pop incoming message queue *************************************/
	
	
	/**
	 * Check if there are any messages in the incoming message queue.
	 * <p>
	 * @return true if there the queue is not empty, else false.
	 */
	public boolean poll(){
		if(this.msgQ.isEmpty()){
			return false;
		}else{
			return true;
		}
	}
	
	/************************************ utilities ***************************************************************************************/
	/**		
	 * Assembly contents for the status message.   Status messages are public by default.
	 * <p>
	 * @param status
	 * @return	a {@link java.util.HashMap <em>HashMap</em>} of key values for input into the communication payload.
	 * @throws ProtocolHandlerException on error access the owner&#39;s public key
	 */
	public HashMap<String, String> getStatusMessage(String status) throws ProtocolHandlerException{		
		//status messages are public	
		HashMap<String, String> statusHM = new HashMap<String, String>();
		statusHM.put("status", status);
		
		if(status.equals(STATUS_CONNECTED)){
			try {
				statusHM.put("public_key", Identity.getInstance().getPublicKey().toString());
			} catch (IdentityException e) {
				LOGGER.error("Failed to get a String representation of the public key : " + e.getMessage());
				throw new ProtocolHandlerException(e);
			}
		}
		return statusHM;
	}	
}

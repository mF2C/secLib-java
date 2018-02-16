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

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;

import net.minidev.json.JSONValue;
import net.minidev.json.parser.ParseException;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import eu.mf2c.security.comm.Channel;
import eu.mf2c.security.comm.Receiver;
import eu.mf2c.security.comm.protocol.ProtocolHandler;
import eu.mf2c.security.comm.util.Base64Helper;
import eu.mf2c.security.comm.util.QoS;
import eu.mf2c.security.comm.util.Security;
import eu.mf2c.security.data.Identity;
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
public class Mqtt3Handler extends ProtocolHandler implements MqttCallbackExtended {
	//need to base this on the asynchronous mqtt client
	//think about concurrency
	//these queue needs to have a message type!!!!
	//!!! be careful of thread hanging....
		
	/** message logger */
	private static Logger LOGGER = Logger.getLogger(Mqtt3Handler.class.getName());
	private static String dest;
	
	/***************Constants**********************/
	/** Constant name for the public destination topic */
	private static final String TOPIC_PUBLIC = "mf2c/" + friendlyName + "/public";
	/** Constant name for the private destination topic */
	private static final String TOPIC_PRIVATE = "mf2c/" + friendlyName + "/private";
	/** Constant name for the protected destination topic */
	private static final String TOPIC_PROTECTED = "mf2c/" + friendlyName + "/protected";
	/** Constant name for the handshake topic */
	private static final String TOPIC_HANDSHAKE = "mf2c/" + friendlyName + "/public/handshake";
	/** Constant name for the ping request topic */
	private static final String TOPIC_PINGREQ = "mf2c/public/pingreq";
	/** Constant name for the ping acknowledgement topic */
	private static final String TOPIC_PINGACK = "mf2c/public/pingack";
	/** Constant name for the broker service status topic */
	private static final String TOPIC_STATUS = "mf2c/broker_services/status";  //we just send not subscribe to this topic
	
	/** Constant for connected connection status */
	private static final String STATUS_CONNECTED = "C";
	/** Constant for gracefully disconnected connection status */
	private static final String STATUS_GRACE_DISCONNECT = "DG";
	/** Constant for ungraceful disconnected connection status */
	private static final String STATUS_UG_DISCONNECT = "DU";
	
	
	/** the Mqtt client */
	private MqttAsyncClient client; //asynchronous client is non-blocking, but can also be used in a blocking mode
	
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
	//there should be a separate step to discover the broker and distribute keys before setting up the client!!!!!!!!!!!
	
	
	
	public void setup(HashMap<String, String> properties, Receiver receiver) throws ProtocolHandlerException {
		super.setup(properties, receiver);
		//protocol specific set up
		try{
			MqttConnectOptions connOpt = new MqttConnectOptions();		
			connOpt.setCleanSession(false); //durable subscription.  Info and queued messages are retained after client disconnect
			connOpt.setKeepAliveInterval(keepAlive);
			
			connOpt.setAutomaticReconnect(true); /* we use Paho's reconnect functionality
			Sets whether the client will automatically attempt to reconnect to the server if the connection is lost.
			If set to true, in the event that the connection is lost, the client will attempt to reconnect to the server. 
			It will initially wait 1 second before it attempts to reconnect, for every failed reconnect attempt, the delay will double 
			until it is at 2 minutes at which point the delay will stay at 2 minutes. This prevents both waiting an unnecessary amount 
			of time between reconnect attempts, and also from wasting bandwidth from attempting to connect too frequently.*/

			connOpt.setConnectionTimeout(60); //default is 30 seconds			
			connOpt.setWill(TOPIC_STATUS, Base64Helper.encodeToBytes(JSONValue.toJSONString(super.processPayload(getStatusMessage(STATUS_UG_DISCONNECT), Security.PUBLIC))), QoS.ATLEASTONCE.ordinal(), true);
			//default MQTT version is 3.1.1, then falls back to 3.1
			client = new MqttAsyncClient(broker, friendlyName, new MemoryPersistence()); 
			//may need to use persistent storage for more reliable service instead of MemoryPersistence
			//String tmpDir = System.getProperty("java.io.tmpdir");
	    	//MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);
	    	//
			//enable disconnected publishing, will need to tweak these params
			DisconnectedBufferOptions bufferOpts = new DisconnectedBufferOptions();
			bufferOpts.setBufferEnabled(true); // Enable Disconnected Publishing
			bufferOpts.setBufferSize(100); // Only Store 1000 messages in the buffer
			bufferOpts.setPersistBuffer(false); // Do not persist the buffer
			bufferOpts.setDeleteOldestMessages(true); // Delete oldest messages once the buffer is full
			//
            client.setBufferOpts(bufferOpts);
			client.setCallback(this);  //do we use another class to handle the call back?
			//
			//The receiver is used to handle the onMessage callback
			//MqttAsyncClient subClient = new MqttAsyncClient(serverURIString, methodName + "sub-client");
			//MqttV3Receiver mqttV3Receiver = new MqttV3Receiver(subClient, LoggingUtilities.getPrintStream());
			
			
			//first connection needs to be blocking to stop messages being sent and to make sure there are no non-network issues
			IMqttToken connectToken = client.connect(connOpt); 
			connectToken.waitForCompletion(new Long(60000));//block until connected or timed out (60000 milliseconds)
			this.connack = client.isConnected();
			//
			if(this.connack){
				LOGGER.info("First connection established!");
			}else{
				String errMsg = "Failed to connect the first time due to error or timedout: " + (connectToken.getException() == null ? "exception not set" : connectToken.getException());
				LOGGER.error(errMsg);
				throw new ProtocolHandlerException(errMsg); //may get connected after timeout, but lets the client decide what to do
			}
			//subscribe to the default topics
			this.subscribe();
			//should be ready for action			
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
	
	
	/*********************************** Mqtt3 Publisher handling  *************************************************/
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
	public void publish(String topicName, QoS qos, HashMap<String, Object> payload) throws ProtocolHandlerException{ 
		//We assume that there is only one topic per destination (excluding the default ones like pingack, pingreq....)	
		//assuming security flag, sourceId, publicKey(if used) are already embedded in hashmap
		
		//generate unix timestamp as a String
		payload.put("timestamp", Instant.now().getEpochSecond());
		payload.put("qos",qos.ordinal()); //0=public/AMO, 1=protected/ALO, 2 = private/EO
		//
		if(qos.equals(QoS.ATMOSTONCE)){//asynchronous
			this.asyncPublish(topicName, qos, Base64Helper.encodeToBytes(JSONValue.toJSONString(payload)));
			
		}else{
			this.syncPublish(topicName, qos, Base64Helper.encodeToBytes(JSONValue.toJSONString(payload)));
		}
	}
	
	/**
	 * Publish a message asynchronously.  
	 * <p>
	 * @param topicName	the destination topic
	 * @param qos		the {@link QoS <em>QoS</em>} flag
	 * @param payload	the message payload represented as a {@link java.lang.Byte <em>Byte</em>} array
	 * @throws ProtocolHandlerException  on protocol or other processing exceptions
	 */
	private void asyncPublish(String topicName, QoS qos, byte[] payload) throws ProtocolHandlerException {
		//according to Cheney's code, QoS.AtLEASTONCE is used to flag asynchronous delivery
		
	 	// Send / publish a message to the server
		// Get a token and setup an asynchronous listener on the token which
		// will be notified once the message has been delivered
   		MqttMessage message = new MqttMessage(payload);
    	message.setQos(qos.ordinal());    	
    	LOGGER.debug("Publishing asynchronously to topic \"" + topicName + "\" qos " + qos); //use the logger timestamp

    	// Setup a listener object to be notified when the publish completes.
    	//
    	IMqttActionListener pubListener = new IMqttActionListener() {
    		/**
    		 * Mqtt calls this when the message is published successfully.
    		 */
			public void onSuccess(IMqttToken asyncActionToken) {
				LOGGER.info(asyncActionToken.getMessageId() + "Publish Completed."); //use the logger timestamp	
			}

			public void onFailure(IMqttToken asyncActionToken, Throwable exception) {				
				LOGGER.info(asyncActionToken.getMessageId() + "Publish failed" + exception);					
			}
		};

    	try {
    		//the client is configured to do disconnect publishing and can buffer up to 100 messages
	    	client.publish(topicName, message, friendlyName + "_async publisher", pubListener);
	    	
    	} catch (MqttException e) {
    		LOGGER.error(e.getMessage());
			throw new ProtocolHandlerException(e);
		} catch (Exception ex){
			LOGGER.error("Error publishing message asynchronously: " + ex.getMessage());
			throw new ProtocolHandlerException(ex);
		}
	}
	
	/**
	 * Publish a message synchronously.
	 * <p>
	 * @param topicName	the destination topic
	 * @param qos		the quality of service flag
	 * @param payload	the message payload represented as a {@link java.lang.Byte <em>Byte</em>} array
	 * @throws ProtocolHandlerException  on protocol or other processing exceptions
	 */
	private void syncPublish(String topicName, QoS qos, byte[] payload) throws ProtocolHandlerException {
		MqttMessage message = new MqttMessage(payload);
    	message.setQos(qos.ordinal());    	
    	LOGGER.debug("Publishing synchronously to topic \"" + topicName + "\" qos " + qos); //use the logger timestamp
    	//
    	try {
			IMqttDeliveryToken dt = client.publish(topicName, message);
			dt.waitForCompletion();
			LOGGER.debug("Published message to " + topicName);
		} catch (MqttException e) {
			LOGGER.error(e.getMessage());
			throw new ProtocolHandlerException(e);
		} catch (Exception ex){
			LOGGER.error("Error publishing message asynchronously: " + ex.getMessage());
			throw new ProtocolHandlerException(ex);
		}
	}
	
	/*********************************** Mqtt3 Subscriber handling *************************************************/
	
	private void subscribe() throws ProtocolHandlerException{
			//we subscribe to the default topics sequentially using a blocking method, we won't store the subscription tokens.
			String[] topics = {TOPIC_PUBLIC, TOPIC_PRIVATE, TOPIC_PROTECTED, TOPIC_PINGREQ, TOPIC_PINGACK, TOPIC_HANDSHAKE};
			int[] qoss = new int[topics.length];
			Arrays.fill(qoss, 1);
			//
	    	try {
	    		if(client == null || !client.isConnected()){
					throw new Exception("Null client or not connected, cannot subscribe!");
				}
	    		IMqttToken mt = client.subscribe(topics, qoss);
	    		mt.waitForCompletion();
	    	} catch (MqttException e) {
	    		LOGGER.error("Mqtt Error making subscriptions: " + e.getMessage());
				throw new ProtocolHandlerException(e);
			} catch (Exception e){
				LOGGER.error("Error making subscriptions: " + e.getMessage());
				throw new ProtocolHandlerException(e);				
			}
				
	}
	
	
	
	
	/************************************ Mqtt3 callback handling *************************************************/
	/**  Note the method javadocs are copied from the mqtt3 library */	
	
	/**
	 * This method is called when the connection to the server is lost.
	 * <p>
	 * @param t the reason behind the loss of connection.
	 */
	@Override
	public void connectionLost(Throwable t) {
		this.connack = false;
		//we are tolerating short term lost of connection, the Logger will provide the time stamp for the event
		LOGGER.warn("Lost connection to MQTT broker :  " + t.getMessage());
		//reconnect option is set to true, mqtt will auto reconnect and buffered messages sent
		//as no connection, no point sending out status message
		
	}
	/**
	 * Called when delivery for a message has been completed, and all
	 * acknowledgments have been received in line with QoS.
	 * <p>
	 * <ul>
	 * <ui> QoS 0 &#58; the message has been handed to the network for delivery</ui>
	 * <ui> QoS 1 &#58; when PUBACK is received</ui>
	 * <ui> QoS 2 &#58; when PUBCOMP is received</ui>
	 * </ul>
	 * <p>
	 * The token will be the same token as that returned when the message was published.
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
	 * This method is called when a message arrives from the server.	
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
	 * @param msg the actual MQTT message.
	 * @throws Exception if a terminal error has occurred, and the client should be
	 * shut down.
	 */
	@Override
	public void messageArrived(String topic, MqttMessage msg) throws Exception {
		//we are assuming that the pingack and pingreq have their own topics, and the rest are mF2C application/infrastructure messages		
		//handshake will be handled in a blocking call
		if(topic.equals(TOPIC_PINGACK)){
			this.pingAckQ.offer(msg.getPayload());
		}else if(topic.equals(TOPIC_PINGREQ)){
			this.pingReqQ.offer(msg.getPayload());
		}else if (topic.equals(TOPIC_PRIVATE)  || topic.equals(TOPIC_PROTECTED) || topic.equals(TOPIC_PUBLIC)){
			this.msgQ.offer(msg.getPayload());
		}		
		LOGGER.debug("Offered message(" +  msg.getId()+ ") to " + topic + " queue");
	}
	
	/**
	 * Extension of MqttCallback to allow new callbacks
	 * without breaking the API for existing applications.
	 * Classes implementing this interface can be registered on
	 * both synchronous and asynchronous clients.
	 * <p>
	 * If the cleanSession flag is set to false, then any
	 * subscriptions would not have to be re-made.
	 *<p>
	 * called when the connection to the server is completed successfully.
	 * @param reconnect If true, the connection was the result of automatic reconnect.
	 * @param serverURI The server URI that the connection was made to.
	 */
	 
	@SuppressWarnings("unchecked")
	@Override
	public void connectComplete(boolean reconnect, String serverURI) {
		String a = "";
		if(reconnect){
			a = "Re";
		}
		LOGGER.info(a +"Connected to broker(" + this.broker + ") at " + Instant.now()); //get the timestamp
		this.connack = true;
		/*  as we don't (can't) send a disconnected message, is there any point of doing this?
		try {
			this.publish(TOPIC_STATUS, QoS.ATLEASTONCE, super.processPayload(getStatusMessage(STATUS_CONNECTED),Security.PUBLIC));
		} catch (ProtocolHandlerException e) {
			LOGGER.error("Error publishing 'reconnected' status to " + TOPIC_STATUS + " : " + e.getMessage());
		}*/
		//we are using a durable client with cleanSession = false; no need to re-subscribe
		//see https://github.com/eclipse/paho.mqtt.java/issues/9
	}	
	
	/************************************** Instance methods *******************************************************************************/
	
	/**
	 * Disconnect the mqtt3 client.  This method blocks.
	 * The handler should publish a disconnect message to the status topic
	 * before invoking the disconnect method.   If the client failed to disconnect 
	 * gracefully, the disconnectForibly method will be called.
	 *   
	 */
	public void disconnect(){		
				
		this.connack = false;
		if(client != null && client.isConnected()){
			try {
				this.cleanUp(); //send disconnect message to broker & other clean up operations
				IMqttToken disconnectToken = client.disconnect();
				disconnectToken.waitForCompletion(); //blocking
				//
			} catch (MqttException e) {
				// 
				LOGGER.error("Error disconnecting : " + e.getMessage() + ".  Willl disconnect forceably....");
				//TODO needs to send disconnect message (ungraceful)
				try {
					client.disconnectForcibly();  //method waits for 30 secs
				} catch (MqttException e1) {
					// 
					LOGGER.error("Error disconnecting ungracefully : " + e.getMessage() + ".  Willl swallow the error....");
				} 
			} catch (Exception oe){
				//just log the error
				LOGGER.error("Error disconnecting : " + oe.getMessage());
			}finally{
				try {
					client.close();
					LOGGER.info("Released mqtt client resources.");
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error closing client : " + e.getMessage() + ".  Willl swallow the error....");
				}
			}
			
		}
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Send disconnect message to broker.
	 */
	public void cleanUp(){
		
		try{
			this.publish(TOPIC_STATUS, QoS.ATLEASTONCE, super.processPayload(getStatusMessage(STATUS_GRACE_DISCONNECT), Security.PROTECTED));
			//??anything else we need to tidy up?? here????
			//
		}catch(Exception e){
			//we are disconnecting, so ignore error publishing status
			LOGGER.error("Error publishing disconnect message to broker: " + e.getMessage());
			
		}
		
		
		
		
	}
	
	/************************************** methods for Channel to poll and pop incoming message queue *************************************/
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public HashMap<String, Object> pop() throws ProtocolHandlerException{
		HashMap<String, Object> payloadHM = null;
		byte[] payloadB = this.msgQ.poll();
		if(payloadB != null){
			//
			try{
	        	payloadHM = (HashMap<String, Object>) JSONValue.parseStrict(Base64Helper.decodeToString(payloadB));
	        }catch (ParseException e) {
	            LOGGER.error("ParseException converting payload to hashmap : " + e.getMessage());
	            throw new ProtocolHandlerException(e);
	        } catch(Exception e){
	        	 LOGGER.error("Error popping payload message : " + e.getMessage());
		            throw new ProtocolHandlerException(e);			
	        }
			
		}
		return payloadHM;
	}
	
	/************************************ utilities ***************************************************************************************/
	/**		
	 * Assembly contents for the status message.   Status messages are public by default.
	 * <p>
	 * @param status
	 * @return	a {@link java.util.HashMap <em>HashMap</em>} of key values for input into the communication payload.
	 * @throws ProtocolHandlerException on error access the owner&#39;s public key
	 */
	public HashMap<String, Object> getStatusMessage(String status) throws ProtocolHandlerException{	
		//we use String, Object to comply with the Smart-Json processing further downstream
		//status messages are public	
		HashMap<String, Object> statusHM = new HashMap<String, Object>();
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

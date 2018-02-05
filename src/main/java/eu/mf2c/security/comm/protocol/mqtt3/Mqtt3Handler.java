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

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import eu.mf2c.security.comm.protocol.ProtocolHandler;
import eu.mf2c.security.comm.util.Message;
import eu.mf2c.security.data.ReceivedMessage;

/**
 * Mqtt3 messaging protocol handler.  The caller must set the three message queues
 * to enable call backs after instantiating the handler...... 
 * 
 * 
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
	
	/**
	 * Default empty constructor
	 */
	public Mqtt3Handler() {
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
	
	

}

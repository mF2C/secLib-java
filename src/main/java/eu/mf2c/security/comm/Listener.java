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

import java.time.Instant;
import java.util.HashMap;

import org.apache.log4j.Logger;

import eu.mf2c.security.comm.protocol.ProtocolHandler;
import eu.mf2c.security.comm.util.QoS;
import eu.mf2c.security.comm.util.Security;
import eu.mf2c.security.data.Message;
import eu.mf2c.security.exception.MessageException;

/**
 * The Listener handles incoming ping acknowledgement and request {@link Message <em>Message</em>}s.
 * <p>
 * @author Shirley Crompton
 * @email  shirley.crompton@stfc.ac.uk
 * @org Data Science and Technology Group,
 *      UKRI Science and Technology Council
 * @Created 16 Jan 2018
 *
 */
public class Listener implements Runnable {
	/** message logger attribute */
	private static final Logger LOGGER = Logger.getLogger(Listener.class.getName());
	/** the protocol handler attribute for publishing and receiving messages*/
	private ProtocolHandler handler;
	/** last ping timestamp in epoch seconds */
	private long lastPing;
	/** last ping acknowledgement timestamp in epoch seconds*/
	private long lastPingAck;
	/** timeout in seconds */
	private long timeout;
	/**
	 * construct an instance.
	 * <p>
	 * @param parent  the parent of this instance.
	 */
	public Listener(ProtocolHandler pHandler, long timeoutLength){		
		this.handler = pHandler;
		this.timeout = timeoutLength;
	}
	/**
	 * Getter for the {@link eu.mf2c.security.comm.protocol.ProtocolHandler <em>ProtocolHandler</em>} 
	 * <p>
	 * @return the {@link eu.mf2c.security.comm.protocol.ProtocolHandler <em>ProtocolHandler</em>} attribute
	 */
	public ProtocolHandler getHandler(){
		return this.handler;
	}
	
	/**
	 * Send a ping acknowledgement message.
	 * <p>
	 * @param msg	the incoming ping request {@link Message <em>Message</em>} object
	 */
	private void ackPing(Message msg){
		try{
			msg.unpackMsg(); //it is a public msg, no need to verify signature & decrypt payload
			String target = (String) msg.getPayloadHM().get("source");			
			Message ackMsg = new Message((HashMap<String, Object>) this.handler.getPingMessage(msg.getPayloadHM().get("timestamp")));
			msg.packMsg( Security.PUBLIC, this.handler.getProtocol(), QoS.EXACTLYONCE, null);
			this.handler.publish(this.handler.getPingAckDest(target), QoS.EXACTLYONCE, ackMsg.getPayloadHM());
			this.lastPing = Instant.now().getEpochSecond();
		}catch(Exception e){
			LOGGER.error("Acknowledge ping error: " + e.getMessage() + ".  Bypassing this one.");
			//we swallow this exception and pass on
		}
	}
	/**
	 * Process the ping acknowledgement {@link Message <em>Message</em>} objects
	 * @param msg	the incoming ping acknowledgement {@link Message <em>Message</em>} object
	 */
	private void processPingAck(Message msg){
		//
		try {
			msg.unpackMsg();
			this.lastPingAck = (long) msg.getPayloadHM().get("timestamp");
			if(this.lastPingAck - (long) msg.getPayloadHM().get("pingRequestTS") > this.timeout ){
				//what are we going to do??????????????????  
				LOGGER.warn("ping acknowledgement took longer than the time out value!");
			}
		} catch (MessageException e) {
			// 
			LOGGER.error("Check ping acknowledgement error: " + e.getMessage() + ".  Bypassing this one.");
			//we swallow this exception and pass on
		}
		
	}

	@Override
	public void run() {
		//
		Message pingMsg;
		while ((pingMsg = this.handler.popPingRequest()) != null){ 
			this.ackPing(pingMsg);
			/*  not sure this is legitimate, the pingack may not be mapped t the source pingreq.  Need some other mechanism to determine timeout when the ping target is 
			 * not responding (not sending packack)
			if(this.lastPing - this.lastPingAck > this.timeout){
				LOGGER.warn("Ping acknowledgement took longer than timeout value!");
			}*/
		}
		LOGGER.debug("Current run completing.....");
	}
	
	

}

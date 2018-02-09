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

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import eu.mf2c.security.data.ReceivedMessage;

/**
 * The Listener listens to incoming messages and enqueue them. 
 * If it subscribes to the <i>ping</i> topic, the {@link Channel <em>Channel</em>}
 * needs to response to the pings.
 * @author Shirley Crompton
 * @email  shirley.crompton@stfc.ac.uk
 * @org Data Science and Technology Group,
 *      UKRI Science and Technology Council
 * @Created 16 Jan 2018
 *
 */
public class Listener {
	/** message logger attribute */
	private static final Logger LOGGER = Logger.getLogger(Listener.class.getName());
	/** synchronised FIFO queue attribute, incoming messages are buffered in the concrete message protocol handler*/
	
	
	//according to the UML diagram the Listener has a ref to the protocol handler
	
	
	
	///primary role is to listen to incoming messages and enqueue them.  ??Wouldn't this be better if we need to protocol handler filters the message, then we don't need to know about
	//protocol-specific processing (eg. serializaton, how the 'destination' is represented [e.g. as a topic in messaging] etc.)
	//will need to use more than 1 queue anyway, as once you pop it, you can't put it back or if you enqueue it again, it will be at the bottom of the queue
	//peek only looks at the head object in the queue	
	
	//???needs to implement threading 	//needs to the instantiated with a ConcurrentLinkedQueue so that the FIFO queued messages can be accessed threadsafe 
	//a PingService aggregates a Listener object
	//Channel creates Listener with a ProtocolHandler ref
	
	public ReceivedMessage pop(){
		//get 1 message at a time from the FIFO queue
		//should return a multi-valued class (friendlyname, flags, sender_id, msg, tmstamp sent from src and received here)
		//implementation should return NULL (ConcurrentLinkedQueue returns null when empty) or an empty String
		//this should be used via the Channel object by the user client 
		
		
		return null;
	}
	
	public void poll(){
		
		//use the protocolhander to get/listen for incoming messages, non-blocking
		//this is used by the user application via the channel object to determine if the queue has message/s
		
		
		
		
		
	}
	
	

}

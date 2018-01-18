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

import org.apache.log4j.Logger;

import eu.mf2c.security.comm.protocol.ProtocolHandler;
import eu.mf2c.security.comm.util.Protocol;
import eu.mf2c.security.data.Identity;
import eu.mf2c.security.data.Message;
import eu.mf2c.security.exception.ChannelException;


/**
 * This Class implements the {@link Channelable} interface to
 * support the secure communication of data and control messages
 * via various communication protocols.  An mF2C agent can 
 * run several channels but each channel handles only one
 * specific protocol. 
 * <p>
 * @author Shirley Crompton
 * @email  shirley.crompton@stfc.ac.uk
 * @org Data Science and Technology Group,
 *      UKRI Science and Technology Council
 * @Created 9 Jan 2018
 *
 */
public class Channel implements Channelable {
	
	/*
	 * !!!!!!!!!!!!!!
	A Channel has a Listener
	A Ping Service has a Listener
	A Listener may be part of a Ping Service
	
	*/
	
	private final static Logger LOGGER = Logger
			.getLogger(Channel.class.getName());
	/** Transport type  */
	protected Protocol transport = null; //initialised to null
	/** Channel friendly name */
	protected String friendyName;
	/** Identity attribute, this is a Singleton */
	protected Identity identity;
	
	/** Listener attribute */
	protected Listener listener;
	/** Sender attribute */
	protected Sender sender;
	/** ProtocolHandler attribute */
	protected ProtocolHandler handler;
	
	
	/**
	 * Construct an instance.  This will throw an exception if error initialising the channel. 
	 * The calling application must handle the error.
	 * <p>
	 * @param destination  {@link java.lang.String <em>String</em> representation of the communication destination
	 * @param transport    {@link Protocol <em>Protocol</em>} flag
	 * @param friendyName  {@link java.lang.String <em>String</em> representation of the instance friendy name
	 * @throws {@link ChannelException} on {@link #initChannel(String, Protocol)} error
	 */
	public Channel(String destination, Protocol transport, String friendyName) throws ChannelException{
		LOGGER.debug("Creating an instance : " + friendyName + ", " + destination + ", " + transport);
		//
		//this.transport = transport;
		this.friendyName = friendyName;
		try{
			this.initChannel(destination, transport);
			
			//!!!!bootstrap the identity... this is passed to the protocolHandler on instantiation
			this.identity = Identity.getInstance();
			
			
			
		}catch(Exception e){
			LOGGER.error("Failed to instantiate channel with friendy name(" + this.friendyName + "): " + e.getMessage()) ;
			throw new ChannelException(e.getCause());
		}
				
	}
	/** 
	 * initialise the Channel
	 * <p>
	 * @param destination  {@link java.lang.String <em>String</em> representation of the communication destination
	 * @param transport    {@link Protocol <em>Protocol</em>} flag
	 */
	private void initChannel(String destination, Protocol transport) throws Exception{
		
		//TODO
		
		
		
		
	}
	@Override
	public void send(Message message, List<Enum> flags) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void flush() {
		// TODO flush all the buffers if used
		
	}
	@Override
	public void destruct() {
		
		// flush() and gracefully terminate the connection
		this.friendyName = null;
		this.flush();
		//call the protocol handler to terminate connection gracefully....
		
		
	}
	

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

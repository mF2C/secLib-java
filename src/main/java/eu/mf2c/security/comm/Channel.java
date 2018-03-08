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

import java.security.PublicKey;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import eu.mf2c.security.comm.protocol.ProtocolHandler;
import eu.mf2c.security.comm.protocol.ProtocolHandlers;
import eu.mf2c.security.comm.util.Privacy;
import eu.mf2c.security.comm.util.Protocol;
import eu.mf2c.security.comm.util.QoS;
import eu.mf2c.security.comm.util.Security;
import eu.mf2c.security.data.Identity;
import eu.mf2c.security.data.Message;
import eu.mf2c.security.exception.ChannelException;
import eu.mf2c.security.exception.MessageException;
import eu.mf2c.security.exception.ProtocolHandlerException;


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
	
	
	private final static Logger LOGGER = Logger
			.getLogger(Channel.class.getName());
	/** Transport type  */
	protected Protocol transport = null; //initialised to null	
	/** Channel friendy name */
	protected String friendyName;
	/** communication target.  A null destination signifies a listening channel */
	protected String destination = null;
	/** Identity attribute, this is a Singleton */
	protected Identity identity;
	/** Listener attribute */
	protected Listener listener; 
	/** Ping service attribute */
	protected PingService pingService; 	
	/** ProtocolHandler attribute */
	protected ProtocolHandler handler;
	/** message broker address */
	private String broker = "vds095.gridpp.rl.ac.uk"; //hardcoded for the moment, needs to be discovered during the bootstrap process
	/** time out value in seconds */ //hardcoded for the moment, needs to be configurable
	private int timeout = 60;
	
	
	
	/**
	 * Construct an instance.  This will throw an exception if error initialising the channel. 
	 * The calling application must handle the error.
	 * <p>
	 * @param destination  {@link java.lang.String <em>String</em> representation of the communication destination
	 * @param transport    {@link Protocol <em>Protocol</em>} flag
	 * @param friendyName  {@link java.lang.String <em>String</em> representation of the instance&#39;s friendy name
	 * @throws {@link ChannelException} on {@link #initChannel(String, Protocol)} error
	 */
	public Channel(String destination, Protocol protocol, String friendyName) throws ChannelException{
		//validate entry
		if(protocol == null){
			throw new ChannelException("transport protocol cannot be null!");
		}
		if(friendyName == null || friendyName.isEmpty()){
			throw new ChannelException("friendyName cannot be null or empty!");
		}
		if(destination == null){	// just a listening channel, temporary fix until we use PKI
			LOGGER.debug("Creating a listening Channel instance : " + friendyName + ", using " + transport);
		}else{
			this.destination = destination;
			LOGGER.debug("Creating a Channel instance : " + friendyName + ", using " + transport + " to " + destination);
		}		
		this.friendyName = friendyName;
		try{
			//bootstrap the identity... this is passed to the protocolHandler on instantiation
			this.identity = Identity.getInstance();
			//this creates the correct protocol client
			this.initChannel();
			//this creates the Listener object
			createListener();			
			//starts the ping service
			startPingService();
		}catch(Exception e){
			LOGGER.error("Failed to instantiate channel with friendy name(" + this.friendyName + "): " + e.getMessage()) ;
			throw new ChannelException(e.getCause());
		}
				
	}
	/** 
	 * initialise the Channel and set up the required {@link ProtocolHandler <em>ProtocolHandler</em>}	 * 
	 */
	private void initChannel() throws Exception{
		
		//create the handler
		this.handler = ProtocolHandlers.newProtocolHandler(this.transport);
		//initiaise it
		HashMap<String, String> properties = new HashMap<String, String>();
		properties.put("friendyName", this.friendyName);
		properties.put("broker", this.broker);
		properties.put("destination", (destination == null ? null : this.destination)); //could be null
		properties.put("keepAlive",String.valueOf(this.timeout));
		properties.put("timeOut", String.valueOf(this.timeout));
		//
		this.handler.setup(properties); //set up handles key exchange etc.		
	}
	/**
	 * Create an instance of the {@link Listener <em>Listener</em>} to handle
	 * incoming ping requests and ping acknowledgements.
	 */
	private void createListener(){
		//
		this.listener = new Listener(this.handler, this.timeout); //provide a ref to the handler
	}
	/**
	 * Create and start the service in a {@link java.lang.Thread <em>Thread</em>}.
	 * If this is not just a listening channel, the service will send out a ping request 
	 * to the target {@link #destination <em>destination</em>} at a set interval.
	 * The service also runs the {@link Listener <em>Listener</em>} periodically to
	 * handle incoming ping requests and acknowledgements.
	 */
	private void startPingService(){
		LOGGER.debug("About to start ping service in a thread ....");
		//
		boolean noPing = false;
		if(this.destination == null){
			noPing = true;
		}
		this.pingService = new PingService(this.timeout, this.listener, noPing); //try every 60 seconds as the ping interval, could make this configurable
		Thread pingServiceThread = new Thread(this.pingService, "PingService");
		pingServiceThread.start();
		
	}
	/**
	 * Stop the {@link PingService <em>PingService</em>} thread by
	 * setting its keepRunning flag to false.
	 */
	private void stopPingService(){
		LOGGER.debug("About to stop ping service ....");
		//
		this.pingService.setKeepRunning(false);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void send(Message message, List<Enum<?>> flags) throws ChannelException {
		//validate 
		if(message == null || message.getPayloadHM() == null || message.getPayloadHM().isEmpty() ){
			LOGGER.error("Unable to send message, there is no message or message payload!");
			throw new ChannelException("Unable to send message, there is no message or message payload!");
		}
		if(flags == null || flags.isEmpty()){
			LOGGER.error("Unable to send message, need to specify flags!");
			throw new ChannelException("Unable to send message, need to specify flags!");
		}
		Security sec = (Security) getFlag(Security.class, flags);
		if(sec == null ){
			LOGGER.error("Unable to send message, need to specify security flag!");
			throw new ChannelException("Unable to send message, need to specify security flag!");
		}
		QoS qos = (QoS) getFlag(QoS.class, flags);
		if(qos == null ){
			LOGGER.error("Unable to send message, need to specify QoS flag!");
			throw new ChannelException("Unable to send message, need to specify QoS flag!");
		}
		Privacy privacy = (Privacy) getFlag(Privacy.class, flags);
		if(privacy == null ){
			LOGGER.error("Unable to send message, need to specify privacy flag!");
			throw new ChannelException("Unable to send message, need to specify privacy flag!");
		}
		//we are safe now, go ahead
		try{
			message.packMsg(sec, this.transport, qos, null);
			this.handler.publish(this.handler.getDestination(sec),qos, message.getPayloadHM());
			
		}catch(Exception e){
			LOGGER.error("Error sending message: " + e.getMessage());
			throw new ChannelException("Error sending message: " + e.getMessage());
		}		
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void flush() {
		// flush all the outgoing message buffers if used
		//TODO clarify if we need to flush the concurrent queues too
		try {
			this.handler.flush();
		} catch (ProtocolHandlerException e) {
			LOGGER.error("Error flushing buffers: " + e.getMessage());
		}
		
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void destruct() {
		//stop the ping service which also controls the listener
		this.stopPingService();
		// flush() and gracefully terminate the connection
		this.friendyName = null;
		this.flush();
		//call the protocol handler to terminate connection gracefully....
		this.handler.disconnect();
		LOGGER.info("Successfully destroyed Channel(" + this.friendyName + ")!  Goodbye!");
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean poll() {
		//poll whether a message is available 
		return this.handler.poll();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Message pop()  {
		// pops a message off the message queue
		// returns null if queue is empty, caller must guard for NULL
		try {
			Message rm = this.handler.pop();
			//
				rm.unpackMsg(); //this should, if necessary, verify signature and decrypt payload msg
				//we can have this returning just the payload hashmap, pending further discussion re requirements.
				//so far so good, signed message is verified and encrypted message decrypted at this stage
				LOGGER.debug("Unpacked message, verified signature and decrypted payload as per secuirty requirement.");
				return rm;
		} catch (MessageException me) {
			// 
			LOGGER.error("Error unpacking message: " + me.getMessage());
			return null;
		}  
	}
	///////////////////////////////////////////instance methods///////////////////////////////////////
	/**
	 * Find the flag according to the provided {@link java.lang.Enum <em>Enum</em>} type
	 * <p>
	 * @param clazz	the required {@link java.lang.Enum <em>Enum</em>} type
	 * @param list	The {@link java.util.List <em>List</em>} to filter 
	 * @return	the retrieved object or null if the type is not found.
	 */
	public Enum<?> getFlag(Class<?> clazz, List<Enum<?>> list){
		return list.stream().filter(e -> clazz.isInstance(e)).findAny().orElse(null);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
		
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	

}

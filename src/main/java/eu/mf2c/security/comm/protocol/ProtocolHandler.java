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

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.Signature;
import java.time.Instant;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.crypto.Cipher;

import org.apache.log4j.Logger;

import eu.mf2c.security.comm.Receiver;
import eu.mf2c.security.comm.util.Protocol;
import eu.mf2c.security.comm.util.Security;
import eu.mf2c.security.data.Identity;
import eu.mf2c.security.data.Message;
import eu.mf2c.security.data.ReceivedMessage;
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
	/** friendly name of this Channel*/
	protected String friendlyName;
	//external services such as MQTT broker, remote key issuance service need to be discovered through NDP protocol
	/** broker address */
	protected String broker;
	/** Destination of the communication channel */
	protected String destination;	
	/** Broker&#39;s public key which is obtained in the initial handshade operation*/
	private PublicKey brokerPK;
	
	/** 
	 * Keepalive interval, the maximum number of seconds allowed between communications
	 * with the broker.  If no other messages are sent, the client will send a ping request
	 * at this interval 
	 * */
	protected int keepAlive;
	/** The ping acknowledgement queue attribute */ 
	protected ConcurrentLinkedQueue<ReceivedMessage> pingAckQ = new ConcurrentLinkedQueue<ReceivedMessage>(); //may need to block until something is in the buffer
	/** The ping request queue attribute */
	protected ConcurrentLinkedQueue<ReceivedMessage> pingReqQ = new ConcurrentLinkedQueue<ReceivedMessage>(); //may need to block until something is in the buffer
	/** The incoming message queue attribute */
	protected ConcurrentLinkedQueue<ReceivedMessage> msgQ = new ConcurrentLinkedQueue<ReceivedMessage>(); //may need to block until something is in the buffer
    //also need to check the removeAll operation is threadsafe
	/** Buffer for outgoing messages attribute  */
	protected ConcurrentLinkedQueue<Message> outMsgBuffer = new ConcurrentLinkedQueue<Message>();

	/** {@link Protocol <em>Protocol</em>} attribute */
	protected Protocol protocol;
	/** Connection status flag */
	protected boolean connack = false; 
	
	/**
	 * Default constructor
	 */
	public ProtocolHandler(){
		
	}
	/**
	 * Setter for the {@link #brokerPK <em>brokerPK</em>} attribute}
	 * <p>
	 * @param pk  the broker&#39;s {@link java.security.PublicKey <em>PublicKey</em>} object
	 */
	public void setBrokerPK(PublicKey pk){
		this.brokerPK = pk;
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
		if(msgQ.isEmpty()){
			return false;
		}else
			return true;
	}
	/**
	 * Pop an incoming message from the head of the incoming message queue. 
	 * Use this in a loop to get more than one message&#58;
	 * <p>
	 * <pre>
	 * 	while ((receivedMessage = queue.poll()) != null) { //do your processing...
	 * </pre>
	 * <p>
	 * @return the oldest {@link ReceivedMessage <em>ReceivedMessage</em>} 
	 * 		object in the queue or NULL if the head of the queue is empty
	 */
	public ReceivedMessage pop() {
		return msgQ.poll();
	}
	
	
	/**
	 * Initialise the channel, this includes subscribing#47;listening to all the correct inboxes,
	 * including the default built&#45;in ones according to the structure illustrated below&#58;
	 * <pre>
	 *  mf2c/[destination]/public
	 *	mf2c/[destination]/protected
	 *	mf2c/[destination]/private
	 *	mf2c/[destination]/public/pingreq
	 *	mf2c/[destination]/public/pingack
	 * </pre>
	 * It will also carry out the initial handshake and set up a pinger to periodically
	 * check if the mF2C destination is listening and alive.
	 * <p>
	 * @param properties
	 * @param receiver
	 * @throws {@link ProtocolHandlerException <em>ProtocolHandlerException</em>} on set up errors
	 */
	public void setup(Properties properties, Receiver receiver) throws ProtocolHandlerException{
		//protocol specific operations to be defined by the concrete classes
		friendlyName = (String) properties.get("friendlyName");
		broker = (String) properties.getProperty("broker");
		destination = (String) properties.getProperty("destination");
		keepAlive = Integer.valueOf((String) properties.getProperty("keepAlive"));
	}
	
	/*********************************************Utilities*********************************************************************/
	/**
	 * Complete metadata for the payload and enforce the security requirement. 
	 * No security is enforced for public messages, the metadata and payload message
	 * are key&#45;value elements in passed in {@link java.util.HashMap <em>HashMap</em>}.
	 * The payload is signed for protected content and a signature is added.
	 * The payload is encrypted for private content.  
	 * <p>
	 * @param entries	a {@link java.util.HashMap <em>HashMap</em>} of key values for input into the message
	 * @param secFlag	the security level applicable to the message.
	 * @return	the payload serialised into a byte array. ?????
	 * @throws ProtocolHandlerException if there are errors in fetching the public key, signing or encrypting the payload.
	 */
	public byte[] generatePayload(HashMap<String,String> entries, Security secFlag) throws ProtocolHandlerException{
		//Jens wants to use JOSE but there may be a size limit to the payload as normally the payload contains claims
		//8 Feb 18, we add timestamp (String unixTime) and source (String, agent friendly name)
				
		//generate unix timestamp as a String
		entries.put("timestamp", String.valueOf(Instant.now().getEpochSecond()));
		if(!entries.containsKey("source")){
			entries.put("ID", this.friendlyName);			
		}	
		//enforce security
		if(!secFlag.equals(Security.PUBLIC)){
			//needs to sign protected and private messages
			try{				
				//need to get the payload element and sign that
				if(entries.containsKey("payload") && !entries.get("payload").isEmpty()){
					entries.put("publicKey", Identity.getInstance().getPublicKey().toString());
					String signature = Identity.getInstance().signMessageAsString(entries.get("payload").getBytes());
					if(signature != null){
						entries.put("signature", signature); //add the signature for verifying the payload
					}else{
						LOGGER.error("Failed to generate signature!  Signature is null!");
						throw new ProtocolHandlerException("Failed to generate signature!  Signature is null!");
					}
					
				}else{//nothing to sign
					LOGGER.warn("There is no payload message to sign!");
					//TODO do we still go ahead????  let's go ahead for the moment
				}
				
				//byte[] b64Payload = Base64Helper.encodeToBytes(entries.toString());  //we serialise into base64 just before sending
				
			}catch(IdentityException ie){
				LOGGER.error("Error getting the public key: " + ie.getMessage());
				throw new ProtocolHandlerException(ie);
			}catch(Exception e){
				LOGGER.error("Error creating the payload: " + e.getMessage());
				if(e instanceof ProtocolHandlerException){
					throw e;
				}else{
					throw new ProtocolHandlerException(e);
				}
			}
		}
		if(secFlag.equals(Security.PRIVATE)){//private message, needs to encrypt payload with broker's public key
			//
			if(this.brokerPK == null){
				LOGGER.error("No broker public key, cannot encrypt message for " + this.destination + "!");
				throw new ProtocolHandlerException("No broker public key, cannot encrypt message for " + this.destination + "!");
			}
			//go ahead
			try{
				byte[] en_byte = encryptPayload(entries.get("payload"));
				entries.put("payload", new String(en_byte, StandardCharsets.UTF_8)); //replace the payload
			}catch(Exception e){
				LOGGER.error("Error tyring to encrypt payload using broker's key: " + e.getMessage());
				throw new ProtocolHandlerException(e);
			}
			
		}
		//dump hashmap as a JSON String and base 64 encode it
		
		
		
		
		
		
		//then convert to byte[]
		
		return null;
	
	}
	
	public String getSignature(KeyPair keypair) throws Exception {
		
		Signature signAlg;
			signAlg = Signature.getInstance("SHA256withRSA");		
			// we will initialize the crypto signature instance with the created private key
			signAlg.initSign(keypair.getPrivate());
			// we will load the data - this signature will computed from the base64 data, similar to JWS approach
			signAlg.update(keypair.getPublic().toString().getBytes());
			// and we simply create a signature
			return new String(signAlg.sign(), StandardCharsets.UTF_8);	
			// and base64 it for easy handling
			//Base64.Encoder b64encoder = Base64.getEncoder();
			//return b64encoder.encodeToString(result);
	}
	/**
	 * Encrypt the payload using the broker&#39;s {@link java.security.PublicKey <em>PublicKey</em>}
	 * The payload is encrypted using the RSA asymmetric key encryption method.
	 * <p> 
	 * @param payload   a {@link java.lang.String <em>String</em>} representation of the payload
	 * @return	the encrypted {@link java.lang.Byte <em>Byte</em>} array object.
	 * @throws Exception on any processing error
	 */
	public byte[] encryptPayload(String payload) throws Exception {
		//depends on requirements, we could also swap to encrypt with Identity object's private key, assuming recipients got
		//the reciprocal public key
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, this.brokerPK);
		return cipher.doFinal(payload.getBytes());
	}
	/**
	 * Decrypt the payload using broker&#39;s {@link java.security.PublicKey <em>PublicKey</em>}
	 * The payload  is encrypted using the RSA asymmetric key encryption method.
	 * <p>
	 * @param enc_string  a {@link java.lang.String <em>String</em>} representation of the encrypted payload
	 * @return	the decrypted payload {@link java.lang.String <em>String</em>}
	 * @throws Exception on any processing error
	 */
	public String decryptPayload(String enc_string) throws Exception{
		//assumming that we use the broker's public key to decrypt the message and assuming that it is RSA encrypted as in the
		//encryptPayload method
		//TODO need to redefine this
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, this.brokerPK);
		//the input string should be decoded from base64
		return new String(cipher.doFinal(enc_string.getBytes()), StandardCharsets.UTF_8); 	
	}
	
	
	
	
	
	
	
	
	
}

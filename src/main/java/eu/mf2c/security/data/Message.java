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
package eu.mf2c.security.data;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;

import javax.crypto.Cipher;

import net.minidev.json.JSONValue;
import net.minidev.json.parser.ParseException;

import org.apache.log4j.Logger;

import eu.mf2c.security.comm.util.Base64Helper;
import eu.mf2c.security.comm.util.Protocol;
import eu.mf2c.security.comm.util.QoS;
import eu.mf2c.security.comm.util.Security;
import eu.mf2c.security.exception.IdentityException;
import eu.mf2c.security.exception.MessageException;

/**
 * The Message object wraps the client message with sufficient metadata to be portable
 * and readable by heterogeneous devices with varying capabilities.  In this implementation,
 * the message is serialised as a base64 encoded representation of the {@link #payloadHM <em>payloadHM</em>} 
 * attribute serialised into a Json String.  We will move towards a more formal structure like a JOSE with separate 
 * header and payload sections in a later implementation.
 * <p>
 * In the current version, the {@link #payloadHM <em>payloadHM</em>} is populated with these key&#45;value pairs:
 * <ul>
 * <ui>timestamp&#58; channel publication timestamp</ui>
 * <ui>qos&#58; delivery quality of service represented as the ordinal of the QoS enum</ui>
 * <ui>sec&#58; security level applicable represented as the ordinal of the Security enum</ui>
 * <ui>pro&#58; transport protocol used represented as the ordinal of the Protocol enum</ui>
 * <ui>source&#58; friendly name of sending channel</ui>
 * <ui>payload&#58; the message content</ui>
 * <ui>signature&#58; the signature data if message is protected &#40;the payload is signed with the sender's private key&#41;</ui>
 * <ui>publicKey&#58; the sender&#58;s public key &#40;if message is signed&#41;</ui>
 * </ul>
 * <p>
 * @author Shirley Crompton
 * @email  shirley.crompton@stfc.ac.uk
 * @org Data Science and Technology Group,
 *      UKRI Science and Technology Council
 * @Created 16 Jan 2018
 *
 */
public class Message {
	/** logger attribute */
	private final static Logger LOGGER = Logger.getLogger(Message.class.getName());
	
	private boolean isIncoming = true; //default to incoming (received) message
	/** the full base64encoded message serialised as a byte array */
	private byte[] msgB64Bytes; 
	/** Receipt timestamp, represented as the number of seconds from the Java epoch of 1970-01-01T00:00:00Z */
	private long received_tmsp; //this is 0 it is a sent message
	/** The message unencoded from Base64 and represented as a HashMap */
	private HashMap<String, Object> payloadHM;	
	/** The recipient&#39;s public key attribute */ 
	private PublicKey destKey = null; //may need to swap to using keystore later on
	
	/**
	 * Construct an instant using the received message payload.  To minimise processing time, we will not
	 * process the payload.
	 * <p>
	 * @param msgPayload 	a {@link Java.lang.Byte <en>Byte</em>} array representation of the message payload.
	 * @param pk			the destination receipient&#39; {@link java.security.PubliKey <em>PubliKey</em>} or null
	 */
	public Message(byte[] msgPayload, PublicKey pk){ 
		//Dealing with a received message, we decrypt incoming msg using Identity's key
		this.msgB64Bytes = msgPayload;
		this.received_tmsp = Instant.now().getEpochSecond();	
		if(pk != null)
			this.destKey = pk;
	}
	/**
	 * Construct an instant of an outgoing message
	 * <p>
	 * @param payloadHM  a {@link Java.util.HashMap <em>HashMap</em>} representation
	 * 					of the message and metadata key values
	 */
	public Message(HashMap<String, Object> payloadHM/*, PublicKey pk*/){ //PK should be in the payload for the prototype
		//Dealing with instantiating a message for sending
		this.isIncoming = false;
		this.payloadHM.putAll(payloadHM);
//		if(pk != null){
//			this.destKey = pk; 
//		}else{
//			LOGGER.debug("Message objected instantiated without destination public key...");
//		}
		
	}
	/////////////////////////////getters	
	/**
	 * @return the {@link #isIncoming <em>isIncoming</em>} message flag.
	 */
	public boolean isIncoming() {
		return isIncoming;
	}

	/**
	 * Getter for the received_tmsp attribute.
	 * @return the received_tmsp attribute.
	 */
	public long getReceived_tmsp() {
		return received_tmsp;
	}
	
	/**
	 * Getter for the {@link #payloadHM <em>payload</em>} attribute
	 * @return the {@link #payloadHM <em>payload</em>} attribute
	 */
	public HashMap<String, Object> getPayloadHM() {
		return payloadHM;
	}	
	/**
	 * Getter for the  {@link #destKey <em>destKey</em>}&#58; the sender&#39;s public key
	 * @return the {@link #destKey <em>destKey</em>}&#58; attribute
	 */
	public PublicKey getDestKey() {
		return destKey;
	}
	////////////////////////instant methods
	/**
	 * Deserialise the received payload which is a Base64encoded {@link java.lang.String <em>String</em>}.
	 * If the payload is signed, the signature is verified against the sender&#39;s public key.
	 * If the payload is encrypted, it is decrypted using the owner&#39;s private key.
	 * <p>
	 * @throws MessageException on processing errors.
	 */
	@SuppressWarnings("unchecked")
	public void unpackMsg() throws MessageException{
		
		//assuming that it is an incoming msg
		if(!this.isIncoming ){
			LOGGER.error("This is not an incoming message!");
			throw new MessageException("This is not an incoming message!");
		}
		if(this.msgB64Bytes == null){
			LOGGER.error("This there is nothing to unpack!");
			throw new MessageException("This there is nothing to unpack!");
		}
        try{
        	this.payloadHM = (HashMap<String, Object>) JSONValue.parseStrict((Base64Helper.decodeToString(msgB64Bytes)));
        }catch (ParseException e) {
        	LOGGER.error("Parse error extracting the payload from byte[]: " + e.getMessage());
            throw new MessageException(e);
        } 
        //convert from ordinal to value
        Security secFlag = Security.values()[(int) this.payloadHM.get("security")];
        
        if(!secFlag.equals(Security.PUBLIC)){
        	try {
		    	//need to verify signature
		    	if(this.payloadHM.get("signature") == null || this.payloadHM.get("publicKey") == null || this.payloadHM.get("payload") == null){
		    		throw new Exception("Unable to verify the signature as the signature/payload/publicKey is null!");
		    	}
		    	//sender's public key always sent with the 
		    	this.destKey = this.convertPK((String) this.payloadHM.get("publicKey"));
		    	LOGGER.debug("About to verify signature using the accompanying public key....");
				if(!this.verifySignature((String) this.payloadHM.get("signature"), (String) this.payloadHM.get("payload"))){
					throw new Exception("mismatched signature on non-public payload!");
				}
				//now decrypt the payload
				if(secFlag.equals(Security.PRIVATE)){
					LOGGER.debug("About to decrypt payload using owner's private key....");
					this.payloadHM.put("decryptedPayload", (Identity.getInstance()).decryptPayload((String) this.payloadHM.get("payload")));
				}
			} catch (Exception e) {
				LOGGER.error("Error unpacking message: " + e.getMessage());
				throw new MessageException(e);
			}        	
        }
        LOGGER.debug("Unpacked payload");
	}

	
	//message parser methods (create and parse messages)
	//list of enum flags convert to int[] then unicode String to put into messageheader, and parser reverse th process
	//data encryption, compression
	////////////////////////////////////////////////////utilties////////////////////////////////////////////////
	/**
	 * Complete metadata for the payload and enforce the security requirement. 
	 * No security is enforced for public messages, the metadata and payload message
	 * are key&#45;value elements in passed in {@link java.util.HashMap <em>HashMap</em>}.
	 * The payload is signed using the owner&#39;s private key for protected content and a signature is added.
	 * The payload is encrypted using the recipient&#39;s public key for private content. 
	 * The timestamp is added just before publication. 
	 * <p>
	 * @param secFlag		the security level applicable to the message.
	 * @param protocolFlag	the transport protocol applicable used represented
	 * @param recipientPK	the recipient&#39;s public key object
	 * @return	a {@link java.util.HashMap <em>HashMap</em>} of metadata and processed payload. 
	 * @throws MessageException if there are errors in fetching the public key, signing or encrypting the payload.
	 */
	public void packMsg(Security secFlag, Protocol protocolFlag, QoS qosFlag, PublicKey recipientPK) throws MessageException{
		//Jens wants to use JOSE but there may be a size limit to the payload as normally the payload contains claims
		
		if(this.isIncoming){
			LOGGER.error("Can only generate payload for outgoing messages!");
			throw new MessageException("Can only generate payload for outgoing messages!");
		}
		if(this.payloadHM == null || this.payloadHM.isEmpty()){
			LOGGER.error("No key values hashmap to process!");
			throw new MessageException("No key values hashmap to process!");
		}
		if(recipientPK != null){
			this.destKey = recipientPK;
		}		
		//this.payload.put("source", this.friendlyName); the caller must populate this in the HashMap
		this.payloadHM.put("sec", secFlag.ordinal());
		this.payloadHM.put("pro", protocolFlag.ordinal());
		this.payloadHM.put("qos", qosFlag.ordinal());			
		//enforce security
		if(!secFlag.equals(Security.PUBLIC)){
			//needs to sign protected and private messages
			try{				
				//need to get the payload element and sign that using owner's private key
				if(this.payloadHM.containsKey("payload") && this.payloadHM.get("payload") != null && !((String) this.payloadHM.get("payload")).isEmpty()){
					if(this.payloadHM.get("publicKey") == null){ //might have been populated by the getStatusMessage method
						this.payloadHM.put("publicKey", Identity.getInstance().getPublicKey().toString());
					}							
					String signature = Identity.getInstance().signMessageAsString(((String) this.payloadHM.get("payload")).getBytes());
					if(signature != null){
						this.payloadHM.put("signature", signature); //add the signature for verifying the payload
					}else{
						LOGGER.error("Failed to generate signature!  Signature is null!");
						throw new MessageException("Failed to generate signature!  Signature is null!");
					}
				}else{//nothing to sign
					LOGGER.warn("There is no payload message to sign!");
					//TODO do we still go ahead????  let's go ahead for the moment
				}
			}catch(IdentityException ie){
				LOGGER.error("Error getting the public key: " + ie.getMessage());
				throw new MessageException(ie);
			}catch(Exception e){
				LOGGER.error("Error creating the payload: " + e.getMessage());
				if(e instanceof MessageException){
					throw e;
				}else{
					throw new MessageException(e);
				}
			}
		}
		if(secFlag.equals(Security.PRIVATE)){//private message, needs to encrypt payload with recipient's public key
			//
			if(this.destKey == null){
				LOGGER.error("No recipient's public key, cannot encrypt message!");
				throw new MessageException("No recipient's public key, cannot encrypt message!");
			}
			//go ahead
			try{
				byte[] en_byte = encryptPayload((String) this.payloadHM.get("payload"));
				this.payloadHM.put("payload", new String(en_byte, StandardCharsets.UTF_8)); //replace the payload
			}catch(Exception e){
				LOGGER.error("Error tyring to encrypt payload using recipient's public key: " + e.getMessage());
				throw new MessageException(e);
			}
		}
		//we can now turn the whole payloadHM into a Json String, then base64 encoded it
		//this.msgB64Bytes = Base64Helper.encodeToBytes(JSONValue.toJSONString(this.payloadHM));
	}
	/**
	 * Complete metadata for the payload and enforce the security requirement. 
	 * No security is enforced for public messages, the metadata and payload message
	 * are key&#45;value elements in passed in {@link java.util.HashMap <em>HashMap</em>}.
	 * The payload is signed for protected content and a signature is added.
	 * The payload is encrypted for private content. 
	 * The whole object is first converted into a JSON String, base64 encoded and then
	 * finally transformed into a {@link Java.lang.Byte <em>Byte</em>} object. 
	 * <p>
	 * @param entries	a {@link java.util.HashMap <em>HashMap</em>} of key values for input into the message
	 * @param secFlag	the security level applicable to the message.
	 * @return	the message serialised into a {@link Java.lang.Byte <em>Byte</em>} object.
	 * @throws ProtocolHandlerException if there are errors in fetching the public key, signing or encrypting the payload.
	
	public byte[] generatePayload(HashMap<String,Object> entries, Security secFlag) throws ProtocolHandlerException{
		//Jens wants to use JOSE but there may be a size limit to the payload as normally the payload contains claims
		//8 Feb 18, we add timestamp (String unixTime) and source (String, agent friendly name)
				
		//generate unix timestamp as a String
		entries.put("timestamp", Instant.now().getEpochSecond());
		if(!entries.containsKey("source")){
			entries.put("ID", this.friendlyName);			
		}	
		//enforce security
		if(!secFlag.equals(Security.PUBLIC)){
			//needs to sign protected and private messages
			try{				
				//need to get the payload element and sign that
				if(entries.containsKey("payload") && entries.get("payload") != null && !((String) entries.get("payload")).isEmpty()){
					entries.put("publicKey", Identity.getInstance().getPublicKey().toString());
					String signature = Identity.getInstance().signMessageAsString(((String) entries.get("payload")).getBytes());
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
				byte[] en_byte = encryptPayload((String) entries.get("payload"));
				entries.put("payload", new String(en_byte, StandardCharsets.UTF_8)); //replace the payload
			}catch(Exception e){
				LOGGER.error("Error tyring to encrypt payload using broker's key: " + e.getMessage());
				throw new ProtocolHandlerException(e);
			}
			
		}
		//we start with a HashMap<String, Object>  
		//we Base64 encode the JSON String
		return Base64Helper.encodeToBytes(JSONValue.toJSONString(entries));
	}	 */
	

	/**
	 * Decrypt the payload using the owner&#39;s {@link java.security.Private <em>Private</em>}
	 * The sender is the destination recipient.  The owner is the user of this {@link Channel <em>Channel</em>} object.
	 * The payload is encrypted using the RSA asymmetric key encryption method.
	 * <p>
	 * @param enc_string  a {@link java.lang.String <em>String</em>} representation of the encrypted payload
	 * @return	the decrypted payload {@link java.lang.String <em>String</em>}
	 * @throws Exception on any processing error
	
	public String decryptPayload(String enc_string) throws Exception{
		//In the prototype, we assume that the sender encrypted the payload using the owner's 
		//public key.  So we use the owner's private key to decrypt the payload
		//
		//encryptPayload method.  The incoming String should be base64 unencoded and deserialised from Json
		//TODO need to redefine this
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, this.destKey);
		//the input string should be decoded from base64
		return new String(cipher.doFinal(enc_string.getBytes()), StandardCharsets.UTF_8); 	
	} */
	/**
	 * Verify the integrity of the signed payload.
	 * <p>
	 * @param value		{@link java.lang.String <em>String</em>} representation of the signature.
	 * @param payload	a {@link java.lang.String <em>String</em>} representation of the payload, this should not be base64 encoded.
	 * @return			true if the signature is good, else false
	 * @throws Exception 	on any processing error
	 */
	public boolean verifySignature(String value, String payload) throws Exception{
		//
		Signature signAlg = Signature.getInstance("SHA256withRSA");
		signAlg.initVerify(this.destKey);
		signAlg.update(payload.getBytes()); //load the payload message
		return signAlg.verify(value.getBytes()); //load the signature and verify
	}
	/**
	 * Encrypt the payload using the recipient&#39;s {@link java.security.PublicKey <em>PublicKey</em>}
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
		cipher.init(Cipher.ENCRYPT_MODE, this.destKey);//Jen said this must be done with the recipient's public key 
		return cipher.doFinal(payload.getBytes());
	}   
	
	/**
	 * Convert a public key from {@link java.lang.String <em>String</em>} to {@link java.security.PublicKey <em>PublicKey</em>} format.
	 * <p>
	 * @param pkString	a {@link java.lang.String <em>String</em>} representation of the destination public key
	 * @return the converted {@link java.security.PublicKey <em>PublicKey</em>} object
	 * @throws Exception on conversion errors
	 */
	private PublicKey convertPK(String pkString) throws Exception{
		String publicKeyContent = pkString;
		publicKeyContent = publicKeyContent.replaceAll("\\n", "").replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");
		KeyFactory kf = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
        return (RSAPublicKey) kf.generatePublic(keySpecX509);	
	}
}

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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Random;

import org.apache.log4j.Logger;

import eu.mf2c.security.comm.Channel;
import eu.mf2c.security.exception.IdentityException;

/**
 * Identity of the mF2C agent or client application associated with the {@link Channel <em>Channel</em>} instance.
 *   
 * <p>  
 * @author Shirley Crompton
 * @email  shirley.crompton@stfc.ac.uk
 * @org Data Science and Technology Group,
 *      UKRI Science and Technology Council
 * @Created 9 Jan 2018
 *
 */
public class Identity {
	//we will generate identity on the fly for the moment, need to add PKI handshake later
	//will need to implement local keystore and methods for interacting with this
	
	//??? do we need it to be serializable? the getInstance approach needs to be amended to guarantee threadsafe....
	/** Message logger attribute */
	private final static Logger LOGGER = Logger
			.getLogger(Identity.class.getName());
	/** Device Id attribute */
	private byte[] deviceId = null; //32 bits
	/** RSA Keypair */
	KeyPair keyPair;
	
	
	/**
	 * Private constructor.  It calls the {@link #initID() <em>initID</em>} method
	 * to bootstrap the identity object.
	 * <p>
	 * @throws Exception  on errors in generating RSA keypair and random 
	 * 						device id. 
	 */
	private Identity() {
		LOGGER.debug("Creating an instance.");
		try{
			initID();
		}catch(Exception e){
			LOGGER.error("Failed to initialise the Identity Singleton : " + e.getMessage());
		}
	}
	
	/**
	 * Bootstrap the identity object.
	 * <p>
	 * @throws Exception  on errors in generating RSA keypair and random 
	 * 						device id. 
	 */
	private void initID() throws Exception {
		//
		LOGGER.debug("Initializing the identity instance....");
		//TODO just generate a RSA keypair for the moment, needs to call out to get it in later versions
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		//SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
		//keyGen.initialize(1024, random);
		keyGen.initialize(2048);
		this.keyPair = keyGen.generateKeyPair();//TODO randomly generates a device id for now, replace this
		//According to api spec, device id could be calculated locally
		this.deviceId = new byte[32];
		SecureRandom.getInstanceStrong().nextBytes(this.deviceId);
		//InetAddress ip = InetAddress.getLocalHost();
		//String myHostName = ip.getHostName();
	}
	/**
	 * Inner helper class to create the singleton instance of
	 * {@link eu.mf2c.security.data.Identity <em>Identity </em>}
	 *<p>
	 * @author Shirley Crompton
	 * @email  shirley.crompton@stfc.ac.uk
	 * @org Data Science and Technology Group,
	 *      UKRI Science and Technology Council
	 * @Created 12 Jan 2018
	 *
	 */
	private static class SingletonHelper {
		//lazy loading, guarantee single INSTANCE per class loader, but this variable cannot be changed	
		private static final Identity INSTANCE = new Identity();
	}
	
	
	/** 
	 * Get an instance.
	 * The identity and device id is generated on initiation 
	 * for the current implementation. 
	 * <p>
	 * return 	An {@link @Identity <em>Identity</em>} instance	or
	 *          null if there are errors in bootstrapping the object
	 * */
	public static Identity getInstance() throws IdentityException {
		LOGGER.debug("About to return an instance.");
		if(SingletonHelper.INSTANCE == null){
			throw new IdentityException("Failed to get the Identity singleton object!");
		}else{
			return SingletonHelper.INSTANCE;
		}
		
	}
	/** 
	 * Getter for the {@link #deviceId} attribute
	 * @return    the {@link #deviceId} attribute
	 * */
	public byte[] getDeviceId() throws UnknownHostException{
		//TODO need to clarify what is a DeviceId .....
		return this.deviceId;
	}
	/**
	 * Getter for the RSA public key of this identity object.
	 * <p>
	 * @return the public key object.
	 */
	public PublicKey getPublicKey(){
		return this.keyPair.getPublic();
	}
	
	//method for signing message????
	
}

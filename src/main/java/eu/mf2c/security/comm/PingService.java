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
import eu.mf2c.security.exception.PingServiceException;


/**
 * Protocol ping service 
 * <p>
 * @author Shirley Crompton
 * @email  shirley.crompton@stfc.ac.uk
 * @org Data Science and Technology Group,
 *      UKRI Science and Technology Council
 * @Created 16 Jan 2018
 *
 */
public class PingService implements Runnable{
	/** Logger attribute */
	private static final Logger LOGGER = Logger.getLogger(PingService.class.getName());
	//Listener listener;
	protected long lastPing;
	/** flag indicating state of the thread */
	private boolean keepRunning = false;
	/** ping interval in seconds **/
	private int interval;
	/** protocol handler attribute */ 
	private Listener listener;
	/** ping request flag. Ping request is send if set to false  */
	private boolean noPing = false;
	/** last time Listener
	
	
	/**
	 * Construct an instance.
	 * <p>
	 * @param pingInterval  the ping interval in seconds
	 * @param handler		the {@link ProtocolHanlder <em>ProtocolHandler</em>} for this communication {@link Channel <em>Channel</em>} 
	 * @parm noPing			set to false if no ping request should be sent
	 */
	public PingService(int pingInterval, Listener listener, boolean noPing){
		LOGGER.info("Setting ping service ping interval to : " + pingInterval + " on an " + listener.getHandler().getProtocol() + " client");
		this.listener = listener;
		this.interval = pingInterval;
		this.noPing = noPing;
	}
	
	protected void setKeepRunning(boolean state){
		LOGGER.debug("Setting PingService thread status to : " + state);
		this.keepRunning = state;
	}
	/**
	 * Ping the target destination. 
	 * <p>
	 * @throws PingServiceException
	 */
	public void ping() throws PingServiceException{
		//in the botch version, we don't ping if there is no target destination!!!!
		//
		while(keepRunning){
			try {
				Message msg = new Message((HashMap<String, Object>) this.listener.getHandler().getPingMessage(null)); 
				msg.packMsg( Security.PUBLIC, this.listener.getHandler().getProtocol(), QoS.EXACTLYONCE, null);
				this.listener.getHandler().publish(this.listener.getHandler().getPingRequestDest(), QoS.EXACTLYONCE, msg.getPayloadHM());
				
			} catch (Exception e) {
				// 
				LOGGER.error("Ping error : " + e.getMessage());
				throw new PingServiceException(e);
			}
		}
		
		//TODO need to refine the logic, e.g. if ping is timed out, needs to update status flag
	}
	
	public int respond(){
		
		//creates a ping acknowledgement message and sends it
		//The listener is doing the ping acknowledgement
		return 0;	
	}
	
	/**
	 * Obtain a new {@link java.lang.Thread <em>Thread</em>} wrapping the
	 * {@link Listener <em>Listener</em>} 
	 * <p>
	 * @return the new {@link java.lang.Thread <em>Thread</em>} object
	 */
	public Thread getNewThread(){
		return new Thread(this.listener,"listener");
	}
	
	
	/**
	 * Run the PingService as a thread.  Also launch a {@link Listener <em>Listener</em>} 
	 * {@link java.lang.Thread <em>Thread</em>} to process the incoming ping acknowledgements
	 * and ping requests.  We do not launch a new {@link Listener <em>Listener</em>} {@link java.lang.Thread <em>Thread</em>}
	 * if the previous one is still active.
	 */
	@Override
	public void run() {
		LOGGER.debug("Setting PingService thread status to true before running thread");
		this.keepRunning = true;
		//

		//need another thread to listen to the arriving ping acknowledgement, request.....
		while(keepRunning){
			try{
				long waitTime = Instant.now().getEpochSecond() - this.lastPing;
				if(!noPing){
					if(waitTime <=0){
						this.ping();
					}else{
						Thread.sleep(waitTime * 1000); // sleep is in milliseconds
						this.ping();
					}
				}
				//launch a thread to run the listener to acknowledge pings
				Thread listenerThread = getNewThread();
				if(listenerThread.getState().equals(Thread.State.TERMINATED)){
					listenerThread.start();
				}else{
					LOGGER.debug("Not starting new listener thread, existing one not yet terminated....");
				}
				Thread.sleep(this.interval * 1000); //sleep is in milliseconds
			}catch(InterruptedException e){
				if(keepRunning){
					LOGGER.error("InterruptedException : " + e.getMessage());
				}else{
					LOGGER.info("PingService interrupted : " + e.getMessage());
				}
				
			} catch (PingServiceException e) {
				LOGGER.error("Ping Exception : " + e.getMessage());
			}
			
		}
		LOGGER.warn("PingService thread stopping.....");
	}
	//////////////////////////////////////////////////////utilities////////////////////////////////////////////////////
	
	
}

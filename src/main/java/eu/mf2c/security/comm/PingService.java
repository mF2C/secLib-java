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
import java.util.TimerTask;

import org.apache.log4j.Logger;


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
	protected long currentTime;
	/** flag indicating state of the thread */
	private boolean keepRunning = false;
	/** ping interval in seconds **/
	private int interval;
	
	
	
	/**
	 * !!!!!!!!
	 * This class periodically ping the target to ensure that the connection is alive
	 * According to class diagram, this has a ref to the listener class 
	 * 
	 * needs a timer thread to do the periodic ping......
	 * if ping req is timed out, needs to flag that service is interrupted to ProtocolHandler.isConnack to let it cache outgoing messages
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @return
	 */
	
	public PingService(int pingInterval){
		LOGGER.info("Setting ping service ping interval to : " + pingInterval);		
		this.interval = pingInterval;
	}
	
	protected void setKeepRunning(boolean state){
		LOGGER.debug("Setting PingService thread status to : " + state);
		this.keepRunning = state;
	}

	public int ping(){
		
		//if ping is timed out, needs to update status flag
		//unix timestamp in milliseconds
		long currentTime = Instant.now().getEpochSecond();
		//create ping message
		//send ping message using Channel.send????? then we don't need to know about the protocol client????
		//replace last ping timestamp
		
		
		
		
		
		return 0;
		
	}
	
	public int respond(){
		
		//creates a ping acknowledgement message and sends it
		//or should we let listener do this???????
		return 0;	}
	
	/**
	 * Run the PingService as a thread.
	 */
	@Override
	public void run() {
		LOGGER.debug("Setting PingService thread status to true before running thread");
		this.keepRunning = true;
		//
		while(keepRunning){
		
			try{
				this.ping();
				Thread.sleep(10000); //sleeps for ten seconds or for the required interval
			}catch(InterruptedException e){
				if(keepRunning){
					LOGGER.error("InterruptedException : " + e.getMessage());
				}else{
					LOGGER.info("PingService interruptedException : " + e.getMessage());
				}
				
			}
			
		}
		LOGGER.warn("PingService thread stopped.....");
	}
	
}

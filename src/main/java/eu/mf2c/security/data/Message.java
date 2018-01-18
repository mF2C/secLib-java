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

import org.apache.log4j.Logger;

import com.eclipsesource.json.JsonObject;

import eu.mf2c.security.comm.Channel;

/**
 * The Message object is formatted in JSON with sufficient metadata to be portable
 * and readable by heterogeneous devices with varying capabilities.
 * <p>
 * An example is given below&#58;
 * <pre>
 * {"version":"mf2c-01",				
 *  "time_sent":"20170912T094215Z",		Sender UTC timestamp (time acually sent)
 *  "sender_id":"XYZ",					TBD
 *  "friendlyname":"agent status",
 *  "flags":{
 *  		"sec":1,
 *          "qos":1,
 *          "pri":1,
 *          "pro":1},					Enum flags communicated as key-values
 *  "data": [
 *          {
 *          "length":1024,
 *          "sec":something,
 *          "cksum":"abcdef12",			Optional, an be left out, or set to null
 *          "cksumtype":"adler"}
 *          ]
 * }
 * </pre>
 * The Message header may be signed, which would also communicate the sender&#39;s cryptographic id.
 * The offset &#40;zero&#41; of the first data blob indicates the first byte following the last &#34;&#125;&#34; 
 * of the header. 
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
	
	public Message(JsonObject message){
		//Dealing with a received message, decrypt the JsonObject into a message object
		//??what does a ping acknowledge looks like??  Do we need to differentiate a message and ping ack?????
		
	}
	
	public Message(){
		//Dealing with instantiating a message for sending
	}
	
	//message parser methods (create and parse messages)
	//list of enum flags convert to int[] then unicode String to put into messageheader, and parser reverse th process
	//data encryption, compression
}

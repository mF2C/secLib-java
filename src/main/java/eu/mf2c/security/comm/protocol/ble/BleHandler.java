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
package eu.mf2c.security.comm.protocol.ble;

import java.util.HashMap;

import eu.mf2c.security.comm.protocol.ProtocolHandler;
import eu.mf2c.security.comm.util.QoS;
import eu.mf2c.security.comm.util.Security;
import eu.mf2c.security.data.Message;
import eu.mf2c.security.exception.ProtocolHandlerException;

/**
 * @author Shirley Crompton
 * @email  shirley.crompton@stfc.ac.uk
 * @org Data Science and Technology Group,
 *      UKRI Science and Technology Council
 * @Created 16 Jan 2018
 *
 */
public class BleHandler extends ProtocolHandler {

	@Override
	public Message pop() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public HashMap<String, Object> getStatusMessage(String status)
			throws ProtocolHandlerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void publish(String topicName, QoS qos,
			HashMap<String, Object> payload) throws ProtocolHandlerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void flush() throws ProtocolHandlerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public HashMap<String, Object> getPingMessage(Object reqTS)
			throws ProtocolHandlerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPingRequestDest() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPingAckDest(String target) {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void setup(HashMap<String, String> properties) throws ProtocolHandlerException {
		//TODO
	}

	@Override
	public String getDestination(Security sec) {
		// TODO Auto-generated method stub
		return null;
	}
}

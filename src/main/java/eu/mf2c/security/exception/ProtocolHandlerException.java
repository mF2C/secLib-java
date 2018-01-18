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
package eu.mf2c.security.exception;

/**
 * Errors associated with the {@link eu.mf2c.security.comm.ProtocolHandler <em>ProtocolHandler </em>} object,
 * including its instantiation by the factory method 
 * {@link eu.mf2c.security.comm.util.Protocol.ProtocolHandlerFactory <em>ProtocolHandlerFactory</em>}
 * <p>
 * @author Shirley Crompton
 * @email  shirley.crompton@stfc.ac.uk
 * @org Data Science and Technology Group,
 *      UKRI Science and Technology Council
 * @Created 12 Jan 2018
 *
 */
public class ProtocolHandlerException extends Exception {
	/**
	 * unique identifier of this error
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Create an instance with a specific error message.
	 * 
	 * @param message Error message to include
	 */
	public ProtocolHandlerException(String message) {
		super(message);
	}

	/**
	 * Create an instance with a specific error message and the
	 * {@link Throwable} cause.
	 * 
	 * @param message error message {@link String}
	 * @param cause {@link Throwable cause}
	 */
	public ProtocolHandlerException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create an instance with a specific {@link Throwable} cause.
	 * 
	 * @param cause {@link Throwable} cause
	 */
	public ProtocolHandlerException(Throwable cause) {
		super(cause);
	}

}

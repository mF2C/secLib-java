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
package eu.mf2c.security.comm.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 
 * Helper for base64 encoding and decoding plain {@link java.lang.String <em>String</em>}.
 * <p>
 * @author Shirley Crompton
 * @email  shirley.crompton@stfc.ac.uk
 * @org Data Science and Technology Group,
 *      UKRI Science and Technology Council
 * @Created 8 Feb 2018
 *
 */
public class Base64Helper {
	/**
	 * Base64 encode an input String 
	 * <p>
	 * @param input a {@link java.lang.String <em>String</em>}
	 * @return a {@link java.lang.Byte <em>Byte</em>} representation of the base64 encoded 
	 * 		input {@link java.lang.String <em>String</em>}
	 */
	public static byte[] encodeToBytes(String input){
		
		Base64.Encoder encoder = Base64.getEncoder();
		return encoder.encode(input.getBytes());
		
	}
	/**
	 * Base64 encode an input String 
	 * <p>
	 * @param input a {@link java.lang.String <em>String</em>}
	 * @return the base64 encoded {@link java.lang.String <em>String</em>}
	 */
	public static String encodeToSstring(String input){
		Base64.Encoder encoder = Base64.getEncoder();
		return encoder.encodeToString(input.getBytes());
	}
	/**
	 * Decode a base64 encoded {@link java.lang.String <em>String</em>}
	 * <p>
	 * @param b64String	the base64 encoded {@link java.lang.String <em>String</em>}
	 * @return	a {@link java.lang.Byte <em>Byte</em>} representation of the decoded input
	 * 		{@link java.lang.String <em>String</em>}
	 */
	public static byte[] decodeToBytes(String b64String){
		Base64.Decoder decoder = Base64.getDecoder();
		return decoder.decode(b64String);
	}
	/**
	 * Decode a {@link java.lang.Byte <em>Byte</em>} representation of the base64 encoded
	 * {@link java.lang.String <em>String</em>}
	 * <p>
	 * @param b64Bytes the input in {@link java.lang.Byte <em>Byte</em>} 
	 * @return  the decoded {@link java.lang.String <em>String</em>}
	 */
	public static String decodeToString(byte[] b64Bytes){
		Base64.Decoder decoder = Base64.getDecoder();
		return new String(decoder.decode(b64Bytes), StandardCharsets.UTF_8);
	}
	/**
	 * @param args
	 
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}*/

}

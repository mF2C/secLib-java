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

/**
 * An {@link java.lang.Enum Enum <em>Enum<em>} of communication protocols supported in mF2C.
 * Based on the prototype created by Cheney Ketley.
 * In Iteration 1, mF2C only supports MQTT, HTTP and BLE.
 * <p>
 * @author Shirley Crompton
 * @email  shirley.crompton@stfc.ac.uk
 * @org Data Science and Technology Group,
 *      UKRI Science and Technology Council
 * @Created 9 Jan 2018
 *
 */
public enum Protocol {
	/** MQTT messaging protocol */
	MQTT,
	/** Hypertext Transfer communication protocol */
    HTTP,
    /** Bluetooth Low Energy communication protocol */
    BLE,
    /** Bluetooth communication protocol */
    B,
    /** LORA communication protocol */
    LORA,
    /** LORA Wide Area Network communication protocol */
    LORAWAN,
    /** COAP communication protocol */
    COAP,
    /** ZIGBEE communication protocol */
    ZIGBEE,
    /** SIGFOX communication protocol */
    SIGFOX,
    /** DDS communication protocol */
    DDS,
    /** IPv6 Low-Power Wireless Personal Area Network communication protocol */
    SIXLOWPAN,
    /** Threaded communication */
    THREAD,
    /** HaLow communication protocol */
    HALOW,
    /** 2G mobile communication protocol */
    TWO_G,
    /** 3G mobile communication protocol */
    THREE_G,
    /** 4G mobile communication protocol */
    FOUR_G,
    /** LTE Category 0 communication protocol */
    LTECAT0,
    /** LTE Category 1 communication protocol */
    LTECAT1,
    /** LTE Category 3 communication protocol */
    LTECAT3,
    /** ZWAVE wireless communication protocol */
    ZWAVE,
    /** LTE-M1 communication protocol */
    LTEM1,
    /** NBIOT communication protocol */
    NBIOT,
    /** Near Field communication protocol */
    NFC,
    /** Radio-frequency Identity communication protocol */
    RFID,
    /** DIGIMESH communication protocol */
    DIGIMESH,
    /** INGENU communication protocol */
    INGENU,
    /** WEIGHTLESSN wireless communication protocol */
    WEIGHTLESSN,
    /** WEIGHTLESSP wireless communication protocol */
    WEIGHTLESSP,
    /** WEIGHTLESSW wireless communication protocol */
    WEIGHTLESSW,
    /** ANT wireless communication protocol */
    ANT,
    /** ANTPLUS wireless communication protocol */
    ANTPLUS,
    /** MIWI communication protocol */
    MIWI,
    /** ENOCEAN communication protocol */
    ENOCEAN,
    /** DASH7 communication protocol */
    DASH7,
    /** WIERLESSHART communication protocol */
    WIRELESSHART,
    /** Remote Procedure Call protocol */
    RPC,
    /** KAFKA messaging protocol */
    KAFKA,
    /** Java messaging Service protocol */
    JMS,
    /** AMQP messaging protocol */
    AMQP,
    /** Rabit MQ messaging protocol */
    RABBITMQ,
    /** ActiveMQ MQ messaging protocol */
    ACTIVEMQ,
    /** ZeroMQ messaging protocol */
    ZEROMQ,
    /** ICE instant messaging protocol */
    ICE,
    /** Common Object Request Broker communication protocol */
    CORBA,
    /** Apache Thrift communication protocol */
    THRIFT,
    /** Google PBuff communication protocol */
    GOOGLE_PBUFF,
    /** Zookeeper communication protocol */
    ZOOKEEPER,
    /** GO communication protocol */
    GO,
    /*
    PLACEHOLDER_08,
    PLACEHOLDER_09,
    PLACEHOLDER_10,
    PLACEHOLDER_11,
    PLACEHOLDER_12,
    PLACEHOLDER_13,
    PLACEHOLDER_14,
    PLACEHOLDER_15,
    PLACEHOLDER_16,
    PLACEHOLDER_17,
    PLACEHOLDER_18,
    PLACEHOLDER_19,
    PLACEHOLDER_20,
    PLACEHOLDER_21,
    PLACEHOLDER_22,
    PLACEHOLDER_23,
    PLACEHOLDER_24,
    PLACEHOLDER_25,
    PLACEHOLDER_26,
    PLACEHOLDER_27,
    PLACEHOLDER_28,
    PLACEHOLDER_29,
    PLACEHOLDER_30,
    */
    /** Network bridge communication */
    BRIDGE,
    /** Internet Protocol communication */ 
    IP_ONLY;
    /** None 
    NONE;	*/
	
}

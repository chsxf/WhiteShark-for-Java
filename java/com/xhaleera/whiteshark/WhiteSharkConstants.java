package com.xhaleera.whiteshark;

import java.nio.ByteOrder;

/**
 * WhiteShark constants
 * 
 * @author Christophe SAUVEUR (christophe@xhaleera.com)
 * @since 1.0
 * @version 1.0
 */
public final class WhiteSharkConstants {

	/**
	 * Format identifier
	 * <p>
	 * First four bytes of a valid WhiteShark stream. 
	 */
	public static final String FORMAT_IDENTIFIER = "WSFI";
	
	/** Format version */
	public static final short VERSION = 1;

	/** Format byte order */
	public static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
	
	/** Map property name */
	public static final String MAP_PROPERTY_NAME_PREFIX = ":m:";
	public static final String COLLECTION_ITEM_PROPERTY_NAME = ":ci:";
	
	// Format options
	/** Default option set */
	public static final short OPTIONS_DEFAULT 						= 0x0000;
	/** Options to serialize objects as generic class-less objects */
	public static final short OPTIONS_OBJECTS_AS_GENERICS 			= 0x0001;
}

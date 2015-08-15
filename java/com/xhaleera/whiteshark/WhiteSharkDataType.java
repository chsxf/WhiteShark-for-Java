package com.xhaleera.whiteshark;

/**
 * Enum containing data types stored by the WhiteShark serialization format.
 * 
 * @author Christophe SAUVEUR (christophe@xhaleera.com)
 * @since 1.0
 * @version 1.0
 */
enum WhiteSharkDataType {
	/** Identifies a property */
	PROPERTY	( (byte) 8 ),
	/** Identifies an object */
	OBJECT		( (byte) 7 ),
	/** Identifies an array */
	ARRAY		( (byte) 6 ),
	/** Identifies an floating-point number (single or double precision) */
	REAL		( (byte) 5 ),
	/** Identifies a string */
	STRING		( (byte) 4 ),
	/** Identifies a character */
	CHAR		( (byte) 3 ),
	/** Identifies an integer (up to signed 64 bits) */
	INTEGER		( (byte) 2 ),
	/** Identifies a boolean */
	BOOLEAN		( (byte) 1 ),
	/** Identifies a null value */
	NULL		( (byte) 0 );
	
	/** Binary mask value of the data type */
	private byte mask;
	
	/**
	 * Constructor
	 * @param mask Binary mask value
	 */
	private WhiteSharkDataType(byte mask) {
		this.mask = mask;
	}
	
	/**
	 * Retrieved the binary mask value of the data type
	 * @return a byte containing the binary mask value
	 */
	public final byte getMask() {
		return mask;
	}
}

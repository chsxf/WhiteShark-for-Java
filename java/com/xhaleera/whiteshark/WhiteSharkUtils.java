package com.xhaleera.whiteshark;

import java.nio.ByteBuffer;

import com.xhaleera.whiteshark.annotations.WhiteSharkSerializationVersion;

/**
 * Utility class
 * 
 * @author Christophe SAUVEUR (christophe@xhaleera.com)
 * @since 1.0
 * @version 1.0
 */
final class WhiteSharkUtils {

	/**
	 * Sanitizes a stream identifier
	 * <p>
	 * Stream identifiers are used to identify the content of a specific stream.
	 * It is stored in the stream header, just after the constant format identifier.
	 * <p>
	 * If the input identifier is longer than four bytes, it is trimmed to exactly four bytes.
	 * If the input identifier is lesser than four bytes, white spaces are padded to the right up to four bytes.
	 * <p>
	 * The identifier must be composed only of US-ASCII letters and digits.
	 * Any non-compliant character is ignored.  
	 * 
	 * @param identifier Unfiltered identifier to sanitize
	 * @return the sanitized four-byte identifier
	 */
	public static String sanitizeIdentifier(String identifier) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 4 && i < identifier.length(); i++) {
			Character c = identifier.charAt(i);
			if (c.charValue() > 127 || !Character.isLetterOrDigit(c))
				continue;
			sb.append(c);
		}
		return String.format("%-4s", sb.toString());
	}
	
	/**
	 * Wraps a byte array into a ByteBuffer instance
	 * 
	 * @param b the byte array to wrap
	 * @return a ByteBuffer instance, with byte order correctly set, wrapping the input byte array.
	 */
	public static ByteBuffer wrapWithByteBuffer(byte[] b) {
		ByteBuffer buf = ByteBuffer.wrap(b);
		buf.order(WhiteSharkConstants.BYTE_ORDER);
		return buf;
	}
	
	/**
	 * Allocates a ByteBuffer instance
	 * 
	 * @param capacity Capacity of the ByteBuffer
	 * @return a ByteBuffer instance, with byte order and capacity correctly set.
	 */
	public static ByteBuffer allocateByteBuffer(int capacity) {
		ByteBuffer buf = ByteBuffer.allocate(capacity);
		buf.order(WhiteSharkConstants.BYTE_ORDER);
		return buf;
	}

	/**
	 * Get a class for its name.
	 * 
	 * @param name Class name
	 * @return a Class instance
	 * @throws ClassNotFoundException If the search class name does not match any class.
	 */
	public static Class<?> classForName(String name) throws ClassNotFoundException {
		if (name.equals("boolean"))
			return boolean.class;
		else if (name.equals("byte"))
			return byte.class;
		else if (name.equals("short"))
			return short.class;
		else if (name.equals("char"))
			return char.class;
		else if (name.equals("int"))
			return int.class;
		else if (name.equals("long"))
			return long.class;
		else if (name.equals("float"))
			return float.class;
		else if (name.equals("double"))
			return double.class;
		else
			return Class.forName(name);
	}
	
	/**
	 * Tells if the option flags contains a specific option
	 * 
	 * @param options Option flags
	 * @param option Specific option to check
	 * @return
	 */
	public static boolean hasOption(short options, short option) {
		return ((options & option) != 0);
	}
	
	/**
	 * Gets the serialization version of a class from its {@link WhiteSharkSerializationVersion} annotation.
	 * 
	 * @param cls Source class 
	 * @return the annotated serialization version of the class, or {@link WhiteSharkConstants#DEFAULT_SERIALIZATION_VERSION} if no version is provided.
	 */
	public static int getSerializationVersion(Class<?> cls) {
		WhiteSharkSerializationVersion serializationVersionAnnotation = cls.getAnnotation(WhiteSharkSerializationVersion.class);
		if (serializationVersionAnnotation != null)
			return serializationVersionAnnotation.value();
		else
			return WhiteSharkConstants.DEFAULT_SERIALIZATION_VERSION;
	}
	
}

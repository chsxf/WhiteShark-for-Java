package com.xhaleera.whiteshark;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Vector;

import com.xhaleera.whiteshark.annotations.WhiteSharkAsGenerics;
import com.xhaleera.whiteshark.annotations.WhiteSharkSerializable;
import com.xhaleera.whiteshark.annotations.WhiteSharkSerializableMap;

/**
 * WhiteShark serializer class
 * 
 * @author Christophe SAUVEUR <christophe@xhaleera.com>
 * @since 1.0
 * @version 1.0
 */
public class WhiteSharkSerializer {

	private static Vector<Class<?>> classDictionary = new Vector<>();
	private static Vector<String> propertyDictionary = new Vector<>();
	
	/**
	 * Serializes an object using WhiteShark serialization format
	 * 
	 * @param identifier Custom WhiteShark stream identifier. This identifier is sanitized to a four-byte identifier.
	 * @param stream Destination stream
	 * @param obj Object to serialize
	 * @throws IOException
	 * @throws IllegalAccessException
	 */
	public static void serialize(String identifier, OutputStream stream, Object obj) throws IOException, IllegalAccessException {
		serialize(identifier, stream, obj, WhiteSharkConstants.OPTIONS_DEFAULT);
	}
	
	/**
	 * Serializes an object using WhiteShark serialization format
	 * 
	 * @param identifier Custom WhiteShark stream identifier. This identifier is sanitized to a four-byte identifier.
	 * @param stream Destination stream
	 * @param obj Object to serialize
	 * @param options Serialization options
	 * @throws IOException
	 * @throws IllegalAccessException
	 */
	public static void serialize(String identifier, OutputStream stream, Object obj, short options) throws IOException, IllegalAccessException {
		classDictionary.clear();
		propertyDictionary.clear();
		
		identifier = WhiteSharkUtils.sanitizeIdentifier(identifier);
		
		ByteBuffer buffer = WhiteSharkUtils.allocateByteBuffer(12);
		buffer.put(WhiteSharkConstants.FORMAT_IDENTIFIER.getBytes("US-ASCII"));
		buffer.put(identifier.getBytes("US-ASCII"), 0, 4);
		buffer.putShort(WhiteSharkConstants.VERSION);
		buffer.putShort(options);
		stream.write(buffer.array());
		
		serialize(stream, obj, options);
	}
	
	/**
	 * Generic serialization method
	 * 
	 * @param stream Destination stream
	 * @param obj Object to serialize
	 * @params options Serialization options
	 * @throws IOException
	 * @throws IllegalAccessException
	 */
	private static void serialize(OutputStream stream, Object obj, short options) throws IOException, IllegalAccessException {
		if (obj == null)
			serializeNull(stream, options);
		
		else if (obj instanceof String)
			serializeString(stream, obj.toString(), options);
		
		else if (obj instanceof Character)
			serializeCharacter(stream, (Character) obj, options); 
		
		else if (obj instanceof Number)
			serializeNumber(stream, (Number) obj, options);
		
		else if (obj instanceof Boolean)
			serializeBoolean(stream, (Boolean) obj, options);
		
		else if (obj.getClass().isArray())
			serializeArray(stream, obj, options);
		
		else
			serializeObject(stream, obj, options);
	}
	
	/**
	 * Serializes a null value
	 * @param stream Destination stream
	 * @params options Serialization options
	 * @throws IOException
	 */
	private static void serializeNull(OutputStream stream, short options) throws IOException {
		stream.write(WhiteSharkDataType.NULL.getMask());
	}
	
	/**
	 * Serializes a string
	 * @param stream Destination stream
	 * @param str String to serialize
	 * @params options Serialization options
	 * @throws IOException
	 */
	private static void serializeString(OutputStream stream, String str, short options) throws IOException {
		byte[] stringBytes = str.getBytes("UTF-8");
		
		byte mask = WhiteSharkDataType.STRING.getMask();
		
		int length = stringBytes.length;
		int lengthByteCount;
		if (length < Byte.MAX_VALUE)
			lengthByteCount = 1;
		else if (length < Short.MAX_VALUE)
			lengthByteCount = 2;
		else
			lengthByteCount = 4;
		
		mask |= ((byte) lengthByteCount) << 4;
		length += 1 + lengthByteCount;
		ByteBuffer buf = WhiteSharkUtils.allocateByteBuffer(length);
		buf.put(mask);
		switch (lengthByteCount) {
		case 1:
			buf.put((byte) stringBytes.length);
			break;
		case 2:
			buf.putShort((short) stringBytes.length);
			break;
		default:
			buf.putInt(stringBytes.length);
			break;
		}
		buf.put(stringBytes);
		
		stream.write(buf.array());
	}
	
	/**
	 * Serializes a character
	 * @param stream Destination stream
	 * @param character Character to serialize
	 * @params options Serialization options
	 * @throws IOException
	 */
	private static void serializeCharacter(OutputStream stream, Character character, short options) throws IOException {
		ByteBuffer buf = WhiteSharkUtils.allocateByteBuffer(3);
		buf.put(WhiteSharkDataType.CHAR.getMask());
		buf.putChar(character);
		stream.write(buf.array());
	}
	
	/**
	 * Serializes a Number
	 * @param stream Destination stream
	 * @param number Number to serialize
	 * @params options Serialization options
	 * @throws IOException
	 */
	private static void serializeNumber(OutputStream stream, Number number, short options) throws IOException {
		ByteBuffer buf = null;
		
		if (number instanceof Byte) {
			buf = WhiteSharkUtils.allocateByteBuffer(2);
			buf.put((byte) (WhiteSharkDataType.INTEGER.getMask() | 0x10));
			buf.put(number.byteValue());
		}
		else if (number instanceof Short) {
			buf = WhiteSharkUtils.allocateByteBuffer(3);
			buf.put((byte) (WhiteSharkDataType.INTEGER.getMask() | 0x20));
			buf.putShort(number.shortValue());
		}
		else if (number instanceof Integer) {
			buf = WhiteSharkUtils.allocateByteBuffer(5);
			buf.put((byte) (WhiteSharkDataType.INTEGER.getMask() | 0x40));
			buf.putInt(number.intValue());
		}
		else if (number instanceof Long) {
			buf = WhiteSharkUtils.allocateByteBuffer(9);
			buf.put((byte) (WhiteSharkDataType.INTEGER.getMask() | 0x80));
			buf.putLong(number.longValue());
		}
		
		else if (number instanceof Float) {
			buf = WhiteSharkUtils.allocateByteBuffer(5);
			buf.put(WhiteSharkDataType.REAL.getMask());
			buf.putFloat(number.floatValue());
		}
		else if (number instanceof Double) {
			buf = WhiteSharkUtils.allocateByteBuffer(9);
			buf.put((byte) (WhiteSharkDataType.REAL.getMask() | 0x10));
			buf.putDouble(number.doubleValue());
		}
		
		stream.write(buf.array());
	}

	/**
	 * Serializes a boolean value
	 * @param stream Destination stream
	 * @param bool Boolean value to serialize
	 * @params options Serialization options
	 * @throws IOException
	 */
	private static void serializeBoolean(OutputStream stream, Boolean bool, short options) throws IOException {
		byte b = WhiteSharkDataType.BOOLEAN.getMask();
		if (bool)
			b |= 0x10;
		stream.write(b);
	}
	
	/**
	 * Serializes an array
	 * @param stream Destination stream
	 * @param array Array instance to serialize
	 * @params options Serialization options
	 * @throws IOException
	 * @throws IllegalAccessException
	 */
	private static void serializeArray(OutputStream stream, Object array, short options) throws IOException, IllegalAccessException {
		byte mask = WhiteSharkDataType.ARRAY.getMask();
		
		int lengthByteCount;
		int length = Array.getLength(array);
		if (length == 0)
			lengthByteCount = 0;
		else if (length < Byte.MAX_VALUE)
			lengthByteCount = 1;
		else if (length < Short.MAX_VALUE)
			lengthByteCount = 2;
		else
			lengthByteCount = 4;
		byte lengthByteCountMask = (byte) ((lengthByteCount == 4) ? 3 : lengthByteCount);
		
		Class<?> componentClass = array.getClass().getComponentType();
		if (!componentClass.isPrimitive() && !componentClass.equals(String.class) && !componentClass.equals(Boolean.class)) {
			if (WhiteSharkUtils.hasOption(options, WhiteSharkConstants.OPTIONS_OBJECTS_AS_GENERICS)) {
				if (componentClass.isArray())
					componentClass = Object[].class;
				else
					componentClass = Object.class;
			}
			else {
				if (!componentClass.isArray() && componentClass.getAnnotation(WhiteSharkAsGenerics.class) != null)
					componentClass = Object.class;
			}
		}
		
		byte[] classNameBytes = null;
		int classNameLength = 2;
		boolean classInDictionary = classDictionary.contains(componentClass);
		int classDictionaryIndex = -1;
		if (!classInDictionary) {
			classDictionary.add(componentClass);
			classNameBytes = componentClass.getName().getBytes("US-ASCII");
			classNameLength += classNameBytes.length;
		}
		else
			classDictionaryIndex = classDictionary.indexOf(componentClass);
		
		mask |= ((byte) lengthByteCountMask) << 4;
		if (classInDictionary)
			mask |= 0x40;
		ByteBuffer buf = WhiteSharkUtils.allocateByteBuffer(1 + classNameLength + lengthByteCount);
		buf.put(mask);
		if (classInDictionary)
			buf.putShort((short) classDictionaryIndex);
		else {
			buf.putShort((short) classNameBytes.length);
			buf.put(classNameBytes);
		}
		switch (lengthByteCount) {
		case 0:
			break;
		case 1:
			buf.put((byte) length);
			break;
		case 2:
			buf.putShort((short) length);
			break;
		default:
			buf.putInt(length);
			break;
		}
		
		stream.write(buf.array());
		
		for (int i = 0; i < length; i++)
			serialize(stream, Array.get(array, i), options);
	}
	
	/**
	 * Serializes an object
	 * <p>
	 * This method must only be used to serialize objects that are not of primitive or array types
	 *  
	 * @param stream Destination stream
	 * @param obj Object to serialize
	 * @params options Serialization options
	 * @throws IOException
	 * @throws IllegalAccessException
	 */
	private static void serializeObject(OutputStream stream, Object obj, short options) throws IOException, IllegalAccessException {
		Class<?> c = obj.getClass();
		byte[] classCanonicalNameBytes = null;
		
		boolean serializesAsGenerics = WhiteSharkUtils.hasOption(options, WhiteSharkConstants.OPTIONS_OBJECTS_AS_GENERICS) || (c.getAnnotation(WhiteSharkAsGenerics.class) != null);
		
		@SuppressWarnings("unchecked")
		Map<String,Object> map = (Map<String,Object>) obj;
		boolean serializableMap = (map != null && c.getAnnotation(WhiteSharkSerializableMap.class) != null);
		
		boolean classInDictionary = classDictionary.contains(c);
		int classDictionaryIndex = -1;
		
		if (!serializesAsGenerics) {
			if (!classInDictionary) {
				classDictionary.add(c);
				classCanonicalNameBytes = c.getCanonicalName().getBytes("US-ASCII");
			}
			else
				classDictionaryIndex = classDictionary.indexOf(c);
		}
		
		// Locating fields
		Field[] fields = c.getFields();
		Vector<Field> serializableFields = null;
		for (Field f : fields) {
			if (f.getAnnotation(WhiteSharkSerializable.class) != null) {
				if (serializableFields == null)
					serializableFields = new Vector<Field>();
				serializableFields.add(f);
			}
		}
		int fieldCount = (serializableFields == null) ? 0 : serializableFields.size();
		// -- Serializable map?
		if (serializableMap)
			fieldCount += ((Map<?, ?>) obj).size();
		
		int fieldCountByteCount;
		if (fieldCount == 0)
			fieldCountByteCount = 0;
		else if (fieldCount < Byte.MAX_VALUE)
			fieldCountByteCount = 1;
		else if (fieldCount < Short.MAX_VALUE)
			fieldCountByteCount = 2;
		else
			fieldCountByteCount = 4;
		byte fieldCountByteMask = (byte) ((fieldCountByteCount == 4) ? 3 : fieldCountByteCount);
		
		byte mask = WhiteSharkDataType.OBJECT.getMask();
		mask |= ((byte) fieldCountByteMask) << 4;
		if (serializesAsGenerics)
			mask |= 0x80;
		else if (classInDictionary)
			mask |= 0x40;
		
		int byteBufferLength = 1 + fieldCountByteCount;
		if (!serializesAsGenerics) {
			byteBufferLength += 2;
			if (!classInDictionary)
				byteBufferLength += classCanonicalNameBytes.length;
		}
		ByteBuffer buf = WhiteSharkUtils.allocateByteBuffer(byteBufferLength);
		buf.put(mask);
		if (!serializesAsGenerics) {
			if (classInDictionary)
				buf.putShort((short) classDictionaryIndex);
			else {
				buf.putShort((short) classCanonicalNameBytes.length);
				buf.put(classCanonicalNameBytes);
			}
		}
		switch (fieldCountByteCount) {
		default:
			break;
		case 1:
			buf.put((byte) fieldCount);
			break;
		case 2:
			buf.putShort((short) fieldCount);
			break;
		case 4:
			buf.putInt(fieldCount);
			break;
		}
		
		stream.write(buf.array());
		
		if (serializableFields != null) {
			for (Field f : serializableFields)
				serializeProperty(stream, obj, f, options);
		}
		if (serializableMap) {
			for (Map.Entry<String,Object> entry : map.entrySet())
				serializeProperty(stream, WhiteSharkConstants.MAP_PROPERTY_NAME_PREFIX + entry.getKey(), entry.getValue(), options);
		}
	}
	
	/**
	 * Serializes an object property (field)
	 * @param stream Destination stream
	 * @param obj Object whose the property belongs
	 * @param f Property (Field) to serialize
	 * @params options Serialization options
	 * @throws IOException
	 * @throws IllegalAccessException
	 */
	private static void serializeProperty(OutputStream stream, Object obj, Field f, short options) throws IOException, IllegalAccessException {
		serializeProperty(stream, f.getName(), f.get(obj), options);
	}
	
	/**
	 * Serializes an object property using its name and value
	 * @param stream Destination stream
	 * @param name Property name
	 * @param obj Property value
	 * @param options Serialization options
	 * @throws IOException
	 * @throws IllegalAccessException
	 */
	private static void serializeProperty(OutputStream stream, String name, Object obj, short options) throws IOException, IllegalAccessException {
		byte mask = WhiteSharkDataType.PROPERTY.getMask();
		byte[] fieldNameBytes = null;
		int bufferByteCount;
		boolean longFieldName = false;
		
		boolean propertyInDictionary = propertyDictionary.contains(name);
		int propertyDictionaryIndex = -1;
		if (propertyInDictionary) {
			propertyDictionaryIndex = propertyDictionary.indexOf(name);
			bufferByteCount = 3;
			mask |= 0x40;
		}
		else {
			fieldNameBytes = name.getBytes("US-ASCII");
			propertyDictionary.add(name);
			
			int fieldNameByteLength = fieldNameBytes.length;
			longFieldName = (fieldNameByteLength >= Byte.MAX_VALUE);
			if (longFieldName)
				mask |= 0x10;
			bufferByteCount = 1 + (longFieldName ? 2 : 1) + fieldNameBytes.length;
		}
		
		ByteBuffer buf = WhiteSharkUtils.allocateByteBuffer(bufferByteCount);
		buf.put(mask);
		if (propertyInDictionary)
			buf.putShort((short) propertyDictionaryIndex);
		else {
			if (!longFieldName)
				buf.put((byte) fieldNameBytes.length);
			else
				buf.putShort((short) fieldNameBytes.length);
			buf.put(fieldNameBytes);
		}
		stream.write(buf.array());
		
		serialize(stream, obj, options);
	}
	
}

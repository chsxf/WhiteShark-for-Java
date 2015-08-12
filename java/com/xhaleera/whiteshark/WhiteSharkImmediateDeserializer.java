package com.xhaleera.whiteshark;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Vector;

import com.xhaleera.whiteshark.annotations.WhiteSharkSerializable;
import com.xhaleera.whiteshark.annotations.WhiteSharkSerializableMap;
import com.xhaleera.whiteshark.exceptions.WhiteSharkException;
import com.xhaleera.whiteshark.exceptions.WhiteSharkMismatchingIdentifierException;
import com.xhaleera.whiteshark.exceptions.WhiteSharkMissingFormatIdentifierException;
import com.xhaleera.whiteshark.exceptions.WhiteSharkNotAPropertyException;
import com.xhaleera.whiteshark.exceptions.WhiteSharkUnsupportedVersionException;

/**
 * Immediate (buffered) deserializer class
 * <p>
 * This class is used to deserialize completely buffered WhiteShark streams.
 * To deserialize incomplete (unbuffered) streams, use WhiteSharkProgressiveDeserializer.
 * 
 * @author Christophe SAUVEUR <christophe@xhaleera.com>
 * @since 1.0
 * @version 1.0
 */
public class WhiteSharkImmediateDeserializer {

	private static Vector<Class<?>> classDictionary = new Vector<>();
	private static Vector<String> propertyDictionary = new Vector<>();
	
	/**
	 * Deserializes a completely buffered WhiteShark stream
	 * 
	 * @param identifier Custom WhiteShark stream identifier to handle
	 * @param stream Stream to deserialize
	 * @return a generic Object containing the deserialized value
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws WhiteSharkException In case the format identifier, the custom identifier or the stream version do not match.
	 * @throws NoSuchFieldException
	 * @throws IOException
	 */
	public static Object deserialize(String identifier, InputStream stream) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, WhiteSharkException, NoSuchFieldException, IOException {
		classDictionary.clear();
		propertyDictionary.clear();
		
		identifier = WhiteSharkUtils.sanitizeIdentifier(identifier);
		
		byte[] b = new byte[12];
		ByteBuffer buf = WhiteSharkUtils.wrapWithByteBuffer(b);
		stream.read(b);
		
		byte[] b4 = new byte[4];
		buf.get(b4);
		if (new String(b4, "US-ASCII").equals(WhiteSharkConstants.FORMAT_IDENTIFIER) == false)
			throw new WhiteSharkMissingFormatIdentifierException("Format identifier unfound");
		
		buf.get(b4);
		if (new String(b4, "US-ASCII").equals(identifier) == false)
			throw new WhiteSharkMismatchingIdentifierException("Identifiers do not match");
		
		if (buf.getShort() != WhiteSharkConstants.VERSION)
			throw new WhiteSharkUnsupportedVersionException("Versions do not match");
		
		short options = buf.getShort();
		return deserialize(stream, options);
	}
	
	/**
	 * Generic deserialization method
	 * @param stream Stream to deserialize
	 * @params options Serialization options
	 * @return a generic Object containing the deserialized value
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws WhiteSharkNotAPropertyException
	 * @throws NoSuchFieldException
	 * @throws IOException
	 */
	private static Object deserialize(InputStream stream, short options) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, WhiteSharkNotAPropertyException, NoSuchFieldException, IOException {
		byte mask = (byte) stream.read();
		byte dataType = (byte) (mask & 0xf);
		
		// Null
		if (dataType == WhiteSharkDataType.NULL.getMask())
			return null;
		
		// Boolean
		else if (dataType == WhiteSharkDataType.BOOLEAN.getMask())
			return deserializeBoolean(mask, options);
		
		// Integer
		else if (dataType == WhiteSharkDataType.INTEGER.getMask())
			return deserializeInteger(stream, mask, options);
		
		// Real
		else if (dataType == WhiteSharkDataType.REAL.getMask())
			return deserializeReal(stream, mask, options);
		
		// Character
		else if (dataType == WhiteSharkDataType.CHAR.getMask())
			return deserializeCharacter(stream, mask, options);
		
		// String
		else if (dataType == WhiteSharkDataType.STRING.getMask())
			return deserializeString(stream, mask, options);
		
		// Array
		else if (dataType == WhiteSharkDataType.ARRAY.getMask())
			return deserializeArray(stream, mask, options);
		
		// Object
		else
			return deserializeObject(stream, mask, options);
	}

	/**
	 * Unseralizes a boolean value
	 * @param mask Byte mask
	 * @params options Serialization options
	 * @return the deserialized boolean value
	 */
	private static boolean deserializeBoolean(byte mask, short options) {
		return ((mask & 0xf0) != 0);
	}
	
	/**
	 * Deserializes an integer value
	 * @param stream Stream to deserialize
	 * @param mask Byte mask
	 * @params options Serialization options
	 * @return a Number instance representing the deserialized integer value
	 * @throws IOException
	 */
	private static Number deserializeInteger(InputStream stream, byte mask, short options) throws IOException {
		ByteBuffer buf;
		byte[] b;
		
		int integerType = (mask & 0xf0) >> 4;
		switch (integerType)
		{
			case 1:
				return (byte) stream.read();
				
			case 2:
				b = new byte[2];
				stream.read(b);
				buf = WhiteSharkUtils.wrapWithByteBuffer(b);
				return buf.getShort();
				
			case 4:
				b = new byte[4];
				stream.read(b);
				buf = WhiteSharkUtils.wrapWithByteBuffer(b);
				return buf.getInt();
				
			default:
				b = new byte[8];
				stream.read(b);
				buf = WhiteSharkUtils.wrapWithByteBuffer(b);
				return buf.getLong();
		}
	}
	
	/**
	 * Deserializes a floating-point number value
	 * @param stream Stream to deserialize
	 * @param mask Byte mask
	 * @params options Serialization options
	 * @return a Number instance representing the deserialized floating-point value
	 * @throws IOException
	 */
	private static Number deserializeReal(InputStream stream, byte mask, short options) throws IOException {
		if ((mask & 0xf0) != 0)
		{
			byte[] b = new byte[8];
			stream.read(b);
			ByteBuffer buf = WhiteSharkUtils.wrapWithByteBuffer(b);
			return buf.getDouble();
		}
		else {
			byte[] b = new byte[4];
			stream.read(b);
			ByteBuffer buf = WhiteSharkUtils.wrapWithByteBuffer(b);
			return buf.getFloat();
		}
	}
	
	/**
	 * Deserializes a character
	 * @param stream Stream to deserialize
	 * @param mask Byte mask
	 * @params options Serialization options
	 * @return the deserialized character
	 * @throws IOException
	 */
	private static Character deserializeCharacter(InputStream stream, byte mask, short options) throws IOException {
		byte[] b = new byte[2];
		stream.read(b);
		ByteBuffer buf = WhiteSharkUtils.wrapWithByteBuffer(b);
		return buf.getChar();
	}
	
	/**
	 * Deserializes a string
	 * @param stream Stream to deserialize
	 * @param mask Byte mask
	 * @params options Serialization options
	 * @return the deserialized string
	 * @throws IOException
	 */
	private static String deserializeString(InputStream stream, byte mask, short options) throws IOException {
		ByteBuffer buf;
		byte[] b;
		
		int length;
		int lengthByteCount = ((mask & 0xf0) >> 4);
		switch (lengthByteCount) {
			case 1:
				length = stream.read();
				break;
				
			case 2:
				b = new byte[2];
				stream.read(b);
				buf = WhiteSharkUtils.wrapWithByteBuffer(b);
				length = buf.getShort();
				break;
				
			default:
				b = new byte[4];
				stream.read(b);
				buf = WhiteSharkUtils.wrapWithByteBuffer(b);
				length = buf.getInt();
				break;
		}
		
		b = new byte[length];
		stream.read(b);
		return new String(b, "UTF-8");
	}
	
	/**
	 * Deserializes an array
	 * @param stream Stream to deserialize
	 * @param mask Byte mask
	 * @params options Serialization options
	 * @return an Object containing an array instance
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws ArrayIndexOutOfBoundsException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws WhiteSharkNotAPropertyException
	 * @throws NoSuchFieldException
	 */
	private static Object deserializeArray(InputStream stream, byte mask, short options) throws IOException, ClassNotFoundException, ArrayIndexOutOfBoundsException, IllegalArgumentException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, WhiteSharkNotAPropertyException, NoSuchFieldException {
		byte[] b, classNameBytes = null;
		ByteBuffer buf;
		
		boolean classInDictionary = ((mask & 0x40) != 0);
		int classDictionaryIndex = -1;
		
		b = new byte[2];
		stream.read(b);
		buf = WhiteSharkUtils.wrapWithByteBuffer(b);
		if (classInDictionary)
			classDictionaryIndex = buf.getShort();
		else {
			int classNameLength = buf.getShort();
			classNameBytes = new byte[classNameLength];
			stream.read(classNameBytes);
		}
		
		int lengthByteCount = ((mask & 0x30) >> 4);
		if (lengthByteCount == 3)
			lengthByteCount = 4;
		int count;
		if (lengthByteCount == 1)
			count = stream.read();
		else if (lengthByteCount == 2) {
			b = new byte[2];
			stream.read(b);
			buf = WhiteSharkUtils.wrapWithByteBuffer(b);
			count = buf.getShort();
		}
		else {
			b = new byte[4];
			stream.read(b);
			buf = WhiteSharkUtils.wrapWithByteBuffer(b);
			count = buf.getInt();
		}
		
		Class<?> primitiveClass;
		if (classInDictionary)
			primitiveClass = classDictionary.elementAt(classDictionaryIndex);
		else {
			String className = new String(classNameBytes, "US-ASCII");
			primitiveClass = Class.forName(className);
			classDictionary.add(primitiveClass);
		}
		Object arr = Array.newInstance(primitiveClass, count);
		for (int i = 0; i < count; i++)
			Array.set(arr, i, deserialize(stream, options));
		return arr;
	}
	
	/**
	 * Deserializes an object
	 * <p>
	 * This method must be used only with objects that are not primitive or array types.
	 * 
	 * @param stream Stream to deserialize
	 * @param mask Byte mask
	 * @params options Serialization options
	 * @return the deserialized Object instance
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws WhiteSharkNotAPropertyException
	 * @throws NoSuchFieldException
	 */
	private static Object deserializeObject(InputStream stream, byte mask, short options) throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, WhiteSharkNotAPropertyException, NoSuchFieldException {
		byte[] b, classNameBytes = null;
		ByteBuffer buf;
		boolean serializedAsGenerics = WhiteSharkUtils.hasOption(options, WhiteSharkConstants.OPTIONS_OBJECTS_AS_GENERICS) || ((mask & 0x80) != 0);
		boolean classInDictionary = ((mask & 0x40) != 0);
		int classDictionaryIndex = -1;
		
		if (!serializedAsGenerics) {
			b = new byte[2];
			stream.read(b);
			buf = WhiteSharkUtils.wrapWithByteBuffer(b);
			if (!classInDictionary) {
				int classNameLength = buf.getShort();
				b = new byte[classNameLength];
				stream.read(b);
				classNameBytes = b;
			}
			else
				classDictionaryIndex = buf.getShort();
		}
		
		int fieldCountByteCount = ((mask & 0x30) >> 4);
		if (fieldCountByteCount == 3)
			fieldCountByteCount = 4;
		int count = 0;
		if (fieldCountByteCount == 1)
			count = stream.read();
		else if (fieldCountByteCount == 2) {
			b = new byte[2];
			stream.read(b);
			buf = WhiteSharkUtils.wrapWithByteBuffer(b);
			count = buf.getShort();
		}
		else if (fieldCountByteCount == 4) {
			b = new byte[4];
			stream.read(b);
			buf = WhiteSharkUtils.wrapWithByteBuffer(b);
			count = buf.getInt();
		}
		
		Object o;
		if (serializedAsGenerics)
			o = new WhiteSharkGenericObject(count);
		else {
			Class<?> c;
			if (!classInDictionary) {
				String className = new String(classNameBytes, "US-ASCII");
				c = Class.forName(className);
				classDictionary.add(c);
			}
			else
				c = classDictionary.elementAt(classDictionaryIndex);
			Constructor<?> constructor = c.getConstructor();
			o = constructor.newInstance();
		}
		
		for (int i = 0; i < count; i++)
			deserializeProperty(stream, o, serializedAsGenerics, options);
		
		return o;
	}
	
	/**
	 * Deserializes an object's property
	 * @param stream Stream to deserialize
	 * @param parentObj Object whose the property belongs
	 * @params options Serialization options
	 * @throws IOException
	 * @throws WhiteSharkNotAPropertyException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 * @throws InvocationTargetException
	 */
	private static void deserializeProperty(InputStream stream, Object parentObj, boolean parentObjectAsGenerics, short options) throws IOException, WhiteSharkNotAPropertyException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException {
		byte mask = (byte) stream.read();
		byte dataType = (byte) (mask & 0xf);
		byte[] b;
		ByteBuffer buf;
		
		if (dataType != WhiteSharkDataType.PROPERTY.getMask())
			throw new WhiteSharkNotAPropertyException("Not a property");
		
		boolean propertyInDictionary = ((mask & 0x40) != 0);
		String fieldName;
		if (propertyInDictionary) {
			b = new byte[2];
			stream.read(b);
			buf = WhiteSharkUtils.wrapWithByteBuffer(b);
			int propertyDictionaryIndex = buf.getShort();
			fieldName = propertyDictionary.elementAt(propertyDictionaryIndex);
		}
		else {
			int fieldNameByteLength = ((mask & 0x10) != 0) ? 2 : 1;
			int fieldNameLength;
			if (fieldNameByteLength == 1)
				fieldNameLength = stream.read();
			else {
				b = new byte[2];
				stream.read(b);
				buf = WhiteSharkUtils.wrapWithByteBuffer(b);
				fieldNameLength = buf.getShort();
			}
			
			b = new byte[fieldNameLength];
			stream.read(b);
			fieldName = new String(b, "US-ASCII");
			
			propertyDictionary.add(fieldName);
		}
		
		if (parentObjectAsGenerics) {
			WhiteSharkGenericObject obj = (WhiteSharkGenericObject) parentObj;
			obj.put(fieldName, deserialize(stream, options));
		}
		else {
			Object o = deserialize(stream, options);
			Class<?> c = parentObj.getClass();
			
			if (fieldName.startsWith(WhiteSharkConstants.MAP_PROPERTY_NAME_PREFIX)) {
				@SuppressWarnings("unchecked")
				Map<String,Object> map = (Map<String,Object>) parentObj;
				if (map != null && c.getAnnotation(WhiteSharkSerializableMap.class) != null)
					map.put(fieldName.substring(WhiteSharkConstants.MAP_PROPERTY_NAME_PREFIX.length()), o);
			}
			else {
				Field f = c.getField(fieldName);
				if (f.getAnnotation(WhiteSharkSerializable.class) != null)
					f.set(parentObj, o);
			}
		}
	}
	
}

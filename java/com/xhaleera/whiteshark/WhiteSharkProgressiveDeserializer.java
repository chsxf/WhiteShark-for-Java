package com.xhaleera.whiteshark;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import com.xhaleera.whiteshark.annotations.WhiteSharkSerializableCollection;
import com.xhaleera.whiteshark.annotations.WhiteSharkSerializableMap;
import com.xhaleera.whiteshark.exceptions.WhiteSharkException;
import com.xhaleera.whiteshark.exceptions.WhiteSharkMissingFormatIdentifierException;
import com.xhaleera.whiteshark.exceptions.WhiteSharkUnsupportedVersionException;

/**
 * Progressive (unbuffered) deserializer class
 * <p>
 * This class is used to deserialize unbuffered WhiteShark streams in several successive steps.
 * To deserialize complete (buffered) streams, prefer using WhiteSharkImmediateDeserializer.
 * 
 * @author Christophe SAUVEUR <christophe@xhaleera.com>
 * @since 1.0
 * @version 1.0
 */
public final class WhiteSharkProgressiveDeserializer {

	/**
	 * Deserialization result information class
	 * 
	 * @author Christophe SAUVEUR <christophe@xhaleera.com>
	 * @since 1.0
	 * @version 1.0
	 */
	public class DeserializationResult {
		/** Flag indicating if the deserialization is complete */
		public final boolean complete;
		/** Deserialization result holder */
		public final Object result;
		
		/** Current object's property name holder */
		protected final String propertyName;
		/** Flag indicating if the result is a complex element (object or array) */
		protected final boolean isComplex;
		/** Flag indicating if the result, as an object, has been serialized as generics */
		protected final boolean objectAsGenerics;
		/** Number of sub elements (properties or array items) in a complex element */
		protected final int subElementCount;
		
		/**
		 * Default constructor
		 * <p>
		 * Used for incomplete or invalid result.
		 */
		public DeserializationResult() {
			this.complete = false;
			this.result = null;
			this.propertyName = null;
			this.isComplex = false;
			this.objectAsGenerics = false;
			this.subElementCount = 0;
		}
		
		/**
		 * Constructor with completion indicator
		 * @param complete Tells if the deserialization is complete or not
		 * @param result Deserialization result
		 */
		public DeserializationResult(boolean complete, Object result) {
			this.complete = complete;
			this.result = result;
			this.propertyName = null;
			this.isComplex = false;
			this.objectAsGenerics = false;
			this.subElementCount = 0;
		}
		
		/**
		 * Constructor for objects properties results
		 * <p>
		 * Generates incomplete deserialization results.
		 * 
		 * @param result The deserialized property value
		 * @param propertyName The property name
		 */
		public DeserializationResult(String propertyName) {
			this.complete = false;
			this.result = null;
			this.propertyName = propertyName;
			this.isComplex = false;
			this.objectAsGenerics = false;
			this.subElementCount = 0;
		}
		
		/**
		 * Constructor for complex element results
		 * <p>
		 * Generates incomplete deserialization results.
		 * 
		 * @param result The deserialized complex element instance 
		 * @param objectAsGenerics Flag indicating if the complex instance, as an object, has been serialized as generics
		 * @param subElementCount The number of sub elements for this complex element
		 */
		public DeserializationResult(Object result, boolean objectAsGenerics, int subElementCount) {
			this.complete = false;
			this.result = result;
			this.propertyName = null;
			this.isComplex = true;
			this.objectAsGenerics = objectAsGenerics;
			this.subElementCount = subElementCount;
		}
	}
	
	/** Custom stream identifier */
	private String identifier;
	/** Serialization options */
	private short options;
	/** Output stream instance */
	private ByteArrayOutputStream baos;
	
	/** Flag indicating if the stream header has been deserialized or not */
	private boolean headerDeserialized;
	
	/** Class dictionary */
	private Vector<Class<?>> classDictionary;
	/** Property dictionary */
	private Vector<String> propertyDictionary;
	
	/**
	 * Deserialization level container
	 * 
	 * @author Christophe SAUVEUR <christophe@xhaleera.com>
	 * @since 1.0
	 * @version 1.0
	 */
	private class DeserializationLevel {
		/** Current level's object instance */
		public final Object object;
		/** Current level's flag indicating if the object has been serialized as generics */
		public final boolean objectAsGenerics;
		/** Current object's sub element index */
		public int currentIndex;
		/** Current object's max sub element index */
		public final int maxIndex;
		/** Current object's sub property name */
		public String propertyName;
		/** Current object's is a serializable map */
		public boolean serializableMap;
		/** Current object's is a serializable collection */
		public boolean serializableCollection;
		
		/**
		 * Constructor
		 * @param object Object instance for the current level
		 * @param objectAsGenerics Flag indicating if the object instance has been serialized as generics
		 * @param maxIndex Max sub element index for the current object instance
		 * @param serializableMap If set, this level is a serializable map
		 * @param serializableCollection If set, this level is a serializable collection
		 */
		public DeserializationLevel(Object object, boolean objectAsGenerics, int maxIndex, boolean serializableMap, boolean serializableCollection) {
			this.object = object;
			this.objectAsGenerics = objectAsGenerics;
			this.currentIndex = 0;
			this.maxIndex = maxIndex;
			this.propertyName = null;
			this.serializableMap = serializableMap;
			this.serializableCollection = serializableCollection;
		}
	}
	/** Deserialization levels stack */
	public Stack<DeserializationLevel> levels;
	
	/**
	 * Constructor
	 * @param identifier Custom stream identifier
	 */
	public WhiteSharkProgressiveDeserializer(String identifier) {
		this.identifier = WhiteSharkUtils.sanitizeIdentifier(identifier);
		options = WhiteSharkConstants.OPTIONS_DEFAULT;
		baos = new ByteArrayOutputStream();
		
		headerDeserialized = false;
		levels = null;
		
		classDictionary = new Vector<>();
		propertyDictionary = new Vector<>();
	}
	
	/**
	 * Updates the deserializer with incoming bytes 
	 * @param bytes New bytes
	 * @return a DeserializationResult instance containing progress and result information
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws UnsupportedEncodingException
	 * @throws WhiteSharkException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public DeserializationResult update(byte[] bytes) throws NoSuchFieldException, IllegalAccessException, UnsupportedEncodingException, WhiteSharkException, ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalArgumentException, InvocationTargetException {
		return update(bytes, 0, bytes.length);
	}
	
	/**
	 * Updates the deserializer with a bytes coming for a portion of a byte array
	 * @param bytes Byte array
	 * @param off Start offset in the byte array
	 * @param len Number of bytes to consider
	 * @return a DeserializationResult instance containing progress and result information
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws UnsupportedEncodingException
	 * @throws WhiteSharkException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public DeserializationResult update(byte[] bytes, int off, int len) throws NoSuchFieldException, IllegalAccessException, UnsupportedEncodingException, WhiteSharkException, ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalArgumentException, InvocationTargetException {
		baos.write(bytes, off, len);
		
		DeserializationResult result = new DeserializationResult();
		
		while (canDeserializationContinue(0)) {
			if (!headerDeserialized)
			{
				ByteBuffer buf = WhiteSharkUtils.wrapWithByteBuffer(baos.toByteArray());
				
				byte[] b4 = new byte[4];
				buf.get(b4);
				if (new String(b4, "US-ASCII").equals(WhiteSharkConstants.FORMAT_IDENTIFIER) == false)
					throw new WhiteSharkMissingFormatIdentifierException("Format identifier unfound");
				
				buf.get(b4);
				if (new String(b4, "US-ASCII").equals(identifier) == false)
					throw new WhiteSharkMissingFormatIdentifierException("Identifiers do not match");
				
				if (buf.getShort() != WhiteSharkConstants.VERSION)
					throw new WhiteSharkUnsupportedVersionException("Versions do not match");
				
				options = buf.getShort();
				
				removeFirstBytesFromStream(12);
				headerDeserialized = true;
			}
			
			else {
				DeserializationResult _result = deserializeNext();
				if (_result != null && _result.complete)
					return _result;
			}
		}
		
		return result;
	}
	
	/**
	 * Finalizes the deserialization with incoming bytes
	 * @param bytes New bytes
	 * @return a DeserializationResult instance containing progress and result information
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws UnsupportedEncodingException
	 * @throws WhiteSharkException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public DeserializationResult finalize(byte[] bytes) throws NoSuchFieldException, IllegalAccessException, UnsupportedEncodingException, WhiteSharkException, ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalArgumentException, InvocationTargetException {
		return finalize(bytes, 0, bytes.length);
	}
	
	/**
	 * Finalizes the deserialization with a portion of a byte array
	 * @param bytes Byte array
	 * @param off Starting offset in the byte array
	 * @param len Number of bytes to consider
	 * @return a DeserializationResult instance containing progress and result information
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws UnsupportedEncodingException
	 * @throws WhiteSharkException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public DeserializationResult finalize(byte[] bytes, int off, int len) throws NoSuchFieldException, IllegalAccessException, UnsupportedEncodingException, WhiteSharkException, ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalArgumentException, InvocationTargetException {
		DeserializationResult result = update(bytes, off, len);
		if (!result.complete)
			throw new WhiteSharkException("Deserialization incomplete");
		return result;
	}

	/**
	 * Removes bytes for the start of the container ByteArrayOutputStream
	 * @param length Number of bytes to remove
	 */
	private void removeFirstBytesFromStream(int length) {
		byte[] bytes = baos.toByteArray();
		length = Math.min(Math.max(0, length), bytes.length);
		
		baos.reset();
		if (length < bytes.length)
			baos.write(bytes, length, bytes.length - length);
	}
	
	/**
	 * Tells if the deserialization process can continue with the bytes contained if the ByteArrayOutputStream
	 * @param offset Starting offset in the ByteArrayOutputStream
	 * @return <code>true</code> if the deserialization process can continue, <code>false</code> either.
	 */
	private boolean canDeserializationContinue(int offset) {
		if (baos.size() == offset)
			return false;
		
		else if (!headerDeserialized)
			return (baos.size() >= 12);
		
		else {
			ByteBuffer buf = WhiteSharkUtils.wrapWithByteBuffer(baos.toByteArray());
			byte mask = buf.get(offset);
			byte dataType = (byte) (mask & 0xf);
			
			// Null / Boolean
			if (dataType == WhiteSharkDataType.NULL.getMask() || dataType == WhiteSharkDataType.BOOLEAN.getMask())
				return true;
			
			// Integer
			else if (dataType == WhiteSharkDataType.INTEGER.getMask()) {
				int sizeof = (mask & 0xf0) >> 4; 
				return (baos.size() >= offset + sizeof + 1);
			}
			
			// Real
			else if (dataType == WhiteSharkDataType.REAL.getMask()) {
				int precision = (mask & 0xf0) >> 4;
				return (baos.size() >= offset + 1 + ((precision == 0) ? 4 : 8)); 
			}
			
			// Character
			else if (dataType == WhiteSharkDataType.CHAR.getMask())
				return (baos.size() >= offset + 3);
			
			// String
			else if (dataType == WhiteSharkDataType.STRING.getMask()) {
				int lengthByteCount = (mask & 0xf0) >> 4;
				if (baos.size() < offset + 1 + lengthByteCount)
					return false;
				int length = 0;
				switch (lengthByteCount) {
					case 1:
						length = buf.get(offset + 1);
						break;
					case 2:
						length = buf.getShort(offset + 1);
						break;
					default:
						length = buf.getInt(offset + 1);
						break;
				}
				return (baos.size() >= offset + 1 + lengthByteCount + length);
			}
			
			// Property
			else if (dataType == WhiteSharkDataType.PROPERTY.getMask()) {
				boolean propertyInDictionary = ((mask & 0x40) != 0);
				int size, fieldNameLengthByteCount = 0;
				if (propertyInDictionary)
					size = 3;
				else {
					fieldNameLengthByteCount = ((mask & 0x10) != 0) ? 2 : 1;
					size = 1 + fieldNameLengthByteCount;
				}
				if (baos.size() < offset + size)
					return false;
				
				int length;
				if (propertyInDictionary)
					length = 0;
				else
					length = (fieldNameLengthByteCount == 2) ? buf.getShort(offset + 1) : buf.get(offset + 1);
				int newOffset = offset + size + length;
				return (baos.size() >= newOffset) && canDeserializationContinue(newOffset);
			}
			
			// Array
			else if (dataType == WhiteSharkDataType.ARRAY.getMask()) {
				int lengthByteCount = (mask & 0x30) >> 4;
				if (lengthByteCount == 3)
					lengthByteCount = 4;
				if (baos.size() < offset + 3)
					return false;
				
				int classNameLength = 2;
				if ((mask & 0x40) == 0)
					classNameLength += buf.getShort(offset + 1);
				return (baos.size() >= offset + 1 + classNameLength + lengthByteCount);
			}
			
			// Object
			else {
				boolean serializedAsGenerics = (dataType == WhiteSharkDataType.OBJECT.getMask()
						&& (WhiteSharkUtils.hasOption(options, WhiteSharkConstants.OPTIONS_OBJECTS_AS_GENERICS) || ((mask & 0x80) != 0)));
				boolean classInDictionary = ((mask & 0x40) != 0);
				int lengthByteCount = (mask & 0x30) >> 4;
				if (baos.size() < offset + 3)
					return false;
				int classNameLength;
				if (serializedAsGenerics)
					classNameLength = 0;
				else {
					classNameLength = 2;
					if (!classInDictionary)
						classNameLength += buf.getShort(offset + 1);
				}
				return (baos.size() >= offset + 1 + classNameLength + lengthByteCount);
			}
		}
	}
	
	/**
	 * Deserializes the next element in the stream
	 * @return a DeserializationResult instance containing progress and result information
	 * @throws UnsupportedEncodingException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	private DeserializationResult deserializeNext() throws UnsupportedEncodingException, ClassNotFoundException, NoSuchFieldException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		byte[] bytes = baos.toByteArray();
		byte mask = bytes[0];
		byte dataType = (byte) (mask & 0xf);
		
		boolean isRoot = (levels == null || levels.isEmpty());
		
		removeFirstBytesFromStream(1);
		
		DeserializationResult result;
		
		// Null
		if (dataType == WhiteSharkDataType.NULL.getMask())
			result = new DeserializationResult(isRoot, null);
		
		// Boolean
		else if (dataType == WhiteSharkDataType.BOOLEAN.getMask())
			result = new DeserializationResult(isRoot, deserializeBoolean(mask));
		
		// Integer
		else if (dataType == WhiteSharkDataType.INTEGER.getMask())
			result = new DeserializationResult(isRoot, deserializeInteger(mask));
		
		// Real
		else if (dataType == WhiteSharkDataType.REAL.getMask())
			result = new DeserializationResult(isRoot, deserializeReal(mask));
		
		// Character
		else if (dataType == WhiteSharkDataType.CHAR.getMask())
			result = new DeserializationResult(isRoot, deserializeCharacter(mask));
		
		// String
		else if (dataType == WhiteSharkDataType.STRING.getMask())
			result = new DeserializationResult(isRoot, deserializeString(mask));
		
		// Property
		else if (dataType == WhiteSharkDataType.PROPERTY.getMask())
			result = deserializeProperty(mask);
		
		// Array
		else if (dataType == WhiteSharkDataType.ARRAY.getMask())
			result = deserializeArray(isRoot, mask);
		
		// Object
		else
			result = deserializeObject(isRoot, mask);
		
		// Result treatment
		if (result != null && !result.complete) {
			if (levels != null) {
				DeserializationLevel level = levels.peek();
				if (result.propertyName != null)
					level.propertyName = result.propertyName;
				else {
					if (level.propertyName != null) {
						if (level.objectAsGenerics) {
							WhiteSharkGenericObject obj = (WhiteSharkGenericObject) level.object;
							obj.put(level.propertyName, result.result);
						}
						else {
							Class<?> c = level.object.getClass();
							
							if (level.propertyName.equals(WhiteSharkConstants.COLLECTION_ITEM_PROPERTY_NAME)) {
								try {
									@SuppressWarnings("unchecked")
									Collection<Object> coll = (Collection<Object>) level.object;
									if (coll != null && (level.serializableCollection || c.getAnnotation(WhiteSharkSerializableCollection.class) != null))
										coll.add(result.result);
								}
								catch (ClassCastException e) { }
							}
							else if (level.propertyName.startsWith(WhiteSharkConstants.MAP_PROPERTY_NAME_PREFIX)) {
								try {
									@SuppressWarnings("unchecked")
									Map<String, Object> map = (Map<String,Object>) level.object;
									if (map != null && (level.serializableMap || c.getAnnotation(WhiteSharkSerializableMap.class) != null))
										map.put(level.propertyName.substring(WhiteSharkConstants.MAP_PROPERTY_NAME_PREFIX.length()), result.result);
								}
								catch (ClassCastException e) { }
							}
							else {
								Field f = c.getField(level.propertyName);
								f.set(level.object, result.result);
							}
						}
					}
					else
						Array.set(level.object, level.currentIndex, result.result);
					level.currentIndex++;
					
					if (!result.isComplex) {
						while (levels.size() > 1) {
							level = levels.peek();
							if (level.currentIndex >= level.maxIndex)
								levels.pop();
							else
								break;
						}
						
						level = levels.peek();
						if (levels.size() == 1 && level.currentIndex >= level.maxIndex)
							return new DeserializationResult(true, level.object);
					}
				}
			}
			
			if (result.isComplex) {
				boolean serializableMap = false;
				boolean serializableCollection = false;
				if (levels == null)
					levels = new Stack<DeserializationLevel>();
				else if (levels.size() > 0) {
					DeserializationLevel level = levels.peek();
					if (level.propertyName != null && !level.propertyName.equals(WhiteSharkConstants.COLLECTION_ITEM_PROPERTY_NAME) && !level.propertyName.startsWith(WhiteSharkConstants.MAP_PROPERTY_NAME_PREFIX)) {
						Class<?> c = level.object.getClass();
						Field f = c.getField(level.propertyName);
						serializableMap = (f.getAnnotation(WhiteSharkSerializableMap.class) != null);
						serializableCollection = (f.getAnnotation(WhiteSharkSerializableCollection.class) != null);
					}
				}
				levels.add(new DeserializationLevel(result.result, result.objectAsGenerics, result.subElementCount, serializableMap, serializableCollection));
			}
		}
		
		return result;
	}
	
	/**
	 * Deserializes a boolean value
	 * @param mask Byte mask
	 * @return the deserialized boolean value
	 */
	private boolean deserializeBoolean(byte mask) {
		return ((mask & 0xf0) != 0);
	}
	
	/**
	 * Deserializes an integer value
	 * @param mask Byte mask
	 * @return a Number instance representing the deserialized integer value
	 */
	private Number deserializeInteger(byte mask) {
		ByteBuffer buf = WhiteSharkUtils.wrapWithByteBuffer(baos.toByteArray());
		
		int integerType = (mask & 0xf0) >> 4;
		removeFirstBytesFromStream(integerType);
		switch (integerType)
		{
			case 1:
				return buf.get();
				
			case 2:
				return buf.getShort();
				
			case 4:
				return buf.getInt();
				
			default:
				return buf.getLong();
		}
	}
	
	/**
	 * Deserializes a floating-point value
	 * @param mask Byte mask
	 * @return a Number instance representing the deserialized floating-point value
	 */
	private Number deserializeReal(byte mask) {
		ByteBuffer buf = WhiteSharkUtils.wrapWithByteBuffer(baos.toByteArray());
		
		if ((mask & 0xf0) != 0)
		{
			removeFirstBytesFromStream(8);
			return buf.getDouble();
		}
		else {
			removeFirstBytesFromStream(4);
			return buf.getFloat();
		}
	}
	
	/**
	 * Deserializes a character
	 * @param mask Byte mask
	 * @return the deserialized character
	 */
	private Character deserializeCharacter(byte mask) {
		ByteBuffer buf = WhiteSharkUtils.wrapWithByteBuffer(baos.toByteArray());
		removeFirstBytesFromStream(2);
		return buf.getChar();
	}
	
	/**
	 * Deserializes a string
	 * @param mask Byte mask
	 * @return the deserialized string
	 * @throws UnsupportedEncodingException
	 */
	private String deserializeString(byte mask) throws UnsupportedEncodingException {
		ByteBuffer buf = WhiteSharkUtils.wrapWithByteBuffer(baos.toByteArray());
		
		int length;
		int lengthByteCount = ((mask & 0xf0) >> 4);
		switch (lengthByteCount) {
			case 1:
				length = buf.get();
				break;
				
			case 2:
				length = buf.getShort();
				break;
				
			default:
				length = buf.getInt();
				break;
		}
		
		removeFirstBytesFromStream(lengthByteCount + length);
		
		byte[] b = new byte[length];
		buf.get(b);
		return new String(b, "UTF-8");
	}
	
	/**
	 * Deserializes an array
	 * @param isRoot Flag indicating if this element is the stream's root
	 * @param mask Byte mask
	 * @return a DeserializationResult instance containing progress and result information
	 * @throws ClassNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	private DeserializationResult deserializeArray(boolean isRoot, byte mask) throws ClassNotFoundException, UnsupportedEncodingException {
		ByteBuffer buf = WhiteSharkUtils.wrapWithByteBuffer(baos.toByteArray());
		
		boolean classInDictionary = ((mask & 0x40) != 0);
		int classDictionaryIndex = -1;
		
		int classNameLength = 0;
		byte[] b = null;
		if (!classInDictionary) {
			classNameLength = buf.getShort();
			b = new byte[classNameLength];
			buf.get(b);
		}
		else
			classDictionaryIndex = buf.getShort();
		
		int lengthByteCount = ((mask & 0x30) >> 4);
		if (lengthByteCount == 3)
			lengthByteCount = 4;
		int count;
		if (lengthByteCount == 1)
			count = buf.get();
		else if (lengthByteCount == 2)
			count = buf.getShort();
		else
			count = buf.getInt();
		
		removeFirstBytesFromStream(2 + classNameLength + lengthByteCount);
		
		Class<?> primitiveClass;
		if (classInDictionary)
			primitiveClass = classDictionary.elementAt(classDictionaryIndex);
		else {
			String className = new String(b, "US-ASCII");
			primitiveClass = Class.forName(className);
			classDictionary.add(primitiveClass);
		}
		
		Object arr = Array.newInstance(primitiveClass, count);
		if (isRoot && count == 0)
			return new DeserializationResult(true, arr);
		else
			return new DeserializationResult(arr, WhiteSharkUtils.hasOption(options, WhiteSharkConstants.OPTIONS_OBJECTS_AS_GENERICS), count);
	}
	
	/**
	 * Deserializes an object
	 * @param isRoot Flag indicating if this element is the stream's root
	 * @param mask Byte mask
	 * @return a DeserializationResult instance containing progress and result information
	 * @throws UnsupportedEncodingException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	private DeserializationResult deserializeObject(boolean isRoot, byte mask) throws UnsupportedEncodingException, ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		ByteBuffer buf = WhiteSharkUtils.wrapWithByteBuffer(baos.toByteArray());
		boolean serializedAsGenerics = WhiteSharkUtils.hasOption(options, WhiteSharkConstants.OPTIONS_OBJECTS_AS_GENERICS) || ((mask & 0x80) != 0);
		boolean classInDictionary = ((mask & 0x40) != 0);
		int classDictionaryIndex = -1;
		
		int classNameLength = 0;
		byte[] classNameBytes = null;
		if (!serializedAsGenerics) {
			if (!classInDictionary) {
				classNameLength = buf.getShort();
				classNameBytes = new byte[classNameLength];
				buf.get(classNameBytes);
			}
			else
				classDictionaryIndex = buf.getShort();
		}
		
		int fieldCountByteCount = ((mask & 0x30) >> 4);
		if (fieldCountByteCount == 3)
			fieldCountByteCount = 4;
		int count = 0;
		if (fieldCountByteCount == 1)
			count = buf.get();
		else if (fieldCountByteCount == 2)
			count = buf.getShort();
		else if (fieldCountByteCount == 4)
			count = buf.getInt();
		
		Object o;
		if (serializedAsGenerics) {
			removeFirstBytesFromStream(fieldCountByteCount);
			o = new WhiteSharkGenericObject(count);
		}
		else {
			Class<?> c;
			if (!classInDictionary) {
				removeFirstBytesFromStream(2 + classNameLength + fieldCountByteCount);
				String className = new String(classNameBytes, "US-ASCII");
				c = Class.forName(className);
				classDictionary.add(c);
			}
			else {
				removeFirstBytesFromStream(2 + fieldCountByteCount);
				c = classDictionary.elementAt(classDictionaryIndex);
			}
			Constructor<?> constructor = c.getConstructor();
			o = constructor.newInstance();
		}
		
		if (isRoot && count == 0)
			return new DeserializationResult(true, o);
		else
			return new DeserializationResult(o, serializedAsGenerics, count);
	}
	
	/**
	 * Deserializes an object property
	 * @param mask Byte mask
	 * @return a DeserializationResult instance containing progress and result information
	 * @throws UnsupportedEncodingException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	private DeserializationResult deserializeProperty(byte mask) throws UnsupportedEncodingException, ClassNotFoundException, NoSuchFieldException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		ByteBuffer buf = WhiteSharkUtils.wrapWithByteBuffer(baos.toByteArray());
		
		boolean propertyInDictionary = ((mask & 0x40) != 0);
		String fieldName;
		
		if (propertyInDictionary) {
			int propertyDictionaryIndex = buf.getShort();
			fieldName = propertyDictionary.elementAt(propertyDictionaryIndex);
			removeFirstBytesFromStream(2);
		}
		else {
			int fieldNameByteLength = ((mask & 0x10) != 0) ? 2 : 1;
			int fieldNameLength;
			if (fieldNameByteLength == 1)
				fieldNameLength = buf.get();
			else
				fieldNameLength = buf.getShort();
			
			removeFirstBytesFromStream(fieldNameByteLength + fieldNameLength);
			
			byte[] b = new byte[fieldNameLength];
			buf.get(b);
			fieldName = new String(b, "US-ASCII");
			
			propertyDictionary.add(fieldName);
		}
		
		return new DeserializationResult(fieldName);
	}
	
}

package com.xhaleera.whiteshark.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import org.json.JSONArray;

import com.xhaleera.whiteshark.WhiteSharkConstants;
import com.xhaleera.whiteshark.WhiteSharkGenericObject;
import com.xhaleera.whiteshark.WhiteSharkImmediateDeserializer;
import com.xhaleera.whiteshark.WhiteSharkProgressiveDeserializer;
import com.xhaleera.whiteshark.WhiteSharkSerializer;
import com.xhaleera.whiteshark.annotations.WhiteSharkSerializable;
import com.xhaleera.whiteshark.annotations.WhiteSharkSerializableCollection;
import com.xhaleera.whiteshark.annotations.WhiteSharkSerializableMap;

public class WhiteSharkTest {

	public static void main(String[] args) {
		try {
			Team data = Employee.buildTestData();
			dump(data);
			
			String streamId = "TEST";
			String path = "/Users/christophe/Desktop/Test.bin";
			
			// Serializing
			System.out.println("Serializing...");
			FileOutputStream fileStream = new FileOutputStream(new File(path));
			WhiteSharkSerializer.serialize(streamId, fileStream, data);
			fileStream.close();
			System.out.println("");
			
			// Serializing (Java native serialization)
			System.out.println("Serializing (Java native)...");
			fileStream = new FileOutputStream(new File("/Users/christophe/Desktop/Test-java.bin"));
			ObjectOutputStream oos = new ObjectOutputStream(fileStream);
			oos.writeObject(data);
			fileStream.close();
			System.out.println("");
			
			// Serializing (JSON serialization)
			System.out.println("Serializing (JSON)...");
			fileStream = new FileOutputStream(new File("/Users/christophe/Desktop/Test-json.bin"));
			JSONArray arr = new JSONArray();
			for (Employee e : data)
				arr.put(e.toJSON());
			fileStream.write(arr.toString().getBytes("UTF-8"));
			fileStream.close();
			System.out.println("");
			
			// Deserializing (immediate)
			System.out.println("Deserializing (immediate) ...");
			FileInputStream inStream = new FileInputStream(new File(path));
			Object o = WhiteSharkImmediateDeserializer.deserialize(streamId, inStream);
			dump(o);
			inStream.close();
			System.out.println("");
			
			// Deserializing (progressive)
			WhiteSharkProgressiveDeserializer.DeserializationResult result = null;
			System.out.println("Deserializing (progressive) ...");
			inStream = new FileInputStream(new File(path));
			WhiteSharkProgressiveDeserializer deserializer = new WhiteSharkProgressiveDeserializer(streamId);
			byte[] b = new byte[2];
			int c;
			while (inStream.available() > b.length) {
				c = inStream.read(b);
				result = deserializer.update(b, 0, c);
				if (result.complete)
					break;
			}
			if (result != null && !result.complete) {
				c = inStream.read(b);
				result = deserializer.finalize(b, 0, Math.max(0, c));
			}
			inStream.close();
			if (result != null && result.complete)
				dump(result.result);
			else
				System.out.println("Deserialization incomplete");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void dump(Object o) {
		dump(o, 0, false, false);
	}
	
	private static void dump(Object o, int level, boolean serializableMap, boolean serializableCollection) {
		if (o == null)
			System.out.println(String.format("%snull", tabs(level)));
		else {
			Class<?> c = o.getClass();
			if (c.isPrimitive() || o instanceof String || o instanceof Number || o instanceof Boolean)
				System.out.println(String.format("%s%s: %s", tabs(level), c.getCanonicalName(), o.toString()));
			else if (c.isArray()) {
				System.out.println(String.format("%s%s [", tabs(level), c.getCanonicalName()));
				for (int i = 0; i < Array.getLength(o); i++)
					dump(Array.get(o, i), level + 1, false, false);
				System.out.println(String.format("%s]", tabs(level)));
			}
			else {
				System.out.println(String.format("%s%s {", tabs(level), c.getCanonicalName()));
				if (o instanceof WhiteSharkGenericObject) {
					for (Map.Entry<String, Object> entry : ((WhiteSharkGenericObject) o).entrySet()) {
						System.out.println(String.format("%s\"%s\":", tabs(level), entry.getKey()));
						dump(entry.getValue(), level + 1, false, false);
					}
				}
				else {
					Field[] fields = c.getFields();
					for (Field f : fields) {
						if (f.getAnnotation(WhiteSharkSerializable.class) != null) {
							System.out.println(String.format("%s\"%s\":", tabs(level), f.getName()));
							try {
								dump(f.get(o), level + 1, f.getAnnotation(WhiteSharkSerializableMap.class) != null, f.getAnnotation(WhiteSharkSerializableCollection.class) != null);
							}
							catch (IllegalAccessException e) {
								e.printStackTrace();
							}
						}
					}
					
					try {
						@SuppressWarnings("unchecked")
						Map<String,Object> map = (Map<String,Object>) o;
						if (map != null && (serializableMap || c.getAnnotation(WhiteSharkSerializableMap.class) != null)) {
							System.out.println(String.format("%s%s {", tabs(level), WhiteSharkConstants.MAP_PROPERTY_NAME_PREFIX));
							for (Map.Entry<String,Object> entry : map.entrySet()) {
								System.out.println(String.format("%s\"%s\":", tabs(level + 1), entry.getKey()));
								dump(entry.getValue(), level + 2, false, false);
							}
							System.out.println(String.format("%s}", tabs(level)));
						}
					}
					catch (ClassCastException e) { }
					
					try {
						@SuppressWarnings("unchecked")
						Collection<Object> coll = (Collection<Object>) o;
						if (coll != null && (serializableCollection || c.getAnnotation(WhiteSharkSerializableCollection.class) != null)) {
							System.out.println(String.format("%s%s [", tabs(level), WhiteSharkConstants.COLLECTION_ITEM_PROPERTY_NAME));
							for (Object _o : coll)
								dump(_o, level + 1, false, false);
							System.out.println(String.format("%s]", tabs(level)));
						}
					}
					catch (ClassCastException e) { }
				}
				System.out.println(String.format("%s}", tabs(level)));
			}
		}
	}
	
	private static String tabs(int level) {
		String str = "";
		for (int i = 0; i < level; i++)
			str += "\t";
		return str;
	}

}

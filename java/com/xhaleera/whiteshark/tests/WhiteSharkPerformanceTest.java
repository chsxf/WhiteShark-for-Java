package com.xhaleera.whiteshark.tests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import com.xhaleera.whiteshark.WhiteSharkConstants;
import com.xhaleera.whiteshark.WhiteSharkExternalClassMapper;
import com.xhaleera.whiteshark.WhiteSharkImmediateDeserializer;
import com.xhaleera.whiteshark.WhiteSharkProgressiveDeserializer;
import com.xhaleera.whiteshark.WhiteSharkSerializer;

public class WhiteSharkPerformanceTest {

	private static final int RUN_COUNT = 10000;
	private static final int PROGRESS_STEP = 1000;
	
	public static void main(String[] args) {
		try {
			WhiteSharkExternalClassMapper classMapper = new WhiteSharkExternalClassMapper();
			classMapper.mapClass(Employee.class, "Xhaleera::WhiteShark::Tests::Employee");
			
			Team data = Employee.buildTestData();
			
			String streamId = "TEST";
			
			ByteArrayOutputStream oStream; 
			ByteArrayInputStream iStream;
			
			// Serializing
			long start = System.currentTimeMillis();
			System.out.println("Serializing...");
			for (int i = 0; i < RUN_COUNT; i++) {
				oStream = new ByteArrayOutputStream();
				WhiteSharkSerializer.serialize(streamId, oStream, data, classMapper);
				oStream.close();
				if (i % PROGRESS_STEP == 0)
					System.out.print(String.format("%d%% ", (i / PROGRESS_STEP) * 10));
			}
			long duration = System.currentTimeMillis() - start;
			System.out.println("100%");
			System.out.println(String.format("%d runs in %f s (%f ms / run)", RUN_COUNT, (float) duration / 1000f, (float) duration / (float) RUN_COUNT));
			System.out.println("");
			System.gc();
			
			// Serializing (Java native serialization)
			start = System.currentTimeMillis(); 
			System.out.println("Serializing (Java native)...");
			for (int i = 0; i < RUN_COUNT; i++) {
				oStream = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(oStream);
				oos.writeObject(data);
				oStream.close();
				if (i % PROGRESS_STEP == 0)
					System.out.print(String.format("%d%% ", (i / PROGRESS_STEP) * 10));
			}
			duration = System.currentTimeMillis() - start;
			System.out.println("100%");
			System.out.println(String.format("%d runs in %f s (%f ms / run)", RUN_COUNT, (float) duration / 1000f, (float) duration / (float) RUN_COUNT));
			System.out.println("");
			System.gc();
			
			// Serializing (JSON serialization)
			start = System.currentTimeMillis();
			System.out.println("Serializing (JSON)...");
			for (int i = 0; i < RUN_COUNT; i++) {
				oStream = new ByteArrayOutputStream();
				JSONObject json = buildJSONObject(data);
				oStream.write(json.toString().getBytes("UTF-8"));
				oStream.close();
				if (i % PROGRESS_STEP == 0)
					System.out.print(String.format("%d%% ", (i / PROGRESS_STEP) * 10));
			}
			duration = System.currentTimeMillis() - start;
			System.out.println("100%");
			System.out.println(String.format("%d runs in %f s (%f ms / run)", RUN_COUNT, (float) duration / 1000f, (float) duration / (float) RUN_COUNT));
			System.out.println("");
			System.gc();
			
			// Deserializing (immediate)
			oStream = new ByteArrayOutputStream();
			WhiteSharkSerializer.serialize(streamId, oStream, data, classMapper);
			byte[] b = oStream.toByteArray();
			oStream.close();
			start = System.currentTimeMillis();
			System.out.println("Deserializing (immediate) ...");
			for (int i = 0; i < RUN_COUNT; i++) {
				iStream = new ByteArrayInputStream(b);
				WhiteSharkImmediateDeserializer.deserialize(streamId, iStream, classMapper);
				iStream.close();
				if (i % PROGRESS_STEP == 0)
					System.out.print(String.format("%d%% ", (i / PROGRESS_STEP) * 10));
			}
			duration = System.currentTimeMillis() - start;
			System.out.println("100%");
			System.out.println(String.format("%d runs in %f s (%f ms / run)", RUN_COUNT, (float) duration / 1000f, (float) duration / (float) RUN_COUNT));
			System.out.println("");
			System.gc();
			
			// Deserializing (progressive)
			start = System.currentTimeMillis();
			System.out.println("Deserializing (progressive) ...");
			for (int i = 0; i < RUN_COUNT; i++) {
				WhiteSharkProgressiveDeserializer deserializer = new WhiteSharkProgressiveDeserializer(streamId, classMapper);
				deserializer.finalize(b, 0, b.length);
				if (i % PROGRESS_STEP == 0)
					System.out.print(String.format("%d%% ", (i / PROGRESS_STEP) * 10));
			}
			duration = System.currentTimeMillis() - start;
			System.out.println("100%");
			System.out.println(String.format("%d runs in %f s (%f ms / run)", RUN_COUNT, (float) duration / 1000f, (float) duration / (float) RUN_COUNT));
			System.out.println("");
			System.gc();
			
			// Deserializing (Java native serialization)
			oStream = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(oStream);
			oos.writeObject(data);
			b = oStream.toByteArray();
			oStream.close();
			start = System.currentTimeMillis(); 
			System.out.println("Deerializing (Java native)...");
			for (int i = 0; i < RUN_COUNT; i++) {
				iStream = new ByteArrayInputStream(b);
				ObjectInputStream ois = new ObjectInputStream(iStream);
				ois.readObject();
				iStream.close();
				if (i % PROGRESS_STEP == 0)
					System.out.print(String.format("%d%% ", (i / PROGRESS_STEP) * 10));
			}
			duration = System.currentTimeMillis() - start;
			System.out.println("100%");
			System.out.println(String.format("%d runs in %f s (%f ms / run)", RUN_COUNT, (float) duration / 1000f, (float) duration / (float) RUN_COUNT));
			System.out.println("");
			System.gc();
			
			// Deserializing (JSON serialization)
			oStream = new ByteArrayOutputStream();
			JSONObject json = buildJSONObject(data);
			oStream.write(json.toString().getBytes("UTF-8"));
			start = System.currentTimeMillis();
			System.out.println("Deserializing (JSON)...");
			for (int i = 0; i < RUN_COUNT; i++) {
				new JSONObject(oStream.toString("UTF-8"));
				if (i % PROGRESS_STEP == 0)
					System.out.print(String.format("%d%% ", (i / PROGRESS_STEP) * 10));
			}
			duration = System.currentTimeMillis() - start;
			oStream.close();
			System.out.println("100%");
			System.out.println(String.format("%d runs in %f s (%f ms / run)", RUN_COUNT, (float) duration / 1000f, (float) duration / (float) RUN_COUNT));
			System.out.println("");
			System.gc();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static JSONObject buildJSONObject(Team data) {
		JSONObject json = new JSONObject();
		JSONArray arr = new JSONArray();
		for (int md : data.monthDays)
			arr.put(md);
		json.put("monthDays", arr);
		arr = new JSONArray();
		for (Employee e : data)
			arr.put(e.toJSON());
		json.put(WhiteSharkConstants.COLLECTION_ITEM_PROPERTY_NAME, arr);
		return json;
	}

}

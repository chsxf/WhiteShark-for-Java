package com.xhaleera.whiteshark.tests;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.json.JSONArray;

import com.xhaleera.whiteshark.WhiteSharkImmediateDeserializer;
import com.xhaleera.whiteshark.WhiteSharkProgressiveDeserializer;
import com.xhaleera.whiteshark.WhiteSharkSerializer;

public class WhiteSharkPerformanceTest {

	private static final int RUN_COUNT = 10000;
	private static final int PROGRESS_STEP = 1000;
	
	public static void main(String[] args) {
		try {
			Employee[] data = Employee.buildTestData();
			
			String streamId = "TEST";
			String path = "/Users/christophe/Desktop/Test.bin";
			
			FileOutputStream fileStream;
			FileInputStream inStream;
			
			// Serializing
			long start = System.currentTimeMillis();
			System.out.println("Serializing...");
			for (int i = 0; i < RUN_COUNT; i++) {
				fileStream = new FileOutputStream(new File(path));
				WhiteSharkSerializer.serialize(streamId, fileStream, data);
				fileStream.close();
				if (i % PROGRESS_STEP == 0)
					System.out.print(String.format("%d%% ", (i / PROGRESS_STEP) * 10));
			}
			long duration = System.currentTimeMillis() - start;
			System.out.println("100%");
			System.out.println(String.format("%d runs in %f s (%d ms / run)", RUN_COUNT, (float) duration / 1000f, duration / 100000));
			System.out.println("");
			
			// Serializing (Java native serialization)
			start = System.currentTimeMillis(); 
			System.out.println("Serializing (Java native)...");
			for (int i = 0; i < RUN_COUNT; i++) {
				fileStream = new FileOutputStream(new File("/Users/christophe/Desktop/Test-java.bin"));
				ObjectOutputStream oos = new ObjectOutputStream(fileStream);
				oos.writeObject(data);
				fileStream.close();
				if (i % PROGRESS_STEP == 0)
					System.out.print(String.format("%d%% ", (i / PROGRESS_STEP) * 10));
			}
			duration = System.currentTimeMillis() - start;
			System.out.println("100%");
			System.out.println(String.format("%d runs in %f s (%d ms / run)", RUN_COUNT, (float) duration / 1000f, duration / 100000));
			System.out.println("");
			
			// Serializing (JSON serialization)
			start = System.currentTimeMillis();
			System.out.println("Serializing (JSON)...");
			for (int i = 0; i < RUN_COUNT; i++) {
				fileStream = new FileOutputStream(new File("/Users/christophe/Desktop/Test-json.bin"));
				JSONArray arr = new JSONArray();
				for (Employee e : data)
					arr.put(e.toJSON());
				fileStream.write(arr.toString().getBytes("UTF-8"));
				fileStream.close();
				if (i % PROGRESS_STEP == 0)
					System.out.print(String.format("%d%% ", (i / PROGRESS_STEP) * 10));
			}
			duration = System.currentTimeMillis() - start;
			System.out.println("100%");
			System.out.println(String.format("%d runs in %f s (%d ms / run)", RUN_COUNT, (float) duration / 1000f, duration / 100000));
			System.out.println("");
			
			// Deserializing (immediate)
			start = System.currentTimeMillis();
			System.out.println("Deserializing (immediate) ...");
			for (int i = 0; i < RUN_COUNT; i++) {
				inStream = new FileInputStream(new File(path));
				WhiteSharkImmediateDeserializer.deserialize(streamId, inStream);
				inStream.close();
				if (i % PROGRESS_STEP == 0)
					System.out.print(String.format("%d%% ", (i / PROGRESS_STEP) * 10));
			}
			duration = System.currentTimeMillis() - start;
			System.out.println("100%");
			System.out.println(String.format("%d runs in %f s (%d ms / run)", RUN_COUNT, (float) duration / 1000f, duration / 100000));
			System.out.println("");
			
			// Deserializing (progressive)
			WhiteSharkProgressiveDeserializer.DeserializationResult result = null;
			byte[] b = new byte[256];
			int c;
			start = System.currentTimeMillis();
			System.out.println("Deserializing (progressive) ...");
			for (int i = 0; i < RUN_COUNT; i++) {
				inStream = new FileInputStream(new File(path));
				WhiteSharkProgressiveDeserializer deserializer = new WhiteSharkProgressiveDeserializer(streamId);
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
				if (i % PROGRESS_STEP == 0)
					System.out.print(String.format("%d%% ", (i / PROGRESS_STEP) * 10));
			}
			duration = System.currentTimeMillis() - start;
			System.out.println("100%");
			System.out.println(String.format("%d runs in %f s (%d ms / run)", RUN_COUNT, (float) duration / 1000f, duration / 100000));
			System.out.println("");
			
			// Deserializing (Java native serialization)
			start = System.currentTimeMillis(); 
			System.out.println("Deerializing (Java native)...");
			for (int i = 0; i < RUN_COUNT; i++) {
				inStream = new FileInputStream(new File("/Users/christophe/Desktop/Test-java.bin"));
				ObjectInputStream ois = new ObjectInputStream(inStream);
				ois.readObject();
				inStream.close();
				if (i % PROGRESS_STEP == 0)
					System.out.print(String.format("%d%% ", (i / PROGRESS_STEP) * 10));
			}
			duration = System.currentTimeMillis() - start;
			System.out.println("100%");
			System.out.println(String.format("%d runs in %f s (%d ms / run)", RUN_COUNT, (float) duration / 1000f, duration / 100000));
			System.out.println("");
			
			// Deserializing (JSON serialization)
			start = System.currentTimeMillis();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			System.out.println("Deserializing (JSON)...");
			for (int i = 0; i < RUN_COUNT; i++) {
				baos.reset();
				inStream = new FileInputStream(new File("/Users/christophe/Desktop/Test-json.bin"));
				do {
					c = inStream.read(b);
					if (c != -1)
						baos.write(b, 0, c);
				}
				while (c != -1);
				inStream.close();
				new JSONArray(baos.toString("UTF-8"));
				if (i % PROGRESS_STEP == 0)
					System.out.print(String.format("%d%% ", (i / PROGRESS_STEP) * 10));
			}
			duration = System.currentTimeMillis() - start;
			System.out.println("100%");
			System.out.println(String.format("%d runs in %f s (%d ms / run)", RUN_COUNT, (float) duration / 1000f, duration / 100000));
			System.out.println("");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}

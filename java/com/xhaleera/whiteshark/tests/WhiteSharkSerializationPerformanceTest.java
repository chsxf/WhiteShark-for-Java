package com.xhaleera.whiteshark.tests;

import java.io.ByteArrayOutputStream;

import com.xhaleera.whiteshark.WhiteSharkExternalClassMapper;
import com.xhaleera.whiteshark.WhiteSharkSerializer;

public class WhiteSharkSerializationPerformanceTest {

	private static final int RUN_COUNT = 10000;
	private static final int PROGRESS_STEP = 1000;
	
	public static void main(String[] args) {
		try {
			WhiteSharkExternalClassMapper classMapper = new WhiteSharkExternalClassMapper();
			classMapper.mapClass(Employee.class, "Xhaleera::WhiteShark::Tests::Employee");
			
			Team data = Employee.buildTestData();
			
			String streamId = "TEST";
			
			ByteArrayOutputStream oStream; 
			
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
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}

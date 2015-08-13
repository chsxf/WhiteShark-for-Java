package com.xhaleera.whiteshark.tests;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.xhaleera.whiteshark.WhiteSharkConstants;
import com.xhaleera.whiteshark.annotations.WhiteSharkSerializable;
import com.xhaleera.whiteshark.annotations.WhiteSharkSerializableMap;

@WhiteSharkSerializableMap
public class Employee extends HashMap<String,Integer> implements Serializable {

	static final long serialVersionUID = 1;
	
	@WhiteSharkSerializable
	public String firstName;
	@WhiteSharkSerializable
	public String lastName;
	@WhiteSharkSerializable
	public int age;
	@WhiteSharkSerializable
	public boolean man;
	@WhiteSharkSerializable
	@WhiteSharkSerializableMap
	public HashMap<String,Boolean> skills;
	
	public Employee() { }
	
	public Employee(String firstName, String lastName, int age, boolean man) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
		this.man = man;
		
		this.put("missing", 0);
		this.put("ill", 2);
		this.put("vacation", 10);
		this.put("years", 3);
		
		this.skills = new HashMap<>();
		this.skills.put("management", true);
		this.skills.put("human_resources", false);
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("firstName", firstName);
		json.put("lastName", lastName);
		json.put("age", age);
		json.put("man", man);
		
		for (Map.Entry<String, Integer> entry : this.entrySet())
			json.put(WhiteSharkConstants.MAP_PROPERTY_NAME_PREFIX + entry.getKey(), entry.getValue().intValue());
		
		JSONObject sub = new JSONObject();
		for (Map.Entry<String, Boolean> entry : this.skills.entrySet())
			sub.put(entry.getKey(), entry.getValue());
		json.put("skills", sub);
		
		return json;
	}

	public static Employee[] buildTestData() {
		return new Employee[]{
				new Employee("Charlotte", "HUMBERT", 30, false),
				new Employee("Eric", "BALLET", 38, true),
				new Employee("Charles", "SAVEUR", 35, true),
				new Employee("Carli", "BRUNA", 26, false),
				new Employee("William", "MARTIN", 31, true),
				new Employee("Marine", "DAVID", 35, true)
			};
	}
	
}

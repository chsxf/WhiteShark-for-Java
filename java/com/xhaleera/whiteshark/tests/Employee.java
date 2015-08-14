package com.xhaleera.whiteshark.tests;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import com.xhaleera.whiteshark.WhiteSharkConstants;
import com.xhaleera.whiteshark.annotations.WhiteSharkSerializable;
import com.xhaleera.whiteshark.annotations.WhiteSharkSerializableCollection;
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
	public float height;
	@WhiteSharkSerializable
	@WhiteSharkSerializableMap
	public HashMap<String,String> meta;
	@WhiteSharkSerializable
	@WhiteSharkSerializableCollection
	public Vector<String> skills;
	
	public Employee() { }
	
	public Employee(String firstName, String lastName, int age, boolean man, float height) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
		this.man = man;
		this.height = height;
		
		this.put("missing", 0);
		this.put("ill", 2);
		this.put("vacation", 10);
		this.put("years", 3);
		
		this.meta = new HashMap<>();
		this.meta.put("dob", "1901-01-01");
		this.meta.put("entry date", "1902-01-01");
		
		this.skills = new Vector<>();
		this.skills.add("management");
		this.skills.add("human_resources");
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("firstName", firstName);
		json.put("lastName", lastName);
		json.put("age", age);
		json.put("man", man);
		json.put("height", height);
		
		for (Map.Entry<String, Integer> entry : this.entrySet())
			json.put(WhiteSharkConstants.MAP_PROPERTY_NAME_PREFIX + entry.getKey(), entry.getValue().intValue());
		
		JSONObject sub = new JSONObject();
		for (Map.Entry<String,String> entry : this.meta.entrySet())
			sub.put(entry.getKey(), entry.getValue());
		json.put("meta", sub);
		
		JSONArray arr = new JSONArray();
		for (String s : skills)
			arr.put(s);
		json.put("skills", arr);
		
		return json;
	}

	public static Team buildTestData() {
		Team t = new Team();
		t.add(new Employee("Charlotte", "HUMBERT", 30, false, 1.8f));
		t.add(new Employee("Eric", "BALLET", 38, true, 1.65f));
		t.add(new Employee("Charles", "SAVEUR", 35, true, 1.8f));
		t.add(new Employee("Carli", "BRUNA", 26, false, 1.6f));
		t.add(new Employee("William", "MARTIN", 31, true, 1.75f));
		t.add(new Employee("Marine", "DAVID", 35, true, 1.55f));
		return t;
	}
	
}

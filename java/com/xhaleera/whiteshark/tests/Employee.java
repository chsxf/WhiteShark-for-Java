package com.xhaleera.whiteshark.tests;

import java.io.Serializable;

import org.json.JSONObject;

import com.xhaleera.whiteshark.annotations.WhiteSharkSerializable;

public class Employee implements Serializable {

	static final long serialVersionUID = 1;
	
	@WhiteSharkSerializable
	public String firstName;
	@WhiteSharkSerializable
	public String lastName;
	@WhiteSharkSerializable
	public int age;
	@WhiteSharkSerializable
	public boolean man;
	
	public Employee() { }
	
	public Employee(String firstName, String lastName, int age, boolean man) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
		this.man = man;
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("firstName", firstName);
		json.put("lastName", lastName);
		json.put("age", age);
		json.put("man", man);
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

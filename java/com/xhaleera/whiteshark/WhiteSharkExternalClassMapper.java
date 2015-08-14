package com.xhaleera.whiteshark;

import java.util.HashMap;
import java.util.Map;

public final class WhiteSharkExternalClassMapper {

	private HashMap<Class<?>, String> map;
	
	public WhiteSharkExternalClassMapper() {
		map = new HashMap<>();
	}

	public void mapClass(Class<?> cls, String externalClass) {
		map.put(cls, externalClass);
	}
	
	public void unmapClass(Class<?> cls) {
		map.remove(cls);
	}
	
	public String getExternalFromClass(Class<?> cls) {
		return map.containsKey(cls) ? map.get(cls) : cls.getCanonicalName();
	}
	
	public Class<?> getClassFromExternal(String externalClass) throws ClassNotFoundException {
		if (map.containsValue(externalClass)) {
			for (Map.Entry<Class<?>, String> entry : map.entrySet()) {
				if (entry.getValue().equals(externalClass))
					return entry.getKey();
			}
		}
		return WhiteSharkUtils.classForName(externalClass);
	}
	
}

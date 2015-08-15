package com.xhaleera.whiteshark;

import java.util.HashMap;
import java.util.Map;

/**
 * WhiteShark external class mapper
 * <p>
 * This class is used to map Java classes to external classes,
 * allowing class mapping with other languages, such as PHP,
 * C# or even other Java implementations.
 * 
 * @author Christophe SAUVEUR (christophe@xhaleera.com)
 * @since 1.0
 * @version 1.0
 */
public final class WhiteSharkExternalClassMapper {

	/** Classes map */
	private HashMap<Class<?>, String> map;
	
	/**
	 * Construtor
	 */
	public WhiteSharkExternalClassMapper() {
		map = new HashMap<>();
	}

	/**
	 * Maps a Java to an external class' name
	 * @param cls Java class
	 * @param externalClass External class' name
	 */
	public void mapClass(Class<?> cls, String externalClass) {
		map.put(cls, externalClass);
	}
	
	/**
	 * Unmaps a Java class
	 * @param cls Java class
	 */
	public void unmapClass(Class<?> cls) {
		map.remove(cls);
	}
	
	/**
	 * Get external class name from a Java class name.
	 * @param cls Java class
	 * @return the mapped external class' name, or the Java class' canonical name if it is not mapped.
	 */
	public String getExternalFromClass(Class<?> cls) {
		return map.containsKey(cls) ? map.get(cls) : cls.getCanonicalName();
	}
	
	/**
	 * Get Java class from an external class' name.
	 * @param externalClass External class' name
	 * @return the mapped Java class, or the Java class matching the name if it is not mapped.
	 * @throws ClassNotFoundException If the external class' name is not mapped and no Java class matches it.
	 */
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

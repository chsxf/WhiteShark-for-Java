package com.xhaleera.whiteshark;

import java.util.HashMap;

/**
 * WhiteShark generic object class
 * <p>
 * This class is used to store data of objects serialized as generics.
 * Internally, it is implemented as a {@link HashMap}{@code <String,Object>}.
 * 
 * @author Christophe SAUVEUR (christophe@xhaleera.com)
 * @since 1.0
 * @version 1.0
 */
public final class WhiteSharkGenericObject extends HashMap<String, Object> {

	static final long serialVersionUID = 1;

	/**
	 * Constructor
	 * @param initialCapacity Initial capacity of the map. See {@link HashMap}
	 */
	public WhiteSharkGenericObject(int initialCapacity) {
		super(initialCapacity);
	}

}

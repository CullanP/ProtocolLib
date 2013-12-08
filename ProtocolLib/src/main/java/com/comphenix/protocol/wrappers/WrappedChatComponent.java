package com.comphenix.protocol.wrappers;

import org.bukkit.ChatColor;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.FuzzyReflection.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;

/**
 * Represents a chat component added in Minecraft 1.7.2
 * @author Kristian
 */
public class WrappedChatComponent {
	private static final Class<?> SERIALIZER = MinecraftReflection.getChatSerializer();
	private static final Class<?> COMPONENT = MinecraftReflection.getIChatBaseComponent();
	private static MethodAccessor SERIALIZE_COMPONENT = null;
	private static MethodAccessor DESERIALIZE_COMPONENT = null;
	private static MethodAccessor CONSTRUCT_COMPONENT = null;
	
	static {
		FuzzyReflection fuzzy = FuzzyReflection.fromClass(SERIALIZER);
		
		// Retrieve the correct methods
		SERIALIZE_COMPONENT = FuzzyReflection.getMethodAccessor(
			fuzzy.getMethodByParameters("serialize", String.class, new Class<?>[] { COMPONENT }));
		DESERIALIZE_COMPONENT = FuzzyReflection.getMethodAccessor(
				fuzzy.getMethodByParameters("serialize", COMPONENT, new Class<?>[] { String.class }));
	
		// Get a component from a standard Minecraft message
		CONSTRUCT_COMPONENT = FuzzyReflection.getMethodAccessor(
			MinecraftReflection.getCraftChatMessage(), "fromString", String.class);
	}
	
	private Object handle;
	private transient String cache;
	
	private WrappedChatComponent(Object handle, String cache) {
		this.handle = handle;
		this.cache = cache;
	}
	
	/**
	 * Construct a new chat component wrapper around the given NMS object.
	 * @param handle - the NMS object. 
	 * @return The wrapper.
	 */
	public static WrappedChatComponent fromHandle(Object handle) {
		if (handle == null)
			throw new IllegalArgumentException("handle cannot be NULL.");
		if (!COMPONENT.isAssignableFrom(handle.getClass()))
			throw new IllegalArgumentException("handle (" + handle + ") is not a " + COMPONENT);
		return new WrappedChatComponent(handle, null);
	}
	
	/**
	 * Construct a new chat component wrapper from the given JSON string.
	 * @param json - the json.
	 * @return The chat component wrapper.
	 */
	public static WrappedChatComponent fromJson(String json) {
		return new WrappedChatComponent(DESERIALIZE_COMPONENT.invoke(null, json), json);
	}
	
	/**
	 * Construct an array of chat components from a standard Minecraft message.
	 * <p>
	 * This uses {@link ChatColor} for formating.
	 * @param message - the message.
	 * @return The equivalent chat components.
	 */
	public static WrappedChatComponent[] fromChatMessage(String message) {
		Object[] components = (Object[]) CONSTRUCT_COMPONENT.invoke(null, message);
		WrappedChatComponent[] result = new WrappedChatComponent[components.length];
		
		for (int i = 0; i < components.length; i++) {
			result[i] = fromHandle(components[i]);
		}
		return result;
	}
	
	/**
	 * Retrieve a copy of this component as a JSON string.
	 * <p>
	 * Note that any modifications to this JSON string will not update the current component.
	 * @return The JSON representation of this object.
	 */
	public String getJson() {
		if (cache == null) {
			cache = (String) SERIALIZE_COMPONENT.invoke(null, handle);
		}
		return cache;
 	}
	
	/**
	 * Set the content of this component using a JSON object.
	 * @param obj - the JSON that represents the new component.
	 */
	public void setJson(String obj) {
		this.handle = DESERIALIZE_COMPONENT.invoke(null, obj);
		this.cache = obj;
	}
	
	/**
	 * Retrieve the underlying IChatBaseComponent instance.
	 * @return The underlying instance.
	 */
	public Object getHandle() {
		return handle;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof WrappedChatComponent) {
			return ((WrappedChatComponent) obj).handle.equals(handle);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return handle.hashCode();
	}
}
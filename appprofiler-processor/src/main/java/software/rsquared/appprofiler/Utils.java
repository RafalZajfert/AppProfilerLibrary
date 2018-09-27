package software.rsquared.appprofiler;

import java.util.Set;

/**
 * @author Rafał Zajfert
 */
class Utils {

	/**
	 * Convert given name to capitalized camel case convention
	 *
	 * @param name {@link String} to convert
	 * @return capitalized camel case name
	 */
	static String toCapitalizedCamelCase(String name) {
		name = name.trim()
				.replaceAll("[^a-zA-Z0-9]", "_")
				.replaceAll("__", "_")
				.toLowerCase();
		StringBuilder builder = new StringBuilder();
		for (String chunk : name.split("_")) {
			if (chunk.length() > 0) {
				builder.append(String.valueOf(chunk.charAt(0)).toUpperCase()).append(chunk.substring(1));
			}
		}
		return builder.toString();
	}


	/**
	 * Convert given name to camel case convention
	 *
	 * @param name {@link String} to convert
	 * @return camel case name
	 */
	public static String toCamelCase(String name) {
		name = name.trim()
				.replaceAll("[^a-zA-Z0-9]", "_")
				.replaceAll("__", "_")
				.toLowerCase();
		StringBuilder builder = new StringBuilder();
		for (String chunk : name.split("_")) {
			if (chunk.length() > 0) {
				String firstChar = builder.length() > 0 ? String.valueOf(chunk.charAt(0)).toUpperCase() : String.valueOf(chunk.charAt(0));
				builder.append(firstChar).append(chunk.substring(1));
			}
		}
		return builder.toString();
	}

	/**
	 * This method is searching for default profile in {@link ProfilerDescription profilerDescription} if none is default than first profile will be returned
	 *
	 * @param profilerDescription profiler with profiles
	 * @return default or first profile
	 */
	static ProfileDescription getDefaultOrFirstProfile(ProfilerDescription profilerDescription) {
		if (profilerDescription == null) {
			return null;
		}
		Set<ProfileDescription> profiles = profilerDescription.getProfiles();
		if (profiles == null || profiles.isEmpty()) {
			return null;
		}
		ProfileDescription defaultProfile = null;
		for (ProfileDescription profile : profiles) {
			if (profile.isDefaultProfile()) {
				return profile;
			}
			if (defaultProfile == null) {
				defaultProfile = profile;
			}
		}
		return defaultProfile;
	}

	/**
	 * check if at least one profile is marked as default
	 *
	 * @param profilerDescription profiler with profiles
	 * @return true if at least one profile is marked as default, false otherwise
	 */
	static boolean hasDefaultProfile(ProfilerDescription profilerDescription) {
		ProfileDescription defaultProfile = getDefaultOrFirstProfile(profilerDescription);
		return defaultProfile != null && defaultProfile.isDefaultProfile();
	}

	public static ProfileDescription getCustomProfile(ProfilerDescription profilerDescription) {
		if (profilerDescription == null) {
			return null;
		}
		Set<ProfileDescription> profiles = profilerDescription.getProfiles();
		if (profiles == null || profiles.isEmpty()) {
			return null;
		}
		for (ProfileDescription profile : profiles) {
			if ("Custom".equals(profile.getName())) {
				return profile;
			}
		}
		return null;
	}

	/**
	 * get object class for given type (e.g. {@link Integer} instead of {@code int})
	 *
	 * @param valueType type of value
	 * @return class of type
	 */
	static Class<?> getClassFor(ValueType valueType) {
		switch (valueType) {
			case INT:
				return Integer.class;
			case LONG:
				return Long.class;
			case BOOLEAN:
				return Boolean.class;
			case FLOAT:
				return Float.class;
			case STRING:
			default:
				return String.class;
		}
	}


	/**
	 * get format of value for given type
	 *
	 * @param valueType type of value
	 * @return format
	 */
	static String getTypeFormat(ValueType valueType) {
		switch (valueType) {
			case STRING:
				return "$S";
			case FLOAT:
				return "$Lf";
			case LONG:
				return "$Ll";
			case INT:
			case BOOLEAN:
			default:
				return "$L";
		}
	}

	/**
	 * Returns default value for the field, if value from defaultProfile is null then will be returned default value form field
	 *
	 * @param fieldDescription Field for which value should be searched
	 * @param defaultProfile   default profile with values
	 * @return value for the field or null
	 */
	static String getDefaultFieldValue(FieldDescription fieldDescription, ProfileDescription defaultProfile) {
		String fieldValue = defaultProfile == null ? null : defaultProfile.getValueFor(fieldDescription.getName());
		if (isValueNull(fieldValue)) {
			fieldValue = fieldDescription.getDefaultValue();
		}
		if (isValueNull(fieldValue)) {
			return null;
		}
		return fieldValue;
	}

	/**
	 * checks if value is {@code null} or {@link Profile#NULL}
	 * @param fieldValue value of the field
	 * @return true if {@code null} or {@link Profile#NULL}, false otherwise
	 */
	private static boolean isValueNull(String fieldValue) {
		return fieldValue == null || Profile.NULL.equals(fieldValue);
	}
}

package software.rsquared.appprofiler;

import java.util.Arrays;
import java.util.Set;

/**
 * @author Rafal Zajfert
 */
class ProfileDescription {
	private String name;
	private boolean defaultProfile;
	private Set<ValueDescription> values;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isDefaultProfile() {
		return defaultProfile;
	}

	public void setDefaultProfile(boolean defaultProfile) {
		this.defaultProfile = defaultProfile;
	}

	public Set<ValueDescription> getValues() {
		return values;
	}

	public void setValues(Set<ValueDescription> values) {
		this.values = values;
	}

	@Override
	public String toString() {
		return "ProfileDescription{" +
				"name='" + name + '\'' +
				", defaultProfile=" + defaultProfile +
				", values=" + (values == null ? "null" : Arrays.toString(values.toArray())) +
				'}';
	}

	public String getValueFor(String name) {
		if (values !=null) {
			for (ValueDescription value : values) {
				if (value.getName().equals(name)) {
					return value.getValue();
				}
			}
		}
		return null;
	}
}
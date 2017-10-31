package software.rsquared.appprofiler;

import java.util.Arrays;
import java.util.Set;

/**
 * @author Rafal Zajfert
 */
class ProfilerDescription {

	private String activityClass;

	private String packageName;

	private boolean active;

	private Set<FieldDescription> fields;

	private Set<ProfileDescription> profiles;

	public String getActivityClass() {
		return activityClass;
	}

	public void setActivityClass(String activityClass) {
		this.activityClass = activityClass;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Set<FieldDescription> getFields() {
		return fields;
	}

	public void setFields(Set<FieldDescription> fields) {
		this.fields = fields;
	}

	public Set<ProfileDescription> getProfiles() {
		return profiles;
	}

	public void setProfiles(Set<ProfileDescription> profiles) {
		this.profiles = profiles;
	}

	@Override
	public String toString() {
		return "ProfilerDescription{" +
				"activityClass='" + activityClass + '\'' +
				", packageName='" + packageName + '\'' +
				", active=" + active +
				", fields=" + (fields == null ? "null" : Arrays.toString(fields.toArray())) +
				", profiles=" + (profiles == null ? "null" : Arrays.toString(profiles.toArray())) +
				'}';
	}
}

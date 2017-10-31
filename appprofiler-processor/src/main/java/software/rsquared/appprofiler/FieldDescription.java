package software.rsquared.appprofiler;

import java.util.Arrays;

import sftware.rsquared.appprofiler.ValueType;

/**
 * @author Rafal Zajfert
 */
class FieldDescription {

	private String name;
	private String capitalizedCamelCaseName;
	private String camelCaseName;

	private String label;

	private ValueType valueType;

	private String[] values;

	private String defaultValue;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		this.capitalizedCamelCaseName = Utils.toCapitalizedCamelCase(name);
		this.camelCaseName = Utils.toCamelCase(name);
	}

	public String getCapitalizedCamelCaseName() {
		return capitalizedCamelCaseName;
	}

	public String getCamelCaseName() {
		return camelCaseName;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public ValueType getValueType() {
		return valueType;
	}

	public void setValueType(ValueType valueType) {
		this.valueType = valueType;
	}

	public String[] getValues() {
		return values;
	}

	public void setValues(String[] values) {
		this.values = values;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public String toString() {
		return "FieldDescription{" +
				"name='" + name + '\'' +
				", label='" + label + '\'' +
				", valueType=" + valueType +
				", values=" + Arrays.toString(values) +
				", defaultValue='" + defaultValue + '\'' +
				'}';
	}
}

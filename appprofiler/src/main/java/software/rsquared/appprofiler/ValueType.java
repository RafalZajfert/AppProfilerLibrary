package software.rsquared.appprofiler;

/**
 * @author Rafal Zajfert
 */
public enum ValueType {
	INT,
	LONG,
	STRING,
	BOOLEAN,
	FLOAT;

	public Object parseValue(String value) {
		switch (this) {
			case INT:
				try {
					return Integer.parseInt(value);
				} catch (NumberFormatException e) {
					return 0;
				}
			case LONG:
				try {
					return Long.parseLong(value);
				} catch (NumberFormatException e) {
					return 0L;
				}
			case BOOLEAN:
				try {
					return Boolean.parseBoolean(value);
				} catch (NumberFormatException e) {
					return false;
				}
			case FLOAT:
				try {
					return Float.parseFloat(value);
				} catch (NumberFormatException e) {
					return 0f;
				}
			case STRING:
			default:
				return value;
		}
	}
}

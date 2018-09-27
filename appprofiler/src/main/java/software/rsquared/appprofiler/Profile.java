package software.rsquared.appprofiler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Rafal Zajfert
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Profile {
	String NULL = "__value_null";

	String name();

	Profile.Value[] values();

	boolean defaultProfile() default false;

	@Retention(RetentionPolicy.SOURCE)
	@Target(ElementType.ANNOTATION_TYPE)
	 @interface Field {
		String name();

		String label() default NULL;

		ValueType valueType() default ValueType.STRING;

		String[] values() default {};

		String defaultValue() default NULL;
	}

	@Retention(RetentionPolicy.SOURCE)
	@Target(ElementType.ANNOTATION_TYPE)
	@interface Value {

		String name();

		String value() default NULL;
	}

}

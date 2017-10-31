package sftware.rsquared.appprofiler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Rafal Zajfert
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Profiler {
	Class<?> startActivity();

	String packageName() default "sftware.rsquared.appprofiler";

	boolean active() default true;

	Profile.Field[] fields();

	Profile[] defaultProfiles() default {};
}

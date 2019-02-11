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
public @interface Profiler {
	/**
	 * Fully qualified activity name
	 * @return
	 */
	String startActivity();

	String packageName() default "software.rsquared.appprofiler";

	boolean active() default true;

	boolean useAndroidX() default false;

	Profile.Field[] fields();

	Profile[] defaultProfiles() default {};
}

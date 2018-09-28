package software.rsquared.appprofiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;

/**
 * @author Rafał Zajfert
 */

class AppProfilerGenerator extends Generator {
	private static final ClassName classContext = ClassName.get("android.content", "Context");
	private static final ClassName classSharedPreferences = ClassName.get("android.content", "SharedPreferences");

	static void generate(ProfilerDescription profilerDescription) {
		try {
			/**
			 * Generate a class
			 */
			TypeSpec.Builder appProfilerClass = TypeSpec
					.classBuilder("AppProfiler")
					.addModifiers(Modifier.PUBLIC, Modifier.FINAL);

			ProfileDescription defaultProfile = Utils.getDefaultOrFirstProfile(profilerDescription);
			ProfileDescription customProfile = Utils.getCustomProfile(profilerDescription);
			if (defaultProfile == null) {
				messager.printMessage(Diagnostic.Kind.ERROR, "AppProfiler need at least one default profile");
				return;
			}

			appProfilerClass.addField(FieldSpec.builder(OnProfileChangedListener.class, "changedListener", Modifier.PRIVATE, Modifier.STATIC).build());
			if (profilerDescription.isActive()) {
				appProfilerClass.addField(createPreferencesField());
				appProfilerClass.addField(createProfileField());
				for (FieldDescription fieldDescription : profilerDescription.getFields()) {
					appProfilerClass.addField(createField(fieldDescription));
				}
			}

			appProfilerClass.addMethod(createInitMethod(profilerDescription.isActive()));
			appProfilerClass.addMethod(createGetOnProfileChangedListenerMethod());
			if (profilerDescription.isActive()) {
				appProfilerClass.addMethod(createCheckInit());
				appProfilerClass.addMethod(createProfileSetter(profilerDescription.getFields()));
			}
			appProfilerClass.addMethod(createProfileGetter(defaultProfile.getName(), !profilerDescription.isActive()));


			for (FieldDescription fieldDescription : profilerDescription.getFields()) {
				String fieldValue = Utils.getDefaultFieldValue(fieldDescription, defaultProfile);
				appProfilerClass.addMethod(createGetter(fieldDescription, fieldValue, !profilerDescription.isActive()));

				if (profilerDescription.isActive()) {
					appProfilerClass.addMethod(createCustomValueGetter(fieldDescription, Utils.getDefaultFieldValue(fieldDescription, customProfile)));
					appProfilerClass.addMethod(createSetter(fieldDescription));
				}
			}
			/**
			 * Write generated class to a file
			 */
			JavaFile.builder(profilerDescription.getPackageName(), appProfilerClass.build()).indent("\t").build().writeTo(filer);
		} catch (IOException ex) {
			messager.printMessage(Diagnostic.Kind.ERROR, ex.toString());
		}
	}

	private static FieldSpec createPreferencesField() {
		FieldSpec.Builder builder = FieldSpec.builder(classSharedPreferences, "preferences");
		builder.addModifiers(Modifier.PRIVATE, Modifier.STATIC);
		return builder.build();
	}

	private static MethodSpec createInitMethod(boolean initPreferences) {
		MethodSpec.Builder spec = MethodSpec.methodBuilder("init")
				.addParameter(classContext, "context")
				.addParameter(OnProfileChangedListener.class, "profileChangedListener")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
		if (initPreferences) {
			spec.addStatement("preferences = context.getSharedPreferences($S, $T.MODE_PRIVATE)", "profiler.pref", classContext);
		}
		spec.beginControlFlow("if (profileChangedListener != null)");
		spec.addStatement("changedListener = profileChangedListener");
		spec.addStatement("changedListener.onProfileChanged(true)");
		spec.endControlFlow();
		return spec.build();
	}

	private static MethodSpec createGetOnProfileChangedListenerMethod() {
		MethodSpec.Builder spec = MethodSpec.methodBuilder("getOnProfileChangedListener")
				.returns(OnProfileChangedListener.class)
				.addModifiers(Modifier.PROTECTED, Modifier.STATIC, Modifier.FINAL);
		spec.addStatement("return changedListener");
		return spec.build();
	}

	private static FieldSpec createProfileField() {
		return FieldSpec.builder(String.class, "profile", Modifier.PRIVATE, Modifier.STATIC).build();
	}

	private static MethodSpec createProfileGetter(String fieldValue, boolean withoutPreferences) {
		MethodSpec.Builder spec = MethodSpec.methodBuilder("getProfile")
				.addJavadoc("Current profile")
				.returns(String.class)
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
		if (!withoutPreferences) {
			spec.addStatement("checkIsInitialized()");
			spec.beginControlFlow("if (profile == null)");
			spec.addStatement("profile = preferences." + getGetPreferenceMethod(ValueType.STRING) + "($S, " + Utils.getTypeFormat(ValueType.STRING) + ")", "profile_name", fieldValue);
			spec.endControlFlow();
			spec.addStatement("return profile");
		} else {
			spec.addStatement("return " + Utils.getTypeFormat(ValueType.STRING), fieldValue);
		}
		return spec.build();
	}

	private static MethodSpec createProfileSetter(Set<FieldDescription> fields) {
		MethodSpec.Builder spec = MethodSpec.methodBuilder("setProfile")
				.addJavadoc("Current profile")
				.addParameter(String.class, "value")
				.addModifiers(Modifier.PROTECTED, Modifier.STATIC, Modifier.FINAL);

		spec.addStatement("checkIsInitialized()");
		spec.addStatement("profile = value");
		spec.addStatement("preferences.edit()." + getPutPreferenceMethod(ValueType.STRING) + "($S, value).apply()", "profile_name");
		return spec.build();
	}

	private static FieldSpec createField(FieldDescription fieldDescription) {
		return FieldSpec.builder(Utils.getClassFor(fieldDescription.getValueType()), fieldDescription.getCamelCaseName(), Modifier.PRIVATE, Modifier.STATIC).build();
	}

	private static MethodSpec createGetter(FieldDescription fieldDescription, String fieldValue, boolean withoutPreferences) {
		Class<?> type = Utils.getClassFor(fieldDescription.getValueType());

		MethodSpec.Builder spec = MethodSpec.methodBuilder((ValueType.BOOLEAN.equals(fieldDescription.getValueType()) ? "is" : "get") + fieldDescription.getCapitalizedCamelCaseName())
				.addJavadoc(fieldDescription.getLabel())
				.returns(type)
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
		if (!withoutPreferences) {
			spec.addStatement("checkIsInitialized()");
			spec.beginControlFlow("if (" + fieldDescription.getCamelCaseName() + " == null)");
			spec.addStatement(fieldDescription.getCamelCaseName() + " = " + (fieldValue == null ? "!preferences.contains(\"" + fieldDescription.getName() + "\") ? null : " : "") + "preferences." + getGetPreferenceMethod(fieldDescription.getValueType()) + "($S, " + Utils.getTypeFormat(fieldDescription.getValueType()) + ")", fieldDescription.getName(), getFieldValueOrDefault(fieldValue, fieldDescription.getValueType()));
			spec.endControlFlow();
			spec.addStatement("return " + fieldDescription.getCamelCaseName());
		} else {
			spec.addStatement("return " + Utils.getTypeFormat(fieldDescription.getValueType()), fieldValue);
		}
		return spec.build();
	}

	private static MethodSpec createCustomValueGetter(FieldDescription fieldDescription, String fieldValue) {
		Class<?> type = Utils.getClassFor(fieldDescription.getValueType());

		MethodSpec.Builder spec = MethodSpec.methodBuilder("get" + fieldDescription.getCapitalizedCamelCaseName() + "CustomValue")
				.addJavadoc("Value from 'Custom' profile\n" + fieldDescription.getLabel())
				.returns(type)
				.addModifiers(Modifier.PROTECTED, Modifier.STATIC, Modifier.FINAL);
		spec.addStatement("checkIsInitialized()");
		spec.addStatement("return " + (fieldValue == null ? "!preferences.contains(\"" + "CUSTOM_" + fieldDescription.getName() + "\") ? null : " : "") + " preferences." + getGetPreferenceMethod(fieldDescription.getValueType()) + "($S, " + Utils.getTypeFormat(fieldDescription.getValueType()) + ")", "CUSTOM_" + fieldDescription.getName(), getFieldValueOrDefault(fieldValue, fieldDescription.getValueType()));
		return spec.build();
	}

	private static MethodSpec createSetter(FieldDescription fieldDescription) {
		Class<?> type = Utils.getClassFor(fieldDescription.getValueType());

		MethodSpec.Builder spec = MethodSpec.methodBuilder("set" + fieldDescription.getCapitalizedCamelCaseName())
				.addJavadoc(fieldDescription.getLabel())
				.addParameter(type, "value")
				.addModifiers(Modifier.PROTECTED, Modifier.STATIC, Modifier.FINAL);

		spec.addStatement("checkIsInitialized()");
		spec.addStatement(fieldDescription.getCamelCaseName() + " = value");

		spec.addStatement("$T.Editor editor = preferences.edit()", classSharedPreferences);
		spec.beginControlFlow("if (value == null)");
		spec.addStatement("if (profile.equals(\"Custom\")) editor.remove($S)", "CUSTOM_" + fieldDescription.getName());
		spec.addStatement("editor.remove($S).apply()", fieldDescription.getName());
		spec.nextControlFlow("else");

		spec.addStatement("if (profile.equals(\"Custom\")) editor." + getPutPreferenceMethod(fieldDescription.getValueType()) + "($S, value)", "CUSTOM_" + fieldDescription.getName());
		spec.addStatement("editor." + getPutPreferenceMethod(fieldDescription.getValueType()) + "($S, value).apply()", fieldDescription.getName());

		spec.endControlFlow();
		spec.addStatement("editor.apply()");

		return spec.build();
	}

	private static MethodSpec createCheckInit() {

		MethodSpec.Builder spec = MethodSpec.methodBuilder("checkIsInitialized")
				.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);

		spec.beginControlFlow("if (preferences == null)");
		spec.addStatement("throw new $T($S)", IllegalStateException.class, "Profiler is not initialized, call AppProfiler.init(context); in Application class");
		spec.endControlFlow();
		return spec.build();
	}


	private static Object getFieldValueOrDefault(String fieldValue, ValueType valueType) {
		if (fieldValue == null) {
			switch (valueType) {
				case INT:
					return "0";
				case LONG:
					return "0";
				case BOOLEAN:
					return "false";
				case FLOAT:
					return "0";
				case STRING:
				default:
					return null;
			}
		} else {
			return fieldValue;
		}
	}


	/**
	 * Returns shared preferences get method
	 *
	 * @param valueType type of value
	 * @return shared preferences get method e.g "getInt"
	 */
	private static String getGetPreferenceMethod(ValueType valueType) {
		switch (valueType) {
			case INT:
				return "getInt";
			case LONG:
				return "getLong";
			case BOOLEAN:
				return "getBoolean";
			case FLOAT:
				return "getFloat";
			case STRING:
			default:
				return "getString";
		}
	}


	/**
	 * Returns shared preferences get method
	 *
	 * @param valueType type of value
	 * @return shared preferences get method e.g "getInt"
	 */
	private static String getPutPreferenceMethod(ValueType valueType) {
		switch (valueType) {
			case INT:
				return "putInt";
			case LONG:
				return "putLong";
			case BOOLEAN:
				return "putBoolean";
			case FLOAT:
				return "putFloat";
			case STRING:
			default:
				return "putString";
		}
	}

}

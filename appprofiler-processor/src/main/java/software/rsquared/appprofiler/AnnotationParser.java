package software.rsquared.appprofiler;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

import sftware.rsquared.appprofiler.Profile;
import sftware.rsquared.appprofiler.Profiler;
import sftware.rsquared.appprofiler.ValueType;

/**
 * @author Rafał Zajfert
 */
class AnnotationParser {
	private static Messager messager;
	private static ProfilerDescription profilerDescription;

	static void init(ProcessingEnvironment processingEnvironment) {
		messager = processingEnvironment.getMessager();
	}

	static ProfilerDescription parse(RoundEnvironment roundEnvironment) {
		if (profilerDescription == null) {
			Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Profiler.class);
			for (Element element : elements) {
				if (elements.size() > 1) {
					messager.printMessage(Diagnostic.Kind.WARNING, "Only one profiler annotation can be used, only " + element.getSimpleName() + " will be processed!");
				}
				profilerDescription = processProfilesAnnotation(element.getAnnotation(Profiler.class));
			}
		}
		return profilerDescription;
	}

	private static ProfilerDescription processProfilesAnnotation(Profiler profiler) {
		ProfilerDescription profilerDescription = new ProfilerDescription();
		Profile.Field[] fields = profiler.fields();
		Profile[] profiles = profiler.defaultProfiles();

		Set<FieldDescription> fieldDescriptions = new LinkedHashSet<>();
		Set<ProfileDescription> profileDescriptions = new LinkedHashSet<>();

		for (Profile.Field field : fields) {
			String name = field.name();
			String[] values = field.values();
			ValueType valueType = field.valueType();
			FieldDescription fieldDescription = new FieldDescription();
			fieldDescription.setValues(values);
			fieldDescription.setName(name);
			fieldDescription.setDefaultValue(field.defaultValue());
			fieldDescription.setLabel(field.label());
			fieldDescription.setValueType(valueType);
			fieldDescriptions.add(fieldDescription);
		}

		for (Profile profile : profiles) {
			ProfileDescription profileDescription = new ProfileDescription();
			profileDescription.setName(profile.name());
			boolean defaultProfile = profile.defaultProfile();
			if (defaultProfile && Utils.hasDefaultProfile(profilerDescription)) {
				messager.printMessage(Diagnostic.Kind.WARNING, "Only one profile can be default, " + profile.name() + " profile cannot be default!");
				defaultProfile = false;
			}
			profileDescription.setDefaultProfile(defaultProfile);
			Set<ValueDescription> valueDescriptions = new LinkedHashSet<>();
			Profile.Value[] values = profile.values();
			for (Profile.Value profileValue : values) {
				ValueDescription valueDescription = new ValueDescription();
				valueDescription.setName(profileValue.name());
				valueDescription.setValue(profileValue.value());
				valueDescriptions.add(valueDescription);
			}
			profileDescription.setValues(valueDescriptions);
			profileDescriptions.add(profileDescription);
		}

		profilerDescription.setActivityClass(profiler.startActivity());
		profilerDescription.setPackageName(profiler.packageName());
		profilerDescription.setFields(fieldDescriptions);
		profilerDescription.setProfiles(profileDescriptions);
		profilerDescription.setActive(profiler.active());
		return profilerDescription;
	}
}

package software.rsquared.appprofiler;

import java.util.Collections;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;


@SupportedAnnotationTypes("s8ftware.rsquared.appprofiler.Profiler")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AppProfilerProcessor extends AbstractProcessor {
	private boolean created = false;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnvironment) {
		super.init(processingEnvironment);
		Generator.init(processingEnvironment);
		AnnotationParser.init(processingEnvironment);
	}

	@Override
	public Set<String> getSupportedOptions() {
		return super.getSupportedOptions();
	}

	@Override
	public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
		if (created) {
			return true;
		}
		ProfilerDescription profilerDescription = AnnotationParser.parse(roundEnvironment);
		if (!roundEnvironment.processingOver()) {
			AppProfilerGenerator.generate(profilerDescription);
			AppProfilerActivityGenerator.generate(profilerDescription);
			created = true;
		}
		return true;
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Collections.singleton(Profiler.class.getCanonicalName());
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}
}

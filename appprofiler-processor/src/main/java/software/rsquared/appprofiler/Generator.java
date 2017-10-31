package software.rsquared.appprofiler;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;

/**
 * Created by Rafal on 27.10.2017.
 */

abstract class Generator {
	static Filer filer;
	static Messager messager;
	static Elements elements;

	static void init(ProcessingEnvironment processingEnvironment){
		filer = processingEnvironment.getFiler();
		messager = processingEnvironment.getMessager();
		elements = processingEnvironment.getElementUtils();
	}

}

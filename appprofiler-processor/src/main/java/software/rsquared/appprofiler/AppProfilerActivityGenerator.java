package software.rsquared.appprofiler;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;

/**
 * @author Rafał Zajfert
 */
class AppProfilerActivityGenerator extends Generator {
	private static ClassName classAppProfiler;
	private static final ClassName classContext = ClassName.get("android.content", "Context");
	private static final ClassName classDialogInterface = ClassName.get("android.content", "DialogInterface");
	private static final ClassName classDialogInterfaceOnClickListener = ClassName.get("android.content.DialogInterface", "OnClickListener");
	private static final ClassName classDialogInterfaceOnShowListener = ClassName.get("android.content.DialogInterface", "OnShowListener");

	private static final ClassName classIntent = ClassName.get("android.content", "Intent");
	private static final ClassName classColor = ClassName.get("android.graphics", "Color");

	private static final ClassName classBundle = ClassName.get("android.os", "Bundle");
	private static final ClassName classHandler = ClassName.get("android.os", "Handler");
	private static final ClassName classLooper = ClassName.get("android.os", "Looper");

	private static ClassName classAlertDialog = ClassName.get("android.support.v7.app", "AlertDialog");
	private static ClassName classAppCompatActivity = ClassName.get("android.support.v7.app", "AppCompatActivity");
	private static ClassName classCheckBox = ClassName.get("android.support.v7.widget", "AppCompatCheckBox");
	private static ClassName classEditText = ClassName.get("android.support.v7.widget", "AppCompatEditText");
	private static ClassName classTextView = ClassName.get("android.support.v7.widget", "AppCompatTextView");
	private static ClassName classLinearLayoutCompat = ClassName.get("android.support.v7.widget", "LinearLayoutCompat");

	private static final ClassName classEditable = ClassName.get("android.text", "Editable");
	private static final ClassName classInputType = ClassName.get("android.text", "InputType");
	private static final ClassName classTextWatcher = ClassName.get("android.text", "TextWatcher");

	private static final ClassName classTypedValue = ClassName.get("android.util", "TypedValue");

	private static final ClassName classMenu = ClassName.get("android.view", "Menu");
	private static final ClassName classMenuItem = ClassName.get("android.view", "MenuItem");
	private static final ClassName classMotionEvent = ClassName.get("android.view", "MotionEvent");
	private static final ClassName classView = ClassName.get("android.view", "View");
	private static final ClassName classViewOnClickListener = ClassName.get("android.view.View", "OnClickListener");
	private static final ClassName classViewOnFocusChangeListener = ClassName.get("android.view.View", "OnFocusChangeListener");
	private static final ClassName classOnTouchListener = ClassName.get("android.view.View", "OnTouchListener");
	private static final ClassName classViewGroup = ClassName.get("android.view", "ViewGroup");
	private static final ClassName classInputMethodManager = ClassName.get("android.view.inputmethod", "InputMethodManager");

	private static final ClassName classButton = ClassName.get("android.widget", "Button");
	private static final ClassName classCompoundButton = ClassName.get("android.widget", "CompoundButton");
	private static final ClassName classOnCheckedChangeListener = ClassName.get("android.widget.CompoundButton", "OnCheckedChangeListener");
	private static final ClassName classScrollView = ClassName.get("android.widget", "ScrollView");


	public static void generate(ProfilerDescription profilerDescription) {
		try {

			if (profilerDescription.isUseAndroidX()){
			   classAlertDialog = ClassName.get("androidx.appcompat.app", "AlertDialog");
			   classAppCompatActivity = ClassName.get("androidx.appcompat.app", "AppCompatActivity");
			   classCheckBox = ClassName.get("androidx.appcompat.widget", "AppCompatCheckBox");
			   classEditText = ClassName.get("androidx.appcompat.widget", "AppCompatEditText");
			   classTextView = ClassName.get("androidx.appcompat.widget", "AppCompatTextView");
			   classLinearLayoutCompat = ClassName.get("androidx.appcompat.widget", "LinearLayoutCompat");
			}

			// Generate a class
			TypeSpec.Builder activityClass = TypeSpec
					.classBuilder("AppProfilerActivity")
					.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
					.superclass(classAppCompatActivity);



			classAppProfiler = ClassName.get(profilerDescription.getPackageName(), "AppProfiler");
			if (profilerDescription.isActive()) {
				TypeSpec listenerSpec = TypeSpec.anonymousClassBuilder("")
						.addSuperinterface(classViewOnFocusChangeListener)
						.addMethod(MethodSpec.methodBuilder("onFocusChange")
								.addAnnotation(Override.class)
								.addModifiers(Modifier.PUBLIC)
								.addParameter(classView, "v")
								.addParameter(boolean.class, "hasFocus")
								.beginControlFlow("if (hasFocus && !(v instanceof $T))", classEditText)
								.addStatement("hideKeyboard()")
								.endControlFlow()
								.build())
						.build();
				TypeSpec touchListenerSpec = TypeSpec.anonymousClassBuilder("")
						.addSuperinterface(classOnTouchListener)
						.addMethod(MethodSpec.methodBuilder("onTouch")
								.addAnnotation(Override.class)
								.returns(boolean.class)
								.addModifiers(Modifier.PUBLIC)
								.addParameter(classView, "v")
								.addParameter(classMotionEvent, "event")
								.addStatement("return true")
								.build())
						.build();

				TypeSpec autoCloseRunnableSpec = TypeSpec.anonymousClassBuilder("")
						.addSuperinterface(Runnable.class)
						.addField(FieldSpec.builder(int.class, "time", Modifier.PRIVATE).initializer("5").build())
						.addMethod(MethodSpec.methodBuilder("run")
								.addAnnotation(Override.class)
								.addModifiers(Modifier.PUBLIC)
								.beginControlFlow("if (time > 0)")
								.addStatement("time--")
								.beginControlFlow("if (dialogChangeButton != null)")
								.addStatement("dialogChangeButton.setText(\"Start (\" + time + \")\")")
								.endControlFlow()
								.addStatement("handler.postDelayed(this, 1000)")
								.endControlFlow()
								.beginControlFlow("else")
								.addStatement("dialogChangeButton.setEnabled(false)")
								.addStatement("closeProfiler(true)")
								.endControlFlow()
								.build())
						.build();

				activityClass.addField(FieldSpec.builder(int.class, "TEXT_LIGHT_COLOR", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).initializer("$T.parseColor($S)", classColor, "#DE000000").build());
				activityClass.addField(FieldSpec.builder(int.class, "SECONDARY_TEXT_LIGHT_COLOR", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).initializer("$T.parseColor($S)", classColor, "#8A000000").build());
				activityClass.addField(FieldSpec.builder(int.class, "SEPARATOR_COLOR", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).initializer("$T.parseColor($S)", classColor, "#1F000000").build());
				activityClass.addField(FieldSpec.builder(int.class, "SEPARATOR_DARK_COLOR", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).initializer("$T.parseColor($S)", classColor, "#8A000000").build());
				activityClass.addField(FieldSpec.builder(int.class, "WARNING_COLOR", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).initializer("$T.parseColor($S)", classColor, "#B71C1C").build());

				activityClass.addField(FieldSpec.builder(classLinearLayoutCompat, "linearLayout", Modifier.PRIVATE).build());
				activityClass.addField(FieldSpec.builder(classTextView, "disabledTextView", Modifier.PRIVATE).build());
				activityClass.addField(FieldSpec.builder(classTextView, "profileTextView", Modifier.PRIVATE).build());

				for (FieldDescription fieldDescription : profilerDescription.getFields()) {
					activityClass.addField(FieldSpec.builder(getFieldViewClass(fieldDescription), fieldDescription.getCamelCaseName() + "ValueView", Modifier.PRIVATE).build());
				}
				activityClass.addField(FieldSpec.builder(classButton, "dialogChangeButton", Modifier.PRIVATE).build());

				activityClass.addField(FieldSpec.builder(float.class, "dp", Modifier.PRIVATE).build());
				activityClass.addField(FieldSpec.builder(classHandler, "handler", Modifier.PRIVATE).initializer("new $T($T.myLooper())", classHandler, classLooper).build());
				activityClass.addField(FieldSpec.builder(Runnable.class, "autoCloseRunnable", Modifier.PRIVATE).initializer("$L", autoCloseRunnableSpec).build());
				activityClass.addField(FieldSpec.builder(classViewOnFocusChangeListener, "focusListener", Modifier.PRIVATE).initializer("$L", listenerSpec).build());
				activityClass.addField(FieldSpec.builder(classOnTouchListener, "touchListener", Modifier.PRIVATE).initializer("$L", touchListenerSpec).build());
				activityClass.addField(FieldSpec.builder(classAlertDialog, "dialog", Modifier.PRIVATE).build());
			}

			activityClass.addMethod(generateOnCreateMethod(profilerDescription));
			if (profilerDescription.isActive()) {
				activityClass.addMethod(generateOnCreateOptionsMenu());
				activityClass.addMethod(generateOnOptionsItemSelected());
				activityClass.addMethod(generateCloseProfilerMethod(profilerDescription.getActivityClass()));
				activityClass.addMethod(generateSetupProfileMethod(profilerDescription));
				activityClass.addMethod(generateAddFocusViewMethod());
				activityClass.addMethod(generateAddSeparatorMethod());
				activityClass.addMethod(generateAddDarkSeparatorMethod());
				activityClass.addMethod(generateAddProfilesOptionMethod(profilerDescription));
				for (FieldDescription fieldDescription : profilerDescription.getFields()) {
					if (isEditOption(fieldDescription)) {
						activityClass.addMethod(generateAddEditOptionMethod(fieldDescription));
					} else if (isCheckOption(fieldDescription)) {
						activityClass.addMethod(generateAddCheckOptionMethod(fieldDescription));
					} else {
						activityClass.addMethod(generateAddSelectOptionMethod(fieldDescription));
					}
				}
				activityClass.addMethod(generateAddTitleMethod());
				activityClass.addMethod(generateSetFocusableMethod());
				activityClass.addMethod(generateHideKeyboardMethod());
				activityClass.addMethod(generateArrayToStringMethod());

			}
			// Write generated class to a file
			JavaFile.builder(profilerDescription.getPackageName(), activityClass.build()).indent("\t").build().writeTo(filer);
		} catch (IOException ex) {
			messager.printMessage(Diagnostic.Kind.ERROR, ex.toString());
		}
	}

	private static ClassName getFieldViewClass(FieldDescription fieldDescription) {
		if (isEditOption(fieldDescription)) {
			return classEditText;
		} else if (isCheckOption(fieldDescription)) {
			return classCheckBox;
		} else {
			return classTextView;
		}
	}

	private static MethodSpec generateOnCreateMethod(ProfilerDescription profilerDescription) {
		MethodSpec.Builder spec = MethodSpec.methodBuilder("onCreate")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PROTECTED)
				.addParameter(classBundle, "savedInstanceState");
		spec.addStatement("super.onCreate(savedInstanceState)");
		spec.addCode("\n");
		spec.addStatement("setTitle($S)", "App Profiler");
		spec.addCode("\n");
		if (profilerDescription.isActive()) {
			spec.addStatement("dp = getResources().getDisplayMetrics().density");
			spec.addStatement("$1T layoutPadding = ($1T)(21f * dp)", int.class);
			spec.addCode("\n");
			spec.addStatement("$1T scrollView = new $1T(this)", classScrollView);
			spec.addStatement("linearLayout = new $1T(this)", classLinearLayoutCompat);
			spec.addStatement("linearLayout.setPadding(layoutPadding, layoutPadding, layoutPadding, layoutPadding)");
			spec.addStatement("linearLayout.setClipToPadding(false)");
			spec.addStatement("linearLayout.setOrientation($T.VERTICAL)", classLinearLayoutCompat);
			spec.addStatement("scrollView.addView(linearLayout)");
			spec.addStatement("scrollView.setClipToPadding(false)");
			spec.addCode("\n");
			spec.addStatement("addFocusView()");

			spec.addCode("\n");
			spec.addStatement("addOptionProfiles()");
			spec.addStatement("addSeparator()");

			spec.addCode("\n");
			spec.addStatement("disabledTextView = new $1T(this)", classTextView);
			spec.addStatement("disabledTextView.setText($S)", "Only \"Custom\" profile can be edited!");
			spec.addStatement("disabledTextView.setTextColor(WARNING_COLOR)");
			spec.addStatement("disabledTextView.setTextSize($T.COMPLEX_UNIT_SP, 12)", classTypedValue);
			spec.addStatement("setFocusable(disabledTextView)");
			spec.addStatement("linearLayout.addView(disabledTextView)");
			spec.addCode("\n");

			for (FieldDescription fieldDescription : profilerDescription.getFields()) {
				spec.addStatement("addOption$L()", fieldDescription.getCapitalizedCamelCaseName());
			}

			spec.addCode("\n");
			spec.addStatement("setContentView(scrollView)");
			spec.addCode("\n");
			spec.addStatement("setupProfile($T.getProfile())", classAppProfiler);
			spec.addCode("\n");

			List<String> profiles = new ArrayList<>();
			for (ProfileDescription profileDescription : profilerDescription.getProfiles()) {
				if (!"Custom".equals(profileDescription.getName())) {
					profiles.add(profileDescription.getName());
				}
			}
			profiles.add("Custom");
			spec.addStatement("final $1T[] values = new $1T[]{$2L}", String.class, "\"" + String.join("\", \"", profiles) + "\"");

			TypeSpec dialogYesListenerSpec = TypeSpec.anonymousClassBuilder("")
					.addSuperinterface(classDialogInterfaceOnClickListener)
					.addMethod(MethodSpec.methodBuilder("onClick")
							.addAnnotation(Override.class)
							.addModifiers(Modifier.PUBLIC)
							.addParameter(classDialogInterface, "dialog")
							.addParameter(int.class, "which")
							.addStatement("handler.removeCallbacks(autoCloseRunnable)")
							.addStatement("closeProfiler(true)")
							.build())
					.build();


			TypeSpec dialogEditListenerSpec = TypeSpec.anonymousClassBuilder("")
					.addSuperinterface(classDialogInterfaceOnClickListener)
					.addMethod(MethodSpec.methodBuilder("onClick")
							.addAnnotation(Override.class)
							.addModifiers(Modifier.PUBLIC)
							.addParameter(classDialogInterface, "dialog")
							.addParameter(int.class, "which")
							.addStatement("handler.removeCallbacks(autoCloseRunnable)")
							.build())
					.build();

			TypeSpec showListenerSpec = TypeSpec.anonymousClassBuilder("")
					.addSuperinterface(classDialogInterfaceOnShowListener)
					.addMethod(MethodSpec.methodBuilder("onShow")
							.addAnnotation(Override.class)
							.addModifiers(Modifier.PUBLIC)
							.addParameter(classDialogInterface, "dialog")
							.addStatement("dialogChangeButton = (($1T) dialog).getButton($1T.BUTTON_POSITIVE)", classAlertDialog)
							.addStatement("handler.postDelayed(autoCloseRunnable, 1000)")
							.build())
					.build();


			TypeSpec dialogListenerSpec = TypeSpec.anonymousClassBuilder("")
					.addSuperinterface(classDialogInterfaceOnClickListener)
					.addMethod(MethodSpec.methodBuilder("onClick")
							.addAnnotation(Override.class)
							.addModifiers(Modifier.PUBLIC)
							.addParameter(classDialogInterface, "dialog")
							.addParameter(int.class, "which")
							.addStatement("$T value = values[which]", String.class)
							.addStatement("$T.setProfile(value)", classAppProfiler)
							.addStatement("profileTextView.setText(value)")
							.addStatement("setupProfile(value)")
							.addStatement("handler.removeCallbacks(autoCloseRunnable)")
							.addStatement("dialog.dismiss()")
							.beginControlFlow("if(which < values.length - 1)")
							.addStatement("closeProfiler(false)")
							.endControlFlow()
							.build())
					.build();

			spec.addStatement("$1T.Builder builder = new $1T.Builder(this)", classAlertDialog)
					.addStatement("builder.setTitle($S)", "Profile")
					.addStatement("builder.setCancelable(false)")
					.addStatement("builder.setSingleChoiceItems(values, $T.asList(values).indexOf($T.getProfile()), $L)", Arrays.class, classAppProfiler, dialogListenerSpec)
					.addStatement("builder.setPositiveButton($S, $L)", "Start (5)", dialogYesListenerSpec)
					.addStatement("builder.setNeutralButton($S, $L)", "Edit", dialogEditListenerSpec)
					.addStatement("dialog = builder.create()")
					.addStatement("dialog.setOnShowListener($L)", showListenerSpec)
					.addStatement("dialog.show()");

		} else {
			spec.addStatement("$T intent = new $T()", classIntent, classIntent);
			spec.addStatement("intent.setClassName(this, $S)", profilerDescription.getActivityClass());
			spec.addStatement("startActivity(intent)");
			spec.addStatement("finish()");
		}
		return spec.build();
	}

	private static boolean isEditOption(FieldDescription fieldDescription) {
		String[] values = fieldDescription.getValues();
		return !ValueType.BOOLEAN.equals(fieldDescription.getValueType()) && (values == null || values.length == 0);
	}

	private static boolean isCheckOption(FieldDescription fieldDescription) {
		String[] values = fieldDescription.getValues();
		return ValueType.BOOLEAN.equals(fieldDescription.getValueType()) && (values == null || values.length == 0);
	}

	private static MethodSpec generateOnCreateOptionsMenu() {
		MethodSpec.Builder spec = MethodSpec.methodBuilder("onCreateOptionsMenu")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(boolean.class)
				.addParameter(classMenu, "menu");
		spec.addStatement("$T saveItem = menu.add($S)", classMenuItem, "Start");
		spec.addStatement("saveItem.setShowAsAction($T.SHOW_AS_ACTION_ALWAYS)", classMenuItem);
		spec.addStatement("return super.onCreateOptionsMenu(menu)");
		return spec.build();
	}

	private static MethodSpec generateOnOptionsItemSelected() {
		MethodSpec.Builder spec = MethodSpec.methodBuilder("onOptionsItemSelected")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(boolean.class)
				.addParameter(classMenuItem, "item");
		spec.addStatement("closeProfiler(false)");
		spec.addStatement("return true");
		return spec.build();
	}

	private static MethodSpec generateCloseProfilerMethod(String activityClass) {
		MethodSpec.Builder spec = MethodSpec.methodBuilder("closeProfiler")
				.addModifiers(Modifier.PRIVATE)
				.addParameter(boolean.class, "closeDialog");
		spec.addStatement("handler.removeCallbacks(autoCloseRunnable)");
		spec.addStatement("$T listener = $T.getOnProfileChangedListener()", OnProfileChangedListener.class, classAppProfiler);
		spec.beginControlFlow("if (listener != null)");
		spec.addStatement("listener.onProfileChanged(false)");
		spec.endControlFlow();
		spec.beginControlFlow("if (dialog != null && closeDialog)");
		spec.addStatement("dialog.dismiss()");
		spec.endControlFlow();
		spec.addStatement("$T intent = new $T()", classIntent, classIntent);
		spec.addStatement("intent.setClassName(this, $S)", activityClass);
		spec.addStatement("startActivity(intent)");
		spec.addStatement("finish()");
		return spec.build();
	}

	private static MethodSpec generateSetupProfileMethod(ProfilerDescription profilerDescription) {
		MethodSpec.Builder spec = MethodSpec.methodBuilder("setupProfile")
				.addParameter(String.class, "profile")
				.addModifiers(Modifier.PRIVATE, Modifier.FINAL);

		spec.addStatement("$T enabled = false", boolean.class);
		spec.beginControlFlow("switch (profile)");
		for (ProfileDescription profileDescription : profilerDescription.getProfiles()) {
			if ("Custom".equals(profileDescription.getName())) {
				continue;
			}
			spec.beginControlFlow("case $S:", profileDescription.getName());
			for (FieldDescription fieldDescription : profilerDescription.getFields()) {
				boolean check;
				if (isEditOption(fieldDescription)) {
					check = false;
				} else if (isCheckOption(fieldDescription)) {
					check = true;
				} else {
					check = false;
				}

				String value = Utils.getDefaultFieldValue(fieldDescription, profileDescription);
				if (check) {
					if (value == null) {
						spec.addStatement(fieldDescription.getCamelCaseName() + "ValueView.setChecked(false)");
					} else {
						spec.addStatement(fieldDescription.getCamelCaseName() + "ValueView.setChecked(Boolean.TRUE.equals(" + Utils.getTypeFormat(fieldDescription.getValueType()) + "))", value);
					}
				} else {
					if (value == null) {
						spec.addStatement(fieldDescription.getCamelCaseName() + "ValueView.setText(null)");
					} else {
						spec.addStatement(fieldDescription.getCamelCaseName() + "ValueView.setText($T.valueOf(" + Utils.getTypeFormat(fieldDescription.getValueType()) + "))", String.class, value);
					}
				}
			}

			spec.addStatement("enabled = false");
			spec.endControlFlow();
			spec.addStatement("break");
		}
		spec.beginControlFlow("case \"Custom\":");

		spec.addStatement("Object value");
		for (FieldDescription fieldDescription : profilerDescription.getFields()) {
			boolean check;
			if (isEditOption(fieldDescription)) {
				check = false;
			} else {
				check = isCheckOption(fieldDescription);
			}

			spec.addStatement("value = $T.get" + fieldDescription.getCapitalizedCamelCaseName() + "CustomValue()", classAppProfiler);
			if (check) {
				spec.addStatement(fieldDescription.getCamelCaseName() + "ValueView.setChecked(Boolean.TRUE.equals(value))");
			} else {
				spec.addStatement(fieldDescription.getCamelCaseName() + "ValueView.setText(value == null ? null : $T.valueOf(value))", String.class);
			}
		}
		spec.addStatement("enabled = true");
		spec.endControlFlow();
		spec.addStatement("break");
		spec.beginControlFlow("default:");
		spec.addStatement("enabled = true");
		spec.endControlFlow();
		spec.addStatement("break");
		spec.endControlFlow();
		spec.beginControlFlow("for (int i = linearLayout.indexOfChild(disabledTextView) + 1; i < linearLayout.getChildCount(); i++)");
		spec.addStatement("$T child = linearLayout.getChildAt(i)", classView);
		spec.addStatement("child.setOnTouchListener(enabled ? null : touchListener)");
		spec.addStatement("child.setAlpha(enabled ? 1f : 0.6f)");
		spec.endControlFlow();
		spec.addStatement("disabledTextView.setVisibility(!enabled ? $1T.VISIBLE : $1T.GONE)", classView);
		return spec.build();
	}

	private static MethodSpec generateAddFocusViewMethod() {
		MethodSpec.Builder spec = MethodSpec.methodBuilder("addFocusView")
				.addModifiers(Modifier.PRIVATE, Modifier.FINAL);

		spec.addStatement("$1T view = new $1T(this)", classView);
		spec.addStatement("setFocusable(view)");
		spec.addStatement("$1T.LayoutParams params = new $1T.LayoutParams($2T.LayoutParams.MATCH_PARENT, (int) (dp))", classLinearLayoutCompat, classViewGroup);
		spec.addStatement("view.setLayoutParams(params)");
		spec.addStatement("linearLayout.addView(view)");
		return spec.build();
	}

	private static MethodSpec generateSetFocusableMethod() {
		MethodSpec.Builder spec = MethodSpec.methodBuilder("setFocusable")
				.addParameter(classView, "view")
				.addModifiers(Modifier.PRIVATE, Modifier.FINAL);

		spec.addStatement("view.setFocusable(true)");
		spec.addStatement("view.setFocusableInTouchMode(true)");
		spec.addStatement("view.setClickable(true)");
		spec.addStatement("view.setOnFocusChangeListener(focusListener)");
		return spec.build();
	}

	private static MethodSpec generateAddSeparatorMethod() {
		MethodSpec.Builder spec = MethodSpec.methodBuilder("addSeparator")
				.addModifiers(Modifier.PRIVATE, Modifier.FINAL);

		spec.addStatement("$1T view = new $1T(this)", classView);
		spec.addStatement("setFocusable(view)");
		spec.addStatement("$1T.LayoutParams params = new $1T.LayoutParams($2T.LayoutParams.MATCH_PARENT, (int) (dp))", classLinearLayoutCompat, classViewGroup);
		spec.addStatement("params.topMargin = ($T) (8f*dp)", int.class);
		spec.addStatement("params.bottomMargin = ($T) (8f*dp)", int.class);
		spec.addStatement("view.setLayoutParams(params)");
		spec.addStatement("view.setBackgroundColor(SEPARATOR_COLOR)");
		spec.addStatement("linearLayout.addView(view)");
		return spec.build();
	}

	private static MethodSpec generateAddDarkSeparatorMethod() {
		MethodSpec.Builder spec = MethodSpec.methodBuilder("addDarkSeparator")
				.addModifiers(Modifier.PRIVATE, Modifier.FINAL);

		spec.addStatement("$1T view = new $1T(this)", classView);
		spec.addStatement("setFocusable(view)");
		spec.addStatement("$1T.LayoutParams params = new $1T.LayoutParams($2T.LayoutParams.MATCH_PARENT, (int) (dp))", classLinearLayoutCompat, classViewGroup);
		spec.addStatement("params.leftMargin = ($T) (4f*dp)", int.class);
		spec.addStatement("params.rightMargin = ($T) (4f*dp)", int.class);
		spec.addStatement("view.setLayoutParams(params)");
		spec.addStatement("view.setBackgroundColor(SEPARATOR_DARK_COLOR)");
		spec.addStatement("linearLayout.addView(view)");
		return spec.build();
	}

	private static MethodSpec generateHideKeyboardMethod() {
		MethodSpec.Builder spec = MethodSpec.methodBuilder("hideKeyboard")
				.addModifiers(Modifier.PRIVATE, Modifier.FINAL);

		spec.addStatement("$1T v = getCurrentFocus()", classView)
				.addStatement("$1T imm = ($1T) getSystemService($2T.INPUT_METHOD_SERVICE)", classInputMethodManager, classContext)
				.beginControlFlow("if (imm != null)")
				.addStatement("imm.hideSoftInputFromWindow(v.getWindowToken(), 0)")
				.endControlFlow()
				.beginControlFlow("if (v instanceof $T)", classEditText)
				.addStatement("v.clearFocus()")
				.endControlFlow();
		return spec.build();
	}

	private static MethodSpec generateAddTitleMethod() {
		MethodSpec.Builder spec = MethodSpec.methodBuilder("addFieldTitle")
				.addParameter(String.class, "title")
				.addParameter(boolean.class, "focusable")
				.returns(classTextView)
				.addModifiers(Modifier.PRIVATE, Modifier.FINAL);

		spec.addStatement("$1T titleTextView = new $1T(this)", classTextView);
		spec.addStatement("titleTextView.setText(title)");
		spec.addStatement("titleTextView.setTextColor(TEXT_LIGHT_COLOR)");
		spec.addStatement("titleTextView.setTextSize($T.COMPLEX_UNIT_SP, 16)", classTypedValue);
		spec.addStatement("titleTextView.setPadding(0, ($T)(12 * dp), 0, 0)", int.class);
		spec.addStatement("linearLayout.addView(titleTextView)");
		spec.beginControlFlow("if (focusable)");
		spec.addStatement("setFocusable(titleTextView)");
		spec.endControlFlow();
		spec.addStatement("return titleTextView");
		return spec.build();
	}

	private static MethodSpec generateArrayToStringMethod() {
		MethodSpec.Builder spec = MethodSpec.methodBuilder("arrayToString")
				.addParameter(ArrayTypeName.of(Object.class), "array")
				.returns(String.class)
				.addModifiers(Modifier.PRIVATE, Modifier.FINAL);

		spec.addStatement("$1T builder = new $1T()", StringBuilder.class);
		spec.beginControlFlow("for (int i = 0; i < array.length; i++)");
		spec.beginControlFlow("if (builder.length() > 0)");
		spec.addStatement("builder.append($S)", ", ");
		spec.endControlFlow();
		spec.addStatement("builder.append($T.valueOf(array[i]))", String.class);
		spec.endControlFlow();
		spec.addStatement("return builder.toString()", String.class);
		return spec.build();
	}

	private static MethodSpec generateAddProfilesOptionMethod(ProfilerDescription profilerDescription) {

		MethodSpec.Builder spec = MethodSpec.methodBuilder("addOptionProfiles")
				.addModifiers(Modifier.PRIVATE, Modifier.FINAL);

		spec.addStatement("profileTextView = new $1T(this)", classTextView);
		spec.addStatement("profileTextView.setTextColor(TEXT_LIGHT_COLOR)");
		spec.addStatement("profileTextView.setIncludeFontPadding(false)");
		spec.addStatement("profileTextView.setTextSize($T.COMPLEX_UNIT_SP, 22)", classTypedValue);
		spec.addStatement("profileTextView.setText($T.getProfile())", classAppProfiler);
		spec.addStatement("linearLayout.addView(profileTextView)");

		spec.addStatement("$1T changeTextView = new $1T(this)", classTextView);
		spec.addStatement("changeTextView.setTextColor(SECONDARY_TEXT_LIGHT_COLOR)");
		spec.addStatement("changeTextView.setIncludeFontPadding(false)");
		spec.addStatement("changeTextView.setTextSize($T.COMPLEX_UNIT_SP, 9)", classTypedValue);
		spec.addStatement("changeTextView.setText($S)", "(click to change)");
		spec.addStatement("linearLayout.addView(changeTextView)");

		List<String> profiles = new ArrayList<>();
		for (ProfileDescription profileDescription : profilerDescription.getProfiles()) {
			if (!"Custom".equals(profileDescription.getName())) {
				profiles.add(profileDescription.getName());
			}
		}
		profiles.add("Custom");

		spec.addStatement("final $1T[] values = new $1T[]{$2L}", String.class, "\"" + String.join("\", \"", profiles) + "\"");

		TypeSpec dialogListenerSpec = TypeSpec.anonymousClassBuilder("")
				.addSuperinterface(classDialogInterfaceOnClickListener)
				.addMethod(MethodSpec.methodBuilder("onClick")
						.addAnnotation(Override.class)
						.addModifiers(Modifier.PUBLIC)
						.addParameter(classDialogInterface, "dialog")
						.addParameter(int.class, "which")
						.addStatement("$T value = values[which]", String.class)
						.addStatement("$T.setProfile(value)", classAppProfiler)
						.addStatement("profileTextView.setText(value)")
						.addStatement("setupProfile(value)")
						.addStatement("dialog.dismiss()")
						.build())
				.build();

		TypeSpec listenerSpec = TypeSpec.anonymousClassBuilder("")
				.addSuperinterface(classViewOnClickListener)
				.addMethod(MethodSpec.methodBuilder("onClick")
						.addAnnotation(Override.class)
						.addModifiers(Modifier.PUBLIC)
						.addParameter(classView, "v")
						.addStatement("hideKeyboard()")
						.addStatement("$1T.Builder builder = new $1T.Builder(v.getContext())", classAlertDialog)
						.addStatement("builder.setTitle($S)", "Profile")
						.addStatement("builder.setSingleChoiceItems(values, $T.asList(values).indexOf($T.getProfile()), $L)", Arrays.class, classAppProfiler, dialogListenerSpec)
						.addStatement("builder.show()")
						.build())
				.build();
		spec.addStatement("$T listener = $L", classViewOnClickListener, listenerSpec);
		spec.addStatement("profileTextView.setOnClickListener(listener)");
		spec.addStatement("changeTextView.setOnClickListener(listener)");
		return spec.build();
	}


	private static MethodSpec generateAddSelectOptionMethod(FieldDescription fieldDescription) {
		String viewName = fieldDescription.getCamelCaseName() + "ValueView";

		MethodSpec.Builder spec = MethodSpec.methodBuilder("addOption" + fieldDescription.getCapitalizedCamelCaseName())
				.addModifiers(Modifier.PRIVATE, Modifier.FINAL);

		spec.addStatement("$T titleTextView = addFieldTitle($S, false)", classTextView, fieldDescription.getLabel());
		spec.addStatement(viewName + " = new $1T(this)", classTextView);
		spec.addStatement(viewName + ".setText($T.valueOf($T." + (ValueType.BOOLEAN.equals(fieldDescription.getValueType()) ? "is" : "get") + fieldDescription.getCapitalizedCamelCaseName() + "()))", String.class, classAppProfiler);
		spec.addStatement(viewName + ".setTextColor(SECONDARY_TEXT_LIGHT_COLOR)");
		spec.addStatement(viewName + ".setTextSize($T.COMPLEX_UNIT_SP, 18)", classTypedValue);
		spec.addStatement(viewName + ".setPadding(($T)(4*dp), 0, 0, 0)", int.class);
		spec.addStatement("linearLayout.addView(" + viewName + ")");
		spec.addStatement("addDarkSeparator()");
		spec.addStatement("final $1T[] values = new $1T[]{$2L}", String.class, "\"" + String.join("\", \"", fieldDescription.getValues()) + "\"");


		TypeSpec dialogListenerSpec = TypeSpec.anonymousClassBuilder("")
				.addSuperinterface(classDialogInterfaceOnClickListener)
				.addMethod(MethodSpec.methodBuilder("onClick")
						.addAnnotation(Override.class)
						.addModifiers(Modifier.PUBLIC)
						.addParameter(classDialogInterface, "dialog")
						.addParameter(int.class, "which")
						.addStatement("$T value = values[which]", String.class)
						.addStatement(viewName + ".setText(value)", Arrays.class)
						.addStatement("$T.set" + fieldDescription.getCapitalizedCamelCaseName() + "(($T)$T.$L.parseValue(value))", classAppProfiler, Utils.getClassFor(fieldDescription.getValueType()), ValueType.class, fieldDescription.getValueType().name())
						.addStatement("dialog.dismiss()")
						.build())
				.build();

		TypeSpec listenerSpec = TypeSpec.anonymousClassBuilder("")
				.addSuperinterface(classViewOnClickListener)
				.addMethod(MethodSpec.methodBuilder("onClick")
						.addAnnotation(Override.class)
						.addModifiers(Modifier.PUBLIC)
						.addParameter(classView, "v")
						.addStatement("hideKeyboard()")
						.addStatement("$1T.Builder builder = new $1T.Builder(v.getContext())", classAlertDialog)
						.addStatement("builder.setTitle($S)", fieldDescription.getLabel())
						.addStatement("builder.setSingleChoiceItems(values, $T.asList(values).indexOf($T.valueOf($T." + (ValueType.BOOLEAN.equals(fieldDescription.getValueType()) ? "is" : "get") + fieldDescription.getCapitalizedCamelCaseName() + "())), $L)", Arrays.class, String.class, classAppProfiler, dialogListenerSpec)
						.addStatement("builder.show()")
						.build())
				.build();
		spec.addStatement("$T listener = $L", classViewOnClickListener, listenerSpec);
		spec.addStatement("titleTextView.setOnClickListener(listener)");
		spec.addStatement(viewName + ".setOnClickListener(listener)");
		return spec.build();
	}

	private static MethodSpec generateAddEditOptionMethod(FieldDescription fieldDescription) {
		String viewName = fieldDescription.getCamelCaseName() + "ValueView";

		ValueType type = fieldDescription.getValueType();
		MethodSpec.Builder spec = MethodSpec.methodBuilder("addOption" + fieldDescription.getCapitalizedCamelCaseName())
				.addModifiers(Modifier.PRIVATE, Modifier.FINAL);

		spec.addStatement("addFieldTitle($S, true)", fieldDescription.getLabel());
		spec.addStatement(viewName + " = new $1T(this)", classEditText);
		spec.addStatement(viewName + ".setText($T.valueOf($T." + (ValueType.BOOLEAN.equals(fieldDescription.getValueType()) ? "is" : "get") + fieldDescription.getCapitalizedCamelCaseName() + "()))", String.class, classAppProfiler);
		spec.addStatement(viewName + ".setTextColor(SECONDARY_TEXT_LIGHT_COLOR)");
		spec.addStatement(viewName + ".setPadding(" + viewName + ".getPaddingLeft(), 0, " + viewName + ".getPaddingRight(), " + viewName + ".getPaddingBottom())");

		switch (type) {
			case INT:
			case LONG:
				spec.addStatement(viewName + ".setInputType($1T.TYPE_CLASS_NUMBER)", classInputType);
				break;
			case FLOAT:
				spec.addStatement(viewName + ".setInputType($1T.TYPE_CLASS_NUMBER | $1T.TYPE_NUMBER_FLAG_DECIMAL)", classInputType);
				break;
			default:
				spec.addStatement(viewName + ".setInputType($1T.TYPE_CLASS_TEXT)", classInputType);
		}

		TypeSpec listenerSpec = TypeSpec.anonymousClassBuilder("")
				.addSuperinterface(classTextWatcher)
				.addMethod(MethodSpec.methodBuilder("beforeTextChanged")
						.addAnnotation(Override.class)
						.addModifiers(Modifier.PUBLIC)
						.addParameter(CharSequence.class, "s")
						.addParameter(int.class, "start")
						.addParameter(int.class, "count")
						.addParameter(int.class, "after")
						.build())
				.addMethod(MethodSpec.methodBuilder("onTextChanged")
						.addAnnotation(Override.class)
						.addModifiers(Modifier.PUBLIC)
						.addParameter(CharSequence.class, "s")
						.addParameter(int.class, "start")
						.addParameter(int.class, "before")
						.addParameter(int.class, "after")
						.build())
				.addMethod(MethodSpec.methodBuilder("afterTextChanged")
						.addAnnotation(Override.class)
						.addModifiers(Modifier.PUBLIC)
						.addParameter(classEditable, "s")
						.addStatement("$T.set" + fieldDescription.getCapitalizedCamelCaseName() + "(s.length() > 0 ? ($T)$T.$L.parseValue(s.toString()) : null)", classAppProfiler, Utils.getClassFor(fieldDescription.getValueType()), ValueType.class, fieldDescription.getValueType().name())
						.build())
				.build();
		spec.addStatement(viewName + ".addTextChangedListener($L)", listenerSpec);
		spec.addStatement("linearLayout.addView(" + viewName + ")");

		return spec.build();
	}

	private static MethodSpec generateAddCheckOptionMethod(FieldDescription fieldDescription) {
		String viewName = fieldDescription.getCamelCaseName() + "ValueView";

		MethodSpec.Builder spec = MethodSpec.methodBuilder("addOption" + fieldDescription.getCapitalizedCamelCaseName())
				.addModifiers(Modifier.PRIVATE);

		spec.addStatement(viewName + " = new $1T(this)", classCheckBox);
		spec.addStatement(viewName + ".setTextColor(TEXT_LIGHT_COLOR)");
		spec.addStatement(viewName + ".setTextSize($T.COMPLEX_UNIT_SP, 16)", classTypedValue);
		spec.addStatement(viewName + ".setText($S)", fieldDescription.getLabel());
		spec.addStatement("$1T.LayoutParams params = new $1T.LayoutParams($2T.LayoutParams.MATCH_PARENT, $2T.LayoutParams.WRAP_CONTENT)", classLinearLayoutCompat, classViewGroup);
		spec.addStatement("params.topMargin = ($T) (12f*dp)", int.class);
		spec.addStatement(viewName + ".setLayoutParams(params)");

		spec.addStatement(viewName + ".setChecked(Boolean.TRUE.equals($T.is" + fieldDescription.getCapitalizedCamelCaseName() + "()))", classAppProfiler);

		TypeSpec listenerSpec = TypeSpec.anonymousClassBuilder("")
				.addSuperinterface(classOnCheckedChangeListener)
				.addMethod(MethodSpec.methodBuilder("onCheckedChanged")
						.addAnnotation(Override.class)
						.addModifiers(Modifier.PUBLIC)
						.addParameter(classCompoundButton, "buttonView")
						.addParameter(boolean.class, "isChecked")
						.addStatement("$T.set" + fieldDescription.getCapitalizedCamelCaseName() + "(isChecked)", classAppProfiler)
						.addStatement("hideKeyboard()")
						.build())
				.build();
		spec.addStatement(viewName + ".setOnCheckedChangeListener($L)", listenerSpec);
		spec.addStatement("linearLayout.addView(" + viewName + ")");

		return spec.build();
	}
}

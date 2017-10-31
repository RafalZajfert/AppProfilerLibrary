package software.rsquared.appprofiler.sample;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Arrays;
public class MainActivity extends AppCompatActivity {

	private float dp;
	private float dp21;
	private float dp8;
	private int textLight = Color.parseColor("#DE000000");
	private int secondaryTextLight = Color.parseColor("#8A000000");
	private int separator = Color.parseColor("#1F000000");
	private ScrollView scrollView;
	private LinearLayoutCompat linearLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("App Profiler");
		dp = getResources().getDisplayMetrics().density;
		dp21 = 21 * dp;
		dp8 = 8 * dp;


		scrollView = new ScrollView(this);
		scrollView.setClipToPadding(false);
		linearLayout = new LinearLayoutCompat(this);
		linearLayout.setPadding((int) dp21, (int) dp21, (int) dp21, (int) dp21);
		linearLayout.setClipToPadding(false);
		linearLayout.setOrientation(LinearLayoutCompat.VERTICAL);
		scrollView.addView(linearLayout);

		addFocusView();

		addOption("field1", "label1", true, "value1", new String[]{"1", "2", "3"}, true);
		addSeparator();

		addOption("field2", "label2", System.currentTimeMillis() % 2 == 0, "value2", new String[]{"1", "2", "3"}, false);
		addSeparator();

		addOption("field3", "label3", System.currentTimeMillis() % 2 == 0, "value3", new String[]{"1", "2", "3"}, false);
		addSeparator();

		addOption("field4", "label4", System.currentTimeMillis() % 2 == 0, "value4", new String[]{"1", "2", "3"}, false);
		addSeparator();

		addOption("field5", "label5", System.currentTimeMillis() % 2 == 0, "value5", new String[]{"1", "2", "3"}, false);
		addSeparator();

		addOption("field6", "label6", System.currentTimeMillis() % 2 == 0, "value6", new String[]{"1", "2", "3"}, false);
		addSeparator();

		addOption("field7", "label7", System.currentTimeMillis() % 2 == 0, "value7", new String[]{"1", "2", "3"}, false);
		addSeparator();

		addOption("field8", "label8", System.currentTimeMillis() % 2 == 0, "value8", new String[]{"1", "2", "3"}, false);
		addSeparator();

		addOption("field9", "label9", System.currentTimeMillis() % 2 == 0, "value9", new String[]{"1", "2", "3"}, false);
		addSeparator();

		addOption("field10", "label10", System.currentTimeMillis() % 2 == 0, "value10", new String[]{"1", "2", "3"}, false);
		addSeparator();

		addOption("field11", "label11", System.currentTimeMillis() % 2 == 0, "value11", new String[]{"1", "2", "3"}, false);
		addSeparator();

		addOption("field12", "label12", System.currentTimeMillis() % 2 == 0, "value12", new String[]{"1", "2", "3"}, false);
		addSeparator();

		setContentView(scrollView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem saveItem = menu.add("Save");
		saveItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		startActivity(new Intent(this, MainActivity.class));
		finish();
		return true;
	}

	private void addFocusView() {
		View focusView = new View(this);
		setFocusable(focusView);
		LinearLayoutCompat.LayoutParams params = new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (1 * dp));
		focusView.setLayoutParams(params);
		linearLayout.addView(focusView);
	}

	private void addOption(String name, String label, boolean select, String value, String[] options, boolean multiselect) {
		TextView titleTextView = new TextView(this);
		titleTextView.setText(label);
		titleTextView.setTextColor(textLight);
		titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		linearLayout.addView(titleTextView);
		setFocusable(titleTextView);

		if (select) {
			TextView valueTextView = new TextView(this);
			valueTextView.setText(value);
			valueTextView.setTextColor(secondaryTextLight);
			valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
			valueTextView.setTag(name);
			linearLayout.addView(valueTextView);
			setFocusable(valueTextView);
			View.OnClickListener listener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext())
							.setTitle(label);
					if (multiselect) {
						boolean[] checkedItems = new boolean[options.length];
						Arrays.fill(checkedItems, false);
						builder.setMultiChoiceItems(options, checkedItems, null);
					} else {
						builder.setSingleChoiceItems(options, -1, null);
					}
					builder
							.setPositiveButton("Set", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
//									valueTextView.setText(values.toString());
								}
							})
							.setNeutralButton("Cancel", null).show();

				}
			};
			titleTextView.setOnClickListener(listener);
			valueTextView.setOnClickListener(listener);
		} else {
			EditText valueEditText = new EditText(this);
			valueEditText.setText(value);
			valueEditText.setTextColor(secondaryTextLight);
			valueEditText.setTag(name);
			valueEditText.setInputType(InputType.TYPE_CLASS_TEXT);
			linearLayout.addView(valueEditText);
		}
	}

	private void setFocusable(View view) {
		view.setFocusable(true);
		view.setFocusableInTouchMode(true);
		view.setClickable(true);
		view.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus && !(v instanceof EditText)) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				}
			}
		});
	}

	private void addSeparator() {
		View separatorView = new View(this);
		LinearLayoutCompat.LayoutParams params = new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (1 * dp));
		params.topMargin = (int) dp8;
		params.bottomMargin = (int) dp8;
		separatorView.setLayoutParams(params);
		separatorView.setBackgroundColor(separator);
		linearLayout.addView(separatorView);
	}
}

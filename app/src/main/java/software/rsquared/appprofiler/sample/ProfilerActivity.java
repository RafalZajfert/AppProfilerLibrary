package software.rsquared.appprofiler.sample;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class ProfilerActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences preferences = getSharedPreferences("asd", MODE_PRIVATE);
		String[] a = new String[]{"a", "b"};
		preferences.edit().putString("asd", "asd").apply();
//		preferences.getInt("asd",true);
//		preferences.getLong("asd",true);
//		preferences.getString("asd",true);
//		preferences.getBoolean("asd",true);
//		preferences.getFloat("asd",true);
		TextView textView = new TextView(this);
		AppCompatSpinner spinner = new AppCompatSpinner(this);
		spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"a","b","c"}));
		setContentView(spinner);

//		recyclerView = new RecyclerView(this);
//		recyclerView.setPadding(padding, padding, padding, padding);
//		recyclerView.setClipToPadding(false);
//		setContentView(recyclerView);
	}

}

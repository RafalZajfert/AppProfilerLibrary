package software.rsquared.appprofiler.sample;

import android.app.Application;

import software.rsquared.appprofiler.OnProfileChangedListener;
import software.rsquared.appprofiler.Profile;
import software.rsquared.appprofiler.Profiler;
import software.rsquared.appprofiler.ValueType;


/**
 * @author Rafał Zajfert
 */

@Profiler(
		startActivity = "software.rsquared.appprofiler.sample.ProfilerActivity",
		packageName = BuildConfig.APPLICATION_ID,
		useAndroidX = false,
		active = true,
		fields = {
				@Profile.Field(name = "FIELD_1", label = "Field 1", valueType = ValueType.INT, values = {"1", "2", "3", "4"}, defaultValue = "2"),
				@Profile.Field(name = "FIELD_2", label = "Field 2", valueType = ValueType.STRING, values = {"1", "2", "3", "4"}),
				@Profile.Field(name = "FIELD_3", label = "Field 3", valueType = ValueType.INT),
				@Profile.Field(name = "FIELD_4", label = "Field 4", valueType = ValueType.FLOAT),
				@Profile.Field(name = "FIELD_B4", label = "Field B4", valueType = ValueType.STRING),
				@Profile.Field(name = "FIELD_5", label = "Field 5", valueType = ValueType.BOOLEAN),
				@Profile.Field(name = "FIELD_6", label = "Field 6", valueType = ValueType.BOOLEAN, defaultValue = "false"),
				@Profile.Field(name = "FIELD_A1", label = "Field _1", valueType = ValueType.INT, defaultValue = "1"),
				@Profile.Field(name = "FIELD_A2", label = "Field _2", valueType = ValueType.LONG, defaultValue = "2"),
				@Profile.Field(name = "FIELD_A3", label = "Field _3", valueType = ValueType.FLOAT, defaultValue = "1.2"),
				@Profile.Field(name = "FIELD_A4", label = "Field _4", valueType = ValueType.FLOAT, defaultValue = "2.3"),
				@Profile.Field(name = "FIELD_A5", label = "Field _5", valueType = ValueType.BOOLEAN, defaultValue = "false"),
				@Profile.Field(name = "FIELD_A6", label = "Field _6", valueType = ValueType.STRING, defaultValue = "test")
		},
		defaultProfiles = {
				@Profile(name = "Custom", values = {
						@Profile.Value(name = "FIELD_2", value = "114"),
				}),
				@Profile(name = "Alpha", values = {
						@Profile.Value(name = "FIELD_1"),
						@Profile.Value(name = "FIELD_2", value = "4"),
						@Profile.Value(name = "FIELD_3", value = "1"),
						@Profile.Value(name = "FIELD_4", value = "11.5"),
						@Profile.Value(name = "FIELD_5", value = "false")
				}),
				@Profile(name = "Beta", defaultProfile = true, values = {
						@Profile.Value(name = "FIELD_1"),
						@Profile.Value(name = "FIELD_2", value = "2"),
						@Profile.Value(name = "FIELD_3", value = "3"),
						@Profile.Value(name = "FIELD_4", value = "3.5"),
						@Profile.Value(name = "FIELD_5", value = "false")
				}),
				@Profile(name = "Production", values = {
						@Profile.Value(name = "FIELD_1", value = "1"),
						@Profile.Value(name = "FIELD_2", value = "1"),
						@Profile.Value(name = "FIELD_3", value = "2"),
						@Profile.Value(name = "FIELD_4", value = "1.5"),
						@Profile.Value(name = "FIELD_5", value = "true")
				})
		})
public class CustomApplication extends Application implements OnProfileChangedListener {

	@Override
	public void onCreate() {
		super.onCreate();
		AppProfiler.init(this, this);
	}

	@Override
	public void onProfileChanged(boolean fromInit) {

	}
}

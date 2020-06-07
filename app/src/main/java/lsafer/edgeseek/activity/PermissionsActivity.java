/*
 *	Copyright 2020 LSafer
 *
 *	Licensed under the Apache License, Version 2.0 (the "License");
 *	you may not use this file except in compliance with the License.
 *	You may obtain a copy of the License at
 *
 *	    http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS,
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *	See the License for the specific language governing permissions and
 *	limitations under the License.
 */
package lsafer.edgeseek.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceDataStore;

import java.util.Objects;

import cufyx.perference.SimplePreferenceFragment;
import lsafer.edgeseek.App;
import lsafer.edgeseek.R;
import lsafer.edgeseek.util.Util;

/**
 * Activity to manage permissions.
 *
 * @author lsafer
 * @version 0.1.5
 * @since 02-Jun-20
 */
final public class PermissionsActivity extends AppCompatActivity implements SimplePreferenceFragment.OwnerActivity {
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		//initial
		super.onCreate(savedInstanceState);
		this.setTheme(Util.theme(App.data.theme));
		this.setContentView(R.layout.activity_fragment);

		//fragment instance
		this.getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.fragment, new SimplePreferenceFragment())
				.commit();
	}

	@Override
	public int getPreferenceResources(SimplePreferenceFragment fragment) {
		//fragment layout
		Objects.requireNonNull(fragment, "fragment");
		return R.xml.fragment_permissions;
	}

	@Override
	public PreferenceDataStore getPreferenceDataStore(SimplePreferenceFragment fragment) {
		//data store
		Objects.requireNonNull(fragment, "fragment");
		return new PreferenceDataStore() {
			/**
			 * The application context.
			 */
			private Context context = PermissionsActivity.this;

			@Override
			public void putBoolean(String key, boolean value) {
				switch (key) {
					case "SYSTEM_ALERT_WINDOW":
						Intent i0 = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
						i0.setData(Uri.parse("package:" + this.context.getPackageName()));
						i0.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						this.context.startActivity(i0);
						break;
					case "WRITE_SETTINGS":
						Intent i1 = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
						i1.setData(Uri.parse("package:" + this.context.getPackageName()));
						i1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						this.context.startActivity(i1);
						break;
					case "IGNORE_BATTERY_OPTIMIZATIONS":
						Intent i2 = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
						i2.setData(Uri.parse("package:" + this.context.getPackageName()));
						i2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						this.context.startActivity(i2);
						break;
				}
			}

			@Override
			public boolean getBoolean(String key, boolean defValue) {
				switch (key) {
					case "display_over_other_apps":
						return Settings.canDrawOverlays(this.context);
					case "write_system_settings":
						return Settings.System.canWrite(this.context);
					case "ignore_battery_optimization":
						return this.context.getSystemService(PowerManager.class).isIgnoringBatteryOptimizations(this.context.getPackageName());
					default:
						return false;
				}
			}
		};
	}
}

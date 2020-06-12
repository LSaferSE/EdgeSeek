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

import android.os.Bundle;
import android.view.WindowManager;
import androidx.annotation.Nullable;
import cufyx.perference.SimplePreferenceActivity;
import lsafer.edgeseek.App;
import lsafer.edgeseek.R;
import lsafer.edgeseek.util.Position;

/**
 * An activity that manages the data of a side.
 *
 * @author lsafer
 * @version 0.1.5
 * @since 08-Jun-20
 */
final public class SideActivity extends SimplePreferenceActivity {
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		int position = this.getIntent().getIntExtra("side", -1);

		super.onCreate(savedInstanceState);
		this.setTheme(App.data.getTheme());
		this.setPreferenceDataStore(App.data.sides.get(position).store);
		this.setPreferenceLayout(R.xml.preference_side);
		this.setContentView(R.layout.activity_preference);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

		//fragment setup
		this.findPreferenceByKey(R.id.fragment, "title")
				.setTitle(Position.side.getTitle(position));
	}
}

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
package lsafer.edgeseek;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import lsafer.edgeseek.data.EdgeData;
import lsafer.edgeseek.legacy.SingleToast;
import lsafer.edgeseek.util.Util;

import static android.view.WindowManager.LayoutParams;

/**
 * An instance that responsible for viewing and managing an edge.
 *
 * @author lsafer
 * @version 0.1.5
 * @since 27-May-20
 */
public class Edge {
	/**
	 * The data of this edge.
	 */
	final public EdgeData data;

	/**
	 * The context this edge is using.
	 */
	final private Context context;
	/**
	 * The window-manager for this edge to attach to.
	 */
	final private WindowManager manager;
	/**
	 * The layout params of this edge.
	 */
	final private WindowManager.LayoutParams params = new WindowManager.LayoutParams();
	/**
	 * The view to be displayed.
	 */
	final private View view;
	/**
	 * The view that shows the toast.
	 */
	final private TextView toast;
	/**
	 * True, if this edge have been built.
	 */
	private boolean built = false;
	/**
	 * If this edge is landscape or not.
	 */
	private boolean landscape;
	/**
	 * The current position of this edge.
	 */
	private int position;
	/**
	 * True, if this edge is currently showing on the screen.
	 */
	private boolean attached = false;

	/**
	 * Construct a new edge from the given data.
	 *
	 * @param context the context of the application
	 * @param manager for this edge to attach to
	 * @param data    to construct the edge from
	 */
	public Edge(Context context, WindowManager manager, EdgeData data) {
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(manager, "manager");
		Objects.requireNonNull(data, "data");
		this.context = context;
		this.manager = manager;
		this.data = data;

		//------- initial build

		this.toast = new TextView(context);

		this.view = new View(context) {
			@Override
			protected void onConfigurationChanged(Configuration newConfig) {
				super.onConfigurationChanged(newConfig);
				Edge.this.reattach();
			}
		};

		this.params.type = Build.VERSION.SDK_INT >= 26 ?
						   WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
						   WindowManager.LayoutParams.TYPE_PHONE;
		this.params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
							WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
							WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
							WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
	}

	/**
	 * Display this edge to the screen.
	 *
	 * @return this
	 * @throws IllegalStateException if this edge is showing already or if the edge not built yet
	 */
	public Edge attach() {
		if (this.attached)
			throw new IllegalStateException("edge already in the screen");
		if (!this.built)
			throw new IllegalStateException("edge has not been built");

		if (this.data.activated) {
			this.manager.addView(this.view, this.params);
			this.attached = true;
		}

		return this;
	}

	/**
	 * Build the view of this edge.
	 *
	 * @return this
	 */
	public Edge build() {
		//positioning
		int d = this.manager.getDefaultDisplay().getRotation();
		this.position = Util.position(this.data.position, false, this.manager.getDefaultDisplay().getRotation());
		this.landscape = this.position == 0 || this.position == 2;

		//listeners
		switch (this.data.seek) {
			case "brightness":
				this.view.setOnTouchListener(new BrightnessController());
				break;
			case "media":
			case "ringtone":
			case "alarm":
			case "system":
		}

		//dimensions
		this.params.gravity = Util.gravity(this.position);
		this.params.width = this.landscape ? LayoutParams.MATCH_PARENT : this.data.width;
		this.params.height = this.landscape ? this.data.width : LayoutParams.MATCH_PARENT;

		//appearance
		this.view.setBackgroundColor(this.data.color);
		this.view.setAlpha(Color.alpha(this.data.color));
		this.params.alpha = Color.alpha(this.data.color);

		this.built = true;
		return this;
	}

	/**
	 * Remove this edge from the screen.
	 *
	 * @return this
	 * @throws IllegalStateException if this edge is not showing
	 */
	public Edge detach() {
		if (!this.attached)
			throw new IllegalStateException("edge is not on the screen in the first place");

		this.manager.removeView(this.view);
		this.attached = false;
		return this;
	}

	/**
	 * Check if this edge has been built.
	 *
	 * @return true, if this edge has been built
	 */
	public boolean isBuilt() {
		return this.built;
	}

	/**
	 * Checks if this edge is currently shown in the display or not.
	 *
	 * @return the showing status of this edge
	 */
	public boolean isAttached() {
		return this.attached;
	}

	/**
	 * Update the view live. (call only when the edge is shown)
	 *
	 * @return this
	 * @throws IllegalStateException if the edge is currently not shown
	 */
	public Edge reattach() {
		if (!this.attached)
			throw new IllegalStateException("edge is not attached in the first place");

		this.manager.removeView(this.view);

		if (this.data.activated) {
			this.build();
			this.manager.addView(this.view, this.params);
			this.attached = true;
		} else {
			this.attached = false;
		}

		return this;
	}


	/**
	 *
	 */
	private class BrightnessController implements View.OnTouchListener {
		/**
		 * The previous axis.
		 */
		Float axis;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_DOWN:
					this.axis = null;
				case MotionEvent.ACTION_UP:
					Vibrator vibrator = Edge.this.context.getSystemService(Vibrator.class);
					vibrator.vibrate(1);
					break;
				default:
					float axis = Edge.this.landscape ? event.getX() : event.getY();
					axis *= Edge.this.landscape && Edge.this.position == 3 ? -1 : 1;

					if (this.axis == null)
						this.axis = axis;

					float change = (this.axis - axis) * ((float) Edge.this.data.sensitivity / 100);
					this.axis = axis;

					try {
						ContentResolver resolver = Edge.this.context.getContentResolver();

						float value = change + Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS);
						value = value > 255f ? 255f : value < 0 ? 0 : value;

						Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
						Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, (int) value);

						SingleToast.makeText(Edge.this.context, String.valueOf((int) value), Toast.LENGTH_SHORT).show();
					} catch (Settings.SettingNotFoundException e) {
						e.printStackTrace();
					}
			}
			return true;
		}
	}
}

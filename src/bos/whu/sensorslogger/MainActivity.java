package bos.whu.sensorslogger;

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import bos.whu.sensorslogger.support.IUpdateUI;
import bos.whu.sensorslogger.support.LogEntity;
import bos.whu.sensorslogger.support.LogService;
import bos.whu.sensorslogger.support.StringUtils;
import bos.whu.sensorslogger.support.views.DynamicLinePlot;

import com.androidplot.xy.XYPlot;

public class MainActivity extends Activity {
	private static final int PLOT_REFRESH_TIME = 100;
	private static final int PLOT_ACCEL_X_AXIS_KEY = 0;
	private static final int PLOT_ACCEL_Y_AXIS_KEY = 1;
	private static final int PLOT_ACCEL_Z_AXIS_KEY = 2;
	private String plotAccelXAxisTitle = "X";
	private String plotAccelYAxisTitle = "Y";
	private String plotAccelZAxisTitle = "Z";
	private LinearLayout scrollView;
	private RelativeLayout relativeLayoutAcce, relativeLayoutOri,relativeLayoutGyro,relativeLayoutMag;
	private LogService logService;
	private myServiceConnection mConnection;
	private Switch AcceSwitch, OriSwitch,MagSwitch,GyroSwitch;
	private TextView AcceTvX, AcceTvY, AcceTvZ;
	private TextView OriTvX, OriTvY, OriTvZ;
	private TextView GyroTvX, GyroTvY, GyroTvZ;
	private TextView MagTvX, MagTvY, MagTvZ;
	private DynamicLinePlot dynamicPlotAcce, dynamicPlotOri,dynamicPlotGyro,dynamicPlotMag;
	private LayoutInflater inflater;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	class myServiceConnection implements ServiceConnection {
		public void onServiceConnected(ComponentName name, IBinder service) {
			System.out.println("onServiceConnected");
			logService = (LogService) ((LogService.binder) service)
					.getService();
			logService.setUpdataUIListener(new upDataUI());
		}

		public void onServiceDisconnected(ComponentName arg0) {
			System.out.println("onServiceDisconnected");
			mConnection = null;
			logService = null;
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mConnection = new myServiceConnection();
		startService(new Intent(MainActivity.this, LogService.class));
		bindService(new Intent(MainActivity.this, LogService.class),
				mConnection, Context.BIND_AUTO_CREATE);
		scrollView = (LinearLayout) findViewById(R.id.activity_main_scrollview);
		inflater = LayoutInflater.from(this);
		initAcceView();
		initOriView();
		initGyroView();
		initMagView();
	}

	private void initAcceView() {
		relativeLayoutAcce = (RelativeLayout) inflater.inflate(
				R.layout.activity_main_item, null);
		AcceSwitch = (Switch) relativeLayoutAcce
				.findViewById(R.id.activity_main_item_switch);
		AcceSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					logService.startSensor(Sensor.TYPE_ACCELEROMETER);
					handler.sendEmptyMessage(Sensor.TYPE_ACCELEROMETER);
				} else {
					logService.stopSensor(Sensor.TYPE_ACCELEROMETER);
					handler.removeMessages(Sensor.TYPE_ACCELEROMETER);
				}
			}
		});
		AcceTvX = (TextView) relativeLayoutAcce
				.findViewById(R.id.activity_main_item_acc_x);
		AcceTvY = (TextView) relativeLayoutAcce
				.findViewById(R.id.activity_main_item_acc_y);
		AcceTvZ = (TextView) relativeLayoutAcce
				.findViewById(R.id.activity_main_item_acc_z);
		// 初始化Chart
		XYPlot plot = (XYPlot) relativeLayoutAcce
				.findViewById(R.id.activity_main_item_plot_sensor);
		((TextView)relativeLayoutAcce
		.findViewById(R.id.activity_main_item_title)).setText("加速器");
		plot.setTitle("");
		dynamicPlotAcce = new DynamicLinePlot(plot);
		dynamicPlotAcce.setMaxRange(30);
		dynamicPlotAcce.setMinRange(-30);
		addAccelerationPlot();
		// 添加到主Activity
		scrollView.addView(relativeLayoutAcce);
	}

	private void addAccelerationPlot() {
		addPlotAcce(plotAccelXAxisTitle, PLOT_ACCEL_X_AXIS_KEY, Color.BLUE);
		addPlotAcce(plotAccelYAxisTitle, PLOT_ACCEL_Y_AXIS_KEY, Color.GREEN);
		addPlotAcce(plotAccelZAxisTitle, PLOT_ACCEL_Z_AXIS_KEY, Color.RED);
	}
	private void addPlotAcce(String title, int key, int color) {
		dynamicPlotAcce.addSeriesPlot(title, key, color);
	}
	private void initOriView() {
		relativeLayoutOri = (RelativeLayout) inflater.inflate(
				R.layout.activity_main_item, null);
		OriSwitch = (Switch) relativeLayoutOri
				.findViewById(R.id.activity_main_item_switch);
		OriSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					logService.startSensor(Sensor.TYPE_ORIENTATION);
					handler.sendEmptyMessage(Sensor.TYPE_ORIENTATION);
				} else {
					logService.stopSensor(Sensor.TYPE_ORIENTATION);
					handler.removeMessages(Sensor.TYPE_ORIENTATION);
				}
			}
		});
		OriTvX = (TextView) relativeLayoutOri
				.findViewById(R.id.activity_main_item_acc_x);
		OriTvY = (TextView) relativeLayoutOri
				.findViewById(R.id.activity_main_item_acc_y);
		OriTvZ = (TextView) relativeLayoutOri
				.findViewById(R.id.activity_main_item_acc_z);
		// 初始化Chart
		XYPlot plot = (XYPlot) relativeLayoutOri
				.findViewById(R.id.activity_main_item_plot_sensor);
		((TextView)relativeLayoutOri
		.findViewById(R.id.activity_main_item_title)).setText("方向传感器");
		plot.setTitle("");
		dynamicPlotOri = new DynamicLinePlot(plot);
		dynamicPlotOri.setMaxRange(360);
		dynamicPlotOri.setMinRange(-180);
		addOrilerationPlot();
		// 添加到主Activity
		scrollView.addView(relativeLayoutOri);
	}

	private void addOrilerationPlot() {
		addPlotOri(plotAccelXAxisTitle, PLOT_ACCEL_X_AXIS_KEY, Color.BLUE);
		addPlotOri(plotAccelYAxisTitle, PLOT_ACCEL_Y_AXIS_KEY, Color.GREEN);
		addPlotOri(plotAccelZAxisTitle, PLOT_ACCEL_Z_AXIS_KEY, Color.RED);
	}

	private void addPlotOri(String title, int key, int color) {
		dynamicPlotOri.addSeriesPlot(title, key, color);
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mConnection != null)
			unbindService(mConnection);
		if (logService != null) {
			stopService(new Intent(this, LogService.class));
		}
	}

	private void initGyroView() {
		relativeLayoutGyro = (RelativeLayout) inflater.inflate(
				R.layout.activity_main_item, null);
		GyroSwitch = (Switch) relativeLayoutGyro
				.findViewById(R.id.activity_main_item_switch);
		GyroSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					logService.startSensor(Sensor.TYPE_GYROSCOPE);
					handler.sendEmptyMessage(Sensor.TYPE_GYROSCOPE);
				} else {
					logService.stopSensor(Sensor.TYPE_GYROSCOPE);
					handler.removeMessages(Sensor.TYPE_GYROSCOPE);
				}
			}
		});
		GyroTvX = (TextView) relativeLayoutGyro
				.findViewById(R.id.activity_main_item_acc_x);
		GyroTvY = (TextView) relativeLayoutGyro
				.findViewById(R.id.activity_main_item_acc_y);
		GyroTvZ = (TextView) relativeLayoutGyro
				.findViewById(R.id.activity_main_item_acc_z);
		// 初始化Chart
		XYPlot plot = (XYPlot) relativeLayoutGyro
				.findViewById(R.id.activity_main_item_plot_sensor);
		((TextView)relativeLayoutGyro
		.findViewById(R.id.activity_main_item_title)).setText("陀螺仪");
		plot.setTitle("");
		dynamicPlotGyro = new DynamicLinePlot(plot);
		dynamicPlotGyro.setMaxRange(50);
		dynamicPlotGyro.setMinRange(-50);
		addGyrolerationPlot();
		// 添加到主Activity
		scrollView.addView(relativeLayoutGyro);
	}

	private void addGyrolerationPlot() {
		addPlotGyro(plotAccelXAxisTitle, PLOT_ACCEL_X_AXIS_KEY, Color.BLUE);
		addPlotGyro(plotAccelYAxisTitle, PLOT_ACCEL_Y_AXIS_KEY, Color.GREEN);
		addPlotGyro(plotAccelZAxisTitle, PLOT_ACCEL_Z_AXIS_KEY, Color.RED);
	}
	private void addPlotGyro(String title, int key, int color) {
		dynamicPlotGyro.addSeriesPlot(title, key, color);
	}
	
	
	private void initMagView() {
		relativeLayoutMag = (RelativeLayout) inflater.inflate(
				R.layout.activity_main_item, null);
		MagSwitch = (Switch) relativeLayoutMag
				.findViewById(R.id.activity_main_item_switch);
		MagSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					logService.startSensor(Sensor.TYPE_MAGNETIC_FIELD);
					handler.sendEmptyMessage(Sensor.TYPE_MAGNETIC_FIELD);
				} else {
					logService.stopSensor(Sensor.TYPE_MAGNETIC_FIELD);
					handler.removeMessages(Sensor.TYPE_MAGNETIC_FIELD);
				}
			}
		});
		MagTvX = (TextView) relativeLayoutMag
				.findViewById(R.id.activity_main_item_acc_x);
		MagTvY = (TextView) relativeLayoutMag
				.findViewById(R.id.activity_main_item_acc_y);
		MagTvZ = (TextView) relativeLayoutMag
				.findViewById(R.id.activity_main_item_acc_z);
		// 初始化Chart
		XYPlot plot = (XYPlot) relativeLayoutMag
				.findViewById(R.id.activity_main_item_plot_sensor);
		((TextView)relativeLayoutMag
		.findViewById(R.id.activity_main_item_title)).setText("磁场传感器");
		plot.setTitle("");
		dynamicPlotMag = new DynamicLinePlot(plot);
		dynamicPlotMag.setMaxRange(40);
		dynamicPlotMag.setMinRange(-40);
		addMaglerationPlot();
		// 添加到主Activity
		scrollView.addView(relativeLayoutMag);
	}

	private void addMaglerationPlot() {
		addPlotMag(plotAccelXAxisTitle, PLOT_ACCEL_X_AXIS_KEY, Color.BLUE);
		addPlotMag(plotAccelYAxisTitle, PLOT_ACCEL_Y_AXIS_KEY, Color.GREEN);
		addPlotMag(plotAccelZAxisTitle, PLOT_ACCEL_Z_AXIS_KEY, Color.RED);
	}
	private void addPlotMag(String title, int key, int color) {
		dynamicPlotMag.addSeriesPlot(title, key, color);
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	class upDataUI implements IUpdateUI {

		@Override
		public void Update(LogEntity logEntity) {
			// TODO Auto-generated method stub
			DecimalFormat df = new DecimalFormat("0.0000000");
			switch (logEntity.getType()) {
			case Sensor.TYPE_ACCELEROMETER:
				AcceTvX.setText(String.valueOf(df.format(logEntity.getData()
						.get(0))));
				AcceTvY.setText(String.valueOf(df.format(logEntity.getData()
						.get(1))));
				AcceTvZ.setText(String.valueOf(df.format(logEntity.getData()
						.get(2))));
				break;
			case Sensor.TYPE_ORIENTATION:
				OriTvX.setText(String.valueOf(df.format(logEntity.getData()
						.get(0))));
				OriTvY.setText(String.valueOf(df.format(logEntity.getData()
						.get(1))));
				OriTvZ.setText(String.valueOf(df.format(logEntity.getData()
						.get(2))));
				break;
			case Sensor.TYPE_GYROSCOPE:
				GyroTvX.setText(String.valueOf(df.format(logEntity.getData()
						.get(0))));
				GyroTvY.setText(String.valueOf(df.format(logEntity.getData()
						.get(1))));
				GyroTvZ.setText(String.valueOf(df.format(logEntity.getData()
						.get(2))));
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				MagTvX.setText(String.valueOf(df.format(logEntity.getData()
						.get(0))));
				MagTvY.setText(String.valueOf(df.format(logEntity.getData()
						.get(1))));
				MagTvZ.setText(String.valueOf(df.format(logEntity.getData()
						.get(2))));
				break;
			default:
				break;
			}
		}

	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case Sensor.TYPE_ACCELEROMETER:
				if (!StringUtils.isEmpty(AcceTvX.getText().toString())) {
					dynamicPlotAcce.setData(
							Float.valueOf(AcceTvX.getText().toString()),
							PLOT_ACCEL_X_AXIS_KEY);
					dynamicPlotAcce.setData(
							Float.valueOf(AcceTvY.getText().toString()),
							PLOT_ACCEL_Y_AXIS_KEY);
					dynamicPlotAcce.setData(
							Float.valueOf(AcceTvZ.getText().toString()),
							PLOT_ACCEL_Z_AXIS_KEY);
					dynamicPlotAcce.draw();
				}
				handler.sendEmptyMessageDelayed(Sensor.TYPE_ACCELEROMETER,
						PLOT_REFRESH_TIME);
				break;
			case Sensor.TYPE_ORIENTATION:
				if (!StringUtils.isEmpty(OriTvX.getText().toString())) {
					dynamicPlotOri.setData(
							Float.valueOf(OriTvX.getText().toString()),
							PLOT_ACCEL_X_AXIS_KEY);
					dynamicPlotOri.setData(
							Float.valueOf(OriTvY.getText().toString()),
							PLOT_ACCEL_Y_AXIS_KEY);
					dynamicPlotOri.setData(
							Float.valueOf(OriTvZ.getText().toString()),
							PLOT_ACCEL_Z_AXIS_KEY);
					dynamicPlotOri.draw();
				}
				handler.sendEmptyMessageDelayed(Sensor.TYPE_ORIENTATION,
						PLOT_REFRESH_TIME);
				break;
			case Sensor.TYPE_GYROSCOPE:
				if (!StringUtils.isEmpty(GyroTvX.getText().toString())) {
					dynamicPlotGyro.setData(
							Float.valueOf(GyroTvX.getText().toString()),
							PLOT_ACCEL_X_AXIS_KEY);
					dynamicPlotGyro.setData(
							Float.valueOf(GyroTvY.getText().toString()),
							PLOT_ACCEL_Y_AXIS_KEY);
					dynamicPlotGyro.setData(
							Float.valueOf(GyroTvZ.getText().toString()),
							PLOT_ACCEL_Z_AXIS_KEY);
					dynamicPlotGyro.draw();
				}
				handler.sendEmptyMessageDelayed(Sensor.TYPE_GYROSCOPE,
						PLOT_REFRESH_TIME);
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				if (!StringUtils.isEmpty(MagTvX.getText().toString())) {
					dynamicPlotMag.setData(
							Float.valueOf(MagTvX.getText().toString()),
							PLOT_ACCEL_X_AXIS_KEY);
					dynamicPlotMag.setData(
							Float.valueOf(MagTvY.getText().toString()),
							PLOT_ACCEL_Y_AXIS_KEY);
					dynamicPlotMag.setData(
							Float.valueOf(MagTvZ.getText().toString()),
							PLOT_ACCEL_Z_AXIS_KEY);
					dynamicPlotMag.draw();
				}
				handler.sendEmptyMessageDelayed(Sensor.TYPE_MAGNETIC_FIELD,
						PLOT_REFRESH_TIME);
				break;
			default:
				break;
			}
		}

	};
}

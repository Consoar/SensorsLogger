package bos.whu.sensorslogger.support;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;
import bos.whu.sensorslogger.BuildConfig;

public class LogService extends Service {
	private static final String TAG = "LogService";
	private WakeLock mWakeLock;
	private IBinder binder;
	private SensorManager mSensorManager;
	private float[] mDataAccelerometer = new float[3];
	private float[] mDataOrientation = new float[3];

	private SparseArray<Sensor> mSensors;
	private static final int[] SENSOR_TYPES = new int[] {
			Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE, Sensor.TYPE_ORIENTATION,
			Sensor.TYPE_MAGNETIC_FIELD};
	private SparseArray<String> SENSOR_TYPES_NAME;
	private SparseArray<BufferedWriter> mSensorFileWriters;
	private SparseArray<SensorEventListener> mSensorEventListeners;
	private BlockingQueue<LogEntity> mPushQueue = new LinkedBlockingQueue<LogEntity>();
	private ScheduledExecutorService saveDataService = Executors
			.newScheduledThreadPool(1);
	private LPFAndroidDeveloper lpfAndDev;
	private IUpdateUI updateUIListener;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	SimpleDateFormat sdfFileName = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	Runnable saveTask;

	public class binder extends Binder {
		public Service getService() {
			return LogService.this;
		}
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		binder = (IBinder) new binder();
		mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
				| PowerManager.ON_AFTER_RELEASE, "DataCollectionService");
		mWakeLock.acquire();
		init();
		initSensors();
		startCollect();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if (BuildConfig.DEBUG)
			Log.d(TAG, "onStartCommand");
		return START_REDELIVER_INTENT;
	}

	public IBinder onBind(Intent intent) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "onBind");
		return binder;
	}

	public void onRebind(Intent intent) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "onRebind");
		return;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "onUnbind");
		super.onUnbind(intent);
		return true;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		if (BuildConfig.DEBUG)
			Log.d(TAG, "onDestroy");
		super.onDestroy();
		mWakeLock.release();
		stopCollect();
	}

	private void init() {
		// TODO Auto-generated method stub
		lpfAndDev = new LPFAndroidDeveloper();
		SENSOR_TYPES_NAME = new SparseArray<String>();
		mSensorFileWriters = new SparseArray<BufferedWriter>();
		mSensorEventListeners = new SparseArray<SensorEventListener>();
		initSensorTypesName();
		saveTask = new Runnable() {
			BufferedWriter fileWriter;

			public void run() {
				try {
					try {
						DecimalFormat df = new DecimalFormat("0.0000000");
						while (!mPushQueue.isEmpty()) {
							LogEntity entity = mPushQueue.take();

							StringBuilder sb = new StringBuilder();
							fileWriter = getFileWriter(entity.type);

							sb.append(sdf.format(new Date(entity.timestamp)));
							sb.append(System.getProperty("line.separator"));
							for (Float value : entity.data) {
								sb.append(";");
								sb.append(df.format(value));
							}
							sb.append(System.getProperty("line.separator"));
							fileWriter.write(sb.toString());
							fileWriter.flush();
						}
					} catch (InterruptedException e) {
						Log.d(TAG, "saveTask interrupted");
					}
				} catch (IOException e) {
					Log.d(TAG, "saveTask IOException: " + e.getMessage());
				}
			}
		};
	}

	private void initSensorTypesName() {
		SENSOR_TYPES_NAME.put(Sensor.TYPE_ACCELEROMETER, "ACCELEROMETER");
		SENSOR_TYPES_NAME.put(Sensor.TYPE_GYROSCOPE, "GYROSCOPE");
		SENSOR_TYPES_NAME.put(Sensor.TYPE_ORIENTATION, "ORIENTATION");
		SENSOR_TYPES_NAME.put(Sensor.TYPE_MAGNETIC_FIELD, "MAGNETIC");
	}

	private String getSensorTypesName(int sensorType) {
		return SENSOR_TYPES_NAME.get(sensorType);
	}

	private void initSensors() {
		mSensors = new SparseArray<Sensor>();
		for (int sensorType : SENSOR_TYPES) {
			Sensor sensor = mSensorManager.getDefaultSensor(sensorType);
			if (sensor != null) {
				mSensors.put(sensorType, sensor);
			} else {
				Toast.makeText(this,
						String.format("Missing Sensor, ID: %1$d", sensorType),
						Toast.LENGTH_SHORT).show();
			}
		}
		for (int sensorType : SENSOR_TYPES) {
			SensorEventListener sensorListener = getSensorEventListener(sensorType);
			if (sensorListener != null) {
				mSensorEventListeners.put(sensorType, sensorListener);
			} else {
				Toast.makeText(
						this,
						String.format("Missing SensorListeners, ID: %1$d",
								sensorType), Toast.LENGTH_SHORT).show();
			}
		}
	}

	private BufferedWriter getFileWriter(int sensorType) {
		BufferedWriter fileWriter = mSensorFileWriters.get(sensorType);
		if (fileWriter != null) {
			return fileWriter;
		} else {
			File sdDir = FilesUtils.getFilesDir(getApplicationContext());
			File sensorDir = new File(sdDir, getSensorTypesName(sensorType));
			if (!sensorDir.exists())
				sensorDir.mkdirs();
			String fileName = sdfFileName.format(new Date()) + ".txt";
			File sensorFile = new File(sensorDir, fileName);
			try {
				boolean flag = true;
				if (BuildConfig.DEBUG)
					flag = false;
				fileWriter = new BufferedWriter(
						new FileWriter(sensorFile, flag));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mSensorFileWriters.put(sensorType, fileWriter);
			return fileWriter;
		}

	}

	private SensorEventListener getSensorEventListener(int sensorType) {
		switch (sensorType) {
		case Sensor.TYPE_ACCELEROMETER:
			return new AccelerometerListener();
		case Sensor.TYPE_ORIENTATION:
			return new OrientationListener();
		default:
			return new CommonListener();
		}
	}

	private void startCollect() {
//		for (int sensorType : SENSOR_TYPES) {
//			if (mSensors.get(sensorType) != null)
//				mSensorManager.registerListener(
//						mSensorEventListeners.get(sensorType),
//						mSensors.get(sensorType),
//						SensorManager.SENSOR_DELAY_FASTEST);
//		}
		 saveDataService.scheduleAtFixedRate(saveTask, 0,
		 1000,TimeUnit.MILLISECONDS);
	}

	public void startSensor(int sensorType) {
		if (mSensors.get(sensorType) != null)
			mSensorManager.registerListener(
					mSensorEventListeners.get(sensorType),
					mSensors.get(sensorType),
					SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void stopSensor(int sensorType) {
		if (mSensors.get(sensorType) != null) {
			mSensorManager.unregisterListener(
					mSensorEventListeners.get(sensorType),
					mSensors.get(sensorType));
			BufferedWriter fileWriter = mSensorFileWriters.get(sensorType);
			if (fileWriter != null) {
				try {
					fileWriter.flush();
					fileWriter.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void stopCollect() {
//		for (int sensorType : SENSOR_TYPES) {
//			if (mSensors.get(sensorType) != null) {
//				mSensorManager.unregisterListener(
//						mSensorEventListeners.get(sensorType),
//						mSensors.get(sensorType));
//				BufferedWriter fileWriter = mSensorFileWriters.get(sensorType);
//				if (fileWriter != null) {
//					try {
//						fileWriter.flush();
//						fileWriter.close();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}
//		}
		saveDataService.shutdown();
	}

	class AccelerometerListener implements SensorEventListener {
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// do nothing
		}

		public void onSensorChanged(SensorEvent event) {
			LogEntity entity = new LogEntity();
			entity.timestamp = System.currentTimeMillis();
			entity.type = Sensor.TYPE_ACCELEROMETER;
			entity.data = new ArrayList<Float>();
//			float[] lpfAndDevOutput = new float[3];
			System.arraycopy(event.values, 0, mDataAccelerometer, 0,
					event.values.length);
//
//			mDataAccelerometer[0] = mDataAccelerometer[0]
//					/ SensorManager.GRAVITY_EARTH;
//			mDataAccelerometer[1] = mDataAccelerometer[1]
//					/ SensorManager.GRAVITY_EARTH;
//			mDataAccelerometer[2] = mDataAccelerometer[2]
//					/ SensorManager.GRAVITY_EARTH;
//			lpfAndDevOutput = lpfAndDev.addSamples(mDataAccelerometer);

			for (int index = 0; index < mDataAccelerometer.length; ++index) {
				entity.data.add((Float) mDataAccelerometer[index]);
			}
			if (updateUIListener != null) {
				updateUIListener.Update(entity);
			}
			try {
				mPushQueue.put(entity);
			} catch (InterruptedException e) {
				Log.d(TAG,
						"AccelerometerListener onSensorChanged Interrupted: "
								+ e.getMessage());
			}
		}
	}

	class OrientationListener implements SensorEventListener {
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// do nothing
		}

		public void onSensorChanged(SensorEvent event) {
			float x = event.values[SensorManager.DATA_X]; 
			float y = event.values[SensorManager.DATA_Y]; 
			float z = event.values[SensorManager.DATA_Z];
//			System.out.println("x"+x+" "+"y"+y+" "+"z"+z+" ");
			LogEntity entity = new LogEntity();
			entity.timestamp = System.currentTimeMillis();
			entity.type = Sensor.TYPE_ORIENTATION;
			entity.data = new ArrayList<Float>();
			System.arraycopy(event.values, 0, mDataOrientation, 0,
					event.values.length);
			for (int index = 0; index < event.values.length; ++index) {
				entity.data.add((Float) event.values[index]);
			}
			if (updateUIListener != null) {
				updateUIListener.Update(entity);
			}
			try {
				mPushQueue.put(entity);
			} catch (InterruptedException e) {
				Log.d(TAG, "OrientationListener onSensorChanged Interrupted: "
						+ e.getMessage());
			}
		}
	}

	class CommonListener implements SensorEventListener {
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// do nothing
		}

		public void onSensorChanged(SensorEvent event) {
			LogEntity entity = new LogEntity();
			entity.timestamp = System.currentTimeMillis();
			entity.type = event.sensor.getType();
			entity.data = new ArrayList<Float>();
			for (int index = 0; index < event.values.length; ++index) {
				entity.data.add((Float) event.values[index]);
			}
			if (updateUIListener != null) {
				updateUIListener.Update(entity);
			}
			try {
				mPushQueue.put(entity);
			} catch (InterruptedException e) {
				Log.d(TAG,
						"CommonListener onSensorChanged Interrupted: "
								+ e.getMessage());
			}
		}
	}

	public void setUpdataUIListener(IUpdateUI updateUI) {
		this.updateUIListener = updateUI;
	}
}

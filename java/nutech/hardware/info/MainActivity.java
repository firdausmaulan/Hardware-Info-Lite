package nutech.hardware.info;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings.Secure;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;

@SuppressLint("NewApi")
public class MainActivity extends ActionBarActivity {
	private ListView lvItem;
	private ArrayList<ListContent> listItem;
	private String _BRAND, _HARDWARE, _MODEL, _DISPLAY, _CPU_ABI, _CPU_INFO,
			_TOTALMEM, _AVAILMEM, _TOTALROM, _AVAILROM,
			_TOTALSDCARD = "Can't Read SDCard",
			_AVAILSDCARD = "Can't Read SDCard", _BATCAPACITY, _BATLEVEL,
			_BACK_CAMERA = "Not  Available", _FRONT_CAMERA = "Not Available",
			_NETWORKTYPE = "No Sim Card", _IMEI, _IMSI, _RELEASE,
			_OSNAME = "Unknowed", _API, _SCREENSIZE, _SCREENRES, _DPI,
			_OPERATOR = "No Sim Card";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		String[] listLeft = { "Brand", "Hardware", "Type", "Build ID",
				"Android Version", "OS Name", "API Level", "Processor",
				"Cores", "Total RAM", "Available RAM", "Total Storage",
				"Available Storage", /*"External Storage", "External Available",*/
				"Screen Size", "Screen Resolution", "DPI", "Back Camera",
				"Front Camera", "Battery Capacity", "Battery Level",
				"Network Type", "IMEI", "IMSI", "Operator Name" };

		// Get device & android system info
		androidSystem();
		// get Processor RAM info
		CPU_RAM();
		// get storage info
		storage();
		// get display info
		display();
		// get camera info
		backCamera();
		frontCamera();
		// get battery info
		battery();
		// get telephony manager info
		telephonyManager();

		// Add Item to ListView
		String[] listRight = { _BRAND, _HARDWARE, _MODEL, _DISPLAY, _RELEASE,
				_OSNAME, _API, _CPU_ABI, _CPU_INFO, _TOTALMEM, _AVAILMEM,
				_TOTALROM, _AVAILROM, /*_TOTALSDCARD, _AVAILSDCARD,*/ _SCREENSIZE,
				_SCREENRES, _DPI, _BACK_CAMERA, _FRONT_CAMERA, _BATCAPACITY,
				_BATLEVEL, _NETWORKTYPE, _IMEI, _IMSI, _OPERATOR };
		lvItem = (ListView) findViewById(R.id.listContent);
		listItem = new ArrayList<ListContent>();
		ListContent inside = null;
		for (int i = 0; i < listLeft.length; i++) {
			inside = new ListContent();
			inside.setLeft(listLeft[i]);
			inside.setRight(listRight[i]);
			listItem.add(inside);
		}
		CustomListView adapter = new CustomListView(MainActivity.this, listItem);
		lvItem.setAdapter(adapter);

	}// End of method onCreate (UI)

	// Android System
	private void androidSystem() {
		_RELEASE = android.os.Build.VERSION.RELEASE;
		_MODEL = android.os.Build.MODEL;
		_BRAND = android.os.Build.BRAND;
		_DISPLAY = android.os.Build.DISPLAY;
		_CPU_ABI = android.os.Build.CPU_ABI;
		_HARDWARE = android.os.Build.HARDWARE;
		_API = String.valueOf(android.os.Build.VERSION.SDK_INT);
		StringBuilder _OSName = new StringBuilder();
		Field[] fields = Build.VERSION_CODES.class.getFields();
		for (Field field : fields) {
			String fieldName = field.getName();
			int fieldValue = -1;
			try {
				fieldValue = field.getInt(new Object());
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			if (fieldValue == Build.VERSION.SDK_INT) {
				_OSNAME = String.valueOf(_OSName.append(fieldName));
			}
		}
	}

	// CPU & RAM
	private void CPU_RAM() {
		// CPU
		String _CPUVersion;
		if (Build.VERSION.SDK_INT >= 17) {
			_CPU_INFO = String.valueOf(Runtime.getRuntime()
					.availableProcessors());
		} else { // old version
			_CPUVersion = getCPUVersion();
			_CPU_INFO = getCPUInfo() + " " + _CPUVersion;
		}
		// RAM
		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(memoryInfo);
		long availableMemory = memoryInfo.availMem / 1048576L;
		long totalMemory;
		if (Build.VERSION.SDK_INT >= 16) {
			totalMemory = (memoryInfo.totalMem + availableMemory) / 1048576L;
		} else {
			totalMemory = Long.parseLong(getTotalRAM());
			totalMemory = (totalMemory + availableMemory) / 1048576L;
		}
		_AVAILMEM = String.valueOf(availableMemory) + " MB";
		_TOTALMEM = String.valueOf(totalMemory) + " MB";
	}

	// CPU Old Version
	public static String getCPUInfo() {
		RandomAccessFile reader = null;
		String load = null;
		try {
			reader = new RandomAccessFile("/proc/cpuinfo", "r");
			load = reader.readLine();
			reader.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return load;
	}

	public static String getCPUVersion() {
		RandomAccessFile reader = null;
		String load = null;
		try {
			reader = new RandomAccessFile("/proc/version", "r");
			load = reader.readLine();
			reader.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return load;
	}

	// RAM Old Version
	public static String getTotalRAM() {
		RandomAccessFile reader = null;
		String load = null;
		try {
			reader = new RandomAccessFile("/proc/meminfo", "r");
			load = reader.readLine();
			reader.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return load;
	}

	private void storage() {
		// Internal Storage (ROM)
		long TotalROM = new File(getFilesDir().getAbsoluteFile().toString())
				.getTotalSpace();
		long AvailROM = new File(getFilesDir().getAbsoluteFile().toString())
				.getFreeSpace();
		_TOTALROM = String.valueOf(TotalROM / 1048576L) + " MB";
		_AVAILROM = String.valueOf(AvailROM / 1048576L) + " MB";
		
		// External Storage
		String state = Environment.getExternalStorageState();
		if (android.os.Environment.MEDIA_MOUNTED.equals(state)) {
			if (Build.VERSION.SDK_INT >= 19) {
				File externalStorageDir = Environment.getExternalStorageDirectory();
				long AvailSDCard = externalStorageDir.getFreeSpace() / 1024 / 1024;
				long TotalSDCard = externalStorageDir.getTotalSpace() / 1024 /1024;
				_TOTALSDCARD = String.valueOf(TotalSDCard  + " MB");
				_AVAILSDCARD = String.valueOf(AvailSDCard  + " MB");
			} else {
				getTotalExternalMemorySize();
				getAvailableExternalMemorySize();
			}
		}
	}

	// External Storage Old Version
	public static boolean externalMemoryAvailable() {
		return android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
	}

	@SuppressWarnings("deprecation")
	public void getAvailableExternalMemorySize() {
		if (externalMemoryAvailable()) {
			File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
			_AVAILSDCARD = String.valueOf(formatSize(availableBlocks * blockSize));
		}
	}

	@SuppressWarnings("deprecation")
	public void getTotalExternalMemorySize() {
		if (externalMemoryAvailable()) {
			File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
			_TOTALSDCARD = String.valueOf(formatSize(totalBlocks * blockSize));
		}
	}
	
	public static String formatSize(long size) {
        String suffix = null;

        if (size >= 1024) {
            suffix = "KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = "MB";
                size /= 1024;
            }
        }

        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }

        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }

	// Display
	private void display() {
		WindowManager wm = ((WindowManager) getSystemService(Context.WINDOW_SERVICE));
		Display display = wm.getDefaultDisplay();
		Point myPoint = getDisplaySize(display);
		_SCREENRES = String.valueOf(myPoint.y) + "x" + String.valueOf(myPoint.x) + " pixel";
		DisplayMetrics dm = getResources().getDisplayMetrics();
		int densityDpi = dm.densityDpi;
		float screenSize = myPoint.y / densityDpi;
		_SCREENSIZE = String.valueOf(screenSize + 0.5) + " inch";
		_DPI = String.valueOf(densityDpi) + " pixel/inch";
	}

	@SuppressWarnings("deprecation")
	private static Point getDisplaySize(final Display display) {
		Point point = new Point();
		if (Build.VERSION.SDK_INT >= 13) {
			display.getSize(point);
		} else {
			point.x = display.getWidth();
			point.y = display.getHeight();
		}
		return point;
	}

	// Camera
	// Back Camera
	private void backCamera() {
		Context context = this;
		PackageManager packageManager = context.getPackageManager();
		int checkCamera = Camera.getNumberOfCameras();
		if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
				&& checkCamera > 0) {
			Camera camera = Camera.open(0); // For Back Camera
			android.hardware.Camera.Parameters params = camera.getParameters();
			List<Size> sizes = params.getSupportedPictureSizes();
			Camera.Size result = null;

			ArrayList<Integer> arrayListForWidth = new ArrayList<Integer>();
			ArrayList<Integer> arrayListForHeight = new ArrayList<Integer>();

			for (int i = 0; i < sizes.size(); i++) {
				result = (Size) sizes.get(i);
				arrayListForWidth.add(result.width);
				arrayListForHeight.add(result.height);
			}
			if (arrayListForWidth.size() != 0 && arrayListForHeight.size() != 0) {
				float back = (((Collections.max(arrayListForWidth)) * (Collections
						.max(arrayListForHeight))) / (1024 * 1024.0f));
				DecimalFormat df = new DecimalFormat("#.#");
				_BACK_CAMERA = String.valueOf(df.format(back)) + " MP";
			}
			camera.release();
			arrayListForWidth.clear();
			arrayListForHeight.clear();

		}
	}

	// Front Camera
	private void frontCamera() {
		Context context = this;
		PackageManager packageManager = context.getPackageManager();
		int checkCamera = Camera.getNumberOfCameras();
		if (packageManager
				.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
				&& checkCamera > 1) {
			Camera camera = Camera.open(1); // For Front Camera
			android.hardware.Camera.Parameters params1 = camera.getParameters();
			List<Size> sizes1 = params1.getSupportedPictureSizes();
			Camera.Size result1 = null;

			ArrayList<Integer> arrayListForWidth = new ArrayList<Integer>();
			ArrayList<Integer> arrayListForHeight = new ArrayList<Integer>();

			for (int i = 0; i < sizes1.size(); i++) {
				result1 = (Size) sizes1.get(i);
				arrayListForWidth.add(result1.width);
				arrayListForHeight.add(result1.height);
			}
			if (arrayListForWidth.size() != 0 && arrayListForHeight.size() != 0) {
				float front = (((Collections.max(arrayListForWidth)) * (Collections
						.max(arrayListForHeight))) / (1024 * 1024.0f));
				DecimalFormat df = new DecimalFormat("#.#");
				_FRONT_CAMERA = String.valueOf(df.format(front)) + " MP";
			}
			camera.release();
		}
	}

	// Battery
	private void battery() {
		_BATCAPACITY = getBatteryCapacity() + " mAh";
		Intent batteryIntent = getApplicationContext().registerReceiver(null,
				new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int rawlevel = batteryIntent.getIntExtra("level", -1);
		double scale = batteryIntent.getIntExtra("scale", -1);
		_BATLEVEL = "Unknowed";
		if (rawlevel >= 0 && scale > 0) {
			_BATLEVEL = String.valueOf(rawlevel / scale * 100).trim() + " %";
		}
	}

	// Battery Capacity
	public String getBatteryCapacity() {
		Object mPowerProfile_ = null;
		String capacity = "";
		final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";
		try {
			mPowerProfile_ = Class.forName(POWER_PROFILE_CLASS)
					.getConstructor(Context.class).newInstance(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			double batteryCapacity = (Double) Class
					.forName(POWER_PROFILE_CLASS)
					.getMethod("getAveragePower", java.lang.String.class)
					.invoke(mPowerProfile_, "battery.capacity");
			capacity = String.valueOf(batteryCapacity);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return capacity;
	}

	// Telephony Manager
	// IMEI, IMSI, Network Operator Name, Network Type
	private void telephonyManager() {
		String serviceName = Context.TELEPHONY_SERVICE;
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(serviceName);
		if (telephonyManager.getDeviceId() != null) {
			_IMEI = telephonyManager.getDeviceId();
		} else {
			_IMEI = Secure.getString(getApplicationContext()
					.getContentResolver(), Secure.ANDROID_ID);
		}
		_IMSI = telephonyManager.getSubscriberId();
		if (!telephonyManager.getNetworkOperatorName().equals(null)) {
			_OPERATOR = telephonyManager.getNetworkOperatorName();
			_NETWORKTYPE = getNetworkType();
		}
	}

	// Network Type
	public String getNetworkType() {
		TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		int networkType = mTelephonyManager.getNetworkType();
		switch (networkType) {
		case TelephonyManager.NETWORK_TYPE_GPRS:
		case TelephonyManager.NETWORK_TYPE_EDGE:
		case TelephonyManager.NETWORK_TYPE_CDMA:
		case TelephonyManager.NETWORK_TYPE_1xRTT:
		case TelephonyManager.NETWORK_TYPE_IDEN:
			return "2G";
		case TelephonyManager.NETWORK_TYPE_UMTS:
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
		case TelephonyManager.NETWORK_TYPE_HSDPA:
		case TelephonyManager.NETWORK_TYPE_HSUPA:
		case TelephonyManager.NETWORK_TYPE_HSPA:
		case TelephonyManager.NETWORK_TYPE_EVDO_B:
		case TelephonyManager.NETWORK_TYPE_EHRPD:
		case TelephonyManager.NETWORK_TYPE_HSPAP:
			return "3G";
		case TelephonyManager.NETWORK_TYPE_LTE:
			return "4G";
		default:
			return "Unknown";
		}
	}

	// Get My Number
	public void getMyNumber(View v) {
		String ussd = "Temporary we only support Network Operator in Indonesia (TELKOMSEL / INDOSAT / XL / 3)";
		if (_OPERATOR.equals("3")) {
			ussd = "*111*1" + Uri.encode("#");
			startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
					+ ussd)));
		} else if (_OPERATOR.equals("INDOSAT")) {
			ussd = "*123*7*2*1" + Uri.encode("#");
			startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
					+ ussd)));
		} else if (_OPERATOR.equals("INDOSATOOREDOO")) {
			ussd = "*123*7*2*1" + Uri.encode("#");
			startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
					+ ussd)));
		} else if (_OPERATOR.equals("TELKOMSEL")) {
			ussd = "*808" + Uri.encode("#");
			startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
					+ ussd)));
		} else if (_OPERATOR.equals("XL")) {
			ussd = "*123*22*1*1" + Uri.encode("#");
			startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
					+ ussd)));
		} else {
			AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
					.create();
			alertDialog.setTitle("Hardware Info Lite");
			alertDialog.setMessage(ussd);
			alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			alertDialog.show();
		}
	}

	// Handle Back Button
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			final CharSequence[] items = { "Share", "Rate This App",
					"More Apps by Nutech", "Exit" };
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Hardware Info Lite");
			builder.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					if (item == 0) {
						Intent intent = new Intent();
						intent.setAction(Intent.ACTION_SEND);
						intent.setType("text/plain");
						intent.putExtra(
								Intent.EXTRA_TEXT,
								"Android Hardware Info Lite | https://play.google.com/store/apps/details?id=nutech.hardware.info");
						startActivity(Intent.createChooser(intent, "Share"));
					} else if (item == 1) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri
								.parse("https://play.google.com/store/apps/details?id=nutech.hardware.info"));
						startActivity(intent);
					} else if (item == 2) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri
								.parse("https://play.google.com/store/search?q=nutech%20integrasi&c=apps"));
						startActivity(intent);
					} else if (item == 3) {
						finish();
					}
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}
		return false;
	}
}

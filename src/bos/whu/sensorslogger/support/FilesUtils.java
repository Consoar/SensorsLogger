package bos.whu.sensorslogger.support;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import java.io.File;


public class FilesUtils {
  private static final String EXTERNAL_STORAGE_PERMISSION =
      "android.permission.WRITE_EXTERNAL_STORAGE";

  public static File getFilesDir(final Context context) {
    File appFilesDir = null;
    if (android.os.Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
        && hasExternalStoragePermission(context)) {
      appFilesDir = getExternalFilesDir(context);
    }
    if (appFilesDir == null) appFilesDir = context.getFilesDir();
    return appFilesDir;
  }

  /**
   * Get the external app Files directory.
   *
   * @param context The context to use
   * @return The external Files dir
   */
  public static File getExternalFilesDir(final Context context) {
    if (hasExternalFilesDir()) return context.getExternalFilesDir(null);

    // Before Froyo we need to construct the external Files dir ourselves
    final String FilesDir = "/Android/data/" + context.getPackageName() + "/files/";
    return new File(Environment.getExternalStorageDirectory().getPath() + FilesDir);
  }

  public static File getExternalFilesDir(final Context context, String name) {
    return new File(getExternalFilesDir(context), name);
  }

  public static File getFilesDir(final Context context, String name) {
    return new File(getFilesDir(context), name);
  }

  public static boolean hasExternalFilesDir() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
  }

  private static boolean hasExternalStoragePermission(Context context) {
    int perm = context.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION);
    return perm == PackageManager.PERMISSION_GRANTED;
  }
}

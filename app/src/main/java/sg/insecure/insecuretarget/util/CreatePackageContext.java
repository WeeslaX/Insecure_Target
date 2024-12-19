package sg.insecure.insecuretarget.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

public class CreatePackageContext {

    private static final String TAG = CreatePackageContext.class.getCanonicalName();

    public static void scanAndLoadPackage(Context appContext, String targetPrefix){
        PackageManager pm = appContext.getPackageManager();

        try{
            // Iterate over all installed packages
            for (PackageInfo installedPackage : pm.getInstalledPackages(0)) {
                String currentPackageName = installedPackage.packageName;

                // Check if the package name matches the required prefix
                if (currentPackageName.startsWith(targetPrefix)) {
                    Log.d(TAG, "Found potential package " + currentPackageName + ", attempting to DCL.");

                    // Create a package context for the module
                    Context moduleContext = appContext.createPackageContext(
                            currentPackageName,
                            Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY
                    );

                    /* Loading Class */
                    ClassLoader moduleClassLoader = moduleContext.getClassLoader();
                    Class<?> moduleClass = moduleClassLoader.loadClass(currentPackageName + ".utils.CodeToLoad");

                    /* Printing available information about loaded class */
                    Log.d(TAG, "Class found: " + moduleClass.getCanonicalName());

                    // Get all declared fields (including constants)
                    Log.d(TAG, "-- Printing Constants --");
                    java.lang.reflect.Field[] fields = moduleClass.getDeclaredFields();

                    // Loop through fields and print constants
                    for (java.lang.reflect.Field field : fields) {
                        // Check if the field is static and final (constant)
                        if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) &&
                                java.lang.reflect.Modifier.isFinal(field.getModifiers())) {

                            // Check if the field is private or not
                            boolean isPrivate = java.lang.reflect.Modifier.isPrivate(field.getModifiers());

                            // Bypass access restrictions for private fields
                            field.setAccessible(true);

                            // Get the value of the constant
                            Object value = field.get(null);  // 'null' for static fields

                            // Print whether the field is private or not
                            String visibility = isPrivate ? "Private" : "Public";
                            Log.d(TAG, "Constant: " + field.getName() + " = " + value + " (" + visibility + ")");
                        }
                    }

                    Log.d(TAG, "-- Printing Methods --");
                    // Get all public methods (including inherited ones)
                    java.lang.reflect.Method[] methods = moduleClass.getMethods();
                    // Log each method name
                    for (java.lang.reflect.Method method : methods) {
                        Log.d(TAG, "Public Method: " + method.getName());
                    }
                    // Get all declared methods (including private ones, not inherited)
                    java.lang.reflect.Method[] declaredMethods = moduleClass.getDeclaredMethods();
                    for (java.lang.reflect.Method method : declaredMethods) {
                        Log.d(TAG, "Declared/Private Method: " + method.getName());
                    }

                    /* Invoking Method */
                    // Instantiate the module class and invoke a method (reflection) - Accessing a Class Object Public Method (Verified)
//                    Object moduleInstance = moduleClass.newInstance();
//                    moduleClass.getMethod("initialize", Context.class).invoke(moduleInstance, appContext);

                    // Instantiate the module class and invoke a method (reflection) - Accessing a static Method (Verified)
                    moduleClass.getMethod("initialize", Context.class).invoke(null, appContext);

                    // Instantiate the module class and invoke a method (reflection) - Accessing a private method (Experimental)
//                    Object moduleInstance = moduleClass.newInstance();
//                    java.lang.reflect.Method privateMethod = moduleClass.getDeclaredMethod("initialize", Context.class);
//                    privateMethod.setAccessible(true);
//                    privateMethod.invoke(moduleInstance, appContext);

                    Log.d(TAG, "Module loaded successfully: " + installedPackage);

                    return;
                }
            }

            // Print Toast when target prefix not found
            Toast.makeText(appContext, "Target package prefix \"" + targetPrefix + "\" not found. DCL not performed.", Toast.LENGTH_LONG).show();

        }catch(Exception e){
            // Unable to find target package
            Log.e(TAG, "Error loading or invoking module method: " + e.getMessage());
            Log.e(TAG, "Stack Trace:    ", e);
        }
    }
}



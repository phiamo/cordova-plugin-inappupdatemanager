package InAppUpdateManager;

import android.content.Context;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;

import android.content.IntentSender;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.gms.Task;

/**
 * This class echoes a string called from JavaScript.
 */
public class InAppUpdateManager extends CordovaPlugin {

    protected AppUpdateManager appUpdateManager;

    @Override
    public boolean execute(String action, JSONArray args,
            CallbackContext callbackContext) {
        if (action.equals("immediate")) {

            Context context = cordova.getActivity().getApplicationContext();
            this.startUpdateCheck(context);
            return true;
        }
        return false;
    }

    public static final int REQUEST_CODE = 108108;

    private void startUpdateCheck(Context context) {
        // Creates instance of the manager.
        appUpdateManager = AppUpdateManagerFactory.create(context);

        // Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    // For a flexible update, use AppUpdateType.FLEXIBLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                // Request the update.

                try {
                    appUpdateManager.startUpdateFlowForResult(
                            // Pass the intent that is returned by 'getAppUpdateInfo()'.
                            appUpdateInfo,
                            // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                            AppUpdateType.IMMEDIATE,
                            // The current activity making the update request.
                            cordova.getActivity(),
                            // Include a request code to later monitor this update request.
                            REQUEST_CODE);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Checks that the update is not stalled during 'onResume()'.
    // However, you should execute this check at all entry points into the app.
    @Override
    public void onResume(boolean multitaskin) {
        super.onResume(multitaskin);

        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(
                        appUpdateInfo -> {
                            if (appUpdateInfo
                                    .updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                                // If an in-app update is already running, resume the update.
                                try {
                                    appUpdateManager.startUpdateFlowForResult(
                                            appUpdateInfo,
                                            AppUpdateType.IMMEDIATE,
                                            cordova.getActivity(),
                                            REQUEST_CODE);
                                } catch (IntentSender.SendIntentException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
    }
}
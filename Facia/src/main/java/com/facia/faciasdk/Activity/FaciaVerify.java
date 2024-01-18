package com.facia.faciasdk.Activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.facia.faciasdk.Activity.Helpers.Enums.DocumentType;
import com.facia.faciasdk.Activity.Helpers.Enums.ServiceType;
import com.facia.faciasdk.Activity.Helpers.IntentHelper;
import com.facia.faciasdk.Activity.Helpers.RequestModel;
import com.facia.faciasdk.Camera.CameraFragment;
import com.facia.faciasdk.Consent.ConsentFragment;
import com.facia.faciasdk.Logs.Webhooks;
import com.facia.faciasdk.R;
import com.facia.faciasdk.Singleton.SingletonData;
import com.facia.faciasdk.Utils.Constants.ApiConstants;
import com.facia.faciasdk.Utils.Constants.ThresholdConstants;
import com.facia.faciasdk.Utils.Utilities;
import com.facia.faciasdk.VerificationType.VerificationTypeFragment;

import org.json.JSONException;

import java.util.HashMap;

public class FaciaVerify extends AppCompatActivity implements SensorEventListener, ActivityListener {
    /**
     * method to send uncaught exceptions to Crash report to ErrorLog class
     */
    private final Thread.UncaughtExceptionHandler _unCaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            try {
                Webhooks.sendStacktraceReport((Exception) ex, this.getClass().getName());
            } catch (Exception e) {
                Webhooks.exceptionReport(e, "FaciaVerify/_unCaughtExceptionHandler");
            }
        }
    };
    private RequestModel requestModel;
    private SensorManager mSensorManager;
    private int iterationAccel = 0, iterationGyro = 0;
    private float prevMotionAccel = 0, prevMotionGyro = 0;
    private Boolean isAccelChanged = false, isAccelIterationComp = false, isToOpenCam = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facia_verify);
        if (!SingletonData.getInstance().isSdkStarted()) {
            SingletonData.getInstance().setSdkStarted(true);
            exceptionInitialization();
            setAppContextRelatedData();
        }
    }

    /**
     * setting app related values e.g
     * themes, singleton values etc
     */
    private void setAppContextRelatedData() {
        try {
            setTheme(R.style.Theme_Facia);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            requestModel = (RequestModel) IntentHelper.getInstance().getObject(ApiConstants.REQUEST_MODEL);
            initValuesInSingleton();
            if (requestModel.getConfigObject().getBoolean("emulatorDetection")) {
                initSensors();
                loadInterface();
            }
            if (!requestModel.getConfigObject().getBoolean("emulatorDetection") ||
                    requestModel.getConfigObject().getBoolean("showConsent") ||
                    requestModel.getConfigObject().getBoolean("showVerificationType"))
                loadInterface();
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "FaciaVerify/setAppContextRelatedData");
        }
    }

    /**
     * this function is initializing the thread that catch uncaught exceptions
     * invoke initializingClasses method
     */
    private void exceptionInitialization() {
        try {
            Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(_unCaughtExceptionHandler);
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "FaciaVerify/exceptionInitialization");
        }
    }

    /**
     * method to set values in the Singleton that
     * are required be initialised on start of the SDK
     */
    private void initValuesInSingleton() {
        try {
            SingletonData.getInstance().setActivity(this);
            SingletonData.getInstance().setContext(this);
            SingletonData.getInstance().setApplication(getApplication());
            SingletonData.getInstance().setReferenceId("");
            SingletonData.getInstance().setBlinkCount(0);
            SingletonData.getInstance().setVideoRecording(false);
            SingletonData.getInstance().setOvalSizeIncreased(false);
            SingletonData.getInstance().setSizeIncreasing(false);
            SingletonData.getInstance().setFaceFinalized(false);
            SingletonData.getInstance().setActivityListener(this);
            SingletonData.getInstance().setFragmentManager(getSupportFragmentManager());
            if (requestModel.getConfigObject().has("appInfo")) {
                SingletonData.getInstance().setAppInfo(", " + requestModel.getConfigObject().getString("appInfo"));
            }
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "FaciaVerify/initValuesInSingleton");
        }
    }

    /**
     * method to init sensors to detect emulators
     */
    private void initSensors() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor mGyroScope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroScope, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * method to load UI, if to show consent or not
     */
    private void loadInterface() {
        try {
            if (requestModel.getConfigObject().getBoolean("showConsent")) {
                showConsentFragment();
            } else if (!requestModel.isSimilarity() && requestModel.getConfigObject().getBoolean("showVerificationType")) {
                showVerificationTypeFragment();
            } else {
                if (requestModel.isSimilarity()) {
                    showCameraFragment();
                } else if (Utilities.SimilarMethods.checkCameraPermission("camera")) {
                    showCameraFragment();
                }
            }
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "FaciaVerify/loadInterface");
        }
    }

    /**
     * call Consent screen to be displayed
     */
    private void showConsentFragment() {
        try {
            Fragment consentFragment = new ConsentFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, consentFragment, consentFragment.getClass().getSimpleName())
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "FaciaVerify/showConsentFragment");
        }
    }

    private void showVerificationTypeFragment() {
        try {
            Fragment verificationTypeFragment = new VerificationTypeFragment();
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.enter_from_left)
                    .replace(R.id.nav_host_fragment, verificationTypeFragment, verificationTypeFragment.getClass().getSimpleName())
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "FaciaVerify/showVerificationTypeFragment");
        }
    }

    /**
     * method to start the camera if user allowed camera permission
     */
    private void showCameraFragment() {
        try {
            Fragment cameraFragment = new CameraFragment();
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.enter_from_left)
                    .replace(R.id.nav_host_fragment, cameraFragment, cameraFragment.getClass().getSimpleName())
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "FaciaVerify/showCameraFragment");
        }
    }

    /**
     * method to terminate SDK
     * if emulator detected, or user did not allow to access camera
     *
     * @param event event to return to merchant to update about sdk result
     */
    @Override
    public void terminateSdk(String event) {
        try {
            HashMap<String, String> requestResponseObj;
            requestResponseObj = new HashMap<>();
            requestResponseObj.put("reference_id", "");
                requestResponseObj.put("event", event);
            requestModel.getRequestListener().requestStatus(requestResponseObj);
            finish();
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "FaciaVerify/terminateSdk");
        }
    }

    /**
     * will return a value on sensor's value change
     * will track values for Accelerometer and GyroScope
     * to detect emulators
     *
     * @param sensorEvent event containing values regarding the sensor
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        try {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER && !isAccelChanged) {
                if (iterationAccel == 0) {
                    prevMotionAccel = sensorEvent.values[0] + sensorEvent.values[1];
                } else if (iterationAccel < 4) {
                    float currMotionAccel = sensorEvent.values[0] + sensorEvent.values[1];
                    if (currMotionAccel != prevMotionAccel) {
                        isAccelChanged = true;
                    }
                } else if (iterationAccel == 4) {
                    isAccelIterationComp = true;
                    mSensorManager.unregisterListener(this);
                    terminateSdk("emulator.detected");
                }
                iterationAccel++;
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                if (iterationGyro == 0) {
                    prevMotionGyro = sensorEvent.values[0] + sensorEvent.values[1];
                } else if (iterationGyro < 4) {
                    float currMotionGyro = sensorEvent.values[0] + sensorEvent.values[1];
                    if (currMotionGyro != prevMotionGyro) {
                        mSensorManager.unregisterListener(this);
                    }
                } else if (isAccelChanged || isAccelIterationComp) {
                    mSensorManager.unregisterListener(this);
                    terminateSdk("emulator.detected");
                }
                iterationGyro++;
            }
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "FaciaVerify/onSensorChanged");
        }
    }

    /**
     * callback on deciding permission of apps by the user
     * if app got camera permission, will invoke further method
     * to open camera screen, and to set consent shown track value
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (requestCode == ThresholdConstants.CAMERA_PERMISSION_CODE_FACE_LIVENESS) {
                    requestModel.getConfigObject().put("serviceType", ServiceType.FACE_LIVENESS);
                } else if (requestCode == ThresholdConstants.CAMERA_PERMISSION_CODE_MATCH_ID) {
                    requestModel.getConfigObject().put("serviceType", ServiceType.MATCH_TO_PHOTO_ID);
                } else if (requestCode == ThresholdConstants.CAMERA_PERMISSION_CODE_DOC_LIVENESS) {
                    requestModel.getConfigObject().put("serviceType", ServiceType.DOCUMENT_LIVENESS);
                } else if (requestCode == ThresholdConstants.CAMERA_PERMISSION_CODE_ID_CARD_DOC) {
                    requestModel.getConfigObject().put("documentType", DocumentType.ID_CARD);
                } else if (requestCode == ThresholdConstants.CAMERA_PERMISSION_CODE_PASSPORT_DOC) {
                    requestModel.getConfigObject().put("documentType", DocumentType.PASSPORT);
                }
                showCameraFragment();
            } else {
//                terminateSdk(true);
                permissionDialog(requestCode);
            }
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "FaciaVerify/onRequestPermissionsResult");
        }
    }

    /**
     * dialog to open settings to take camera permission
     */
    private void permissionDialog(int requestCode) {
        try {
            Dialog dialog = new Dialog(SingletonData.getInstance().getContext());
            dialog.setContentView(R.layout.permission_dialog_box);
            dialog.setCancelable(false);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            Button goButton = dialog.findViewById(R.id.goBtn);
            goButton.setOnClickListener(view -> {
                dialog.dismiss();
                if (requestCode == ThresholdConstants.CAMERA_PERMISSION_CODE_CAPTURE)
                    finish();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            });
            dialog.show();
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "FaciaVerify/permissionDialog");
        }
    }


    /**
     * will be called whenever the accuracy of the sensors will be changed
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    /**
     * this will be called on SDK termination
     * will unregister the sensor listeners
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mSensorManager != null) {
                mSensorManager.unregisterListener(this);
            }
            if (requestModel != null) {
                if (requestModel.getFaceImage() != null) {
                    if (requestModel.getFaceImage().exists()) {
                        requestModel.getFaceImage().delete();
                    }
                }
                if (requestModel.getIdImage() != null) {
                    if (requestModel.getIdImage().exists()) {
                        requestModel.getIdImage().delete();
                    }
                }
            }
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "FaciaVerify/onDestroy");
        }
    }
}
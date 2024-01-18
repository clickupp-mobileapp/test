package com.facia.faciasdk.Utils;

import static android.os.Build.VERSION.SDK_INT;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.facia.faciasdk.Activity.Helpers.IntentHelper;
import com.facia.faciasdk.Activity.Helpers.RequestModel;
import com.facia.faciasdk.ApiModels.Result.Data;
import com.facia.faciasdk.Logs.Webhooks;
import com.facia.faciasdk.R;
import com.facia.faciasdk.Singleton.SingletonData;
import com.facia.faciasdk.Utils.Constants.ApiConstants;
import com.facia.faciasdk.Utils.Constants.ThresholdConstants;

import java.util.Date;

public class Utilities {

    /**
     * Methods that have to be called multiple times
     */
    public static class SimilarMethods {
        private static Dialog dialog;

        /**
         * Method to check if internet is connected or not
         *
         * @return is internet connected or not i.e: true/false
         */
        public static boolean isConnected() {
            boolean connected = false;
            if (SingletonData.getInstance().getContext() != null) {
                ConnectivityManager connectivityManager = (ConnectivityManager) SingletonData.getInstance().getContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);
                NetworkCapabilities capabilities = null;  // need ACCESS_NETWORK_STATE permission
                if (SDK_INT >= Build.VERSION_CODES.M) {
                    capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                }
                if (SDK_INT >= Build.VERSION_CODES.M) {
                    if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() ==
                            NetworkInfo.State.CONNECTED && capabilities != null && capabilities.hasCapability(
                            NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                        //we are connected to a network
                        connected = true;
                    } else if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null) {
                        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() ==
                                NetworkInfo.State.CONNECTED && capabilities != null && capabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                            //we are connected to a network
                            connected = true;
                        }
                    }
                } else {
                    if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                        //we are connected to a network
                        connected = true;
                    } else if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null) {
                        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED) {
                            //we are connected to a network
                            connected = true;
                        }
                    }
                }
            }
            return connected;
        }

        /**
         * method to return bearer token
         *
         * @param token merchant token to be converted in form of bearer
         * @return it will return Bearer token
         */
        public static String bearerToken(String token) {
            return "Bearer " + token;
        }

        /**
         * method to return unique device id
         */
        @SuppressLint("HardwareIds")
        public static String getDeviceId() {
            return Settings.Secure.getString(
                    SingletonData.getInstance().getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        /**
         * method to display internet dialog in case there is no internet
         */
        public static void internetDialog() {
            try {
                Dialog dialog = new Dialog(SingletonData.getInstance().getContext());
                dialog.setContentView(R.layout.internet_dialog_box);
                dialog.setCancelable(false);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                Button negativeButton = dialog.findViewById(R.id.dismissBtn);
                negativeButton.setOnClickListener(view -> {
                    dialog.dismiss();
                    SingletonData.getInstance().getActivityListener().terminateSdk("internet.issue");
                });
                dialog.show();
            } catch (Exception e) {
                Webhooks.exceptionReport(e, "Utilities/SimilarMethods/internetDialog");
            }
        }

        /**
         * Check Camera Permission
         *
         * @return if permission is granted or not
         */
        public static boolean checkCameraPermission(String fromWhere) {
            if (ContextCompat.checkSelfPermission(SingletonData.getInstance().getContext(),
                    "android.permission.CAMERA") != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SingletonData.getInstance().getActivity(),
                        ThresholdConstants.REQUIRED_PERMISSIONS_CAMERA, getCameraPermissionRequestCode(fromWhere));
                return false;
            }
            return true;
        }

        public static String getDeviceDetails(){
            return Build.FINGERPRINT + ", " + ApiConstants.SDK_VERSION + SingletonData.getInstance().getAppInfo();
        }
        private static int getCameraPermissionRequestCode(String fromWhere) {
            switch (fromWhere) {
                case "consent":
                    return ThresholdConstants.CAMERA_PERMISSION_CODE_CONSENT;
                case "faceLiveness":
                    return ThresholdConstants.CAMERA_PERMISSION_CODE_FACE_LIVENESS;
                case "matchId":
                    return ThresholdConstants.CAMERA_PERMISSION_CODE_MATCH_ID;
                case "documentLiveness":
                    return ThresholdConstants.CAMERA_PERMISSION_CODE_DOC_LIVENESS;
                case "idCardDoc":
                    return ThresholdConstants.CAMERA_PERMISSION_CODE_ID_CARD_DOC;
                case "passportDoc":
                    return ThresholdConstants.CAMERA_PERMISSION_CODE_PASSPORT_DOC;
                default:
                    return ThresholdConstants.CAMERA_PERMISSION_CODE_CAPTURE;
            }
        }
    }
}
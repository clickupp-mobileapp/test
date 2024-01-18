package com.facia.faciasdk.Camera.ApiHelpers;

import android.os.Build;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.facia.faciasdk.Activity.Helpers.IntentHelper;
import com.facia.faciasdk.Activity.Helpers.RequestModel;
import com.facia.faciasdk.ApiModels.CheckSimilarity.CheckSimilarity;
import com.facia.faciasdk.ApiModels.UploadProgress.CountingRequestBody;
import com.facia.faciasdk.Logs.Webhooks;
import com.facia.faciasdk.R;
import com.facia.faciasdk.Singleton.SingletonData;
import com.facia.faciasdk.TrialEnd.TrialEndFragment;
import com.facia.faciasdk.Utils.Constants.ApiConstants;
import com.facia.faciasdk.Utils.Utilities;
import com.facia.faciasdk.databinding.FragmentCameraBinding;

import org.json.JSONObject;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

public class SimilarityApiHelper {
    private final FragmentCameraBinding fragmentCameraBinding;
    private final String token;

    public SimilarityApiHelper(FragmentCameraBinding fragmentCameraBinding, String token) {
        this.fragmentCameraBinding = fragmentCameraBinding;
        this.token = token;
    }

    /**
     * method to set request object to upload files to check similarity
     * for liveness request
     */
    public void checkSimilarityJsonObject(File faceImage, File idImage, float similarityScore, Boolean isCaptured) {
        try {
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            RequestBody faceImageRequestBody = new CountingRequestBody(RequestBody.create(MediaType.parse("image/*"), faceImage),
                    (bytesUploaded, totalBytes) -> {
                        if (bytesUploaded == totalBytes) {
                            SingletonData.getInstance().getCameraListeners().fileUploaded();
                        }
                    });
            RequestBody idImageRequestBody = new CountingRequestBody(RequestBody.create(MediaType.parse("image/*"), idImage),
                    (bytesUploaded, totalBytes) -> {
                        if (bytesUploaded == totalBytes) {
                            SingletonData.getInstance().getCameraListeners().fileUploaded();
                        }
                    });
            builder.addFormDataPart("type", "photo_id_match")
                    .addFormDataPart("face_frame", faceImage.getName(), faceImageRequestBody)
                    .addFormDataPart("device_id", Utilities.SimilarMethods.getDeviceId())
                    .addFormDataPart("id_frame", idImage.getName(), idImageRequestBody);
            RequestBody requestBody = builder.build();
            if (Utilities.SimilarMethods.isConnected()) {
                checkSimilarityApi(requestBody, similarityScore, faceImage, idImage, isCaptured);
            } else {
                Utilities.SimilarMethods.internetDialog();
            }
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "Result/ApiHelper/checkSimilarityJsonObject");
        }
    }

    /**
     * method to call api to upload video
     *
     * @param requestBody request body to be uploaded
     */
    private void checkSimilarityApi(RequestBody requestBody, float similarityScore, File faceFile, File docFile, Boolean isCaptured) {
        try {
            RequestModel requestModel = (RequestModel) IntentHelper.getInstance().getObject(ApiConstants.REQUEST_MODEL);
            SingletonData.getInstance().getApiInterface().checkSimilarity(Utilities.SimilarMethods.bearerToken(token),
                            Utilities.SimilarMethods.getDeviceDetails(), "android", requestBody)
                    .enqueue(new retrofit2.Callback<CheckSimilarity>() {
                        @Override
                        public void onResponse(retrofit2.Call<CheckSimilarity> call, Response<CheckSimilarity> response) {
                            if (response.isSuccessful()) {
                                handleSimilaritySuccess(response, similarityScore, isCaptured);
                            } else {
                                String errorStr = "";
                                try {
                                    String errorBody = response.errorBody().string();
                                    JSONObject jsonObject = new JSONObject(errorBody);
                                    errorStr = jsonObject.getString("message");
                                    handleSimilarityError(response.code() == 400 && errorStr.contains("limit"));
                                } catch (Exception e) {
                                    handleSimilarityError(false);
                                }
                                try {
                                    Webhooks.apiReport("Create Transaction (photo_id_match), Unsuccessful: " + response.code(), response.errorBody().string(), ApiConstants.CREATE_TRANSACTION);
                                } catch (Exception e) {
                                    Webhooks.apiReport("Create Transaction (photo_id_match), Unsuccessful: " + response.code(), "Response not successful", ApiConstants.CREATE_TRANSACTION);
                                }
                            }
                            SingletonData.getInstance().setQuickRequestInProcess(false);
                        }

                        @Override
                        public void onFailure(retrofit2.Call<CheckSimilarity> call, Throwable t) {
                            try {
                                handleSimilarityFailure(t.getMessage());
                            }catch (Exception e){
                                handleSimilarityFailure("");
                            }
                        }
                    });
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "Result/ApiHelper/checkSimilarityApi");
        }
    }

    /**
     * method to handle if check similarity' API's response is successful
     *
     * @param response        response of the API
     * @param similarityScore pre defined similarity score
     */
    private void handleSimilaritySuccess(Response<CheckSimilarity> response, float similarityScore, Boolean isCaptured) {
        try {
            SingletonData.getInstance().setReferenceId(
                    response.body().getResult().getData().getReferenceId());
            SingletonData.getInstance().getCameraListeners().setUi(View.GONE, View.GONE,
                    View.GONE, View.GONE, View.GONE, View.GONE, View.VISIBLE, View.GONE);

            if (isCaptured) {
                if (response.body().getResult().getData().getSimilarityStatus() == 1) {
                    SingletonData.getInstance().getCameraListeners().setAnimationViewsAndText(
                            fragmentCameraBinding.animationView, "facia_success.svg",
                            SingletonData.getInstance().getActivity().getString(R.string.photo_id_matched), "photo_id.match_success",
                            response.body().getResult().getData().getSimilarityScore(), true);
                } else {
                    SingletonData.getInstance().getCameraListeners().setAnimationViewsAndText(
                            fragmentCameraBinding.animationView, "facia_failure.svg",
                            SingletonData.getInstance().getActivity().getString(R.string.photo_id_not_matched), "photo_id.match_failure",
                            response.body().getResult().getData().getSimilarityScore(), true);
                }
            } else if (response.body().getResult().getData().getSimilarityScore() >= similarityScore) {
                SingletonData.getInstance().getCameraListeners().setAnimationViewsAndText(
                        fragmentCameraBinding.animationView, "facia_success.svg",
                        SingletonData.getInstance().getActivity().getString(R.string.photo_id_matched), "photo_id.match_success",
                        response.body().getResult().getData().getSimilarityScore(), true);
            }
//            if (response.body().getResult().getData().getSimilarityScore() >= similarityScore ||
//                    (response.body().getStatus() == true && isCaptured)) {
//                SingletonData.getInstance().getFrameProcessing().setAnimationViewsAndText(
//                        fragmentCameraBinding.animationView, "facia_success.svg",
//                        SingletonData.getInstance().getActivity().getString(R.string.photo_id_matched), "photo_id.match_success",
//                        response.body().getResult().getData().getSimilarityScore());
//            }
            else {
                SingletonData.getInstance().getCameraListeners().setAnimationViewsAndText(
                        fragmentCameraBinding.animationView, "facia_failure.svg",
                        SingletonData.getInstance().getActivity().getString(R.string.photo_id_not_matched), "photo_id.match_failure",
                        response.body().getResult().getData().getSimilarityScore(), true);
            }
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "Camera/ApiHelper/handleSimilaritySuccess");
        }
    }

    /**
     * method to handle if check similarity' API's response is not successful
     */
    private void handleSimilarityError(Boolean isLimitExceeded) {
        try {
            SingletonData.getInstance().getCameraListeners().setUi(View.GONE, View.GONE,
                    View.GONE, View.GONE, View.GONE, View.GONE, View.VISIBLE, View.GONE);
            if (isLimitExceeded) {
                navigateToTrialEnd();
            }else {
                SingletonData.getInstance().getCameraListeners().setAnimationViewsAndText(
                        fragmentCameraBinding.animationView, "facia_failure.svg",
                        SingletonData.getInstance().getActivity().getString(R.string.went_wrong), "error.occurred", 0.0, true);
            }
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "Camera/ApiHelper/handleSimilarityError");
        }
    }

    /**
     * method to navigate to trial end screen
     */
    private void navigateToTrialEnd() {
        try {
            Fragment trialEndFragment = new TrialEndFragment();
            SingletonData.getInstance().getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_from_left)
                    .replace(R.id.nav_host_fragment, trialEndFragment, trialEndFragment.getClass().getSimpleName())
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "SimilarityApiHelper/navigateToTrialEnd");
        }
    }

    /**
     * method to handle if failed to call check similarity' API successfully
     */
    private void handleSimilarityFailure(String error) {
        try {
            SingletonData.getInstance().setQuickRequestInProcess(false);
            SingletonData.getInstance().getCameraListeners().setUi(View.GONE, View.GONE, View.GONE, View.GONE, View.GONE, View.GONE, View.VISIBLE, View.GONE);
            SingletonData.getInstance().getCameraListeners().setAnimationViewsAndText(fragmentCameraBinding.animationView,
                    "facia_failure.svg", SingletonData.getInstance().getActivity().getString(R.string.went_wrong),
                    "error.occurred", 0.0, true);
            Webhooks.apiReport("Create Transaction (photo_id_match), Failure", error, ApiConstants.CREATE_TRANSACTION);
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "Camera/ApiHelper/handleSimilarityFailure");
        }
    }
}
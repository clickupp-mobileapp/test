package com.facia.faciasdk.Camera;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;

import androidx.camera.core.CameraControl;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.facia.faciasdk.Activity.Helpers.Enums.FaceLivenessType;
import com.facia.faciasdk.Activity.Helpers.Enums.OvalSize;
import com.facia.faciasdk.Activity.Helpers.Enums.ServiceType;
import com.facia.faciasdk.Activity.Helpers.RequestModel;
import com.facia.faciasdk.Camera.ApiHelpers.SimilarityApiHelper;
import com.facia.faciasdk.Feedback.FeedbackFragment;
import com.facia.faciasdk.Greeting.GreetingFragment;
import com.facia.faciasdk.Logs.Webhooks;
import com.facia.faciasdk.R;
import com.facia.faciasdk.Singleton.SingletonData;
import com.facia.faciasdk.Utils.Constants.ApiConstants;
import com.facia.faciasdk.Utils.Constants.ThresholdConstants;
import com.facia.faciasdk.Utils.Constants.TimeConstants;
import com.facia.faciasdk.databinding.FragmentCameraBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.JsonArray;

import org.json.JSONException;

import java.util.HashMap;

public class HelperFunctions {
    private final FragmentCameraBinding fragmentCameraBinding;
    private final CameraFragment cameraFragment;
    private final RequestModel requestModel;
    int countdownTime = 3;
    private Boolean isBottomSheetVisible = false;

    public HelperFunctions(FragmentCameraBinding fragmentCameraBinding, CameraFragment cameraFragment, RequestModel requestModel) {
        this.fragmentCameraBinding = fragmentCameraBinding;
        this.cameraFragment = cameraFragment;
        this.requestModel = requestModel;
    }

    /**
     * method to show exit Dialog on back press
     */
    protected void exitDialog() {
        try {
            cameraFragment.dialog = new Dialog(SingletonData.getInstance().getContext());
            cameraFragment.dialog.setContentView(R.layout.exit_dialog_box);
            cameraFragment.dialog.setCancelable(false);
            cameraFragment.dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cameraFragment.dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            Button negativeButton = cameraFragment.dialog.findViewById(R.id.reject);
            Button positiveButton = cameraFragment.dialog.findViewById(R.id.accept);
            negativeButton.setOnClickListener(view -> {
                cameraFragment.dialog.dismiss();
                dialogAction(true);
            });
            positiveButton.setOnClickListener(view -> {
                cameraFragment.dialog.dismiss();
                dialogAction(false);
            });
            cameraFragment.dialog.show();
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "Utilities/SimilarMethods/exitDialog");
        }
    }

    protected void initDlOval() {
        try {
            SingletonData.getInstance().setQuickRequestInProcess(false);
            int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
            int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
            int ovalHeight = (int) (screenHeight * getHeightPercentage());
            int ovalWidth = (int) (ovalHeight * 0.72);

            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) fragmentCameraBinding.faceBorder.getLayoutParams();
            layoutParams.height = ovalHeight;
            layoutParams.width = ovalWidth >= screenWidth ? (int) (screenWidth * 0.9) : ovalWidth;
            fragmentCameraBinding.faceBorder.setLayoutParams(layoutParams);
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/HelperFunctions/initDlOval");
        }
    }

    private double getHeightPercentage() throws JSONException {
        double smallHeightPercent, largeHeightPercent, xLargeHeightPercent, defaultHeightPercent;

        if (cameraFragment.currentState.equals("QL")) {
            smallHeightPercent = 0.6;
            largeHeightPercent = 0.45;
            xLargeHeightPercent = 0.4;
            defaultHeightPercent = 0.5;
        } else {
            if (OvalSize.valueOf(requestModel.getConfigObject().getString("ovalSize")) == OvalSize.SMALL) {
                smallHeightPercent = 0.4;
                largeHeightPercent = 0.25;
                xLargeHeightPercent = 0.2;
                defaultHeightPercent = 0.3;
            } else if (OvalSize.valueOf(requestModel.getConfigObject().getString("ovalSize")) == OvalSize.LARGE) {
                smallHeightPercent = 0.6;
                largeHeightPercent = 0.45;
                xLargeHeightPercent = 0.4;
                defaultHeightPercent = 0.5;
            } else {
                smallHeightPercent = 0.5;
                largeHeightPercent = 0.35;
                xLargeHeightPercent = 0.3;
                defaultHeightPercent = 0.4;
            }
        }
        if ((cameraFragment.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {
            return smallHeightPercent;
        } else if ((cameraFragment.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
            return largeHeightPercent;
        } else if ((cameraFragment.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            return xLargeHeightPercent;
        } else {
            return defaultHeightPercent;
        }
    }

    /**
     * method to check if a device is rooted or not
     *
     * @return true/false
     */
    protected boolean isDeviceRooted() {
        boolean isRootedDevice;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            isRootedDevice = true;
        } catch (Exception e) {
            isRootedDevice = false;
        } finally {
            if (process != null) {
                try {
                    process.destroy();
                } catch (Exception e) {
                }
            }
        }
        return isRootedDevice;
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void tapToFocus(CameraControl camera) {
        fragmentCameraBinding.previewView.setOnTouchListener((View view, MotionEvent motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                return true;
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                MeteringPointFactory factory = fragmentCameraBinding.previewView.getMeteringPointFactory();
                MeteringPoint point = factory.createPoint(motionEvent.getX(), motionEvent.getY());
                FocusMeteringAction action = new FocusMeteringAction.Builder(point).build();
                camera.startFocusAndMetering(action);
                return true;
            } else {
                return false;
            }
        });
    }

    /**
     * method to set oval ui according defined time if required
     */
    protected void setOvalBorder() {
        try {
            new Handler().postDelayed(() -> {
                try {
                    if (!SingletonData.getInstance().isProcessingStarted() && !cameraFragment.currentState.equals("QL")) {
                        SingletonData.getInstance().getFragmentCameraBinding().faceDetectInst
                                .setText(R.string.position_face_in_oval);
                    }
                } catch (Exception e) {
                    Webhooks.exceptionReport(e, "CameraFragment/HelperFunctions/setOvalBorder/Handler-inner");
                }
            }, TimeConstants.INIT_OVAL_UI_AT_START);
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/HelperFunctions/setOvalBorder");
        }
    }

    /**
     * method to check if user define logo at app end or not
     *
     * @return true/false, drawable exists or not
     */
    protected Boolean isAppLogoExists() {
        try {
            int checkExistence = requestModel.getParentActivity().getResources().getIdentifier(
                    ApiConstants.MERCHANT_APP_LOGO, "drawable", requestModel.getParentActivity().getPackageName());
            // the resource exists
            // checkExistence == 0  // the resource does NOT exist
            return checkExistence != 0;
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/HelperFunctions/checkAppLogoExistence");
            return false;
        }
    }

    protected void setRequestResponse(String requestStatus, double similarityScore) {
        try {
            cameraFragment.requestResponseObj = new HashMap<>();
            cameraFragment.requestResponseObj.put("reference_id", SingletonData.getInstance().getReferenceId());
            cameraFragment.requestResponseObj.put("event", requestStatus);
            if (requestModel.isSimilarity() || (ServiceType.valueOf(requestModel.getConfigObject().getString("serviceType")) ==
                    ServiceType.MATCH_TO_PHOTO_ID && cameraFragment.currentState.equals("docDetection"))) {
                cameraFragment.requestResponseObj.put("similarity_score", String.valueOf(similarityScore));
            }
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/HelperFunctions/setReturnResponse");
        }
    }

    protected void terminateSdkAndReturnResponse() {
        try {
            stopCamera();
            requestModel.getRequestListener().requestStatus(cameraFragment.requestResponseObj);
            SingletonData.getInstance().getActivity().finish();
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/HelperFunctions/terminateSdkAndReturnResponse");
        }
    }

    protected void feedbackScr() {
        try {
//            if (requestModel.getConfigObject().getBoolean("showFeedback")) {
//                stopCamera();
//                Fragment nextFragment;
//                if (SingletonData.getInstance().getReferenceId().equals("")) {
//                    nextFragment = new GreetingFragment(cameraFragment.requestResponseObj);
//                } else {
//                    nextFragment = new FeedbackFragment(requestModel.getToken(), cameraFragment.requestResponseObj);
//                }
//                SingletonData.getInstance().getFragmentManager().beginTransaction()
//                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_from_left)
//                        .replace(R.id.nav_host_fragment, nextFragment, nextFragment.getClass().getSimpleName())
//                        .addToBackStack(null)
//                        .commitAllowingStateLoss();
//            } else if (requestModel.getConfigObject().getBoolean("showGreetings")) {
//                stopCamera();
//                Fragment greetingFragment = new GreetingFragment(cameraFragment.requestResponseObj);
//                SingletonData.getInstance().getFragmentManager().beginTransaction()
//                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_from_left)
//                        .replace(R.id.nav_host_fragment, greetingFragment, greetingFragment.getClass().getSimpleName())
//                        .addToBackStack(null)
//                        .commitAllowingStateLoss();
//            } else {
            SingletonData.getInstance().setQlReqInProcess(false);
            terminateSdkAndReturnResponse();
//            }
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/HelperFunctions/feedbackScr");
        }
    }

    private void dialogAction(Boolean isDialogDismissed) {
        try {
            if (isDialogDismissed) {
                SingletonData.getInstance().setCameraBackPressed(false);
                if (fragmentCameraBinding.docParentLayout.getVisibility() == View.VISIBLE) {
                    cameraFragment.detectCard();
                } else {
                    SingletonData.getInstance().getCameraListeners().frameProcessed();
                }
            } else {
                setRequestResponse("request.cancelled", 0.0f);
                terminateSdkAndReturnResponse();
            }
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/HelperFunctions/dialogAction");
        }
    }

    protected void setQlUi(Boolean isRetry) {
        try {
            cameraFragment.frameCounter = 0;
            SingletonData.getInstance().setQlFrameList(new JsonArray());
            fragmentCameraBinding.cameraInstParentLayout.setVisibility(View.GONE);
            if (isRetry) {
                cameraFragment.initCamera(true);
            }
//            else {
//                showBottomSheet();
//            }
            SingletonData.getInstance().setQuickRequestInProcess(false);
            SingletonData.getInstance().setQlReqInProcess(false);
            SingletonData.getInstance().setSmallOvalSteadyTimerOn(false);
            cameraFragment.recursiveQlCounter = cameraFragment.recursiveQlCounter + 1;
            cameraFragment.currentState = "QL";
            initDlOval();
            fragmentCameraBinding.animationView.loadUrl("");
            cameraFragment.isQuickLiveness = true;
            SingletonData.getInstance().setQuickLiveness(true);
            SingletonData.getInstance().setQuickLivenessFrameCount(0);
            SingletonData.getInstance().setDetectionTimerOn(false);
            SingletonData.getInstance().getCameraListeners().setUi(View.VISIBLE, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE, View.GONE, View.GONE);
//            SingletonData.getInstance().getCameraListeners().setUi(View.VISIBLE, View.GONE, View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE, View.GONE);

//            if (!isRetry) {
//                isBottomSheetVisible = true;
//                new Handler().postDelayed(() -> {
//                    if (isBottomSheetVisible) {
//                        cameraFragment.isCamInstShown = false;
//                        cameraFragment.bottomSheetDialog.cancel();
//                        cameraFragment.frameProcessed();
//                    }
//                }, 5000);
//            }

//            if (requestModel.getConfigObject().getBoolean("qlBottomInstructionSheet")) {
//                showBottomSheet();
//            }else {
            cameraFragment.checkCamInst();
            cameraFragment.frameProcessed();
//            }
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/HelperFunctions/setQlUi");
        }
    }

    protected void setDlUi() {
        try {
            cameraFragment.frameCounter = 0;
            fragmentCameraBinding.cameraInstParentLayout.setVisibility(View.GONE);
            SingletonData.getInstance().setSmallOvalSteadyTimerOn(false);
            SingletonData.getInstance().setQuickRequestInProcess(false);
//            showBottomSheet();
            cameraFragment.currentState = "DL";
            initDlOval();
//            SingletonData.getInstance().getFragmentCameraBinding().faceDetectInst.setText(R.string.position_face_in_oval);
//            fragmentCameraBinding.faceBorder.setBackgroundResource(R.drawable.face_border_bg);
            cameraFragment.isQuickLiveness = false;
            SingletonData.getInstance().setDetectionState("smallOval");
            SingletonData.getInstance().setQuickLiveness(false);
            SingletonData.getInstance().getCameraListeners().setUi(View.VISIBLE, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE, View.GONE, View.GONE);

//            isBottomSheetVisible = true;
//            new Handler().postDelayed(() -> {
//                try {
//                    if (isBottomSheetVisible) {
//                        cameraFragment.isCamInstShown = false;
//                        cameraFragment.bottomSheetDialog.cancel();
//                        cameraFragment.frameProcessed();
//                    }
//                } catch (Exception e) {
//                    Webhooks.exceptionReport(e, "CameraFragment/HelperFunctions/setDlUi-handler");
//                }
//            }, 5000);
            cameraFragment.checkCamInst();
            cameraFragment.frameProcessed();
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/HelperFunctions/setDlUi");
        }
    }

    protected void handleCheckSimilarity(SimilarityApiHelper similarityApiHelper, Boolean isCaptured) {
        try {
            cameraFragment.fileUploadCount = 0;
            SingletonData.getInstance().getCameraListeners().setUi(View.GONE, View.GONE, View.GONE, View.GONE, View.GONE, View.GONE, View.VISIBLE, View.GONE);
            SingletonData.getInstance().getCameraListeners().setAnimationViewsAndText(fragmentCameraBinding.animationViewUpload, "facia_uploading.svg",
                    SingletonData.getInstance().getActivity().getString(R.string.uploading), "", 0.0, false);
            similarityApiHelper.checkSimilarityJsonObject(requestModel.getFaceImage(), requestModel.getIdImage(),
                    requestModel.getSimilarityScore(), isCaptured);
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/HelperFunctions/handleCheckSimilarity");
        }
    }

    protected void stopCamera() {
        try {
            SingletonData.getInstance().setCameraBackPressed(true);
            cameraFragment.storagePath = "";
            SingletonData.getInstance().setIncreasedFrameCount(0);
            SingletonData.getInstance().setSmallOvalSteadyTimerOn(true);
//            SingletonData.getInstance().setResultApiCount(0);
            SingletonData.getInstance().setBlinkCount(0);
            SingletonData.getInstance().setVideoRecording(false);
            SingletonData.getInstance().setOvalSizeIncreased(false);
            SingletonData.getInstance().setSizeIncreasing(false);
            SingletonData.getInstance().setFaceFinalized(false);
            SingletonData.getInstance().setProcessingStarted(false);
            SingletonData.getInstance().setCameraProcessing(false);
            SingletonData.getInstance().setQuickLiveness(true);
            SingletonData.getInstance().setQuickLivenessFrameCount(0);
            //timer
            SingletonData.getInstance().setDetectionTimerOn(false);

            cameraFragment.cameraProvider.unbindAll();
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/HelperFunctions/stopCamera");
        }
    }

    protected void setAdditionalLivenessUi(WebView animationView) {
        try {
            cameraFragment.isQuickLiveness = false;
//            fragmentCameraBinding.faceBorder.setBackgroundResource(R.drawable.face_border_bg);
            SingletonData.getInstance().setQuickLiveness(false);
            if (requestModel.getConfigObject().getBoolean("showAdditionalVerification")) {
                fragmentCameraBinding.resultContinueBtn.setVisibility(View.GONE);
                fragmentCameraBinding.animationView.loadUrl("");
                fragmentCameraBinding.resultTxt.setText(R.string.additional_verification);
                fragmentCameraBinding.resultTxt.setVisibility(View.VISIBLE);
                animationView.loadUrl("file:///android_asset/additional_verification.svg");
                animationView.setBackgroundColor(Color.TRANSPARENT);
                animationView.setVisibility(View.VISIBLE);
                new Handler().postDelayed(() -> showDlAfterQl(animationView), TimeConstants.ADDITIONAL_VERIFICATION_DELAY);
            } else {
                showDlAfterQl(animationView);
            }
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/HelperFunctions/setAdditionalLivenessUi");
        }
    }

    private void showDlAfterQl(WebView animationView) {
        try {
            cameraFragment.initCamera(true);
            animationView.loadUrl("");
            SingletonData.getInstance().setDetectionTimerOn(false);
            setDlUi();
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/HelperFunctions/showDlAfterQl");
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    protected void initAnimationViews(WebView animationView) {
        try {
            fragmentCameraBinding.animationView.setVisibility(View.GONE);
            fragmentCameraBinding.animationViewUpload.setVisibility(View.GONE);
            animationView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            animationView.clearCache(true);
            animationView.getSettings().setLoadWithOverviewMode(true);
            animationView.getSettings().setUseWideViewPort(true);
            animationView.getSettings().setJavaScriptEnabled(true);

            animationView.loadUrl("");

            animationView.setPadding(0, 0, 0, 0);
            animationView.setInitialScale(1);
            animationView.getSettings().setBuiltInZoomControls(false);

        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/HelperFunctions/initAnimationViews");
        }
    }

    protected void setLivenessUi() {
        try {
            switch (FaceLivenessType.valueOf(requestModel.getConfigObject().getString("livenessType"))) {
                case ADDITIONAL_CHECK_LIVENESS:
                    cameraFragment.requestType = "additionalCheckLiveness";
                    setQlUi(false);
                    break;
                case QUICK_LIVENESS_ONLY:
                    cameraFragment.requestType = "quickLivenessOnly";
                    setQlUi(false);
                    break;
                case DETAILED_LIVENESS_ONLY:
                    cameraFragment.requestType = "detailedLivenessOnly";
                    setDlUi();
                    break;
                default:
                    if (isDeviceRooted()) {
                        cameraFragment.requestType = "detailedLivenessOnly";
                        setDlUi();
                    } else {
                        cameraFragment.requestType = "additionalCheckLiveness";
                        cameraFragment.defaultAndNotRooted = true;
                        setQlUi(false);
                    }
                    break;
            }
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/HelperFunctions/setLivenessUi");
        }
    }

    protected void initClickListeners() {
        try {
            fragmentCameraBinding.resultContinueBtn.setOnClickListener(cameraFragment);
            fragmentCameraBinding.imgContinueBtn.setOnClickListener(cameraFragment);
            fragmentCameraBinding.retakeBtn.setOnClickListener(cameraFragment);
            fragmentCameraBinding.captureDocBtn.setOnClickListener(cameraFragment);
            fragmentCameraBinding.instRetryBtn.setOnClickListener(cameraFragment);
            fragmentCameraBinding.captureDocLivenessBtn.setOnClickListener(cameraFragment);
            fragmentCameraBinding.showInstBtn.setOnClickListener(cameraFragment);
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/HelperFunctions/initClickListeners");
        }
    }

    protected void startCountdownTimer() {
//        fragmentCameraBinding.qlCountDownTimer.setVisibility(View.VISIBLE);
        final int startSize = 48; // Starting text size in sp
        final int endSize = 72; // Ending text size in sp
        final long animationDuration = 400; // Animation duration in milliseconds
        countdownTime = 3;
        final Handler handler = new Handler();

        ValueAnimator animator = ValueAnimator.ofFloat(startSize, endSize);
        animator.setDuration(animationDuration);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();
            fragmentCameraBinding.qlCountDownTimer.setTextSize(animatedValue);
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                fragmentCameraBinding.qlCountDownTimer.setText(String.valueOf(countdownTime));
                fragmentCameraBinding.qlCountDownTimer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (countdownTime > 1) {
                    countdownTime--;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            animator.start(); // Start the next animation
                        }
                    }, 1000); // Delay between animations
                } else {
                    fragmentCameraBinding.qlCountDownTimer.setVisibility(View.GONE);
                    SingletonData.getInstance().getCameraListeners().frameProcessed();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animator.start(); // Start the first animation
            }
        }, 500); // Start the countdown after a delay of 1 second
    }

    protected void showResultText(String text, WebView animationView) {
        try {
            fragmentCameraBinding.resultTxt.setText(text);
            fragmentCameraBinding.resultTxt.setVisibility(View.VISIBLE);
            if (animationView.getVisibility() != View.VISIBLE) {
                Webhooks.testingValues("Result scr text shown but animation is not visible.");
            }
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/showResultText");
        }
    }

    protected void loadAnimation(WebView animationView, String animationName) {
        try {
            initAnimationViews(animationView);
            animationView.clearCache(true);
            animationView.loadUrl("file:///android_asset/" + animationName);
            animationView.setBackgroundColor(Color.TRANSPARENT);
            animationView.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/loadAnimation");
        }
    }

    protected void handleCardDetection(Bitmap detectedFrame) {
        Bitmap croppedBitmap = cropFrame(detectedFrame);
        Rect boxRect = getCardBoxRect();
        Rect cardRect = new Rect(SingletonData.getInstance().getCardJson().get("xMin").getAsInt(),
                SingletonData.getInstance().getCardJson().get("yMin").getAsInt(),
                SingletonData.getInstance().getCardJson().get("xMax").getAsInt(),
                SingletonData.getInstance().getCardJson().get("yMax").getAsInt());
        if (boxRect.contains(cardRect)) {
            if (cardRect.width() > boxRect.width() / ThresholdConstants.CARD_BOX_DIVIDER &&
                    cardRect.height() > boxRect.height() / ThresholdConstants.CARD_BOX_DIVIDER) {
                if (!cameraFragment.isDocCaptured) {
                    cameraFragment.convertAndDisplayCapturedCard(croppedBitmap);
                }
            } else {
                cameraFragment.detectCard();
            }
        } else {
            cameraFragment.detectCard();
        }
    }

    protected Bitmap cropFrame(Bitmap bmpToCrop) {
        return Bitmap.createBitmap(bmpToCrop, fragmentCameraBinding.docBorder.getLeft(),
                fragmentCameraBinding.docBorder.getTop(),
                fragmentCameraBinding.docBorder.getWidth(),
                fragmentCameraBinding.docBorder.getHeight());
    }

    /**
     * method to get Rect of oval
     */
    private Rect getCardBoxRect() {
        float left = (fragmentCameraBinding.docBorder.getX() - 25 > 0) ? (fragmentCameraBinding.docBorder.getX() - 25) : 0;
        float right = (fragmentCameraBinding.docBorder.getX() + fragmentCameraBinding.docBorder.getWidth() + 25 <
                Resources.getSystem().getDisplayMetrics().widthPixels) ? (fragmentCameraBinding.docBorder.getX() +
                fragmentCameraBinding.docBorder.getWidth() + 25) : Resources.getSystem().getDisplayMetrics().widthPixels;
        return new Rect((int) left,
                (int) fragmentCameraBinding.docBorder.getY() - 25, (int) right,
                (int) fragmentCameraBinding.docBorder.getY() +
                        fragmentCameraBinding.docBorder.getHeight() + 25);
    }

    protected void showResult(WebView animationView, String animationName, String text, String requestStatus, double similarityScore) {
        try {
            //stop camera processing, set response for the callback
            stopCamera();
            setRequestResponse(requestStatus, similarityScore);
            //show result
            //after delay, move to feedback or greetings
            //else show result button
            loadAnimation(animationView, animationName);
            showResultText(text, animationView);
//            if (requestModel.getConfigObject().getBoolean("showFeedback") &&
//                    !SingletonData.getInstance().getReferenceId().equals("")) {
//                new Handler().postDelayed(this::showFeedbackFragment, TimeConstants.REMOVE_RESULT_SCREEN);
//            } else if (requestModel.getConfigObject().getBoolean("showGreetings")) {
//                new Handler().postDelayed(this::showGreetingsFragment, TimeConstants.REMOVE_RESULT_SCREEN);
//            } else {
            //show continue button
            fragmentCameraBinding.resultContinueBtn.setVisibility(View.VISIBLE);
//            }


        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/showResult");
        }
    }

    protected void notToShowResult(String requestStatus, double similarityScore) {
        try {
            //stop camera processing, set response for the callback
            stopCamera();
            setRequestResponse(requestStatus, similarityScore);
            //show feedback or greetings or exit the sdk
//            if (requestModel.getConfigObject().getBoolean("showFeedback") &&
//                    !SingletonData.getInstance().getReferenceId().equals("")) {
//                showFeedbackFragment();
//            } else if (requestModel.getConfigObject().getBoolean("showGreetings")) {
//                showGreetingsFragment();
//            } else {
            //exit SDK
            terminateSdkAndReturnResponse();
//            }
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/notToShowResult");
        }
    }

    private void showFeedbackFragment() {
        try {
            Fragment feedbackFragment = new FeedbackFragment(requestModel.getToken(), cameraFragment.requestResponseObj);
            SingletonData.getInstance().getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_from_left)
                    .replace(R.id.nav_host_fragment, feedbackFragment, feedbackFragment.getClass().getSimpleName())
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/showFeedbackFragment");
        }
    }

    private void showGreetingsFragment() {
        try {
            Fragment greetingFragment = new GreetingFragment(cameraFragment.requestResponseObj);
            SingletonData.getInstance().getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_from_left)
                    .replace(R.id.nav_host_fragment, greetingFragment, greetingFragment.getClass().getSimpleName())
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/showGreetingsFragment");
        }
    }

    /**
     * initialising instruction's bottom sheet
     */
    protected void initialiseBottomSheet() {
        try {
            cameraFragment.bottomSheetDialog = new BottomSheetDialog(SingletonData.getInstance().getActivity(),
                    R.style.BottomSheetDialogTheme);
            cameraFragment.bottomSheetView = LayoutInflater.from(SingletonData.getInstance().getActivity()).inflate(
                    R.layout.instructions_bottom_sheet, null);
            cameraFragment.bottomSheetDialog.setContentView(cameraFragment.bottomSheetView);
            cameraFragment.bottomSheetDialog.setCanceledOnTouchOutside(true);

            ConstraintLayout constraintLayout1 = cameraFragment.bottomSheetDialog.findViewById(R.id.topMainLayout);
            ConstraintLayout constraintLayout2 = cameraFragment.bottomSheetDialog.findViewById(R.id.midMainLayout);
            ConstraintLayout constraintLayout3 = cameraFragment.bottomSheetDialog.findViewById(R.id.bottomMainLayout);
            ImageView crossIcon = cameraFragment.bottomSheetDialog.findViewById(R.id.crossImg);
            int height = (Resources.getSystem().getDisplayMetrics().heightPixels - 200) / 3;
            constraintLayout1.setMinHeight(height);
            constraintLayout2.setMinHeight(height);
            constraintLayout3.setMinHeight(height);

            try {
                crossIcon.setOnClickListener(view -> {
                    try {
                        cameraFragment.isCamInstShown = false;
                        cameraFragment.bottomSheetDialog.cancel();
                        cameraFragment.frameProcessed();
                    } catch (Exception e) {
                        Webhooks.exceptionReport(e, "Camera/HelperFunctions/initialiseBottomSheet-crossIcon-inner");
                    }
                });
            } catch (Exception e) {
                Webhooks.exceptionReport(e, "Camera/HelperFunctions/initialiseBottomSheet-crossIcon");
            }

            ConstraintLayout.LayoutParams lp1 = (ConstraintLayout.LayoutParams) constraintLayout1.getLayoutParams();
            ConstraintLayout.LayoutParams lp2 = (ConstraintLayout.LayoutParams) constraintLayout2.getLayoutParams();
            ConstraintLayout.LayoutParams lp3 = (ConstraintLayout.LayoutParams) constraintLayout3.getLayoutParams();
            lp1.height = height;
            constraintLayout1.setLayoutParams(lp1);
            lp2.height = height;
            constraintLayout2.setLayoutParams(lp2);
            lp3.height = height;
            constraintLayout3.setLayoutParams(lp3);

            cameraFragment.bottomSheetDialog.setOnDismissListener(dialog -> {
                isBottomSheetVisible = false;
                cameraFragment.isCamInstShown = false;
                cameraFragment.frameProcessed();
            });
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/initialiseBottomSheet");
        }
    }

    /**
     * displaying instruction's bottom sheet
     */
    protected void showBottomSheet() {
        try {
            BottomSheetBehavior<View> behavior;
            behavior = BottomSheetBehavior.from((View) cameraFragment.bottomSheetView.getParent());
            behavior.setDraggable(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            //To make sure bottom sheet doesn't have collapsed state
            behavior.setSkipCollapsed(true);
            //set minimum height to parent view
            ConstraintLayout layout = cameraFragment.bottomSheetDialog.findViewById(R.id.instructionsBottomSheet);
            assert layout != null;
            layout.setMinimumHeight(Resources.getSystem().getDisplayMetrics().heightPixels - 200);

            cameraFragment.bottomSheetDialog.show();
            cameraFragment.isCamInstShown = true;
//            new Handler().postDelayed(() -> cameraFragment.bottomSheetDialog.cancel(), TimeConstants.BOTTOM_SHEET_DIALOG_HIDE_TIME);
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/HelperFunctions/showBottomSheet");
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    protected void showOvalTickAnimation() {
        try {
//            animationView.clearCache(true);
            fragmentCameraBinding.ovalTickAnimation.getSettings().setJavaScriptEnabled(true);
            fragmentCameraBinding.ovalTickAnimation.loadUrl("file:///android_asset/facia_success.svg");
            fragmentCameraBinding.ovalTickAnimation.setBackgroundColor(Color.TRANSPARENT);
            fragmentCameraBinding.ovalTickAnimation.setVisibility(View.VISIBLE);
//            animationView.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/HelperFunctions/showOvalTickAnimation");
        }
    }
}
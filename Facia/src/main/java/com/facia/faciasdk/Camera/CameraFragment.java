package com.facia.faciasdk.Camera;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaActionSound;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
<<<<<<< HEAD
=======
import android.util.Size;
>>>>>>> origin/main
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.FallbackStrategy;
import androidx.camera.video.FileOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
<<<<<<< HEAD
=======
import androidx.camera.view.PreviewView;
>>>>>>> origin/main
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.facia.faciasdk.Activity.Helpers.Enums.DocumentType;
import com.facia.faciasdk.Activity.Helpers.Enums.FaceDetectionThreshold;
import com.facia.faciasdk.Activity.Helpers.Enums.ServiceType;
import com.facia.faciasdk.Activity.Helpers.IntentHelper;
import com.facia.faciasdk.Activity.Helpers.Enums.FaceLivenessType;
import com.facia.faciasdk.Activity.Helpers.Enums.OvalSize;
import com.facia.faciasdk.Activity.Helpers.RequestModel;
<<<<<<< HEAD
import com.facia.faciasdk.BuildConfig;
=======
>>>>>>> origin/main
import com.facia.faciasdk.Camera.ApiHelpers.LivenessApiHelper;
import com.facia.faciasdk.Camera.ApiHelpers.SimilarityApiHelper;
import com.facia.faciasdk.Camera.CameraXHelpers.CameraListeners;
import com.facia.faciasdk.Camera.CameraXHelpers.CameraXViewModel;
import com.facia.faciasdk.Camera.CardDetection.CardDetectionTflite;
import com.facia.faciasdk.Camera.FaceDetection.FaceDetectionHelper;
import com.facia.faciasdk.DocumentType.DocTypeFragment;
import com.facia.faciasdk.Logs.Webhooks;
import com.facia.faciasdk.R;
import com.facia.faciasdk.Singleton.SingletonData;
import com.facia.faciasdk.Utils.Constants.ApiConstants;
import com.facia.faciasdk.Utils.Constants.TimeConstants;
import com.facia.faciasdk.databinding.FragmentCameraBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraFragment extends Fragment implements CameraListeners, View.OnClickListener {
    private final Handler docBtnHandler = new Handler();
    @Nullable
    protected ProcessCameraProvider cameraProvider;
    @Nullable
    protected String storagePath = "";
    protected Boolean isQuickLiveness = true, defaultAndNotRooted = false, isCamInstShown = false, isBgChanging = false,
            backPressed = false, isCardDetectionInProcess = false, isCamStopped = false, isDocCaptured = false;
    protected int fileUploadCount = 0;
    protected Dialog dialog;
    protected HashMap<String, String> requestResponseObj;
    protected int recursiveQlCounter = 0, frameCounter = 0;
    protected String requestType = "", currentState = "";
    protected BottomSheetDialog bottomSheetDialog;
    protected View bottomSheetView;
    @Nullable
    protected Preview previewUseCase;
    private long qlListCurrent, qlListPrevious;
    private FaceDetectionHelper faceDetectionHelper;
<<<<<<< HEAD
    private Bitmap detectedFaceFrame;
=======
//    private Bitmap detectedFaceFrame;
>>>>>>> origin/main
    private FragmentCameraBinding fragmentCameraBinding;
    private final Runnable docBtnRunnable = () -> {
        if (fragmentCameraBinding.imagePreviewParentLayout.getVisibility() != View.VISIBLE) {
            fragmentCameraBinding.captureDocBtn.setVisibility(View.VISIBLE);
        }
    };
    private VideoCapture<Recorder> videoCapture;
    private Recording currentRecording;
    private CameraSelector cameraSelector;
    private File videoFile;
    private LivenessApiHelper livenessApiHelper;
    private SimilarityApiHelper similarityApiHelper;
    private RequestModel requestModel;
    private HelperFunctions helperFunctions;
    private List<Bitmap> framesList = new ArrayList<>();
    /**
     * method to handle screen's back press click
     * resetting values
     * unbinding camera
     * calling previous screen
     */
    private final OnBackPressedCallback backPressCallBack = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            try {
                if (fragmentCameraBinding.cameraParentLayout.getVisibility() == View.VISIBLE) {
                    if (ServiceType.valueOf(requestModel.getConfigObject().getString("serviceType")) == ServiceType.DOCUMENT_LIVENESS &&
                            requestModel.getConfigObject().getBoolean("showDocumentType")) {
                        showDocTypeFragment();
                    } else if (fragmentCameraBinding.ovalTickAnimation.getVisibility() != View.VISIBLE &&
                            !fragmentCameraBinding.faceDetectInst.getText().equals(R.string.perfect)){
                        helperFunctions.exitDialog();
                    }
                }
            } catch (Exception e) {
                Webhooks.exceptionReport(e, "CameraFragment/handleOnBackPressed");
            }
        }
    };
    private CardDetectionTflite cardDetectionTflite;
    /**
     * CaptureEvent listener
     * on start recording and stop, will set a variable
     * on stop recording, will check if processing is completed
     * then it will unbind camera and will call next screen to upload & show result
     */
    private final Consumer<VideoRecordEvent> videoCallback = new Consumer<androidx.camera.video.VideoRecordEvent>() {
        @Override
        public void accept(androidx.camera.video.VideoRecordEvent videoRecordEvent) {
            try {
                if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                    SingletonData.getInstance().setVideoRecording(true);
                    if (SingletonData.getInstance().isFaceFinalized()) {
                        Webhooks.testingValues("CameraFragment/videoCallback-Video STARTED, while face is detected.");
                    }
                } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                    SingletonData.getInstance().setVideoRecording(false);
                    if (SingletonData.getInstance().isFaceFinalized()) {
                        SingletonData.getInstance().setFaceFinalized(false);
<<<<<<< HEAD
                        initRequests(storagePath);
=======
                        initRequests(storagePath, null);
>>>>>>> origin/main
                        try {
                            cameraProvider.unbindAll();
                        } catch (Exception e) {
                            Webhooks.exceptionReport(e, "CameraFragment/videoCallback-inner");
                        }
                    } else {
//                        if (currentState.equals("QL")) {
//                            SingletonData.getInstance().setQlVideoPath(storagePath);
//                        }else {
                        videoFile.delete();
//                        }
                    }
                }
            } catch (Exception e) {
                Webhooks.exceptionReport(e, "CameraFragment/videoCallback");
            }
        }
    };

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentCameraBinding = FragmentCameraBinding.inflate(inflater, container, false);
        initialization();
        return fragmentCameraBinding.getRoot();
    }

    /**
     * Method to initialize variables
     * and setting camera related values
     * invoking further method to bind camera use cases
     */
    @SuppressLint("RestrictedApi")
    private void initialization() {
        try {
            requestModel = (RequestModel) IntentHelper.getInstance().getObject(ApiConstants.REQUEST_MODEL);
            SingletonData.getInstance().setDetectionTimerOn(false);
            dialog = new Dialog(SingletonData.getInstance().getContext());
            SingletonData.getInstance().setFragmentCameraBinding(fragmentCameraBinding);
            SingletonData.getInstance().setCameraListeners(this);
<<<<<<< HEAD
=======
            SingletonData.getInstance().setQlReqInProcess(false);
>>>>>>> origin/main
            SingletonData.getInstance().setCameraBackPressed(false);
            initHelperClasses();
            if (requestModel.isSimilarity()) {
                requestType = "checkSimilarity";
                currentState = "similarityRequest";
                helperFunctions.handleCheckSimilarity(similarityApiHelper, false);
            } else {
                if (ServiceType.valueOf(requestModel.getConfigObject().getString("serviceType")) == ServiceType.MATCH_TO_PHOTO_ID) {
                    requestType = "photoIdMatch";
                    helperFunctions.setQlUi(false);
                    initCamera(true);
                } else if (ServiceType.valueOf(requestModel.getConfigObject().getString("serviceType")) == ServiceType.DOCUMENT_LIVENESS) {
                    if (DocumentType.valueOf(requestModel.getConfigObject().getString("documentType")) == DocumentType.ID_CARD) {
                        matchIdByCapture();
                    } else {
                        currentState = "docDetection";
                        initCamera(false);
                        setUi(View.VISIBLE, View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE);
                    }
                } else {
                    helperFunctions.setLivenessUi();
                    initCamera(true);
                }
            }
            helperFunctions.initClickListeners();
            requireActivity().getOnBackPressedDispatcher().addCallback(this.requireActivity(), backPressCallBack);
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/initialization");
        }
    }

    protected void checkCamInst() {
        try {
            new Handler().postDelayed(() -> {
                try {
                    if (fragmentCameraBinding.cameraInstParentLayout.getVisibility() == View.GONE) {
                        Webhooks.testingValues("Inst issue found: " +
                                SingletonData.getInstance().isQuickRequestInProcess() + " : " +
                                SingletonData.getInstance().isCameraBackPressed() + " : " +
                                isCamInstShown + " : " + dialog.isShowing() + " : " +
                                (fragmentCameraBinding.previewView.getBitmap() == null));
                    }
                } catch (Exception e) {
                    Webhooks.exceptionReport(e, "CameraFragment/checkCamInst/Handler");
                }
            }, 1500);
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/checkCamInst-inner");
        }
    }

    private void initHelperClasses() {
        try {
            if (requestModel.isSimilarity()) {
                similarityApiHelper = new SimilarityApiHelper(fragmentCameraBinding, requestModel.getToken());
            } else {
                if (ServiceType.valueOf(requestModel.getConfigObject().getString("serviceType")) == ServiceType.MATCH_TO_PHOTO_ID) {
                    similarityApiHelper = new SimilarityApiHelper(fragmentCameraBinding, requestModel.getToken());
                }
                livenessApiHelper = new LivenessApiHelper(fragmentCameraBinding, requestModel.getToken());
            }
            helperFunctions = new HelperFunctions(fragmentCameraBinding, this, requestModel);
            faceDetectionHelper = new FaceDetectionHelper(SingletonData.getInstance().getContext(),
                    requestModel.getConfigObject().getBoolean("dlEyesBlinkDetectionTimeout"),
                    FaceDetectionThreshold.valueOf(requestModel.getConfigObject().getString("faceDetectionThreshold")),
                    OvalSize.valueOf(requestModel.getConfigObject().getString("ovalSize")));
            helperFunctions.initialiseBottomSheet();
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/initHelperClasses");
        }
    }

    private void showDocTypeFragment() {
        try {
            backPressed = true;
            helperFunctions.stopCamera();
            Fragment docTypeFragment = new DocTypeFragment();
            SingletonData.getInstance().getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_from_left)
                    .replace(R.id.nav_host_fragment, docTypeFragment, docTypeFragment.getClass().getSimpleName())
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/showDocTypeFragment");
        }
    }

    protected void initCamera(Boolean isLiveness) {
        try {
            cameraSelector = new CameraSelector.Builder().requireLensFacing(isLiveness ?
                    CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK).build();
            new ViewModelProvider(this, (ViewModelProvider.Factory)
                    ViewModelProvider.AndroidViewModelFactory.getInstance(SingletonData.getInstance().getApplication()))
                    .get(CameraXViewModel.class)
                    .getProcessCameraProvider()
                    .observe(getViewLifecycleOwner(),
                            provider -> {
                                cameraProvider = provider;
                                bindAllCameraUseCases(isLiveness);
                            });
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/initCamera");
        }
    }

    /**
     * method to init binding camera use cases
     * will check camera provider, will unbind previous use cases
     * and will call further method to bind camera use cases
     */
    private void bindAllCameraUseCases(Boolean isLiveness) {
        try {
            if (cameraProvider != null) {
                cameraProvider.unbindAll();
                if (cameraProvider == null) {
                    return;
                }
                if (previewUseCase != null) {
                    cameraProvider.unbind(previewUseCase);
                }
                bindCameraUseCases(isLiveness);
            }
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/bindAllCameraUseCases");
        }
    }

    /**
     * method to bind camera use cases
     * will get camera provider instance
     * and will bind preview and video use case
     * invoking further method to set FaceMesh detection
     */
    private void bindCameraUseCases(Boolean isLiveness) {
        try {
            Preview.Builder builder = new Preview.Builder();
<<<<<<< HEAD
            previewUseCase = builder.build();
            previewUseCase.setSurfaceProvider(fragmentCameraBinding.previewView.getSurfaceProvider());
            Recorder recorder = new Recorder.Builder()
=======
//            @SuppressLint("RestrictedApi") Preview.Builder builder = new Preview.Builder().setMaxResolution(new Size(1920,1080));
            previewUseCase = builder.build();
            previewUseCase.setSurfaceProvider(fragmentCameraBinding.previewView.getSurfaceProvider());
            Recorder recorder = new Recorder.Builder()
//                    .setQualitySelector(QualitySelector.from(Quality.HIGHEST, FallbackStrategy.higherQualityOrLowerThan(Quality.HIGHEST))).build();
>>>>>>> origin/main
                    .setQualitySelector(QualitySelector.from(Quality.SD, FallbackStrategy.higherQualityOrLowerThan(Quality.SD))).build();
            videoCapture = VideoCapture.withOutput(recorder);
            new ViewModelProvider(this, (ViewModelProvider.Factory)
                    ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication()))
                    .get(CameraXViewModel.class)
                    .getProcessCameraProvider()
                    .observe(
                            getViewLifecycleOwner(),
                            provider -> {
                                cameraProvider = provider;
                                try {
                                    cameraProvider.unbindAll();
                                    try {
                                        Camera cameraX = cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector,
                                                previewUseCase, videoCapture);
                                        helperFunctions.tapToFocus(cameraX.getCameraControl());
                                    } catch (Exception e) {
                                        Webhooks.exceptionReport(e, "CameraFragment/bindCameraUseCases/ViewModelProvider-inner");
                                    }
                                } catch (Exception e) {
                                    Webhooks.exceptionReport(e, "CameraFragment/bindCameraUseCases/ViewModelProvider-inner");
                                }
                            });
            if (isLiveness) {
//                helperFunctions.setOvalBorder();
            } else if (ServiceType.valueOf(requestModel.getConfigObject().getString("serviceType")) == ServiceType.MATCH_TO_PHOTO_ID ||
                    ServiceType.valueOf(requestModel.getConfigObject().getString("serviceType")) == ServiceType.DOCUMENT_LIVENESS &&
                            DocumentType.valueOf(requestModel.getConfigObject().getString("documentType")) == DocumentType.ID_CARD) {
<<<<<<< HEAD
=======
                new TestPreview().execute();//android 14
>>>>>>> origin/main
                SingletonData.getInstance().getActivity().runOnUiThread(() ->
                        new Handler().postDelayed(() -> detectCard(), 1000));
            }
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/bindCameraUseCases");
        }
    }

    /**
     * callback to process next frame
     */
    @Override
    public void frameProcessed() {
        try {
            if (!SingletonData.getInstance().isQuickRequestInProcess() && !SingletonData.getInstance().isCameraBackPressed()
                    && !isCamInstShown) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        if (!dialog.isShowing()) {
                            SingletonData.getInstance().setCameraProcessing(true);
                            Bitmap bitmap;
                            if (framesList.size() == 0) {
                                bitmap = fragmentCameraBinding.previewView.getBitmap();
                            } else {
                                bitmap = framesList.get(framesList.size() - 1);
                                framesList.remove(framesList.size() - 1);
                            }
                            frameCounter++;
                            if (bitmap != null && frameCounter > 1) {
                                try {
<<<<<<< HEAD
                                    if (Color.alpha(bitmap.getPixel(0, 0)) != 0 &&
                                            fragmentCameraBinding.cameraInstParentLayout.getVisibility() == View.GONE) {
=======
                                    if ((Color.alpha(bitmap.getPixel(0, 0)) != 0 || fragmentCameraBinding.previewView.getPreviewStreamState()
                                            .getValue().toString().equalsIgnoreCase("STREAMING"))  &&
                                            fragmentCameraBinding.cameraInstParentLayout.getVisibility() == View.GONE) {
                                        fragmentCameraBinding.hidePreviewView.setVisibility(View.GONE);//android 14
>>>>>>> origin/main
                                        fragmentCameraBinding.cameraInstParentLayout.setVisibility(View.VISIBLE);
                                    }
                                } catch (Exception e) {
                                    try {
                                        Webhooks.exceptionReport(e, "Camera/frameProcessed-inner1");
                                        new Handler().postDelayed(() -> fragmentCameraBinding.cameraInstParentLayout.setVisibility(View.VISIBLE),
                                                500);
                                    } catch (Exception ex) {
                                        Webhooks.exceptionReport(e, "Camera/frameProcessed-inner2");
                                        fragmentCameraBinding.cameraInstParentLayout.setVisibility(View.VISIBLE);
                                    }
                                }

<<<<<<< HEAD
                                if (SingletonData.getInstance().isQuickLiveness()) {
                                    detectedFaceFrame = bitmap;
                                }
=======
//                                if (SingletonData.getInstance().isQuickLiveness()) {
//                                    detectedFaceFrame = bitmap;
//                                }
>>>>>>> origin/main
                                detectFaceInFrame(bitmap);
                            } else {
                                frameProcessed();
                            }
                        }
                    } catch (Exception e) {
                        Webhooks.exceptionReport(e, "CameraFragment/frameProcessed/Handler");
                        frameProcessed();
                    }
                });
            }
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/frameProcessed");
            frameProcessed();
        }
    }

    private void detectFace(Bitmap frame) {
        try {
            InputImage image = InputImage.fromBitmap(frame, 0);
            FaceDetectorOptions faceDetectorOptions =
                    new FaceDetectorOptions.Builder()
                            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                            .build();
            FaceDetector detector = FaceDetection.getClient(faceDetectorOptions);
            detector.process(image)
                    .addOnSuccessListener(
                            faces -> {
                                try {
                                    if (faces.size() > 0) {
                                        int index;
                                        if (faces.size() > 1) {
                                            index = 0;
                                            int height = faces.get(0).getBoundingBox().height();
                                            for (int i = 1; i < faces.size(); i++) {
                                                if (faces.get(i).getBoundingBox().height() > height) {
                                                    height = faces.get(i).getBoundingBox().height();
                                                    index = i;
                                                }
                                            }
                                        } else {
                                            index = 0;
                                        }
<<<<<<< HEAD
                                        faceDetectionHelper.setFaceMeshResult(faces.get(index));
                                    } else {
                                        faceDetectionHelper.setFaceMeshResult(null);
=======
                                        faceDetectionHelper.setFaceMeshResult(faces.get(index), frame);
                                    } else {
                                        faceDetectionHelper.setFaceMeshResult(null, frame);
>>>>>>> origin/main
                                    }
                                } catch (Exception e) {
                                    Webhooks.exceptionReport(e, "CameraFragment/detectFaceInFrame/faceCallback");
                                }
                            })
                    .addOnFailureListener(
                            e -> {
                                Webhooks.testingValues("Face detection failure: " + e.getMessage());
<<<<<<< HEAD
                                faceDetectionHelper.setFaceMeshResult(null);
=======
                                faceDetectionHelper.setFaceMeshResult(null, frame);
>>>>>>> origin/main
                            });
            framesList.add(fragmentCameraBinding.previewView.getBitmap());
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/detectFace");
        }
    }

    /**
     * method to detect faces in a frame
     * handle QL & DL timeout
     * handling further flow
     *
     * @param frame single frame from camera stream
     */
    private void detectFaceInFrame(Bitmap frame) {
        try {
            //timer
            if (SingletonData.getInstance().isQuickLiveness() ? requestModel.getConfigObject().getBoolean("qlFaceDetectionTimeout") :
                    requestModel.getConfigObject().getBoolean("dlFaceDetectionTimeout")) {
                if (SingletonData.getInstance().getDetectionState().equals("smallOval")) {
                    SingletonData.getInstance().setCurrent(System.currentTimeMillis());
                    if (!SingletonData.getInstance().isDetectionTimerOn()) {
                        SingletonData.getInstance().setDetectionTimerOn(true);
                        SingletonData.getInstance().setPrevious(System.currentTimeMillis());
                        detectFace(frame);
                    } else if (SingletonData.getInstance().getCurrent() - SingletonData.getInstance().getPrevious() >=
                            TimeConstants.QL_AND_DL_TIMEOUT) {
                        SingletonData.getInstance().getCameraListeners().cameraTimeOut();
                    } else {
                        detectFace(frame);
                    }
                } else if (SingletonData.getInstance().getDetectionState().equals("bigOval")) {
                    SingletonData.getInstance().setCurrent(System.currentTimeMillis());
                    if (!SingletonData.getInstance().isDetectionTimerOn()) {
                        SingletonData.getInstance().setDetectionTimerOn(true);
                        SingletonData.getInstance().setPrevious(System.currentTimeMillis());
                        detectFace(frame);
                    } else if (SingletonData.getInstance().getCurrent() - SingletonData.getInstance().getPrevious() >=
                            TimeConstants.QL_AND_DL_TIMEOUT) {
                        SingletonData.getInstance().getCameraListeners().cameraTimeOut();
                    } else {
                        detectFace(frame);
                    }
                } else {
                    detectFace(frame);
                }
            } else {
                detectFace(frame);
            }
            //timer
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/detectFaceInFrame");
        }
    }

    /**
     * method will be called whenever activity will be resumed
     * if camera was working and activity was paused and after that activity resumed
     * it will restart frame processing
     */
    @Override
    public void onResume() {
        super.onResume();
        isCamStopped = false;
        if (SingletonData.getInstance().isCameraProcessing()) {
            frameProcessed();
<<<<<<< HEAD
        } else if (isCardDetectionInProcess) {
=======
        } else if (isCardDetectionInProcess || fragmentCameraBinding.docParentLayout.getVisibility() == View.VISIBLE) {
>>>>>>> origin/main
            detectCard();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        isCamStopped = true;
    }

    /**
     * callback to start video recording
     */
    @Override
    public void startVideoRecording() {
        startRecording();
    }

    /**
     * callback to stop video recording
     */
    @SuppressLint("RestrictedApi")
    @Override
    public void stopVideoRecording() {
        try {
            currentRecording.stop();
        } catch (Exception e) {
            if (SingletonData.getInstance().isFaceFinalized()) {
                Webhooks.exceptionReport(e, "CameraFragment/stopVideoRecording-catch");
            }
        }
    }

    /**
     * method to start video recording
     * creating temp file in cache
     * saving video to that file's path
     */
    @SuppressLint({"MissingPermission", "RestrictedApi"})
    private void startRecording() {
        try {
            long timeStamp = System.currentTimeMillis();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timeStamp);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");

            File directory = requireContext().getCacheDir();
            videoFile = null;
            try {
                videoFile = File.createTempFile(
                        "facia_recording",
                        ".mp4",
                        directory
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
            storagePath = videoFile.getPath();
            FileOutputOptions fileOutputOptions = new FileOutputOptions.Builder(videoFile).build();
            currentRecording = videoCapture.getOutput().prepareRecording(requireActivity(), fileOutputOptions).start(
//                    getExecutor(), videoCallback);
                    getExecutor(), videoCallback);
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/startRecording");
        }
    }

    /**
     * method to return main executor
     *
     * @return main executor
     */
    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(SingletonData.getInstance().getContext());
    }

    @Override
<<<<<<< HEAD
    public void processQuickLiveness() {
//        stopVideoRecording();
        fragmentCameraBinding.showInstBtn.setEnabled(false);
=======
    public void processQuickLiveness(Bitmap bitmap) {
//        stopVideoRecording();
        fragmentCameraBinding.showInstBtn.setEnabled(false);
        SingletonData.getInstance().setQlReqInProcess(true);
>>>>>>> origin/main
        SingletonData.getInstance().setQuickRequestInProcess(true);
        try {
            cameraProvider.unbindAll();

//            cameraProvider.unbind(previewUseCase);
//            cameraProvider = null;
//            fragmentCameraBinding.previewView.removeAllViews();
//            fragmentCameraBinding.previewView.setBackgroundColor(Color.TRANSPARENT);
////            fragmentCameraBinding.previewView.setAlpha(0);
//            previewUseCase.setSurfaceProvider(null);
//            previewUseCase = new Preview.Builder().build();
//            fragmentCameraBinding.previewView.destroyDrawingCache();
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/processQuickLiveness-inner");
        }
<<<<<<< HEAD
        SingletonData.getInstance().getActivity().runOnUiThread(() -> initRequests(""));
=======
        SingletonData.getInstance().getActivity().runOnUiThread(() -> initRequests("", bitmap));
>>>>>>> origin/main
    }

    @Override
    public void cameraTimeOut() {
        try {
            setUi(View.GONE, View.GONE, View.GONE, View.GONE, View.GONE, View.GONE, View.VISIBLE, View.GONE);
            SingletonData.getInstance().setDetectionTimerOn(false);
            SingletonData.getInstance().setCameraBackPressed(true);
            cameraProvider.unbindAll();
            SingletonData.getInstance().setReferenceId("");
            setAnimationViewsAndText(fragmentCameraBinding.animationView, "facia_failure.svg",
                    SingletonData.getInstance().getActivity().getString(R.string.request_timed_out), "request.timeout", 0.0, true);
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/cameraTimeOut");
        }
    }

    /**
     * method to initialize instances & values
     * invoking further method to set animation views
     * handling click of continue button (exiting from sdk)
     */
<<<<<<< HEAD
    private void initRequests(String filePath) {
=======
    private void initRequests(String filePath, Bitmap bitmap) {
>>>>>>> origin/main
        try {
            isCardDetectionInProcess = false;
            isCamStopped = false;
            SingletonData.getInstance().setCameraProcessing(false);
            SingletonData.getInstance().setVideoRecording(false);
            helperFunctions.showOvalTickAnimation();
            new Handler().postDelayed(() -> {
                try {
                    if (!backPressed) {
                        try {
                            fragmentCameraBinding.showInstBtn.setEnabled(true);
                            isCamInstShown = false;
                            bottomSheetDialog.cancel();

<<<<<<< HEAD
                            fragmentCameraBinding.previewView.removeAllViews();
=======
//                            fragmentCameraBinding.previewView.removeAllViews();//android 14
                            fragmentCameraBinding.hidePreviewView.setVisibility(View.VISIBLE);//android 14
>>>>>>> origin/main

//                        cameraProvider.unbind(previewUseCase);
//                        cameraProvider = null;
//                        fragmentCameraBinding.cameraInstParentLayout.setVisibility(View.GONE);
//                        fragmentCameraBinding.previewView.removeAllViews();
//                        fragmentCameraBinding.previewView.setBackgroundColor(Color.TRANSPARENT);
//                        fragmentCameraBinding.previewView.setAlpha(0);
//                        previewUseCase.setSurfaceProvider(null);
//                        previewUseCase = new Preview.Builder().build();
//                        fragmentCameraBinding.previewView.destroyDrawingCache();
                        } catch (Exception e) {
                            Webhooks.exceptionReport(e, "CameraFragment/initRequests/Handler/!backPressed");
                        }

                        fragmentCameraBinding.ovalTickAnimation.setVisibility(View.GONE);
                        setUi(View.GONE, View.GONE, View.GONE, View.GONE, View.GONE, View.GONE, View.VISIBLE, View.GONE);
                        if (SingletonData.getInstance().isQuickLiveness()) {
                            setAnimationViewsAndText(fragmentCameraBinding.animationView, "facia_loader.svg", "", "", 0.0, false);
<<<<<<< HEAD
                            new ConvertBitmapToFileAndProcess(null).execute();
=======
                            new ConvertBitmapToFileAndProcess(bitmap, true).execute();
>>>>>>> origin/main
                        } else {
                            setAnimationViewsAndText(fragmentCameraBinding.animationViewUpload, "facia_uploading.svg",
                                    SingletonData.getInstance().getActivity().getString(R.string.uploading), "", 0.0, false);
                            if (FaceLivenessType.valueOf(requestModel.getConfigObject().getString("livenessType")) ==
                                    FaceLivenessType.ADDITIONAL_CHECK_LIVENESS && !isQuickLiveness) {
                                livenessApiHelper.livenessRequestObject(new File(filePath));
                            } else {
                                livenessApiHelper.createRequestJsonObject(new File(filePath), "dl", currentState);
                            }
                        }
                    }
                } catch (JSONException e) {
                    Webhooks.exceptionReport(e, "CameraFragment/initRequests/Handler");
                }
            }, 1400);
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "ResultFragment/initRequests");
        }
    }

    private File convertBitmapToFile(Bitmap bmp, Boolean isDocDetected) {
        File file = null;
        try {
            file = new File(SingletonData.getInstance().getActivity().getCacheDir(),
                    isDocDetected ? "detectedDoc.jpeg" : "detectedFace.jpeg");
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 70 /*ignored for PNG*/, bos);
            byte[] bitmapData = bos.toByteArray();
            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bitmapData);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/convertBitmapToFile");
        }
        return file;
    }

    @Override
    public void fileUploaded() {
        try {
            fileUploadCount = fileUploadCount + 1;
            if (fileUploadCount == 2) {
                setAnimationViewsAndText(fragmentCameraBinding.animationViewUpload, "facia_loader.svg",
                        "", "", 0.0, false);
            }
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/fileUploaded");
        }
    }

    /**
     * method to set UI
     */
    @Override
    public void setUi(int cameraLayoutVisibility, int faceLayoutVisibility, int matchIdLayoutVisibility,
                      int docLivenessLayoutVisibility, int quickLivenessInstVisibility, int qlResultInstructionScrVisibility, int resultLayoutVisibility, int imgPreviewLayoutVisibility) {
        try {
            SingletonData.getInstance().getActivity().runOnUiThread(() -> {
                try {
//                    if (currentState.equals("QL")){
//                        fragmentCameraBinding.showInstBtn.setVisibility(View.GONE);
//                    }else if (currentState.equals("DL")){
                    fragmentCameraBinding.showInstBtn.setVisibility(View.VISIBLE);
//                    }
                    fragmentCameraBinding.cameraParentLayout.setVisibility(cameraLayoutVisibility);
                    fragmentCameraBinding.faceParentLayout.setVisibility(faceLayoutVisibility);
                    fragmentCameraBinding.docParentLayout.setVisibility(matchIdLayoutVisibility);
                    fragmentCameraBinding.docLivenessParentLayout.setVisibility(docLivenessLayoutVisibility);
                    fragmentCameraBinding.resultParentLayout.setVisibility(resultLayoutVisibility);
                    fragmentCameraBinding.quickLivenessInst.setVisibility(View.GONE);
//                    fragmentCameraBinding.quickLivenessInst.setVisibility(quickLivenessInstVisibility);
                    fragmentCameraBinding.imagePreviewParentLayout.setVisibility(imgPreviewLayoutVisibility);
                    fragmentCameraBinding.resultInstScrParentLayout.setVisibility(qlResultInstructionScrVisibility);

                    if (resultLayoutVisibility == View.VISIBLE || imgPreviewLayoutVisibility == View.VISIBLE ||
                            qlResultInstructionScrVisibility == View.VISIBLE) {
                        if (helperFunctions.isAppLogoExists()) {
                            int imageResource = getResources().getIdentifier(
                                    ApiConstants.MERCHANT_APP_LOGO, "drawable", requestModel.getParentActivity().getPackageName());
                            fragmentCameraBinding.footer.appLogo.setImageResource(imageResource);
                            fragmentCameraBinding.footer.appLogo.setVisibility(View.VISIBLE);
                        } else {
                            fragmentCameraBinding.footer.appLogo.setVisibility(View.GONE);
                        }
                    }
                } catch (Exception e) {
                    Webhooks.exceptionReport(e, "CameraFragment/setUi-inner");
                }
            });
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/setUi");
        }
    }

    @Override
<<<<<<< HEAD
    public void convertBitmapToBase64() {
        new BitmapToBase64(detectedFaceFrame).execute();
=======
    public void convertBitmapToBase64(Bitmap bitmap) {
        new BitmapToBase64(bitmap).execute();
>>>>>>> origin/main
    }

    /**
     * method to set animation view
     *
     * @param animationView view to be set
     * @param animationName animation to be load
     */
    @Override
    public void setAnimationViewsAndText(WebView animationView, String animationName, String text,
                                         String requestStatus, double similarityScore, Boolean gotResult) {
        try {
            SingletonData.getInstance().getActivity().runOnUiThread(() -> {
                try {
                    fragmentCameraBinding.resultContinueBtn.setVisibility(View.GONE);
                    if (!gotResult) {
                        //show loader/uploader
                        helperFunctions.loadAnimation(animationView, animationName);
                        if (text.equals(SingletonData.getInstance().getActivity().getString(R.string.uploading))) {
                            helperFunctions.showResultText(text, animationView);
                        } else {
                            fragmentCameraBinding.resultTxt.setVisibility(View.GONE);
                        }
                    } else if (requestType.equals("additionalCheckLiveness") && currentState.equals("QL") &&
                            requestStatus.equals("liveness.unverified")) {
                        SingletonData.getInstance().setQuickRequestInProcess(false);
                        if (requestModel.getConfigObject().getBoolean("livenessRetryFlow")) {
                            if (recursiveQlCounter <= requestModel.getConfigObject().getInt("livenessRetryCount")) {
                                //re open QL
                                if (requestModel.getConfigObject().getBoolean("showMiddleInstructions")) {
                                    setUi(View.GONE, View.GONE, View.GONE, View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE);
                                    //auto time reset key should be implement here
//                                    new Handler().postDelayed(() -> helperFunctions.setQlUi(), TimeConstants.REMOVE_QL_INSTRUCTIONS_SCR);
                                } else {
                                    helperFunctions.setQlUi(true);
                                }
                            } else {
                                //QL failed in case of additional, go to DL
                                helperFunctions.setAdditionalLivenessUi(animationView);
                            }
                        } else {
                            //QL failed in case of additional, go to DL
                            helperFunctions.setAdditionalLivenessUi(animationView);
                        }
                    } else if (requestType.equals("photoIdMatch") && currentState.equals("QL")) {
                        if (requestStatus.equals("liveness.verified")) {
                            matchIdByCapture();
//                            //ql detected, show result
//                            helperFunctions.loadAnimation(animationView, animationName);
//                            helperFunctions.showResultText(text);
//                            // go to doc detection after delay
//                            new Handler().postDelayed(this::matchIdByCapture, TimeConstants.REMOVE_RESULT_SCREEN);
                        } else if (requestStatus.equals("liveness.unverified") &&
                                requestModel.getConfigObject().getBoolean("documentRetryFlow")) {
                            if (recursiveQlCounter <= requestModel.getConfigObject().getInt("documentRetryCount")) {
                                //re open QL
                                if (requestModel.getConfigObject().getBoolean("showMiddleInstructions")) {
                                    setUi(View.GONE, View.GONE, View.GONE, View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE);
                                    //auto time reset key should be implement here
//                                    new Handler().postDelayed(() -> helperFunctions.setQlUi(), TimeConstants.REMOVE_QL_INSTRUCTIONS_SCR);
                                } else {
                                    helperFunctions.setQlUi(true);
                                }
                            } else if (requestModel.getConfigObject().getBoolean("showResult")) {
                                helperFunctions.showResult(animationView, animationName, text, requestStatus, similarityScore);
                            } else {
                                helperFunctions.notToShowResult(requestStatus, similarityScore);
                            }
                        } else {
                            if (requestModel.getConfigObject().getBoolean("showResult")) {
                                helperFunctions.showResult(animationView, animationName, text, requestStatus, similarityScore);
                            } else {
                                helperFunctions.notToShowResult(requestStatus, similarityScore);
                            }
                        }
                    } else if (requestType.equals("quickLivenessOnly") && requestStatus.equals("liveness.unverified")) {
                        if (requestModel.getConfigObject().getBoolean("livenessRetryFlow")) {
                            if (recursiveQlCounter <= requestModel.getConfigObject().getInt("livenessRetryCount")) {
                                //re open QL
                                if (requestModel.getConfigObject().getBoolean("showMiddleInstructions")) {
                                    setUi(View.GONE, View.GONE, View.GONE, View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE);
                                    //auto time reset key should be implement here
//                                    new Handler().postDelayed(() -> helperFunctions.setQlUi(), TimeConstants.REMOVE_QL_INSTRUCTIONS_SCR);
                                } else {
                                    helperFunctions.setQlUi(true);
                                }
                            } else {
                                if (requestModel.getConfigObject().getBoolean("showResult")) {
                                    helperFunctions.showResult(animationView, animationName, text, requestStatus, similarityScore);
                                } else if (!requestModel.getConfigObject().getBoolean("showResult")) {
                                    helperFunctions.notToShowResult(requestStatus, similarityScore);
                                }
                            }
                        } else {
                            if (requestModel.getConfigObject().getBoolean("showResult")) {
                                helperFunctions.showResult(animationView, animationName, text, requestStatus, similarityScore);
                            } else if (!requestModel.getConfigObject().getBoolean("showResult")) {
                                helperFunctions.notToShowResult(requestStatus, similarityScore);
                            }
                        }
                    } else if (requestModel.getConfigObject().getBoolean("showResult")) {
                        helperFunctions.showResult(animationView, animationName, text, requestStatus, similarityScore);
                    } else if (!requestModel.getConfigObject().getBoolean("showResult")) {
                        helperFunctions.notToShowResult(requestStatus, similarityScore);
                    }
                } catch (Exception e) {
                    Webhooks.exceptionReport(e, "CameraFragment/setAnimationViewsAndText-inner");
                }
            });
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/setAnimationViewsAndText");
        }
    }

    @Override
    public void onClick(View view) {
        synchronized (view) {
            view.setEnabled(false);
            int id = view.getId();
            if (id == R.id.resultContinueBtn) {
                helperFunctions.feedbackScr();
            } else if (id == R.id.imgContinueBtn) {
                fragmentCameraBinding.retakeBtn.setEnabled(false);
                new Handler().postDelayed(() -> fragmentCameraBinding.retakeBtn.setEnabled(true),
                        TimeConstants.SYNCHRONIZED_CONSTANT);
                isDocCaptured = false;
                uploadCapturedImg();
            } else if (id == R.id.retakeBtn) {
                fragmentCameraBinding.imgContinueBtn.setEnabled(false);
                new Handler().postDelayed(() -> fragmentCameraBinding.imgContinueBtn.setEnabled(true),
                        TimeConstants.SYNCHRONIZED_CONSTANT);
                isDocCaptured = false;
                retakeBtnHandling();
            } else if (id == R.id.captureDocBtn) {
<<<<<<< HEAD
                isDocCaptured = true;
                Bitmap bitmap = fragmentCameraBinding.previewView.getBitmap();
                captureSound();
                convertAndDisplayCapturedCard(helperFunctions.cropFrame(bitmap));
=======
                if (fragmentCameraBinding.previewView.getPreviewStreamState()
                        .getValue().toString().equalsIgnoreCase("STREAMING")) {
                    isDocCaptured = true;
                    Bitmap bitmap = fragmentCameraBinding.previewView.getBitmap();
                    captureSound();
                    convertAndDisplayCapturedCard(helperFunctions.cropFrame(bitmap));
                }
>>>>>>> origin/main
            } else if (id == R.id.captureDocLivenessBtn) {
                Bitmap bitmap = fragmentCameraBinding.previewView.getBitmap();
                fragmentCameraBinding.detectedFrame.setImageBitmap(bitmap);
                setUi(View.GONE, View.GONE, View.GONE, View.GONE, View.GONE, View.GONE, View.GONE, View.VISIBLE);
            } else if (id == R.id.instRetryBtn) {
                helperFunctions.setQlUi(true);
            } else if (id == R.id.showInstBtn) {
                helperFunctions.showBottomSheet();
            }
        }
        new Handler().postDelayed(() -> view.setEnabled(true), TimeConstants.SYNCHRONIZED_CONSTANT);
    }

    private void uploadCapturedImg() {
        try {
            if (ServiceType.valueOf(requestModel.getConfigObject().getString("serviceType")) == ServiceType.DOCUMENT_LIVENESS) {
//                Toast.makeText(SingletonData.getInstance().getContext(), "Please Go Back", Toast.LENGTH_SHORT).show();
            } else {
                helperFunctions.handleCheckSimilarity(similarityApiHelper, true);
            }
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/uploadCapturedImg");
        }
    }

    private void retakeBtnHandling() {
        try {
            if (ServiceType.valueOf(requestModel.getConfigObject().getString("serviceType")) == ServiceType.MATCH_TO_PHOTO_ID) {
                fragmentCameraBinding.captureDocBtn.setVisibility(View.GONE);
                setUi(View.VISIBLE, View.GONE, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE, View.GONE);
                docBtnHandler.postDelayed(docBtnRunnable, TimeConstants.SHOW_CAPTURE_DOC_BTN_DELAY);
                detectCard();
            } else {
                if (DocumentType.valueOf(requestModel.getConfigObject().getString("documentType")) == DocumentType.ID_CARD) {
                    fragmentCameraBinding.captureDocBtn.setVisibility(View.GONE);
                    setUi(View.VISIBLE, View.GONE, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE, View.GONE);
                    docBtnHandler.postDelayed(docBtnRunnable, TimeConstants.SHOW_CAPTURE_DOC_BTN_DELAY);
                    detectCard();
                } else {
                    setUi(View.VISIBLE, View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE);
                }
            }
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/retakeBtnHandling");
        }
    }

    /**
     * invoking further method to change UI
     * and to open camera to detect card
     */
    private void matchIdByCapture() {
        try {
            currentState = "docDetection";
            fragmentCameraBinding.animationView.loadUrl("");
            docBtnHandler.postDelayed(docBtnRunnable, TimeConstants.SHOW_CAPTURE_DOC_BTN_DELAY);
            cardDetectionTflite = new CardDetectionTflite();
            initCamera(false);
            setUi(View.VISIBLE, View.GONE, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE, View.GONE);
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/matchIdByCapture");
        }
    }

    protected void detectCard() {
        try {
            if (!dialog.isShowing() && fragmentCameraBinding.imagePreviewParentLayout.getVisibility() != View.VISIBLE && !backPressed && !isCamStopped) {
                isCardDetectionInProcess = true;
                Bitmap bitmap = fragmentCameraBinding.previewView.getBitmap();
                if (bitmap != null) {
<<<<<<< HEAD
                    if (Color.alpha(bitmap.getPixel(0, 0)) != 0 &&
                            fragmentCameraBinding.docInstParentLayout.getVisibility() == View.GONE) {
=======
                    if ((Color.alpha(bitmap.getPixel(0, 0)) != 0 || fragmentCameraBinding.previewView.getPreviewStreamState()
                            .getValue().toString().equalsIgnoreCase("STREAMING")) &&
                            fragmentCameraBinding.docInstParentLayout.getVisibility() == View.GONE) {
                        fragmentCameraBinding.hidePreviewView.setVisibility(View.GONE);//android 14
>>>>>>> origin/main
                        fragmentCameraBinding.docInstParentLayout.setVisibility(View.VISIBLE);
                    }
                    new CameraFragment.DetectCardInBg(bitmap).execute();
                } else {
//                    if (isResumed){
<<<<<<< HEAD
                    new Handler().postDelayed(this::detectCard, 1000);
=======
                    new Handler().postDelayed(this::detectCard, 500);
>>>>>>> origin/main
                }
//                } else {
//                    detectCard(false);
//                }
            }
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/detectCard");
        }
    }

    protected void convertAndDisplayCapturedCard(Bitmap bitmap) {
        docBtnHandler.removeCallbacks(docBtnRunnable);
<<<<<<< HEAD
        new CameraFragment.ConvertBitmapToFileAndProcess(bitmap).execute();
=======
        new CameraFragment.ConvertBitmapToFileAndProcess(bitmap, false).execute();
>>>>>>> origin/main
        fragmentCameraBinding.detectedFrame.setImageBitmap(bitmap);
        setUi(View.GONE, View.GONE, View.GONE, View.GONE, View.GONE, View.GONE, View.GONE, View.VISIBLE);
    }

    /**
     * method for the capture' sound
     */
    private void captureSound() {
        MediaActionSound sound = new MediaActionSound();
        sound.play(MediaActionSound.SHUTTER_CLICK);
    }


    private Boolean isImgGlow(Bitmap image) {
        int brightPixelCounter = 0;
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];
        image.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int pixel : pixels) {
            int red = Color.red(pixel);
            int green = Color.green(pixel);
            int blue = Color.blue(pixel);
            double pixelBrightness = (red + green + blue) / 3.0;
            if (pixelBrightness > 240) {
//            if (blue > 235 && red > 245 && green > 245) {
//            double pixelBrightness = (0.2126 * red) + (0.7152 * green) + (0.0722 * blue);
//            if (pixelBrightness > 225) {
                brightPixelCounter++;
                double percentage = 1.5;
                double expectedValue = (percentage / 100) * pixels.length;
                if (brightPixelCounter >= expectedValue) {
                    return true;
                }
            }
//            else{
//                Log.i("Faizan", "Faizan2: " + pixelBrightness);
//            }
        }
        return false;
    }

    private boolean isImgGlowAndReflected(Bitmap image, List<FaceContour> faceContourList) {
        int brightPixelCounter = 0;
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];
        image.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < pixels.length; i++) {
            int x = i % width;
            int y = i / width;
            int pixel = pixels[i];

            int red = Color.red(pixel);
            int green = Color.green(pixel);
            int blue = Color.blue(pixel);
            double pixelBrightness = (red + green + blue) / 3.0;
            double reflectance = Math.max(Math.max(red, green), blue) - Math.min(Math.min(red, green), blue);

            if ((pixelBrightness > (getFaceRect(faceContourList).contains(x, y) ? 245 : 230)) || reflectance > 7) {
                brightPixelCounter++;
                double percentage = 1.05;
                double expectedValue = (percentage / 100) * pixels.length;
                if (brightPixelCounter >= expectedValue) {
                    return true;
                }
            }
        }
        return false;
    }

    private Boolean isGlow(Bitmap grayscaleImage) {
        final int BOX_SIZE = 75; // Size of each box (120x120 pixels)
        int connectedBoxCounter = 0;
        List<String> rightArr = new ArrayList<>();
        List<String> bottomArr = new ArrayList<>();
//        Bitmap canvasBmp = grayscaleImage.copy(Bitmap.Config.ARGB_8888, true);
//        Canvas canvas = new Canvas(canvasBmp);

        int imageWidth = grayscaleImage.getWidth();
        int imageHeight = grayscaleImage.getHeight();

        int boxCountX = (imageWidth + BOX_SIZE - 1) / BOX_SIZE; // Number of boxes in x-axis
        int boxCountY = (imageHeight + BOX_SIZE - 1) / BOX_SIZE; // Number of boxes in y-axis

        for (int boxY = 0; boxY < boxCountY; boxY++) {
            for (int boxX = 0; boxX < boxCountX; boxX++) {
                // Calculate the starting pixel coordinates of the current box
                int startX = boxX * BOX_SIZE;
                int startY = boxY * BOX_SIZE;
                // Calculate the ending pixel coordinates of the current box
                int endX = Math.min(startX + BOX_SIZE, imageWidth);
                int endY = Math.min(startY + BOX_SIZE, imageHeight);
                // Perform function within the current box
                double totalBrightness = 0;
                for (int y = startY; y < endY; y++) {
                    for (int x = startX; x < endX; x++) {
                        int pixel = grayscaleImage.getPixel(x, y);
                        int red = Color.red(pixel);
                        int green = Color.green(pixel);
                        int blue = Color.blue(pixel);
                        double pixelBrightness = (red + green + blue) / 3.0;
                        totalBrightness = totalBrightness + pixelBrightness;
                    }
                }
                double boxAvgBrightness = totalBrightness / (BOX_SIZE * BOX_SIZE);
                if (boxAvgBrightness > 225) {
//                    Paint paint = new Paint();
//                    paint.setColor(Color.GREEN);
//                    paint.setStrokeWidth(8);
//                    paint.setStyle(Paint.Style.STROKE);
//                    canvas.drawRect(startX, startY, endX - 1, endY - 1, paint);
                    if (rightArr.contains((startX) + "," + startY + "," +
                            (startX) + "," + (endY)) ||
                            bottomArr.contains(startX + "," + (startY) + "," +
                                    (endX) + "," + (startY))) {
                        //box is connected with another
                        connectedBoxCounter++;
                        if (connectedBoxCounter > 0) {
                            //Glow/Light detected
                            return true;
                        }
                    } else {
                        rightArr.add((endX) + "," + startY + "," +
                                (endX) + "," + (endY));
                        bottomArr.add(startX + "," + endY + "," +
                                endX + "," + endY);
                    }
                }
            }
        }
        return false;
    }

    private Bitmap toGrayscale(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();

        Bitmap grayscaleImage = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(grayscaleImage);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(filter);
        canvas.drawBitmap(image, 0, 0, paint);

        return grayscaleImage;
    }

    /**
     * method to get Rect of face
     */
    private Rect getFaceRect(List<FaceContour> faceContourList) {
        return new Rect((int) (faceContourList.get(0).getPoints().get(28).x),
                (int) (faceContourList.get(0).getPoints().get(0).y),
                (int) (faceContourList.get(0).getPoints().get(8).x),
                (int) (faceContourList.get(0).getPoints().get(18).y));
    }

    /**
     * method to change bg color
     */
    protected void changeBgColor() {
        try {
            isBgChanging = true;
            int startColor, endColor;
            int backgroundColor = ((ColorDrawable) fragmentCameraBinding.faceViewTop.getBackground()).getColor();
            if (backgroundColor == getResources().getColor(R.color.facia_white_color)) {
                startColor = SingletonData.getInstance().getActivity().getResources().getColor(R.color.facia_white_color);
                endColor = SingletonData.getInstance().getActivity().getResources().getColor(R.color.facia_black_color);
            } else {
                startColor = SingletonData.getInstance().getActivity().getResources().getColor(R.color.facia_black_color);
                endColor = SingletonData.getInstance().getActivity().getResources().getColor(R.color.facia_white_color);
            }
            fragmentCameraBinding.faceViewTop.setBackgroundColor(startColor);

            ValueAnimator colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
            colorAnimator.setDuration(700);

            colorAnimator.addUpdateListener(animator -> {
                int animatedValue = (int) animator.getAnimatedValue();
                fragmentCameraBinding.faceViewTop.setBackgroundColor(animatedValue);
            });
            colorAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    isBgChanging = false;
                }
            });
            colorAnimator.start();
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "CameraFragment/HelperFunction/changeBgColor");
        }
    }

    /**
     * Async method to convert bitmap to file
     */
    private class ConvertBitmapToFileAndProcess {
<<<<<<< HEAD
        Bitmap detectedDoc = null;

        public ConvertBitmapToFileAndProcess(Bitmap detectedDoc) {
            this.detectedDoc = detectedDoc;
=======
        Bitmap bitmap = null;
        Boolean isQl = true;

        public ConvertBitmapToFileAndProcess(Bitmap bitmap, Boolean isQl) {
            this.bitmap = bitmap;
            this.isQl = isQl;
>>>>>>> origin/main
        }

        public void execute() {
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                final File faceFileToProcess = doInBackground();
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(() -> onPostExecute(faceFileToProcess));
            });
        }

        protected File doInBackground() {
<<<<<<< HEAD
            if (detectedDoc == null) {
                return convertBitmapToFile(detectedFaceFrame, false);
            } else {
                return convertBitmapToFile(detectedDoc, true);
=======
            if (isQl) {
                return convertBitmapToFile(bitmap, false);
            } else {
                return convertBitmapToFile(bitmap, true);
>>>>>>> origin/main
            }
        }

        protected void onPostExecute(File fileToProcess) {
<<<<<<< HEAD
            if (detectedDoc != null) {
                requestModel.setIdImage(fileToProcess);
            } else {
=======
            if (!isQl) {
                requestModel.setIdImage(fileToProcess);
            }
            else {
>>>>>>> origin/main
                try {
                    if (ServiceType.valueOf(requestModel.getConfigObject().getString("serviceType")) == ServiceType.MATCH_TO_PHOTO_ID) {
                        requestModel.setFaceImage(fileToProcess);
                    }
                } catch (Exception ignored) {
                }
                try {
                    livenessApiHelper.createRequestJsonObject(fileToProcess, "ql", currentState);
                } catch (Exception e) {
                    livenessApiHelper.createRequestJsonObject(fileToProcess, "ql", currentState);
                }
            }
        }
    }

    /**
     * Async method to convert bitmap to base64 and to add in the list
     */
    private class BitmapToBase64 {
        Bitmap bitmap;

        public BitmapToBase64(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        public void execute() {
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                final String base64 = doInBackground();
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(() -> onPostExecute(base64));
            });
        }

        protected String doInBackground() {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        }

        protected void onPostExecute(String base64img) {
            //334 ms delay
            qlListCurrent = System.currentTimeMillis();
            if (SingletonData.getInstance().getQlFrameList().size() == 0) {
                qlListPrevious = System.currentTimeMillis();
                SingletonData.getInstance().getQlFrameList().add(base64img);
<<<<<<< HEAD
            } else if (qlListCurrent - qlListPrevious >= TimeConstants.ADD_IN_QL_LIST_DELAY) {
=======
            } else if (qlListCurrent - qlListPrevious >= TimeConstants.ADD_IN_QL_LIST_DELAY &&
                    SingletonData.getInstance().getQlFrameList().size() < 9) {
>>>>>>> origin/main
                SingletonData.getInstance().getQlFrameList().add(base64img);
                qlListPrevious = System.currentTimeMillis();
            }
        }
    }

    /**
     * Async method to process frame in bg to detect card through TFLite model
     */
    private class DetectCardInBg {
        Bitmap bitmap;

        public DetectCardInBg(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        public void execute() {
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                final Bitmap resultedBitmap = doInBackground();
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(() -> onPostExecute(resultedBitmap));
            });
        }

        protected Bitmap doInBackground() {
            return cardDetectionTflite.run(bitmap);
        }

        protected void onPostExecute(Bitmap resultedBitmap) {
            if (resultedBitmap == null) {
                detectCard();
            } else {
                helperFunctions.handleCardDetection(resultedBitmap);
            }
        }
    }

<<<<<<< HEAD
    private class DetectLight {
        Bitmap bitmap;
        Face face;

        public DetectLight(Bitmap bitmap, Face face) {
            this.bitmap = bitmap;
            this.face = face;
        }

        public void execute() {
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                final Boolean isGlow = doInBackground();
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(() -> onPostExecute(isGlow));
=======
    private class TestPreview {
        public void execute() {
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                final Boolean isDone = doInBackground();
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(() -> onPostExecute(isDone));
>>>>>>> origin/main
            });
        }

        protected Boolean doInBackground() {
<<<<<<< HEAD
//            return isGlow(toGrayscale(bitmap));
//            return isImgGlow(toGrayscale(bitmap));
            return isImgGlowAndReflected(toGrayscale(bitmap), face.getAllContours());
        }

        protected void onPostExecute(Boolean isGlow) {
//            if (currentState.equals("QL")) {
            if (isGlow) {
//                SingletonData.getInstance().setQuickLivenessFrameCount(0);
//                fragmentCameraBinding.quickLivenessInstTxt.setText(getResources().getString(R.string.improper_lighting));
//                fragmentCameraBinding.quickLivenessInst.setVisibility(View.VISIBLE);
//                frameProcessed();

                SingletonData.getInstance().setQuickLivenessFrameCount(0);
//                SingletonData.getInstance().getFragmentCameraBinding().faceBorder.setBackgroundResource(
//                        R.drawable.face_border_bg_red);
                SingletonData.getInstance().getFragmentCameraBinding().faceDetectInst.setText(R.string.improper_lighting);
                frameProcessed();
            } else {
//                fragmentCameraBinding.quickLivenessInstTxt.setText("No Light Detected");
                faceDetectionHelper.setFaceMeshResult(face);
            }
//            }else {
//                if (!isBgChanging) {
//                    changeBgColor();
//                }
//                faceDetectionHelper.setFaceMeshResult(face);
//            }
        }
    }
=======
            return fragmentCameraBinding.previewView.getPreviewStreamState()
                    .getValue().toString().equalsIgnoreCase("STREAMING");
        }

        protected void onPostExecute(Boolean isDone) {
            if (isDone){
                fragmentCameraBinding.hidePreviewView.setVisibility(View.GONE);
            }else {
                new TestPreview().execute();
            }
        }
    }

//    private class DetectLight {
//        Bitmap bitmap;
//        Face face;
//
//        public DetectLight(Bitmap bitmap, Face face) {
//            this.bitmap = bitmap;
//            this.face = face;
//        }
//
//        public void execute() {
//            Executor executor = Executors.newSingleThreadExecutor();
//            executor.execute(() -> {
//                final Boolean isGlow = doInBackground();
//                Handler mainHandler = new Handler(Looper.getMainLooper());
//                mainHandler.post(() -> onPostExecute(isGlow));
//            });
//        }
//
//        protected Boolean doInBackground() {
////            return isGlow(toGrayscale(bitmap));
////            return isImgGlow(toGrayscale(bitmap));
//            return isImgGlowAndReflected(toGrayscale(bitmap), face.getAllContours());
//        }
//
//        protected void onPostExecute(Boolean isGlow) {
////            if (currentState.equals("QL")) {
//            if (isGlow) {
////                SingletonData.getInstance().setQuickLivenessFrameCount(0);
////                fragmentCameraBinding.quickLivenessInstTxt.setText(getResources().getString(R.string.improper_lighting));
////                fragmentCameraBinding.quickLivenessInst.setVisibility(View.VISIBLE);
////                frameProcessed();
//
//                SingletonData.getInstance().setQuickLivenessFrameCount(0);
////                SingletonData.getInstance().getFragmentCameraBinding().faceBorder.setBackgroundResource(
////                        R.drawable.face_border_bg_red);
//                SingletonData.getInstance().getFragmentCameraBinding().faceDetectInst.setText(R.string.improper_lighting);
//                frameProcessed();
//            } else {
////                fragmentCameraBinding.quickLivenessInstTxt.setText("No Light Detected");
//                faceDetectionHelper.setFaceMeshResult(face);
//            }
////            }else {
////                if (!isBgChanging) {
////                    changeBgColor();
////                }
////                faceDetectionHelper.setFaceMeshResult(face);
////            }
//        }
//    }
>>>>>>> origin/main
}
package com.facia.faciasdk.Camera.CameraXHelpers;

import android.graphics.Bitmap;
import android.webkit.WebView;

public interface CameraListeners {
    void frameProcessed();

    void startVideoRecording();

    void stopVideoRecording();

<<<<<<< HEAD
    void processQuickLiveness();
=======
    void processQuickLiveness(Bitmap bitmap);
>>>>>>> origin/main

    void cameraTimeOut();

    void fileUploaded();

    void setAnimationViewsAndText(WebView animationView, String animationName, String text,
                                  String resultStatus, double similarityScore, Boolean gotResult);

    void setUi(int cameraLayoutVisibility, int faceLayoutVisibility, int matchIdLayoutVisibility, int docLivenessLayoutVisibility, int quickLivenessInstVisibility,
               int qlResultInstructionScrVisibility, int resultLayoutVisibility, int imgPreviewLayoutVisibility);

<<<<<<< HEAD
    void convertBitmapToBase64();
=======
    void convertBitmapToBase64(Bitmap bitmap);
>>>>>>> origin/main
}

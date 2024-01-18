package com.facia.faciasdk.Activity.Helpers;

import android.app.Activity;
import org.json.JSONObject;
import java.io.File;

public class RequestModel {
    private String token;
    private float similarityScore;
    private File faceImage, idImage;
    private Activity parentActivity;
    private Boolean isSimilarity = false;
    private RequestListener requestListener;
    private JSONObject configObject;

    public RequestModel() {
    }

    public float getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(float similarityScore) {
        this.similarityScore = similarityScore;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Activity getParentActivity() {
        return parentActivity;
    }

    public void setParentActivity(Activity parentActivity) {
        this.parentActivity = parentActivity;
    }

    public RequestListener getRequestListener() {
        return requestListener;
    }

    public void setRequestListener(RequestListener requestListener) {
        this.requestListener = requestListener;
    }

    public File getFaceImage() {
        return faceImage;
    }

    public void setFaceImage(File faceImage) {
        this.faceImage = faceImage;
    }

    public File getIdImage() {
        return idImage;
    }

    public void setIdImage(File idImage) {
        this.idImage = idImage;
    }

    public Boolean isSimilarity() {
        return isSimilarity;
    }

    public void setSimilarity(Boolean similarity) {
        isSimilarity = similarity;
    }

    public JSONObject getConfigObject() {
        return configObject;
    }

    public void setConfigObject(JSONObject configObject) {
        this.configObject = configObject;
    }
}

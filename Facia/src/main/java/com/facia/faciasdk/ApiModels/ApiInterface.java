package com.facia.faciasdk.ApiModels;

import com.facia.faciasdk.ApiModels.CheckSimilarity.CheckSimilarity;
import com.facia.faciasdk.ApiModels.CreateTransaction.CreateTransaction;
import com.facia.faciasdk.ApiModels.LivenessRequest.LivenessRequest;
import com.facia.faciasdk.ApiModels.Result.Result;
import com.facia.faciasdk.ApiModels.SendFeedback.SendFeedback;
import com.facia.faciasdk.ApiModels.UploadQlVideo.UploadQlVideo;
import com.facia.faciasdk.Utils.Constants.ApiConstants;
import com.google.gson.JsonObject;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiInterface {
    @POST(ApiConstants.CREATE_TRANSACTION)
    Call<CreateTransaction> createTransaction(@Header("Authorization") String token,
                                              @Header("user-agent") String userAgent,
                                              @Header("platform") String platform,
                                              @Body RequestBody body);

//    @POST(ApiConstants.CREATE_TRANSACTION)
//    Call<UploadQlVideo> uploadQlVideo(@Header("Authorization") String token,
//                                      @Header("ip") String ip,
//                                      @Header("client-email") String workMail,
//                                      @Header("country-name") String countryName,
//                                      @Header("country-code") String countryCode,
//                                      @Header("continent") String continent,
//                                      @Header("continent-code") String continentCode,
//                                      @Header("city") String city,
//                                      @Header("isp") String isp,
//                                      @Header("lat") double lat,
//                                      @Header("lon") double lon,
//                                      @Header("query") String query,
//                                      @Header("user-agent") String userAgent,
//                                      @Header("platform") String platform,
//                                      @Body RequestBody body);

    @POST(ApiConstants.CREATE_TRANSACTION)
    Call<UploadQlVideo> uploadFrameList(@Header("Authorization") String token,
                                      @Header("user-agent") String userAgent,
                                      @Header("platform") String platform,
                                      @Body JsonObject jsonObject);

    @POST(ApiConstants.LIVENESS_RESULT)
    Call<Result> getResult(@Header("Authorization") String token,
                           @Header("user-agent") String userAgent,
                           @Header("platform") String platform,
                           @Body JsonObject body);

    @POST(ApiConstants.CREATE_TRANSACTION)
    Call<CheckSimilarity> checkSimilarity(@Header("Authorization") String token,
                                          @Header("user-agent") String userAgent,
                                          @Header("platform") String platform,
                                          @Body RequestBody body);

    @POST(ApiConstants.CHECK_LIVENESS)
    Call<LivenessRequest> livenessRequest(@Header("Authorization") String token,
                                          @Header("user-agent") String userAgent,
                                          @Header("platform") String platform,
                                          @Body RequestBody body);

    @POST(ApiConstants.CREATE_FEEDBACK)
    Call<SendFeedback> sendFeedback(@Header("Authorization") String token,
                                    @Header("user-agent") String userAgent,
                                    @Header("platform") String platform,
                                    @Body JsonObject body);
}
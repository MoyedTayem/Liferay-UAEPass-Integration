package com.uae.pass.api;

import com.uae.pass.model.UAEPassTokenResponse;
import com.uae.pass.model.UAEPassUserInfoResponse;
import retrofit2.Call;
import retrofit2.http.*;

interface UAEPassAPIService {

	@FormUrlEncoded
	@POST("/idshub/token")
	Call<UAEPassTokenResponse> getToken(@Header("Content-Type") String mediaType, @Field("grant_type") String grant_type,
			@Field("code") String code, @Field("redirect_uri") String redirect_uri,
			@Field("client_id") String client_id, @Field("client_secret") String client_secret);

	@GET("/idshub/userinfo")
	Call<UAEPassUserInfoResponse> getUserInfo(@Header("Authorization") String bearerToken);
	
}

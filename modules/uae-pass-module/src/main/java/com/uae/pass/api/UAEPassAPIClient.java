package com.uae.pass.api;

import com.uae.pass.model.UAEPassTokenResponse;
import com.uae.pass.model.UAEPassUserInfoResponse;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class UAEPassAPIClient {

	public UAEPassAPIService UAEPassApiService;

	public UAEPassAPIClient(String uaePassApiBaseUrl) {

		OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
				.readTimeout(180, TimeUnit.SECONDS).build();


		UAEPassApiService = new Retrofit.Builder().baseUrl(uaePassApiBaseUrl).client(client)
				.addConverterFactory(JacksonConverterFactory.create()).build().create(UAEPassAPIService.class);
	}

	public UAEPassTokenResponse getToken(String mediaType, String grant_type, String code,
			String redirect_uri, String client_id, String client_secret) throws UAEPassAPIException, IOException {

		Response<UAEPassTokenResponse> response =
				UAEPassApiService.getToken(mediaType, grant_type, code, redirect_uri, client_id, client_secret)
				.execute();
		if(response.isSuccessful()) {
			return response.body();
		} else {
			String errorBody = new String(response.errorBody().bytes(), StandardCharsets.UTF_8);
			throw new UAEPassAPIException(response, "Failed to call " + response.raw().request().url() + " API  "
					+ response.code() + " " + errorBody);
		}
	}

	public UAEPassUserInfoResponse getUserInfo(String bearerToken) throws UAEPassAPIException, IOException {

		Response<UAEPassUserInfoResponse> response = UAEPassApiService.getUserInfo(bearerToken)
				.execute();
		if(response.isSuccessful()) {
			return response.body();
		} else {
			String errorBody = new String(response.errorBody().bytes(), StandardCharsets.UTF_8);
			throw new UAEPassAPIException(response, "Failed to call " + response.raw().request().url() + " API  "
					+ response.code() + " " + errorBody);
		}
	}

}

package com.mtkp.multitool.data.remote.network;

import com.mtkp.multitool.data.remote.dto.AuthDto;
import com.mtkp.multitool.data.remote.dto.AuthLoginRequestDto;
import com.mtkp.multitool.data.remote.dto.AuthRegisterRequestDto;
import com.mtkp.multitool.data.remote.dto.AuthVerifyResponseDto;
import com.mtkp.multitool.data.remote.dto.CategoriesResponseDto;
import com.mtkp.multitool.data.remote.dto.CreateExtensionRequestDto;
import com.mtkp.multitool.data.remote.dto.CreateInstalledExtensionRequestDto;
import com.mtkp.multitool.data.remote.dto.ExtensionDto;
import com.mtkp.multitool.data.remote.dto.ExtensionMetaDto;
import com.mtkp.multitool.data.remote.dto.ExtensionsListResponseDto;
import com.mtkp.multitool.data.remote.dto.InstalledExtensionDto;
import com.mtkp.multitool.data.remote.dto.InstalledExtensionsResponseDto;
import com.mtkp.multitool.data.remote.dto.CreateRatingRequestDto;
import com.mtkp.multitool.data.remote.dto.RatingDto;
import com.mtkp.multitool.data.remote.dto.RatingsResponseDto;
import com.mtkp.multitool.data.remote.dto.UpdateExtensionRequestDto;
import com.mtkp.multitool.data.remote.dto.UpdateRatingRequestDto;
import com.mtkp.multitool.data.remote.dto.UpdateInstalledExtensionRequestDto;
import com.mtkp.multitool.data.remote.dto.UploadVersionResponseDto;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Part;
import retrofit2.http.Streaming;

/**
 * Retrofit endpoints согласно API спецификации проекта.
 */
public interface BackendApiService {

    @POST("auth/register")
    Call<AuthDto> register(@Body AuthRegisterRequestDto body);

    @POST("auth/login")
    Call<AuthDto> login(@Body AuthLoginRequestDto body);

    @GET("auth/verify")
    Call<AuthVerifyResponseDto> verifyToken();

    @GET("extensions")
    Call<ExtensionsListResponseDto> getExtensions(
            @Query("page") int page,
            @Query("per_page") int perPage,
            @Query("category") String category,
            @Query("sort") String sort,
            @Query("search") String search
    );

    @GET("extensions/{id}")
    Call<ExtensionDto> getExtensionById(@Path("id") int id);

    @GET("extensions/{id}/meta")
    Call<ExtensionMetaDto> getExtensionMeta(@Path("id") int id);

    @GET("categories")
    Call<CategoriesResponseDto> getCategories();

    @GET("users/{userId}/installed-extensions")
    Call<InstalledExtensionsResponseDto> getInstalledExtensions(@Path("userId") int userId);

    @POST("users/{userId}/installed-extensions")
    Call<InstalledExtensionDto> createInstalledExtension(
            @Path("userId") int userId,
            @Body CreateInstalledExtensionRequestDto body
    );

    @PATCH("users/{userId}/installed-extensions/{installedId}")
    Call<InstalledExtensionDto> updateInstalledExtension(
            @Path("userId") int userId,
            @Path("installedId") int installedId,
            @Body UpdateInstalledExtensionRequestDto body
    );

    @DELETE("users/{userId}/installed-extensions/{installedId}")
    Call<Void> deleteInstalledExtension(
            @Path("userId") int userId,
            @Path("installedId") int installedId
    );

    @POST("extensions")
    Call<ExtensionDto> createExtension(@Body CreateExtensionRequestDto body);

    @PATCH("extensions/{id}")
    Call<ExtensionDto> updateExtension(
            @Path("id") int extensionId,
            @Body UpdateExtensionRequestDto body
    );

    @Multipart
    @POST("extensions/{id}/versions")
    Call<UploadVersionResponseDto> uploadVersion(
            @Path("id") int extensionId,
            @Part("version") RequestBody version,
            @Part("releaseNotes") RequestBody releaseNotes,
            @Part MultipartBody.Part jarFile,
            @Part("changelog") RequestBody changelog
    );

    @Streaming
    @GET("extensions/{id}/download")
    Call<ResponseBody> downloadExtension(
            @Path("id") int extensionId,
            @Query("version") String version
    );

    @GET("extensions/{id}/ratings")
    Call<RatingsResponseDto> getRatings(
            @Path("id") int extensionId,
            @Query("page") int page,
            @Query("per_page") int perPage
    );

    @POST("extensions/{id}/ratings")
    Call<RatingDto> createRating(
            @Path("id") int extensionId,
            @Body CreateRatingRequestDto body
    );

    @PATCH("extensions/{id}/ratings/{ratingId}")
    Call<RatingDto> updateRating(
            @Path("id") int extensionId,
            @Path("ratingId") int ratingId,
            @Body UpdateRatingRequestDto body
    );

    @DELETE("extensions/{id}/ratings/{ratingId}")
    Call<Void> deleteRating(
            @Path("id") int extensionId,
            @Path("ratingId") int ratingId
    );
}


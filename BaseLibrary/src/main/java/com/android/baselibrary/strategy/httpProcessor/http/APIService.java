package com.android.baselibrary.strategy.httpProcessor.http;

import com.android.baselibrary.bean.AppInfo;
import com.android.baselibrary.bean.Member;
import com.android.baselibrary.strategy.httpProcessor.http.common.HttpResult;

import java.util.Map;
import java.util.WeakHashMap;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * API--接口  服务[这里处理的是同一的返回格式 resultCode  resultInfo Data<T> --->这里的data才是返回的结果,T就是泛型 具体返回的been对象或集合]
 * Created by HDL on 2016/8/3.
 */
public interface APIService {
    /**
     * 用户登录的接口
     *
     * @return RxJava 对象
     */
    //如果去掉@FromUrlEncoded在post请求中使用@Field和@FieldMap，那么程序会抛出
    //java.lang.IllegalArgumentException: @Field parameters can only be used with form encoding. (parameter #1)的错误异常。
    @FormUrlEncoded
    @POST("login")
    Observable<HttpResult<Member>> userLogin(@FieldMap Map<String, String> maps);
    /**
     * app版本的接口
     */
    @GET("app")
    Observable<HttpResult<AppInfo>> checkVersion(@QueryMap Map<String, String> maps);
    /**
     * 下载的接口
     */
    @Streaming
    @GET
    Observable<ResponseBody> download(@Url String url);
    /**
     * 头像上传的接口
     */
    @Multipart
    @POST("user/upload")
    Observable<HttpResult<Member>> updatePhoto(@PartMap Map<String, RequestBody> maps, @Part MultipartBody.Part file);


    @GET
    Observable<String> get(@Url String url, @QueryMap WeakHashMap<String, Object> params);

    @FormUrlEncoded
    @POST
    Observable<String> post(@Url String url, @FieldMap WeakHashMap<String, Object> params);

    @POST
    Observable<String> postRaw(@Url String url, @Body RequestBody body);

    @FormUrlEncoded
    @PUT
    Observable<String> put(@Url String url, @FieldMap WeakHashMap<String, Object> params);

    @PUT
    Observable<String> putRaw(@Url String url, @Body RequestBody body);

    @DELETE
    Observable<String> delete(@Url String url, @QueryMap WeakHashMap<String, Object> params);

    @Streaming
    @GET
    Observable<ResponseBody> download(@Url String url, @QueryMap WeakHashMap<String, Object> params);

    @Multipart
    @POST
    Observable<String> upload(@Url String url, @Part MultipartBody.Part file);
}

package com.example.krtc_informaction.api;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.example.krtc_informaction.BuildConfig;
import com.example.krtc_informaction.model.BaseVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.orhanobut.logger.Logger;

import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClientService {
    public final static int CONNECT_TIMEOUT = 15; // 设置连接超时时间
    public final static int READ_TIMEOUT = 15; // 设置读取超时时间
    public final static int WRITE_TIMEOUT = 15; // 设置写的超时时间

    public static Retrofit sRetrofit;
    public static String sBase = "https://api.kcg.gov.tw/";
    public static String sBaseCode = "";
    public static Gson sGson = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .disableHtmlEscaping()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .setPrettyPrinting()
            .serializeNulls()
            .setLenient()
            .create();

    public static Retrofit initialize() {
        Log.d("ApiClientService", "ApiClientService initialize");
        if (sRetrofit == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(ApiClientService::formatMsg);
            interceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
            if (BuildConfig.DEBUG) {
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            }

            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .retryOnConnectionFailure(true)
                    .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS) // 设置连接超时时间
                    .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS) // 设置读取超时时间
                    .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS) // 设置写的超时时间
                    .addInterceptor(interceptor);

            OkHttpClient client = builder.build();
            sRetrofit = new Retrofit.Builder()
                    .baseUrl(sBase)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(sGson))
                    .client(client)
                    .build();
        }

        return sRetrofit;
    }

    public static Retrofit reStart(String base) {
        sBase = base;
        sRetrofit = null;
        return initialize();
    }

    private static void formatMsg(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        if (msg.contains("-->") && msg.contains("=")) {
            Logger.w(msg);
            return;
        }
        if (msg.contains("<--") && msg.contains("https://")) {
            Logger.i(msg);
            return;
        }
        if (isJson(msg)) {
            Logger.json(msg);
            return;
        }

        Log.d("server msg: ", msg);
    }

    @SuppressWarnings("CatchMayIgnoreException")
    private static boolean isJson(String msg) {
        try {
            new ObjectMapper().readTree(msg);
            return true;
        } catch (Exception e) {
        }

        return false;
    }

    public static <T> ObservableTransformer<Response<BaseVo<T>>, BaseVo<T>> applySubscribeVo() {

        return new ObservableTransformer<Response<BaseVo<T>>, BaseVo<T>>() {
            @Override
            public ObservableSource<BaseVo<T>> apply(Observable<Response<BaseVo<T>>> observable) {
                return observable.subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.computation())
                        .map(new Function<Response<BaseVo<T>>, BaseVo<T>>() {
                            @Override
                            public BaseVo<T> apply(Response<BaseVo<T>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    BaseVo vo = response.body();
                                    vo.setCode(vo.isSuccess() ? 200 : 500);
                                    return vo;
                                }

                                BaseVo vo = new BaseVo();
                                vo.setCode(response.code());
                                return vo;
                            }
                        })
                        .onErrorReturn(new Function<Throwable, BaseVo<T>>() {
                            @Override
                            public BaseVo<T> apply(Throwable e) {
                                BaseVo vo = new BaseVo();
                                vo.setCode(404);
                                vo.setMessage(e.getMessage());
                                if (e instanceof SocketTimeoutException) {
                                    vo.setCode(408);
                                } else if (e instanceof JsonParseException) {
                                    vo.setCode(487);
                                }

                                return vo;
                            }
                        });
            }
        };
    }

//    protected <T> ObservableTransformer<T, T> applySubscribeVo() {
//        return upstream -> upstream
//                .subscribeOn(Schedulers.io()) // 在 I/O 線程執行
//                .observeOn(AndroidSchedulers.mainThread()); // 在主線程處理結果
//    }
}
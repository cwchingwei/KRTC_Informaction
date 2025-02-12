package com.example.krtc_informaction.api;

import static com.example.krtc_informaction.api.ApiClientService.applySubscribeVo;

import com.example.krtc_informaction.model.BaseVo;
import com.example.krtc_informaction.model.Station;

import org.json.JSONObject;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Response;

public class ApiStoreService {
    private static class SingletonHolder {
        private static final ApiStoreService instance = new ApiStoreService();
    }

    public static ApiStoreService initialize() {
        return SingletonHolder.instance;
    }

    private static ApiService sApiStores = ApiClientService.initialize().create(ApiService.class);

    private RequestBody getRequestBody(JSONObject object) {
        return RequestBody.create(MediaType.parse("application/json"), object.toString());
    }

    private RequestBody getRequestBody(String s) {
        return RequestBody.create(MediaType.parse("text/plain"), s);
    }

    public void reStart() {
        sApiStores = ApiClientService.initialize().create(ApiService.class);
    }

    public Observable<BaseVo<List<Station>>> getStationData() {
        reStart();

        return sApiStores.getStations().compose(applySubscribeVo());
    }

}

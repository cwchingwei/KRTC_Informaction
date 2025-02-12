package com.example.krtc_informaction.api;

import com.example.krtc_informaction.model.BaseVo;
import com.example.krtc_informaction.model.Station;
import retrofit2.Response;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("api/service/Get/4278fc6a-c3ea-4192-8ce0-40f00cdb40dd")
    Observable<Response<BaseVo<List<Station>>>> getStations();
}

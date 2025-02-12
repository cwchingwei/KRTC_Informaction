package com.example.krtc_informaction.view_model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.krtc_informaction.model.Station;
import java.util.ArrayList;
import java.util.List;

public class StationViewModel extends ViewModel {
    private final MutableLiveData<List<Station>> stationList = new MutableLiveData<>(new ArrayList<>());

    // ✅ 設置新數據，確保不覆蓋原本的 List
    public void setStations(List<Station> stations) {
        if (stations != null) {
            stationList.postValue(stations); // ✅ 確保 LiveData 更新數據
        }
    }

    // ✅ 確保 getStations() 不會回傳 null
    public LiveData<List<Station>> getStations() {
        return stationList;
    }
}

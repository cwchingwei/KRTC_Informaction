package com.example.krtc_informaction.apapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.krtc_informaction.R;
import com.example.krtc_informaction.model.Station;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.QuickViewHolder;
import java.util.List;

public class StationAdapter extends BaseQuickAdapter<Station, QuickViewHolder> {

    private OnStationClickListener onStationClickListener;
    private static final String TAG = "StationAdapter";  // ✅ 定義 Log 標籤

    public StationAdapter(@Nullable List<Station> data) {
        super(data);
        Log.d(TAG, "Adapter 創建，數據筆數: " + (data != null ? data.size() : 0));  // ✅ Adapter 初始化時記錄數據長度
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: 建立新的 ViewHolder，viewType: " + viewType);  // ✅ 記錄 ViewHolder 創建狀況
        View view = LayoutInflater.from(context).inflate(R.layout.item_station, parent, false);
        return new QuickViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder holder, int position, @Nullable Station station) {
        if (station == null) {
            Log.e(TAG, "onBindViewHolder: station 為 null，位置: " + position);  // ❌ 可能出錯時的記錄
            return;
        }
        TextView stationIcon = holder.getView(R.id.tvStationIcon);

        if (station.getStationId().startsWith("R")) {
            stationIcon.setBackgroundResource(R.drawable.circle_red);
        } else if (station.getStationId().startsWith("O")) {
            stationIcon.setBackgroundResource(R.drawable.circle_orange);
        } else {
            stationIcon.setBackgroundResource(R.drawable.circle_background);  // 預設藍色
        }

        Log.d(TAG, "onBindViewHolder: 綁定數據 - 位置: " + position +
                ", 車站編號: " + station.getStationId() +
                ", 車站名稱: " + station.getStationNameZh());

        holder.setText(R.id.tvStationIcon, station.getStationId())
//                .setText(R.id.tvStationId, "車站編號: " + station.getStationId())
                .setText(R.id.tvStationName, "車站名稱: " + station.getStationNameZh())
                .setText(R.id.tvCoordinates, "緯度: " + station.getLatitude() + ", 經度: " + station.getLongitude());

        holder.itemView.setOnClickListener(v -> {
            if (onStationClickListener != null) {
                onStationClickListener.onStationClick(station);
            }
        });
    }

    public interface OnStationClickListener {
        void onStationClick(Station station);
    }


    public void setOnStationClickListener(OnStationClickListener listener) {
        this.onStationClickListener = listener;
    }

}

package com.example.krtc_informaction;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.krtc_informaction.R;
import com.example.krtc_informaction.Station;

import java.util.List;

public class StationAdapter extends android.widget.BaseAdapter {
    private final Context context;
    private List<Station> stationList;
    private OnStationClickListener onStationClickListener;
    private static final String TAG = "StationAdapter";  // ✅ Log 標籤

    public StationAdapter(Context context, List<Station> stationList) {
        this.context = context;
        this.stationList = stationList;
        Log.d(TAG, "Adapter 創建，數據筆數: " + (stationList != null ? stationList.size() : 0));  // ✅ Adapter 初始化時記錄數據長度
    }

    @Override
    public int getCount() {
        return stationList != null ? stationList.size() : 0;
    }

    @Override
    public Station getItem(int position) {
        return stationList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_station, parent, false);
            holder = new ViewHolder();
            holder.tvStationIcon = convertView.findViewById(R.id.tvStationIcon);
            holder.tvStationName = convertView.findViewById(R.id.tvStationName);
            holder.tvCoordinates = convertView.findViewById(R.id.tvCoordinates);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Station station = getItem(position);
        Log.d(TAG, "onBindViewHolder: 綁定數據 - 位置: " + position +
                ", 車站編號: " + station.getStationId() +
                ", 車站名稱: " + station.getStationNameZh());

        // ✅ 設定 icon 顏色
        if (station.getStationId().startsWith("R")) {
            holder.tvStationIcon.setBackgroundResource(R.drawable.circle_red);
        } else if (station.getStationId().startsWith("O")) {
            holder.tvStationIcon.setBackgroundResource(R.drawable.circle_orange);
        } else {
            holder.tvStationIcon.setBackgroundResource(R.drawable.circle_background);  // 預設藍色
        }

        // ✅ 設定資料
        holder.tvStationIcon.setText(station.getStationId());
        holder.tvStationName.setText("車站名稱: " + station.getStationNameZh());
        holder.tvCoordinates.setText("緯度: " + station.getLatitude() + ", 經度: " + station.getLongitude());

        // ✅ 設定點擊事件
        convertView.setOnClickListener(v -> {
            if (onStationClickListener != null) {
                onStationClickListener.onStationClick(station);
            }
        });

        return convertView;
    }

    public void updateData(List<Station> newStations) {
        this.stationList = newStations;
        notifyDataSetChanged();
    }

    public interface OnStationClickListener {
        void onStationClick(Station station);
    }

    public void setOnStationClickListener(OnStationClickListener listener) {
        this.onStationClickListener = listener;
    }

    static class ViewHolder {
        TextView tvStationIcon, tvStationName, tvCoordinates;
    }
}

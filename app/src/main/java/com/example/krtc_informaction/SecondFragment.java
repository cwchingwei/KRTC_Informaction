package com.example.krtc_informaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.krtc_informaction.base.BaseFragment;
import com.example.krtc_informaction.databinding.FragmentFirstBinding;
import com.example.krtc_informaction.databinding.FragmentSecondBinding;

public class SecondFragment extends BaseFragment<FragmentSecondBinding> {

    private double latitude;
    private double longitude;
    private String stationName;
    private static final int REQUEST_CODE_MAP = 1001;
    @Override
    protected void initialize() {
        // ✅ 取得 `FirstFragment` 傳遞的參數
        if (getArguments() != null) {
            stationName = getArguments().getString("station_name", "未知車站");
            latitude = getArguments().getDouble("latitude", 0.0);
            longitude = getArguments().getDouble("longitude", 0.0);

            // ✅ 更新 `TextView` 顯示車站名稱與座標
            binding.textviewStationName.setText("車站: " + stationName);
            binding.textviewCoordinates.setText("緯度: " + latitude + ", 經度: " + longitude);
        }

        // ✅ 點擊按鈕開啟 Google Maps
        binding.btnOpenMap.setOnClickListener(v -> openGoogleMaps());

        // ✅ 點擊按鈕返回 `FirstFragment`
        binding.buttonSecond.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigateUp(); // ✅ 返回上一個 Fragment
        });

        // ✅ 立即打開 Google Maps
        openGoogleMaps();
    }

    @Override
    protected FragmentSecondBinding createViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentSecondBinding.inflate(inflater, container, false);
    }

    private void openGoogleMaps() {
        if (latitude == 0.0 || longitude == 0.0) {
            showToast("座標錯誤，無法開啟 Google Maps");
            return;
        }

        Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude + "(" + stationName + ")");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapLauncher.launch(mapIntent);
    }

    private final ActivityResultLauncher<Intent> mapLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d("GoogleMaps", "使用者從 Google Maps 返回");
                NavController navController = NavHostFragment.findNavController(this);
                navController.navigateUp(); // ✅ 返回 FirstFragment
            });
}

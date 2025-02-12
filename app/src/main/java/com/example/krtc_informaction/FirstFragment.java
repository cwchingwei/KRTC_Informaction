package com.example.krtc_informaction;

import static com.example.krtc_informaction.api.ApiTransformer.applyBindingConsumer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.krtc_informaction.apapter.StationAdapter;
import com.example.krtc_informaction.api.ApiClientService;
import com.example.krtc_informaction.api.ApiService;
import com.example.krtc_informaction.base.BaseFragment;
import com.example.krtc_informaction.databinding.FragmentFirstBinding;
import com.example.krtc_informaction.model.Station;
import com.example.krtc_informaction.view_model.FabViewModel;
import com.example.krtc_informaction.view_model.StationViewModel;
import com.orhanobut.logger.Logger;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;


import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FirstFragment extends BaseFragment<FragmentFirstBinding> {

    private StationAdapter mAdapter;
    private StationViewModel stationViewModel;
    private FabViewModel fabViewModel;
    private boolean isFabVisible = false; // 記錄 FAB 是否顯示
    private static final int SCROLL_THRESHOLD = 50; // 設定 FAB 出現的滑動距離
    @Override
    protected FragmentFirstBinding createViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentFirstBinding.inflate(inflater, container, false);  // ✅ 由子類負責 ViewBinding
    }
//    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        binding.buttonFirst.setOnClickListener(v ->
//                NavHostFragment.findNavController(FirstFragment.this)
//                        .navigate(R.id.action_FirstFragment_to_SecondFragment)
//        );
//    }

    @Override
    public void onResume() {
        super.onResume();
        initialize();
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d("FirstFragment", "onStart");
    }

    @Override
    protected void initialize() {
        Log.d("FirstFragment", "initialize");
        stationViewModel = new ViewModelProvider(requireActivity()).get(StationViewModel.class);
        fabViewModel = new ViewModelProvider(requireActivity()).get(FabViewModel.class);

        mAdapter = new StationAdapter(new ArrayList<>());
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(mAdapter);

        stationViewModel.getStations().observe(getViewLifecycleOwner(), stations -> {
            if (!stations.isEmpty()) {
                Log.d("FirstFragment", "isEmpty");
                mAdapter.submitList(new ArrayList<>(stations)); // ✅ 確保是新列表，避免 DiffUtil 優化導致 UI 不更新
            }
        });

        // ✅ 預設隱藏 FAB
        fabViewModel.hideFab();

        // ✅ 監聽 RecyclerView 滑動事件
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > SCROLL_THRESHOLD && !isFabVisible) {
                    // ✅ 使用者往下滑，顯示 FAB
                    fabViewModel.showFab(v -> scrollToTop());
                    isFabVisible = true;
                } else if (dy < -SCROLL_THRESHOLD && isFabVisible) {
                    // ✅ 使用者往上滑回頂部，隱藏 FAB
                    fabViewModel.hideFab();
                    isFabVisible = false;
                }
            }
        });

        binding.smartRefresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                Log.d("SmartRefresh", "觸發下拉刷新");
                getStations(refreshlayout);
            }
        });

        mAdapter.setOnStationClickListener(station -> {
            Bundle bundle = new Bundle();
            bundle.putString("station_name", station.getStationNameZh());
            bundle.putDouble("latitude", station.getLatitude());
            bundle.putDouble("longitude", station.getLongitude());

            // 跳轉到 SecondFragment，並傳遞車站資訊
            NavController navController = NavHostFragment.findNavController(FirstFragment.this);
            navController.navigate(R.id.action_FirstFragment_to_SecondFragment, bundle);
        });

        // ✅ 讀取資料
        binding.btnLoadData.setOnClickListener(v -> {
            showToast("正在讀取資料...");
            getStations(binding.smartRefresh);
        });

        // ✅ 清除資料
        binding.btnClearData.setOnClickListener(v -> {
            showToast("資料已清除！");
            stationViewModel.setStations(new ArrayList<>());
            mAdapter.submitList(new ArrayList<>());
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("FirstFragment", "onDestroyView");
        binding = null;
        fabViewModel.hideFab();
    }

    @SuppressLint("CheckResult")
    private void getStations(@Nullable RefreshLayout refreshLayout) {
        if (refreshLayout != null) {
            refreshLayout.autoRefresh();  // ✅ 手動開始刷新動畫（可選）
        }
        sService.getStationData()
                .compose(applyBindingConsumer(this))
                .doOnSubscribe(disposable -> Log.d("API", "開始請求車站數據...")) // ✅ API 開始請求
                .onErrorReturn(throwable -> {
                    showToast("請求發生錯誤: " + throwable.getMessage());
                    Log.e("API", "請求發生錯誤: " + throwable.getMessage()); // ❌ 錯誤日誌
                    if (refreshLayout != null) {
                        refreshLayout.finishRefresh(false);  // ❌ 失敗時結束刷新
                    }
                    return new ArrayList<>();
                })
                .observeOn(Schedulers.io()) // ✅ 在背景執行
                .doOnNext(bean -> Log.d("API", "收到數據: " + bean.size() + " 筆")) // ✅ 接收數據
                .onErrorReturn(throwable -> {
                    showToast("請求發生錯誤: " + throwable.getMessage());
                    if (refreshLayout != null) {
                        refreshLayout.finishRefresh(false);  // ❌ 失敗時結束刷新
                    }
                    return new ArrayList<>();
                })
                .observeOn(AndroidSchedulers.mainThread()) // ✅ 切換到主線程
                .subscribe(beans -> {
                    if (mAdapter != null) {
                        mAdapter.submitList(beans);
                    }
                    stationViewModel.setStations(beans);  // ✅ 確保數據存入 ViewModel
                    if (refreshLayout != null) {
                        binding.smartRefresh.finishRefresh();  // ✅ 停止刷新動畫
                    }
                });
    }

    private void scrollToTop() {
        binding.recyclerView.smoothScrollToPosition(0);
    }
}
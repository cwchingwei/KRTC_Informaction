package com.example.krtc_informaction;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.krtc_informaction.databinding.FragmentFirstBinding;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class FirstFragment extends BaseFragment<FragmentFirstBinding> {

    private StationAdapter mAdapter;
    private StationViewModel stationViewModel;
    private FabViewModel fabViewModel;
    private boolean isFabVisible = false; // 記錄 FAB 是否顯示
    private List<Station> stationList = new ArrayList<>();
    private CompositeDisposable disposable = new CompositeDisposable();
    private static final int SCROLL_THRESHOLD = 50; // 設定 FAB 出現的滑動距離
    private static final String BASE_URL = "https://api.kcg.gov.tw/";
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build())
            .build();

    public interface ApiService {
        @GET("api/service/Get/4278fc6a-c3ea-4192-8ce0-40f00cdb40dd")
        Observable<Response<BaseVo<List<Station>>>> getStations();
    }
    private final ApiService apiService = retrofit.create(ApiService.class);

    @Override
    protected FragmentFirstBinding createViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentFirstBinding.inflate(inflater, container, false);  // ✅ 由子類負責 ViewBinding
    }

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

//        mAdapter = new StationAdapter(new ArrayList<>());
//        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        binding.recyclerView.setAdapter(mAdapter);

        mAdapter = new StationAdapter(requireContext(), stationList);
        binding.listView.setAdapter(mAdapter);

        stationViewModel.getStations().observe(getViewLifecycleOwner(), stations -> {
            if (!stations.isEmpty()) {
                Log.d("FirstFragment", "isEmpty");
                mAdapter.updateData(new ArrayList<>(stations)); // ✅ 確保是新列表，避免 DiffUtil 優化導致 UI 不更新
            }
        });

        // ✅ 預設隱藏 FAB
        fabViewModel.hideFab();

        // ✅ 監聽 RecyclerView 滑動事件
//        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//                if (dy > SCROLL_THRESHOLD && !isFabVisible) {
//                    // ✅ 使用者往下滑，顯示 FAB
//                    fabViewModel.showFab(v -> scrollToTop());
//                    isFabVisible = true;
//                } else if (dy < -SCROLL_THRESHOLD && isFabVisible) {
//                    // ✅ 使用者往上滑回頂部，隱藏 FAB
//                    fabViewModel.hideFab();
//                    isFabVisible = false;
//                }
//            }
//        });

        binding.smartRefresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                Log.d("SmartRefresh", "觸發下拉刷新");
                fetchStations(refreshlayout);
            }
        });

        mAdapter.setOnStationClickListener(station -> {
//            Bundle bundle = new Bundle();
//            bundle.putString("station_name", station.getStationNameZh());
//            bundle.putDouble("latitude", station.getLatitude());
//            bundle.putDouble("longitude", station.getLongitude());
//
//            // 跳轉到 SecondFragment，並傳遞車站資訊
//            NavController navController = NavHostFragment.findNavController(FirstFragment.this);
//            navController.navigate(R.id.action_FirstFragment_to_SecondFragment, bundle);

            Uri gmmIntentUri = Uri.parse("geo:" + station.getLatitude() + "," + station.getLongitude() + "?q=" + station.getLatitude() + "," + station.getLongitude() + "(" + station.getStationNameZh() + ")");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapLauncher.launch(mapIntent);
        });

        // ✅ 讀取資料
        binding.btnLoadData.setOnClickListener(v -> {
            showToast("正在讀取資料...");
            fetchStations(binding.smartRefresh);
        });

        // ✅ 清除資料
        binding.btnClearData.setOnClickListener(v -> {
            showToast("資料已清除！");
            stationViewModel.setStations(new ArrayList<>());
            mAdapter.updateData(new ArrayList<>());
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        fabViewModel.hideFab();
    }

//    private void scrollToTop() {
//        binding.recyclerView.smoothScrollToPosition(0);
//    }

    private void fetchStations(@Nullable RefreshLayout refreshLayout) {
        if (refreshLayout != null) {
            refreshLayout.autoRefresh();  // ✅ 手動開始刷新動畫（可選）
        }
        disposable.add(apiService.getStations()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (response.isSuccessful() && response.body() != null) {
                        return response.body().getData();
                    }
                    showToast("API 回應錯誤");
                    if (refreshLayout != null) {
                        refreshLayout.finishRefresh(false);  // ❌ 失敗時結束刷新
                    }
                    throw new RuntimeException("API 回應錯誤");
                })
                .subscribeWith(new DisposableObserver<List<Station>>() {
                    @Override
                    public void onNext(List<Station> stations) {
                        Log.d("API", "獲取到 " + stations.size() + " 個站點");
                        mAdapter.updateData(stations);
                        if (refreshLayout != null) {
                            binding.smartRefresh.finishRefresh();  // ✅ 停止刷新動畫
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("API_ERROR", "請求失敗：" + e.getMessage());
                        showToast("請求發生錯誤: " + e.getMessage());
                        if (refreshLayout != null) {
                            refreshLayout.finishRefresh(false);  // ❌ 失敗時結束刷新
                        }
                    }

                    @Override
                    public void onComplete() {
                        Log.d("API", "請求完成");
                        if (refreshLayout != null) {
                            binding.smartRefresh.finishRefresh();  // ✅ 停止刷新動畫
                        }
                    }
                }));
    }

    private final ActivityResultLauncher<Intent> mapLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d("GoogleMaps", "使用者從 Google Maps 返回");
                NavController navController = NavHostFragment.findNavController(this);
                navController.navigateUp(); // ✅ 返回 FirstFragment
            });
}
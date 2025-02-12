package com.example.krtc_informaction.base;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.viewbinding.ViewBinding;

import com.example.krtc_informaction.api.ApiStoreService;
import com.orhanobut.logger.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public abstract class BaseFragment<VB extends ViewBinding> extends Fragment {
    protected static final ApiStoreService sService = ApiStoreService.initialize();
    protected VB binding;  // ✅ ViewBinding 變數
    private CompositeDisposable mCompositeDisposable;
    private Toast mToast;
    private boolean isLazyLoad = false;
    private boolean isPrepared = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = createViewBinding(inflater, container);  // ✅ 讓子類提供 ViewBinding
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        isPrepared = true;
        applyVisible();
    }

    protected abstract void initialize();

    protected abstract VB createViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container);

    private void applyVisible() {
        if (getUserVisibleHint() && isPrepared && !isLazyLoad) {
            initialize();

            isLazyLoad = true;
        }
    }
    @Override
    public void onDetach() {
        // 取消注册，以避免内存泄露
        if (mCompositeDisposable != null) {
            mCompositeDisposable.dispose();
        }

        isLazyLoad = false;
        isPrepared = false;
        super.onDetach();
    }

    protected <T> void applySubscribe(Observable<T> observable, DisposableObserver<T> observer) {
        if (mCompositeDisposable == null) {
            mCompositeDisposable = new CompositeDisposable();
        }

        mCompositeDisposable.add(observer);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    protected void applySubscribe(Disposable disposable) {
        if (mCompositeDisposable == null) {
            mCompositeDisposable = new CompositeDisposable();
        }

        mCompositeDisposable.add(disposable);
    }
    public void applyClientStatus(int code) {

        switch (code) {
            case 200:
                Log.d("applyClientStatus", "請求成功");
                break;

            case 400:
                Log.d("applyClientStatus", "請求錯誤 (400)");
                showToast("請求錯誤 (400): 請檢查您的請求內容。");
                break;

            case 401:
                Log.d("applyClientStatus", "未授權 (401)");
                showToast("未授權 (401): 您的身份驗證已過期，請重新登入。");
                break;

            case 403:
                Log.d("applyClientStatus", "禁止訪問 (403)");
                showToast("禁止訪問 (403): 您無權存取此資源。");
                break;

            case 404:
                Log.d("applyClientStatus", "未找到 (404)");
                showToast("未找到 (404): 請求的資源不存在。");
                break;

            case 500:
                Log.d("applyClientStatus", "伺服器錯誤 (500)");
                showToast("伺服器錯誤 (500): 伺服器內部發生錯誤，請稍後再試。");
                break;

            case 503:
                Log.d("applyClientStatus", "服務不可用 (503)");
                showToast("服務不可用 (503): 伺服器暫時無法處理請求，請稍後再試。");
                break;

            default:
                Log.d("applyClientStatus", "未知錯誤 (" + code + ")");
                showToast("未知錯誤 (" + code + "): 發生未知錯誤，請聯繫技術支援。");
                break;
        }
    }

    protected void showToast(String message) {
        if (getActivity() == null || !isAdded()) {
            Log.e("showToast", "Fragment 未附加到 Activity，無法顯示 Toast");
            return;
        }

        requireActivity().runOnUiThread(() -> {
            if (mToast != null) {
                mToast.cancel(); // ✅ 先取消舊的 Toast，防止內容被覆蓋
            }
            mToast = Toast.makeText(requireContext(), message, Toast.LENGTH_LONG);
            mToast.show();
        });
    }
}
package com.example.krtc_informaction.api;

import com.example.krtc_informaction.base.BaseFragment;
import com.example.krtc_informaction.model.BaseVo;

import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ApiTransformer {


    public static <T> ObservableTransformer<BaseVo<T>, T> applyBindingConsumer(BaseFragment fragment) {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(vo -> fragment.applyClientStatus(vo.getCode()))
                .filter(vo -> vo.getCode() == 200)
                .map(BaseVo::getData)
                .observeOn(AndroidSchedulers.mainThread());
    }
}
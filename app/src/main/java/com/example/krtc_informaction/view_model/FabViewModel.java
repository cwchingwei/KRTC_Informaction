package com.example.krtc_informaction.view_model;

import android.view.View;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FabViewModel extends ViewModel {
    private final MutableLiveData<Boolean> fabVisibility = new MutableLiveData<>(false);
    private final MutableLiveData<View.OnClickListener> fabClickListener = new MutableLiveData<>();

    public LiveData<Boolean> getFabVisibility() {
        return fabVisibility;
    }

    public LiveData<View.OnClickListener> getFabClickListener() {
        return fabClickListener;
    }

    public void showFab(View.OnClickListener listener) {
        fabVisibility.setValue(true);
        fabClickListener.setValue(listener);
    }

    public void hideFab() {
        fabVisibility.setValue(false);
        fabClickListener.setValue(null);
    }
}


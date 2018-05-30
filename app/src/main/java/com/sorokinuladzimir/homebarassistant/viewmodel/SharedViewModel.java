package com.sorokinuladzimir.homebarassistant.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;


public class SharedViewModel extends ViewModel {

    private final MediatorLiveData<List<Long>> selectedIds = new MediatorLiveData<>();

    public LiveData<List<Long>> getSelectedIds() {
        return selectedIds;
    }

    public void selectIds(List<Long> ingredients) {
        selectedIds.setValue(ingredients);
    }

}

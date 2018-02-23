package sorokinuladzimir.com.homebarassistant.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;

public class SharedViewModel extends ViewModel {

    private final MediatorLiveData<List<Ingredient>> selected = new MediatorLiveData<>();

    private final MediatorLiveData<List<Long>> selectedIds = new MediatorLiveData<>();

    public void select(List<Ingredient> ingredients) {
        selected.setValue(ingredients);
    }

    public LiveData<List<Ingredient>> getSelected() {
        return selected;
    }

    public LiveData<List<Long>> getSelectedIds() {
        return selectedIds;
    }

    public void selectIds(List<Long> ingredients) {
        selectedIds.setValue(ingredients);
    }
}

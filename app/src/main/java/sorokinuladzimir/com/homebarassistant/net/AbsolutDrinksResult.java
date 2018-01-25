package sorokinuladzimir.com.homebarassistant.net;

import java.util.ArrayList;

import sorokinuladzimir.com.homebarassistant.net.entity.DrinkEntity;

/**
 * Created by sorok on 27.09.2017.
 */

public class AbsolutDrinksResult {
    private ArrayList<DrinkEntity> result;
    private int totalResult;
    private String next;

    public ArrayList<DrinkEntity> getResult() {
        return result;
    }

    public int getTotalResult() {
        return totalResult;
    }

    public String getNext() {
        return next;
    }

    public void setResult(ArrayList<DrinkEntity> result) {
        this.result = result;
    }

    public void setTotalResult(int totalResult) {
        this.totalResult = totalResult;
    }

    public void setNext(String next) {
        this.next = next;
    }
}

package sorokinuladzimir.com.homebarassistant.net;

import java.util.ArrayList;

import sorokinuladzimir.com.homebarassistant.db.entity.Drink;

/**
 * Created by sorok on 27.09.2017.
 */

public class AbsolutDrinksResult {
    private ArrayList<Drink> result;
    private int totalResult;
    private String next;

    public ArrayList<Drink> getResult() {
        return result;
    }

    public int getTotalResult() {
        return totalResult;
    }

    public String getNext() {
        return next;
    }

    public void setResult(ArrayList<Drink> result) {
        this.result = result;
    }

    public void setTotalResult(int totalResult) {
        this.totalResult = totalResult;
    }

    public void setNext(String next) {
        this.next = next;
    }
}

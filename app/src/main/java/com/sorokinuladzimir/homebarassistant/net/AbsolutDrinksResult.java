package com.sorokinuladzimir.homebarassistant.net;

import com.sorokinuladzimir.homebarassistant.net.entity.DrinkEntity;

import java.util.List;

public class AbsolutDrinksResult {
    private List<DrinkEntity> result;
    private int totalResult;
    private String next;

    public List<DrinkEntity> getResult() {
        return result;
    }

    public void setResult(List<DrinkEntity> result) {
        this.result = result;
    }

    public int getTotalResult() {
        return totalResult;
    }

    public void setTotalResult(int totalResult) {
        this.totalResult = totalResult;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }
}

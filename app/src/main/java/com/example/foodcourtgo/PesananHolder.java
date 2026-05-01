package com.example.foodcourtgo;

import java.util.ArrayList;
import java.util.List;

public class PesananHolder {
    private static List<PesananItem> pesananList = new ArrayList<>();

    public static List<PesananItem> getPesananList() {
        return pesananList;
    }

    public static void addItem(PesananItem item) {
        pesananList.add(item);
    }

    public static void clear() {
        pesananList.clear();
    }
}
package com.example.foodcourtgo.model;

import java.util.ArrayList;
import java.util.List;

public class CartHolder {
    private static List<CartItem> cartList = new ArrayList<>();

    public static List<CartItem> getCartList() {
        return cartList;
    }

    public static void addItem(CartItem item) {
        // Cek apakah item dengan menuId dan opsi yang sama sudah ada
        for (int i = 0; i < cartList.size(); i++) {
            CartItem existing = cartList.get(i);
            if (existing.getMenuId().equals(item.getMenuId()) &&
                    (existing.getOpsi() == null ? item.getOpsi() == null : existing.getOpsi().equals(item.getOpsi()))) {
                // Jika sudah ada, tambah qty
                existing.setQty(existing.getQty() + item.getQty());
                return;
            }
        }
        cartList.add(item);
    }

    public static void updateItem(int position, CartItem item) {
        if (position >= 0 && position < cartList.size()) {
            cartList.set(position, item);
        }
    }

    public static void removeItem(int position) {
        if (position >= 0 && position < cartList.size()) {
            cartList.remove(position);
        }
    }

    public static void clear() {
        cartList.clear();
    }

    public static long getTotalHarga() {
        long total = 0;
        for (CartItem item : cartList) {
            total += item.getTotalHarga();
        }
        return total;
    }

    public static int getTotalItemCount() {
        int count = 0;
        for (CartItem item : cartList) {
            count += item.getQty();
        }
        return count;
    }
}
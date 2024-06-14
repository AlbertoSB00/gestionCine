package com.mobilepulse.gestioncine.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.mobilepulse.gestioncine.R;

/**
 * Fragmento para mostrar información sobre los descuentos.
 */
public class DiscountFragment extends Fragment {

    /**
     * Constructor público de la clase DiscountFragment.
     */
    public DiscountFragment() {

    }

    /**
     * Crea una nueva instancia de DiscountFragment.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * Crea el fragmento.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discount, container, false);
    }
}
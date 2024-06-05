package com.mobilepulse.gestioncine.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mobilepulse.gestioncine.R;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "cine_prefs";
    private static final String PREF_NOTIFICATIONS = "notifications";
    private static final String PREF_LANGUAGE = "language";

    private Switch switchNotifications;
    private Spinner spinnerLanguage;
    private Button buttonSaveSettings;

    public SettingsFragment() {
        // Constructor vacío
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar vistas
        switchNotifications = view.findViewById(R.id.switchNotifications);
        spinnerLanguage = view.findViewById(R.id.spinnerLanguage);
        buttonSaveSettings = view.findViewById(R.id.buttonSaveSettings);

        // Cargar configuraciones
        loadSettings();

        buttonSaveSettings.setOnClickListener(v -> saveSettings());
    }

    private void loadSettings() {
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        switchNotifications.setChecked(prefs.getBoolean(PREF_NOTIFICATIONS, false));

        int languageIndex = prefs.getInt(PREF_LANGUAGE, 0);
        spinnerLanguage.setSelection(languageIndex);
    }

    private void saveSettings() {
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean(PREF_NOTIFICATIONS, switchNotifications.isChecked());
        editor.putInt(PREF_LANGUAGE, spinnerLanguage.getSelectedItemPosition());

        editor.apply();

        // Aplicar el idioma seleccionado
        applyLanguage(spinnerLanguage.getSelectedItemPosition());

        // Mostrar un mensaje de confirmación
        Toast.makeText(getActivity(), "Configuraciones guardadas", Toast.LENGTH_SHORT).show();
    }

    private void applyLanguage(int languageIndex) {
        String[] languageCodes = {"es", "en", "fr", "de"};
        String selectedLanguage = languageCodes[languageIndex];

        // Guardar el idioma seleccionado
        SharedPreferences prefs = getActivity().getSharedPreferences("cine_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("language", languageIndex);
        editor.apply();

        // Aplicar el idioma seleccionado
        Locale locale = new Locale(selectedLanguage);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        // Recrear la actividad para aplicar el nuevo idioma
        getActivity().recreate();
    }
}
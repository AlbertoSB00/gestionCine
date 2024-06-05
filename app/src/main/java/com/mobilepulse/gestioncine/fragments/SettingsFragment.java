package com.mobilepulse.gestioncine.fragments;

import android.content.Context;
import android.content.SharedPreferences;
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

public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "cine_prefs";
    private static final String PREF_NOTIFICATIONS = "notifications";
    private static final String PREF_LANGUAGE = "language";
    private static final String PREF_THEME = "theme";

    private Switch switchNotifications;
    private Spinner spinnerLanguage, spinnerTheme;
    private Button buttonChangeAccount, buttonSaveSettings;

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
        spinnerTheme = view.findViewById(R.id.spinnerTheme);
        buttonChangeAccount = view.findViewById(R.id.buttonChangeAccount);
        buttonSaveSettings = view.findViewById(R.id.buttonSaveSettings);

        // Cargar configuraciones
        loadSettings();

        // Configurar listeners para los botones
        buttonChangeAccount.setOnClickListener(v -> {
            // Acción para cambiar la información de la cuenta
            Toast.makeText(getActivity(), "Cambiar información de cuenta", Toast.LENGTH_SHORT).show();
        });

        buttonSaveSettings.setOnClickListener(v -> saveSettings());
    }

    private void loadSettings() {
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        switchNotifications.setChecked(prefs.getBoolean(PREF_NOTIFICATIONS, false));

        int languageIndex = prefs.getInt(PREF_LANGUAGE, 0);
        spinnerLanguage.setSelection(languageIndex);

        int themeIndex = prefs.getInt(PREF_THEME, 0);
        spinnerTheme.setSelection(themeIndex);
    }

    private void saveSettings() {
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean(PREF_NOTIFICATIONS, switchNotifications.isChecked());
        editor.putInt(PREF_LANGUAGE, spinnerLanguage.getSelectedItemPosition());
        editor.putInt(PREF_THEME, spinnerTheme.getSelectedItemPosition());

        editor.apply();

        // Mostrar un mensaje de confirmación
        Toast.makeText(getActivity(), "Configuraciones guardadas", Toast.LENGTH_SHORT).show();
    }
}
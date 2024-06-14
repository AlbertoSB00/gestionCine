package com.mobilepulse.gestioncine.fragments;

import android.annotation.SuppressLint;
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

/**
 * Fragmento para gestionar la configuración de la aplicación.
 */
public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "cine_prefs";
    private static final String PREF_NOTIFICATIONS = "notifications";
    private static final String PREF_LANGUAGE = "language";

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchNotifications;
    private Spinner spinnerLanguage;

    /**
     * Constructor público de la clase SettingsFragment.
     */
    public SettingsFragment() {
        // Constructor vacío
    }

    /**
     * Método llamado cuando se crea el fragmento.
     *
     * @param savedInstanceState El Bundle que contiene el estado anteriormente guardado del fragmento.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Método llamado para crear la vista del fragmento.
     *
     * @param inflater           El LayoutInflater objeto que se puede utilizar para inflar cualquier vista en
     *                           el fragmento.
     * @param container          Si no es nulo, este es el ViewGroup al que se debe adjuntar la vista de fragmento.
     * @param savedInstanceState Si no es nulo, este fragmento es una reanudación de un estado guardado anteriormente.
     * @return La View para el fragmento, simplemente inflada según el archivo de diseño definido en XML.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    /**
     * Método llamado después de que la vista del fragmento ha sido creada.
     *
     * @param view               La vista raíz del fragmento.
     * @param savedInstanceState Si no es nulo, este fragmento es una reanudación de un estado guardado anteriormente.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar vistas
        switchNotifications = view.findViewById(R.id.switchNotifications);
        spinnerLanguage = view.findViewById(R.id.spinnerLanguage);
        Button buttonSaveSettings = view.findViewById(R.id.buttonSaveSettings);

        // Cargar configuraciones
        loadSettings();

        buttonSaveSettings.setOnClickListener(v -> saveSettings());
    }

    /**
     * Método para cargar la configuración guardada.
     */
    private void loadSettings() {
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        switchNotifications.setChecked(prefs.getBoolean(PREF_NOTIFICATIONS, false));

        int languageIndex = prefs.getInt(PREF_LANGUAGE, 0);
        spinnerLanguage.setSelection(languageIndex);
    }

    /**
     * Método para guardar la configuración actual.
     */
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

    /**
     * Método para aplicar el idioma seleccionado.
     *
     * @param languageIndex El índice del idioma seleccionado en el spinner.
     */
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
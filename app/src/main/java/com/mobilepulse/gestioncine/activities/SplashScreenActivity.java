package com.mobilepulse.gestioncine.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.mobilepulse.gestioncine.databinding.ActivitySplashScreenBinding;

/**
 * Actividad de SplashScreen que muestra una animación de fundido al iniciar la aplicación.
 * Cuando la animación termina, la actividad LoginActivity se inicia y la actividad SplashScreen se cierra.
 */
@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {

    private View mContentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySplashScreenBinding binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtener la vista principal
        mContentView = binding.getRoot();

        // Iniciar la animación
        fadeIn();
    }

    /**
     * Aplica una animación de fundido a la vista principal.
     * Cuando la animación termina, inicia la siguiente actividad (LoginActivity) y cierra esta actividad.
     */
    private void fadeIn() {
        // Aplicar la animación de fundido a la vista principal
        mContentView.setAlpha(0f);
        mContentView.animate().alpha(1f).setDuration(1500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Iniciar la siguiente actividad cuando termine la animación
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });
    }
}

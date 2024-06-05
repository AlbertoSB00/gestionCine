package com.mobilepulse.gestioncine.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.mobilepulse.gestioncine.R;
import com.mobilepulse.gestioncine.fragments.CommentFragment;
import com.mobilepulse.gestioncine.fragments.DiscountFragment;
import com.mobilepulse.gestioncine.fragments.HomeFragment;
import com.mobilepulse.gestioncine.fragments.MovieFragment;
import com.mobilepulse.gestioncine.fragments.ReserveFragment;
import com.mobilepulse.gestioncine.fragments.SettingsFragment;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Cargar el lenguaje guardado por el usuario.
        loadLanguagePreference();

        // Obteniendo el correo de la actividad anterior.
        Intent intent = getIntent();
        String correo = intent.getStringExtra("CORREO");

        // Asignando el correo a la barra de navegación.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Asignando el correo a la barra de navegación.
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        TextView userEmailTextView = headerView.findViewById(R.id.user_email_textview);
        userEmailTextView.setText(correo);

        // Asignando el correo a los items de la barra de navegación.
        MenuItem login = navigationView.getMenu().findItem(R.id.nav_login);
        MenuItem register = navigationView.getMenu().findItem(R.id.nav_register);
        MenuItem logout = navigationView.getMenu().findItem(R.id.nav_logout);
        MenuItem admin = navigationView.getMenu().findItem(R.id.nav_admin);

        // Mostrando el correo y botones de acceso a la barra de navegación.
        if (!userEmailTextView.getText().toString().isEmpty()) {
            login.setVisible(false);
            register.setVisible(false);
            admin.setVisible(false);
            logout.setVisible(true);
        } else {
            login.setVisible(true);
            register.setVisible(true);
            admin.setVisible(true);
            logout.setVisible(false);
        }

        // Asignando el comportamiento de los items de la barra de navegación.
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Asignando el comportamiento de los items de la barra de navegación.
        if (savedInstanceState == null) {
            Bundle bundle = new Bundle();
            bundle.putString("CORREO", correo);

            HomeFragment homeFragment = new HomeFragment();
            homeFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, homeFragment)
                    .commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        // Asignando el comportamiento de los items de la barra de navegación.
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    // Si no hay ningún fragmento en el stack, cerrar la actividad
                    if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                        finish();
                    } else {
                        // Si hay fragmentos en el stack, pop el último fragmento
                        getSupportFragmentManager().popBackStack();
                    }
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    // Asignando el comportamiento de los items de la barra de navegación.
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();

        if( itemId == R.id.nav_home ) {
            Intent intent = getIntent();
            String correo = intent.getStringExtra("CORREO");

            Bundle bundle = new Bundle();
            bundle.putString("CORREO", correo);

            HomeFragment homeFragment = new HomeFragment();
            homeFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, homeFragment)
                    .commit();

        } else if( itemId == R.id.nav_movie ) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MovieFragment()).commit();

        }else if( itemId == R.id.nav_reserve ) {
            Intent intent = getIntent();
            String correo = intent.getStringExtra("CORREO");

            Bundle bundle = new Bundle();
            bundle.putString("CORREO", correo);

            ReserveFragment reserveFragment = new ReserveFragment();
            reserveFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, reserveFragment)
                    .commit();

        }else if(itemId == R.id.nav_comment) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CommentFragment()).commit();

        }else if(itemId == R.id.nav_discount) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DiscountFragment()).commit();

        }else if( itemId == R.id.nav_settings ) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();

        }else if( itemId == R.id.nav_login ) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);

        }else if( itemId == R.id.nav_register ) {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);

        } else if(itemId == R.id.nav_admin) {
            Intent intent = new Intent(MainActivity.this, LoginAdminActivity.class);
            startActivity(intent);

        }else if( itemId == R.id.nav_logout ) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadLanguagePreference() {
        SharedPreferences prefs = getSharedPreferences("cine_prefs", Context.MODE_PRIVATE);
        int languageIndex = prefs.getInt("language", 0);

        String[] languageCodes = {"es", "en", "fr", "de"};
        String selectedLanguage = languageCodes[languageIndex];

        Locale locale = new Locale(selectedLanguage);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}
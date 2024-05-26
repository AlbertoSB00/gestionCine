package com.mobilepulse.gestioncine.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mobilepulse.gestioncine.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private static final String IP = "192.168.0.108";
    private static final int PORT = 12345;

    private EditText campoName;
    private EditText campoSurname;
    private EditText campoUser;
    private EditText campoPassword;
    private EditText campoRepitePassword;
    private EditText campoFecha;
    private CheckBox campoConsentimiento;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Asignando los campos.
        campoName = findViewById(R.id.campoName);
        campoSurname = findViewById(R.id.campoSurname);
        campoUser = findViewById(R.id.campoUser);
        campoPassword = findViewById(R.id.campoPassword);
        campoRepitePassword = findViewById(R.id.campoRepitePassword);
        campoFecha = findViewById(R.id.campoFecha);
        campoConsentimiento = findViewById(R.id.campoConsentimiento);
        Button botonSiguiente = findViewById(R.id.botonSiguiente);

        // Añadir TextWatcher al campo de fecha.
        campoFecha.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            private int previousLength;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previousLength = s.length();
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // Verificar si estamos en el proceso de formateo.
                if (isFormatting) {
                    return;
                }

                int currentLength = s.length();
                isFormatting = true;

                // Formatear el texto si se agrega un nuevo carácter.
                if (currentLength > previousLength && (currentLength == 2 || currentLength == 5)) {
                    campoFecha.setText(s + "/");
                    campoFecha.setSelection(campoFecha.getText().length());
                }

                // Eliminar la barra diagonal si se borra un carácter.
                else if (currentLength < previousLength && (currentLength == 2 || currentLength == 5)) {
                    campoFecha.setText(s.toString().substring(0, s.length() - 1));
                    campoFecha.setSelection(campoFecha.getText().length());
                }

                isFormatting = false;
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Nada que hacer aquí.
            }
        });

        // Creando el executor.
        executorService = Executors.newSingleThreadExecutor();

        // Al pulsar "Siguiente".
        botonSiguiente.setOnClickListener(v -> {
            String name = campoName.getText().toString();
            String surname = campoSurname.getText().toString();
            String email = campoUser.getText().toString();
            String password = campoPassword.getText().toString();
            String repitePassword = campoRepitePassword.getText().toString();
            String passwordHashed = cifrarPassword(password);
            String birthdate = campoFecha.getText().toString();
            boolean consentimiento = campoConsentimiento.isChecked();

            // Validamos campos.
            if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty() || repitePassword.isEmpty() || birthdate.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Por favor, rellene todos los campos.", Toast.LENGTH_SHORT).show();
                return;

            } else if (!password.equals(repitePassword)) {
                Toast.makeText(RegisterActivity.this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show();
                return;

            } else if (!consentimiento) {
                Toast.makeText(RegisterActivity.this, "Por favor, acepte los términos y la política de privacidad.", Toast.LENGTH_SHORT).show();
                return;

            } else if (!isValidDate(birthdate)) {
                Toast.makeText(RegisterActivity.this, "La fecha de nacimiento es inválida.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Enviamos orden al servidor.
            ordenServer(name, surname, email, passwordHashed, birthdate);
        });
    }

    // Método para enviar orden al servidor.
    private void ordenServer(String name, String surname, String email, String passwordHashed, String birthdate) {
        executorService.execute(() -> {
            String response = authenticationTask("REGISTER", name, surname, email, passwordHashed, birthdate);

            runOnUiThread(() -> {
                switch (response) {
                    case "REGISTER_SUCCESS":
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.putExtra("CORREO", campoUser.getText().toString());
                        startActivity(intent);
                        break;

                    case "REGISTER_FAILED":
                        Toast.makeText(RegisterActivity.this, "Algo ha ido mal...", Toast.LENGTH_SHORT).show();
                        break;

                    case "NOT_VALID_EMAIL":
                        Toast.makeText(RegisterActivity.this, "Correo no válido", Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        Toast.makeText(RegisterActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                        break;
                }
            });
        });
    }

    // Método para manejar la respuesta del servidor.
    private String authenticationTask(String... params) {
        String response;
        try {
            Socket socket = new Socket(IP, PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Enviamos orden al servidor.
            out.println(params[0]);

            // Enviamos credenciales al servidor.
            out.println(params[1]);
            out.println(params[2]);
            out.println(params[3]);
            out.println(params[4]);
            out.println(params[5]);

            // Leemos respuesta.
            response = in.readLine();

            // Cerramos el socket.
            out.close();
            in.close();
            socket.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return response;
    }

    // Método para cifrar la contraseña usando SHA-256
    public String cifrarPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());

            // Convertir el hash en formato hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

// Método para validar la fecha de nacimiento
public boolean isValidDate(String date) {
    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    sdf.setLenient(false);
    try {
        Date parsedDate = sdf.parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(parsedDate);

        // Verificar si el día es válido para el mes y el año.
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH) + 1; // Se suma 1 porque los meses empiezan en 0.
        int year = cal.get(Calendar.YEAR);

        // Verificar febrero y años bisiestos.
        if (month == 2) {
            if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) {
                return day <= 29;
            } else {
                return day <= 28;
            }
        }

        // Verificar meses con 30 días.
        if (month == 4 || month == 6 || month == 9 || month == 11) {
            return day <= 30;
        }

        return true; // Resto de los meses.
    } catch (ParseException e) {
        return false; // La fecha no se pudo parsear correctamente.
    }
}

    // Metodo para cerrar el executor.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}

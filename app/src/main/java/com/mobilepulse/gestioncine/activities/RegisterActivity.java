package com.mobilepulse.gestioncine.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mobilepulse.gestioncine.R;
import com.mobilepulse.gestioncine.classes.Configuration;

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

    private static final String IP = Configuration.IP;
    private static final int PORT = Configuration.PORT;

    private EditText campoName;
    private EditText campoUser;
    private EditText campoPassword;
    private EditText campoRepitePassword;
    private DatePickerDialog datePickerDialog;
    private Button campoFecha;
    private CheckBox campoConsentimiento;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Asignando los campos.
        campoName = findViewById(R.id.campoName);
        campoUser = findViewById(R.id.campoUser);
        campoPassword = findViewById(R.id.campoPassword);
        campoRepitePassword = findViewById(R.id.campoRepitePassword);
        campoFecha = findViewById(R.id.campoFecha);
        campoConsentimiento = findViewById(R.id.campoConsentimiento);
        Button botonSiguiente = findViewById(R.id.botonSiguiente);

        // DatePicker.
        initDatePicker();

        // Creando el executor.
        executorService = Executors.newSingleThreadExecutor();

        // Mostrar el DatePicker al hacer clic en el botón de fecha.
        campoFecha.setOnClickListener(v -> openDatePicker());

        // Al pulsar "Siguiente".
        botonSiguiente.setOnClickListener(v -> {
            String name = campoName.getText().toString();
            String email = campoUser.getText().toString();
            String password = campoPassword.getText().toString();
            String repitePassword = campoRepitePassword.getText().toString();
            String passwordHashed = cifrarPassword(password);
            String birthdate = campoFecha.getText().toString();
            boolean consentimiento = campoConsentimiento.isChecked();

            // Validamos campos.
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || repitePassword.isEmpty() || birthdate.isEmpty()) {
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
            ordenServer(name, email, passwordHashed, birthdate);
        });
    }

    private void initDatePicker(){
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            month = month + 1;
            String date = dayOfMonth + "/" + month + "/" + year;
            campoFecha.setText(date);
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;
        datePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);

        // Establecer la fecha máxima en hoy
        datePickerDialog.getDatePicker().setMaxDate(cal.getTimeInMillis());
    }

    public void openDatePicker() {
        datePickerDialog.show();
    }

    // Método para enviar orden al servidor.
    private void ordenServer(String name, String email, String passwordHashed, String birthdate) {
        executorService.execute(() -> {
            String response = authenticationTask("REGISTER", name, email, passwordHashed, birthdate);

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

    // Método para manejar la respuesta del servidor.
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

    // Método para cerrar el executor.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
package com.mobilepulse.gestioncine.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mobilepulse.gestioncine.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PaymentActivity extends AppCompatActivity {

    private static final String IP = "192.168.0.108";
    private static final int PORT = 12345;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Handler handler = new Handler(Looper.getMainLooper());

    private Spinner spinnerPaymentMethod;
    private LinearLayout layoutCreditCard, layoutPaypal, layoutBizum, layoutCashDesk;
    private int idUsuario, idPelicula, butacasReservadas;
    private String estadoReserva, metodoPago, sala, hora;
    private double totalPagar;
    private EditText emailFactura;

    // Campos de tarjeta de crédito
    private EditText creditCardNumber, creditCardName, creditCardExpiration, creditCardCVV;
    // Campos de PayPal
    private EditText paypalEmail, paypalPassword;
    // Campos de Bizum
    private EditText bizumPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Obtener los datos del intent
        Intent intent = getIntent();
        idUsuario = intent.getIntExtra("id_usuario", -1);
        idPelicula = intent.getIntExtra("id_pelicula", -1);
        sala = intent.getStringExtra("sala");
        hora = intent.getStringExtra("hora");
        estadoReserva = intent.getStringExtra("estado_reserva");
        butacasReservadas = intent.getIntExtra("butacas_reservadas", 0);
        totalPagar = intent.getDoubleExtra("total_pagar", 0.0);

        // Inicializar las vistas
        emailFactura = findViewById(R.id.editTextEmailResguardo);
        spinnerPaymentMethod = findViewById(R.id.spinnerPaymentMethod);
        layoutCreditCard = findViewById(R.id.layoutCreditCard);
        layoutPaypal = findViewById(R.id.layoutPaypal);
        layoutBizum = findViewById(R.id.layoutBizum);
        layoutCashDesk = findViewById(R.id.layoutCashDesk);
        Button buttonConfirmPayment = findViewById(R.id.buttonConfirmPayment);

        // Inicializar campos específicos de métodos de pago
        creditCardNumber = findViewById(R.id.editTextCardNumber);
        creditCardName = findViewById(R.id.editTextCardNumber);
        creditCardExpiration = findViewById(R.id.editTextCardExpiry);
        creditCardCVV = findViewById(R.id.editTextCardCVV);
        paypalEmail = findViewById(R.id.editTextPaypalEmail);
        paypalPassword = findViewById(R.id.editTextPaypalPassword);
        bizumPhone = findViewById(R.id.editTextBizumPhone);

        // Configurar el listener del spinner
        spinnerPaymentMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: // Tarjeta de Crédito
                        mostrarLayout(layoutCreditCard);
                        ocultarLayouts(layoutPaypal, layoutBizum, layoutCashDesk);
                        break;
                    case 1: // PayPal
                        mostrarLayout(layoutPaypal);
                        ocultarLayouts(layoutCreditCard, layoutBizum, layoutCashDesk);
                        break;
                    case 2: // Bizum
                        mostrarLayout(layoutBizum);
                        ocultarLayouts(layoutCreditCard, layoutPaypal, layoutCashDesk);
                        break;
                    case 3: // Pago en Taquilla
                        mostrarLayout(layoutCashDesk);
                        ocultarLayouts(layoutCreditCard, layoutPaypal, layoutBizum);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });

        buttonConfirmPayment.setOnClickListener(v -> {
            metodoPago = spinnerPaymentMethod.getSelectedItem().toString().trim();

            // Validar los campos antes de enviar la orden
            if (!validarCampos()) {
                mostrarMensaje("Por favor, complete todos los campos obligatorios.");
                return;
            }

            ordenPayment(metodoPago, totalPagar, idUsuario);
        });
    }

    private void mostrarLayout(View layout) {
        layout.setVisibility(View.VISIBLE);
    }

    private void ocultarLayouts(View... layouts) {
        for (View layout : layouts) {
            layout.setVisibility(View.GONE);
        }
    }

    private boolean validarCampos() {
        if (emailFactura.getText().toString().trim().isEmpty()) {
            return false;
        }

        switch (metodoPago) {
            case "Tarjeta de crédito":
                return !creditCardNumber.getText().toString().trim().isEmpty()
                        && !creditCardName.getText().toString().trim().isEmpty()
                        && !creditCardExpiration.getText().toString().trim().isEmpty()
                        && !creditCardCVV.getText().toString().trim().isEmpty();
            case "PayPal":
                return !paypalEmail.getText().toString().trim().isEmpty()
                        && !paypalPassword.getText().toString().trim().isEmpty();
            case "Bizum":
                return !bizumPhone.getText().toString().trim().isEmpty();
            case "Cobro en taquilla":
                // Para pago en taquilla no se requiere información adicional
                return true;
            default:
                return false;
        }
    }

    private void ordenPayment(String metodoPago, Double totalPagar, int idUsuario) {
        executorService.execute(() -> {
            String response = "";

            try {
                Socket socket = new Socket(IP, PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Enviamos la orden al servidor.
                out.println("INSERT_TRANSACTION");

                // Enviamos los datos de la transacción al servidor.
                out.println(metodoPago);
                out.println(totalPagar);
                out.println(idUsuario);

                // Leemos la respuesta.
                response = in.readLine();

                // Cerramos el socket.
                out.close();
                in.close();
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            final String result = response;
            handler.post(() -> procesarPago(result));
        });
    }

    private void procesarPago(String result) {
        if ("INSERT_TRANSACTION_SUCCESS".equals(result)) {
            ordenReserve(idUsuario, idPelicula, sala, hora, butacasReservadas);
        } else {
            mostrarMensaje("Error al realizar la reserva.");
        }
    }

    private void ordenReserve(int idUsuario, int idPelicula, String sala, String hora, int butacasReservadas) {
        executorService.execute(() -> {
            String response = "";
            try {
                Socket socket = new Socket(IP, PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Enviamos orden al servidor.
                out.println("INSERT_RESERVE");

                // Enviamos datos al servidor.
                out.println(idUsuario);
                out.println(idPelicula);
                out.println(sala);
                out.println(hora);
                out.println(estadoReserva);
                out.println(butacasReservadas);

                // Leemos la respuesta.
                response = in.readLine();

                // Cerramos el socket.
                out.close();
                in.close();
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            final String result = response;
            handler.post(() -> procesarReserva(result));
        });
    }

    private void procesarReserva(String result) {
        if ("INSERT_MOVIE_SUCCESS".equals(result)) {
            mostrarMensaje("Reserva realizada con éxito.");
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("CORREO", emailFactura.getText().toString());
            startActivity(intent);
            finish();
        } else {
            mostrarMensaje("Error al realizar la reserva.");
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("CORREO", emailFactura.getText().toString());
            startActivity(intent);
            finish();
        }
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}
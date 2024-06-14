package com.mobilepulse.gestioncine.activities;

import android.annotation.SuppressLint;
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
import com.mobilepulse.gestioncine.classes.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Actividad para procesar los pagos de las reservas de películas.
 */
public class PaymentActivity extends AppCompatActivity {

    private static final String IP = Configuration.IP;
    private static final int PORT = Configuration.PORT;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Handler handler = new Handler(Looper.getMainLooper());

    private Spinner spinnerPaymentMethod;
    private LinearLayout layoutCreditCard, layoutPaypal, layoutBizum, layoutCashDesk;
    private int idUsuario, idPelicula, butacasReservadas;
    private String estadoReserva, metodoPago, sala, hora;
    private double totalPagar;
    private EditText emailFactura;
    private EditText creditCardNumber, creditCardName, creditCardExpiration, creditCardCVV;
    private EditText paypalEmail, paypalPassword;
    private EditText bizumPhone;

    /**
     * Método llamado cuando la actividad es creada por primera vez.
     *
     * @param savedInstanceState Si la actividad está siendo re-inicializada después de haber sido previamente terminada, este Bundle contiene los datos que más recientemente suministró en onSaveInstanceState(Bundle). De lo contrario, está nulo.
     */
    @SuppressLint("CutPasteId")
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
                    case 0:
                        mostrarLayout(layoutCreditCard);
                        ocultarLayouts(layoutPaypal, layoutBizum, layoutCashDesk);
                        break;
                    case 1:
                        mostrarLayout(layoutPaypal);
                        ocultarLayouts(layoutCreditCard, layoutBizum, layoutCashDesk);
                        break;
                    case 2:
                        mostrarLayout(layoutBizum);
                        ocultarLayouts(layoutCreditCard, layoutPaypal, layoutCashDesk);
                        break;
                    case 3:
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

    /**
     * Muestra el layout específico para el método de pago seleccionado.
     *
     * @param layout El layout a mostrar.
     */
    private void mostrarLayout(View layout) {
        layout.setVisibility(View.VISIBLE);
    }

    /**
     * Oculta los layouts que no son necesarios para el método de pago seleccionado.
     *
     * @param layouts Los layouts a ocultar.
     */
    private void ocultarLayouts(View... layouts) {
        for (View layout : layouts) {
            layout.setVisibility(View.GONE);
        }
    }

    /**
     * Valida los campos de entrada según el método de pago seleccionado.
     *
     * @return true si todos los campos obligatorios están llenos, de lo contrario false.
     */
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

    /**
     * Envía la orden de pago al servidor.
     *
     * @param metodoPago El método de pago seleccionado.
     * @param totalPagar La cantidad total a pagar.
     * @param idUsuario  El ID del usuario.
     */
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

    /**
     * Procesa la respuesta del servidor para la orden de pago.
     *
     * @param result La respuesta del servidor.
     */
    private void procesarPago(String result) {
        if ("INSERT_TRANSACTION_SUCCESS".equals(result)) {
            ordenReserve(idUsuario, idPelicula, sala, hora, butacasReservadas);
        } else {
            mostrarMensaje("Error al realizar la reserva.");
        }
    }

    /**
     * Envía la orden de reserva al servidor.
     *
     * @param idUsuario         El ID del usuario.
     * @param idPelicula        El ID de la película.
     * @param sala              La sala de la película.
     * @param hora              La hora de la película.
     * @param butacasReservadas El número de butacas reservadas.
     */
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

    /**
     * Procesa la respuesta del servidor para la orden de reserva.
     *
     * @param result La respuesta del servidor.
     */
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

    /**
     * Muestra un mensaje en pantalla.
     *
     * @param mensaje El mensaje a mostrar.
     */
    private void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}
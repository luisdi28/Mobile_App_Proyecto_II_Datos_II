package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    private String SERVER_IP; // IP de la máquina  en la que Linux está instalado

    private int SERVER_PORT; // Puerto en el que Linux está escuchando

    Button boton_enviar;

    EditText User_IP;

    Intent go_on;

    private SensorManager sensorManager;

    private Sensor accelerator;

    private TextView textView3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String message = "Conexión exitosa desde el dispositivo Android";

        SERVER_PORT = 8971;

        boton_enviar = findViewById(R.id.boton_enviar);
        User_IP = findViewById(R.id.User_IP);
        go_on = new Intent(getApplicationContext(), Controller.class);
        boton_enviar.setOnClickListener(view -> {

            SERVER_IP = User_IP.getText().toString();
            if (isValidIPv4(SERVER_IP)) {
                EnviarInfo(message);
                go_on.putExtra("Server_IP", SERVER_IP);
                startActivity(go_on);
            } else {
                NoValidIP();
            }
        });

        //Inicializamos el SensorManager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //Obtenemos una instancia del sensor
        accelerator = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        Refresh();
    }

        /*if (isValidIPv4((SERVER_IP))) {
            EnviarInfo();
        } else {
            NoValidIP();
        }
    }*/

    private boolean isValidIPv4(String server_ip) {
        Pattern pattern = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
        Matcher matcher = pattern.matcher(server_ip);
        return matcher.matches();
    }

    private void EnviarInfo(String message) {
        new Thread(() -> {
            try {

                socket = new Socket(SERVER_IP, SERVER_PORT);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);

                output.printf(message);
                Log.d("ENVIADO", message);

                String response = input.readLine();
                Log.d("RESPUESTA", response);

                socket.close();
            } catch (IOException e) {
                Log.d("NO SE PUDO ENVIAR", "");
                e.printStackTrace();
            }
        }).start();
    }

    private void NoValidIP() {
        new Thread(() -> {
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void Refresh() {
        new Thread(this::getNetworkIPs).start();

        SERVER_PORT = 8971;
        SERVER_IP = "192.168.0.18";
    }

    private void getNetworkIPs() {
        byte[] ip = new byte[4];
        final StringLinkedList list = new StringLinkedList();
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        ip = ipv4ToBytes(addr.getHostAddress());
                        Log.d("HOST IP ADDRESS", addr.getHostAddress());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;     // exit method, otherwise "ip might not have been initialized"
        }
        final byte[] ipFinal = ip;

        try {
            String ipID = InetAddress.getByAddress(ipFinal).toString().substring(1);
            ipID = ipID.substring(0, ipID.lastIndexOf(".") + 1) + 'X';
            Log.d("IP encontrada ", ipID);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] ipv4ToBytes(String ipv4) throws IllegalArgumentException {
        String[] octets = ipv4.split("\\.");
        if (octets.length != 4) {
            throw new IllegalArgumentException("Invalid IPv4 address format");
        }
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            int octet = Integer.parseInt(octets[i]);
            if (octet < 0 || octet > 255) {
                throw new IllegalArgumentException("Invalid octet value: " + octet);
            }
            bytes[i] = (byte) octet;
        }
        return bytes;
    }

    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerator, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        String message1 = String.format("x:");

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

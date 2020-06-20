package com.sv.iofrebian;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.sv.iofrebian.utils.Tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.UUID;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class Main extends AppCompatActivity implements AIListener {

    private TextToSpeech tts;
    private String strPartes[];
    private String strOracion;
    private String strActual;
    private String strPrevia;
    private int i = 0;
    private int x = 0;

    private AIService mAIService;

    //Identificador de servicio
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;

    //Si se apreta una vez el boton de conectar
    boolean estado = false;

    //Handler es un control para mensajes
    Handler bluetoothIn;

    //Estado del manejador
    final int handlerState = 0;

    //Esto es simplemente un String normal a diferencia que al agregar una sentancia en un bucle se agrega los espacios automaticamente
//for(hasta 20 veces)
//String cadena += " " + "Dato" ---> En un string normal se debe crear el espacio y luego agregar el dato
//Con esto se traduce a = DataStringIN.append(dato);
    private StringBuilder DataStringIN = new StringBuilder();

    //Llama a la sub- clase y llamara los metodos que se encuentran dentro de esta clase
    ConexionThread MyConexionBT;


    private View parent_view;
    private FloatingActionButton bt_play;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initToolbar();
        ttsFunction("Robot activado, listo para operar");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1000);
        }

        final AIConfiguration config = new AIConfiguration("fff3f39b4e6543f9b29a12a9caabffbf",
                AIConfiguration.SupportedLanguages.Spanish,
                AIConfiguration.RecognitionEngine.System);

        mAIService = AIService.getService(this, config);
        mAIService.setListener(this);

        ////////////////Manejador de mensajes y llamara al metodo Run///////////////////////////////
        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    String readMessage = (String) msg.obj;
                    Toast.makeText(Main.this, "Dato Recibido Entero: " + readMessage, Toast.LENGTH_SHORT).show();
                    DataStringIN.append(readMessage);

                    int endOfLineIndex = DataStringIN.indexOf("#");

                    if (endOfLineIndex > 0) {
                        String dataInPrint = DataStringIN.substring(0, endOfLineIndex);
                        //   Toast.makeText(MainActivity.this, "Dato Recibido: " +dataInPrint, Toast.LENGTH_SHORT).show();
                        DataStringIN.delete(0, DataStringIN.length());
                    }
                }
            }

        };
        ///////////////////////////////////////////////////


        //BOTON CONECTAR
        FloatingActionButton btnConectar = findViewById(R.id.bt_play);
        btnConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyConexionBT.write("0");
                Snackbar.make(parent_view, "Stop", Snackbar.LENGTH_SHORT).show();
            }
        });


    }

    public void ttsFunction(final String texto) {

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.getDefault());
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        //oast.makeText(getApplicationContext(), "Lenguaje no soportado", Toast.LENGTH_SHORT).show();
                    } else {

                        tts.setOnUtteranceProgressListener(
                                new UtteranceProgressListener() {
                                    @Override
                                    public void onStart(String s) {
                                        //Log.i(TAG,"Start: "+s);
                                        String strTexto = texto;
                                        String PARAGRAPH_SPLIT_REGEX = "…";
                                        String[] strPrimera = strTexto.split(PARAGRAPH_SPLIT_REGEX);
                                        strPartes = strPrimera[0].split("_");
                                        strOracion = strPrimera[1];
                                    }

                                    @Override
                                    public void onDone(String s) {

                                        for (String textos : strPartes) {
                                            i = 1;
                                            x = i - 1;
                                            StringBuilder sb = new StringBuilder();
                                            sb.append("");
                                            sb.append(i);
                                            strActual = sb.toString();

                                            StringBuilder sbX = new StringBuilder();
                                            sbX.append("");
                                            sbX.append(x);
                                            strPrevia = sbX.toString();

                                            if (s.equals(strPrevia)) {
                                                leerTexto(textos, strActual);
                                                i++;
                                            }
                                        }

                                        leerTexto(strOracion, "Oracion");
                                        //leerTexto("Procesando operación", "fin");
                                        if (s.equals("fin")) {
                                            tts.stop();
                                            tts.shutdown();
                                        }

                                    }

                                    @Override
                                    public void onError(String s) {
                                        Log.i("Habla", "Error: " + s);
                                    }
                                });

                        //Texto inicial
                        leerTexto("", "0");
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Falló la inicialización", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    void leerTexto(String strTexto, String strId) {
        //API 21+
        Bundle bundle = new Bundle();
        bundle.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC);
        tts.speak(strTexto, TextToSpeech.QUEUE_ADD, bundle, strId);
    }

    private void conectar() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        //Direccion mac del dispositivo a conectar
        BluetoothDevice device = btAdapter.getRemoteDevice("98:D3:32:21:B3:B3");

        try {
            //Crea el socket sino esta conectado
            if (!estado) {
                btSocket = createBluetoothSocket(device);

                estado = btSocket.isConnected();
            }

        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }

        // Establece la conexión con el socket Bluetooth.
        try {
            //Realiza la conexion si no se a hecho
            if (!estado) {
                btSocket.connect();
                estado = true;
                MyConexionBT = new ConexionThread(btSocket);
                MyConexionBT.start();
                Toast.makeText(Main.this, "Conexion Realizada Exitosamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(Main.this, "Ya esta vinculado", Toast.LENGTH_SHORT).show();

            }
        } catch (IOException e) {
            try {
                Toast.makeText(Main.this, "Error:", Toast.LENGTH_SHORT).show();
                Toast.makeText(Main.this, e.toString(), Toast.LENGTH_SHORT).show();
                btSocket.close();
            } catch (IOException e2) {
            }
        }
    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.micro);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);
        Tools.setSystemBarColor(this, android.R.color.white);
        Tools.setSystemBarLight(this);
    }


    public void controlClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.bt_prev: {
                MyConexionBT.write("2");
                Snackbar.make(parent_view, "<-", Snackbar.LENGTH_SHORT).show();
                break;
            }
            case R.id.bt_next: {
                MyConexionBT.write("0");
                Snackbar.make(parent_view, "->", Snackbar.LENGTH_SHORT).show();
                break;
            }
            case R.id.bt_up: {
                MyConexionBT.write("1");
                Snackbar.make(parent_view, "UP", Snackbar.LENGTH_SHORT).show();
                break;
            }
            case R.id.bt_down: {
                MyConexionBT.write("2");
                Snackbar.make(parent_view, "Down", Snackbar.LENGTH_SHORT).show();
                break;
            }
        }
    }


    //Crea el socket
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        //crea un conexion de salida segura para el dispositivo
        //usando el servicio UUID
        //return device.createRfcommSocketToServiceRecord(BTMODULEUUID);

        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }


    //Se debe crear una sub-clase para tambien heredar los metodos de CompaActivity y Thread juntos
//Ademas  en Run se debe ejecutar el subproceso(interrupcion)
    private class ConexionThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConexionThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            while (true) {
                // Se mantiene en modo escucha para determinar el ingreso de datos
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    // Envia los datos obtenidos hacia el evento via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        //Enviar los datos
        public void write(String input) {
            try {
                mmOutStream.write(input.getBytes());
            } catch (IOException e) {
                //si no es posible enviar datos se cierra la conexión
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting_round, menu);
        Tools.changeMenuIconColor(menu, getResources().getColor(R.color.colorPrimary));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (!tts.isSpeaking()) {
                Toast.makeText(getApplicationContext(), "opera", Toast.LENGTH_SHORT).show();
                mAIService.startListening();
            }
        } else if(item.getItemId() == R.id.action_settings){
            conectar();
        }
        else {
            Snackbar.make(parent_view, item.getTitle(), Snackbar.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);


    }

    @Override
    public void onResult(AIResponse response) {
        Result result = response.getResult();

        if (result.getFulfillment().getSpeech().equals("prende")) {
            MyConexionBT.write("2");
            Snackbar.make(parent_view, "1", Snackbar.LENGTH_SHORT).show();
        } else if (result.getFulfillment().getSpeech().equals("apaga")) {
            MyConexionBT.write("0");
            Snackbar.make(parent_view, "0", Snackbar.LENGTH_SHORT).show();
        } else {
            ttsFunction(result.getFulfillment().getSpeech());
        }

        //Log.i("FRANKLIN", result.getAction());
//        if (result.getAction().equals("description")) {
//            int filmPosition=0;
//            for (int i=0; i<myFilms.size(); i++) {
//                if (myFilms.get(i).getTitle().contains(result.getParameters().get("film").toString().replace("\"", ""))) {
//                    filmPosition = i;
//                }
//            }
//            openFilmDetails(filmPosition);
//        }
    }

    public void onError(AIError error) {

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }
}

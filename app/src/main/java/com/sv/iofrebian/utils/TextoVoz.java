package com.sv.iofrebian.utils;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.Locale;

public class TextoVoz {

    private TextToSpeech tts;
    private String strPartes[];
    private String strOracion;
    private String strActual;
    private String strPrevia;
    private int i = 0;
    private int x = 0;

    public void ttsFunction(Context aplicacion, final String texto) {

        tts = new TextToSpeech(aplicacion, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.getDefault());
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.i("VOZ", "Lenguaje no soportado");
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
                    Log.i("VOZ", "Falló la inicialización");
                }

            }
        });

    }

    void leerTexto(String strTexto, String strId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //API 21+
            Bundle bundle = new Bundle();
            bundle.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC);
            tts.speak(strTexto, TextToSpeech.QUEUE_ADD, bundle, strId);
        } else {
            //API 15-
        }
    }

}

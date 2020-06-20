package com.sv.iofrebian;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.sv.iofrebian.utils.Cancion;
import com.sv.iofrebian.utils.Tools;


import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class Lista extends AppCompatActivity {

    private View parent_view;

    private RecyclerView recyclerView;
    private AdapterLista mAdapter;
    private ProgressDialog progreso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);
        parent_view = findViewById(android.R.id.content);

        initToolbar();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        listaTraslado();
    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_refresh);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Lista de Canciones");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listaTraslado();
            }
        });
    }


    private void listaTraslado() {
        progreso = new ProgressDialog(this);
        progreso.setMessage("Cargando...");
        progreso.show();

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, "http://192.168.1.43:8081/api/lista", null,
                new Response.Listener<JSONArray>(){
                    @Override
                    public void onResponse(JSONArray response) {
                        // display response

                        List<Cancion> items = new ArrayList<>();
                        try {
                            for (int i = 0; i < response.length(); i++) {

                                Cancion obj = new Cancion();

                                obj.idCancion  = response.getJSONObject(i).getLong("idCancion");
                                obj.artista = response.getJSONObject(i).getString("artista");
                                obj.album = response.getJSONObject(i).getString("album");
                                obj.nombreCancion = response.getJSONObject(i).getString("nombreCancion");
                                obj.path = response.getJSONObject(i).getString("path");

                                items.add(obj);
                            }

                            mAdapter = new AdapterLista(getApplicationContext(), items);
                            recyclerView.setAdapter(mAdapter);

                            mAdapter.setOnClickListener(new AdapterLista.OnClickListener() {
                                @Override
                                public void onItemClick(View view, Cancion obj, int pos) {
                                    Intent intent = new Intent(getApplicationContext(), PlayerMusic.class);
                                    intent.putExtra("idCancion", obj.idCancion.toString());
                                    intent.putExtra("artista", obj.artista);
                                    intent.putExtra("cancion", obj.nombreCancion);
                                    startActivity(intent);
                                }
                            });
                            progreso.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                        progreso.dismiss();
                        recyclerView.setAdapter(null);
                    }
                }
        );
        requestQueue.add(getRequest);
    }

}

package dam.camarasmadrid2410;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import dam.camarasmadrid2410.viewmodel.MenuViewModel;

/**
 * Grupo: 10
 * Alumnos:
 * - Darío Márquez Ibáñez
 * - David Márquez Ibáñez
 */

/**
 * La clase 'FragmentoDetalle' es un fragmento diseñado para mostrar información detallada sobre
 * una cámara, incluyendo su nombre, coordenadas, imagen y opciones de interacción. Implementa la
 * descarga asíncrona de imágnes, maneja la navegación a un mapa y permite actualizar la imagen
 * mostrada. Utiliza el patrón MVVM accediendo a un 'MenuViewModel' para gestionar parámetros de
 * visualización.
 */

/**
 * Extiende de 'Fragment', lo que permite qeu este componente gestione su propio ciclo de vida y UI,
 * dependiendo de una actividad anfitriona.
 */
public class FragmentoDetalle extends Fragment {

    private static final String ARG_NOMBRE = "nombre";
    private static final String ARG_ALL_COORDENADAS = "allCordenadas";
    private static final String ARG_COORDENADA = "coordenadas";
    private static final String ARG_URL = "url";
    private String mParamNombre, mParamCoordenada, mParamUrl;
    private String mParamAllCordenadas[];
    private MenuViewModel menuViewModel;
    ImageView ivImagen;
    private String mostrar = "mostrarUna";
    View view;
    LinearLayout layoutPBCircular;
    FragmentContainerView contenedorDetalle;
    DescargaImagen descargaIm;
    TextView textoError;
    Button buttonUbicacion, buttonCancelar, buttonUpdate;
    public FragmentoDetalle() {
    }

    public static FragmentoDetalle newInstance(int param1, String param2) {
        FragmentoDetalle fragment = new FragmentoDetalle();
        Bundle args = new Bundle();
        args.putInt(ARG_NOMBRE, param1);
        args.putString(ARG_COORDENADA, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Inicializa el ViewModel y recupera los argumentos del fragmento (nombre, coordenadas, URL) si
     * existen.
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        menuViewModel = new ViewModelProvider(getActivity()).get(MenuViewModel.class);
        //setHasOptionsMenu(true);
        if (getArguments() != null) {
            mParamNombre = getArguments().getString(ARG_NOMBRE);
            mParamCoordenada = getArguments().getString(ARG_COORDENADA);
            mParamUrl = getArguments().getString(ARG_URL);
            mParamAllCordenadas = getArguments().getStringArray(ARG_ALL_COORDENADAS);
        }
    }

    /**
     * Infla el layout  del fragmento, inicializa las referencias a los elementos de la interfaz y
     * configura los listeners de los botones.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragmento_detalle, container, false);
        layoutPBCircular = view.findViewById(R.id.layoutPBCircular);
        contenedorDetalle = view.findViewById(R.id.contenedorDetalleCamaras);
        ivImagen = view.findViewById(R.id.imagen);
        buttonUbicacion = view.findViewById(R.id.buttonUbicacion);
        buttonCancelar = view.findViewById(R.id.buttonClose);
        buttonUpdate = view.findViewById(R.id.buttonUpdate);
        descargaIm = new DescargaImagen();
        /**
         * Lanza una actividad de mapa ('MapsActivity') pasando los datos relevantes de la cámara
         * y las preferencias de visualización.
         */
        buttonUbicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                intent.putExtra("coordenada", mParamCoordenada);
                intent.putExtra("nombre", mParamNombre);
                intent.putExtra("allCordenadas", mParamAllCordenadas);
                intent.putExtra("mostrar", menuViewModel.getModoMostrar());
                intent.putExtra("mostrarUbicacion", menuViewModel.getMostrarUbicacion());
                startActivity(intent);
            }
        });
        /**
         * Llama a un método del fragmento listado para ocultar el detalle.
         */
        buttonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getFragmentManager();
                FragmentoListado fragmentoListado = (FragmentoListado) fm.findFragmentById(R.id.contenedorListaCamaras);
                fragmentoListado.ocultarDetalle();
            }
        });
        /**
         * Extrae la URL de la imagen desde el string HTML y ejecuta una nueva descarga asíncrona
         * de la imagen.
         */
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DescargaImagen descargaUp = new DescargaImagen();
                int inicioSrc = mParamUrl.indexOf("src=") + 4; // Obtener la posición inicial del atributo src
                int finSrc = mParamUrl.indexOf(" ", inicioSrc); // Obtener la posición final del atributo src
                String urlImagen = mParamUrl.substring(inicioSrc, finSrc); // Extraer la URL de la imagen
                descargaUp.execute(urlImagen);
            }
        });
        int inicioSrc = mParamUrl.indexOf("src=") + 4; // Obtener la posición inicial del atributo src
        int finSrc = mParamUrl.indexOf(" ", inicioSrc); // Obtener la posición final del atributo src
        String urlImagen = mParamUrl.substring(inicioSrc, finSrc); // Extraer la URL de la imagen
        descargaIm.execute(urlImagen);
        return view;
    }

    /**
     * Lee el archivo 'configuracion.json' desde los assets para obtener el retardo configurado para
     * la descarga de imágenes.
     * @return retardo correspondiente obtenido del fichero de configuración.
     */
    public int getTiempoDescarga() {
        final StringBuilder respuesta = new StringBuilder();
        String linea;
        String descargaImagen = "";
        // Leer fichero Asset
        final AssetManager am = getContext().getAssets();
        try {
            final InputStream is = am.open("configuracion.json");
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
            while ((linea = bufferedReader.readLine()) != null) {
                respuesta.append(linea).append("\n");
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Message", "Error al recuperar el fichero" + e.toString());
        }
        String jsonStr = respuesta.toString();
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            // Objeto anidado
            JSONObject direccion = jsonObject.getJSONObject("retardos");
            descargaImagen = direccion.getString("imagen");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Return int
        return Integer.parseInt(descargaImagen);
    }

    /**
     * Clase interna 'DescargaImagen' extiende 'AsyncTask'
     */
    private class DescargaImagen extends AsyncTask<String, Void, String> {
        String respuesta = "";
        Bitmap imagenBitmap;

        /**
         * Oculta la imagen y muestra el progreso de la UI (usando 'post' para asegurar que se ejecuta
         * en el hilo principal).
         * Aplica un retardo configurable con la función 'getTiempoDescarga()'.
         * Descarga la imagen desde la URL proporcionada, gestionando posibles redirecciones HTTP.
         * Decodifica el stream en un objeto 'Bitmap'.
         * En caso de error, muestra un mensaje en el 'TextView'.
         * @param urls
         * @return
         */
        @Override
        protected String doInBackground(String... urls) {
            try {
                // Oculta la imagen y muestra el progreso de la UI (usando 'post' para asegurar que
                // se ejecuta en el hilo principal).
                ivImagen.post(new Runnable() {
                    @Override
                    public void run() {
                        ivImagen.setVisibility(View.GONE);
                    }
                });
                layoutPBCircular.post(new Runnable() {
                    @Override
                    public void run() {
                        layoutPBCircular.setVisibility(View.VISIBLE);
                    }
                });
                // Aplica un retardo configurable con la función 'getTiempoDescarga()'.
                SystemClock.sleep(getTiempoDescarga()); // Retardo imagen
                URL url = new URL(urls[0]);
                // Descarga la imagen desde la URL proporcionada, gestionando posibles redirecciones
                // HTTP.
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                if(urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    url = new URL(urlConnection.getHeaderField("Location"));
                    urlConnection.disconnect();
                    urlConnection = (HttpURLConnection) url.openConnection();
                }
                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream is = urlConnection.getInputStream();
                    // Decodifica el stream en un objeto 'Bitmap'.
                    imagenBitmap = BitmapFactory.decodeStream(is, null, null);
                    is.close();
                }else{
                    textoError.setText("Error en conexion la url es: "+urls[0]);
                }
                urlConnection.disconnect();
                } catch (MalformedURLException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            return respuesta;
        }

        /**
         * Oculta el progreso y muestra la imagen descargada en la UI.
         * @param resultado
         */
        @Override
        protected void onPostExecute(final String resultado) {
            layoutPBCircular.setVisibility(View.GONE);
            ivImagen.setImageBitmap(imagenBitmap);
            ivImagen.setVisibility(View.VISIBLE);
        }
    }
}



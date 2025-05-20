package dam.camarasmadrid2410.descarga;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Grupo: 10
 * Alumnos:
 * - Darío Márquez Ibáñez
 * - David Márquez Ibáñez
 */

/**
 * Esta clase está diseñada para descargar un archivo KML desde una URL, utilizando una tarea asíncrona
 * (AsyncTask), y almacenar el resultado en el almacenamiento interno del dispositivo. Además, obtiene
 * el tiempo de retardo para la descarga desde un archivo de configuración en formato JSON almacenado
 * en los assets de la aplicacion.
 */

/**
 * Extiende de 'AsyncTask', lo que permite ejecutar tareas en segundo plano y actualizar la interfaz
 * de usuario tras su finalización.
 */
public class DescargaKML extends AsyncTask<String, Void, String> {
  String respuesta;
  Context context;
  URL url;
  HttpURLConnection urlConnection;
  String kml;

  public DescargaKML(Context context){
    this.context = context;
  }

  /**
   * Lee el archivo 'configuraciones.json' desde los assets de la aplicación para obtener el valor
   * de retardo para el análisis.
   * @return
   */
  public int getTiempoDescarga() {
    final StringBuilder respuesta = new StringBuilder();
    String linea;
    String tAnalisis = "";
    // Leer fichero Asset
    final AssetManager am = context.getAssets();
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
      tAnalisis = direccion.getString("descarga-kml");
    } catch (JSONException e) {
      e.printStackTrace();
    }
    // Return int
    return Integer.parseInt(tAnalisis);
  }

  /**
   * Proceso en segundo plano encargado de abrir la conexión Http y descargar el fichero kml.
   * @param urls
   * @return
   */
  @Override
  protected String doInBackground(String... urls) {
    try {
      url = new URL(urls[0]);
      urlConnection = (HttpURLConnection) url.openConnection();
      if(urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
        urlConnection.disconnect();
        url = new URL(urlConnection.getHeaderField("Location"));
        urlConnection = (HttpURLConnection) url.openConnection();
      }
      if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
        respuesta = "Correcto";
        InputStream is = urlConnection.getInputStream();
        final StringBuilder contenido = new StringBuilder();
        String linea;
        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader bufferedReader = new BufferedReader(reader);
        kml = "";
        linea=bufferedReader.readLine();
        while (linea != null && !isCancelled()) {
          contenido.append(linea).append("\n");
          linea = bufferedReader.readLine();
        }
        kml = contenido.toString();
        writeFileOnInternalStorage(context, "CamarasMadrid.kml", kml);
        is.close();
        reader.close();
        bufferedReader.close();
        urlConnection.disconnect();
        SystemClock.sleep(getTiempoDescarga());
      } else {
        respuesta = "Ha fallado la conexión a " + urls[0];
      }
    } catch (Exception e) {
      respuesta = "Se ha producido esta excepción: " + e.toString();
    }
    return respuesta;
  }

  /**
   * Funcion encargada de escribir en el almacenamiento del movil el fichero descargado en la carpeta
   * camaras. La crea si no existe.
   * @param mcoContext
   * @param sFileName
   * @param sBody
   */
  public void writeFileOnInternalStorage(Context mcoContext, String sFileName, String sBody){
    File dir = new File(mcoContext.getFilesDir(), "camaras");
    if(!dir.exists()){
      dir.mkdir();
    }

    try {
      File gpxfile = new File(dir, sFileName);
      FileWriter writer = new FileWriter(gpxfile);
      writer.append(sBody);
      writer.flush();
      writer.close();
    } catch (Exception e) {}
  }
}
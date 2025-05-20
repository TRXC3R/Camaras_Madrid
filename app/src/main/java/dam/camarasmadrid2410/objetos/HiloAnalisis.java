package dam.camarasmadrid2410.objetos;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import dam.camarasmadrid2410.FragmentoListado;
import dam.camarasmadrid2410.descarga.DescargaKML;
import dam.camarasmadrid2410.manejador.ManejadorXML;
/**
 * Grupo: 10
 * Alumnos:
 * - Darío Márquez Ibáñez
 * - David Márquez Ibáñez
 */

 /*
    Su propósito es analizar un fichero KML con la lista de cámaras de tráfico de Madrid, 
    y actualizar la interfaz gráfica de un fragmento (FragmentoListado) con dicha información.
 */
public class HiloAnalisis implements Runnable {
    private String fichero; //Fichero que se abrirá para analizar el contenido 
    private ListaCamaras camaras; //Lista de camaras 

    private FragmentoListado instanciaFragmentoListado;
    private SharedPreferences almacen;
    private boolean descargar;
    private boolean oculto;

    public HiloAnalisis(String fichero, FragmentoListado instanciaFragmentoListado, boolean descargar, boolean oculto) {
        this.fichero = fichero;
        this.instanciaFragmentoListado = instanciaFragmentoListado;
        this.descargar = descargar;
        this.oculto = oculto;
    }

    @Override
    public void run() {
        Context context = instanciaFragmentoListado.getContext();
        almacen = context.getSharedPreferences("datos", Context.MODE_PRIVATE);
        SAXParserFactory fabrica = SAXParserFactory.newInstance();
        fabrica.setNamespaceAware(true);
        try {
            if(descargar){
                if (!oculto)
                    instanciaFragmentoListado.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fijarOrientacion();
                            instanciaFragmentoListado.cambiarTextoCarga("Espera mientras se descarga la lista de cámaras");
                        }
                    });
                else
                    instanciaFragmentoListado.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fijarOrientacion();
                            Toast.makeText(context, "Descargando lista de cámaras en segundo plano", Toast.LENGTH_SHORT).show();
                        }
                    });
                DescargaKML descargaWeb = new DescargaKML(context);
                // Página de la que descarga el fichero KML
                descargaWeb.execute("http://informo.madrid.es/informo/tmadrid/CCTV.kml");

                /*
                    Este while es peligroso: mantiene el hilo ocupado activamente y puede bloquear otros procesos. 
                    Sería mejor usar un Callback o el método onPostExecute() de AsyncTask.
                */
                while(descargaWeb.getStatus() != AsyncTask.Status.FINISHED){}
                //Hacer que espere hasta que el fichero se haya descargado

                final SharedPreferences.Editor editor = almacen.edit();
                Date fecha = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
                editor.putString("fecha", sdf.format(fecha));
                editor.commit();
            }
            if (!oculto)
                instanciaFragmentoListado.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fijarOrientacion();
                        instanciaFragmentoListado.cambiarTextoCarga("Obteniendo cámaras del fichero KML descargado");
                    }
                });

            File kml = new File(context.getFilesDir() + "/camaras/CamarasMadrid.kml");

            /*
            InputStream inputStream = new FileInputStream(kml);
            SAXParser analizadorSAX = fabrica.newSAXParser();
            ManejadorXML manejadorXML = new ManejadorXML(instanciaFragmentoListado);
            analizadorSAX.parse(new InputSource(inputStream), manejadorXML);    // Ejecución del analizador
            camaras = manejadorXML.getResultado(); // Recoger los datos del analizador
            */
            //He realizado esta cambio porque es más limpio y evita olvidos o errores al cerrar recursos.

            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(kml);
                SAXParser analizadorSAX = fabrica.newSAXParser();
                ManejadorXML manejadorXML = new ManejadorXML(instanciaFragmentoListado);
                analizadorSAX.parse(new InputSource(inputStream), manejadorXML);
                camaras = manejadorXML.getResultado();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Log.e("Cerrar Stream", "Error al cerrar el InputStream: " + e.toString());
                    }
                }
            }

            // Se necesita invocar al método actualizaListaCamaras() de FragmentoListado
            // Como ese método hace uso de vistas de la UI no se puede invocar directamente.
            // Se tiene que hacer mediante el método runOnUiThread() que tiene la actividad a la que pertenece el fragmento
            instanciaFragmentoListado.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    instanciaFragmentoListado.actualizaListaCamaras(camaras);
                    instanciaFragmentoListado.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }
            });
            if(oculto)
                instanciaFragmentoListado.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        instanciaFragmentoListado.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                        Toast.makeText(context, "Mostrando lista de cámaras descargada", Toast.LENGTH_SHORT).show();
                    }
                });
        } catch (SAXException | IOException | ParserConfigurationException e) {
            Log.d("Errores SAX", "Se ha producido un error: " + e.toString());
        }
    }

    private void fijarOrientacion() {
        // Para bloquear en modo actual (portrait o landscape):
        int currentOrientation = instanciaFragmentoListado.getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            instanciaFragmentoListado.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            instanciaFragmentoListado.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

}

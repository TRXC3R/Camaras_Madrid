package dam.camarasmadrid2410;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toolbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import dam.camarasmadrid2410.network.NetworkChangeReceiver;
import dam.camarasmadrid2410.viewmodel.MenuViewModel;

/**
 * Grupo: 10
 * Alumnos:
 * - Darío Márquez Ibáñez
 * - David Márquez Ibáñez
 */

/**
 * La clase 'MainActivity' es la actividad principal. Extiende 'AppCompatActivity' y gestiona el menú
 * de opciones,la interacción con el usuario sobre permisos de ubicación y la integración con el patrón
 * MVVM mediante el uso de 'MenuViewModel'. La clase también coordina la visualización de fragmentos
 * y responde a acciones del usuario desde la barra de herramientas.
 */
public class MainActivity extends AppCompatActivity{
    private MenuViewModel menuViewModel;
    private Menu menuOpciones;
    FragmentoListado listaFragmentos;
    boolean oscuro;
    Toolbar toolbar;


    private ActivityResultLauncher<String> lanzadorPeticionPermiso = registerForActivityResult(new ActivityResultContracts.RequestPermission(), esConcendido -> {
        if (esConcendido) {
            menuOpciones.getItem(2).setChecked(true);
            menuViewModel.setMostrarUbicacion(true);
        }
    });

    /**
     * Inicializa la actividad, establece el layout y comprueba si el permiso de ubicación ya está
     * concedido. Inicializa el 'MenuViewModel' usando 'ViewModelProvider'.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("preferencias_tema", MODE_PRIVATE);
        oscuro = prefs.getBoolean("tema_oscuro", false);
        setTheme(oscuro ? R.style.Theme_CamarasMadridDark : R.style.Theme_CamarasMadridLight);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

        }
        menuViewModel = new ViewModelProvider(this).get(MenuViewModel.class);
    }

    /**
     * Infla el menú de opciones desde un recurso XML. Sincroniza el estado visual de los ítems del
     * menú con los valores actuales del 'MenuViewModel'.
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.barraherramientas, menu);
        menuOpciones = menu;
        menuOpciones.getItem(3).setChecked(menuViewModel.getMostrarUbicacion());
        if(menuViewModel.getModoMostrar() == "mostrarUna")
            menuOpciones.getItem(4).setChecked(true);
        else if(menuViewModel.getModoMostrar() == "mostrarTodas")
            menuOpciones.getItem(5).setChecked(true);
        else
            menuOpciones.getItem(6).setChecked(true);
        return true;

    }

    /**
     * Gestiona las acciones del usuario sobre el menú.
     * Los items descarga y orden están presentes, pero no implementan lógica en esta actividad
     * (se gestiona en fragmentos).
     * @param item
     * @return
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        int id = item.getItemId();
        listaFragmentos = new FragmentoListado();

        if (id == R.id.download) {
            // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            return false; // NO SE HACE EN ESTA ACTIVIDAD
        } else if (id == R.id.mostrarUbicacion) {
            boolean estado = item.isChecked();
            if (estado) { // Simplified from estado == true
                item.setChecked(false);
                menuViewModel.setMostrarUbicacion(false);
            } else {
                // Si se han dado los permisos de localización los permisos
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    item.setChecked(true);
                    menuViewModel.setMostrarUbicacion(true);
                } else {
                    solicitarPermiso();
                }
            }
            return false;
        } else if (id == R.id.mostrarAgrupación) {
            menuViewModel.setModoMostrar("mostrarAgrupacion");
            item.setChecked(true);
            return false;
        } else if (id == R.id.mostrarTodas) {
            menuViewModel.setModoMostrar("mostrarTodas");
            item.setChecked(true);
            return false;
        } else if (id == R.id.mostrarUna) {
            menuViewModel.setModoMostrar("mostrarUna");
            item.setChecked(true);
            return false;
        } else if (id == R.id.order) {
            return false; //NO SE HACE EN ESTA ACTIVIDAD
        } else if (id == R.id.app_theme_switch) {
            mostrarDialogoSeleccionTema();
            return false;
        } else {
            return super.onOptionsItemSelected(item);
        }

    }



    /**
     * Salta un cuadro de dialogo que te permite elegir el tema que quieres para la aplicación
     */
    private void mostrarDialogoSeleccionTema() {
        final String[] temas = {"Claro", "Oscuro"};
        new AlertDialog.Builder(this)
                .setItems(temas, (dialog, which) -> {
                    if (which == 0) {
                        cambiarTema(R.style.Theme_CamarasMadridLight, false);
                    } else {
                        cambiarTema(R.style.Theme_CamarasMadridDark, true);
                    }
                })
                .show();
    }

    /**
     *
     * @param themeResId
     * @param oscuro
     */
    //Cuando el usuario cambia el tema, guarda la preferencia
    private void cambiarTema(int themeResId, boolean oscuro) {
        SharedPreferences prefs = getSharedPreferences("preferencias_tema", MODE_PRIVATE);
        prefs.edit().putBoolean("tema_oscuro", oscuro).apply();
        setTheme(themeResId);
        recreate();
    }

    /**
     * Muestra un diálogo explicativo si el usuario ha denegado el permiso previamente, permitiendo
     * abrir la configuración de la aplicación para cambiar los permisos manualmente. Si no, solicita
     * el permiso directamente mostrando un diálogo de confirmación.
     */
    private void solicitarPermiso(){
        SpannableString titulo = new SpannableString("Solicitud de permiso de ubicación");
        titulo.setSpan(new ForegroundColorSpan(Color.BLACK), 0, titulo.length(), 0); // Usa el color que quieras
        SpannableString message = new SpannableString("Seleccionó no volver a preguntar el permiso de ubicación. ¿Desea activarlo manualmente?");
        message.setSpan(new ForegroundColorSpan(Color.BLACK), 0, message.length(), 0); // Usa el color que quieras
        boolean showRationale = shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION);
        if(!showRationale){
            final Activity actividad = this;
            new AlertDialog.Builder(actividad)
                    // Hemos usado la clase SpannableString para cambiarle el color al texto y no use el de por defecto.
                    .setTitle(titulo)
                    .setMessage(message)
                    . setNegativeButton("No acepto", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    })
                    .setPositiveButton("Acepto", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Intent intent = new Intent();
                            intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    })
                    .show();
        } else {
            final Activity actividad = this;
            new AlertDialog.Builder(actividad)
                    .setTitle("Solicitud de permiso de ubicación")
                    .setMessage("A continuación se le solicitará permiso para poder acceder a su ubicación.")
                    . setNegativeButton("No acepto", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            new AlertDialog.Builder(actividad)
                                    .setTitle("ATENCIÓN")
                                    .setMessage("No ha aceptado solicitud de permiso a su ubicación por lo que no podrá hacer uso de esta función.")
                                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                        }
                                    })
                                    .show();
                        }
                    })
                    .setPositiveButton("Acepto", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            lanzadorPeticionPermiso.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
                        }
                    })
                    .show();
        }

    }
    private NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkChangeReceiver);
    }
}



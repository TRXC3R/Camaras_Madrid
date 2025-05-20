package dam.camarasmadrid2410;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.clustering.ClusterManager;

import dam.camarasmadrid2410.cluster.MyItem;
import dam.camarasmadrid2410.databinding.ActivityMapsBinding;
import dam.camarasmadrid2410.network.NetworkChangeReceiver;
import dam.camarasmadrid2410.viewmodel.MapaViewModel;
/**
 * Grupo: 10
 * Alumnos:
 * - Darío Márquez Ibáñez
 * - David Márquez Ibáñez
 */

/**
 * 'MapsActivity' es una actividad que muestra un mapa interactivo usando GoogleMaps. Permite visualizar
 * una o varias cámaras, la ubicación del usuario, y soporta modos de visualización (normal, satélite,
 * híbrido y terreno). Integra funcionalidades de clustering para agrupar múltiples cámaras y utiliza
 * un 'MapViewModel' para mantener el estado del tipo de mapa seleccionado.
 */
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {

  private GoogleMap mMap;
  private ActivityMapsBinding binding;
  private CircleOptions circleOptions;
  private Circle circle;
  private MarkerOptions markerOpSeleccion;
  private Marker markerSeleccion;
  private Toolbar barraHerramientas;
  private ActionBar actionBar;
  private RadioGroup vistas;
  private ClusterManager<MyItem> clusterManager;
  private MapaViewModel modeViewModel;
  String[] allCoordenadas;

  /**
   * - Inicializa el binding y el ViewModel.
   * - Configura la barra de herramientas y el botón flotante que alterna la visibilidad del selector
   * de vistas ('RadioGroup').
   * - Obtiene el fragmento de mapa y solicita la inicialización asíncrona ('getMapAsync').
   * @param savedInstanceState
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    SharedPreferences prefs = getSharedPreferences("preferencias_tema", MODE_PRIVATE);
    boolean oscuro = prefs.getBoolean("tema_oscuro", false);

    Toast.makeText(this,"Oscuro está puesto en: " + oscuro, Toast.LENGTH_SHORT).show();
    setTheme(oscuro ? R.style.Theme_CamarasMadridDark : R.style.Theme_CamarasMadridLight);
    setTheme(oscuro ? R.style.Theme_CamarasMadridDark_CobertorAppBar : R.style.Theme_CamarasMadridLight_CobertorAppBar);
    setTheme(oscuro ? R.style.Theme_CamarasMadridDark_CobertorPopup: R.style.Theme_CamarasMadridLight_CobertorPopup);
    setTheme(oscuro ? R.style.Theme_CamarasMadridDark_SinActionBar: R.style.Theme_CamarasMadridLight_SinActionBar);

    super.onCreate(savedInstanceState);
    binding = ActivityMapsBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    modeViewModel = new ViewModelProvider(this).get(MapaViewModel.class);
    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
            .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);


    barraHerramientas = findViewById(R.id.toolbar);

    // Obtener el color definido en el tema usando TypedValue
    TypedValue typedValue = new TypedValue();
    Resources.Theme theme = getTheme();
    theme.resolveAttribute(com.google.android.material.R.attr.colorPrimaryVariant, typedValue, true);
    int colorTextoBarraHerramientas = typedValue.data;
    barraHerramientas.setTitleTextColor(colorTextoBarraHerramientas);
    vistas = findViewById(R.id.vistas);
    vistas.setVisibility(View.GONE);

    setSupportActionBar(barraHerramientas);
    actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    binding.fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if(vistas.getVisibility() == View.GONE)
          vistas.setVisibility(View.VISIBLE);
        else
          vistas.setVisibility(View.GONE);
      }
    });
  }

  /**
   * - Obtiene los parámetros de la intención: coordenada seleccionada, nombre, lista de todas las
   * coordenadas, modo de visualización y si mostrar la ubicación del usuario.
   * - Parsea la coordenada seleccionada y la muestra con un marcador azul.
   * - Si hay un modo de mapa guardado en el ViewModel, lo aplica.
   * - Centra la cámara en la ubicación seleccionada y muestra el 'InfoWindow'.
   * - Configura un listener para centrar y hacer zoom al pulsar un marcador.
   * - Si se solicita mostar la ubicación:
   *    - Usa 'FusedLocationProviderClient' para obtener la última localización conocida.
   *    - Añade un marcador verde y un círculo de 1km de radio en la ubicación del usuario.
   * - Según el modo de visualización ('mostar'):
   *    - 'mostrarUna': centra la camara en la cámara seleccionada con nivel de zoom de calle.
   *    - 'mostrarTodas': añade todos los marcadores y ajusta la cámara para que todos sean visibles.
   *    - 'mostrarAgrupación': llama a 'setUpClusterer()' para agrupar marcadores.
   * @param googleMap
   */
  @Override
  public void onMapReady(GoogleMap googleMap) {

    mMap = googleMap;

    // leer preferencia de tema
    SharedPreferences prefs = getSharedPreferences("preferencias_tema", MODE_PRIVATE);
    boolean oscuro = prefs.getBoolean("tema_oscuro", false);

    try {
      int styleRes = oscuro ? R.raw.map_style_dark : R.raw.map_style_light;
      boolean success = mMap.setMapStyle(
              MapStyleOptions.loadRawResourceStyle(this, styleRes)
      );
      if (!success) {
        Log.e("MapsActivity", "Style parsing failed.");
      }
    } catch (Resources.NotFoundException e) {
      Log.e("MapsActivity", "Can't find style. Error: ", e);
    }

    String coordenada = getIntent().getStringExtra("coordenada");
    String nombre = getIntent().getStringExtra("nombre");
    allCoordenadas = getIntent().getStringArrayExtra("allCordenadas");
    String mostrar = getIntent().getStringExtra("mostrar");
    boolean mostrarUbicacion = getIntent().getBooleanExtra("mostrarUbicacion", false);
    String[] partes = coordenada.split(",");
    String longitud= partes[0].trim();
    String latitud = partes[1].trim();

    LatLng ubicacionSeleccionada = new LatLng(Double.parseDouble(latitud), Double.parseDouble(longitud));
    mMap.addMarker(new MarkerOptions().position(ubicacionSeleccionada).title(nombre));
    mMap.moveCamera(CameraUpdateFactory.newLatLng(ubicacionSeleccionada));
    markerOpSeleccion = new MarkerOptions();
    markerOpSeleccion.position(ubicacionSeleccionada);
    markerOpSeleccion.title(nombre);
    markerOpSeleccion.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

    if (modeViewModel.getModeMap() >= 0) mMap.setMapType(modeViewModel.getModeMap());

    markerSeleccion = mMap.addMarker(markerOpSeleccion);
    markerSeleccion.showInfoWindow();

    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener(){
      @Override
      public boolean onMarkerClick(@NonNull Marker marker) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 17));
        return false;
      }
    });

    if(mostrarUbicacion){
      FusedLocationProviderClient clienteLocalizacion;
      clienteLocalizacion = LocationServices.getFusedLocationProviderClient(this);
      if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){}
      else {
        Task<Location> ultimaLocalizacion = clienteLocalizacion.getLastLocation();
        ultimaLocalizacion.addOnSuccessListener(this, new OnSuccessListener<Location>() {
          @Override
          public void onSuccess(Location localizacion) {
            if(localizacion != null){
              MarkerOptions markerOpLocation = new MarkerOptions();
              markerOpLocation.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
              markerOpLocation.position(new LatLng(localizacion.getLatitude(), localizacion.getLongitude()));
              mMap.addMarker(markerOpLocation);
              circleOptions = new CircleOptions()
                      .center(new LatLng(localizacion.getLatitude(), localizacion.getLongitude()))
                      .radius(1000).strokeColor(Color.BLACK);
              circle = mMap.addCircle(circleOptions);
              circle.setVisible(true);
            }
          }
        });
      }
    }

    //Añadir el resto de marcadores;
    switch (mostrar) {
      case "mostrarUna":
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(ubicacionSeleccionada)
                .zoom(17) // nivel de zoom a nivel de calles
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        break;
      case "mostrarTodas":
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(int i = 0; i< allCoordenadas.length; i++){
          String[] div = allCoordenadas[i].split(",");
          String partelongitud= div[0].trim();
          String partelatitud = div[1].trim();
          LatLng latLng = new LatLng(Double.parseDouble(partelatitud), Double.parseDouble(partelongitud));
          mMap.addMarker(new MarkerOptions().position(latLng));
          builder.include(latLng);
        }
        // Crea el objeto LatLngBounds que contiene todos los marcadores
        LatLngBounds bounds = builder.build();
        // Crea un margen alrededor de los límites geográficos
        int padding = 50; // en píxeles
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
        break;
      case "mostrarAgrupacion":
        setUpClusterer();
        break;
      default:
        break;
    }
  }

  /**
   * - Inicializa 'ClusterManager' con el contexto y el mapa.
   * - Asigna el listener de cámara y de click de marcador al 'ClusterManager'.
   * - Crea y añade un 'MyItem' (elemento clusterizable) por cada coordenada.
   * - Ajusta la cámara para mostar todos los clusters.
   */
  private void setUpClusterer() {

    // Initialize the manager with the context and the map.
    // (Activity extends context, so we can pass 'this' in the constructor.)
    clusterManager = new ClusterManager<MyItem>(this, mMap);

    // Point the map's listeners at the listeners implemented by the cluster
    // manager.
    final CameraPosition[] mPreviousCameraPosition = {null};
    mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
      @Override
      public void onCameraIdle() {
        CameraPosition position = mMap.getCameraPosition();
        if(mPreviousCameraPosition[0] == null || mPreviousCameraPosition[0].zoom != position.zoom) {
          mPreviousCameraPosition[0] = mMap.getCameraPosition();
          clusterManager.cluster();
        }
      }
    });
    mMap.setOnMarkerClickListener(clusterManager);

    LatLngBounds.Builder builder = new LatLngBounds.Builder();
    // Add cluster items (markers) to the cluster manager.
    for(int i = 0; i< allCoordenadas.length; i++){
      String[] div = allCoordenadas[i].split(",");
      String partelongitud= div[0].trim();
      String partelatitud = div[1].trim();
      builder.include(new LatLng(Double.parseDouble(partelatitud), Double.parseDouble(partelongitud)));
      clusterManager.addItem(new MyItem(Double.parseDouble(partelatitud), Double.parseDouble(partelongitud), "Title" + i, "Snippet" + i));
    }
    LatLngBounds bounds = builder.build();
    int padding = 50;
    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
    mMap.animateCamera(cu);
  }

  @Override
  public void onMapClick(@NonNull LatLng latLng) {

  }

  @Override
  public void onMapLongClick(@NonNull LatLng latLng) {

  }

  public void modeNormal (View view){
    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    modeViewModel.setModeMap(GoogleMap.MAP_TYPE_NORMAL);
  }

  public void modeSatellite (View view){
    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
    modeViewModel.setModeMap(GoogleMap.MAP_TYPE_SATELLITE);
  }

  public void modeHybrid (View view){
    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    modeViewModel.setModeMap(GoogleMap.MAP_TYPE_HYBRID);
  }

  public void modeTerrain (View view){
    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
    modeViewModel.setModeMap(GoogleMap.MAP_TYPE_TERRAIN);

  }

  /**
   * Llama a 'onBackPressed()' para gestionar la navegación hacia atrás.
   * @return
   */
  @Override
  public boolean onSupportNavigateUp() {
    super.onBackPressed();
    return true;
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
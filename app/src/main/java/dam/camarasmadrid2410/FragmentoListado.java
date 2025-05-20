package dam.camarasmadrid2410;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.util.Collections;

import dam.camarasmadrid2410.objetos.Adaptador;
import dam.camarasmadrid2410.objetos.Camara;
import dam.camarasmadrid2410.viewmodel.CamaraViewModel;
import dam.camarasmadrid2410.objetos.HiloAnalisis;
import dam.camarasmadrid2410.objetos.ListaCamaras;
import dam.camarasmadrid2410.viewmodel.MenuViewModel;

/**
 * Grupo: 10
 * Alumnos:
 * - Darío Márquez Ibáñez
 * - David Márquez Ibáñez
 */

/**
 * La clase 'FragmentoListado' es un fragmento que implementa la vista principal de una lista de
 * cámaras, permite gestionar la visualización, búsqueda, filtardo, ordenación y selección de elementos,
 * así como la interacción con otros fragmentos (como el de detalle) y la gestión de descargas/restauraciones
 * de datos. Utiliza el patrón MVVM con los ViewModels 'CameraViewModel' y 'MenuViewModel' para
 * mantener el estado y a configuración de la interfaz.
 */
public class FragmentoListado extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private CamaraViewModel camaraViewModel;
    private MenuViewModel menuViewModel;
    private LinearLayout progreso;
    private TextView mensajeProgreso;
    private ListView listViewCamaras;
    FragmentContainerView contenedorDetalle;
    private Menu menuOpciones;
    private ScrollView contenedorListaCamaras;
    private Adaptador adaptador;
    private ListaCamaras listaCamaras;
    String coordenadas[] = new String[0];
    private final String nombreFichero = "CamarasMadridCompleto.kml";
    private SharedPreferences almacen;
    private boolean iniciado = false;
    private int posicionSeleccionada = -1;
    private String modoOrden = "";
    private String textoAnterior = "";
    TextView infoModo;
    Thread thread;
    private boolean ordenar = false;
    String mostrarUbicacion;
    String modoUbicacion;
    private boolean buscar = false;
    FragmentoListado instanciaFragmentoListado = this;
    private ListaCamaras listaCamaraFiltradas = new ListaCamaras();
    private SearchView searchView;

    public FragmentoListado() {
    }

    public static FragmentoListado newInstance(String param1, String param2) {
        FragmentoListado fragment = new FragmentoListado();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Inicializa los ViewModels y recupera argumentos y estado guardado. Configura el menú de opciones
     * y el estado inicial del fragmento.
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        camaraViewModel = new ViewModelProvider(this).get(CamaraViewModel.class);
        menuViewModel = new ViewModelProvider(getActivity()).get(MenuViewModel.class);
        if (getArguments() != null) {
            iniciado = getArguments().getBoolean("iniciado");
        }
        if (savedInstanceState != null) {
            // Recuperar el estado de la aplicación desde el Bundle
            if (savedInstanceState.containsKey("posicionSeleccionada")) {
                posicionSeleccionada = savedInstanceState.getInt("posicionSeleccionada", -1);
            }
            if (savedInstanceState.containsKey("iniciado")) {
                iniciado = savedInstanceState.getBoolean("iniciado", false);
            }
            if (savedInstanceState.containsKey("orden")) {
                modoOrden = savedInstanceState.getString("orden", "");
            }
        }
    }

    /**
     * Configura el menú de opciones, habilitando/disabilitando opciones según el estado. Actualiza
     * los textos de modo de visualización y ubicación en la interfaz.
     * @param menu The options menu in which you place your items.
     *
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Do something that differs the Activity's menu here
            menuOpciones = menu;
        if(iniciado)
            for(int i = 0; i < menuOpciones.size(); i++) {
                menuOpciones.getItem(i).setEnabled(true);
            }
        if(menuViewModel.getMostrarUbicacion())
            mostrarUbicacion = "Ubicación activada";
        else
            mostrarUbicacion = "Ubicación desactivada";
        if(menuViewModel.getModoMostrar() == "mostrarUna")
            modoUbicacion = "Mostrar una cámara";
        else if(menuViewModel.getModoMostrar() == "mostrarTodas")
            modoUbicacion = "Mostrar todas las cámaras";
        else
            modoUbicacion = "Mostrar la agrupación";
        infoModo.setText(mostrarUbicacion+" | "+modoUbicacion);
    }

    /**
     * Gestiona las acciones del menú:
     * - Descarga/restauración de datos (lanza un hilo de análisis)
     * - Alternancia de modos de visualización y ubicación
     * - Ordenación de la lista (A-Z, Z-A)
     * - Búsqueda
     * @param item The menu item that was selected.
     *
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.download) {
            // Inhabilitar botones del menú
            for(int i = 0; i < menuOpciones.size(); i++) {
                menuOpciones.getItem(i).setEnabled(false);
            }

            HiloAnalisis hiloAnalisis = new HiloAnalisis(nombreFichero, instanciaFragmentoListado, true, true);
            thread = new Thread(hiloAnalisis);
            thread.start();
            return true;
        } else if (id == R.id.mostrarUbicacion) {
            if(menuViewModel.getMostrarUbicacion())
                mostrarUbicacion = "Ubicación activada";
            else
                mostrarUbicacion = "Ubicación desactivada";
            infoModo.setText(mostrarUbicacion+" | "+modoUbicacion);
            return true;
        } else if (id == R.id.mostrarAgrupación) {
            modoUbicacion = "Mostrar la agrupación";
            infoModo.setText(mostrarUbicacion+" | "+modoUbicacion);
            return true;
        } else if (id == R.id.mostrarTodas) {
            modoUbicacion = "Mostrar todas las cámaras";
            infoModo.setText(mostrarUbicacion+" | "+modoUbicacion);
            return true;
        } else if (id == R.id.mostrarUna) {
            menuViewModel.setModoMostrar("mostrarUna");
            modoUbicacion = "Mostrar una cámara";
            infoModo.setText(mostrarUbicacion+" | "+modoUbicacion);
            return true;
        } else if (id == R.id.order) {
            ordenar = true;
            actualizaListaCamaras(listaCamaras);
            ocultarDetalle();
            return true;
        }else if (id == R.id.search) {
            if (searchView.getVisibility() == View.GONE) {
                searchView.setVisibility(View.VISIBLE);
                int searchCloseButtonId = searchView.getContext().getResources()
                        .getIdentifier("android:id/search_close_btn", null, null);
                ImageView closeButton = (ImageView) this.searchView.findViewById(searchCloseButtonId);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        searchView.setQuery("", false);
                        camaraViewModel.setBusqueda("");
                        buscar = false;
                        if(camaraViewModel.getListaCamaraSeleccionada() != null)
                            listaCamaras = camaraViewModel.getListaCamaraSeleccionada();
                        listaCamaraFiltradas.setListaCamaras(listaCamaras.getListaCamaras());
                        listaCamaraFiltradas.setListaCamaras(listaCamaraFiltradas.filter(""));
                        for (int i = 0; i< listaCamaraFiltradas.getNombreCamaras().size();i++)
                            actualizaListaCamaras(listaCamaraFiltradas);
                        ocultarDetalle();
                    }
                });

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        camaraViewModel.setBusqueda(s);
                        if(camaraViewModel.getListaCamaraSeleccionada() != null)
                            listaCamaras = camaraViewModel.getListaCamaraSeleccionada();
                        if(s.length() == 0){
                            buscar = false;
                            actualizaListaCamaras(listaCamaras);
                        }

                        else if (s.length() < textoAnterior.length()) {
                            buscar = true;
                            textoAnterior = s;
                            listaCamaraFiltradas.setListaCamaras(listaCamaras.getListaCamaras());// crea una nueva lista filtrada
                            listaCamaraFiltradas.setListaCamaras(listaCamaraFiltradas.filter(textoAnterior)); // filtra la lista temporal
                            for (int i = 0; i< listaCamaraFiltradas.getNombreCamaras().size();i++)
                                actualizaListaCamaras(listaCamaraFiltradas);
                        }else {
                            buscar = true;
                            textoAnterior = s;
                            listaCamaraFiltradas.setListaCamaras(listaCamaras.getListaCamaras());// crea una nueva lista filtrada
                            listaCamaraFiltradas.setListaCamaras(listaCamaraFiltradas.filter(s)); // filtra la lista temporal
                            actualizaListaCamaras(listaCamaraFiltradas);
                        }
                        camaraViewModel.setListaFiltrada(listaCamaraFiltradas);
                        return true;
                    }
                });
            }else if(searchView.getVisibility() == View.VISIBLE) searchView.setVisibility(View.GONE);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

    }
//        switch (id) {
//            case R.id.download:
//                // Inhabilitar botones del menú
//                for(int i = 0; i < menuOpciones.size(); i++) {
//                    menuOpciones.getItem(i).setEnabled(false);
//                }
//                HiloAnalisis hiloAnalisis = new HiloAnalisis(nombreFichero, instanciaFragmentoListado, true, true);
//                thread = new Thread(hiloAnalisis);
//                thread.start();
//                return true;
//            case R.id.mostrarUbicacion:
//                if(menuViewModel.getMostrarUbicacion())
//                    mostrarUbicacion = "Ubicación activada";
//                else
//                    mostrarUbicacion = "Ubicación desactivada";
//                infoModo.setText(mostrarUbicacion+" | "+modoUbicacion);
//                return true;
//            case R.id.mostrarAgrupación:
//                modoUbicacion = "Mostrar la agrupación";
//                infoModo.setText(mostrarUbicacion+" | "+modoUbicacion);
//                return true;
//            case R.id.mostrarTodas:
//                modoUbicacion = "Mostrar todas las cámaras";
//                infoModo.setText(mostrarUbicacion+" | "+modoUbicacion);
//                return true;
//            case R.id.mostrarUna:
//                modoUbicacion = "Mostrar una cámara";
//                infoModo.setText(mostrarUbicacion+" | "+modoUbicacion);
//                return true;
//
//            case R.id.order:
//                ordenar = true;
//                actualizaListaCamaras(listaCamaras);
//                ocultarDetalle();
//                return true;
//            case R.id.search:
//                if (searchView.getVisibility() == View.GONE) {
//                    searchView.setVisibility(View.VISIBLE);
//                    int searchCloseButtonId = searchView.getContext().getResources()
//                            .getIdentifier("android:id/search_close_btn", null, null);
//                    ImageView closeButton = (ImageView) this.searchView.findViewById(searchCloseButtonId);
//                    closeButton.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            searchView.setQuery("", false);
//                            camaraViewModel.setBusqueda("");
//                            buscar = false;
//                            if(camaraViewModel.getListaCamaraSeleccionada() != null)
//                                listaCamaras = camaraViewModel.getListaCamaraSeleccionada();
//                            listaCamaraFiltradas.setListaCamaras(listaCamaras.getListaCamaras());
//                            listaCamaraFiltradas.setListaCamaras(listaCamaraFiltradas.filter(""));
//                            for (int i = 0; i< listaCamaraFiltradas.getNombreCamaras().size();i++)
//                                actualizaListaCamaras(listaCamaraFiltradas);
//                            ocultarDetalle();
//                        }
//                    });
//
//                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//                        @Override
//                        public boolean onQueryTextSubmit(String s) {
//                            return false;
//                        }
//
//                        @Override
//                        public boolean onQueryTextChange(String s) {
//                            camaraViewModel.setBusqueda(s);
//                            if(camaraViewModel.getListaCamaraSeleccionada() != null)
//                                listaCamaras = camaraViewModel.getListaCamaraSeleccionada();
//                            if(s.length() == 0){
//                                buscar = false;
//                                actualizaListaCamaras(listaCamaras);
//                            }
//
//                            else if (s.length() < textoAnterior.length()) {
//                                buscar = true;
//                                textoAnterior = s;
//                                listaCamaraFiltradas.setListaCamaras(listaCamaras.getListaCamaras());// crea una nueva lista filtrada
//                                listaCamaraFiltradas.setListaCamaras(listaCamaraFiltradas.filter(textoAnterior)); // filtra la lista temporal
//                                for (int i = 0; i< listaCamaraFiltradas.getNombreCamaras().size();i++)
//                                    actualizaListaCamaras(listaCamaraFiltradas);
//                            }else {
//                                buscar = true;
//                                textoAnterior = s;
//                                listaCamaraFiltradas.setListaCamaras(listaCamaras.getListaCamaras());// crea una nueva lista filtrada
//                                listaCamaraFiltradas.setListaCamaras(listaCamaraFiltradas.filter(s)); // filtra la lista temporal
//                                actualizaListaCamaras(listaCamaraFiltradas);
//                            }
//                            camaraViewModel.setListaFiltrada(listaCamaraFiltradas);
//                            return true;
//                        }
//                    });
//                }else if(searchView.getVisibility() == View.VISIBLE) searchView.setVisibility(View.GONE);
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    /**
     * Infla el layout y recupera referencias a los elementos de a UI.
     * - Si hay una cámara seleccionada, muestra el fragmento de detalle con los datos correspondientes
     * - Si no se ha iniciado, gestiona la restauración/descarga del fichero de datos mediante un diálogo
     * - S ya hay datos cargados, actualiza la lista y gestiona la visibilidad de los contenedores
     * - Configura el listener de selección de elementos en la lista, lanzando el fragmento de detalle
     * y actualizando el ViewModel según corresponda.
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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragmento_listado, container, false);
        menuViewModel = new ViewModelProvider(getActivity()).get(MenuViewModel.class);
        // Recuperar las referencias a las vistas
        progreso = getActivity().findViewById(R.id.progreso);
        mensajeProgreso = getActivity().findViewById(R.id.textoProgreso);
        listViewCamaras = view.findViewById(R.id.listaCamaras);
        contenedorListaCamaras = view.findViewById(R.id.contenedorListaCamaras);
        searchView = view.findViewById(R.id.search_view);
        contenedorDetalle = getActivity().findViewById(R.id.contenedorDetalleCamaras);
        infoModo = view.findViewById(R.id.infoModo);
        if (camaraViewModel.getCamaraSeleccionada() != null) {
            FragmentoDetalle fragmentoDetalle = new FragmentoDetalle();
            Bundle parametros = new Bundle();
            parametros.putString("nombre", camaraViewModel.getCamaraSeleccionada().getNombre());
            parametros.putString("coordenadas", camaraViewModel.getCamaraSeleccionada().getCoordenadas());
            parametros.putString("url", camaraViewModel.getCamaraSeleccionada().getURL());
            fragmentoDetalle.setArguments(parametros);
            // Establecer la visibilidad del contenedor de detalle a VISIBLE
            if(camaraViewModel.getListaCamaraFiltrada() !=null){
                buscar = true;
                listaCamaras = camaraViewModel.getListaCamaraSeleccionada();
                actualizaListaCamaras(camaraViewModel.getListaCamaraFiltrada());

            }else
                actualizaListaCamaras(camaraViewModel.getListaCamaraSeleccionada());
            contenedorDetalle.setVisibility(View.VISIBLE);
            listViewCamaras.setSelection(camaraViewModel.getPosicionCamara());
        } else {
            if(!iniciado){
                contenedorListaCamaras.setVisibility(View.GONE);
                contenedorDetalle.setVisibility(View.GONE);
                progreso.setVisibility(View.VISIBLE);
                File kmlExiste = new File(this.getContext().getFilesDir() + "/camaras/CamarasMadrid.kml");
                String stringFecha = "";
                if (kmlExiste.exists()){
                    almacen = getContext().getSharedPreferences("datos", Context.MODE_PRIVATE);
                    if(almacen.contains(("fecha"))) stringFecha = almacen.getString("fecha", "");
                    AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
                    builder.setTitle("Existe un fichero KML con fecha: " + stringFecha + " en el almacenamiento.");
                    builder.setMessage("¿Quieres restaurar el fichero del almacenamiento o descargarlo de nuevo?");
                    builder.setPositiveButton("Descargar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            HiloAnalisis hiloAnalisis = new HiloAnalisis(nombreFichero, instanciaFragmentoListado, true, false);
                            thread = new Thread(hiloAnalisis);
                            thread.start();
                            iniciado = true;
                        }
                    });


                    builder.setNegativeButton("Restaurar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            HiloAnalisis hiloAnalisis = new HiloAnalisis(nombreFichero, instanciaFragmentoListado, false, false);
                            thread = new Thread(hiloAnalisis);
                            thread.start();
                            iniciado = true;
                        }
                    });
                    builder.setCancelable(false); // Evita que al pulsar fuera
                    builder.show();

                } else {
                    HiloAnalisis hiloAnalisis = new HiloAnalisis(nombreFichero, this, true, false);
                    thread = new Thread(hiloAnalisis);
                    thread.start();
                    iniciado = true;
                }
            } else {
                if(camaraViewModel.getListaCamaraFiltrada() !=null){
                    buscar = true;
                    listaCamaras = camaraViewModel.getListaCamaraSeleccionada();
                    actualizaListaCamaras(camaraViewModel.getListaCamaraFiltrada());

                }else
                    actualizaListaCamaras(camaraViewModel.getListaCamaraSeleccionada());
            }
            camaraViewModel.setListaCamaras(listaCamaras);

        }
        listViewCamaras.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int posicion, long id) {

                if(buscar == true) {
                    Camara camaraClickada = camaraViewModel.getListaCamaraFiltrada().getListaCamaras().get(posicion);
                    coordenadas = listaCamaras.getAllCoordenadas();
                    posicionSeleccionada = posicion;
                    camaraViewModel.setCamaraSeleccionada(camaraClickada, posicionSeleccionada, listaCamaras);
                    FragmentoDetalle fragmentoDetalle = new FragmentoDetalle();
                    Bundle parametros = new Bundle();
                    parametros.putString("nombre", camaraClickada.getNombre());
                    parametros.putString("coordenadas", camaraClickada.getCoordenadas());
                    parametros.putString("url", camaraClickada.getURL());
                    parametros.putStringArray("allCordenadas", coordenadas);
                    fragmentoDetalle.setArguments(parametros);
                    if (getParentFragmentManager().findFragmentById(R.id.contenedorDetalleCamaras) != null) {
                        getParentFragmentManager().beginTransaction().replace(R.id.contenedorDetalleCamaras, fragmentoDetalle).commit();
                    } else {
                        getParentFragmentManager().beginTransaction().add(R.id.contenedorDetalleCamaras, fragmentoDetalle).commit();
                    }
                    contenedorDetalle.setVisibility(View.VISIBLE);
                }else {
                    Camara camaraClickada = listaCamaras.getListaCamaras().get(posicion);
                    coordenadas = listaCamaras.getAllCoordenadas();
                    posicionSeleccionada = posicion;
                    camaraViewModel.setCamaraSeleccionada(camaraClickada, posicionSeleccionada, listaCamaras);
                    FragmentoDetalle fragmentoDetalle = new FragmentoDetalle();
                    Bundle parametros = new Bundle();
                    parametros.putString("nombre", camaraClickada.getNombre());
                    parametros.putString("coordenadas", camaraClickada.getCoordenadas());
                    parametros.putString("url", camaraClickada.getURL());
                    parametros.putStringArray("allCordenadas", coordenadas);
                    fragmentoDetalle.setArguments(parametros);
                    if (getParentFragmentManager().findFragmentById(R.id.contenedorDetalleCamaras) != null) {
                        getParentFragmentManager().beginTransaction().replace(R.id.contenedorDetalleCamaras, fragmentoDetalle).commit();
                    } else {
                        getParentFragmentManager().beginTransaction().add(R.id.contenedorDetalleCamaras, fragmentoDetalle).commit();
                    }
                    contenedorDetalle.setVisibility(View.VISIBLE);
                }
            }
        });
        if (savedInstanceState != null) {
            // Verificar si existe la clave "posicionSeleccionada" en el Bundle
            if (savedInstanceState.containsKey("posicionSeleccionada")) {
                // Restaurar la posición seleccionada del Bundle
                posicionSeleccionada = savedInstanceState.getInt("posicionSeleccionada", -1);
                if (posicionSeleccionada != -1) {
                    listViewCamaras.post(new Runnable() {
                        @Override
                        public void run() {
                            listViewCamaras.setSelection(posicionSeleccionada);
                            listViewCamaras.setItemChecked(posicionSeleccionada, true);
                        }
                    });
                }
            }
        }
        return view;
    }

    /**
     * Guarda el estado relevante (posición seleccionada, estado de inicio, modo de orden) para
     * restauración tras recreación del fragmento.
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Guardar la posición seleccionada en el bundle
        outState.putInt("posicionSeleccionada", posicionSeleccionada);
        outState.putBoolean("iniciado", iniciado);
        outState.putString("orden", modoOrden);
    }

    /**
     * Actualiza la interfaz de usuario con la lista de cámaras más reciente, gestionando
     * el adaptador de la lista, la visibilidad de los elementos de la interfaz y el estado de
     * selección.
     * @param listadoCamaras
     */
    public void actualizaListaCamaras (ListaCamaras listadoCamaras) {
        if (listadoCamaras != null) {
            if (ordenar){
                if (modoOrden == "Z-A") {
                    Collections.sort(listadoCamaras.getListaCamaras());
                    modoOrden = "A-Z";
                } else if (modoOrden == "A-Z") {
                    Collections.sort(listadoCamaras.getListaCamaras(), Collections.reverseOrder());
                    modoOrden = "Z-A";
                } else {
                    Collections.sort(listadoCamaras.getListaCamaras());
                    modoOrden = "A-Z";
                }
                ordenar = false;
            }
            progreso.setVisibility(View.GONE);
            if(menuOpciones != null) {
                for (int i = 0; i < menuOpciones.size(); i++) menuOpciones.getItem(i).setEnabled(true);
            }
            contenedorListaCamaras.setVisibility(View.VISIBLE);
            contenedorDetalle.setVisibility(View.GONE);
            
            if(buscar == false) {
                listaCamaras = listadoCamaras;
                camaraViewModel.setListaCamaras(listaCamaras);
            }
            adaptador = new Adaptador(getContext(), listadoCamaras.getNombreCamaras());
            listViewCamaras.setAdapter(adaptador);

        }
    }

    /**
     * Oculta el fragmento de detalle de cámara y restaura la vista principal de la lista.
     */
    public void ocultarDetalle() {
        contenedorListaCamaras.setVisibility(View.VISIBLE);
        contenedorDetalle.setVisibility(View.GONE);
        posicionSeleccionada = -1;
        listViewCamaras.setItemChecked(posicionSeleccionada, true);
        camaraViewModel.desseleccionarCamara();
        posicionSeleccionada = -1;
    }

    /**
     * Cambia el texto de carga.
     * @param texto
     */
    public void cambiarTextoCarga(String texto){
        mensajeProgreso.setText(texto);
    }

}
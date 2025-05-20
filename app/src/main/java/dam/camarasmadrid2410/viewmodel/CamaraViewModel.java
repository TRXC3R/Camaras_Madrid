package dam.camarasmadrid2410.viewmodel;

import androidx.lifecycle.ViewModel;

import dam.camarasmadrid2410.objetos.Camara;
import dam.camarasmadrid2410.objetos.ListaCamaras;
/**
 * Grupo: 10
 * Alumnos:
 * - Darío Márquez Ibáñez
 * - David Márquez Ibáñez
 */

/**
 * La calse 'CamaraViewModel' implementa un ViewModel para gestionar el estado y los datos relacionados
 * con cámaras dentro de una arquitectura MVVM (Model-View-ViewModel). Permite almacenar y manipular
 * la cámara seleccionada, la lista completa de cámaras, una lista filtrada, la posición de la cámara
 * seleccionada y el término de búsqueda actual.
 */

/**
 * Extiende de 'androidx.lifecycle.ViewModel', lo que permite que los datos sobrevivan a los cambios
 * de configuración (como rotaciones de pantalla) y sean compartidos entre fragments o actividades.
 */
public class CamaraViewModel extends ViewModel {
    private Camara camaraSeleccionada; // Referencia a la cámara actualmente seleccionada
    private ListaCamaras listado, filtrada; // Lista principal de cámaras y lista filtrada de cámaras
    private int posicionCamara; // Posición de la cámara seleccionada en la lista
    private String busqueda = ""; // Término de búsqueda actual, inicializado como cadena vacía

    /**
     * Establece la cámara seleccionada, su posición y la lista de cámaras asociada.
     * @param camara
     * @param posicion
     * @param listadoCamaras
     */
    public void setCamaraSeleccionada(Camara camara,int posicion, ListaCamaras listadoCamaras){
        posicionCamara = posicion;
        listado = listadoCamaras;
        camaraSeleccionada = camara;
    }

    /**
     * Deselecciona la cámara, estableciendo la posición en -1 y la referencia a la cámara en 'null'
     */
    public void desseleccionarCamara(){
        posicionCamara = -1;
        camaraSeleccionada = null;
    }

    /**
     * Asigna una nueva lista principal de cámaras.
     * @param listadoCamaras
     */
    public void setListaCamaras(ListaCamaras listadoCamaras){
        listado = listadoCamaras;
    }

    /**
     * Asigna un nueva lista filtrada de cámaras.
     * @param listadoCamaras
     */
    public void setListaFiltrada(ListaCamaras listadoCamaras){
        filtrada = listadoCamaras;
    }

    /**
     * Devuelve la lista principal de cámaras.
     * @return listado de cámaras
     */
    public ListaCamaras getListaCamaraSeleccionada() {
        return listado;
    }

    /**
     * Devuelve la lista filtrada de cámaras.
     * @return listado filtrado de cámaras
     */
    public ListaCamaras getListaCamaraFiltrada() {
        return filtrada;
    }

    /**
     * Devuelve la cámara actualmente seleccionada.
     * @return cámara seleccionada
     */
    public Camara getCamaraSeleccionada() {
        return camaraSeleccionada;
    }

    /**
     * Devuelve la posición de la cámara seleccionada.
     * @return posición cámara seleccionada
     */
    public int getPosicionCamara(){
        return posicionCamara;
    }

    /**
     * Devuelve el término de búsqueda actual.
     * @return término de búsqueda
     */
    public String getBusqueda(){return busqueda;};

    /**
     * Establece el término de búsqueda.
     * @param s
     */
    public void setBusqueda(String s){busqueda = s;}
}

package dam.camarasmadrid2410.cluster;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
/**
 * Grupo: 10
 * Alumnos:
 * - Darío Márquez Ibáñez
 * - David Márquez Ibáñez
 */

/**
 * La clase implementa la interfaz 'ClusterItem' de la librería de utilidades de Google Maps.
 * Su proposito principal es represntar un elemento individual que puede ser agrupado (clusterizado)
 * en un mapa de Google Maps utilizando la funcionalidad de clustering.
 */
public class MyItem implements ClusterItem {
    private final LatLng position;  // Almacena la posición geográfica (latitud y longitud) del elemento
    private final String title;     // Título descriptivo del elemento
    private final String snippet;   // Información adicional o descripción breve

    public MyItem(double lat, double lng, String title, String snippet) {
        position = new LatLng(lat, lng);
        this.title = title;
        this.snippet = snippet;
    }

    /**
     * Devuelve el objeto 'LatLng' que representa la posición del elemento
     * Implementación requerida por la interfaz 'ClusterItem'
     * @return objeto position 'LatLng'
     */
    @Override
    public LatLng getPosition() {
        return position;
    }

    /**
     * Devuelve el título del elemento.
     * @return objeto title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Devuelve el snippet o descripción adicional.
     * @return objeto snippet
     */
    public String getSnippet() {
        return snippet;
    }

    /**
     * Devuelve un valor flotante ('0f') que puede ser utilizado para definir el orden de apilamiento
     * (z-index) de los elementos en el mapa.
     * Está anotado como '@Nullable', indicando que puede devolver 'null', aunque en este caso
     * siempre retorna '0f'
     * @return 0f
     */
    @Nullable
    public Float getZIndex() {
        return 0f;
    }
}


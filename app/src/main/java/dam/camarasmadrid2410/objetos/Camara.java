package dam.camarasmadrid2410.objetos;

import android.net.Uri;
/**
 * Grupo: 10
 * Alumnos:
 * - Darío Márquez Ibáñez
 * - David Márquez Ibáñez
 */

/**
 * La clase 'Camara' representa un objeto de dominio para una cámara y está diseñada para almacenar
 * información básica sobre cada cámara, como su nombre, coordenadas y una URL asociada.
 */
public class Camara implements Comparable{

    String nombre;          // Almacena el nombre de la cámara
    String coordenadas;     // Almacena las coordenadas geográficas de la cámara
    Uri URL;                // Almacena la URL asociadada a la cámara como un objeto 'Uri'

    public Camara(String nombre, String coordenadas, String URL){
        this.nombre = nombre;
        this.coordenadas = coordenadas;
        this.URL = Uri.parse(URL);
    }

    /**
     * Devuelve el nombre de la cámara.
     * @return nombre de la cámara
     */
    public String getNombre() { return nombre; }

    /**
     * Devuelve las coordenadas de la cámara.
     * @return coordenadas de la cámara
     */
    public String getCoordenadas() { return coordenadas; }

    /**
     * Devuelve la URL de la cámara como 'String'.
     * @return URL de la cámara como 'String'
     */
    public String getURL() { return URL.toString(); }

    /**
     * Implementación de la interfaz 'Comparable'.
     * @param o
     * @return 0 si se cumple que son iguales, negativo si es más pequeño y positivo si es más grande
     * léxicamente hablando
     */
    @Override
    public int compareTo(Object o) {
        if (o instanceof Camara) {
            Camara other = (Camara) o;
            return this.nombre.compareTo(other.nombre);
        }
        throw new ClassCastException("No se puede comparar una Camara con un objeto de otra clase");
    }

}

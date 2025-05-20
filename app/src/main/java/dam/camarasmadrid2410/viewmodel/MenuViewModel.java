package dam.camarasmadrid2410.viewmodel;

import androidx.lifecycle.ViewModel;
/**
 * Grupo: 10
 * Alumnos:
 * - Darío Márquez Ibáñez
 * - David Márquez Ibáñez
 */

/**
 * La clase 'MenuViewModel' es un ViewModel con la funcion principal de gestionar el estado de dos
 * parámetros relacionados con la visualización de información en la interfaz de usuario: si se debe
 * mostrar la ubicacion y el modo de visualización actual.
 */
public class MenuViewModel extends ViewModel {
  // Indica si se debe mostrar la ubicación (valor por defecto 'false')
  private boolean mostrarUbicacion = false;
  // Almacena el modo de visualización actual, inicializado como 'mostrarUna'
  private String modoMostrar = "mostrarUna";

  /**
   * Establece el valor de 'mostrarUbicación'
   * @param b
   */
  public void setMostrarUbicacion (boolean b){mostrarUbicacion = b;}

  /**
   * Devuelve el valor de 'mostrarUbicación'
   * @return
   */
  public boolean getMostrarUbicacion(){return mostrarUbicacion;}

  /**
   * Establece el valor de 'modoMostrar'
   * @param s
   */
  public void setModoMostrar (String s){modoMostrar = s;}

  /**
   * Devuelve el valor actual de 'modoMostrar'
   * @return
   */
  public String getModoMostrar (){return modoMostrar;}

}

package dam.camarasmadrid2410.viewmodel;

import androidx.lifecycle.ViewModel;
/**
 * Grupo: 10
 * Alumnos:
 * - Darío Márquez Ibáñez
 * - David Márquez Ibáñez
 */

/**
 * La clase 'MapaViewModel' es un ViewModel con la función principal de almacenar y proporcionar el
 * estado del modo de visualización del mapa, permitiendoque este estado sobreviva a cambios de
 * configuración como la rotación de pantalla.
 */
public class MapaViewModel extends ViewModel {
    // Alacena el modo actual del mapa. El valor por defecto es -1, lo que indica que no se ha seleccionado
    // ningún modo.
    private int modeMap = -1;

    /**
     * Establece el modo del mapa.
     * @param modeMap
     */
    public void setModeMap ( int modeMap){
      this.modeMap = modeMap;
    }

    /**
     * Devuelve el modo actual del mapa.
     * @return
     */
    public int getModeMap (){
      return modeMap;
    }
}

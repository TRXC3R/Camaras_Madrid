package dam.camarasmadrid2410.objetos;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import dam.camarasmadrid2410.R;
/**
 * Grupo: 10
 * Alumnos:
 * - Darío Márquez Ibáñez
 * - David Márquez Ibáñez
 */

/**
 * La clase 'Adaptador' es un 'ArrayAdapter' personalizado para mostrar elementos en un 'ListView'.
 * Muestra elementos de cámaras con un patrón de colores alternado en los elementos de la lista.
 * Extiende 'ArrayAdapter'
 */
public class Adaptador extends ArrayAdapter<String> {
    private ArrayList<String> camaras; // Lista de datos a mostrar
    private final LayoutInflater inflador; // Para inflar layouts XML.
    public Adaptador (Context contexto, ArrayList<String> camaras) {
        super (contexto,0, camaras); // Invocar al constructor de ArrayAdapter, pasando un 0 en el
        this.camaras = camaras; //2º parámetro (el layout a usar) porque ahora vamos a usar el nuestro
        inflador = (LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Consturye y devuelve la vista para cada elemento de la lista
     * @param posicion
     * @param vistaReciclada
     * @param padre
     * @return
     */
    @Override
    public View getView(int posicion, View vistaReciclada, ViewGroup padre ) {
        // Reutiliza 'vistaReciclada' si existe, evitando inflar layouts innecesariamente.
        if (vistaReciclada==null) {
            // Usa el layout personalizado.
            vistaReciclada = inflador.inflate(R.layout.elemento_lista, padre,false);
        }
        TextView nombreCamara = vistaReciclada.findViewById(R.id.elementoLista);
        String nombre = getItem(posicion);
        nombreCamara.setText(nombre);
        // Color de fondo alternado en la lista.
        vistaReciclada.setBackgroundColor(posicion % 2 == 0 ? Color.parseColor("#E2E2E2") : Color.parseColor("#FFFFFF"));
        return vistaReciclada;
    }
}
package dam.camarasmadrid2410.objetos;

import java.util.ArrayList;
/**
 * Grupo: 10
 * Alumnos:
 * - Darío Márquez Ibáñez
 * - David Márquez Ibáñez
 */

 //El proposito de ListaCamaras de contener y gestionar una lista de objetos Camara.
 //Añadir y listar camaras

public class ListaCamaras {
  private ArrayList<Camara> listaCamaras;

  public ListaCamaras() {
    listaCamaras = new ArrayList<>();
  }

  //Por seguridad evitar añadir null
  public void addCamara(Camara camara) {
    if (camara != null) {
      listaCamaras.add(camara);
    }
  }

  public ArrayList<Camara> getListaCamaras() {
    return listaCamaras;
  }

  public void setListaCamaras(ArrayList<Camara> lista){
    listaCamaras = lista;
  }
  public ArrayList<String> getNombreCamaras() {
    ArrayList<String> camaras = new ArrayList<>();
    for (Camara camara: listaCamaras){
      camaras.add(camara.getNombre());
    }
    return camaras;
  }
  public String[] getAllCoordenadas() {
    String[] coordenadas = new String[listaCamaras.size()]; // Inicializar el arreglo con elementos
    for (int i = 0; i < listaCamaras.size(); i++) {
      coordenadas[i] = listaCamaras.get(i).getCoordenadas();
    }
    return coordenadas;
  }


  public ArrayList<Camara> filter(String searchTerm) {
    ArrayList<Camara> filteredList = new ArrayList<>();

      for (Camara camara : listaCamaras) {
        if (camara.getNombre().toLowerCase().contains(searchTerm.toLowerCase())) {
          filteredList.add(camara);
        }
      }

    return filteredList;
  }

}
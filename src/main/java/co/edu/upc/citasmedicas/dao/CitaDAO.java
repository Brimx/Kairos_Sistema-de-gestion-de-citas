package co.edu.upc.citasmedicas.dao;

import co.edu.upc.citasmedicas.model.Cita;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para la entidad Cita.
 */
public class CitaDAO {

    /**
     * Guarda o actualiza una cita en la base de datos.
     * 
     * @param cita cita a guardar
     */
    public void guardar(Cita cita) {
        System.out.println("[DAO] Guardando cita con ID: " + cita.getId());
    }

    /**
     * Busca una cita por su identificador único.
     * 
     * @param id identificador
     * @return Cita o null si no se encuentra
     */
    public Cita buscarPorId(String id) {
        System.out.println("[DAO] Buscando cita por ID: " + id);
        return null;
    }

    /**
     * Retorna todas las citas registradas.
     * 
     * @return List de Citas
     */
    public List<Cita> obtenerTodas() {
        System.out.println("[DAO] Listando todas las citas");
        return new ArrayList<>();
    }
}

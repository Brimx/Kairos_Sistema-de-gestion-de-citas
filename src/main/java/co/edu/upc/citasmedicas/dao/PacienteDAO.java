package co.edu.upc.citasmedicas.dao;

import co.edu.upc.citasmedicas.model.Paciente;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para la entidad Paciente.
 */
public class PacienteDAO {

    /**
     * Guarda o actualiza un paciente en la base de datos.
     * 
     * @param paciente paciente a guardar
     */
    public void guardar(Paciente paciente) {
        System.out.println("[DAO] Guardando paciente: " + paciente.getNombre() + " " + paciente.getApellido());
    }

    /**
     * Busca un paciente por su identificador único.
     * 
     * @param id identificador
     * @return Paciente o null si no se encuentra
     */
    public Paciente buscarPorId(String id) {
        System.out.println("[DAO] Buscando paciente por ID: " + id);
        return null;
    }

    /**
     * Retorna una lista con todos los pacientes.
     * 
     * @return List de Pacientes
     */
    public List<Paciente> obtenerTodos() {
        System.out.println("[DAO] Listando todos los pacientes");
        return new ArrayList<>();
    }
}

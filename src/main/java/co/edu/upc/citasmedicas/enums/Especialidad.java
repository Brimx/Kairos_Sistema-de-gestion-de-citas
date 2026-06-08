package co.edu.upc.citasmedicas.enums;

/**
 * Lista de especialidades médicas disponibles en el sistema EPS.
 * Cada médico tiene asignada una especialidad de esta lista.
 * Cada cita también referencia una especialidad.
 */
public enum Especialidad {
    MEDICINA_GENERAL,
    PEDIATRIA,
    GINECOLOGIA,
    CARDIOLOGIA,
    DERMATOLOGIA,
    ORTOPEDIA,
    NEUROLOGIA,
    OFTALMOLOGIA,
    ODONTOLOGIA,
    PSICOLOGIA;

    public String getNombre() {
        String[] partes = name().toLowerCase().split("_");
        StringBuilder builder = new StringBuilder();
        for (String parte : partes) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(parte.charAt(0))).append(parte.substring(1));
        }
        return builder.toString();
    }
}

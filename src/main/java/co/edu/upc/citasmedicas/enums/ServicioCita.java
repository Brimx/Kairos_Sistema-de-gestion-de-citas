package co.edu.upc.citasmedicas.enums;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public enum ServicioCita {

    MEDICINA_GENERAL("Consulta Externa General", Especialidad.MEDICINA_GENERAL),
    ODONTOLOGIA_GENERAL("Consulta Externa General", Especialidad.ODONTOLOGIA),
    OPTOMETRIA("Consulta Externa General", Especialidad.OFTALMOLOGIA),

    PEDIATRIA("Pediatria y Medicina Familiar", Especialidad.PEDIATRIA),
    MEDICINA_FAMILIAR("Pediatria y Medicina Familiar", Especialidad.MEDICINA_FAMILIAR),

    CRECIMIENTO_DESARROLLO("Programas de Promocion y Prevencion", Especialidad.PEDIATRIA),
    ASESORIA_LACTANCIA("Programas de Promocion y Prevencion", Especialidad.PEDIATRIA),

    GINECOLOGIA("Especialidades Medicas", Especialidad.GINECOLOGIA),
    DERMATOLOGIA("Especialidades Medicas", Especialidad.DERMATOLOGIA),
    PSICOLOGIA("Especialidades Medicas", Especialidad.PSICOLOGIA),
    PSIQUIATRIA("Especialidades Medicas", Especialidad.PSIQUIATRIA),
    CARDIOLOGIA("Especialidades Medicas", Especialidad.CARDIOLOGIA),
    ORTOPEDIA_TRAUMATOLOGIA("Especialidades Medicas", Especialidad.ORTOPEDIA),

    NUTRICION_DIETETICA("Servicios de Apoyo y Terapias", Especialidad.NUTRICION),
    FISIOTERAPIA("Servicios de Apoyo y Terapias", Especialidad.FISIATRIA),
    FONOAUDIOLOGIA("Servicios de Apoyo y Terapias", Especialidad.FONOAUDIOLOGIA),
    TERAPIA_OCUPACIONAL("Servicios de Apoyo y Terapias", Especialidad.TERAPIA_OCUPACIONAL),

    ENFERMERIA_GENERAL("Procedimientos y Apoyo Diagnostico", Especialidad.MEDICINA_GENERAL),
    VACUNACION("Procedimientos y Apoyo Diagnostico", Especialidad.PEDIATRIA),
    LABORATORIO_CLINICO("Procedimientos y Apoyo Diagnostico", Especialidad.MEDICINA_GENERAL);

    private final String grupo;
    private final Especialidad especialidadRequerida;

    ServicioCita(String grupo, Especialidad especialidadRequerida) {
        this.grupo = grupo;
        this.especialidadRequerida = especialidadRequerida;
    }

    public String getGrupo() {
        return grupo;
    }

    public Especialidad getEspecialidadRequerida() {
        return especialidadRequerida;
    }

    public String getNombre() {
        String[] partes = name().toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : partes) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }

    public static List<ServicioCita> porGrupo(String grupo) {
        List<ServicioCita> result = new ArrayList<>();
        for (ServicioCita s : values()) {
            if (s.getGrupo().equals(grupo)) {
                result.add(s);
            }
        }
        return result;
    }

    public static List<String> grupos() {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (ServicioCita s : values()) {
            set.add(s.getGrupo());
        }
        return new ArrayList<>(set);
    }
}

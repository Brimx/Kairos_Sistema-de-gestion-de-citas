package co.edu.upc.citasmedicas.enums;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public enum ServicioCita {

    MEDICINA_GENERAL("Consulta Externa General", Especialidad.MEDICINA_GENERAL, 20, 15),
    ODONTOLOGIA_GENERAL("Consulta Externa General", Especialidad.ODONTOLOGIA, 30, 20),
    OPTOMETRIA("Consulta Externa General", Especialidad.OFTALMOLOGIA, 30, 20),

    PEDIATRIA("Pediatria y Medicina Familiar", Especialidad.PEDIATRIA, 20, 15),
    MEDICINA_FAMILIAR("Pediatria y Medicina Familiar", Especialidad.MEDICINA_FAMILIAR, 20, 15),

    CRECIMIENTO_DESARROLLO("Programas de Promocion y Prevencion", Especialidad.PEDIATRIA, 30, 20),
    ASESORIA_LACTANCIA("Programas de Promocion y Prevencion", Especialidad.PEDIATRIA, 30, 20),

    GINECOLOGIA("Especialidades Medicas", Especialidad.GINECOLOGIA, 30, 20),
    DERMATOLOGIA("Especialidades Medicas", Especialidad.DERMATOLOGIA, 20, 15),
    PSICOLOGIA("Especialidades Medicas", Especialidad.PSICOLOGIA, 45, 30),
    PSIQUIATRIA("Especialidades Medicas", Especialidad.PSIQUIATRIA, 45, 30),
    CARDIOLOGIA("Especialidades Medicas", Especialidad.CARDIOLOGIA, 30, 20),
    ORTOPEDIA_TRAUMATOLOGIA("Especialidades Medicas", Especialidad.ORTOPEDIA, 30, 20),

    NUTRICION_DIETETICA("Servicios de Apoyo y Terapias", Especialidad.NUTRICION, 30, 20),
    FISIOTERAPIA("Servicios de Apoyo y Terapias", Especialidad.FISIATRIA, 30, 20),
    FONOAUDIOLOGIA("Servicios de Apoyo y Terapias", Especialidad.FONOAUDIOLOGIA, 30, 20),
    TERAPIA_OCUPACIONAL("Servicios de Apoyo y Terapias", Especialidad.TERAPIA_OCUPACIONAL, 30, 20),

    ENFERMERIA_GENERAL("Procedimientos y Apoyo Diagnostico", Especialidad.MEDICINA_GENERAL, 15, 10),
    VACUNACION("Procedimientos y Apoyo Diagnostico", Especialidad.PEDIATRIA, 15, 10),
    LABORATORIO_CLINICO("Procedimientos y Apoyo Diagnostico", Especialidad.MEDICINA_GENERAL, 20, 15);

    private final String grupo;
    private final Especialidad especialidadRequerida;
    private final int duracionMinutos;
    private final int duracionControlMinutos;

    ServicioCita(String grupo, Especialidad especialidadRequerida, int duracionMinutos, int duracionControlMinutos) {
        this.grupo = grupo;
        this.especialidadRequerida = especialidadRequerida;
        this.duracionMinutos = duracionMinutos;
        this.duracionControlMinutos = duracionControlMinutos;
    }

    public String getGrupo() {
        return grupo;
    }

    public Especialidad getEspecialidadRequerida() {
        return especialidadRequerida;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public int getDuracionControlMinutos() {
        return duracionControlMinutos;
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

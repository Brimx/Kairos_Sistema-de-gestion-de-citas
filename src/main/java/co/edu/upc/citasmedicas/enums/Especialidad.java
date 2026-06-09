package co.edu.upc.citasmedicas.enums;

public enum Especialidad {
    MEDICINA_GENERAL,
    MEDICINA_FAMILIAR,
    PEDIATRIA,
    GINECOLOGIA,
    CARDIOLOGIA,
    DERMATOLOGIA,
    ORTOPEDIA,
    NEUROLOGIA,
    OFTALMOLOGIA,
    ODONTOLOGIA,
    PSICOLOGIA,
    PSIQUIATRIA,
    NUTRICION,
    FISIATRIA,
    FONOAUDIOLOGIA,
    TERAPIA_OCUPACIONAL;

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

package co.edu.upc.citasmedicas.enums;

public enum TipoCita {
    PRESENCIAL("Presencial"),
    VIRTUAL("Virtual");

    private final String nombre;

    TipoCita(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public static TipoCita fromNombre(String nombre) {
        return valueOf(nombre.toUpperCase().replace(' ', '_').replace('-', '_').replace('/', '_'));
    }
}

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
}

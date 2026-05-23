package co.edu.upc.citasmedicas;

import co.edu.upc.citasmedicas.enums.Especialidad;
import co.edu.upc.citasmedicas.enums.EstadoCita;
import co.edu.upc.citasmedicas.enums.TipoCita;
import co.edu.upc.citasmedicas.model.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Punto de entrada del sistema EPS.
 *
 * Por ahora solo verifica que todos los modelos funcionan correctamente.
 * En el Hito 4 este Main será reemplazado por el arranque de JavaFX.
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("=== SISTEMA DE CITAS MÉDICAS EPS ===\n");

        // --- Crear un Paciente de prueba ---
        Paciente paciente = new Paciente(
                "P-001",
                "Santiago", "García",
                "santiago@email.com", "pass123", "3001234567",
                "CC", "1234567890",
                LocalDate.of(2002, 5, 15),
                "Calle 10 #5-20, Valledupar",
                "EPS Sura"
        );

        // --- Crear un Médico de prueba ---
        Medico medico = new Medico(
                "M-001",
                "Carlos", "Martínez",
                "dr.martinez@eps.com", "medico123", "3109876543",
                "REG-12345", Especialidad.MEDICINA_GENERAL, "Consultorio 3"
        );

        // --- Crear un Administrador de prueba ---
        Administrador admin = new Administrador(
                "A-001",
                "Laura", "Pérez",
                "admin@eps.com", "admin123", "3155551234",
                "ADM-001", "Coordinadora de Agenda"
        );

        // --- Crear una Cita de prueba ---
        Cita cita = new Cita(
                "C-001",
                paciente, medico,
                Especialidad.MEDICINA_GENERAL,
                LocalDate.now().plusDays(2),
                LocalTime.of(9, 0),
                TipoCita.PRESENCIAL,
                "Dolor de cabeza frecuente"
        );

        // --- Agregar cita al historial del paciente y agenda del médico ---
        cita.confirmar();
        paciente.agregarCitaAlHistorial(cita);
        medico.agregarCitaAAgenda(cita);

        // --- Mostrar resultados en consola ---
        System.out.println("PACIENTE CREADO:");
        System.out.println(paciente);
        System.out.println();

        System.out.println("MÉDICO CREADO:");
        System.out.println(medico);
        System.out.println();

        System.out.println("ADMINISTRADOR CREADO:");
        System.out.println(admin);
        System.out.println();

        System.out.println("CITA CREADA:");
        System.out.println(cita);
        System.out.println();

        System.out.println("ÚLTIMA CITA DEL PACIENTE (Stack.peek):");
        System.out.println(paciente.verUltimaCita());
        System.out.println();

        System.out.println("SIGUIENTE EN AGENDA DEL MÉDICO (Queue.peek):");
        System.out.println(medico.verSiguientePaciente());
        System.out.println();

        System.out.println("MENÚ DEL PACIENTE:");
        for (String opcion : paciente.getMenuOpciones()) {
            System.out.println("  " + opcion);
        }

        System.out.println("\n=== TODOS LOS MODELOS FUNCIONAN CORRECTAMENTE ===");
    }
}

package co.edu.upc.citasmedicas.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class InasistenciaService {

    private static final InasistenciaService INSTANCE = new InasistenciaService();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "inasistencias-detector");
        t.setDaemon(true);
        return t;
    });
    private boolean iniciado;

    private InasistenciaService() {}

    public static InasistenciaService getInstance() {
        return INSTANCE;
    }

    public void iniciar(CitaService citaService) {
        if (iniciado) return;
        iniciado = true;
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                citaService.autoDetectarInasistencias();
            } catch (Exception ignored) {}
        }, 0, 5, TimeUnit.MINUTES);
    }

    public void detener() {
        scheduler.shutdown();
    }
}

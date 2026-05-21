package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;
import sua.autonomouscar.driving.interfaces.IDrivingService;
import sua.autonomouscar.infraestructure.driving.DrivingService;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Probe;
import es.upv.pros.tatami.osgi.utils.components.OSGiUtils;

public class SondaDrivingService extends Probe {

	public static String ID = "Sonda Driving Service";
	private Thread hilo;
	private boolean stop = false;
	private String ultimoServicio = "";

	public SondaDrivingService(BundleContext context) {
		super(context, ID);
		this.hilo = new Thread(() -> {
			while (!stop) {
				try {
					Thread.sleep(2000);
					
					// Comprobación de seguridad: si el contexto es nulo, no buscar todavía
					if (this.getBundleContext() == null) continue;

					String filter = String.format("(%s=true)", DrivingService.ACTIVE);
					IDrivingService service = OSGiUtils.getService(this.getBundleContext(), IDrivingService.class, filter);
					
					//String idActual = (service != null) ? service.getId() : "NONE";
					// REEMPLAZA todo el bloque anterior por esto:
					String idActual = "NONE";
					if (service != null) {
					    String sid = service.getId();
					    if ("L3_CityChauffer".equals(sid))            idActual = "driving.L3.CityChauffer";
					    else if ("L3_HighwayChauffer".equals(sid))    idActual = "driving.L3.HighwayChauffer";
					    else if ("L3_TrafficJamChauffer".equals(sid)) idActual = "driving.L3.TrafficJamChauffer";
					    else if ("L2_AdaptiveCruiseControl".equals(sid)) idActual = "driving.L2.AdaptiveCruiseControl";
					    else if ("L1_AssistedDriving".equals(sid))    idActual = "driving.L1.AssistedDriving";
					    else idActual = sid;
					}

					if (!idActual.equals(ultimoServicio)) {
					    ultimoServicio = idActual;
					    this.reportMeasure(idActual);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (Exception e) {
					// Captura errores inesperados al buscar servicios
				}
			}
		});
		this.hilo.start();
	}
}
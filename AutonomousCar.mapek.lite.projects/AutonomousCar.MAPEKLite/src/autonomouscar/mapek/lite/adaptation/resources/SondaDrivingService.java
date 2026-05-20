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
					
					String idActual = (service != null) ? service.getId() : "NONE";
					
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
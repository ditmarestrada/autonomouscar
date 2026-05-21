package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;
import java.util.List; // IMPORTANTE: Necesitamos importar List
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Probe;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;
import sua.autonomouscar.devices.interfaces.IDistanceSensor;
import sua.autonomouscar.infraestructure.OSGiUtils;

public class SondaSensores extends Probe {
	public static String ID = "Sonda Sensores";
	private Thread hilo;
	private boolean stop = false;
	private static SmartLogger logger = SmartLogger.getLogger(SondaSensores.class);

	public SondaSensores(BundleContext context) {
		super(context, ID);
		this.hilo = new Thread(() -> {
			while (!stop) {
				try {
					Thread.sleep(3000);
					
					if (this.getBundleContext() == null) continue;
					
					// Recibimos una LISTA, no un array
					List<IDistanceSensor> sensores = OSGiUtils.getServices(this.getBundleContext(), IDistanceSensor.class, null);
					
					// Comprobamos si la lista tiene elementos
					String estadoActual = (sensores != null && !sensores.isEmpty()) ? "OK" : "FALLO";
					//String estadoActual = "FALLO";// para probar L3-1b
					
					this.reportMeasure(estadoActual); 
					
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (Exception e) {
					// Silenciamos excepciones
				}
			}
		});
		this.hilo.start();
	}
}
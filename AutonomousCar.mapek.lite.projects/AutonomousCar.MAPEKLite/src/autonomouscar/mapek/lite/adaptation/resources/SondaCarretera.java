package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Probe;
import es.upv.pros.tatami.osgi.utils.components.OSGiUtils;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;
import sua.autonomouscar.devices.interfaces.IRoadSensor;


public class SondaCarretera extends Probe {

	protected static SmartLogger logger = SmartLogger.getLogger(SondaCarretera.class);
	public static String ID = "Sonda Carretera";
	
	// Usamos un hilo propio para que no bloquee el sistema
	private Thread hilo;
	private boolean stop = false;
	private String ultimoEstadoConocido = "";

	public SondaCarretera(BundleContext context) {
		super(context, ID); // Nota: Solo pasamos context e ID, como en el ejemplo del profe
		this.hilo = new Thread(() -> {
			while (!stop) {
				try {
					Thread.sleep(2000);
					IRoadSensor roadSensor = OSGiUtils.getService(this.getBundleContext(), IRoadSensor.class);
					
					if (roadSensor != null) {
						String tipo = roadSensor.getRoadType().name();
						String estado = roadSensor.getRoadStatus().name();
						String estadoActual = tipo + ";" + estado;
						
						if (!estadoActual.equals(ultimoEstadoConocido)) {
							this.ultimoEstadoConocido = estadoActual;
							// Usamos el método reportMeasure heredado de Probe
							this.reportMeasure(estadoActual); 
						}
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		});
		this.hilo.start();
	}
    
}
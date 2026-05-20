package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Monitor;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IKnowledgeProperty;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IMonitor;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.BasicMAPEKLiteLoopHelper;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;

public class MonitorCarretera extends Monitor {

	protected static SmartLogger logger = SmartLogger.getLogger(MonitorCarretera.class);
	public static String ID = "Monitor Carretera";

	public MonitorCarretera(BundleContext context) {
		super(context, ID);
	}

	@Override
	public IMonitor report(Object arg) {
		try {
			String[] datos = ((String) arg).split(";");
			if (datos.length == 2) {
				String tipoCarretera = datos[0];
				String estadoTrafico = datos[1];

				// Obtenemos las propiedades de conocimiento
				IKnowledgeProperty kpRoadType = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("road-type");
				IKnowledgeProperty kpRoadStatus = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("road-status");

				// Las actualizamos si existen
				if (kpRoadType != null) {
					kpRoadType.setValue(tipoCarretera);
				}
				if (kpRoadStatus != null) {
					kpRoadStatus.setValue(estadoTrafico);
				}
				
				logger.info("MonitorCarretera actualiza Knowledge -> Type: " + tipoCarretera + " | Status: " + estadoTrafico);
			}
		} catch (Exception e) {
			// En caso de error, devolvemos 'this' para no romper la cadena, 
			// tal como hace el ejemplo de tu profesor.
			return this;
		}
		
		return this; // Esto es lo que faltaba para cumplir con IMonitor
	}
}
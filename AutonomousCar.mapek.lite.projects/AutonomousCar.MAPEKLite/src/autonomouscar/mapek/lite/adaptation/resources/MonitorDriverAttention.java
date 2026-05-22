package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Monitor;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IKnowledgeProperty;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IMonitor;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.BasicMAPEKLiteLoopHelper;

public class MonitorDriverAttention extends Monitor {

	public static final String ID = "Monitor Driver Attention";
	public static final String KP_DRIVER_ATTENTION = "DriverAttention";

	public MonitorDriverAttention(BundleContext context) {
		super(context, ID);
	}

	@Override
	public IMonitor report(Object measure) {
		try {
			if (measure == null) {
				return this;
			}

			String value = measure.toString().toUpperCase();
			IKnowledgeProperty kp = BasicMAPEKLiteLoopHelper.getKnowledgeProperty(KP_DRIVER_ATTENTION);

			if (kp != null && (kp.getValue() == null || !kp.getValue().equals(value))) {				
				this.logger.debug(String.format("Actualizando propiedad de conocimiento %s a %s", kp.getId(), value));
				kp.setValue(value);
			}
		} catch (Exception e) {
			this.logger.error("Error al actualizar la propiedad DriverAttention: " + e.getMessage());
			return this;
		}

		return this;
	}
}
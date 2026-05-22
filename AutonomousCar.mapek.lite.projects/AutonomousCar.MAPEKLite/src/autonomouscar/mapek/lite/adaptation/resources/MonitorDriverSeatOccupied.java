package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Monitor;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IKnowledgeProperty;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IMonitor;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.BasicMAPEKLiteLoopHelper;

public class MonitorDriverSeatOccupied extends Monitor {

	public static final String ID = "Monitor Driver Seat Occupied";
	public static final String KP_DRIVER_SEAT = "driver-seat-occupied";

	public MonitorDriverSeatOccupied(BundleContext context) {
		super(context, ID);
	}

	@Override
	public IMonitor report(Object measure) {
		try {
			if (measure == null) {
				throw new IllegalArgumentException("La medida recibida por el monitor de asiento del conductor es nula");
			}

			IKnowledgeProperty kp = BasicMAPEKLiteLoopHelper.getKnowledgeProperty(KP_DRIVER_SEAT);
			if (kp == null) {
				throw new IllegalStateException("La knowledge property driver-seat-occupied no existe");
			}

			Boolean value = Boolean.valueOf(measure.toString());

			if (kp.getValue() == null || !kp.getValue().equals(value)) {
				this.logger.debug(String.format("Actualizando %s a %s", kp.getId(), value));
				kp.setValue(value);
			}
		} catch (Exception e) {
			this.logger.error("Error al actualizar la propiedad driver-seat-occupied: " + e.getMessage());
		}

		return this;
	}
}
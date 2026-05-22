package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Probe;
import sua.autonomouscar.devices.interfaces.IHumanSensors;
import sua.autonomouscar.infraestructure.OSGiUtils;

public class SondaDriverSeatOccupied extends Probe {

	public static final String ID = "Sonda Driver Seat Occupied";

	public SondaDriverSeatOccupied(BundleContext context) {
		super(context, ID);
	}

	public void sampleAndReport() {
		IHumanSensors humanSensors = OSGiUtils.getService(this.context, IHumanSensors.class);
		if (humanSensors == null) {
			return;
		}

		this.reportMeasure(humanSensors.isDriverSeatOccupied());
	}
}
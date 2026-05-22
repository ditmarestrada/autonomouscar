package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Probe;
import sua.autonomouscar.devices.interfaces.IHumanSensors;
import sua.autonomouscar.infraestructure.OSGiUtils;

public class SondaDriverHandsOnWheel extends Probe {

	public static final String ID = "Sonda Driver Hands On Wheel";

	public SondaDriverHandsOnWheel(BundleContext context) {
		super(context, ID);
	}

	public void sampleAndReport() {
		IHumanSensors humanSensors = OSGiUtils.getService(this.context, IHumanSensors.class);
		if (humanSensors == null) {
			return;
		}

		this.reportMeasure(humanSensors.areTheHandsOnTheWheel());
	}
}

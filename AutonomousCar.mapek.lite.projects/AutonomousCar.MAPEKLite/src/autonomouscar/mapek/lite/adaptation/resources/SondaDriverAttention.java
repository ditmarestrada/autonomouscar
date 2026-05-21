package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Probe;
import sua.autonomouscar.devices.interfaces.IHumanSensors;
import sua.autonomouscar.infraestructure.OSGiUtils;
import sua.autonomouscar.interfaces.EFaceStatus;

public class SondaDriverAttention extends Probe {

	public static final String ID = "Sonda Driver Attention";

	public SondaDriverAttention(BundleContext context) {
		super(context, ID);
	}

	public void sampleAndReport() {
		IHumanSensors humanSensors = OSGiUtils.getService(this.context, IHumanSensors.class);
		if (humanSensors == null) {
			return;
		}

		EFaceStatus status = humanSensors.getFaceStatus();
		if (status == null) {
			return;
		}

		this.reportMeasure(status.name());
	}
}
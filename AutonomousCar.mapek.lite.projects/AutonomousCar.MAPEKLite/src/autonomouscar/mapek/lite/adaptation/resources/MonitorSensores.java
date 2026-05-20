package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Monitor;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IKnowledgeProperty;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IMonitor;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.BasicMAPEKLiteLoopHelper;

public class MonitorSensores extends Monitor {
	public static String ID = "Monitor Sensores";

	public MonitorSensores(BundleContext context) {
		super(context, ID);
	}

	@Override
	public IMonitor report(Object arg) {
		try {
			String status = (String) arg; // Llegará "OK" o "FALLO"
			String sensorRightDistance = status.equals("OK") ? "RightDistanceSensor" : "NO_DISPONIBLE";

			// 1. Actualizamos el estado del sensor
			IKnowledgeProperty kpSensorRight = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("sensor-right-distance");
			if (kpSensorRight != null) kpSensorRight.setValue(sensorRightDistance);

			// 2. Evaluamos qué FallbackPlan debería estar activo según las tablas del PDF
			IKnowledgeProperty kpL3 = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("active-l3-service");
			IKnowledgeProperty kpRoad = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("road-type");
			IKnowledgeProperty kpFallback = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("fallback-plan-activo");

			if (kpL3 != null && kpRoad != null && kpFallback != null) {
				String l3 = (String) kpL3.getValue();
				String road = (String) kpRoad.getValue();

				if (l3 != null && !l3.equals("NONE") && road != null) {
					// Lógica del PDF para PARK_SHOULDER
					if ((road.equals("HIGHWAY") || road.equals("STD_ROAD")) && !sensorRightDistance.equals("NO_DISPONIBLE")) {
						kpFallback.setValue("PARK_SHOULDER");
					} else {
						// Lógica del PDF para EMERGENCY (Vía urbana/Off-road o sensor estropeado)
						kpFallback.setValue("EMERGENCY");
					}
				}
			}
		} catch (Exception e) {}
		
		return this;
	}
}
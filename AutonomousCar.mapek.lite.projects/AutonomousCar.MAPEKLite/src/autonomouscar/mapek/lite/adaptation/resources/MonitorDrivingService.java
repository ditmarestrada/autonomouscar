package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Monitor;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IKnowledgeProperty;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IMonitor;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.BasicMAPEKLiteLoopHelper;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;

public class MonitorDrivingService extends Monitor {

	protected static SmartLogger logger = SmartLogger.getLogger(MonitorDrivingService.class);
	public static String ID = "Monitor Driving Service";

	public MonitorDrivingService(BundleContext context) {
		super(context, ID);
	}

	@Override
	public IMonitor report(Object arg) {
		String activeServiceId = (String) arg;
		IKnowledgeProperty kp = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("active-l3-service");
		
		if (kp != null) {
			kp.setValue(activeServiceId);
			logger.info("MonitorDrivingService actualiza active-l3-service a: " + activeServiceId);
		}
		return this;
	}
}
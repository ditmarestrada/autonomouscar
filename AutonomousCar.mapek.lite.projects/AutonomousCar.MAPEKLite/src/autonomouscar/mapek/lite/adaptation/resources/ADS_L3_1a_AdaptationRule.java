package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.structures.systemconfiguration.interfaces.IRuleComponentsSystemConfiguration;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.AdaptationRule;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IKnowledgeProperty;
import es.upv.pros.tatami.adaptation.mapek.lite.exceptions.analyzing.RuleException;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.BasicMAPEKLiteLoopHelper;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.SystemConfigurationHelper;
import es.upv.pros.tatami.adaptation.mapek.lite.structures.systemconfiguration.interfaces.IRuleSystemConfiguration;
import es.upv.pros.tatami.osgi.utils.interfaces.ITimeStamped;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;

public class ADS_L3_1a_AdaptationRule extends AdaptationRule {

	protected static SmartLogger logger = SmartLogger.getLogger(ADS_L3_1a_AdaptationRule.class);
	public static String ID = "ADS_L3_1a";

	IKnowledgeProperty kp_ActiveL3Service = null;
	IKnowledgeProperty kp_RoadType;
	IKnowledgeProperty kp_SensorFrontDistance;
	

	public ADS_L3_1a_AdaptationRule(BundleContext context) {
	    super(context, ID);
	    logger.info("ADS_L3_1a creada");

	    this.setListenToKnowledgePropertyChanges("road-type");

	    kp_ActiveL3Service = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("active-l3-service");
	    kp_RoadType = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("road-type");
	    kp_SensorFrontDistance = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("sensor-front-distance");
	}

	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
	    if (kp_RoadType == null)
	        kp_RoadType = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("road-type");
	    if (kp_ActiveL3Service == null)
	        kp_ActiveL3Service = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("active-l3-service");
	    if (kp_SensorFrontDistance == null)
	        kp_SensorFrontDistance = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("sensor-front-distance");

	    if (kp_RoadType == null || kp_ActiveL3Service == null || kp_SensorFrontDistance == null) return false;

	    String roadType = (String) kp_RoadType.getValue();
	    String active = (String) kp_ActiveL3Service.getValue();
	    String sensorFront = (String) kp_SensorFrontDistance.getValue();

	    if (roadType == null) return false;
	    if ("HIGHWAY".equals(roadType) || "CITY".equals(roadType)) return false;
	    if (active == null || "NONE".equals(active)) return false;
	    if ("NO_DISPONIBLE".equals(sensorFront)) return false; // ← nuevo

	    return true;
	}
	
	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {
		logger.info("ADS_L3_1a ejecutándose");
	    if (kp_ActiveL3Service == null)
	        kp_ActiveL3Service = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("active-l3-service");

	    if (kp_RoadType == null)
	        kp_RoadType = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("road-type");

	    if (kp_SensorFrontDistance == null)
	        kp_SensorFrontDistance = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("sensor-front-distance");

	    if (kp_ActiveL3Service == null
	            || kp_RoadType == null
	            || kp_SensorFrontDistance == null)
	    	//return noChanges();
	    	return null;

	    String activeL3Service = (String) kp_ActiveL3Service.getValue();
	    String roadType = (String) kp_RoadType.getValue();
	    String sensorFrontDistance = (String) kp_SensorFrontDistance.getValue();

	    if (activeL3Service == null
	            || roadType == null
	            || sensorFrontDistance == null)
	    	//return noChanges();
	    	return null;

	    boolean roadUnsupported =
	            "STD_ROAD".equals(roadType)
	            || "OFF_ROAD".equals(roadType);

	    boolean active =
	            !"NONE".equals(activeL3Service);

	    boolean frontAvailable =
	            !"FALLO".equals(sensorFrontDistance)
	            && !"NO_DISPONIBLE".equals(sensorFrontDistance);

	    if (!roadUnsupported || !active || !frontAvailable) {
	    	//return noChanges();
	    	return null;
	    }

	    logger.info("ADS_L3_1a: Activando L2 Adaptive Cruise Control");

	    return configuracionL2(activeL3Service);
	}

	private IRuleComponentsSystemConfiguration configuracionL2(String activeL3Service) {

	    IRuleComponentsSystemConfiguration nextConfig =
	            SystemConfigurationHelper.createPartialSystemConfiguration(
	                    this.getId() + "_L2_" + ITimeStamped.getCurrentTimeStamp());

	    // undeploy L3 actual
	    SystemConfigurationHelper.componentToRemove(
	            nextConfig,
	            activeL3Service,
	            "1.0.0");

	    // deploy L2
	    SystemConfigurationHelper.componentToAdd(
	            nextConfig,
	            "driving.L2.AdaptiveCruiseControl",
	            "1.0.0");

	    // bindings
	    SystemConfigurationHelper.bindingToAdd(
	            nextConfig,
	            "driving.L2.AdaptiveCruiseControl",
	            "1.0.0",
	            "required_engine",
	            "device.Engine",
	            "1.0.0",
	            "provided_device");

	    SystemConfigurationHelper.bindingToAdd(
	            nextConfig,
	            "driving.L2.AdaptiveCruiseControl",
	            "1.0.0",
	            "required_frontdistancesensor",
	            "device.FrontDistanceSensor",
	            "1.0.0",
	            "provided_sensor");

	    SystemConfigurationHelper.bindingToAdd(
	            nextConfig,
	            "driving.L2.AdaptiveCruiseControl",
	            "1.0.0",
	            "required_notificationservice",
	            "interaction.NotificationService",
	            "1.0.0",
	            "provided_service");

	    return nextConfig;
	}
	private IRuleComponentsSystemConfiguration noChanges() {
	    return SystemConfigurationHelper.createPartialSystemConfiguration(
	        this.getId() + "_noop_" + ITimeStamped.getCurrentTimeStamp()
	    );
	}
}
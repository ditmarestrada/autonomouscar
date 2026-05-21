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

public class ADS_L3_1b_AdaptationRule extends AdaptationRule {

    protected static SmartLogger logger =
            SmartLogger.getLogger(ADS_L3_1b_AdaptationRule.class);

    public static String ID = "ADS_L3_1b";

    IKnowledgeProperty kp_ActiveL3Service = null;
    IKnowledgeProperty kp_RoadType = null;
    IKnowledgeProperty kp_SensorFrontDistance = null;

    /*public ADS_L3_1b_AdaptationRule(BundleContext context) {
        super(context, ID);
        logger.info("ADS_L3_1b creada");

        this.setListenToKnowledgePropertyChanges("road-type");

        kp_ActiveL3Service =
                BasicMAPEKLiteLoopHelper.getKnowledgeProperty("active-l3-service");

        kp_RoadType =
                BasicMAPEKLiteLoopHelper.getKnowledgeProperty("road-type");

        kp_SensorFrontDistance =
                BasicMAPEKLiteLoopHelper.getKnowledgeProperty("sensor-front-distance");
    }*/
    public ADS_L3_1b_AdaptationRule(BundleContext context) {
        super(context, ID);
        logger.info("ADS_L3_1b creada");

        this.setListenToKnowledgePropertyChanges("road-type");
    }

    @Override
    public boolean checkAffectedByChange(IKnowledgeProperty property) {
        if (kp_ActiveL3Service == null)
            kp_ActiveL3Service = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("active-l3-service");
        if (kp_RoadType == null)
            kp_RoadType = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("road-type");
        if (kp_SensorFrontDistance == null)
            kp_SensorFrontDistance = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("sensor-front-distance");

        if (kp_ActiveL3Service == null || kp_RoadType == null || kp_SensorFrontDistance == null)
            return false;

        String active = (String) kp_ActiveL3Service.getValue();
        String roadType = (String) kp_RoadType.getValue();
        String sensorFront = (String) kp_SensorFrontDistance.getValue();

        if (active == null || "NONE".equals(active)) return false;
        if (roadType == null || (!"STD_ROAD".equals(roadType) && !"OFF_ROAD".equals(roadType))) return false;
        if (sensorFront == null || (!("NO_DISPONIBLE".equals(sensorFront)) && !("FALLO".equals(sensorFront)))) return false;

        return true;
    }
    
    /*@Override
    public boolean checkAffectedByChange(IKnowledgeProperty property) {
        logger.info("ADS_L3_1b checkAffectedByChange: " + property);
        return true;
    }*/

    @Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {
	
	    String activeL3Service = (String) kp_ActiveL3Service.getValue();
	    String roadType = (String) kp_RoadType.getValue();
	    String sensorFrontDistance = (String) kp_SensorFrontDistance.getValue();
	
	    logger.info("ADS_L3_1b activeL3Service = " + activeL3Service);
	    logger.info("ADS_L3_1b roadType = " + roadType);
	    logger.info("ADS_L3_1b sensorFrontDistance = " + sensorFrontDistance);
	
	    if (activeL3Service == null || roadType == null || sensorFrontDistance == null)
	        return null;
	
	    boolean hayL3Activo = !"NONE".equals(activeL3Service);
	    boolean carreteraNoSoportada = "STD_ROAD".equals(roadType) || "OFF_ROAD".equals(roadType);
	    boolean sensorFrontalNoDisponible = "NO_DISPONIBLE".equals(sensorFrontDistance) 
	                                     || "FALLO".equals(sensorFrontDistance);
	
	    if (hayL3Activo && carreteraNoSoportada && sensorFrontalNoDisponible) {
	        logger.info("ADS_L3_1b: Activando L1 AssistedDriving");
	        return configuracionL1(activeL3Service);
	    }
	
	    return null;
	}

    private IRuleComponentsSystemConfiguration configuracionL1(String activeL3Service) {

        IRuleComponentsSystemConfiguration nextConfig =
                SystemConfigurationHelper.createPartialSystemConfiguration(
                        this.getId() + "_L1_" + ITimeStamped.getCurrentTimeStamp());

        SystemConfigurationHelper.componentToRemove(
                nextConfig,
                activeL3Service,
                "1.0.0");

        SystemConfigurationHelper.componentToAdd(
                nextConfig,
                "driving.L1.AssistedDriving",
                "1.0.0");

        SystemConfigurationHelper.bindingToAdd(
                nextConfig,
                "driving.L1.AssistedDriving",
                "1.0.0",
                "required_frontdistancesensor",
                "device.FrontDistanceSensor",
                "1.0.0",
                "provided_sensor");

        SystemConfigurationHelper.bindingToAdd(
                nextConfig,
                "driving.L1.AssistedDriving",
                "1.0.0",
                "required_leftlinesensor",
                "device.LeftLineSensor",
                "1.0.0",
                "provided_sensor");

        SystemConfigurationHelper.bindingToAdd(
                nextConfig,
                "driving.L1.AssistedDriving",
                "1.0.0",
                "required_rightlinesensor",
                "device.RightLineSensor",
                "1.0.0",
                "provided_sensor");

        SystemConfigurationHelper.bindingToAdd(
                nextConfig,
                "driving.L1.AssistedDriving",
                "1.0.0",
                "required_notificationservice",
                "interaction.NotificationService",
                "1.0.0",
                "provided_service");

        return nextConfig;
    }
}
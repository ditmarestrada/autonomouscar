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

public class ADS_L3_2_AdaptationRule extends AdaptationRule {

    protected static SmartLogger logger = SmartLogger.getLogger(ADS_L3_2_AdaptationRule.class);
    public static String ID = "ADS_L3_2";

    IKnowledgeProperty kp_ActiveL3Service = null;
    IKnowledgeProperty kp_RoadStatus = null;

    public ADS_L3_2_AdaptationRule(BundleContext context) {
        super(context, ID);
        this.setListenToKnowledgePropertyChanges("active-l3-service");
        this.setListenToKnowledgePropertyChanges("road-status");

        kp_ActiveL3Service = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("active-l3-service");
        kp_RoadStatus = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("road-status");
    }

    @Override
    public boolean checkAffectedByChange(IKnowledgeProperty property) {
        if (kp_ActiveL3Service == null)
            kp_ActiveL3Service = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("active-l3-service");
        if (kp_RoadStatus == null)
            kp_RoadStatus = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("road-status");

        if (kp_ActiveL3Service == null || kp_RoadStatus == null) return false;

        String active = (String) kp_ActiveL3Service.getValue();
        String roadStatus = (String) kp_RoadStatus.getValue();

        if (active == null || !"driving.L3.HighwayChauffer".equals(active)) return false;
        if (roadStatus == null || "FLUID".equals(roadStatus)) return false;

        return true;
    }

    @Override
    public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {
        String active = (String) kp_ActiveL3Service.getValue();
        String roadStatus = (String) kp_RoadStatus.getValue();

        if (active == null || roadStatus == null) return null;

        if ("driving.L3.HighwayChauffer".equals(active) &&
            ("JAM".equals(roadStatus) || "COLLAPSED".equals(roadStatus))) {

            logger.info("ADS_L3_2: HighwayChauffer → TrafficJamChauffer (tráfico congestionado)");
            return configuracionTrafficJam();
        }

        return null;
    }

    private IRuleComponentsSystemConfiguration configuracionTrafficJam() {
        IRuleComponentsSystemConfiguration config = SystemConfigurationHelper
            .createPartialSystemConfiguration(this.getId() + "_" + ITimeStamped.getCurrentTimeStamp());

        // Undeploy HighwayChauffer
        SystemConfigurationHelper.componentToRemove(config, "driving.L3.HighwayChauffer", "1.0.0");

        // Deploy TrafficJamChauffer
        SystemConfigurationHelper.componentToAdd(config, "driving.L3.TrafficJamChauffer", "1.0.0");
        SystemConfigurationHelper.setParameter(config, "driving.L3.TrafficJamChauffer", "1.0.0", "referencespeed", "60");

        // Bindings
        SystemConfigurationHelper.bindingToAdd(config, "driving.L3.TrafficJamChauffer", "1.0.0", "required_engine", "device.Engine", "1.0.0", "provided_device");
        SystemConfigurationHelper.bindingToAdd(config, "driving.L3.TrafficJamChauffer", "1.0.0", "required_steering", "device.Steering", "1.0.0", "provided_device");
        SystemConfigurationHelper.bindingToAdd(config, "driving.L3.TrafficJamChauffer", "1.0.0", "required_frontdistancesensor", "device.FrontDistanceSensor", "1.0.0", "provided_sensor");
        SystemConfigurationHelper.bindingToAdd(config, "driving.L3.TrafficJamChauffer", "1.0.0", "required_reardistancesensor", "device.RearDistanceSensor", "1.0.0", "provided_sensor");
        SystemConfigurationHelper.bindingToAdd(config, "driving.L3.TrafficJamChauffer", "1.0.0", "required_rightdistancesensor", "device.RightDistanceSensor", "1.0.0", "provided_sensor");
        SystemConfigurationHelper.bindingToAdd(config, "driving.L3.TrafficJamChauffer", "1.0.0", "required_leftdistancesensor", "device.LeftDistanceSensor", "1.0.0", "provided_sensor");
        SystemConfigurationHelper.bindingToAdd(config, "driving.L3.TrafficJamChauffer", "1.0.0", "required_leftlinesensor", "device.LeftLineSensor", "1.0.0", "provided_sensor");
        SystemConfigurationHelper.bindingToAdd(config, "driving.L3.TrafficJamChauffer", "1.0.0", "required_rightlinesensor", "device.RightLineSensor", "1.0.0", "provided_sensor");
        SystemConfigurationHelper.bindingToAdd(config, "driving.L3.TrafficJamChauffer", "1.0.0", "required_notificationservice", "interaction.NotificationService", "1.0.0", "provided_service");
        SystemConfigurationHelper.bindingToAdd(config, "driving.L3.TrafficJamChauffer", "1.0.0", "required_fallbackplan", "driving.FallbackPlan.ParkInTheRoadShoulder", "1.0.0", "provided_drivingservice");
        SystemConfigurationHelper.bindingToAdd(config, "driving.L3.TrafficJamChauffer", "1.0.0", "required_roadsensor", "device.RoadSensor", "1.0.0", "provided_sensor");

        return config;
    }
}
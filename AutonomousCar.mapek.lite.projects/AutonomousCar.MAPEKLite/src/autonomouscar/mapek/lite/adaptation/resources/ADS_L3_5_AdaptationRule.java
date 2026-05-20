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

public class ADS_L3_5_AdaptationRule extends AdaptationRule {

	protected static SmartLogger logger = SmartLogger.getLogger(ADS_L3_5_AdaptationRule.class);
	public static String ID = "Regla ADS L3-5 TrafficJam to City";

	IKnowledgeProperty kp_ActiveL3Service = null;
	IKnowledgeProperty kp_RoadType = null;

	public ADS_L3_5_AdaptationRule(BundleContext context) {
		super(context, ID);
		this.setListenToKnowledgePropertyChanges("active-l3-service");
		this.setListenToKnowledgePropertyChanges("road-type");
	}

	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
		return true;
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {
		if (kp_ActiveL3Service == null) kp_ActiveL3Service = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("active-l3-service");
		if (kp_RoadType == null) kp_RoadType = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("road-type");

		if (kp_ActiveL3Service == null || kp_RoadType == null) return null;

		String activeL3Service = (String) kp_ActiveL3Service.getValue();
		String roadType = (String) kp_RoadType.getValue();

		if (activeL3Service == null || roadType == null) return null;

		if (activeL3Service.equals("driving.L3.TrafficJamChauffer") && roadType.equals("CITY")) {
			logger.info("Ejecutando Regla ADS L3-5: Transicion de TrafficJamChauffer a CityChauffer (zona urbana)");
			return configuracionActivarCityChauffer();
		}

		return null;
	}

	private IRuleComponentsSystemConfiguration configuracionActivarCityChauffer() {
		IRuleComponentsSystemConfiguration nextConfig = SystemConfigurationHelper.createPartialSystemConfiguration(this.getId() + "_" + ITimeStamped.getCurrentTimeStamp());

		SystemConfigurationHelper.componentToRemove(nextConfig, "driving.L3.TrafficJamChauffer", "1.0.0");
		SystemConfigurationHelper.componentToAdd(nextConfig, "driving.L3.CityChauffer", "1.0.0");
		SystemConfigurationHelper.setParameter(nextConfig, "driving.L3.CityChauffer", "1.0.0", "referencespeed", "50");
		hacerBindings(nextConfig, "driving.L3.CityChauffer");

		return nextConfig;
	}

	private void hacerBindings(IRuleComponentsSystemConfiguration config, String drivingServiceId) {
		SystemConfigurationHelper.bindingToAdd(config, drivingServiceId, "1.0.0", "required_engine", "device.Engine", "1.0.0", "provided_device");
		SystemConfigurationHelper.bindingToAdd(config, drivingServiceId, "1.0.0", "required_left_linesensor", "device.LeftLineSensor", "1.0.0", "provided_sensor");
		SystemConfigurationHelper.bindingToAdd(config, drivingServiceId, "1.0.0", "required_right_linesensor", "device.RightLineSensor", "1.0.0", "provided_sensor");
		SystemConfigurationHelper.bindingToAdd(config, drivingServiceId, "1.0.0", "required_front_distancesensor", "device.FrontDistanceSensor", "1.0.0", "provided_sensor");
		SystemConfigurationHelper.bindingToAdd(config, drivingServiceId, "1.0.0", "required_rear_distancesensor", "device.RearDistanceSensor", "1.0.0", "provided_sensor");
		SystemConfigurationHelper.bindingToAdd(config, drivingServiceId, "1.0.0", "required_notificationservice", "interaction.NotificationService", "1.0.0", "provided_service");
		SystemConfigurationHelper.bindingToAdd(config, drivingServiceId, "1.0.0", "required_fallbackplan", "driving.FallbackPlan.ParkInTheRoadShoulder", "1.0.0", "provided_fallbackplan");
	}
}

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

public class ADS_L3_4_AdaptationRule extends AdaptationRule {

	protected static SmartLogger logger = SmartLogger.getLogger(ADS_L3_4_AdaptationRule.class);
	public static String ID = "Regla ADS L3-4 TrafficJam to Highway";

	IKnowledgeProperty kp_ActiveL3Service = null;
	IKnowledgeProperty kp_RoadStatus = null;

	public ADS_L3_4_AdaptationRule(BundleContext context) {
		super(context, ID);
		this.setListenToKnowledgePropertyChanges("active-l3-service");
		this.setListenToKnowledgePropertyChanges("road-status");
	}

	/*@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
		return true;
	}*/
	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
	    if (kp_ActiveL3Service == null)
	        kp_ActiveL3Service = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("active-l3-service");
	    if (kp_RoadStatus == null)
	        kp_RoadStatus = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("road-status");

	    if (kp_ActiveL3Service == null || kp_RoadStatus == null)
	        return false;

	    String active = (String) kp_ActiveL3Service.getValue();
	    String status = (String) kp_RoadStatus.getValue();

	    return "driving.L3.TrafficJamChauffer".equals(active)
	            && "FLUID".equals(status);
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {
		if (kp_ActiveL3Service == null) kp_ActiveL3Service = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("active-l3-service");
		if (kp_RoadStatus == null) kp_RoadStatus = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("road-status");

		if (kp_ActiveL3Service == null || kp_RoadStatus == null) return null;

		String activeL3Service = (String) kp_ActiveL3Service.getValue();
		String roadStatus = (String) kp_RoadStatus.getValue();

		if (activeL3Service == null || roadStatus == null) return null;

		if (activeL3Service.equals("driving.L3.TrafficJamChauffer") && roadStatus.equals("FLUID")) {
			logger.info("Ejecutando Regla ADS L3-4: Transicion de TrafficJamChauffer a HighwayChauffer (trafico fluido)");
			return configuracionActivarHighwayChauffer();
		}

		return null;
	}

	private IRuleComponentsSystemConfiguration configuracionActivarHighwayChauffer() {
		IRuleComponentsSystemConfiguration nextConfig = SystemConfigurationHelper.createPartialSystemConfiguration(this.getId() + "_" + ITimeStamped.getCurrentTimeStamp());

		SystemConfigurationHelper.componentToRemove(nextConfig, "driving.L3.TrafficJamChauffer", "1.0.0");
		SystemConfigurationHelper.componentToAdd(nextConfig, "driving.L3.HighwayChauffer", "1.0.0");
		SystemConfigurationHelper.setParameter(nextConfig, "driving.L3.HighwayChauffer", "1.0.0", "referencespeed", "120");
		hacerBindings(nextConfig, "driving.L3.HighwayChauffer");

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
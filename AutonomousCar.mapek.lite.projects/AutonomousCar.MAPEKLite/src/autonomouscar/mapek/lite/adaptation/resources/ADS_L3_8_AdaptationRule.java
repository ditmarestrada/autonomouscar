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

public class ADS_L3_8_AdaptationRule extends AdaptationRule {

	protected static SmartLogger logger = SmartLogger.getLogger(ADS_L3_8_AdaptationRule.class);
	public static String ID = "Regla ADS L3-8 Fallback Plan";

	IKnowledgeProperty kp_ActiveL3Service = null;
	IKnowledgeProperty kp_FallbackPlanActivo = null;
	IKnowledgeProperty kp_SensorRight = null;
	
	private String fallbackAplicado = "EMERGENCY";

	public ADS_L3_8_AdaptationRule(BundleContext context) {
		super(context, ID);
		this.setListenToKnowledgePropertyChanges("active-l3-service");
		this.setListenToKnowledgePropertyChanges("fallback-plan-activo");
		this.setListenToKnowledgePropertyChanges("sensor-right-distance");
	}

	/*@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
		return true; // Permitimos que la regla evalúe siempre que haya un cambio
	}*/
	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
	    if (kp_ActiveL3Service == null) 
	        kp_ActiveL3Service = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("active-l3-service");
	    if (kp_FallbackPlanActivo == null) 
	        kp_FallbackPlanActivo = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("fallback-plan-activo");

	    if (kp_ActiveL3Service == null || kp_FallbackPlanActivo == null) return false;

	    String active = (String) kp_ActiveL3Service.getValue();
	    String fallback = (String) kp_FallbackPlanActivo.getValue();

	    if (active == null || "NONE".equals(active)) return false;
	    if (fallback == null) return false;

	    // Solo actúa si el fallback actual es diferente al que ya aplicamos
	    if (fallback.equals(fallbackAplicado)) return false;

	    return true;
	}
	

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {
	    // LAZY INITIALIZATION: Recuperamos las propiedades si están nulas para evitar el NPE
	    if (kp_ActiveL3Service == null) kp_ActiveL3Service = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("active-l3-service");
	    if (kp_FallbackPlanActivo == null) kp_FallbackPlanActivo = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("fallback-plan-activo");
	    if (kp_SensorRight == null) kp_SensorRight = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("sensor-right-distance");

	    if (kp_ActiveL3Service == null || kp_FallbackPlanActivo == null || kp_SensorRight == null) return null;

	    String activeL3Service = (String) kp_ActiveL3Service.getValue();
	    String fallbackPlanActivo = (String) kp_FallbackPlanActivo.getValue();

	    // Si los datos son nulos o el servicio no está definido, no hacemos NADA
	    if (activeL3Service == null || fallbackPlanActivo == null || activeL3Service.equals("NONE")) {
	        return null;
	    }

	    // Si ya estamos en el estado deseado, no hacemos NADA (Evita bucles)
	    if (fallbackPlanActivo.equals(fallbackAplicado)) {
	        return null;
	    }

	    // Ejecución de la lógica según el plan
	    if (fallbackPlanActivo.equals("PARK_SHOULDER")) {
	        logger.info("ADS L3-8a: Aplicando ParkInTheRoadShoulder");
	        fallbackAplicado = "PARK_SHOULDER";
	        return configuracionParkShoulder(activeL3Service);
	    } 
	    else if (fallbackPlanActivo.equals("EMERGENCY")) {
	        logger.info("ADS L3-8b: Aplicando EmergencyFallbackPlan");
	        fallbackAplicado = "EMERGENCY";
	        return configuracionEmergency(activeL3Service);
	    }

	    return null;
	}

	private IRuleComponentsSystemConfiguration configuracionParkShoulder(String activeL3Service) {
	    IRuleComponentsSystemConfiguration nextConfig = SystemConfigurationHelper.createPartialSystemConfiguration(this.getId() + "_a_" + ITimeStamped.getCurrentTimeStamp());
	    
	    SystemConfigurationHelper.bindingToRemove(nextConfig, activeL3Service, "1.0.0", "required_fallbackplan", "driving.FallbackPlan.Emergency", "1.0.0", "provided_fallbackplan");
	    SystemConfigurationHelper.componentToAdd(nextConfig, "driving.FallbackPlan.ParkInTheRoadShoulder", "1.0.0");
	    SystemConfigurationHelper.bindingToAdd(nextConfig, activeL3Service, "1.0.0", "required_fallbackplan", "driving.FallbackPlan.ParkInTheRoadShoulder", "1.0.0", "provided_fallbackplan");
	    SystemConfigurationHelper.bindingToAdd(nextConfig, "driving.FallbackPlan.ParkInTheRoadShoulder", "1.0.0", "required_engine", "device.Engine", "1.0.0", "provided_device");
	    SystemConfigurationHelper.bindingToAdd(nextConfig, "driving.FallbackPlan.ParkInTheRoadShoulder", "1.0.0", "required_steering", "device.Steering", "1.0.0", "provided_device");
	    SystemConfigurationHelper.bindingToAdd(nextConfig, "driving.FallbackPlan.ParkInTheRoadShoulder", "1.0.0", "required_right_distancesensor", "device.RightDistanceSensor", "1.0.0", "provided_sensor");

	    return nextConfig;
	}

	private IRuleComponentsSystemConfiguration configuracionEmergency(String activeL3Service) {
	    IRuleComponentsSystemConfiguration nextConfig = SystemConfigurationHelper.createPartialSystemConfiguration(this.getId() + "_b_" + ITimeStamped.getCurrentTimeStamp());

	    SystemConfigurationHelper.bindingToRemove(nextConfig, activeL3Service, "1.0.0", "required_fallbackplan", "driving.FallbackPlan.ParkInTheRoadShoulder", "1.0.0", "provided_fallbackplan");
	    SystemConfigurationHelper.componentToAdd(nextConfig, "driving.FallbackPlan.Emergency", "1.0.0");
	    SystemConfigurationHelper.bindingToAdd(nextConfig, activeL3Service, "1.0.0", "required_fallbackplan", "driving.FallbackPlan.Emergency", "1.0.0", "provided_fallbackplan");
	    SystemConfigurationHelper.bindingToAdd(nextConfig, "driving.FallbackPlan.Emergency", "1.0.0", "required_engine", "device.Engine", "1.0.0", "provided_device");
	    SystemConfigurationHelper.bindingToAdd(nextConfig, "driving.FallbackPlan.Emergency", "1.0.0", "required_steering", "device.Steering", "1.0.0", "provided_device");

	    return nextConfig;
	}
}
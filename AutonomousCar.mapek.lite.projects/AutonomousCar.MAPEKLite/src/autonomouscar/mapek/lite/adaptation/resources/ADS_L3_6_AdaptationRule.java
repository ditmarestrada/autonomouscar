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
import sua.autonomouscar.infraestructure.driving.ARC.DrivingServiceARC;
import sua.autonomouscar.infraestructure.driving.ARC.FallbackPlanARC;

public class ADS_L3_6_AdaptationRule extends AdaptationRule {

	protected static SmartLogger logger = SmartLogger.getLogger(ADS_L3_6_AdaptationRule.class);
	public static String ID = "Regla ADS L3-6 City to Highway";
	
	// 1. Definimos las propiedades de conocimiento (Knowledge) que necesitamos
	IKnowledgeProperty kp_ActiveL3Service = null;
	IKnowledgeProperty kp_RoadType = null;
	IKnowledgeProperty kp_RoadStatus = null;

	public ADS_L3_6_AdaptationRule(BundleContext context) {
		super(context, ID);
		// 2. Nos suscribimos a los cambios en estas propiedades
		this.setListenToKnowledgePropertyChanges("active-l3-service");
		this.setListenToKnowledgePropertyChanges("road-type");
		this.setListenToKnowledgePropertyChanges("road-status");

		// 3. Obtenemos las referencias a las propiedades
		kp_ActiveL3Service = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("active-l3-service");
		kp_RoadType = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("road-type");
		kp_RoadStatus = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("road-status");
	}

	//@Override
	/*public boolean checkAffectedByChange(IKnowledgeProperty property) {
		if (kp_ActiveL3Service == null || kp_RoadType == null || kp_RoadStatus == null) {
			logger.trace("Faltan propiedades en el Knowledge. No se ejecuta la regla...");
			return false;
		}
		return true;
	}*/
	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
	    if (kp_ActiveL3Service == null) 
	        kp_ActiveL3Service = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("active-l3-service");
	    if (kp_RoadType == null) 
	        kp_RoadType = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("road-type");
	    if (kp_RoadStatus == null) 
	        kp_RoadStatus = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("road-status");

	    if (kp_ActiveL3Service == null || kp_RoadType == null || kp_RoadStatus == null) return false;

	    String active = (String) kp_ActiveL3Service.getValue();
	    String roadType = (String) kp_RoadType.getValue();

	    if (active == null || !"driving.L3.CityChauffer".equals(active)) return false;
	    if (roadType == null || !"HIGHWAY".equals(roadType)) return false;

	    return true;
	}
	
	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {
		
		// 4. Leemos los valores actuales del Knowledge
		String activeL3Service = (String) kp_ActiveL3Service.getValue();
		String roadType = (String) kp_RoadType.getValue();
		String roadStatus = (String) kp_RoadStatus.getValue();

		logger.info("DEBUG: El servicio activo que leo en el Knowledge es: '" + activeL3Service + "'");
		
		if (activeL3Service == null || roadType == null || roadStatus == null) {
			throw new RuleException("Valores nulos", "Alguna propiedad es null. No se ejecuta la regla.");
		}

		// 5. Evaluamos las condiciones diseñadas
		// Condición compartida: Estamos en L3_CityChauffer y entramos en Highway
		if (activeL3Service.equals("driving.L3.CityChauffer") && roadType.equals("HIGHWAY")) {
			
			// Regla ADS L3-6a: Tráfico Fluido
			if (roadStatus.equals("FLUID")) {
				logger.info("Ejecutando Regla ADS L3-6a: Transición a HighwayChauffer (Tráfico Fluido)");
				return this.configuracionSistemaActivarHighwayChauffer();
			} 
			// Regla ADS L3-6b: Tráfico Congestionado (JAM o COLLAPSED)
			else if (roadStatus.equals("JAM") || roadStatus.equals("COLLAPSED")) {
				logger.info("Ejecutando Regla ADS L3-6b: Transición a TrafficJamChauffer (Tráfico Congestionado)");
				return this.configuracionSistemaActivarTrafficJamChauffer();
			}
		}
		
		// Si no se cumple nada, la regla no hace nada (no lanza excepción, simplemente no aplica)
		return null; 
	}
	
	// --- ACCIONES (CUERPO DE LAS REGLAS) ---

	protected IRuleComponentsSystemConfiguration configuracionSistemaActivarHighwayChauffer() {
		IRuleComponentsSystemConfiguration nextConfig = SystemConfigurationHelper.createPartialSystemConfiguration(this.getId() + "_a_" + ITimeStamped.getCurrentTimeStamp());

		// 1. Quitar el servicio de ciudad
		SystemConfigurationHelper.componentToRemove(nextConfig, "driving.L3.CityChauffer", "1.0.0");
		
		// 2. Añadir el servicio de autovía
		SystemConfigurationHelper.componentToAdd(nextConfig, "driving.L3.HighwayChauffer", "1.0.0");
		
		// 3. Set: Establecer velocidad de referencia a 120 Km/h
		SystemConfigurationHelper.setParameter(nextConfig, "driving.L3.HighwayChauffer", "1.0.0", "referencespeed", "120");

		// 4. Rehacer los bindings (Conectar L3_HighwayChauffer con el resto de componentes del coche)
		hacerBindings(nextConfig, "driving.L3.HighwayChauffer");

		return nextConfig;		
	}

	protected IRuleComponentsSystemConfiguration configuracionSistemaActivarTrafficJamChauffer() {
		IRuleComponentsSystemConfiguration nextConfig = SystemConfigurationHelper.createPartialSystemConfiguration(this.getId() + "_b_" + ITimeStamped.getCurrentTimeStamp());

		// 1. Quitar el servicio de ciudad
		SystemConfigurationHelper.componentToRemove(nextConfig, "driving.L3.CityChauffer", "1.0.0");
		
		// 2. Añadir el servicio de atasco
		SystemConfigurationHelper.componentToAdd(nextConfig, "driving.L3.TrafficJamChauffer", "1.0.0");
		
		// 3. Set: Establecer velocidad de referencia a 60 Km/h
		SystemConfigurationHelper.setParameter(nextConfig, "driving.L3.TrafficJamChauffer", "1.0.0", "referencespeed", "60");

		// 4. Rehacer los bindings
		hacerBindings(nextConfig, "driving.L3.TrafficJamChauffer");

		return nextConfig;		
	}

	// Método auxiliar corregido usando literales de texto para evitar errores de importación
		private void hacerBindings(IRuleComponentsSystemConfiguration config, String drivingServiceId) {
			// Conectar con el Engine
			SystemConfigurationHelper.bindingToAdd(config, drivingServiceId, "1.0.0", "required_engine", "device.Engine", "1.0.0", "provided_device");
			
			// Conectar con LineSensors
			SystemConfigurationHelper.bindingToAdd(config, drivingServiceId, "1.0.0", "required_left_linesensor", "device.LeftLineSensor", "1.0.0", "provided_sensor");
			SystemConfigurationHelper.bindingToAdd(config, drivingServiceId, "1.0.0", "required_right_linesensor", "device.RightLineSensor", "1.0.0", "provided_sensor");
			
			// Conectar con DistanceSensors
			SystemConfigurationHelper.bindingToAdd(config, drivingServiceId, "1.0.0", "required_front_distancesensor", "device.FrontDistanceSensor", "1.0.0", "provided_sensor");
			SystemConfigurationHelper.bindingToAdd(config, drivingServiceId, "1.0.0", "required_rear_distancesensor", "device.RearDistanceSensor", "1.0.0", "provided_sensor");
			
			// Conectar con NotificationService
			SystemConfigurationHelper.bindingToAdd(config, drivingServiceId, "1.0.0", "required_notificationservice", "interaction.NotificationService", "1.0.0", "provided_service");
			
			// Conectar con FallbackPlan 
			SystemConfigurationHelper.bindingToAdd(config, drivingServiceId, "1.0.0", "required_fallbackplan", "driving.FallbackPlan.ParkInTheRoadShoulder", "1.0.0", "provided_fallbackplan");
		}
}
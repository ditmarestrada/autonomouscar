package autonomouscar.mapek.lite.adaptation.starter;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import autonomouscar.mapek.lite.adaptation.resources.IluminacionConfortAdaptationRule;
import autonomouscar.mapek.lite.adaptation.resources.MonitorModo;
import autonomouscar.mapek.lite.adaptation.resources.SondaModo;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.interfaces.IAdaptiveReadyComponent;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.structures.systemconfiguration.interfaces.IComponentsSystemConfiguration;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.structures.systemconfiguration.interfaces.IRuleComponentsSystemConfiguration;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IKnowledgeProperty;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.BasicMAPEKLiteLoopHelper;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.SystemConfigurationHelper;
import es.upv.pros.tatami.osgi.utils.interfaces.ITimeStamped;
import sua.autonomouscar.infraestructure.devices.ARC.EngineARC;
import sua.autonomouscar.infraestructure.devices.ARC.SteeringARC;
import sua.autonomouscar.infraestructure.driving.ARC.FallbackPlanARC;
import sua.autonomouscar.infraestructure.driving.ARC.L3_DrivingServiceARC;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;

// Imports de tus Requisitos ADS L3-6 y L3-8
import autonomouscar.mapek.lite.adaptation.resources.ADS_L3_6_AdaptationRule;
import autonomouscar.mapek.lite.adaptation.resources.MonitorCarretera;
import autonomouscar.mapek.lite.adaptation.resources.SondaCarretera;
import autonomouscar.mapek.lite.adaptation.resources.MonitorDrivingService;
import autonomouscar.mapek.lite.adaptation.resources.SondaDrivingService;

import autonomouscar.mapek.lite.adaptation.resources.ADS_L3_8_AdaptationRule;
import autonomouscar.mapek.lite.adaptation.resources.MonitorSensores;
import autonomouscar.mapek.lite.adaptation.resources.SondaSensores;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private static SmartLogger logger = SmartLogger.getLogger(Activator.class);
	
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		
		BasicMAPEKLiteLoopHelper.BUNDLECONTEXT = bundleContext;
		BasicMAPEKLiteLoopHelper.REFERENCE_MODEL = "AutonomousCar"; 

		// ... adding the initial system configuration
		IComponentsSystemConfiguration theInitialSystemConfiguration = 
				SystemConfigurationHelper.createSystemConfiguration("InititalConfiguration");
		SystemConfigurationHelper.addComponent(theInitialSystemConfiguration, "device.RoadSensor", "1.0.0");
		BasicMAPEKLiteLoopHelper.INITIAL_SYSTEMCONFIGURATION = theInitialSystemConfiguration;

		BasicMAPEKLiteLoopHelper.MODELSREPOSITORY_FOLDER = System.getProperty("modelsrepository.folder");
		BasicMAPEKLiteLoopHelper.ADAPTATIONREPORTS_FOLDER = System.getProperty("adaptationreports.folder");
		
		// STARTING THE MAPE-K LOOP
		BasicMAPEKLiteLoopHelper.startLoopModules();
		
		BasicMAPEKLiteLoopHelper.addInitialSelfConfigurationCapabilities(createInitialSystemConfiguration());
		
		// ---------------------------------------------------------
		// 1. ADAPTATION PROPERTIES (Knowledge)
		// ---------------------------------------------------------
		IKnowledgeProperty kp_Modo = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("Modo");
		
		// Propiedades L3-6
		IKnowledgeProperty kp_RoadType = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("road-type");
		IKnowledgeProperty kp_RoadStatus = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("road-status");
		IKnowledgeProperty kp_ActiveL3Service = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("active-l3-service");
		
		// Propiedades L3-8
		IKnowledgeProperty kp_SensorRight = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("sensor-right-distance");
		IKnowledgeProperty kp_Fallback = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("fallback-plan-activo");


		// ---------------------------------------------------------
		// 2. ADAPTATION RULES
		// ---------------------------------------------------------
		IAdaptiveReadyComponent theIluminacionConfortAdaptationRuleARC = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new IluminacionConfortAdaptationRule(bundleContext));		
		IAdaptiveReadyComponent theADS_L3_6_Rule = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ADS_L3_6_AdaptationRule(bundleContext));
		IAdaptiveReadyComponent theADS_L3_8_Rule = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ADS_L3_8_AdaptationRule(bundleContext));


		// ---------------------------------------------------------
		// 3. MONITORS
		// ---------------------------------------------------------
		IAdaptiveReadyComponent theModoMonitorARC = BasicMAPEKLiteLoopHelper.deployMonitor(new MonitorModo(bundleContext));		
		IAdaptiveReadyComponent theMonitorCarretera = BasicMAPEKLiteLoopHelper.deployMonitor(new MonitorCarretera(bundleContext));
		IAdaptiveReadyComponent theMonitorDS = BasicMAPEKLiteLoopHelper.deployMonitor(new MonitorDrivingService(bundleContext));
		IAdaptiveReadyComponent theMonitorSensores = BasicMAPEKLiteLoopHelper.deployMonitor(new MonitorSensores(bundleContext));


		// ---------------------------------------------------------
		// 4. PROBES
		// ---------------------------------------------------------
		IAdaptiveReadyComponent theModoProbeARC = BasicMAPEKLiteLoopHelper.deployProbe(new SondaModo(bundleContext), theModoMonitorARC);
		BasicMAPEKLiteLoopHelper.deployProbe(new SondaCarretera(bundleContext), theMonitorCarretera);
		BasicMAPEKLiteLoopHelper.deployProbe(new SondaDrivingService(bundleContext), theMonitorDS);
		BasicMAPEKLiteLoopHelper.deployProbe(new SondaSensores(bundleContext), theMonitorSensores);


	
	}

	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

	
	protected IRuleComponentsSystemConfiguration createInitialSystemConfiguration() {
		
		IRuleComponentsSystemConfiguration theInitialSystemConfiguration = SystemConfigurationHelper.createPartialSystemConfiguration("InitialConfiguration_" + ITimeStamped.getCurrentTimeStamp());
			
		// Añadimos los componentes "device.RoadSensor" y "device.Engine"
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.RoadSensor", "1.0.0");
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.Engine", "1.0.0");
		SystemConfigurationHelper.componentToRemove(theInitialSystemConfiguration, "device.Steering", "1.0.0");
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.RightDistanceSensor", "1.0.0");
		
		// Añadimos el servicio Fallback de emergencia
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "driving.FallbackPlan.Emergency", "1.0.0");
		
		// Añadimos CityChauffer (Base para test L3-6)
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "driving.L3.CityChauffer", "1.0.0");
		
		// Añadimos el Sensor Derecho (Base para test L3-8)
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.RightDistanceSensor", "1.0.0");
		
		// Bindings iniciales
		SystemConfigurationHelper.bindingToAdd(theInitialSystemConfiguration, 
				"driving.FallbackPlan.Emergency", "1.0.0", FallbackPlanARC.REQUIRED_ENGINE,
				"device.Engine", "1.0.0", EngineARC.PROVIDED_DEVICE);

		SystemConfigurationHelper.bindingToRemove(theInitialSystemConfiguration, 
				"driving.FallbackPlan.Emergency", "1.0.0", FallbackPlanARC.REQUIRED_STEERING,
				"device.Steering", "1.0.0", SteeringARC.PROVIDED_DEVICE);

		SystemConfigurationHelper.setParameter(theInitialSystemConfiguration, 
				"driving.L3.HighwayChauffer", "1.0.0", L3_DrivingServiceARC.PARAMETER_REFERENCESPEED, "100");

		return theInitialSystemConfiguration;
	}
}
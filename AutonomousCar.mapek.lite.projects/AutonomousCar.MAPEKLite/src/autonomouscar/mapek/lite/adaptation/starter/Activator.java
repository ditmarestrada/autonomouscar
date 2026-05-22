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

// Imports los Requisitos ADS L3-6 y L3-8
import autonomouscar.mapek.lite.adaptation.resources.ADS_L3_6_AdaptationRule;
import autonomouscar.mapek.lite.adaptation.resources.MonitorCarretera;
import autonomouscar.mapek.lite.adaptation.resources.SondaCarretera;
import autonomouscar.mapek.lite.adaptation.resources.MonitorDrivingService;
import autonomouscar.mapek.lite.adaptation.resources.SondaDrivingService;

import autonomouscar.mapek.lite.adaptation.resources.ADS_L3_8_AdaptationRule;
import autonomouscar.mapek.lite.adaptation.resources.MonitorSensores;
import autonomouscar.mapek.lite.adaptation.resources.SondaSensores;
import autonomouscar.mapek.lite.adaptation.resources.ADS_L3_1a_AdaptationRule;
import autonomouscar.mapek.lite.adaptation.resources.ADS_L3_1b_AdaptationRule;
import autonomouscar.mapek.lite.adaptation.resources.ADS_L3_2_AdaptationRule;
import autonomouscar.mapek.lite.adaptation.resources.ADS_L3_3_AdaptationRule;
import autonomouscar.mapek.lite.adaptation.resources.ADS_L3_4_AdaptationRule;
import autonomouscar.mapek.lite.adaptation.resources.ADS_L3_5_AdaptationRule;

import autonomouscar.mapek.lite.adaptation.resources.Interact1aRule;
import autonomouscar.mapek.lite.adaptation.resources.Interact1bRule;
import autonomouscar.mapek.lite.adaptation.resources.Interact1cRule;
import autonomouscar.mapek.lite.adaptation.resources.MonitorDriverAttention;
import autonomouscar.mapek.lite.adaptation.resources.SondaDriverAttention;
import autonomouscar.mapek.lite.adaptation.resources.MonitorDriverHandsOnWheel;
import autonomouscar.mapek.lite.adaptation.resources.SondaDriverHandsOnWheel;
import autonomouscar.mapek.lite.adaptation.resources.Interact2aRule;
import autonomouscar.mapek.lite.adaptation.resources.Interact2bRule;
import autonomouscar.mapek.lite.adaptation.resources.MonitorDriverSeatOccupied;
import autonomouscar.mapek.lite.adaptation.resources.SondaDriverSeatOccupied;
import autonomouscar.mapek.lite.adaptation.resources.Interact3aRule;
import autonomouscar.mapek.lite.adaptation.resources.Interact3bRule;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private static SmartLogger logger = SmartLogger.getLogger(Activator.class);
	
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		System.out.println(">>>>> ACTIVATOR CARGADO - VERSION NUEVA");
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
		IKnowledgeProperty kp_SensorFrontDistance = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("sensor-front-distance");
		
		//kp_RoadType.setValue("CITY");
		kp_RoadType.setValue("HIGHWAY");
		kp_RoadStatus.setValue("FLUID");
		//kp_ActiveL3Service.setValue("driving.L3.CityChauffer");
		kp_ActiveL3Service.setValue("NONE");
		kp_SensorRight.setValue("RightDistanceSensor");
		kp_Fallback.setValue("EMERGENCY");
		kp_SensorFrontDistance.setValue("FrontDistanceSensor");
		//kp_SensorFrontDistance.setValue("NO_DISPONIBLE");
		
		// Propiedad Interact-1
		IKnowledgeProperty kp_DriverAttention = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("DriverAttention");
		kp_DriverAttention.setValue("LOOKING_FORWARD");
		System.out.println(">>>>> KP CREADA: " + kp_DriverAttention);
		System.out.println(">>>>> KP ID: " + kp_DriverAttention.getId());
		System.out.println(">>>>> KP VALUE: " + kp_DriverAttention.getValue());
		
		// Propiedad Interact-2
		IKnowledgeProperty kp_DriverHandsOnWheel = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("driver-hands-on-wheel");
		
		// Propiedad Interact-3
		IKnowledgeProperty kp_DriverSeatOccupied = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("driver-seat-occupied");

		
		BasicMAPEKLiteLoopHelper.addInitialSelfConfigurationCapabilities(createInitialSystemConfiguration());

		// ---------------------------------------------------------
		// 2. ADAPTATION RULES
		// ---------------------------------------------------------
		IAdaptiveReadyComponent theIluminacionConfortAdaptationRuleARC = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new IluminacionConfortAdaptationRule(bundleContext));		
		IAdaptiveReadyComponent theADS_L3_6_Rule = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ADS_L3_6_AdaptationRule(bundleContext));
		IAdaptiveReadyComponent theADS_L3_8_Rule = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ADS_L3_8_AdaptationRule(bundleContext));
		IAdaptiveReadyComponent theADS_L3_1a_Rule = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ADS_L3_1a_AdaptationRule(bundleContext));
        IAdaptiveReadyComponent theADS_L3_1b_Rule = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ADS_L3_1b_AdaptationRule(bundleContext));
        IAdaptiveReadyComponent theADS_L3_2_Rule = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ADS_L3_2_AdaptationRule(bundleContext));
        IAdaptiveReadyComponent theADS_L3_3_Rule = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ADS_L3_3_AdaptationRule(bundleContext));
        IAdaptiveReadyComponent theADS_L3_4_Rule = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ADS_L3_4_AdaptationRule(bundleContext));
 		IAdaptiveReadyComponent theADS_L3_5_Rule = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ADS_L3_5_AdaptationRule(bundleContext));
 		
 		IAdaptiveReadyComponent interact1aRuleARC = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new Interact1aRule(bundleContext));
 		IAdaptiveReadyComponent interact1bRuleARC = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new Interact1bRule(bundleContext));
 		IAdaptiveReadyComponent interact1cRuleARC = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new Interact1cRule(bundleContext));	
 		IAdaptiveReadyComponent interact2aRuleARC = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new Interact2aRule(bundleContext));
 		IAdaptiveReadyComponent interact2bRuleARC = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new Interact2bRule(bundleContext));
 		IAdaptiveReadyComponent interact3aRuleARC = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new Interact3aRule(bundleContext));
 		IAdaptiveReadyComponent interact3bRuleARC = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new Interact3bRule(bundleContext));
 		
		// ---------------------------------------------------------
		// 3. MONITORS
		// ---------------------------------------------------------
		IAdaptiveReadyComponent theModoMonitorARC = BasicMAPEKLiteLoopHelper.deployMonitor(new MonitorModo(bundleContext));		
		IAdaptiveReadyComponent theMonitorCarretera = BasicMAPEKLiteLoopHelper.deployMonitor(new MonitorCarretera(bundleContext));
		IAdaptiveReadyComponent theMonitorDS = BasicMAPEKLiteLoopHelper.deployMonitor(new MonitorDrivingService(bundleContext));
		IAdaptiveReadyComponent theMonitorSensores = BasicMAPEKLiteLoopHelper.deployMonitor(new MonitorSensores(bundleContext));
		
		IAdaptiveReadyComponent driverAttentionMonitorARC = BasicMAPEKLiteLoopHelper.deployMonitor(new MonitorDriverAttention(bundleContext));		
		IAdaptiveReadyComponent driverHandsOnWheelMonitorARC = BasicMAPEKLiteLoopHelper.deployMonitor(new MonitorDriverHandsOnWheel(bundleContext));
		IAdaptiveReadyComponent driverSeatOccupiedMonitorARC = BasicMAPEKLiteLoopHelper.deployMonitor(new MonitorDriverSeatOccupied(bundleContext));

		// ---------------------------------------------------------
		// 4. PROBES
		// ---------------------------------------------------------
		IAdaptiveReadyComponent theModoProbeARC = BasicMAPEKLiteLoopHelper.deployProbe(new SondaModo(bundleContext), theModoMonitorARC);
		BasicMAPEKLiteLoopHelper.deployProbe(new SondaCarretera(bundleContext), theMonitorCarretera);
		BasicMAPEKLiteLoopHelper.deployProbe(new SondaDrivingService(bundleContext), theMonitorDS);
		BasicMAPEKLiteLoopHelper.deployProbe(new SondaSensores(bundleContext), theMonitorSensores);
		
		logger.info("Creada property road-type: " + kp_RoadType);
		logger.info("Creada property active-l3-service: " + kp_ActiveL3Service);
		logger.info("Creada property sensor-front-distance: " + kp_SensorFrontDistance);
			
		IAdaptiveReadyComponent driverAttentionProbeARC =
				BasicMAPEKLiteLoopHelper.deployProbe(new SondaDriverAttention(bundleContext), driverAttentionMonitorARC);		
		IAdaptiveReadyComponent driverHandsOnWheelProbeARC =
				BasicMAPEKLiteLoopHelper.deployProbe(new SondaDriverHandsOnWheel(bundleContext), driverHandsOnWheelMonitorARC);		
		IAdaptiveReadyComponent driverSeatOccupiedProbeARC =
				BasicMAPEKLiteLoopHelper.deployProbe(new SondaDriverSeatOccupied(bundleContext), driverSeatOccupiedMonitorARC);

		//		
	}

	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

	
	protected IRuleComponentsSystemConfiguration createInitialSystemConfiguration() {
		
		IRuleComponentsSystemConfiguration theInitialSystemConfiguration = SystemConfigurationHelper.createPartialSystemConfiguration("InitialConfiguration_" + ITimeStamped.getCurrentTimeStamp());
			
		// Añadimos los componentes "device.RoadSensor" y "device.Engine"
		// Sensores y dispositivos básicos
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.Engine", "1.0.0");
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.Steering", "1.0.0");

		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.FrontDistanceSensor", "1.0.0");
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.RightDistanceSensor", "1.0.0");

		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.LeftLineSensor", "1.0.0");
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.RightLineSensor", "1.0.0");
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.RearDistanceSensor", "1.0.0");
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.LeftDistanceSensor", "1.0.0");

		// Servicios de interacción
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "interaction.NotificationService", "1.0.0");
		
		// Añadimos el servicio Fallback de emergencia
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "driving.FallbackPlan.Emergency", "1.0.0");
		
		// Añadimos CityChauffer (Base para test L3-6)
		//SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "driving.L3.CityChauffer", "1.0.0");
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "driving.L3.HighwayChauffer", "1.0.0");

		// Añadimos el Sensor Derecho (Base para test L3-8)
		//SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.RightDistanceSensor", "1.0.0");
		
		// Para Interact-1 , Interact-2 y Interact-3 
		
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.HumanSensors", "1.0.0");
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.DriverFaceMonitor", "1.0.0");
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.DriverSeatSensor", "1.0.0");
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.CopilotSeatSensor", "1.0.0");
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.HandsOnWheelSensor", "1.0.0");
		
		// Bindings iniciales
		SystemConfigurationHelper.bindingToAdd(theInitialSystemConfiguration, 
				"driving.FallbackPlan.Emergency", "1.0.0", FallbackPlanARC.REQUIRED_ENGINE,
				"device.Engine", "1.0.0", EngineARC.PROVIDED_DEVICE);

		SystemConfigurationHelper.bindingToAdd(
			    theInitialSystemConfiguration, 
			    "driving.FallbackPlan.Emergency", "1.0.0", FallbackPlanARC.REQUIRED_STEERING,
			    "device.Steering", "1.0.0", SteeringARC.PROVIDED_DEVICE);

		
		SystemConfigurationHelper.bindingToAdd(theInitialSystemConfiguration,
				"driving.L3.HighwayChauffer", "1.0.0",
			    "required_engine",
			    "device.Engine", "1.0.0",
			    "provided_device");

		SystemConfigurationHelper.bindingToAdd(theInitialSystemConfiguration,
				"driving.L3.HighwayChauffer", "1.0.0",
			    "required_frontdistancesensor",
			    "device.FrontDistanceSensor", "1.0.0",
			    "provided_sensor");

		SystemConfigurationHelper.bindingToAdd(theInitialSystemConfiguration,
				"driving.L3.HighwayChauffer", "1.0.0",
			    "required_leftlinesensor",
			    "device.LeftLineSensor", "1.0.0",
			    "provided_sensor");

		SystemConfigurationHelper.bindingToAdd(theInitialSystemConfiguration,
				"driving.L3.HighwayChauffer", "1.0.0",
			    "required_rightlinesensor",
			    "device.RightLineSensor", "1.0.0",
			    "provided_sensor");

		SystemConfigurationHelper.bindingToAdd(theInitialSystemConfiguration,
				"driving.L3.HighwayChauffer", "1.0.0",
			    "required_notificationservice",
			    "interaction.NotificationService", "1.0.0",
			    "provided_service");
		SystemConfigurationHelper.bindingToAdd(theInitialSystemConfiguration,
				"driving.L3.HighwayChauffer", "1.0.0",
			    "required_roadsensor",
			    "device.RoadSensor", "1.0.0",
			    "provided_sensor");

		SystemConfigurationHelper.bindingToAdd(theInitialSystemConfiguration,
				"driving.L3.HighwayChauffer", "1.0.0",
			    "required_fallbackplan",
			    "driving.FallbackPlan.Emergency", "1.0.0",
			    "provided_drivingservice");
		
		SystemConfigurationHelper.bindingToAdd(theInitialSystemConfiguration,
				"driving.L3.HighwayChauffer", "1.0.0",
			    "required_steering",
			    "device.Steering", "1.0.0",
			    "provided_device");
		
		SystemConfigurationHelper.bindingToAdd(
			    theInitialSystemConfiguration,
			    "driving.FallbackPlan.Emergency", "1.0.0",
			    "required_notificationservice",
			    "interaction.NotificationService", "1.0.0",
			    "provided_service");

		SystemConfigurationHelper.bindingToAdd(theInitialSystemConfiguration,
				"driving.L3.HighwayChauffer", "1.0.0",
			    "required_reardistancesensor",
			    "device.RearDistanceSensor", "1.0.0",
			    "provided_sensor");

		SystemConfigurationHelper.bindingToAdd(theInitialSystemConfiguration,
				"driving.L3.HighwayChauffer", "1.0.0",
			    "required_rightdistancesensor",
			    "device.RightDistanceSensor", "1.0.0",
			    "provided_sensor");

		SystemConfigurationHelper.bindingToAdd(theInitialSystemConfiguration,
				"driving.L3.HighwayChauffer", "1.0.0",
			    "required_leftdistancesensor",
			    "device.LeftDistanceSensor", "1.0.0",
			    "provided_sensor");
		


		SystemConfigurationHelper.bindingToAdd(theInitialSystemConfiguration,
				"device.HumanSensors", "1.0.0",
				"required_facemonitor",
				"device.DriverFaceMonitor", "1.0.0",
				"provided_sensor");

			SystemConfigurationHelper.bindingToAdd(theInitialSystemConfiguration,
				"device.HumanSensors", "1.0.0",
				"required_driverseatsensor",
				"device.DriverSeatSensor", "1.0.0",
				"provided_sensor");

			SystemConfigurationHelper.bindingToAdd(theInitialSystemConfiguration,
				"device.HumanSensors", "1.0.0",
				"required_copilotseatsensor",
				"device.CopilotSeatSensor", "1.0.0",
				"provided_sensor");

			SystemConfigurationHelper.bindingToAdd(theInitialSystemConfiguration,
				"device.HumanSensors", "1.0.0",
				"required_handsonwheelsensor",
				"device.HandsOnWheelSensor", "1.0.0",
				"provided_sensor");
			
			SystemConfigurationHelper.bindingToAdd(theInitialSystemConfiguration,
					"driving.L3.HighwayChauffer", "1.0.0",
					"required_humansensors",
					"device.HumanSensors", "1.0.0",
					"provided_sensor");
		
		
		SystemConfigurationHelper.setParameter(theInitialSystemConfiguration, 
				"driving.L3.HighwayChauffer", "1.0.0", L3_DrivingServiceARC.PARAMETER_REFERENCESPEED, "100");

		return theInitialSystemConfiguration;
	}
}
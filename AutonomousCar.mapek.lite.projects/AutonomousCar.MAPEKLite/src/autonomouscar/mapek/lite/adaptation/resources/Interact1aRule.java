package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.ARC.structures.systemconfiguration.interfaces.IRuleComponentsSystemConfiguration;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.AdaptationRule;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IKnowledgeProperty;
import es.upv.pros.tatami.adaptation.mapek.lite.exceptions.analyzing.RuleException;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.SystemConfigurationHelper;
import es.upv.pros.tatami.adaptation.mapek.lite.structures.systemconfiguration.interfaces.IRuleSystemConfiguration;
import es.upv.pros.tatami.osgi.utils.interfaces.ITimeStamped;
import sua.autonomouscar.devices.interfaces.IHumanSensors;
import sua.autonomouscar.driving.interfaces.IL3_DrivingService;
import sua.autonomouscar.infraestructure.OSGiUtils;
import sua.autonomouscar.infraestructure.driving.DrivingService;
import sua.autonomouscar.infraestructure.interaction.ARC.AuditoryBeepARC;
import sua.autonomouscar.infraestructure.interaction.ARC.AuditorySoundARC;
import sua.autonomouscar.infraestructure.interaction.ARC.HapticVibrationARC;
import sua.autonomouscar.infraestructure.interaction.ARC.NotificationServiceARC;
import sua.autonomouscar.infraestructure.interaction.ARC.VisualIconARC;

public class Interact1aRule extends AdaptationRule {

	public static final String ID = "Regla INTERACT-1a";
	private static final String VERSION = "1.0.0";

	public Interact1aRule(BundleContext context) {
		super(context, ID);
		this.setListenToKnowledgePropertyChanges(MonitorDriverAttention.KP_DRIVER_ATTENTION);
	}

	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
		return property != null && MonitorDriverAttention.KP_DRIVER_ATTENTION.equals(property.getId());
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {
		if (property == null || property.getValue() == null) {			
			throw new RuleException("DriverAttention null", "La propiedad DriverAttention es nula");
		}

		String attention = property.getValue().toString();

		IL3_DrivingService activeL3 = OSGiUtils.getService(
			this.context,
			IL3_DrivingService.class,
			String.format("(%s=true)", DrivingService.ACTIVE)
		);

		if (activeL3 == null) {			
			throw new RuleException("No hay servicio L3 activo", "INTERACT-1a solo aplica con un servicio L3 activo");
		}

		IHumanSensors humanSensors = OSGiUtils.getService(this.context, IHumanSensors.class);
		if (humanSensors == null) {
			throw new RuleException("HumanSensors no disponible", "El servicio de HumanSensors  no esta disponible");
		}

		if (!"LOOKING_FORWARD".equals(attention)) {			
			throw new RuleException("Regla no aplicable", "El conductor no está mirando al frente");
		}

		if (!humanSensors.areTheHandsOnTheWheel() || !humanSensors.isDriverSeatOccupied()) {
			throw new RuleException("Regla no aplicable", "El conductor no está completamente listo");
		}

		IRuleComponentsSystemConfiguration cfg =
			SystemConfigurationHelper.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());

		SystemConfigurationHelper.bindingToAdd(cfg,
			"interaction.NotificationService", VERSION, NotificationServiceARC.REQUIRED_SERVICE,
			"interaction.SteeringWheel", VERSION, HapticVibrationARC.PROVIDED_MECHANISM);

		SystemConfigurationHelper.bindingToAdd(cfg,
			"interaction.NotificationService", VERSION, NotificationServiceARC.REQUIRED_SERVICE,
			"interaction.DriverDisplay.VisualIcon", VERSION, VisualIconARC.PROVIDED_MECHANISM);

		SystemConfigurationHelper.bindingToRemove(cfg,
			"interaction.NotificationService", VERSION, NotificationServiceARC.REQUIRED_SERVICE,
			"interaction.Speakers.AuditorySound", VERSION, AuditorySoundARC.PROVIDED_MECHANISM);

		SystemConfigurationHelper.bindingToRemove(cfg,
			"interaction.NotificationService", VERSION, NotificationServiceARC.REQUIRED_SERVICE,
			"interaction.Speakers.AuditoryBeep", VERSION, AuditoryBeepARC.PROVIDED_MECHANISM);

		SystemConfigurationHelper.bindingToRemove(cfg,
			"interaction.NotificationService", VERSION, NotificationServiceARC.REQUIRED_SERVICE,
			"interaction.Seat.Driver", VERSION, HapticVibrationARC.PROVIDED_MECHANISM);

		return cfg;
	}
}
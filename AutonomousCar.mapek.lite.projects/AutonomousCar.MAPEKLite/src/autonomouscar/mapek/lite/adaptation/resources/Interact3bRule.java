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

public class Interact3bRule extends AdaptationRule {

	public static final String ID = "Regla INTERACT-3b";
	
	protected static SmartLogger logger = SmartLogger.getLogger(Interact3bRule.class);

	private IKnowledgeProperty kpActiveL3Service = null;
	private IKnowledgeProperty kpDriverSeatOccupied = null;

	public Interact3bRule(BundleContext context) {
		super(context, ID);
		this.setListenToKnowledgePropertyChanges("active-l3-service");
		this.setListenToKnowledgePropertyChanges("driver-seat-occupied");
	}

	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
		if (kpActiveL3Service == null) {
			kpActiveL3Service = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("active-l3-service");
		}
		if (kpDriverSeatOccupied == null) {
			kpDriverSeatOccupied = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("driver-seat-occupied");
		}

		if (kpActiveL3Service == null || kpDriverSeatOccupied == null) {
			return false;
		}

		String activeL3 = (String) kpActiveL3Service.getValue();
		Boolean seatOccupied = (Boolean) kpDriverSeatOccupied.getValue();

		if (activeL3 == null || "NONE".equals(activeL3)) {
			return false;
		}
		if (seatOccupied == null) {
			return false;
		}

		return !seatOccupied.booleanValue();
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException {
		if (kpActiveL3Service == null) {
			kpActiveL3Service = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("active-l3-service");
		}
		if (kpDriverSeatOccupied == null) {
			kpDriverSeatOccupied = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("driver-seat-occupied");
		}

		if (kpActiveL3Service == null) {
			throw new RuleException("ActiveL3Service null", "La propiedad active-l3-service es nula");
		}
		if (kpDriverSeatOccupied == null) {
			throw new RuleException("DriverSeatOccupied null", "La propiedad driver-seat-occupied es nula");
		}

		String activeL3 = (String) kpActiveL3Service.getValue();
		Boolean seatOccupied = (Boolean) kpDriverSeatOccupied.getValue();

		if (activeL3 == null || "NONE".equals(activeL3)) {
			throw new RuleException("No hay servicio L3 activo", "INTERACT-3b solo aplica con un servicio L3 activo");
		}
		if (seatOccupied == null) {
			throw new RuleException("DriverSeatOccupied sin valor", "La propiedad driver-seat-occupied no tiene valor");
		}
		if (seatOccupied.booleanValue()) {
			throw new RuleException("Regla no aplicable", "El conductor sigue sentado en el asiento");
		}
		
		logger.info("Ejecutando Regla INTERACT-3b: desactivando DriverSeat HapticVibration, DriverDisplay VisualText y DriverDisplay VisualIcon");

		IRuleComponentsSystemConfiguration cfg =
			SystemConfigurationHelper.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());

		SystemConfigurationHelper.bindingToRemove(cfg,
			"interaction.NotificationService", "1.0.0", "required_mechanisms",
			"interaction.Seat.Driver", "1.0.0", "provided_mechanism");

		SystemConfigurationHelper.bindingToRemove(cfg,
			"interaction.NotificationService", "1.0.0", "required_mechanisms",
			"interaction.DriverDisplay.VisualText", "1.0.0", "provided_mechanism");

		SystemConfigurationHelper.bindingToRemove(cfg,
			"interaction.NotificationService", "1.0.0", "required_mechanisms",
			"interaction.DriverDisplay.VisualIcon", "1.0.0", "provided_mechanism");

		return cfg;
	}
}
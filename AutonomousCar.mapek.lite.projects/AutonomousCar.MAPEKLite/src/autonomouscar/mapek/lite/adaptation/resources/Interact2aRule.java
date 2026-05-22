package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.ARC.structures.systemconfiguration.interfaces.IRuleComponentsSystemConfiguration;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.AdaptationRule;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IKnowledgeProperty;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.BasicMAPEKLiteLoopHelper;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.SystemConfigurationHelper;
import es.upv.pros.tatami.adaptation.mapek.lite.structures.systemconfiguration.interfaces.IRuleSystemConfiguration;
import es.upv.pros.tatami.osgi.utils.interfaces.ITimeStamped;
import es.upv.pros.tatami.adaptation.mapek.lite.exceptions.analyzing.RuleException;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;

public class Interact2aRule extends AdaptationRule {

	public static final String ID = "Regla INTERACT-2a";
	
	protected static SmartLogger logger = SmartLogger.getLogger(Interact2aRule.class);

	private IKnowledgeProperty kpActiveL3Service = null;
	private IKnowledgeProperty kpDriverHandsOnWheel = null;

	public Interact2aRule(BundleContext context) {
		super(context, ID);
		this.setListenToKnowledgePropertyChanges("active-l3-service");
		this.setListenToKnowledgePropertyChanges("driver-hands-on-wheel");
	}

	@Override
	public boolean checkAffectedByChange(IKnowledgeProperty property) {
		if (kpActiveL3Service == null) {
			kpActiveL3Service = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("active-l3-service");
		}
		if (kpDriverHandsOnWheel == null) {
			kpDriverHandsOnWheel = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("driver-hands-on-wheel");
		}

		if (kpActiveL3Service == null || kpDriverHandsOnWheel == null) {
			return false;
		}

		String activeL3 = (String) kpActiveL3Service.getValue();
		Boolean handsOnWheel = (Boolean) kpDriverHandsOnWheel.getValue();

		if (activeL3 == null || "NONE".equals(activeL3)) {
			return false;
		}
		if (handsOnWheel == null) {
			return false;
		}

		return handsOnWheel.booleanValue();
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) throws RuleException  {
		if (kpActiveL3Service == null) {
			kpActiveL3Service = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("active-l3-service");
		}
		if (kpDriverHandsOnWheel == null) {
			kpDriverHandsOnWheel = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("driver-hands-on-wheel");
		}

		if (kpActiveL3Service == null) {
			throw new RuleException("ActiveL3Service null", "La propiedad active-l3-service es nula");
		}
		if (kpDriverHandsOnWheel == null) {
			throw new RuleException("DriverHandsOnWheel null", "La propiedad driver-hands-on-wheel es nula");
		}

		String activeL3 = (String) kpActiveL3Service.getValue();
		Boolean handsOnWheel = (Boolean) kpDriverHandsOnWheel.getValue();

		if (activeL3 == null || "NONE".equals(activeL3)) {
			return null;
		}
		
		if (handsOnWheel == null) {
			throw new RuleException("DriverHandsOnWheel sin valor", "La propiedad driver-hands-on-wheel no tiene valor");
		}

		if (!handsOnWheel.booleanValue()) {
			return null;
		}
		
		logger.info("Ejecutando Regla INTERACT-2a: activando SteeringWheel HapticVibration");

		IRuleComponentsSystemConfiguration cfg =
			SystemConfigurationHelper.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());

		SystemConfigurationHelper.bindingToAdd(cfg,
			"interaction.NotificationService", "1.0.0", "required_mechanisms",
			"interaction.SteeringWheel", "1.0.0", "provided_mechanism");

		return cfg;
	}
}
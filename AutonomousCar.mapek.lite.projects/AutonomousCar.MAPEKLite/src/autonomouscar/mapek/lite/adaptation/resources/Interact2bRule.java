package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;

import es.upv.pros.tatami.adaptation.mapek.lite.ARC.structures.systemconfiguration.interfaces.IRuleComponentsSystemConfiguration;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.AdaptationRule;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IKnowledgeProperty;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.BasicMAPEKLiteLoopHelper;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.SystemConfigurationHelper;
import es.upv.pros.tatami.adaptation.mapek.lite.structures.systemconfiguration.interfaces.IRuleSystemConfiguration;
import es.upv.pros.tatami.osgi.utils.interfaces.ITimeStamped;

public class Interact2bRule extends AdaptationRule {

	public static final String ID = "Regla INTERACT-2b";

	private IKnowledgeProperty kpActiveL3Service = null;
	private IKnowledgeProperty kpDriverHandsOnWheel = null;

	public Interact2bRule(BundleContext context) {
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

		return !handsOnWheel.booleanValue();
	}

	@Override
	public IRuleSystemConfiguration onExecute(IKnowledgeProperty property) {
		if (kpActiveL3Service == null) {
			kpActiveL3Service = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("active-l3-service");
		}
		if (kpDriverHandsOnWheel == null) {
			kpDriverHandsOnWheel = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("driver-hands-on-wheel");
		}

		if (kpActiveL3Service == null || kpDriverHandsOnWheel == null) {
			return null;
		}

		String activeL3 = (String) kpActiveL3Service.getValue();
		Boolean handsOnWheel = (Boolean) kpDriverHandsOnWheel.getValue();

		if (activeL3 == null || "NONE".equals(activeL3) || handsOnWheel == null) {
			return null;
		}

		if (handsOnWheel.booleanValue()) {
			return null;
		}

		IRuleComponentsSystemConfiguration cfg =
			SystemConfigurationHelper.createPartialSystemConfiguration(ID + "_" + ITimeStamped.getCurrentTimeStamp());

		SystemConfigurationHelper.bindingToRemove(cfg,
			"interaction.NotificationService", "1.0.0", "required_mechanisms",
			"interaction.SteeringWheel", "1.0.0", "provided_mechanism");

		return cfg;
	}
}
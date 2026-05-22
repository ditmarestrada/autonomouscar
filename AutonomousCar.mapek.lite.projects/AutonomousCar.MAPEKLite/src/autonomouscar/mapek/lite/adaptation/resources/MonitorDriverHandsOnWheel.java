package autonomouscar.mapek.lite.adaptation.resources;

import org.osgi.framework.BundleContext;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.components.Monitor;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IKnowledgeProperty;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IMonitor;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.BasicMAPEKLiteLoopHelper;

public class MonitorDriverHandsOnWheel extends Monitor {

    public static final String ID = "Monitor Driver Hands On Wheel";
    public static final String KP_DRIVER_HANDS = "driver-hands-on-wheel";

    public MonitorDriverHandsOnWheel(BundleContext context) {
        super(context, ID);
    }

    @Override
    public IMonitor report(Object measure) {
        if (measure == null) return this;

        IKnowledgeProperty kp = BasicMAPEKLiteLoopHelper.getKnowledgeProperty(KP_DRIVER_HANDS);
        if (kp == null) return this;

        Boolean value = Boolean.valueOf(measure.toString());
        if (kp.getValue() == null || !kp.getValue().equals(value)) {
            kp.setValue(value);
        }

        return this;
    }
}
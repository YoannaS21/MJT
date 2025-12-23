import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

public class RunTests {
    public static void main(String[] args) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
            .selectors(selectPackage("bg.sofia.uni.fmi.mjt.imagekit"))
            .filters(includeClassNamePatterns(".*Test"))
            .build();

        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);

        TestExecutionSummary summary = listener.getSummary();
        System.out.println("Tests run: " + summary.getTestsStartedCount());
        System.out.println("Tests succeeded: " + summary.getTestsSucceededCount());
        System.out.println("Tests failed: " + summary.getTestsFailedCount());
        
        if (summary.getTestsFailedCount() > 0) {
            System.out.println("\nFailures:");
            summary.getFailures().forEach(failure -> {
                System.out.println(failure.getTestIdentifier().getDisplayName());
                System.out.println(failure.getException());
            });
        }
        
        System.exit(summary.getTestsFailedCount() > 0 ? 1 : 0);
    }
}

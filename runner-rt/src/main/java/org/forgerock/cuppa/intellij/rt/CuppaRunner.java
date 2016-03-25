package org.forgerock.cuppa.intellij.rt;

import java.util.ArrayList;
import java.util.List;

import org.forgerock.cuppa.Runner;
import org.forgerock.cuppa.model.TestBlock;
import org.forgerock.cuppa.reporters.DefaultReporter;

/**
 * Runs Cuppa. This class runs in a separate process forked from IntelliJ and communicates the state of the tests back
 * to the IDE.
 */
public final class CuppaRunner {
    public static void main(String args[]) {
        Runner runner = new Runner();
        List<Class<?>> testClasses = new ArrayList<>();
        for (String arg : args) {
            try {
                testClasses.add(Class.forName(arg));
            } catch (ClassNotFoundException e) {
                System.err.println("Failed to load class " + arg);
                System.exit(1);
            }
        }
        TestBlock rootBlock = runner.defineTests(testClasses);
        runner.run(rootBlock, new DefaultReporter());
    }
}

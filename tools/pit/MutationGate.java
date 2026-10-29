import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Mutation gate over PIT's mutations.xml (ub-mode "Mutator": cover the uncovered, kill the survivors).
 *
 * PIT's own mutationThreshold is a percentage, and it counts TIMED_OUT, MEMORY_ERROR and RUN_ERROR
 * as detected. This gate is stricter: every mutation must end KILLED. There is no allowlist —
 * rewrite code whose mutants cannot be killed (an equivalent mutant means the code says something twice).
 *
 * Two kinds of "not KILLED":
 *   test gap      SURVIVED, NO_COVERAGE, NON_VIABLE, ...  → fail at once.
 *   not analysed  RUN_ERROR, TIMED_OUT, MEMORY_ERROR       → PIT itself failed to finish the mutant
 *                 (on this project a minion JVM occasionally dies mid-run, hitting one random mutant,
 *                 never reproducible in isolation). With --rerun, PIT is run ONCE more and the fresh
 *                 report is judged with no further retry, so a reproducible error or timeout still fails.
 *
 * Usage:  java tools/pit/MutationGate.java target/pit-reports/mutations.xml [--rerun <command...>]
 * Exit:   0 every mutation KILLED, 1 any other status, 2 report missing or without mutations.
 */
public class MutationGate {

    private static final Set<String> NOT_ANALYSED = Set.of("RUN_ERROR", "TIMED_OUT", "MEMORY_ERROR");

    record Result(String status, String description) { }

    public static void main(String[] args) throws Exception {
        File report = new File(args.length > 0 ? args[0] : "target/pit-reports/mutations.xml");
        List<String> rerun = args.length > 2 && "--rerun".equals(args[1])
                ? Arrays.asList(args).subList(2, args.length) : List.of();
        List<Result> notKilled = notKilled(report);
        if (!notKilled.isEmpty() && !rerun.isEmpty() && notKilled.stream().allMatch(MutationGate::notAnalysed)) {
            notKilled.forEach(r -> System.out.println(r.description()));
            System.out.println("MUTATION: " + notKilled.size() + " mutation(s) not analysed by PIT; re-running PIT once");
            int code = new ProcessBuilder(rerun).inheritIO().start().waitFor();
            if (code != 0) {
                exit(1, "MUTATION: PIT re-run failed with exit code " + code);
            }
            notKilled = notKilled(report);
        }
        notKilled.forEach(r -> System.out.println(r.description()));
        if (!notKilled.isEmpty()) {
            exit(1, "MUTATION: " + notKilled.size() + " mutation(s) not KILLED");
        }
        System.out.println("MUTATION: all mutations KILLED");
    }

    static boolean notAnalysed(Result r) {
        return NOT_ANALYSED.contains(r.status());
    }

    private static List<Result> notKilled(File report) throws Exception {
        if (!report.isFile()) {
            exit(2, "MUTATION: report not found: " + report);
        }
        NodeList mutations = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(report)
                .getElementsByTagName("mutation");
        if (mutations.getLength() == 0) {
            exit(2, "MUTATION: no mutations in " + report + " (did PIT skip?)");
        }
        List<Result> notKilled = new ArrayList<>();
        for (int i = 0; i < mutations.getLength(); i++) {
            Element m = (Element) mutations.item(i);
            if (!"KILLED".equals(m.getAttribute("status"))) {
                notKilled.add(new Result(m.getAttribute("status"), describe(m)));
            }
        }
        System.out.println("MUTATION: " + mutations.getLength() + " mutations in " + report.getName());
        return notKilled;
    }

    private static String describe(Element m) {
        String owner = text(m, "mutatedClass");
        return String.format("%-12s %s.%s:%s  %s", m.getAttribute("status"),
                owner.substring(owner.lastIndexOf('.') + 1), text(m, "mutatedMethod"),
                text(m, "lineNumber"), text(m, "description"));
    }

    private static String text(Element m, String tag) {
        NodeList nodes = m.getElementsByTagName(tag);
        return nodes.getLength() == 0 ? "?" : nodes.item(0).getTextContent();
    }

    private static void exit(int code, String message) {
        System.err.println(message);
        System.exit(code);
    }
}

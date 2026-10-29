import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * CRAP gate over a JaCoCo XML report (ub-mode "Then Make it Right → Crapper").
 *
 *   CRAP = CC² × (1 − coverage)³ + CC
 *
 * CC       = JaCoCo COMPLEXITY counter (missed + covered) of the method.
 * coverage = INSTRUCTION covered / total — defined for every method, unlike BRANCH.
 *
 * Usage:  java tools/crap/Crap.java target/site/jacoco/jacoco.xml [threshold=10]
 * Exit:   0 all methods below threshold, 1 a method is at/above it, 2 report missing or empty.
 *
 * Note: a method with CC ≤ 2 scores ≤ 6 even at 0% coverage, so CRAP is not a coverage
 * gate on its own — PIT's coverageThreshold is the backstop for those.
 */
public class Crap {

    record Score(String method, int complexity, double coverage, double crap) { }

    public static void main(String[] args) throws Exception {
        File report = new File(args.length > 0 ? args[0] : "target/site/jacoco/jacoco.xml");
        double threshold = args.length > 1 ? Double.parseDouble(args[1]) : 10.0;
        if (!report.isFile()) {
            exit(2, "CRAP: report not found: " + report);
        }
        List<Score> scores = scoresOf(parse(report));
        if (scores.isEmpty()) {
            exit(2, "CRAP: no methods in " + report + " (did JaCoCo skip?)");
        }
        scores.sort(Comparator.comparingDouble(Score::crap).reversed());
        print(scores, threshold);
        long failing = scores.stream().filter(s -> s.crap() >= threshold).count();
        if (failing > 0) {
            exit(1, "CRAP: " + failing + " method(s) at or above " + threshold);
        }
        System.out.println("CRAP: all " + scores.size() + " methods below " + threshold);
    }

    static double crap(int complexity, double coverage) {
        return complexity * complexity * Math.pow(1 - coverage, 3) + complexity;
    }

    private static Document parse(File report) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // jacoco.xml declares report.dtd — never fetch it
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        return factory.newDocumentBuilder().parse(report);
    }

    private static List<Score> scoresOf(Document doc) {
        List<Score> scores = new ArrayList<>();
        NodeList methods = doc.getElementsByTagName("method");
        for (int i = 0; i < methods.getLength(); i++) {
            Element method = (Element) methods.item(i);
            int[] complexity = counter(method, "COMPLEXITY");
            int[] instructions = counter(method, "INSTRUCTION");
            if (complexity != null && instructions != null) {
                scores.add(score(method, complexity, instructions));
            }
        }
        return scores;
    }

    private static Score score(Element method, int[] complexity, int[] instructions) {
        int cc = complexity[0] + complexity[1];
        double coverage = (double) instructions[1] / (instructions[0] + instructions[1]);
        String owner = ((Element) method.getParentNode()).getAttribute("name").replace('/', '.');
        String name = owner + "." + method.getAttribute("name") + method.getAttribute("desc");
        return new Score(name, cc, coverage, crap(cc, coverage));
    }

    /** Returns {missed, covered} of the method's own counter of the given type, or null. */
    private static int[] counter(Element method, String type) {
        for (Node child = method.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element e && "counter".equals(e.getTagName()) && type.equals(e.getAttribute("type"))) {
                return new int[]{Integer.parseInt(e.getAttribute("missed")), Integer.parseInt(e.getAttribute("covered"))};
            }
        }
        return null;
    }

    private static void print(List<Score> scores, double threshold) {
        System.out.printf(Locale.ROOT, "%8s %4s %7s  %s%n", "CRAP", "CC", "cov%", "method");
        for (Score s : scores) {
            String flag = s.crap() >= threshold ? "  <-- FAIL" : "";
            System.out.printf(Locale.ROOT, "%8.1f %4d %6.1f%%  %s%s%n", s.crap(), s.complexity(), s.coverage() * 100, s.method(), flag);
        }
    }

    private static void exit(int code, String message) {
        System.err.println(message);
        System.exit(code);
    }
}

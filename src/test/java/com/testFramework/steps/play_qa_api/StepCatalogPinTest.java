package com.testFramework.steps.play_qa_api;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.But;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Pins the Cucumber glue surface: every step expression (with its Java parameter types)
 * and every @Before hook. A refactor of the step layer must leave this catalog byte-identical.
 */
class StepCatalogPinTest {

    private static final List<Class<?>> STEP_CLASSES = List.of(
            AccountsSteps.class, CommonSteps.class, HealthSteps.class, MailSteps.class, OptionsSteps.class);

    @Test
    void stepCatalogIsUnchanged() {
        List<String> lines = new ArrayList<>();
        for (Class<?> type : STEP_CLASSES) {
            for (Method m : type.getDeclaredMethods()) {
                lines.addAll(describe(type, m));
            }
        }
        lines.sort(null);
        GoldenFile.assertMatches("step-catalog.txt", String.join("\n", lines) + "\n");
    }

    private static List<String> describe(Class<?> type, Method m) {
        List<String> out = new ArrayList<>();
        String params = Arrays.stream(m.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(","));
        for (Annotation a : m.getAnnotations()) {
            String expression = stepExpression(a);
            if (expression != null) {
                out.add("step  " + a.annotationType().getSimpleName() + " \"" + expression + "\" (" + params + ") "
                        + type.getSimpleName());
            } else if (a instanceof Before b) {
                out.add("hook  Before value=\"" + b.value() + "\" order=" + b.order() + " "
                        + type.getSimpleName() + "." + m.getName() + "(" + params + ")");
            }
        }
        return out;
    }

    private static String stepExpression(Annotation a) {
        if (a instanceof Given g) return g.value();
        if (a instanceof When w) return w.value();
        if (a instanceof Then t) return t.value();
        if (a instanceof And x) return x.value();
        if (a instanceof But x) return x.value();
        return null;
    }
}

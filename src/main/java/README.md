Intentionally empty. All framework code lives in `src/test/java`.

This directory exists only because PIT (`mvn -Pquality verify`) skips a project whose
compile source root is missing on disk ("Project has either no tests or no production code").

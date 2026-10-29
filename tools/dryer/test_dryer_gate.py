"""Tests for the Dryer gate wrapper: it must fail closed on output it cannot fully parse.

Run: python3 -m unittest tools/dryer/test_dryer_gate.py   (dryer-gate.sh runs it before the gate)
"""

import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import dryer_gate  # noqa: E402

TWO = ("DUPLICATE score=1.00\n  stage/a/A.java:3-6\n  stage/a/B.java:10-13\n\n"
       "DUPLICATE score=0.84\n  stage/a/A.java:20-30\n  stage/a/C.java:1-11\n")


class ParseReport(unittest.TestCase):

    def test_no_candidates_line_is_an_empty_report(self):
        self.assertEqual([], dryer_gate.parse_report("No duplicate candidates found.\n"))

    def test_every_block_is_parsed_with_its_score_and_spans(self):
        self.assertEqual([("1.00", "stage/a/A.java", "3", "6", "stage/a/B.java", "10", "13"),
                          ("0.84", "stage/a/A.java", "20", "30", "stage/a/C.java", "1", "11")],
                         dryer_gate.parse_report(TWO))

    def test_rejects_empty_output(self):
        with self.assertRaises(ValueError):
            dryer_gate.parse_report("")

    def test_rejects_an_unknown_format(self):
        for text in ("{:candidates []}\n",
                     "DUPLICATE similarity=0.9\n  a.java:1-2\n  b.java:3-4\n",
                     "No source files to analyze.\n"):
            with self.subTest(text=text), self.assertRaises(ValueError):
                dryer_gate.parse_report(text)

    def test_rejects_trailing_output_after_the_blocks(self):
        with self.assertRaises(ValueError):
            dryer_gate.parse_report(TWO + "WARNING: skipped 3 files\n")

    def test_rejects_a_malformed_block(self):
        with self.assertRaises(ValueError):
            dryer_gate.parse_report("DUPLICATE score=1.00\n  stage/a/A.java:3-6\n")


class SingleStatementBinding(unittest.TestCase):

    def form(self, source: str) -> tuple:
        file = Path(tempfile.mkdtemp()) / "Steps.java"
        file.write_text(source, encoding="utf-8")
        return file, 1, len(source.splitlines())

    def test_a_step_whose_body_is_one_statement_is_a_binding(self):
        self.assertTrue(dryer_gate.is_single_statement_binding(*self.form(
            '    @Then("Convert {string} to X and save as {string}")\n'
            "    public void convert(String raw, String saveAs) {\n"
            "        ctx.convertAndSave(raw, X.class, saveAs);\n"
            "    }\n")))

    def test_a_hook_with_two_statements_is_not(self):
        self.assertFalse(dryer_gate.is_single_statement_binding(*self.form(
            '    @Before("@slow")\n'
            "    public void pace() throws InterruptedException {\n"
            '        rename("pause");\n'
            "        Thread.sleep(2_000);\n"
            "    }\n")))

    def test_a_one_statement_method_without_a_cucumber_annotation_is_not(self):
        self.assertFalse(dryer_gate.is_single_statement_binding(*self.form(
            "    public void convert(String raw) {\n"
            "        ctx.convertAndSave(raw, X.class, raw);\n"
            "    }\n")))

    def test_a_body_with_a_block_is_not(self):
        self.assertFalse(dryer_gate.is_single_statement_binding(*self.form(
            '    @When("Do {string}")\n'
            "    public void doIt(String x) {\n"
            "        if (x.isEmpty()) { ctx.save(x, 1); }\n"
            "    }\n")))


if __name__ == "__main__":
    unittest.main()

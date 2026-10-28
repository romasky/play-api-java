"""ub-mode Dryer gate: run unclebob/dryer over the framework code and fail on duplication >= threshold.

Usage: python3 tools/dryer/dryer_gate.py <dryer-python> [threshold]

Dryer skips every file under a `test` directory, and all of this project's code lives in
src/test/java. So the framework sources (not *Test.java, not DTO models, not pin support) are
copied to target/dryer/stage first, keeping their package paths.

One kind of candidate is accepted, by rule rather than by list: a pair where BOTH forms are a
Cucumber binding (step or hook annotation) whose body is a single statement. Cucumber needs one
method per step expression, and that one statement already calls the shared helper, so there is
nothing left to extract. Every other candidate fails the gate.
"""

from __future__ import annotations

import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SOURCES = ROOT / "src" / "test" / "java"
STAGE = ROOT / "target" / "dryer" / "stage"
EXCLUDED = (re.compile(r"Test\.java$"), re.compile(r"/models/"), re.compile(r"/GoldenFile\.java$"),
            re.compile(r"/support/"))
BINDING = re.compile(r"^\s*@(Given|When|Then|And|But|Before|After|BeforeStep|AfterStep)\b")
PAIR = re.compile(r"DUPLICATE score=([\d.]+)\n\s+(\S+):(\d+)-(\d+)\n\s+(\S+):(\d+)-(\d+)")


def stage() -> int:
    shutil.rmtree(STAGE, ignore_errors=True)
    count = 0
    for src in sorted(SOURCES.rglob("*.java")):
        rel = src.relative_to(SOURCES)
        if any(p.search("/" + rel.as_posix()) for p in EXCLUDED):
            continue
        dest = STAGE / rel
        dest.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(src, dest)
        count += 1
    return count


def original(staged: str) -> Path:
    return SOURCES / (STAGE.parent / staged).resolve().relative_to(STAGE)


def is_single_statement_binding(path: Path, start: int, end: int) -> bool:
    lines = path.read_text(encoding="utf-8").splitlines()[start - 1:end]
    if not any(BINDING.match(line) for line in lines):
        return False
    # annotations carry Cucumber expressions such as {string}; the body starts after them
    text = "\n".join(line for line in lines if not line.lstrip().startswith("@"))
    body = text[text.index("{") + 1:text.rindex("}")]
    statements = [l for l in body.splitlines() if l.strip() and not l.strip().startswith("//")]
    return "{" not in body and sum(l.rstrip().endswith(";") for l in statements) == 1


def main(dryer_python: str, threshold: str) -> int:
    print(f"dryer: staged {stage()} framework sources")
    run = subprocess.run([dryer_python, "-I", "-m", "dryer", "--threshold", threshold, "--text", str(STAGE)],
                         cwd=STAGE.parent, capture_output=True, text=True)
    if run.returncode != 0:
        sys.stderr.write(run.stdout + run.stderr)
        return 2
    accepted, failing = 0, []
    for score, lf, ls, le, rf, rs, re_ in PAIR.findall(run.stdout):
        left, right = (original(lf), int(ls), int(le)), (original(rf), int(rs), int(re_))
        if is_single_statement_binding(*left) and is_single_statement_binding(*right):
            accepted += 1
            continue
        failing.append(f"DUPLICATE score={score}\n  {display(left)}\n  {display(right)}")
    print(f"dryer: threshold {threshold}; {accepted} single-statement Cucumber binding pair(s) accepted")
    if failing:
        print("\n\n".join(failing))
        print(f"dryer: FAIL — {len(failing)} duplicate candidate(s) at or above {threshold}")
        return 1
    print("dryer: no duplication to eliminate")
    return 0


def display(form: tuple[Path, int, int]) -> str:
    path, start, end = form
    return f"{path.relative_to(ROOT)}:{start}-{end}"


if __name__ == "__main__":
    if len(sys.argv) not in (2, 3):
        sys.exit(__doc__)
    sys.exit(main(sys.argv[1], sys.argv[2] if len(sys.argv) == 3 else "0.8"))

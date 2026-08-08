#!/usr/bin/env python3
"""Trim 17_amit_shekhar_questions_answers.kt — stub duplicate sections, keep unique content.

WARNING: Not used by default. The full file is kept for interview prep.
Only run this if you explicitly want a shorter file 17 with redirects to 01–16.
"""
from pathlib import Path
import re
import shutil

ROOT = Path(__file__).resolve().parent.parent
TARGET = ROOT / "17_amit_shekhar_questions_answers.kt"
BACKUP = ROOT / "17_amit_shekhar_questions_answers.kt.bak"

# Sections to replace with redirect stubs (section_num -> primary file)
STUB_SECTIONS = {
    1: "04_coroutines_and_flows.kt",
    2: "04_coroutines_and_flows.kt",
    4: "01_android_fundamentals.kt",
    5: "01_android_fundamentals.kt",
    10: "05_data_persistence.kt",
    12: "02_app_architecture.kt",
    15: "11_testing.kt",
    17: "03_jetpack_compose.kt",
}

STUB_HINTS = {
    1: ["CPS transformation & suspend functions", "Structured concurrency guarantees", "Exception handling in coroutines"],
    2: ["Cold Flow vs hot StateFlow/SharedFlow", "stateIn / shareIn operators", "callbackFlow bridging"],
    4: ["Android app components overview", "Application class lifecycle", "Why Android app lags (frame budget)"],
    5: ["Activity vs Fragment lifecycle interleaving", "onSaveInstanceState timing", "Fragment backstack memory leaks"],
    10: ["Room vs SQLite vs DataStore", "SharedPreferences vs DataStore", "ContentProvider basics"],
    12: ["MVVM with LiveData/Flow", "Repository pattern", "Clean Architecture layers"],
    15: ["ViewModel unit testing with coroutines", "Mockito vs MockK", "Testing LiveData/Flow"],
    17: ["Compose vs View system", "Recomposition mechanics", "Compose state hoisting"],
}

SECTION_PATTERN = re.compile(
    r"^// [═━─]{3,}\s*\n"
    r"^// SECTION (?P<num>\d+)[:\s—]+(?P<title>[^\n]+)\n"
    r"(?:^// [^\n]*\n)*"
    r"^// [═━─]{3,}\s*\n",
    re.MULTILINE,
)


def make_stub(num: int, title: str, primary: str) -> str:
    hints = STUB_HINTS.get(num, ["See primary reference file for full coverage"])
    hint_lines = "\n".join(f" *   - {h}" for h in hints)
    border = "─" * 76
    return f"""// {border}
// SECTION {num}: {title.strip()}
// ⛔ STUB — Full content removed (duplicates {primary})
// 📖 Primary reference: {primary}
// {border}

/**
 * This section duplicated content from {primary}.
 * Read that file for interview tips (💡), gotchas (⚠️), and follow-ups (❓).
 *
 * Quick topics covered there:
{hint_lines}
 *
 * Use this file (17) for UNIQUE sections only — see header navigation table.
 */

"""


def find_sections(content: str) -> list[tuple[int, str, int, int]]:
    """Return list of (num, title, start, end) for each section body start."""
    markers = []
    for m in re.finditer(
        r"^// [═━─]{3,}\s*\n^// SECTION (?P<num>\d+)[:\s—]+(?P<title>[^\n]+)",
        content,
        re.MULTILINE,
    ):
        markers.append((int(m.group("num")), m.group("title").strip(), m.start()))
    markers.sort(key=lambda x: x[2])

    sections = []
    for i, (num, title, start) in enumerate(markers):
        end = markers[i + 1][2] if i + 1 < len(markers) else len(content)
        sections.append((num, title, start, end))
    return sections


def update_header(content: str) -> str:
    """Mark stubbed sections in the header Topics Covered list."""
    replacements = {
        "Section 1:  Kotlin Coroutines": "Section 1:  Kotlin Coroutines (→ 04_coroutines)",
        "Section 2:  Kotlin Flow API": "Section 2:  Kotlin Flow API (→ 04_coroutines)",
        "Section 4:  Android — Base": "Section 4:  Android — Base (→ 01_fundamentals)",
        "Section 5:  Activity and Fragment": "Section 5:  Activity and Fragment (→ 01_fundamentals)",
        "Section 10: Data Saving": "Section 10: Data Saving (→ 05_data_persistence)",
        "Section 12: Android Architecture": "Section 12: Android Architecture (→ 02_app_architecture)",
        "Section 15: Android Unit Testing": "Section 15: Android Unit Testing (→ 11_testing)",
        "Section 17: Jetpack Compose": "Section 17: Jetpack Compose (→ 03_jetpack_compose)",
    }
    for old, new in replacements.items():
        content = content.replace(old, new)

    note = (
        " * TRIMMED: Sections 1,2,4,5,10,12,15,17 are stubs pointing to files 01–11.\n"
        " *         Unique content kept in sections 3,6,7,8,9,11,13,14,16,18,19,20.\n"
    )
    if "TRIMMED:" not in content:
        content = content.replace(
            " * Source: amitshekhar.me",
            note + " * Source: amitshekhar.me",
        )
    return content


def trim() -> None:
    original = TARGET.read_text(encoding="utf-8")
    if not BACKUP.exists():
        shutil.copy2(TARGET, BACKUP)

    header_end = original.find("\n */\n\n// ─")
    if header_end == -1:
        header_end = original.find("\n */\n\n// ═")
    header = original[: header_end + len("\n */")]
    body = original[header_end + len("\n */") :]

    sections = find_sections(body)
    if not sections:
        raise SystemExit("No sections found — aborting.")

    out_parts = [header, "\n"]
    for num, title, start, end in sections:
        if num in STUB_SECTIONS:
            out_parts.append(make_stub(num, title, STUB_SECTIONS[num]))
        else:
            out_parts.append(body[start:end].lstrip("\n"))

    result = update_header("".join(out_parts))
    TARGET.write_text(result, encoding="utf-8")

    old_lines = original.count("\n") + 1
    new_lines = result.count("\n") + 1
    print(f"Trimmed {TARGET.name}: {old_lines} → {new_lines} lines ({old_lines - new_lines} removed)")
    print(f"Backup: {BACKUP.name}")


if __name__ == "__main__":
    trim()

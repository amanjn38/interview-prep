#!/usr/bin/env python3
"""Generate print-guide.html — full printable study guide from all .kt and .md sources."""
from __future__ import annotations

import html
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "print-guide.html"

KT_FILES = sorted(ROOT.glob("[0-9][0-9]_*.kt"))
MD_FILES = [
    ROOT / "16_interview_questions.md",
    ROOT / "18_googleyness_and_leadership.md",
    ROOT / "19_googleyness_questions.md",
    ROOT / "20_dsa_solutions.md",
    ROOT / "STUDY_PLAN.md",
    ROOT / "README.md",
]

CSS = """
* { box-sizing: border-box; margin: 0; padding: 0; }
body {
  font-family: Georgia, 'Times New Roman', serif;
  font-size: 9.5pt;
  line-height: 1.45;
  color: #1a1a1a;
  background: #fff;
}
.page-container { max-width: 8.5in; margin: 0 auto; padding: 0.5in 0.6in; }
.cover-page {
  min-height: 50vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  page-break-after: always;
}
.cover-title { font-size: 22pt; font-weight: bold; margin-bottom: 0.3in; }
.cover-sub { font-size: 11pt; color: #444; margin-bottom: 0.2in; }
.toc { page-break-after: always; margin-bottom: 0.4in; }
.toc h2 { font-size: 14pt; margin-bottom: 0.15in; border-bottom: 1px solid #ccc; padding-bottom: 4px; }
.toc ul { list-style: none; padding-left: 0; }
.toc li { margin: 3px 0; font-size: 9pt; }
.toc a { color: #1a1a1a; text-decoration: none; }
.toc a:hover { text-decoration: underline; }
.chapter { page-break-before: always; margin-top: 0.2in; }
.chapter:first-of-type { page-break-before: auto; }
.chapter-title {
  font-size: 16pt;
  font-weight: bold;
  margin-bottom: 0.15in;
  padding-bottom: 6px;
  border-bottom: 2px solid #1a1a1a;
}
.chapter-meta { font-size: 8.5pt; color: #666; margin-bottom: 0.15in; }
pre, .code-block {
  font-family: 'SF Mono', 'Fira Code', Consolas, monospace;
  font-size: 7.5pt;
  line-height: 1.35;
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 3px;
  padding: 8px 10px;
  overflow-x: auto;
  white-space: pre-wrap;
  word-wrap: break-word;
  margin: 8px 0;
  page-break-inside: avoid;
}
h1 { font-size: 14pt; margin: 14px 0 8px; }
h2 { font-size: 12pt; margin: 12px 0 6px; }
h3 { font-size: 10.5pt; margin: 10px 0 5px; }
p { margin: 6px 0; }
ul, ol { margin: 6px 0 6px 20px; }
li { margin: 2px 0; }
table { border-collapse: collapse; width: 100%; margin: 8px 0; font-size: 8.5pt; page-break-inside: avoid; }
th, td { border: 1px solid #ccc; padding: 4px 6px; text-align: left; }
th { background: #eee; font-weight: bold; }
blockquote { border-left: 3px solid #999; padding-left: 10px; margin: 8px 0; color: #444; font-style: italic; }
hr { border: none; border-top: 1px solid #ccc; margin: 12px 0; }
@media print {
  body { font-size: 9pt; }
  .chapter { page-break-before: always; }
  pre, .code-block { font-size: 7pt; }
  a { color: inherit; text-decoration: none; }
}
@media screen {
  .screen-nav {
    position: fixed; top: 0; left: 0; width: 220px; height: 100vh;
    overflow-y: auto; background: #f8f8f8; border-right: 1px solid #ddd;
    padding: 12px; font-size: 8pt; z-index: 100;
  }
  .screen-nav h3 { font-size: 9pt; margin-bottom: 8px; }
  .screen-nav ul { list-style: none; padding: 0; margin: 0; }
  .screen-nav li { margin: 2px 0; }
  .page-container { margin-left: 240px; }
}
"""


def slug(text: str) -> str:
    s = re.sub(r"[^a-z0-9]+", "-", text.lower()).strip("-")
    return s[:60] or "section"


def md_to_html(text: str) -> str:
    lines = text.split("\n")
    out: list[str] = []
    i = 0
    in_code = False
    code_buf: list[str] = []
    table_buf: list[str] = []

    def flush_table():
        nonlocal table_buf
        if not table_buf:
            return
        rows = [r for r in table_buf if r.strip()]
        if len(rows) < 2:
            for r in rows:
                out.append(f"<p>{inline_md(r)}</p>")
        else:
            out.append("<table>")
            headers = [c.strip() for c in rows[0].split("|") if c.strip()]
            out.append("<tr>" + "".join(f"<th>{inline_md(h)}</th>" for h in headers) + "</tr>")
            for row in rows[2:]:
                cells = [c.strip() for c in row.split("|") if c.strip()]
                out.append("<tr>" + "".join(f"<td>{inline_md(c)}</td>" for c in cells) + "</tr>")
            out.append("</table>")
        table_buf = []

    def inline_md(s: str) -> str:
        s = html.escape(s)
        s = re.sub(r"`([^`]+)`", r"<code>\1</code>", s)
        s = re.sub(r"\*\*([^*]+)\*\*", r"<strong>\1</strong>", s)
        s = re.sub(r"\*([^*]+)\*", r"<em>\1</em>", s)
        return s

    while i < len(lines):
        line = lines[i]
        if line.strip().startswith("```"):
            flush_table()
            if in_code:
                out.append(f'<pre class="code-block">{html.escape(chr(10).join(code_buf))}</pre>')
                code_buf = []
                in_code = False
            else:
                in_code = True
            i += 1
            continue
        if in_code:
            code_buf.append(line)
            i += 1
            continue
        if "|" in line and line.strip().startswith("|"):
            table_buf.append(line)
            i += 1
            continue
        flush_table()
        if line.startswith("# "):
            out.append(f"<h1>{inline_md(line[2:])}</h1>")
        elif line.startswith("## "):
            out.append(f"<h2>{inline_md(line[3:])}</h2>")
        elif line.startswith("### "):
            out.append(f"<h3>{inline_md(line[4:])}</h3>")
        elif line.startswith("- [ ] ") or line.startswith("- [x] "):
            mark = "☑" if "[x]" in line[:6] else "☐"
            out.append(f"<p>{mark} {inline_md(line[6:])}</p>")
        elif line.startswith("- "):
            out.append(f"<li>{inline_md(line[2:])}</li>")
        elif re.match(r"^\d+\.\s", line):
            out.append(f"<li>{inline_md(re.sub(r'^\\d+\\.\\s', '', line))}</li>")
        elif line.strip() == "---":
            out.append("<hr>")
        elif line.strip() == "":
            pass
        else:
            out.append(f"<p>{inline_md(line)}</p>")
        i += 1
    if in_code and code_buf:
        out.append(f'<pre class="code-block">{html.escape(chr(10).join(code_buf))}</pre>')
    flush_table()
    return "\n".join(out)


def kt_to_html(text: str) -> str:
    return f'<pre class="code-block">{html.escape(text)}</pre>'


def chapter_id(path: Path) -> str:
    return slug(path.stem)


def build() -> None:
    toc_entries: list[tuple[str, str, str]] = []  # id, title, kind
    chapters: list[str] = []

    for path in KT_FILES:
        cid = chapter_id(path)
        title = path.stem.replace("_", " ").title()
        toc_entries.append((cid, title, "kotlin"))
        content = path.read_text(encoding="utf-8")
        chapters.append(f"""
<section class="chapter" id="{cid}">
  <h1 class="chapter-title">{html.escape(title)}</h1>
  <p class="chapter-meta">{html.escape(path.name)} · Kotlin reference</p>
  {kt_to_html(content)}
</section>""")

    for path in MD_FILES:
        if not path.exists():
            continue
        cid = chapter_id(path)
        title = path.stem.replace("_", " ").title()
        toc_entries.append((cid, title, "markdown"))
        content = path.read_text(encoding="utf-8")
        chapters.append(f"""
<section class="chapter" id="{cid}">
  <h1 class="chapter-title">{html.escape(title)}</h1>
  <p class="chapter-meta">{html.escape(path.name)} · Markdown guide</p>
  {md_to_html(content)}
</section>""")

    toc_html = "\n".join(
        f'<li><a href="#{cid}">{html.escape(title)}</a></li>'
        for cid, title, _ in toc_entries
    )
    nav_html = "\n".join(
        f'<li><a href="#{cid}">{html.escape(title[:40])}</a></li>'
        for cid, title, _ in toc_entries
    )

    doc = f"""<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Google Android Interview Prep — Print Guide</title>
<style>{CSS}</style>
</head>
<body>
<nav class="screen-nav">
  <h3>Contents</h3>
  <ul>{nav_html}</ul>
</nav>
<div class="page-container">
  <div class="cover-page">
    <div class="cover-title">Google Android Interview Prep</div>
    <div class="cover-sub">SDE-2 (L4) · 4+ Years Experience</div>
    <div class="cover-sub">Domain · System Design · DSA · Googleyness</div>
    <p style="margin-top:0.3in;font-size:9pt;color:#666;">Generated from repository sources · Print or Save as PDF</p>
  </div>
  <nav class="toc">
    <h2>Table of Contents</h2>
    <ul>{toc_html}</ul>
  </nav>
  {"".join(chapters)}
</div>
</body>
</html>"""

    OUT.write_text(doc, encoding="utf-8")
    size_kb = OUT.stat().st_size / 1024
    print(f"Wrote {OUT.name} ({size_kb:.0f} KB, {len(toc_entries)} chapters)")


if __name__ == "__main__":
    build()

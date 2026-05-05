#!/usr/bin/env python3
"""Generate googleyness_COMPLETE_SHEET.html — one printable sheet: 9 STAR stories + all 96 answers."""
import re
from html import escape
from pathlib import Path

from googleyness_complete_qa_data import RAW

STORIES_HTML = r"""
<section class="block" id="stories">
<h2>Signature stories — full STAR (nine write-ups: cheat-sheet IDs S1–S8 plus S3B replay)</h2>
<p class="lead">Aligned with <code>21_googleyness_answer_generator_prompt.md</code>: hook → situation/task (~30s) → actions with <em>why</em> (~60%) → metrics → learning + one trade-off. S3 and S3B are both fully written because you use them for different question clusters.</p>

<article class="story" id="story-s1">
<h3>S1 — Mentoring Sahil (intern → owner)</h3>
<p class="hook"><strong>Hook:</strong> I ramped an intern from zero Android to owning a core SDK path without trading away my own velocity or our quality bar.</p>
<p><strong>Situation &amp; task:</strong> At DevRev I owned parts of our mobile SDK. Sahil joined as an intern straight out of college with no Android experience. His early PRs needed many rounds, and I was already carrying review load for a sensitive area. My job was both to ship and to grow him — not to do his work for him.</p>
<p><strong>Actions:</strong> I started pair programming on tiny, scoped tasks so he could see how I reasoned about lifecycle and state. Early on I caught myself giving line-level fixes; I deliberately shifted to guiding questions — for example where state should live and what would break on rotation — so he had to explain his model before I approved scope increases. I reviewed <em>approach</em> before nitpicking diffs, which cost me more time up front but cut churn later. I only expanded his tasks after he could defend trade-offs. When he first pushed back on one of my suggestions with a better alternative, I treated that as the real success signal, not just merged PRs.</p>
<p><strong>Result:</strong> Within about two months he independently owned wireframe traversal for session replay — a core SDK capability. Review cycles became shorter and his PRs needed fewer structural rewrites.</p>
<p><strong>Trade-off:</strong> Coaching this way is slower than fixing files yourself short-term; I chose sustainable ownership over short-term speed.</p>
<p><strong>Learning:</strong> Structured autonomy with a rising bar beats both hand-holding and sink-or-swim.</p>
</article>

<article class="story" id="story-s2">
<h3>S2 — Crash rate / production health (self-initiated)</h3>
<p class="hook"><strong>Hook:</strong> I drove a large crash reduction nobody had slotted — because stability was slipping and users were paying for it.</p>
<p><strong>Situation &amp; task:</strong> Crash metrics were trending the wrong way while the team stayed focused on feature work. There was no named owner for a deep investigation. I treated it like a product risk: quantify root cause, propose a tight plan, execute, and prove impact — not random patches.</p>
<p><strong>Actions:</strong> I spent two focused evenings in Crashlytics and found roughly 60% of signal clustering around the network path — brittle JSON handling, WebSocket reconnect races, and lifecycle gaps. I wrote a one-pager with impact, proposed sequencing, and tied it to Play Store featuring risk so the decision was business-visible, not “Aman’s side quest.” After buy-in I redesigned that layer with sealed Result types, defensive parsing, exponential backoff on reconnect, and coroutine scopes tied to lifecycle so failures stopped compounding. I also reduced payload size materially by changing serialization where safe.</p>
<p><strong>Result:</strong> Crash rate dropped on the order of ~40%; crash-free sessions moved above ~99.5%; we regained eligibility confidence for featuring. Fewer emergency releases.</p>
<p><strong>Trade-off:</strong> I temporarily borrowed nights from roadmap work — I made that explicit to my manager when negotiating priority.</p>
<p><strong>Learning:</strong> Production monitoring is part of the craft for mobile engineers, not a separate team’s problem.</p>
</article>

<article class="story" id="story-s3">
<h3>S3 — SDK with no spec (50+ clients, ambiguity)</h3>
<p class="hook"><strong>Hook:</strong> The spec was effectively “make integration easy” — I turned conflicting needs into a layered platform instead of a one-size-fits-none compromise.</p>
<p><strong>Situation &amp; task:</strong> I built Kotlin SDK modules wrapping REST and WebSocket APIs for dozens of enterprise clients. Requirements conflicted: some wanted a high-level “just works” surface, others needed granular control, and a few needed raw access. Picking one persona would have forked support load or forced bad abstractions.</p>
<p><strong>Actions:</strong> I interviewed five integration engineers from large accounts and paired with customer success to see real tickets. I mapped three patterns, then designed a layered API: convenience defaults, a Flow-based mid layer for streaming cases, and explicit escape hatches for power users. I shipped a thin vertical to three beta clients, measured which tier they used, and iterated before broad rollout.</p>
<p><strong>Result:</strong> About 80% of clients stayed on the high-level path; integrations commonly landed under a day; fewer bespoke threads for edge cases.</p>
<p><strong>Trade-off:</strong> Layering is more code to maintain than a single facade; I accepted that cost to avoid N custom forks.</p>
<p><strong>Learning:</strong> In ambiguity, talk to users first and ship small before designing the whole universe.</p>
</article>

<article class="story" id="story-s3b">
<h3>S3B — Session replay rebuild (contractor exit, no docs)</h3>
<p class="hook"><strong>Hook:</strong> I inherited a stalled, fragile replay pipeline and chose a controlled rebuild once one real user path made the failure mode obvious.</p>
<p><strong>Situation &amp; task:</strong> Legacy Java replay code had OOM risk from bitmap handling, races from unstructured concurrency, NPEs in traversal, and callback-heavy pipelines that were hard to test. A contractor rewrite ran long; they were stretched across contracts and left without docs. Product still needed a reliable path to ship.</p>
<p><strong>Actions:</strong> Before exit I ran weekly syncs and read their modules to reduce single-person dependency. After they left I traced one screenshot end-to-end: capture → zip → upload → backend reconstruction. Salvage would have meant patching race classes indefinitely, so I rebuilt the capture/upload slice in Kotlin with structured concurrency, Flow with backpressure, sealed upload states, and vertical slices — one path working before edge-case sprawl.</p>
<p><strong>Result:</strong> We moved from “stuck for months” to shipping; eliminated whole classes of crashes tied to the old approach.</p>
<p><strong>Trade-off:</strong> Rewrite risk vs. patch risk — I chose rewrite only after the trace proved depth of coupling.</p>
<p><strong>Learning:</strong> Never assume handoff; always own one path e2e early. With difficult collaborators, reduce dependency and document decisions.</p>
</article>

<article class="story" id="story-s4">
<h3>S4 — MVVM migration / tech–product tension (Fiorano)</h3>
<p class="hook"><strong>Hook:</strong> I disagreed with “ship faster on spaghetti” — I made the tax visible with data, then de-risked with a POC instead of architecture preaching.</p>
<p><strong>Situation &amp; task:</strong> The Android app was Java-heavy with logic in Activities, tangled navigation, and painful regressions. Store rating was weak; features were overrunning estimates badly. My manager’s priority was client-visible velocity. Mine was that the foundation was taxing every future feature.</p>
<p><strong>Actions:</strong> I tracked three representative features for overrun and regression counts so the pain was numeric, not opinionated. I rebuilt the worst screen as MVVM in a POC — ViewModel state, repository IO, UI dumb — and showed zero regressions plus faster iteration on that screen. I proposed a pragmatic rule: new work in MVVM; legacy refactors opportunistically when touched — no big-bang rewrite.</p>
<p><strong>Result:</strong> Coverage climbed materially (roughly 40%→72%); rating improved (roughly 3.8→4.5); onboarding got easier on migrated surfaces.</p>
<p><strong>Trade-off:</strong> Dual patterns in repo temporarily; I accepted that to keep shipping while migrating.</p>
<p><strong>Learning:</strong> Influence with managers is evidence + low-risk path, not louder opinions.</p>
</article>

<article class="story" id="story-s5">
<h3>S5 — DETE startup (failure with learning)</h3>
<p class="hook"><strong>Hook:</strong> My clearest “MBA” was a real product with real users where I picked the wrong economic constraints.</p>
<p><strong>Situation &amp; task:</strong> I co-founded DETE — hyperlocal delivery — building three Kotlin apps with Firebase and shared modules, leading ~15 contributors without formal authority. We reached hundreds of users and dozens of merchants, but unit economics did not close in our geography. I had placement pressure and an education loan; continuing would have meant false hope for users and unsustainable burn for me.</p>
<p><strong>Actions:</strong> I separated technical success from business success honestly. I sunset responsibly, learned where I over-invested in architecture versus validation, and carried forward operational habits (vertical slices, user-visible metrics) into every later job.</p>
<p><strong>Result:</strong> Hard stop with lessons intact; later work shows the behavior change — I timebox spikes and ask “user impact and margin?” earlier.</p>
<p><strong>Trade-off:</strong> I could have raised longer — I chose truth on sustainability over narrative.</p>
<p><strong>Learning:</strong> Timing and margins matter as much as code quality; engineering without PMF is wasted.</p>
</article>

<article class="story" id="story-s6">
<h3>S6 — Bank / enterprise SDK adoption</h3>
<p class="hook"><strong>Hook:</strong> I turned a loud escalation into a partnership by centering facts, empathy, and a communication contract — then fixed the systemic tech issue.</p>
<p><strong>Situation &amp; task:</strong> Our Kotlin SDK served 50+ enterprises; banks were hardest — compliance, legacy stacks, fear of third-party code. One account escalated around me to leadership. I needed to protect roadmap integrity while making them successful.</p>
<p><strong>Actions:</strong> I convened their lead engineer and EM with a ticket timeline that separated SDK defects from integration mistakes — blame became diagnosis. I asked them to teach me their compliance and release train so I could speak their language. I proposed dedicated Slack, weekly sync, and a four-hour response window. Technically I fixed SSL pinning conflicts and generalized a configurable networking layer that helped a large fraction of clients, not only them. I wrote a banking FAQ for CS to reuse.</p>
<p><strong>Result:</strong> Escalations stopped; that integration landed in about two weeks with EM praise; average client integration time dropped toward under two days.</p>
<p><strong>Trade-off:</strong> High-touch for one loud account — I insisted paired systemic fixes so the time scaled.</p>
<p><strong>Learning:</strong> Difficult stakeholders often feel unheard; fix communication first, then code.</p>
</article>

<article class="story" id="story-s7">
<h3>S7 — Testing culture (above job description)</h3>
<p class="hook"><strong>Hook:</strong> I wasn’t hired to fix QA culture — I did it anyway because regressions were a tax on everyone’s velocity.</p>
<p><strong>Situation &amp; task:</strong> Fiorano had weak coverage, little CI discipline, and constant regressions. I was nominally a feature engineer, but watching the same bug classes recur each sprint was unsustainable.</p>
<p><strong>Actions:</strong> I led by example: tests on every PR — unit, in-memory Room, Compose UI where it paid. I proposed a focused testing sprint, wrote a short team guide, and worked with CI to enforce a coverage threshold on merge. On RouteChef (side app) I proved I could hold a higher bar at scale — crash rate improved materially with disciplined testing.</p>
<p><strong>Result:</strong> Team coverage rose toward ~72%; regressions dropped; releases got calmer.</p>
<p><strong>Trade-off:</strong> Slower first PRs while habits formed — I communicated that as an investment, not a surprise.</p>
<p><strong>Learning:</strong> Going above and beyond means leverage for the team, not hero hours.</p>
</article>

<article class="story" id="story-s8">
<h3>S8 — Led ~15 without authority (DETE)</h3>
<p class="hook"><strong>Hook:</strong> Imagine leading where you cannot hire, fire, or pay — you only earn work through clarity, craft, and care.</p>
<p><strong>Situation &amp; task:</strong> Technical co-founder with students, freelancers, and part-time contributors shipping three interconnected apps. Chaos, uneven skills, morale risk when visibility felt unfair.</p>
<p><strong>Actions:</strong> I established shared Kotlin modules so improvements amortized. I wrote architecture, CI, and examples first to earn credibility. I matched tasks to skill, gave public credit, used teaching PRs, and joined merchant calls so the team heard “why.” When a driver-app contributor felt invisible, I restructured demos and gave them a user-facing slice. Weekly cross-functional syncs kept design/marketing/ops aligned.</p>
<p><strong>Result:</strong> Three apps in users’ hands; hundreds of users; two people later credited DETE when landing Android roles.</p>
<p><strong>Trade-off:</strong> I couldn’t pay for focus — I bought alignment with systems and respect.</p>
<p><strong>Learning:</strong> Leadership without authority is infrastructure plus trust before followership.</p>
</article>
</section>
"""


def P(text: str) -> str:
    return "<p>" + text + "</p>"


def _word_count(html: str) -> int:
    t = re.sub(r"<[^>]+>", " ", html)
    t = re.sub(r"\s+", " ", t).strip()
    return len(t.split()) if t else 0


# Natural closing material only when the four core paragraphs are still under ~200 spoken words.
_GOOGLE_BRIDGES = (
    "At Google I want the same operating style at a much larger blast radius: migration-aware changes, crisp design docs, and reviews that teach invariants, not only catch nits. I default to evidence when trade-offs are hard, state disagreement once with data, then commit fully once a lead decides. Early ramp, I plan to ship a small fix and trace one end-to-end path in the codebase first, because that is how I onboard fast without pretending I already know years of compatibility history.",
    "This instinct is what I would bring to L4 Android platform work: protect users when incentives push toward silence, make uncertainty legible with written trade-offs, and scale fixes so one loud customer improves the product for everyone. I am excited to learn from people who authored the tools I rely on daily, and I stay humble about launch processes and cross-team migrations I have not yet lived at true ecosystem scale.",
    "Even when a prompt is reflective, I anchor in receipts: ownership without assignment, ambiguity handled with users in the loop, and influence through prototypes and data instead of louder arguments. At Google the cost of being wrong is higher, so I treat that bar as growth I am seeking. I keep answers aligned with metrics and outcomes I can defend honestly in committee.",
)


def _ensure_long_enough(n: int, body: str) -> str:
    wc = _word_count(body)
    if wc >= 200:
        return body
    b = body
    i = 0
    while _word_count(b) < 200 and i < len(_GOOGLE_BRIDGES):
        b += P(_GOOGLE_BRIDGES[(n + i) % len(_GOOGLE_BRIDGES)])
        i += 1
    return b


def qa(n: int, q: str, story: str, clarify: str, body: str) -> str:
    cq = escape(clarify)
    return f"""<div class="qa" id="q{n}">
<div class="qh"><span class="qn">Q{n}</span><span class="qs">{escape(story)}</span></div>
<p class="qt">{escape(q)}</p>
<p class="clarify"><strong>Clarifying question you could ask:</strong> &#8220;{cq}&#8221;</p>
<div class="ans">{body}</div>
</div>"""


def build_qa_list() -> list[tuple[int, str, str, str, str]]:
    raw = list(RAW)
    rows = sorted(raw, key=lambda t: t[0])
    if len(rows) != 96:
        raise RuntimeError(f"expected 96 QA rows, got {len(rows)}")
    seen: set[int] = set()
    out: list[tuple[int, str, str, str, str]] = []
    for n, q, s, clarify, paras in rows:
        if n in seen:
            raise RuntimeError(f"duplicate question number {n}")
        seen.add(n)
        body = "".join(P(p) for p in paras)
        body = _ensure_long_enough(n, body)
        out.append((n, q, s, clarify, body))
    return out


QA: list[tuple[int, str, str, str, str]] = build_qa_list()


HTML_HEAD = r"""<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Aman Jain — Googleyness &amp; Leadership — Complete Interview Sheet</title>
<style>
:root{--bg:#12121a;--card:#1a2230;--text:#eaeaf0;--muted:#9aa3b2;--a:#3dd6c3;--line:rgba(255,255,255,.08)}
*{box-sizing:border-box}
body{margin:0;font-family:ui-sans-serif,system-ui,Segoe UI,Roboto,sans-serif;background:var(--bg);color:var(--text);font-size:15px;line-height:1.6;padding:20px 18px 48px}
h1{font-size:1.35rem;color:var(--a);text-align:center;margin:0 0 8px}
.sub{text-align:center;color:var(--muted);font-size:.88rem;max-width:52rem;margin:0 auto 12px;line-height:1.45}
nav.toc{max-width:52rem;margin:0 auto 20px;padding:12px 14px;background:var(--card);border:1px solid var(--line);border-radius:10px;font-size:.82rem;line-height:1.65}
nav.toc strong{color:var(--a);display:block;margin-bottom:6px;font-size:.78rem;letter-spacing:.06em;text-transform:uppercase}
nav.toc a{color:#9ed4ff;text-decoration:none;margin-right:10px;white-space:nowrap}
nav.toc a:hover{text-decoration:underline}
h2{font-size:1.1rem;color:var(--a);margin:28px 0 12px;border-bottom:1px solid var(--line);padding-bottom:6px}
h3{font-size:1rem;margin:0 0 8px;color:#c8eef8}
.lead{color:var(--muted);font-size:.9rem;margin:0 0 16px}
.story{background:var(--card);border-radius:10px;padding:16px 18px;margin-bottom:16px;border-left:4px solid var(--a)}
.story .hook{margin-bottom:10px}
.story p{margin:0 0 10px}
.block{margin-bottom:8px}
.qa{background:var(--card);border-radius:8px;padding:14px 16px;margin-bottom:12px;border:1px solid var(--line)}
.qh{display:flex;flex-wrap:wrap;gap:8px;align-items:center;margin-bottom:8px}
.qn{font-weight:800;color:var(--a);min-width:2.2rem}
.qs{font-size:.75rem;color:#8db8ff;text-transform:uppercase;letter-spacing:.04em}
.qt{color:#dfe7f5;font-weight:600;margin:0 0 8px;font-size:.95rem}
.ans{color:var(--muted);font-size:.9rem}
.ans p{margin:0 0 10px}
.ans p:last-child{margin-bottom:0}
.clarify{font-size:.88rem;color:#c9e8ff;border-left:3px solid var(--a);padding:10px 12px 10px 14px;margin:0 0 12px;background:rgba(61,214,195,.1);line-height:1.5}
.clarify strong{color:var(--text);font-weight:700}
code{font-size:.82em;background:rgba(255,255,255,.06);padding:2px 6px;border-radius:4px}
@media print{
body{background:#fff;color:#111;padding:12px;font-size:11px}
h1,h2,h3{color:#111}.story,.qa{border-color:#ccc;background:#f8f8f8}.ans,.lead,.sub,.qt{color:#222}.qs{color:#444}
nav.toc{background:#f0f4f8!important;border-color:#ccc!important}
nav.toc a{color:#0645ad!important}
.clarify{color:#111!important;background:#eef8f6!important;border-left-color:#0a7a6a!important}
.story{break-inside:avoid}.qa{break-inside:avoid}
}
</style>
</head>
<body>
<h1>Googleyness &amp; Leadership — Complete Interview Sheet</h1>
<div class="sub"><strong>One document:</strong> nine full STAR stories (S1–S8 + S3B), then all 96 G&amp;L questions with a suggested clarifying line plus long-form spoken answers (target ~200–320 words after augmentation where needed). Align every number with your résumé; vary phrasing live. Sources: <code>21_googleyness_answer_generator_prompt.md</code>, <code>19_googleyness_real_questions.md</code>, story routing from <code>23_googleyness_96q_codeword_cheatsheet.html</code>.</div>
<nav class="toc" aria-label="Contents"><strong>Jump</strong>
<a href="#stories">STAR stories</a>
<a href="#cat1">Q1–12</a>
<a href="#cat2">Q13–27</a>
<a href="#cat3">Q28–36</a>
<a href="#cat4">Q37–42</a>
<a href="#cat5">Q43–53</a>
<a href="#cat6">Q54–59</a>
<a href="#cat7">Q60–61</a>
<a href="#cat8">Q62–67</a>
<a href="#cat9">Q68–72</a>
<a href="#cat10">Q73–92</a>
<a href="#cat11">Q93–96</a>
</nav>
"""

HTML_TAIL = """
<p class="sub" style="margin-top:32px">Tip: Say the clarifying question only when it buys thinking time or improves fit; skip if the interviewer already narrowed the scope (see <code>21_googleyness_answer_generator_prompt.md</code>).</p>
</body>
</html>
"""


def main():
    cats = [
        ("Category 1: Collaboration & Teamwork (Q1–12)", range(1, 13)),
        ("Category 2: Leadership & Influence (Q13–27)", range(13, 28)),
        ("Category 3: Conflict & Disagreement (Q28–36)", range(28, 37)),
        ("Category 4: Failure & Learning (Q37–42)", range(37, 43)),
        ("Category 5: Ambiguity & Decision-Making (Q43–53)", range(43, 54)),
        ("Category 6: Feedback & Self-Awareness (Q54–59)", range(54, 60)),
        ("Category 7: Mentorship (Q60–61)", range(60, 62)),
        ("Category 8: Initiative & Above and Beyond (Q62–67)", range(62, 68)),
        ("Category 9: Technical Behavioral (Q68–72)", range(68, 73)),
        ("Category 10: Googleyness / Culture Fit (Q73–92)", range(73, 93)),
        ("Category 11: Reflective / Abstract (Q93–96)", range(93, 97)),
    ]
    by_n = {n: (q, s, clarify, body) for n, q, s, clarify, body in QA}
    out = [HTML_HEAD, STORIES_HTML]
    for ci, (title, nums) in enumerate(cats, start=1):
        out.append(f'<h2 id="cat{ci}">{escape(title)}</h2>\n')
        for n in nums:
            q, s, clarify, body = by_n[n]
            out.append(qa(n, q, s, clarify, body))
    out.append(HTML_TAIL)
    root = Path("/Users/amanjn38/Desktop/interview-prep")
    html = "".join(out)
    primary = root / "googleyness_COMPLETE_SHEET.html"
    legacy = root / "24_googleyness_detailed_star_sheet.html"
    primary.write_text(html, encoding="utf-8")
    legacy.write_text(html, encoding="utf-8")
    print("Wrote", primary, "and", legacy, "chars", len(html))


if __name__ == "__main__":
    main()

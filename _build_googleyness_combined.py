#!/usr/bin/env python3
"""Build 20_googleyness_one_day.html from 18 + embedded content from 19."""
from pathlib import Path

ROOT = Path("/Users/administrator/google-android-interview-prep")
SRC = ROOT / "18_googleyness_prep.html"
OUT = ROOT / "20_googleyness_one_day.html"

# (category_title, count, story_hint, [(n, q, source), ...])
DATA = [
    (
        "Collaboration & Teamwork",
        12,
        "Map to stories about XFN work, difficult peers/stakeholders, and communication (often Stories 1–3 or whichever matches your experience).",
        [
            (1, "Tell me about a time you worked on a cross-functional team.", "FinalRoundAI"),
            (2, "Tell me about a time you worked with people outside your team.", "FinalRoundAI (variant), InterviewKickstart"),
            (3, "Tell me about a time you had a colleague who was difficult to work with.", "Interviewing.io (ex-Google interviewer guide)"),
            (4, "Tell me about a time you worked with a difficult stakeholder.", "FinalRoundAI"),
            (5, "Tell me about a time when you interacted with difficult stakeholders.", "InterviewKickstart"),
            (6, "Tell me about a time when you worked with a super performer in your team.", "InterviewKickstart"),
            (7, "Tell me about a time you worked with a poor performer.", "InterviewKickstart"),
            (8, "How do you handle working with someone whose style is different from yours?", "Interviewing.io, multiple candidate compilations"),
            (9, "What values and traits would you bring to the Google team that would help promote teamwork and collaboration?", "MockQuestions.com"),
            (10, "Describe your communication style. How will it benefit you in this job with Google?", "MockQuestions.com"),
            (11, "Describe your ideal team.", "Multiple candidate reports"),
            (12, "What would your teammates say about you?", "Multiple candidate reports"),
        ],
    ),
    (
        "Leadership & Influence",
        15,
        "Use influence-without-authority, mentoring, and initiative stories (often Stories 2, 3, 4, 5, 6).",
        [
            (13, "Tell me about a time you led without formal authority.", "FinalRoundAI"),
            (14, "Tell me about a time you led a project or initiative without being the manager.", "FinalRoundAI (variant), InterviewKickstart"),
            (15, "Tell me about a time when you took up a leadership role unexpectedly.", "InterviewKickstart"),
            (16, "Tell me about a time you had to convince someone to follow your strategy.", "InterviewKickstart"),
            (17, "As a leader, how do you get others to follow your ideas?", "InterviewKickstart"),
            (18, "Tell me about a time you influenced without authority.", "Multiple candidate reports"),
            (19, "At Google, we seek to hire natural leaders. What leadership skills and qualities would you bring to this role?", "MockQuestions.com"),
            (20, "Talk to me about a time when you led by example.", "InterviewKickstart"),
            (21, "What do you think are the qualities of a good leader?", "InterviewKickstart"),
            (22, "What do you enjoy about managing others?", "InterviewKickstart"),
            (23, "How do you make sure that you delegate tasks effectively?", "InterviewKickstart"),
            (24, "What do you do to gain commitment from your team?", "InterviewKickstart"),
            (25, "What is your weakness as a leader?", "InterviewKickstart"),
            (26, "What do you do when someone you are managing doesn't deliver as promised?", "InterviewKickstart"),
            (27, "What do you do to make sure that your team sticks to a schedule?", "InterviewKickstart"),
        ],
    ),
    (
        "Conflict & Disagreement",
        9,
        "Top frequency topic — prep your clearest technical/priorities disagreement and resolution (map to your conflict/alignment stories).",
        [
            (28, "Tell me about a challenge or conflict you faced at work. How did you handle it?", "FinalRoundAI"),
            (29, "Tell me about a time you disagreed with a teammate or manager.", "FinalRoundAI, InterviewKickstart"),
            (30, "Tell me about a time you disagreed with your manager and convinced them.", "InterviewKickstart"),
            (31, "Tell me about a time when you had a conflict with your team members during a live project.", "InterviewKickstart"),
            (32, "Give me an example of an XFN (cross-functional) conflict and how you resolved it.", "InterviewKickstart"),
            (33, "Talk to me about an unpopular decision that you made at work.", "InterviewKickstart"),
            (34, "What do you do when you disagree with a decision that's already been made?", "Multiple candidate reports"),
            (35, "What are one or two behaviors or attitudes that are most likely to cause conflict with you at work?", "MockQuestions.com"),
            (36, "How would you address a gap in skills or a personality conflict on your team?", "FinalRoundAI"),
        ],
    ),
    (
        "Failure & Learning from Mistakes",
        6,
        "Top frequency — use an honest failure with concrete learning and a behavior change (your 'failure' STAR).",
        [
            (37, "Describe a time you made a mistake or failed. What did you learn?", "FinalRoundAI"),
            (38, "Tell me about a time something didn't go as planned.", "Multiple candidate reports"),
            (39, "Tell me about a time when you started a new project but failed miserably.", "InterviewKickstart"),
            (40, "When was the last time you made a wrong decision?", "InterviewKickstart"),
            (41, "Tell me about a time when you were unable to meet your own expectations.", "InterviewKickstart"),
            (42, "When was the last time you faced an unexpected setback?", "InterviewKickstart"),
        ],
    ),
    (
        "Navigating Ambiguity & Decision-Making",
        11,
        "Map to times you made progress with unclear requirements, tradeoffs, or priority storms.",
        [
            (43, "Tell me about a time you had to deal with ambiguity or changing requirements.", "FinalRoundAI"),
            (44, "Tell me about a time you had to make progress with unclear requirements.", "Multiple candidate reports"),
            (45, "Tell me about a time you had to solve a problem creatively.", "FinalRoundAI"),
            (46, "Tell me about a time you had to take a crucial decision under pressure.", "InterviewKickstart"),
            (47, "Tell me about a time you had to make a decision quickly.", "Multiple candidate reports"),
            (48, "How do you prioritize tasks?", "InterviewKickstart"),
            (49, "How do you prioritize when everything is urgent?", "Multiple candidate reports"),
            (50, "What process do you follow to prioritize requests from different stakeholders?", "InterviewKickstart"),
            (51, "Describe a situation when you had to work with limited resources.", "InterviewKickstart"),
            (52, "How would you handle the continually adapting and changing work environment at Google?", "MockQuestions.com"),
            (53, "Working at Google, you will experience changes on a regular basis. When have you had to handle major workplace changes in the past?", "MockQuestions.com"),
        ],
    ),
    (
        "Feedback & Self-Awareness",
        6,
        "Use growth-mindset examples: feedback you acted on, weakness with mitigation, or a retrospective.",
        [
            (54, "Tell me about a time you received negative feedback.", "Interviewing.io (ex-Google interviewer guide)"),
            (55, "Share an example where you received constructive feedback and how you applied it.", "FinalRoundAI"),
            (56, "How do you handle feedback?", "Multiple candidate reports"),
            (57, "What's your biggest weakness?", "Multiple candidate reports, MockQuestions.com"),
            (58, "If you had to improve in one area as you join the Google team, what would it be?", "MockQuestions.com"),
            (59, "How would you rate your performance in this interview so far?", "MockQuestions.com"),
        ],
    ),
    (
        "Mentorship & Growing Others",
        2,
        "Map to teaching, code reviews, and coaching junior engineers (often a dedicated story).",
        [
            (60, "Tell me about a time you helped someone grow.", "Multiple candidate reports"),
            (61, "Tell me about a time you were mentoring a more junior engineer and your feedback wasn't getting through to them.", "Interviewing.io (ex-Google interviewer guide)"),
        ],
    ),
    (
        "Initiative & Going Above and Beyond",
        6,
        "Map to 'saw a gap and fixed it', stretch ownership, and technical + people complexity.",
        [
            (62, "Tell me about a time you identified a problem and fixed it without being asked.", "Multiple candidate reports"),
            (63, "Tell me about a time you went beyond your job description.", "Multiple candidate reports"),
            (64, "Talk to me about a situation when you took on responsibilities beyond expectations.", "InterviewKickstart"),
            (65, "What is the biggest challenge that you have faced in your current role?", "InterviewKickstart"),
            (66, "Tell me about a time when you faced a technical and interpersonal challenge.", "InterviewKickstart"),
            (67, "When was the last time you thought outside the box?", "InterviewKickstart"),
        ],
    ),
    (
        "Technical Behavioral (reported from SDE interviews)",
        5,
        "Often the same as your flagship project or impact story — pick one 'hero' arc with clear obstacles.",
        [
            (68, "Describe a challenging project you worked on and how you overcame obstacles.", "GeeksforGeeks (Google SDE experience)"),
            (69, "How do you handle conflicting opinions within a team when making critical decisions?", "GeeksforGeeks (Google SDE experience)"),
            (70, "How do you ensure quality in your work?", "Multiple candidate reports"),
            (71, "What's the most impactful thing you've done in your career?", "Multiple candidate reports"),
            (72, "How do you stay current with technology?", "Multiple candidate reports"),
        ],
    ),
    (
        "Googleyness / Culture Fit",
        20,
        "Tie to Section 6 (Why Google), mission, learning, and authentic hobbies — not generic praise.",
        [
            (73, "Why Google?", "FinalRoundAI, MockQuestions.com, universally reported"),
            (74, "Why do you want to work for Google?", "MockQuestions.com"),
            (75, "What excites you the most about the potential to become a Googler?", "MockQuestions.com"),
            (76, "Culture fit is important to us at Google. How would you describe your personality?", "MockQuestions.com"),
            (77, "We encourage innovation at Google. What would innovation mean to you in this role?", "MockQuestions.com"),
            (78, "In your own words, describe what Google's company mission means to you.", "MockQuestions.com"),
            (79, "Google celebrates diversity in many ways. In which ways do you celebrate those different from you?", "MockQuestions.com"),
            (80, "What do you think you will like about this role with Google?", "MockQuestions.com"),
            (81, "Who is the smartest person that you know and why do you consider them smart?", "MockQuestions.com"),
            (82, "If you had the ultimate power, what is one way that you would help make an impactful difference in the world?", "MockQuestions.com"),
            (83, "What do you like to work on in your spare time?", "MockQuestions.com"),
            (84, "If you could pick a Google Doodle of the day, what would it be?", "MockQuestions.com"),
            (85, "How do you define success in the work that you do?", "MockQuestions.com"),
            (86, "What is your favorite Google product?", "MockQuestions.com"),
            (87, "What do you believe is the biggest challenge that Google faces today?", "MockQuestions.com"),
            (88, "What do you consider to be your greatest work-related accomplishment?", "MockQuestions.com"),
            (89, "What resources and strategies do you have in place to handle stress on the job?", "MockQuestions.com"),
            (90, "What challenges do you anticipate in this role if hired at Google? How would you plan to meet those challenges head on?", "MockQuestions.com"),
            (91, "If hired for this role at Google, how long will it take you to make a meaningful contribution to our team?", "MockQuestions.com"),
            (92, "If you could start your career over again, what direction would you take?", "MockQuestions.com"),
        ],
    ),
    (
        "Reflective / Abstract Questions",
        4,
        "Convert every abstract question into a concrete experience + lesson (Google-specific pattern).",
        [
            (93, "What do you think about setting goals?", "Interviewing.io (ex-Google, characteristic reflective question)"),
            (94, "If you were going to plan an offsite for the team, how would you do it?", "Interviewing.io (ex-Google interviewer guide)"),
            (95, "We consider research to be the backbone of what we do at Google. Do you think it's important to conduct research before beginning a project or would you rather jump right in?", "MockQuestions.com"),
            (96, "How do you keep up-to-date on new trends and changes in your line of work?", "MockQuestions.com"),
        ],
    ),
]


def esc(s: str) -> str:
    return (
        s.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace('"', "&quot;")
    )


def build_question_bank_html() -> str:
    parts = []
    for title, count, hint, items in DATA:
        inner = [
            f'<p class="rq-cat-hint" style="font-size:12px;color:var(--text-secondary);line-height:1.6;margin:0 0 16px;padding:12px 14px;background:rgba(255,255,255,.02);border-radius:8px;border-left:2px solid var(--accent)"><strong style="color:var(--accent)">Prep angle:</strong> {esc(hint)}</p>'
        ]
        for n, q, src in items:
            inner.append(
                '<div class="rq-question">'
                f'<div class="rq-q-text">{n}. {esc(q)}</div>'
                f'<div class="rq-q-source">Source: {esc(src)}</div>'
                "</div>"
            )
        parts.append(
            '<div class="rq-category">'
            f'<div class="rq-cat-header"><span class="rq-cat-arrow">&#9654;</span> {esc(title)}'
            f'<span class="rq-cat-count">{count}</span></div>'
            f'<div class="rq-cat-body"><div class="rq-cat-body-inner">'
            f'{"".join(inner)}'
            "</div></div></div>"
        )
    return "\n".join(parts)


PATTERNS_AND_ANTIPAT = """
<div class="rq-patterns" id="sec-10-patterns">
<h3>Key patterns (from real-question research)</h3>
<div class="rq-pattern-item"><b>Pattern 1:</b> The overwhelming majority of G&amp;L questions are situation-based (&quot;tell me about a time&quot;). Generic hypotheticals score poorly.</div>
<div class="rq-pattern-item"><b>Pattern 2:</b> Google also asks <em>reflective</em> questions (&quot;What do you think about X?&quot;). Per interviewing.io, convert these into answers grounded in real experience.</div>
<div class="rq-pattern-item"><b>Pattern 3 &mdash; five core signals (InterviewBit + sources):</b> (1) Do the right thing when it is hard, (2) comfort with ambiguity, (3) genuine collaboration over heroics, (4) intellectual humility, (5) continuous learning.</div>
<div class="rq-pattern-item"><b>Pattern 4 &mdash; leadership for ICs:</b> influence through data and empathy, mentoring, unassigned ownership, driving ideas to completion &mdash; not &quot;people manager&quot; theater.</div>
<div class="rq-pattern-item"><b>Pattern 5:</b> The behavioral round is sometimes de-emphasized in scheduling, but when it runs it still matters in the packet; a bad G&amp;L score can sink a strong loop.</div>
<div class="rq-pattern-item"><b>Pattern 6 &mdash; highest frequency topics across sources:</b> (1) conflict/disagreement, (2) failure/mistakes, (3) leadership without authority, (4) collaboration/XFN, (5) ambiguity, (6) growing others, (7) initiative, (8) feedback, (9) &quot;Why Google?&quot; as opener or closer.</div>
</div>
<div class="card" style="margin-top:20px">
<h3>What sources are <em>not</em> looking for (checklist)</h3>
<p style="font-size:14px">Same ideas as Section 8, reinforced from the question-bank research: blaming others, taking all credit, claiming you never fail, generic claims without examples, all-&quot;we&quot; answers, badmouthing employers, no growth/learning, robotic delivery.</p>
</div>
"""

ONE_DAY = """
<div class="card" id="sec-11-plan">
<h3>One-day prep plan (compressed)</h3>
<ol style="font-size:14px;line-height:1.75">
<li><strong>Block 1 (90 min):</strong> Read Sections 1&ndash;3. Write one-line triggers for each STAR part of your 8 stories (S/T/A/R).</li>
<li><strong>Block 2 (2&ndash;3 hr):</strong> For the five highest-frequency topics (conflict, failure, leadership w/o authority, XFN, ambiguity), say each story out loud once, ending with <em>learning</em> and <em>what you&rsquo;d do differently</em>.</li>
<li><strong>Block 3 (60 min):</strong> Skim Section 9 categories; for each category, name which of your 8 stories you would use first. Fill gaps, don&rsquo;t write new life stories&mdash;repurpose the closest example.</li>
<li><strong>Block 4 (45 min):</strong> &quot;Why Google?&quot; + favorite product + mission in your words (Section 6). Rehearse 2&ndash;3 reflective/abstract answers by anchoring in a real past project.</li>
<li><strong>Block 5 (30 min):</strong> Re-read red flags (Section 8) and the anti-pattern checklist. Sleep &gt; cramming new anecdotes.</li>
</ol>
<p style="font-size:13px;color:var(--text-secondary);margin-top:12px">The question list (Section 9) was built from InterviewKickstart, FinalRoundAI, Interviewing.io, MockQuestions, InterviewBit, GeeksforGeeks, and similar public prep pages. Paywalled sources (e.g. Glassdoor, Blind) were not used.</p>
</div>
"""

NEW_SECTIONS = f"""
<!-- SECTION 9: Real questions -->
<div class="section-anchor" id="sec-9"></div>
<div class="section">
<div class="section-header"><div class="section-num">9</div><h2>Real question bank (96)</h2></div>
<div class="card">
<p style="font-size:14px;line-height:1.7">Candidate-reported and prep-site questions, deduplicated. Use this to <strong>drill coverage</strong>, not to memorize wording. For each line, you should have a &quot;first choice&quot; story (from your 8) or a honest pivot (&quot;closest experience&quot;). Categories collapse&mdash;click to expand.</p>
</div>
<div class="rq-summary">
<div class="rq-summary-stat"><span class="rq-big">96</span><span class="rq-label">Questions</span></div>
<div class="rq-summary-stat"><span class="rq-big">11</span><span class="rq-label">Categories</span></div>
<div class="rq-summary-sources"><b>How to use in one day:</b> After your STAR stories are solid, skim by category. Mark 20&ndash;30 lines that would be hardest for you&mdash;those are your warm-up list.</div>
</div>
{build_question_bank_html()}
</div>

<!-- SECTION 10: Patterns -->
<div class="section-anchor" id="sec-10"></div>
<div class="section">
<div class="section-header"><div class="section-num">10</div><h2>Patterns &amp; anti-patterns</h2></div>
{PATTERNS_AND_ANTIPAT}
</div>

<!-- SECTION 11: One day -->
<div class="section-anchor" id="sec-11"></div>
<div class="section">
<div class="section-header"><div class="section-num">11</div><h2>One-day plan &amp; sources</h2></div>
{ONE_DAY}
</div>
"""


def main() -> None:
    text = SRC.read_text(encoding="utf-8")
    if '<span>One-Day Guide for Aman Jain</span>' in text:
        text = text.replace(
            "<span>One-Day Guide for Aman Jain</span>",
            "<span>Framework + 96-question bank &mdash; 1 day</span>",
        )
    # Title
    text = text.replace(
        "<title>Googleyness & Leadership - One-Day Prep Guide</title>",
        "<title>Googleyness & Leadership - Combined One-Day Prep</title>",
    )
    # Insert new sections after section 8's closing </div> (the one that closes .section for sec-8)
    needle = (
        '<div class="redflag-card"><p><strong>Not showing growth.</strong> Every story should end with a learning. If you tell the same story from 2022 with no evolution in your thinking, it suggests stagnation. Show how past experiences shaped your current approach.</p></div>\n</div>\n\n</div>\n</div>\n</main>'
    )
    if needle not in text:
        raise SystemExit("Anchor not found — 18_googleyness_prep.html may have changed")
    # Single </div> closes #contentArea (main = top-bar + content-area only).
    insert = (
        '<div class="redflag-card"><p><strong>Not showing growth.</strong> Every story should end with a learning. If you tell the same story from 2022 with no evolution in your thinking, it suggests stagnation. Show how past experiences shaped your current approach.</p></div>\n</div>\n\n'
        + NEW_SECTIONS
        + "</div>\n</main>"
    )
    text = text.replace(needle, insert)

    old_sections = """const SECTIONS = [
  { id: "sec-1", num: "1", title: "What Is the Googleyness Round?" },
  { id: "sec-2", num: "2", title: "Evaluation Signals" },
  { id: "sec-3", num: "3", title: "The STAR Method" },
  { id: "sec-4", num: "4", title: "Your Stories (8 STAR)" },
  { id: "sec-5", num: "5", title: "Quick Answers (15 Q's)" },
  { id: "sec-6", num: "6", title: "Why Google?" },
  { id: "sec-7", num: "7", title: "Day-Of Tips" },
  { id: "sec-8", num: "8", title: "Red Flags to Avoid" }
];"""

    new_sections = """const SECTIONS = [
  { id: "sec-1", num: "1", title: "What Is the Googleyness Round?" },
  { id: "sec-2", num: "2", title: "Evaluation Signals" },
  { id: "sec-3", num: "3", title: "The STAR Method" },
  { id: "sec-4", num: "4", title: "Your Stories (8 STAR)" },
  { id: "sec-5", num: "5", title: "Quick Answers (15 Q's)" },
  { id: "sec-6", num: "6", title: "Why Google?" },
  { id: "sec-7", num: "7", title: "Day-Of Tips" },
  { id: "sec-8", num: "8", title: "Red Flags to Avoid" },
  { id: "sec-9", num: "9", title: "Real Question Bank (96)" },
  { id: "sec-10", num: "10", title: "Patterns & Anti-Patterns" },
  { id: "sec-11", num: "11", title: "One-Day Plan & Sources" }
];"""

    if old_sections not in text:
        raise SystemExit("SECTIONS block not found")
    text = text.replace(old_sections, new_sections)

    # Increase qa-body for long answers; add rq-cat click handlers after qa block
    text = text.replace(
        ".qa-item.open .qa-body{max-height:400px;padding:0 20px 16px}",
        ".qa-item.open .qa-body{max-height:2000px;padding:0 20px 16px}",
    )

    old_js = """// Collapsible QA items
document.querySelectorAll(".qa-header").forEach(header => {
  header.addEventListener("click", () => {
    const item = header.parentElement;
    item.classList.toggle("open");
  });
});
"""

    new_js = """// Collapsible QA items
document.querySelectorAll(".qa-header").forEach(header => {
  header.addEventListener("click", () => {
    const item = header.parentElement;
    item.classList.toggle("open");
  });
});

// Collapsible real-question categories
document.querySelectorAll(".rq-cat-header").forEach(header => {
  header.addEventListener("click", () => {
    const cat = header.parentElement;
    if (cat) cat.classList.toggle("open");
  });
});
"""

    if old_js not in text:
        raise SystemExit("QA JS block not found")
    text = text.replace(old_js, new_js)

    OUT.write_text(text, encoding="utf-8")
    print("Wrote", OUT)


if __name__ == "__main__":
    main()

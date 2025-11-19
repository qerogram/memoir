# Product Roadmap (Reality-Adjusted)

Roadmap organized by validation gates, minimizing upfront complexity. Tags: `Must` (essential), `Lift` (value add after validation), `Defer` (later scale), `Ops` (internal tooling).

## Phase 0 – Pilot (Weeks 0–2)
- [ ] Cohort Pilot (10 users) using Notion + Slack/Kakao (`Must`)
- [ ] Weekly Reflection Template (3 prompts + rubric) (`Must`)
- [ ] Manual Comment Quality Tagging Sheet (`Ops`)
- [ ] Deposit Hypothesis Interview Script (`Ops`)
- [ ] Metrics Logging (Google Sheet: reflection_submitted, comment_posted, missed_deadline) (`Ops`)

Exit Criteria: ≥55% reflection submission, ≥2 meaningful comments per reflection, qualitative fairness feedback ≥70%.

## Phase 1 – MVP (Weeks 3–12)
- [ ] Auth (Firebase phone) + Minimal Profile (name, role, goals) (`Must`)
- [ ] Cohort Assignment (manual backend tool) (`Ops`)
- [ ] Weekly Reflection CRUD (rich text minimal: bold, italic, lists) (`Must`)
- [ ] Simple Commenting (flat, edit/delete) (`Must`)
- [ ] Push Reminders (weekly reflection + deadline nudge) (`Must`)
- [ ] Basic Feed (chronological, read state) (`Must`)
- [ ] Admin Panel v0 (reflection status, manual penalties spreadsheet export) (`Ops`)
- [ ] Subscription Billing (single monthly payment via Toss) (`Must`)

Deferrals: Deposit automation, meetups scheduling UI, directory search.

Exit Criteria: Reflection completion ≥60%, crash rate <2%, manual penalty process accurate ≥95%.

## Phase 2 – Accountability & Structure (Months 4–5)
- [ ] Deposit & Penalty Automation (refund calculation, missed reflection detection) (`Must`)
- [ ] Cohort Dashboard (submission matrix, comment counts) (`Lift`)
- [ ] Enhanced Profiles (interests, industry tags) (`Lift`)
- [ ] Meetup Scheduling (3 events per cycle, RSVP basic) (`Lift`)
- [ ] Event Reminder Push + Attendance Recording (`Lift`)
- [ ] Feed Filters (by member, unread) (`Lift`)

Exit Criteria: Completion uplift ≥5 pts post-deposit, renewal intent survey ≥40%.

## Phase 3 – Social Layer (Months 6–8)
- [ ] Coffee Chat Matching (simple filter algorithm, manual review) (`Lift`)
- [ ] Comment Quality Indicators (progress toward min comments) (`Lift`)
- [ ] Clubs Beta (join/leave, simple feed) (`Lift`)
- [ ] Notification Preferences UI (`Lift`)
- [ ] Operational Analytics (cohort stats dashboard) (`Ops`)

Defer: Real-time WebSockets (polling acceptable until load demands).

Exit Criteria: Coffee chat participation ≥20%, NPS ≥35, renewal ≥45%.

## Phase 4 – Scale & Reliability (Months 9–10)
- [ ] Performance Hardening (caching, image optimization) (`Must`)
- [ ] Automated Penalty Appeals / Overrides (`Ops`)
- [ ] Achievement Badges (attendance, thoughtful commenter) (`Lift`)
- [ ] Test Coverage 60–70% business logic (`Must`)
- [ ] Monitoring Stack (Crashlytics + Sentry full instrumentation) (`Must`)

Exit Criteria: Crash <1%, P95 feed load <1.5s, support response <12h.

## Phase 5 – Advanced Insights (Months 11–12)
- [ ] AI Reflection Summaries & Theme Tagging (opt-in) (`Lift`)
- [ ] Growth Analytics Dashboard (streaks, topic diversity) (`Lift`)
- [ ] Offline Write & Sync Conflict Resolution (`Lift`)

Defer Further: Video reflections, real-time sockets, multiple payment gateways until ≥3,000 reflections & >500 active members.

## Global Deferrals / Guardrails
- Real-time comments (Socket.io) only after polling latency issue or concurrent usage spike.
- KG Inicis/PortOne integration only if alternative payment method demand >15% inquiries.
- AI mentor matching only post reliable taxonomy & success metrics.

## Risks & Mitigations (Embedded)
- Legal (Deposit): Early manual oversight + clear TOS (Phase 1 deliverable before automation).
- Scalability: Compose + pagination from MVP; Redis only when feed query P95 >2s.
- Data Privacy: Opt-in for AI features; anonymization job before analytics export.

## Tracking & Review
- Weekly Ops Review: retention, reflection completion delta, penalty disputes.
- Phase Exit Reports: learnings, pivot decisions, next-phase scope revalidation.

(Original list superseded by phased plan.)

#!/usr/bin/env python3
"""
dependency-check-report.json → owasp-summary.txt + owasp-summary.html
Exit 1 if CRITICAL/HIGH found (based on CVSS_FAIL_THRESHOLD)
"""

import json, glob, sys, os

CVSS_FAIL_THRESHOLD = float(os.environ.get("CVSS_FAIL_THRESHOLD", "7"))

# ── load report ───────────────────────────────────────────────
reports = glob.glob("**/dependency-check-report.json", recursive=True)
if not reports:
    print("⚠️  No OWASP report found — scan may have failed")
    with open("owasp-summary.txt", "w") as f:
        f.write("STATUS=error\nCOUNT=0\nCRITICAL=0\nHIGH=0\nMEDIUM=0\nROWS\n")
    sys.exit(0)

with open(reports[0]) as f:
    data = json.load(f)

critical, high, medium = [], [], []

for dep in data.get("dependencies", []):
    vulns = dep.get("vulnerabilities", [])
    if not vulns:
        continue
    name = dep.get("fileName", "unknown")

    pkgs = dep.get("packages", [])
    current_version = ""
    if pkgs and isinstance(pkgs, list):
        pkg_id = pkgs[0].get("id", "")
        if "@" in pkg_id:
            current_version = pkg_id.split("@")[-1]

    for v in vulns:
        cvss_v3 = v.get("cvssv3") or {}
        cvss_v2 = v.get("cvssv2") or {}
        score   = cvss_v3.get("baseScore") or cvss_v2.get("score") or 0
        try:
            score = float(score)
        except (TypeError, ValueError):
            score = 0.0

        cve  = v.get("name", "N/A")
        desc = (v.get("description") or "")[:250]

        # Pull fixed version from CVE data (same as GitHub Actions — no hardcoded map)
        fixed_versions = []
        for sw in (v.get("vulnerableSoftware") or []):
            sw_obj = sw.get("software") or {}
            vs_end_excl = sw_obj.get("versionEndExcluding")
            if vs_end_excl:
                fixed_versions.append(vs_end_excl)

        fix_suggestion = ""
        if fixed_versions:
            try:
                fix_suggestion = max(
                    fixed_versions,
                    key=lambda s: [int(x) if x.isdigit() else x
                                   for x in s.replace("-", ".").split(".")]
                )
            except Exception:
                fix_suggestion = fixed_versions[0]

        entry = dict(name=name, cve=cve, score=score, desc=desc,
                     current=current_version, fix=fix_suggestion)

        if   score >= 9: critical.append(entry)
        elif score >= 7: high.append(entry)
        elif score >= 4: medium.append(entry)

total = len(critical) + len(high) + len(medium)

# ── console log (same as GitHub Actions) ─────────────────────
print(f"\n{'='*65}")
print(f"  OWASP DEPENDENCY CHECK RESULTS")
print(f"  Total CVEs : {total}")
print(f"  CRITICAL   : {len(critical)}  (9.0+)")
print(f"  HIGH       : {len(high)}  (7.0-8.9)")
print(f"  MEDIUM     : {len(medium)}  (4.0-6.9)")
print(f"  Fail threshold: {CVSS_FAIL_THRESHOLD}")
print(f"{'='*65}")

summary_rows = []
html_rows    = ""

for label, icon, items in [
    ("CRITICAL", "🔴", critical),
    ("HIGH",     "🟠", high),
    ("MEDIUM",   "🟡", medium),
]:
    if not items:
        continue
    print(f"\n{'─'*65}")
    print(f"  {icon} {label}")
    print(f"{'─'*65}")
    for e in items:
        print(f"\n  Score [{e['score']}]  {e['name']}")
        print(f"  CVE     : {e['cve']}")
        if e['current']:
            print(f"  Current : {e['current']}")
        print(f"  Issue   : {e['desc'][:200]}")
        if e['fix']:
            print(f"  Fix     : Upgrade to >= {e['fix']}")
            fix_cell      = f"Upgrade to >= {e['fix']}"
            fix_cell_html = f"Upgrade to <b>&gt;= {e['fix']}</b>"
        else:
            print(f"  Fix     : Check vendor advisory")
            fix_cell      = "Check vendor advisory"
            fix_cell_html = "Check vendor advisory"

        # for owasp-summary.txt
        summary_rows.append(
            f"| {icon} {label} | `{e['name']}` | {e['cve']} | {e['score']} | {fix_cell} |"
        )

        # for HTML
        sev_colors = {"CRITICAL": "#7b2d8b", "HIGH": "#e53e3e", "MEDIUM": "#dd6b20"}
        c = sev_colors.get(label, "#718096")
        html_rows += f"""<tr>
            <td><span style="background:{c};color:#fff;padding:2px 8px;border-radius:4px;font-size:12px;font-weight:700">{icon} {label}</span></td>
            <td style="font-weight:600">{e['score']}</td>
            <td style="font-family:monospace;font-size:12px">{e['name']}</td>
            <td style="font-family:monospace;font-size:12px;color:#3182ce">{e['cve']}</td>
            <td style="font-size:12px;color:#4a5568">{e['desc'][:150]}</td>
            <td style="font-size:12px">{fix_cell_html}</td>
        </tr>"""

# ── threshold-based status (same as GitHub Actions) ──────────
if   CVSS_FAIL_THRESHOLD >= 9: failing = critical
elif CVSS_FAIL_THRESHOLD >= 7: failing = critical + high
elif CVSS_FAIL_THRESHOLD >= 4: failing = critical + high + medium
else:                           failing = critical + high + medium

status = "fail" if failing else "pass"

# ── write owasp-summary.txt (same as GitHub Actions) ─────────
with open("owasp-summary.txt", "w") as f:
    f.write(f"STATUS={status}\n")
    f.write(f"COUNT={total}\n")
    f.write(f"CRITICAL={len(critical)}\n")
    f.write(f"HIGH={len(high)}\n")
    f.write(f"MEDIUM={len(medium)}\n")
    f.write("ROWS\n")
    for row in summary_rows:
        f.write(row + "\n")

# ── write owasp-summary.html ──────────────────────────────────
banner_color = "#276749" if not failing else "#9b2335"
status_text  = "✅ PASSED" if not failing else "❌ FAILED"

table = f"""
<table>
  <thead><tr>
    <th>Severity</th><th>Score</th><th>Dependency</th>
    <th>CVE</th><th>Description</th><th>Fix</th>
  </tr></thead>
  <tbody>{html_rows}</tbody>
</table>""" if total > 0 else '<p style="color:#48bb78;font-size:16px">🎉 No vulnerabilities found!</p>'

html = f"""<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8">
<title>OWASP CVE Report</title>
<style>
  body   {{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;background:#f7fafc;margin:0;padding:24px;color:#2d3748}}
  .banner{{background:{banner_color};color:#fff;padding:16px 24px;border-radius:8px;margin-bottom:24px}}
  .banner h1{{margin:0 0 4px;font-size:22px}} .banner p{{margin:0;opacity:.85;font-size:14px}}
  .cards{{display:flex;gap:16px;margin-bottom:24px;flex-wrap:wrap}}
  .card{{background:#fff;border-radius:8px;padding:16px 24px;box-shadow:0 1px 3px rgba(0,0,0,.1);min-width:120px;text-align:center}}
  .card .num{{font-size:32px;font-weight:700}} .card .lbl{{font-size:12px;color:#718096;margin-top:4px}}
  .crit{{color:#7b2d8b}} .hi{{color:#e53e3e}} .med{{color:#dd6b20}}
  table{{width:100%;border-collapse:collapse;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 1px 3px rgba(0,0,0,.1)}}
  th{{background:#edf2f7;padding:10px 12px;text-align:left;font-size:12px;text-transform:uppercase;color:#4a5568}}
  td{{padding:10px 12px;border-bottom:1px solid #edf2f7;vertical-align:top}}
  tr:last-child td{{border-bottom:none}} tr:hover td{{background:#f7fafc}}
  .note{{margin-top:16px;padding:12px 16px;background:#ebf8ff;border-left:4px solid #3182ce;border-radius:4px;font-size:13px;color:#2c5282}}
</style></head><body>
<div class="banner"><h1>🛡️ OWASP CVE Scan — {status_text}</h1><p>Dependency vulnerability scan (threshold: CVSS >= {CVSS_FAIL_THRESHOLD})</p></div>
<div class="cards">
  <div class="card"><div class="num">{total}</div><div class="lbl">Total CVEs</div></div>
  <div class="card"><div class="num crit">{len(critical)}</div><div class="lbl">Critical</div></div>
  <div class="card"><div class="num hi">{len(high)}</div><div class="lbl">High</div></div>
  <div class="card"><div class="num med">{len(medium)}</div><div class="lbl">Medium</div></div>
</div>
{table}
{"<div class='note'>💡 <b>Note:</b> Suggested fix version is extracted from CVE data (<code>versionEndExcluding</code>) — verify against vendor advisory before applying.</div>" if total > 0 else ""}
</body></html>"""

with open("owasp-summary.html", "w") as f:
    f.write(html)

print(f"\n[INFO] owasp-summary.txt + owasp-summary.html written")

if failing:
    print(f"\n❌ {len(failing)} vulnerabilities at or above threshold {CVSS_FAIL_THRESHOLD} — fix required!")
    sys.exit(1)
elif medium:
    print(f"\n⚠️  {len(medium)} MEDIUM — review recommended")
else:
    print("\n✅ No HIGH/CRITICAL vulnerabilities!")

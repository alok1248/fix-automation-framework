pipeline {
    agent any

    tools {
        jdk   'jdk-21'
        maven 'maven-3.9'
    }

    environment {
        // ── Optional NVD API key (OWASP runs without it, just slower) ──
        // If the Jenkins credential 'nvd-api-key' does not exist, set
        // NVD_API_KEY to an empty string so the pipeline still proceeds.
        NVD_CACHE_DIR       = "/var/lib/jenkins/.owasp-nvd-cache"
        SEMGREP_VENV        = "/var/lib/jenkins/.semgrep-venv"
        PIP_HOME            = "/var/lib/jenkins/.local"
        CVSS_FAIL_THRESHOLD = "7"
    }

    // ── Trigger: build automatically on every SCM push ────────────────────────
    triggers {
        pollSCM('H/2 * * * *')   // poll every 2 min as fallback; prefer webhook
    }

    stages {

        // ── Stage 1: Checkout ─────────────────────────────────────────────────
        stage('Checkout') {
            steps {
                checkout scm
                echo "✅ Branch  : ${env.BRANCH_NAME}"
                echo "✅ PR      : ${env.CHANGE_ID ?: 'N/A'}"
                echo "✅ PR Title: ${env.CHANGE_TITLE ?: 'N/A'}"
                echo "✅ Commit  : ${env.GIT_COMMIT}"
                sh '''
                    echo "=== Tool versions ==="
                    java -version
                    mvn --version
                    python3 --version
                    echo "JAVA_HOME=$JAVA_HOME"
                    echo "M2_HOME=$M2_HOME"
                '''
            }
        }

        // ── Stage 2: Build + Unit Tests ───────────────────────────────────────
        stage('Build') {
            steps {
                sh '''
                    set -e
                    echo "========================================"
                    echo " STAGE: Maven Build + Unit Tests"
                    echo "========================================"

                    # Tests run; failures are REPORTED but do NOT fail the build.
                    # Remove -Dmaven.test.failure.ignore=true to make them blocking.
                    mvn clean package -Dmaven.test.failure.ignore=true -q
                    echo "✅ Build complete!"
                '''
            }
            post {
                always  { junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true }
                success { echo '✅ Build PASSED' }
                failure { echo '❌ Build FAILED' }
            }
        }

        // ── Stage 3: Semgrep SAST ─────────────────────────────────────────────
        stage('Semgrep SAST') {
            steps {
                sh '''
                    set +e
                    echo "========================================"
                    echo "  STAGE: Semgrep SAST Scan"
                    echo "========================================"

                    export PATH=/var/lib/jenkins/.local/bin:/var/lib/jenkins/.semgrep-venv/bin:$PATH

                    if ! command -v semgrep >/dev/null 2>&1; then
                        echo "Installing python3-pip via apt-get..."
                        sudo apt-get install -y python3-pip -q
                        echo "Installing semgrep via pip..."
                        python3 -m pip install semgrep --quiet --break-system-packages || \
                        python3 -m pip install semgrep --quiet || true
                    fi

                    if command -v semgrep >/dev/null 2>&1; then
                        semgrep --config=auto \
                                --json \
                                --output=semgrep-report.json \
                                --no-rewrite-rule-ids \
                                . || true
                    else
                        echo "[WARN] semgrep not found — skipping scan"
                    fi

                    set -e
                '''

                catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                    sh 'python3 semgrep_parse.py'
                }
            }

            post {
                always {
                    archiveArtifacts artifacts: 'semgrep-report.json,semgrep-summary.txt',
                                     allowEmptyArchive: true
                    publishHTML([
                        allowMissing         : true,
                        alwaysLinkToLastBuild: true,
                        keepAll              : true,
                        reportDir            : '.',
                        reportFiles          : 'semgrep-summary.html',
                        reportName           : '🔒 Semgrep Report'
                    ])
                }
                success { echo '✅ Semgrep PASSED' }
                failure { echo '❌ Semgrep FAILED — fix errors before merging' }
            }
        }

        // ── Stage 4: OWASP CVE Scan ───────────────────────────────────────────
        // NVD_API_KEY is optional — the scan runs without it (offline/cached data).
        stage('OWASP CVE Scan') {
            steps {
                script {
                    // Gracefully load NVD_API_KEY only when the credential exists
                    def nvdKeyArg = ''
                    try {
                        withCredentials([string(credentialsId: 'nvd-api-key', variable: 'NVD_API_KEY_VAL')]) {
                            nvdKeyArg = "-Dnvd.api.key=${NVD_API_KEY_VAL}"
                        }
                    } catch (hudson.AbortException ignored) {
                        echo '[WARN] Jenkins credential nvd-api-key not found — running OWASP without NVD API key (slower updates)'
                    }

                    sh """
                        set -e
                        echo "========================================"
                        echo " STAGE: OWASP Dependency CVE Scan"
                        echo "========================================"

                        mkdir -p "\${NVD_CACHE_DIR}"

                        SUPPRESSION_ARG=""
                        if [ -f "dependency-check-suppressions.xml" ]; then
                            SUPPRESSION_ARG="-DsuppressionFiles=dependency-check-suppressions.xml"
                            echo "✅ Using suppression file"
                        fi

                        mvn org.owasp:dependency-check-maven:check \\
                            -DfailBuildOnCVSS=0 \\
                            -Dformats=HTML,JSON \\
                            ${nvdKeyArg} \\
                            -DdataDirectory="\${NVD_CACHE_DIR}" \\
                            -DretireJsAnalyzerEnabled=false \\
                            -DnodeAnalyzerEnabled=false \\
                            -DassemblyAnalyzerEnabled=false \\
                            -DossindexAnalyzerEnabled=false \\
                            \${SUPPRESSION_ARG} || true

                        echo "✓ OWASP scan complete."
                    """
                }

                catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                    sh "CVSS_FAIL_THRESHOLD=${env.CVSS_FAIL_THRESHOLD} python3 owasp_parse.py"
                }
            }

            post {
                always {
                    archiveArtifacts artifacts: '**/dependency-check-report.html,**/dependency-check-report.json,owasp-summary.txt',
                                     allowEmptyArchive: true
                    publishHTML([
                        allowMissing         : true,
                        alwaysLinkToLastBuild: true,
                        keepAll              : true,
                        reportDir            : '.',
                        reportFiles          : 'owasp-summary.html',
                        reportName           : '🛡️ OWASP Report'
                    ])
                }
                success { echo '✅ OWASP PASSED' }
                failure { echo '❌ OWASP FAILED' }
            }
        }
    }

    // ── Security Summary (mirrors sosuv-workflow-api) ─────────────────────────
    post {
        always {
            script {
                def sg    = [status: "unknown", count: "0", errors: "0", warnings: "0", rows: []]
                def owasp = [status: "unknown", count: "0", critical: "0", high: "0", medium: "0", rows: []]

                try {
                    def inRows = false
                    readFile('semgrep-summary.txt').trim().split('\n').each { line ->
                        if (line == "ROWS") { inRows = true; return }
                        if (inRows) { if (line.trim()) sg.rows << line; return }
                        if (line.startsWith('STATUS='))   sg.status   = line.split('=',2)[1]
                        if (line.startsWith('COUNT='))    sg.count    = line.split('=',2)[1]
                        if (line.startsWith('ERRORS='))   sg.errors   = line.split('=',2)[1]
                        if (line.startsWith('WARNINGS=')) sg.warnings = line.split('=',2)[1]
                    }
                } catch (e) { sg.status = "unknown" }

                try {
                    def inRows = false
                    readFile('owasp-summary.txt').trim().split('\n').each { line ->
                        if (line == "ROWS") { inRows = true; return }
                        if (inRows) { if (line.trim()) owasp.rows << line; return }
                        if (line.startsWith('STATUS='))   owasp.status   = line.split('=',2)[1]
                        if (line.startsWith('COUNT='))    owasp.count    = line.split('=',2)[1]
                        if (line.startsWith('CRITICAL=')) owasp.critical = line.split('=',2)[1]
                        if (line.startsWith('HIGH='))     owasp.high     = line.split('=',2)[1]
                        if (line.startsWith('MEDIUM='))   owasp.medium   = line.split('=',2)[1]
                    }
                } catch (e) { owasp.status = "unknown" }

                def sgIcon    = sg.status    == "fail" ? "❌" : sg.status    == "pass" ? "✅" : "⚠️"
                def owaspIcon = owasp.status == "fail" ? "❌" : owasp.status == "pass" ? "✅" : "⚠️"
                def overallFail = (sg.status == "fail" || owasp.status == "fail")

                currentBuild.description = "Sem:${sg.status.toUpperCase()} | OWASP:${owasp.status.toUpperCase()} | C:${owasp.critical} H:${owasp.high} M:${owasp.medium}"

                echo """
╔══════════════════════════════════════════════════════════════╗
║   🔐 Security Scan Results — Build #${env.BUILD_NUMBER}
╠══════════════════════════════════════════════════════════════╣
║
║   Scan               Status     Findings
║   ─────────────────  ─────────  ──────────────────────────
║   Semgrep SAST       ${sgIcon} ${sg.status.toUpperCase().padRight(6)}   ${sg.errors} errors, ${sg.warnings} warnings
║   OWASP CVE Check    ${owaspIcon} ${owasp.status.toUpperCase().padRight(6)}   CRITICAL:${owasp.critical}  HIGH:${owasp.high}  MEDIUM:${owasp.medium}
║
╚══════════════════════════════════════════════════════════════╝"""

                def sgRowsHtml = sg.rows.collect { row ->
                    def cells = row.split('\\|').collect { it.trim() }.findAll { it }
                    "<tr>${cells.collect { "<td style='padding:8px 12px;border-bottom:1px solid #edf2f7;font-size:13px'>${it}</td>" }.join('')}</tr>"
                }.join('')

                def owaspRowsHtml = owasp.rows.collect { row ->
                    def cells = row.split('\\|').collect { it.trim() }.findAll { it }
                    "<tr>${cells.collect { "<td style='padding:8px 12px;border-bottom:1px solid #edf2f7;font-size:13px'>${it}</td>" }.join('')}</tr>"
                }.join('')

                def overallBanner = overallFail
                    ? "<div style='background:#9b2335;color:#fff;padding:16px 24px;border-radius:8px;margin-bottom:24px'><h2 style='margin:0'>❌ Security issues found — Fix before merging</h2></div>"
                    : "<div style='background:#276749;color:#fff;padding:16px 24px;border-radius:8px;margin-bottom:24px'><h2 style='margin:0'>✅ All security checks passed — Safe to merge</h2></div>"

                def sgSection = (sg.status == "fail" && sg.rows) ? """
                    <h3 style='color:#c53030'>🔴 Code Issues (Semgrep)</h3>
                    <table style='width:100%;border-collapse:collapse;background:#fff;border-radius:8px;box-shadow:0 1px 3px rgba(0,0,0,.1)'>
                      <thead><tr style='background:#edf2f7'>
                        <th style='padding:10px 12px;text-align:left;font-size:12px;text-transform:uppercase;color:#4a5568'>Severity</th>
                        <th style='padding:10px 12px;text-align:left;font-size:12px;text-transform:uppercase;color:#4a5568'>File</th>
                        <th style='padding:10px 12px;text-align:left;font-size:12px;text-transform:uppercase;color:#4a5568'>Rule</th>
                        <th style='padding:10px 12px;text-align:left;font-size:12px;text-transform:uppercase;color:#4a5568'>CWE</th>
                        <th style='padding:10px 12px;text-align:left;font-size:12px;text-transform:uppercase;color:#4a5568'>Fix Hint</th>
                      </tr></thead>
                      <tbody>${sgRowsHtml}</tbody>
                    </table><br>
                    <p style='color:#718096;font-size:13px'>📋 Full details: Click <b>🔒 Semgrep Report</b> in the sidebar</p>
                """ : ""

                def owaspSection = (owasp.status == "fail" && owasp.rows) ? """
                    <h3 style='color:#c53030'>🔴 Dependency Issues (OWASP)</h3>
                    <table style='width:100%;border-collapse:collapse;background:#fff;border-radius:8px;box-shadow:0 1px 3px rgba(0,0,0,.1)'>
                      <thead><tr style='background:#edf2f7'>
                        <th style='padding:10px 12px;text-align:left;font-size:12px;text-transform:uppercase;color:#4a5568'>Severity</th>
                        <th style='padding:10px 12px;text-align:left;font-size:12px;text-transform:uppercase;color:#4a5568'>Library</th>
                        <th style='padding:10px 12px;text-align:left;font-size:12px;text-transform:uppercase;color:#4a5568'>CVE</th>
                        <th style='padding:10px 12px;text-align:left;font-size:12px;text-transform:uppercase;color:#4a5568'>Score</th>
                        <th style='padding:10px 12px;text-align:left;font-size:12px;text-transform:uppercase;color:#4a5568'>Suggested Fix</th>
                      </tr></thead>
                      <tbody>${owaspRowsHtml}</tbody>
                    </table><br>
                    <p style='color:#718096;font-size:13px'>📋 Full details: Click <b>🛡️ OWASP Report</b> in the sidebar</p>
                """ : ""

                def summaryHtml = """<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>
<title>Security Summary — Build #${env.BUILD_NUMBER}</title>
<style>
  body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;background:#f7fafc;margin:0;padding:24px;color:#2d3748}
  table.summary{width:100%;border-collapse:collapse;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 1px 3px rgba(0,0,0,.1);margin-bottom:24px}
  table.summary th{background:#edf2f7;padding:10px 16px;text-align:left;font-size:13px;color:#4a5568}
  table.summary td{padding:12px 16px;border-bottom:1px solid #edf2f7;font-size:14px}
  table.summary tr:last-child td{border-bottom:none}
</style></head><body>
<h1 style='margin-bottom:8px'>🔐 Security Scan Results — fix-automation-framework</h1>
<p style='color:#718096;margin-bottom:20px'>Build #${env.BUILD_NUMBER} — ${new Date().format('dd MMM yyyy, HH:mm')}</p>

<table class='summary'>
  <thead><tr><th>Scan</th><th>Status</th><th>Findings</th></tr></thead>
  <tbody>
    <tr>
      <td>Semgrep SAST</td>
      <td>${sgIcon} ${sg.status.toUpperCase()}</td>
      <td>${sg.errors} errors, ${sg.warnings} warnings</td>
    </tr>
    <tr>
      <td>OWASP CVE Check</td>
      <td>${owaspIcon} ${owasp.status.toUpperCase()}</td>
      <td>CRITICAL: ${owasp.critical}&nbsp;&nbsp;HIGH: ${owasp.high}&nbsp;&nbsp;MEDIUM: ${owasp.medium}</td>
    </tr>
  </tbody>
</table>

${overallBanner}
${sgSection}
${owaspSection}

</body></html>"""

                writeFile file: 'security-summary.html', text: summaryHtml

                publishHTML([
                    allowMissing         : true,
                    alwaysLinkToLastBuild: true,
                    keepAll              : true,
                    reportDir            : '.',
                    reportFiles          : 'security-summary.html',
                    reportName           : '🔐 Security Summary'
                ])
            }
        }
        success { echo '🎉 Pipeline PASSED' }
        failure { echo '❌ Pipeline FAILED' }
        cleanup { cleanWs() }
    }
}

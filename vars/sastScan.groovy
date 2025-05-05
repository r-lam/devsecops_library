def call(Map config = [:]) {
  echo "03 - INICIA ESCANEO ESTÁTICO DEL CÓDIGO CON SEMGREP"

  def repoUrl = config.repo ?: env.PROJECT
  def appname = repoUrl.tokenize('/').last().replace('.git', '')
  def buildid = env.BUILD_ID
  def commitcode = env.GIT_COMMIT
  def timestamp = new Date().format("yyyyMMdd-HHmm")
  def scanversion = "${appname}-${commitcode}-${timestamp}"
  def outputFile = "sast-${scanversion}.json"

  sh """
    echo "Descargando y ejecutando análisis Semgrep..."

    mkdir -p semgrep-rules
    curl -sSL https://semgrep.dev/c/p/java -o semgrep-rules/java.yml
    curl -sSL https://semgrep.dev/c/p/security-audit -o semgrep-rules/security-audit.yml
    curl -sSL https://semgrep.dev/c/p/owasp-top-ten -o semgrep-rules/owasp-top-ten.yml

    echo "Ejecutando análisis Semgrep..."

    semgrep scan ${env.PROJECT_ROOT} \\
      --config semgrep-rules/java.yml \\
      --config semgrep-rules/security-audit.yml \\
      --config semgrep-rules/owasp-top-ten.yml \\
      --metrics=off \\
      --timeout-threshold 10000 \\
      --json --output ${outputFile}
    
    echo "${outputFile}"

  """

  archiveArtifacts artifacts: "${outputFile}", allowEmptyArchive: true
}

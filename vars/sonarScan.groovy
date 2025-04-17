def call(Map config = [:]) {
  def repoUrl = config.repo ?: env.PROJECT
  def appname = repoUrl.tokenize('/').last().replace('.git', '')
  def buildid = env.BUILD_ID
  def commitcode = env.GIT_COMMIT
  def timestamp = new Date().format("yyyyMMdd-HHmm")
  def scanversion = "${appname}-${commitcode}-${timestamp}"
  def scannerHome = tool 'sonar-scanner'

  echo "Ejecutando análisis SonarQube para: ${appname}"

  withSonarQubeEnv('sonarqube') {
    withCredentials([string(credentialsId: '31aa0c79-c552-4d6c-9c22-fd5e646438ad', variable: 'SONAR_TOKEN')]) {
      sh """
        ${scannerHome}/bin/sonar-scanner \
          -Dsonar.projectKey=${appname} \
          -Dsonar.projectName=${appname} \
          -Dsonar.projectVersion=${scanversion} \
          -Dsonar.sources=${env.PROJECT_ROOT} \
          -Dsonar.token=${SONAR_TOKEN} \
          -Dsonar.host.url=${env.SONARQUBE_URL}
      """
    }

    echo "Esperando validación del Quality Gate..."

    // Validación del resultado del análisis
    timeout(time: 2, unit: 'MINUTES') {
      waitForQualityGate abortPipeline: true
    }
  }

  echo "Análisis SonarQube aprobado: ${scanversion}"
}

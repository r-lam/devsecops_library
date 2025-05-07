def call(Map config = [:]) {

    def tokenCredentialId = config.get('credentialId', 'd9bf1f60-98c3-4da5-9328-f92e17558541')
    def defectDojoUrl = config.get('defectDojoUrl', 'http://defectdojo-django.defectdojo.svc')

    def repoUrl = env.PROJECT
    def appname = repoUrl.tokenize('/').last().replace('.git', '')
    def buildid = env.BUILD_ID
    def commitcode = env.GIT_COMMIT
    def timestamp = new Date().format("yyyyMMdd-HHmm")
    def scanversion = "${appname}-${commitcode}-${timestamp}"
    def outputFile = "sast-${scanversion}.json"

    echo "NOMBRE APLICACION: ${appname}"

    withCredentials([string(credentialsId: tokenCredentialId, variable: 'DEFECTDOJO_TOKEN')]) {
        sh """
            file=\$(ls sast-*.json | head -n 1)
            echo "Archivo detectado: \$file"
            scan_date=\$(date +%Y-%m-%d)

            curl -v -i -X POST "${defectDojoUrl}/api/v2/import-scan/" \\
              -H "Authorization: Token \$DEFECTDOJO_TOKEN" \\
              -F "scan_type=Semgrep JSON Report" \\
              -F "product_type_name=Research and Development" \\
              -F "product_name=${appname}" \\
              -F "engagement_name=Semgrep Scan \$(date +%Y-%m-%d)" \\
              -F "auto_create_context=true" \\
              -F "file=@\$file" \\
              -F "active=true" \\
              -F "verified=true" \\
              -F "scan_date=\$scan_date" \\
              -F "minimum_severity=Low" \\
              -F "deduplication_on_engagement=true"
        """
    }
}

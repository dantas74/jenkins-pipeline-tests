def steps

def defaultStagesPath = "${env.WORKSPACE}/infra/jenkins/stages"

switch(gitBranch) {
    case 'main':
        steps = load "${defaultStagesPath}/Prod.groovy"
    break

    case 'develop':
        steps = load "${defaultStagesPath}/Dev.groovy"
    break

    case ~/^release\/.*$/:
        steps = load "${defaultStagesPath}/Release.groovy"
    break

    default:
        throw new Exception('Cannot do CI/CD on this branch')
    break
}

return steps
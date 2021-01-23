!/usr/bin/groovy
package​ ​ com.workshop.stages
import​ ​ com.workshop.Pipeline
import​ ​ com.workshop.Config
def​ ​ details​ ( ​ Pipeline​ ​ p ​ ) {
println​ ( ​ "================​ \u​ 001b[44mDetails Of
Jobs​ \u​ 001b[0m==============="​ )
println​ ( ​ " ​ \u​ 001b[36mRepository Name : ​ \u​ 001b[0m​ ${​ p.repository_name​ } ​ " ​ )
println​ ( ​ " ​ \u​ 001b[36mBranch Name : ​ \u​ 001b[0m​ ${​ p.branch_name​ } ​ " ​ )
println​ ( ​ " ​ \u​ 001b[36mApplication Port : ​ \u​ 001b[0m​ ${​ p.app_port​ } ​ " ​ )
println​ ( ​ ""​ )
println​ ( ​ " ​ \u​ 001b[36mTesting this PR : ​ \u​ 001b[0m#​ ${​ p.pr_num​ } ​ -
https://github.com/​ ${​ p.git_user​ } ​ / ​ ${​ p.repository_name​ } ​ /pull/​ ${​ p.pr_num​ } ​ " ​ )
println​ ( ​ " ​ \u​ 001b[36mMerging to branch : ​ \u​ 001b[0m​ ${​ p.branch_name​ } ​ " ​ )
}
def​ ​ validation​ ( ​ Pipeline​ ​ p ​ ) {
if​ (!p.repository_name) {
"Repository name can't be empty"
error(​ "ERROR101 - MISSING REPOSITORY_NAME"​ )
}
if​ (!p.branch_name) {
"Branch name can't be empty"
error(​ "ERROR101 - MISSING BRANCH_NAME"​ )
}
if​ (!p.app_port) {
"Application port can't be empty"
error(​ "ERROR101 - MISSING APP_PORT"​ )
}
if​ (!p.pr_num) {
"PR Number can't be empty"
error(​ "ERROR101 - MISSING PR_NUM"​ )
}
}
def​ ​ checkoutBuildTest​ ( ​ Pipeline​ ​ p ​ ) {
c = ​ new​ ​ Config​ ()
withCredentials([usernamePassword(credentialsId: ​ 'github-personal'​ ,
passwordVariable: ​ 'git_token'​ , usernameVariable: ​ 'git_username'​ )]) {
println​ ​ "============​ \u​ 001b[44mCommencing PR
Checkout​ \u​ 001b[0m============"
println​ ​ " ​ \u​ 001b[36mChecking out from :
\u​ 001b[0mpull/​ ${​ p.pr_num​ } ​ /head:pr/​ ${​ p.pr_num​ } ​ ..."
git branch: ​ " ​ ${​ p.branch_name​ } ​ " ​ , url:
"https://github.com/​ ${​ p.git_user​ } ​ / ​ ${​ p.repository_name​ } ​ .git"
sh ​ "git config --global user.name '​ ${​ git_username​ } ​ '"
sh ​ "git config --global user.email '​ ${​ git_username​ } ​ @example.com'"
sh ​ "git branch -D pr/​ ${​ p.pr_num​ } ​ &> /dev/null || true"
sh ​ "git fetch origin pull/​ ${​ p.pr_num​ } ​ /head:pr/​ ${​ p.pr_num​ } ​ "
sh ​ "git merge --no-ff pr/​ ${​ p.pr_num​ } ​ "
}
docker.withTool(​ " ​ ${​ c.default_docker_jenkins_tool​ } ​ " ​ ) {
def​ golangImage = docker.image(​ " ​ ${​ c.default_golang_base_image​ } ​ " ​ )
golangImage.inside(​ "-u 0"​ ) {
build = sh returnStatus: ​ true​ , script: ​ "go build -v"
if​ (build == ​ 0 ​ ) {
println​ ​ " ​ \u​ 001b[36mBuilding ​ \u​ 001b[33m. ​ \u​ 001b[32mDONE
!!!​ \u​ 001b[0m"
} ​ else​ {
println​ ​ " ​ \u​ 001b[36mBuilding ​ \u​ 001b[33m. ​ \u​ 001b[31mFAILED
!!!​ \u​ 001b[0m"
error(​ "Build test failed"​ )
}
}
golangImage.inside(​ "-u 0"​ ) {
test = sh returnStatus: ​ true​ , script: ​ "go test ./..."
if​ (build == ​ 0 ​ ) {
println​ ​ " ​ \u​ 001b[36mTesting ​ \u​ 001b[33m. ​ \u​ 001b[32mDONE
!!!​ \u​ 001b[0m"
} ​ else​ {
println​ ​ " ​ \u​ 001b[36mTesting ​ \u​ 001b[33m. ​ \u​ 001b[31mFAILED
!!!​ \u​ 001b[0m"
error(​ "Unit test failed"​ )
}
}
}
}
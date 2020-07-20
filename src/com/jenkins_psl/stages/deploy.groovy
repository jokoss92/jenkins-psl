package com.jenkins_psl.stages

import com.jenkins_psl.Config
import com.jenkins_psl.Pipeline

def deploy(Pipeline p) {
    c = new Config()

    withEnv(["PATH+DOCKER=${p.dockerTool}/bin"]){
        println "Take Down previous Deployment"
        def response = sh script: "docker rm -f \$(docker ps -aq -f 'name=${p.repository_name}') &> /dev/null", returnStatus: true
        if ("${response}" == "0") {
            println("Successfuly removing old container")
        } else {
            println("Old version removed")
        }
    }

    docker.withTool("${c.default_docker_jenkins_tool}") {
        def image = docker.build("${p.git_user}/${p.repository_name}:build-$BUILD_NUMBER")
        println image
        image.run("--name ${p.repository_name}-$BUILD_NUMBER -p ${p.app_port}:${p.app_port}")
    }
}
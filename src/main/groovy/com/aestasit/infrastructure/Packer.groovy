package com.aestasit.infrastructure

import groovy.json.JsonSlurper
import com.aestasit.infrastructure.builder.*
import com.aestasit.infrastructure.provisioner.*
import com.aestasit.infrastructure.model.Box

class Packer {

  void startInstance(String zone, String ami) {
    //def ec2 = new EC2Client(zone)

  }

  void runCommand() {


  }

  void createImage() {

  }

  void processConfiguration(File f) {

    def reader = new FileReader(f)
    def config = new JsonSlurper().parse(reader)

    validate(config)
    def builder = getBuilder(config)
    def shinyBox = builder.startInstance()
    def provisioner = getProvisioner(config, shinyBox)

  }

  void validate(conf) {
    println '> validation not implemented'
    // TODO
  }

  def getProvisioner(builderConfig, Box aBox) {

    def provisioner
    provisioner = new PuppetProvisioner()

  }

  def getBuilder(builderConfig) {
    def builder
    switch ( builderConfig.type ) {

      case 'amazon-ebs':
        builder = new AmiBuilder(builderConfig)
        break
      default:
        builder = new UnsupportedBuilder()
    }

      builder
  }

}


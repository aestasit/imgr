package com.aestasit.infrastructure
import groovy.json.JsonSlurper
import com.aestasit.cloud.aws.*
import com.aestasit.infrastructure.builder.*
class Packer {

  void startInstance(String zone, String ami) {
    def ec2 = new EC2Client(zone)

  }

  void runCommand() {


  }

  void createImage() {

  }

  void processConfiguration(File f) {

    def reader = new FileReader(f)
    def config = new JsonSlurper().parse(reader)

    validate(config)
    def builder = getBuilder(config.type)
    builder.config(config)


  }

  void validate(conf) {
    println '> validation not implemented'
    // TODO
  }

  def getBuilder(String builderType) {
    def builder
    switch ( builderType ) {

      case 'amazon-ebs':
        builder = new AmiBuilder()
        break
      default:
        builder = new UnsupportedBuilder()
    }

      builder
  }

}


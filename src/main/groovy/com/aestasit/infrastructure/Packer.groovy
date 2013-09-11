package com.aestasit.infrastructure

import groovy.json.JsonSlurper
import com.aestasit.infrastructure.builder.*
import com.aestasit.infrastructure.provisioner.*
import com.aestasit.infrastructure.model.Box
import groovy.util.logging.Slf4j

@Slf4j
class Packer {

  void processConfiguration(InputStream f) {

    def config = new JsonSlurper().parse(f.newReader())

    validate(config)

    def builder = getBuilder(config)
    def shinyBox = builder.startInstance()
    log.info("> machine is started with id ${shinyBox.id}")

    if (hasProvisioners (config)) {
      def provisioner = getProvisioner(config, shinyBox)
      provisioner.provision()
    }
    log.info("> provisioning completed, creating image now...")
    builder.createImage(shinyBox, 'name', 'description')

  }

  private boolean hasProvisioners(config) {
    config.provisioners != null
  }

  void validate(conf) {
    println '> validation not implemented'
    println "Number of builders: ${conf.builders.size}"
    println "Number of provisioners: ${conf.provisioners?.size}"
    conf.builders.each {
      println "Type of builder: ${it.type}"
    }
    conf.provisioners.each {
      println "Type of provisioner: ${it.type}"
    }

  }

  def getProvisioner(builderConfig, Box aBox) {

    def provisioner
    switch ( builderConfig.provisioners[0].type ) {
      case 'puppet-masterless':
        provisioner = new PuppetProvisioner(aBox, builderConfig.provisioners[0])
        break
      default:
        provisioner = new UnsupportedProvisioner()
    }

  }

  def getBuilder(builderConfig) {
    def builder
    switch ( builderConfig.builders[0].type ) {
      case 'amazon-ebs':
        builder = new AmiBuilder(builderConfig.builders[0])
        break
      default:
        builder = new UnsupportedBuilder()
    }
  }

  static main(args) {

  }

}


/*
 * Copyright (C) 2011-2013 Aestas/IT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aestasit.infrastructure

import com.aestasit.infrastructure.builder.AmiBuilder
import com.aestasit.infrastructure.builder.UnsupportedBuilder
import com.aestasit.infrastructure.model.Box
import com.aestasit.infrastructure.provisioner.PuppetProvisioner
import com.aestasit.infrastructure.provisioner.ShellProvisioner
import com.aestasit.infrastructure.provisioner.UnsupportedProvisioner
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

@Slf4j
class Packer {

  void processConfiguration(InputStream f) {

    def config = new JsonSlurper().parse(f.newReader())
    validate(config)

    def skipImage = config.skip_image=='true'?true:false


    def builder = getBuilder(config)
    def shinyBox = builder.startInstance()
    log.info("machine is started with id ${shinyBox.id}")

    if (hasProvisioners(config)) {
      config.provisioners.eachWithIndex {p, id ->
        def provisioner = getProvisioner(id, config, shinyBox)
        provisioner.provision()
      }

    }
    log.info("provisioning completed")

    if (!skipImage) {
      log.info("creating image now...")
      builder.createImage(shinyBox, 'name', 'description')
    }
  }


  private boolean hasProvisioners(config) {
    config.provisioners != null
  }

  void validate(conf) {
    // TODO implement validation
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

  def getProvisioner(index, builderConfig, Box aBox) {

    def provisioner
    switch (builderConfig.provisioners[index].type) {
      case 'puppet-masterless':
        provisioner = new PuppetProvisioner(aBox, builderConfig.provisioners[index])
        break
      case 'shell':
        provisioner = new ShellProvisioner(aBox, builderConfig.provisioners[index] )
        break
      default:

        provisioner = new UnsupportedProvisioner()
    }

  }

  def getBuilder(builderConfig) {
    def builder
    switch (builderConfig.builders[0].type) {
      case 'amazon-ebs':
        builder = new AmiBuilder(builderConfig.builders[0])
        break
      default:
        builder = new UnsupportedBuilder()
    }
  }

  static main(args) {
    def packer = new Packer()
    log.debug "> processing ${args[0]}"
    if (args.length == 1) {
      def f = new File(args[0])
      if (f.exists() && f.isFile()) {
        packer.processConfiguration(f.newInputStream())
      }
    }
  }

}


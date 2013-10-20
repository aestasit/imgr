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

package com.aestasit.infrastructure.imgr

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

import com.aestasit.infrastructure.imgr.builder.AmiBuilder
import com.aestasit.infrastructure.imgr.builder.Builder
import com.aestasit.infrastructure.imgr.builder.UnsupportedBuilder
import com.aestasit.infrastructure.imgr.model.Box
import com.aestasit.infrastructure.imgr.provisioner.FileProvisioner
import com.aestasit.infrastructure.imgr.provisioner.Provisioner
import com.aestasit.infrastructure.imgr.provisioner.PuppetProvisioner
import com.aestasit.infrastructure.imgr.provisioner.ShellProvisioner
import com.aestasit.infrastructure.imgr.provisioner.UnsupportedProvisioner

@Slf4j
class Imgr {

  static main(args) {
    def imgr = new Imgr()
    if (args.length == 1) {
      imgr.processConfiguration(args[0])
    } else {
      println "Usage: imgr <configuration.json>"
      System.exit(1)
    }
  }

  void processConfiguration(String fileName) {
    def file = new File(fileName)
    log.debug "> Opening $fileName"
    if (file.exists() && file.isFile()) {
      file.withReader { reader ->
        processConfiguration(reader)
      }
    }
  }
  
  void processConfiguration(Reader reader) {

    def config = new JsonSlurper().parse(reader)
    validate(config)

    def skipImage = config.skip_image == 'true'

    Builder builder = getBuilder(config)
    def box = builder.initiate()
    log.info("> Box with id ${box.id} is initiated.")

    if (hasProvisioners(config)) {
      config.provisioners.eachWithIndex { p, id ->
        Provisioner provisioner = getProvisioner(id, config, box)
        provisioner.provision()
      }
    }
    log.info("> Provisioning completed!")

    if (!skipImage) {
      def imageId = builder.createImage()
      log.info("> Image with id ${imageId} is created.")
    }
    builder.cleanup()
    
  }

  private boolean hasProvisioners(config) {
    config.'provisioners' != null
  }

  void validate(config) {
    // TODO implement validation
    log.info '> Validation not implemented'
    log.info "Number of builders: ${config.builders.size}"
    log.info "Number of provisioners: ${config.provisioners?.size}"
    config.builders.each {
      this.log.info "Type of builder: ${it.type}"
    }
    config.provisioners.each {
      this.log.info  "Type of provisioner: ${it.type}"
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

  def getProvisioner(index, builderConfig, Box box) {
    def provisioner
    switch (builderConfig.provisioners[index].type) {
      case 'puppet-masterless':
        provisioner = new PuppetProvisioner(box, builderConfig.provisioners[index])
        break
      case 'shell':
        provisioner = new ShellProvisioner(box, builderConfig.provisioners[index])
        break
      case 'file':
        provisioner = new FileProvisioner(box, builderConfig.provisioners[index])
        break
      default:
        provisioner = new UnsupportedProvisioner()
    }
  }

}


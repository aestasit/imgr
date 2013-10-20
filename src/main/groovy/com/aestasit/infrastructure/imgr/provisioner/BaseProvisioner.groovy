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

package com.aestasit.infrastructure.imgr.provisioner

import static com.aestasit.infrastructure.imgr.model.PackageProvider.*
import groovy.text.SimpleTemplateEngine
import groovy.text.Template
import groovy.util.logging.Slf4j

import com.aestasit.infrastructure.imgr.BaseComponent
import com.aestasit.infrastructure.imgr.ImgrException
import com.aestasit.infrastructure.imgr.model.Box
import com.aestasit.infrastructure.imgr.model.PackageProvider
import com.aestasit.infrastructure.imgr.transport.SshSession

/**
 * This is base provisioner class that holds common functionality for all other provisioners.
 * 
 * @author Aestas/IT
 *
 */
@Slf4j
abstract class BaseProvisioner extends BaseComponent implements Provisioner {
  
  protected Map provisionerConfig
  protected Box box
  protected SshSession session

  BaseProvisioner(Box box, Map provisionerConfig) {
    this.box = box 
    this.provisionerConfig = provisionerConfig
  }

  void provision() {
    session = new SshSession(
      box.host, 
      provisionerConfig.user, 
      box.keyPath
    )
    doProvision()
  }
  
  abstract void doProvision()

  def boolean fileExists(String file) {
    check("${provisionerConfig.command_prefix ?: ''} test -f $file")
  }

  boolean isRedHatLinux() {
    check('cat /etc/*-release | grep Red | grep Hat')
  }

  boolean isOracleLinux() {
    check('cat /etc/issue* | grep Oracle')
  }
  
  boolean isAmazonLinux() {
    check('cat /etc/*-release | grep Amazon')
  }

  boolean isCentOS() {
    check('cat /etc/*-release | grep CentOS')
  }
  
  boolean isRedHatFamily() {
    isRedHatLinux() || isOracleLinux() || isAmazonLinux() || isCentOS()
  }

  def boolean isDebian() {
    check('test -f /etc/debian_version')
  }

  def boolean isSlackware() {
    check('test -f /etc/slackware-version')
  }

  def boolean isRubyInstalled() {
    check('ruby -v')
  }

  def boolean isRubygemsInstalled() {
    check('gem -v')
  }

  def boolean isPuppetInstalled() {
    check('puppet --version')
  }

  def boolean isBlueprintInstalled() {
    check('blueprint', 1)
  }

  def boolean isLibrarianInstalled() {
    check('librarian-puppet version')
  }

  def boolean isYumAvailable() {
    check('yum --version')
  }

  def boolean isAptAvailable() {
    check('apt-get --version')
  }

  def boolean isYumPackageInstalled(String name) {
    check("rpm -qa | grep ^$name-[0-9]")
  }

  def boolean isAptPackageInstalled(String name) {
    check("rpm -qa | grep ^$name-[0-9]")
  }

  def boolean isGemInstalled(String name) {
    check("gem list $name | grep ^$name")
  }

  def installYumPackage(String name) {
    def cmd = "${provisionerConfig.command_prefix ?: ''} yum --assumeyes install $name"
    if (isYumAvailable()) {
      if (isYumPackageInstalled(name)) {
        log.info("Package is already installed: $name")
      } else {
        log.info("Installing: $name")
        session.exec(cmd)
      }
    } else {
      throw new ImgrException("yum is not available on this system!")
    }
  }

  def installAptPackage(String name) {
    if (isAptAvailable()) {
      if (isAptPackageInstalled(name)) {
        log.info("Package is already installed: $name")
      } else {
        log.info("Installing: $name")
        session.exec("${provisionerConfig.command_prefix ?: ''} apt-get -y install $name")
      }
    } else {
      throw new ImgrException("apt-get is not available on this system!")
    }
  }

  def installGem(String name) {
    if (isRubygemsInstalled()) {
      if (isGemInstalled(name)) {
        log.info("Package is already installed: $name")
      } else {
        log.info("Installing: $name")
        session.exec("${provisionerConfig.command_prefix ?: ''} gem install $name")
      }
    } else {
      throw new ImgrException("gem is not available on this system!")
    }
  }

  def installPackages(PackageProvider provider, Collection packages) {
    packages.each { pkg ->
      switch (provider) {
        case YUM: installYumPackage(pkg); break;
        case APT: installAptPackage(pkg); break;
        case GEM: installGem(pkg); break;
        default: throw new ImgrException("Package provider is not supported!")
      }
    }
  }

  protected boolean check(String command, int exitCode = 0) {
    session.exec(
      command: command, 
      prefix: provisionerConfig.command_prefix ?: '', 
      showCommand: false, 
      showOutput: false, 
      failOnError: false
    ).exitStatus == exitCode
  }

  protected String readResourceFile(String filename) {
    InputStream stream = BaseProvisioner.class.getResourceAsStream(filename)
    if (!stream) {
      throw new ImgrException("$filename not found in resources folder")
    }
    stream.text
  }

  protected String readResourceTemplate(String filename, Map binding) {
    String templateText = readResourceFile(filename)
    SimpleTemplateEngine engine = new SimpleTemplateEngine()
    Template template = engine.createTemplate(templateText)
    template.make(binding)
  }
  
}

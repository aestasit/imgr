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

package com.aestasit.infrastructure.provisioner

import com.aestasit.infrastructure.PackerException
import groovy.util.logging.Slf4j

import static com.aestasit.infrastructure.provisioner.PackageProvider.*

@Slf4j
abstract class BaseProvisioner {

  def session

  def boolean fileExists(String file) {
    check("test -f $file")
  }

  def boolean isRedHat() {
    check('cat /etc/*-release | grep Red | grep Hat')
  }

  def boolean isAmazonLinux() {
    check('cat /etc/*-release | grep Amazon')
  }

  def boolean isCentOS() {
    check('cat /etc/*-release | grep CentOS')
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
    def cmd = "yum --assumeyes install $name"
    if (isYumAvailable()) {
      if (isYumPackageInstalled(name)) {
        log.info("Package is already installed: $name")
      } else {
        log.info("Installing: $name")
        session.exec(cmd)
      }
    } else {
      throw new PackerException("yum is not available on this system!")
    }
  }

  def installAptPackage(String name) {
    if (isAptAvailable()) {
      if (isAptPackageInstalled(name)) {
        log.info("Package is already installed: $name")
      } else {
        log.info("Installing: $name")
        session.exec("sudo apt-get -y install $name")
      }
    } else {
      throw new PackerException("apt-get is not available on this system!")
    }
  }

  def installGem(String name) {
    if (isRubygemsInstalled()) {
      if (isGemInstalled(name)) {
        log.info("Package is already installed: $name")
      } else {
        log.info("Installing: $name")
        session.exec("gem install $name")
      }
    } else {
      throw new PackerException("gem is not available on this system!")
    }
  }

  def installPackages(PackageProvider provider, Collection packages) {
    packages.each { pkg ->
      switch (provider) {
        case YUM: installYumPackage(pkg); break;
        case APT: installAptPackage(pkg); break;
        case GEM: installGem(pkg); break;
        default: throw new PackerException("Package provider is not supported!")
      }
    }
  }

  protected boolean check(String command, int exitCode = 0) {
    session.exec(command: command, showCommand: false, showOutput: false, failOnError: false).exitStatus == exitCode
  }

  abstract void provision()

  protected String readResourceFile(String filename) {

    def stream = BaseProvisioner.class.getResourceAsStream(filename)
    if (stream) return stream.text
    throw new PackerException("$filename not found in resources folder")

  }

}

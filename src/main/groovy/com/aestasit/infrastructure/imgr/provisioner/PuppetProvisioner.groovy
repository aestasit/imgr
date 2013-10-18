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

import static com.aestasit.infrastructure.imgr.provisioner.PackageProvider.APT
import static com.aestasit.infrastructure.imgr.provisioner.PackageProvider.YUM

import java.util.Map;

import com.aestasit.infrastructure.imgr.ImgrException
import com.aestasit.infrastructure.imgr.model.Box

import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j


/**
 * Puppet-based provisioner that is capable of installing Puppet and applying custom manifests.
 *
 * @author Aestas/IT
 *
 */
@Slf4j
class PuppetProvisioner extends BaseProvisioner {

  PuppetProvisioner(Box box, Map provisionerConfig) {
    super(box, provisionerConfig)
  }

  PuppetProvisioner(SshSession session, Map provisionerConfig) {
    super(session, provisionerConfig)
  }

  @Override
  void provision() {
    log.info '> installing Puppet: updating repository on remote machine...'
    updateRepos()
    log.info '> installing Puppet...'
    install()
    log.info '> applying Puppet configuration...'
    applyManifest(provisionerConfig)
  }

  /**
   * Update package repository settings.
   * 
   */
  private void updateRepos() {
    if (isYumAvailable()) {
      if (isRedHat()) {
        puppetRepo()
        epelRepo()
      } else if (isAmazonLinux()) {
        puppetRepo()
        centosRepo()
      } else if (isCentOS()) {
        puppetRepo()
      } else {
        log.info('Unknown operating system. Assuming Yum is already setup!')
      }
    } else {
      throw new ImgrException('This operating system does not support Yum!')
    }
  }

  /**
   * Install Puppet.
   * 
   */
  private void install() {

    if (isAmazonLinux()) {
      // NOTE: Workaround for missing virt-what package in Amazon Linux repositories
      if (!isYumPackageInstalled('virt-what')) {
        session.exec("yum --enablerepo=centos-base --assumeyes install virt-what")
      }
    }

    if (isRedHat() || isAmazonLinux() || isCentOS()) {
      log.debug 'installing puppet for RH,AL,COS'
      installPackages(YUM, [
        'libselinux',
        'libselinux-ruby',
        'facter',
        'puppet',
      ])
    } else if (isDebian()) {
      log.debug 'installing puppet for DB'

      installPackages(APT, [
        'facter',
        'puppet',
      ])
    } else {
      throw new ImgrException('Unknown operating system. Puppet will not be installed!')
    }

    // Create empty hiera.yaml file to avoid warning upon puppet apply.
    session.exec("touch /etc/puppet/hiera.yaml")

  }

  /**
   * Apply Puppet manifest.
   * 
   */
  private void applyManifest() {

    def manifestFile = new File(provisionerConfig.manifest_file).name
    log.debug "Manifest file is $manifestFile"

    log.info '> Uploading new Puppet manifest'
    session.scp(provisionerConfig.manifest_file, provisionerConfig.staging_directory)

    // Apply default manifest.
    log.info '> Applying Puppet configuration'
    session.exec "sudo /usr/bin/puppet apply -v ${provisionerConfig.staging_directory}/${manifestFile}"

  }

  private puppetRepo() {
    session.uploadTxtAsRoot('/etc/yum.repos.d/puppet.repo', readResourceFile('/repos/puppet.repo'))
  }

  private epelRepo() {
    session.uploadTxtAsRoot('/etc/yum.repos.d/epel.repo', readResourceFile('/repos/epel.repo'))
  }

  private centosRepo() {
    session.uploadTxtAsRoot('/etc/yum.repos.d/centos.repo', readResourceFile('/repos/centos.repo'))
  }

}
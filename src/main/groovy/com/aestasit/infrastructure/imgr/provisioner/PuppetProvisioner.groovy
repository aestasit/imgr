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

import static com.aestasit.infrastructure.imgr.model.PackageProvider.APT
import static com.aestasit.infrastructure.imgr.model.PackageProvider.YUM
import groovy.text.SimpleTemplateEngine
import groovy.util.logging.Slf4j

import com.aestasit.infrastructure.imgr.ImgrException
import com.aestasit.infrastructure.imgr.model.Box
import com.aestasit.infrastructure.imgr.transport.SshSession


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

  @Override
  void doProvision() {
    updateRepos()
    installPuppet()
    applyManifest()
  }

  /**
   * Update package repository settings.
   * 
   */
  private void updateRepos() {
    if (isYumAvailable()) {      
      if (isRedHatLinux() || isOracleLinux()) {
        epelYumRepo()
        puppetYumRepo()        
      } else if (isAmazonLinux()) {
        puppetYumRepo()
      } else if (isCentOS()) {
        puppetYumRepo()
      } else {
        log.warn '> Unknown operating system. Assuming Yum is already setup!'
      }
    } else if (isAptAvailable()) {
      if (isDebian()) {
        puppetAptRepo()
      } else {
        log.warn '> Unknown operating system. Assuming Apt is already setup!'
      }
    } else {
      throw new ImgrException('This operating system does not support Yum/Apt!')
    }
  }

  /**
   * Install Puppet.
   * 
   */
  private void installPuppet() {
    if (!isPuppetInstalled()) {

      log.info '> Installing Puppet...'

      if (isRedHatLinux() || isAmazonLinux() || isCentOS() || isOracleLinux()) {
        log.debug '> Installing Puppet for RedHat-like OS'
        installPackages(YUM, additionalYumPackages + [
          "facter${provisionerConfig.facter_version ? '-' + provisionerConfig.facter_version : ''}",
          "puppet${provisionerConfig.puppet_version ? '-' + provisionerConfig.puppet_version : ''}",
        ])
      } else if (isDebian()) {
        log.debug '> Installing Puppet for Debian-like OS'
        installPackages(APT, additionalAptPackages + [
          "facter${provisionerConfig.facter_version ? '=' + provisionerConfig.facter_version : ''}",
          "puppet${provisionerConfig.puppet_version ? '=' + provisionerConfig.puppet_version : ''}",
        ])
      } else {
        throw new ImgrException('Unknown operating system. Puppet will not be installed!')
      }

      // Create empty hiera.yaml file to avoid warning upon puppet apply.
      session.exec("touch /etc/puppet/hiera.yaml")

    }
  }
  
  private getAdditionalYumPackages() {
    def packages = [ 'libselinux', 'libselinux-ruby' ] 
    if (provisionerConfig.containsKey('yum_packages')) {
      if (provisionerConfig.'yum_packages' instanceof Collection) {
        packages = provisionerConfig.'yum_packages'.collect { it.toString() }
      }
    }
    packages    
  }
  
  private getAdditionalAptPackages() {
    def packages = [ ]
    if (provisionerConfig.containsKey('apt_packages')) {
      if (provisionerConfig.'apt_packages' instanceof Collection) {
        packages = provisionerConfig.'apt_packages'.collect { it.toString() }
      }
    }
    packages
  }

  /**
   * Apply Puppet manifest.
   * 
   */
  private void applyManifest() {

    log.info '> Applying Puppet configuration...'

    def manifestFile = new File(provisionerConfig.manifest_file).name
    log.debug "Manifest file is $manifestFile"

    log.info '> Uploading new Puppet manifest'
    session.scp provisionerConfig.manifest_file, provisionerConfig.staging_directory

    // Calculate facts.
    def environmentSetup = ''
    if (provisionerConfig.facts) {
      if (provisionerConfig.facts instanceof Map) {
        provisionerConfig.facts.each { key, value ->
          environmentSetup += "FACTER_${key.toUpperCase()}=${value} "
        }
      }
    }

    // Calculate module path.
    def moduleSetup = ''
    if (provisionerConfig.module_paths) {
      if (provisionerConfig.module_paths instanceof Collection) {
        moduleSetup = '--modulepath=' + provisionerConfig.module_paths.join(':')
      }
    }

    // Apply default manifest.
    log.info '> Applying Puppet configuration'
    session.exec "${environmentSetup}${provisionerConfig.command_prefix ?: ''} /usr/bin/puppet apply -v ${moduleSetup} ${provisionerConfig.staging_directory}/${manifestFile}"

  }

  private void puppetYumRepo() {
    if (!fileExists('/etc/yum.repos.d/puppet.repo')) {
      log.info '> Adding Puppet Labs repositories...'
      session.setText(
        '/etc/yum.repos.d/puppet.repo',
        readResourceTemplate(
          '/repos/puppet.yum.repo',
          [
            basePuppetRepoUrl: provisionerConfig.base_puppet_yum_repo_url ?: 'http://yum.puppetlabs.com/el/6x/products/$basearch/',
            basePuppetDepsRepoUrl: provisionerConfig.base_puppet_deps_yum_repo_url ?: 'http://yum.puppetlabs.com/el/6x/dependencies/$basearch/'
          ]
        )
      )
    }
  }

  private void puppetAptRepo() {
    if (!fileExists('/etc/apt/sources.list.d/puppet.list')) {
      log.info '> Adding Puppet Labs repositories...'
      session.setText(
        '/etc/apt/sources.list.d/puppet.list',
        readResourceTemplate(
          '/repos/puppet.apt.repo',
          [
            basePuppetRepoUrl: provisionerConfig.base_puppet_apt_repo_url ?: 'http://apt.puppetlabs.com',
          ]
        )
      )      
    }
    if (!fileExists('/etc/apt/trusted.gpg.d/puppetlabs-keyring.gpg')) {
      session.setText(
        '/etc/apt/trusted.gpg.d/puppetlabs-keyring.gpg',
        readResourceFile("/keys/puppetlabs-keyring.gpg")
      )
    }
  }
    
  private epelYumRepo() {
    if (!fileExists('/etc/yum.repos.d/epel.repo')) {
      log.info '> Adding EPEL repositories...'
      session.setText(
        '/etc/yum.repos.d/epel.yum.repo',
        readResourceTemplate(
          '/repos/epel.repo',
          [
            baseEpelRepoUrl: provisionerConfig.base_epel_yum_repo_url ?: 'http://dl.fedoraproject.org/pub/epel/6/$basearch/'
          ]
        )
      )
    }
  }
}
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

  PuppetProvisioner(SshSession session, Map provisionerConfig) {
    super(session, provisionerConfig)
  }

  @Override
  void provision() {    
    updateRepos()    
    installPuppet()    
    applyManifest()
  }

  /**
   * Update package repository settings.
   * 
   */
  private void updateRepos() {
    log.info '> Installing Puppet: updating repository on remote machine...'
    if (isYumAvailable()) {
      if (isRedHat()) {
        puppetRepo()
        epelRepo()
      } else if (isAmazonLinux()) {
        puppetRepo()
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
  private void installPuppet() {
    
    log.info '> Installing Puppet...'
    
    if (isRedHat() || isAmazonLinux() || isCentOS()) {
      log.debug '> Installing Puppet for RedHat-like OS'
      installPackages(YUM, [
        'libselinux',
        'libselinux-ruby',
        "facter${provisionerConfig.facter_version ? '-' + provisionerConfig.facter_version : ''}",
        "puppet${provisionerConfig.puppet_version ? '-' + provisionerConfig.puppet_version : ''}",
      ])
    } else if (isDebian()) {
      log.debug '> Installing Puppet for Debian-like OS'
      installPackages(APT, [
        "facter${provisionerConfig.facter_version ? '-' + provisionerConfig.facter_version : ''}",
        "puppet${provisionerConfig.puppet_version ? '-' + provisionerConfig.puppet_version : ''}",
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

    log.info '> Applying Puppet configuration...'
    
    def manifestFile = new File(provisionerConfig.manifest_file).name
    log.debug "Manifest file is $manifestFile"

    log.info '> Uploading new Puppet manifest'
    session.scp provisionerConfig.manifest_file, provisionerConfig.staging_directory

    // Apply default manifest.
    log.info '> Applying Puppet configuration'
    session.exec "${provisionerConfig.command_prefix ?: ''} /usr/bin/puppet apply -v ${provisionerConfig.staging_directory}/${manifestFile}"

  }

  private puppetRepo() {    
    session.uploadTxtAsRoot(
      '/etc/yum.repos.d/puppet.repo', 
      readResourceTemplate(
        '/repos/puppet.repo', 
        basePuppetRepoUrl: provisionerConfig.base_puppet_repo_url ?: 'http://yum.puppetlabs.com/el/6/products/$basearch/',
        basePuppetDepsRepoUrl: provisionerConfig.base_puppet_deps_repo_url ?: 'http://yum.puppetlabs.com/el/6/products/$basearch/'
        )
      )
  }
  
  private epelRepo() {
    session.uploadTxtAsRoot(
      '/etc/yum.repos.d/epel.repo', 
      readResourceTemplate(
        '/repos/epel.repo', 
        baseEpelRepoUrl: provisionerConfig.base_epel_repo_url ?: 'http://dl.fedoraproject.org/pub/epel/6/$basearch/'
        )
      )
  }

}
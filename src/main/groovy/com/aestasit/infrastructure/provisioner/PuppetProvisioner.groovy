package com.aestasit.infrastructure.provisioner

import com.aestasit.infrastructure.PackerException
import com.aestasit.infrastructure.model.Box
import groovy.util.logging.Slf4j

import static com.aestasit.infrastructure.provisioner.PackageProvider.APT
import static com.aestasit.infrastructure.provisioner.PackageProvider.YUM

@Slf4j
class PuppetProvisioner extends BaseProvisioner {

  def provisionerConf

  def PuppetProvisioner(Box aBox, config) {
    // Create a running SSH session
    session = new SshSession(aBox.host, aBox.user, aBox.keyPath)
    provisionerConf = config
  }

  @Override
  void provision() {

    log.info '> installing Puppet: updating repository on remote machine...'
    updateRepos()
    log.info '> installing Puppet...'
    install()
    log.info '> applying Puppet configuration...'
    applyManifest(provisionerConf)

  }


  private updateRepos() {
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
      throw new PackerException('This operating system does not support Yum!')
    }
  }

  /**
   * Install
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
      throw new PackerException('Unknown operating system. Puppet will not be installed!')
    }
    // Create empty hiera.yaml file to avoid warning upon puppet apply.
    session.exec("touch /etc/puppet/hiera.yaml")

  }


  private void applyManifest(provisionerConf) {

    def manifestFile = new File(provisionerConf.manifest_file).name
    log.debug "manifest file is $manifestFile"
    // TODO supporting only one manifest now...
    log.info '> Uploading new Puppet manifest'
    session.scp(provisionerConf.manifest_file,
        provisionerConf.staging_directory)

    // Apply default manifest.
    log.info '> Applying Puppet configuration'
    session.exec "sudo /usr/bin/puppet apply -v ${provisionerConf.staging_directory}/${manifestFile}"

  }


  private puppetRepo() {
    session.uploadTxtAsRoot('/etc/yum.repos.d/puppet.repo',
        readResourceFile('/repos/puppet.repo'))
  }

  private epelRepo() {
    session.uploadTxtAsRoot('/etc/yum.repos.d/epel.repo',
        readResourceFile('/repos/epel.repo'))
  }

  private centosRepo() {
    session.uploadTxtAsRoot('/etc/yum.repos.d/centos.repo',
        readResourceFile('/repos/centos.repo'))
  }

}
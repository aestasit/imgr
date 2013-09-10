package com.aestasit.infrastructure.provisioner

import com.aestasit.infrastructure.model.*
import com.aestasit.infrastructure.*
import static com.aestasit.infrastructure.provisioner.PackageProvider.*
import groovy.util.logging.Slf4j

@Slf4j
class PuppetProvisioner extends BaseProvisioner {



  def PuppetProvisioner(Box aBox) {

    session = new SshSession(aBox.host, aBox.user, aBox.keyPath)

  }


  void provision() {

    updateRepos() // w
    install() // w

    //applyManifest()

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
        log.info('Unknown operting system. Assuming Yum is already setup!')
      }
    } else {
      throw new PackerException('This operating system does not support Yum!')
    }


  }

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
    // Set template directory to known location.
    session.exec(command: 'augtool -b -s set /files/etc/puppet/puppet.conf/main/templatedir /etc/puppet/templates', failOnError: false)

  }



  private void applyManifest() {


  }


  private puppetRepo() {
    session.uploadFileAsRoot('/etc/yum.repos.d/puppet.repo', readResourceFile('/repos/puppet.repo'))
  }

  private epelRepo() {
    session.uploadFileAsRoot('/etc/yum.repos.d/epel.repo',readResourceFile('/repos/epel.repo'))
  }

  private centosRepo() {
    session.uploadFileAsRoot('/etc/yum.repos.d/centos.repo', readResourceFile('/repos/centos.repo'))
  }

}
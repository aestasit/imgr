package com.aestasit.infrastructure.provisioner

import com.aestasit.infrastructure.model.Box
import groovy.util.logging.Slf4j


@Slf4j
class ShellProvisioner extends BaseProvisioner {

  def provisionerConf
  def DEFAULT_UPLOAD_DIR = '/tmp'
  def DEFAULT_SCRIPT_NAME = 'script.sh'

  def ShellProvisioner(Box aBox, java.util.HashMap config) {
    // Create a running SSH session
    session = new SshSession(aBox.host, aBox.user, aBox.keyPath)
    provisionerConf = config
  }

  @Override
  void provision() {

    provisionerConf.each {

      switch (it.key) {
        case 'inline':
          upload(DEFAULT_UPLOAD_DIR, createFile(it.value))
          executeScript()
          break
        case 'script':
          upload(DEFAULT_UPLOAD_DIR, new File(it.value)) // TODO shall I rename the file to a random value?
          executeScript(new File(it.value).name)
          break
      }

    }
  }

  def executeScript(String script = DEFAULT_SCRIPT_NAME) {

    session.exec("chmod +x $DEFAULT_UPLOAD_DIR/$script; $DEFAULT_UPLOAD_DIR/$script")

  }


  def upload(String defaultDir, File file) {

    session.scp(file.path, defaultDir)

  }

  File createFile(List lines) {

    lines.add(0, '#!/bin/bash')
    lines.add(1, '')

    def tmp = System.getProperty('java.io.tmpdir')
    def f = new File("$tmp/$DEFAULT_SCRIPT_NAME")
    f.withWriter { out ->
      lines.each {
        out.println it
      }
    }
    f
  }
}
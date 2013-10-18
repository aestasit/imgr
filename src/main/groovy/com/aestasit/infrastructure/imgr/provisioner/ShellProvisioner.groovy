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

import com.aestasit.infrastructure.imgr.model.Box
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
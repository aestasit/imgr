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

import groovy.util.logging.Slf4j

import com.aestasit.infrastructure.imgr.model.Box
import com.aestasit.infrastructure.imgr.transport.SshSession

/**
 * 
 * 
 * @author Aestas/IT
 *
 */
@Slf4j
class ShellProvisioner extends BaseProvisioner {

  ShellProvisioner(Box box, Map provisionerConfig) {
    super(box, provisionerConfig)
  }

  static String DEFAULT_UPLOAD_DIR = '/tmp'
  static String DEFAULT_SCRIPT_NAME = 'script.sh'

  @Override
  void doProvision() {
    provisionerConfig.each { key, value ->
      switch (key) {
        case 'inline':
          upload(DEFAULT_UPLOAD_DIR, createFile(value))
          executeScript()
          break
        case 'script':
          upload(DEFAULT_UPLOAD_DIR, new File(value)) // TODO shall I rename the file to a random value?
          executeScript(new File(value).name)
          break
      }
    }
  }

  private upload(String defaultDir, File file) {
    session.scp(file.path, defaultDir)
  }

  private executeScript(String script = DEFAULT_SCRIPT_NAME) {
    session.exec("chmod +x $DEFAULT_UPLOAD_DIR/$script; $DEFAULT_UPLOAD_DIR/$script ${provisionerConfig.arguments ?: ''}" )
  }

  private File createFile(String content) {
    def lines = content.readLines()
    lines.add(0, '#!/bin/bash')
    lines.add(1, '')
    String tmp = System.getProperty('java.io.tmpdir')
    File file = new File("$tmp/$DEFAULT_SCRIPT_NAME")
    file.text = lines.join(LF)
    file
  }
  
}
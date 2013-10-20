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

package com.aestasit.infrastructure.imgr.transport

import groovy.util.logging.Slf4j

import com.aestasit.infrastructure.imgr.ImgrException
import com.aestasit.ssh.SshOptions
import com.aestasit.ssh.dsl.SessionDelegate
import com.aestasit.ssh.dsl.SshDslEngine
import com.aestasit.ssh.log.SysOutLogger

/**
 *
 *
 * @author Aestas/IT
 *
 */
@Slf4j
class SshSession {

  SshDslEngine engine

  SshSession(String host, String user, String keyPath) {
    def options = new SshOptions()
    options.with {
      logger            = new SysOutLogger()
      defaultHost       = host
      defaultUser       = user
      defaultKeyFile    = new File(keyPath)
      reuseConnection   = false
      trustUnknownHosts = true
      verbose           = true
    }
    engine = new SshDslEngine(options)
  }

  /**
   * Upload a file or folder to a remote folder.
   * 
   */
  def scp(String source, String target) {
    def res
    def fileFrom = new File(source)
    boolean isDir = false
    if (fileFrom.exists()) {
      isDir = fileFrom.isDirectory()
    } else {
      throw new ImgrException("File does not exist: ${source}")
    }
    engine.remoteSession {
      scp {
        if (isDir) {
          from { localDir source }
        } else {
          from { localFile source }
        }
        into { remoteDir target }
      }
    }
    res
  }

  def exec(Map commandMap) {
    def res
    engine.remoteSession {
      res = exec commandMap
    }
    res
  }

  def exec(String command) {
    def res
    engine.remoteSession {
      res = exec command
    }
    res
  }

  def setText(String remoteFilePath, String content) {
    engine.remoteSession {
      remoteFile(remoteFilePath).text = content
    }
  }

}
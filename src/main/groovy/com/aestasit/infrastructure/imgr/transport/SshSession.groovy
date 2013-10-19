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

import com.aestasit.infrastructure.imgr.ImgrException
import com.aestasit.ssh.SshOptions
import com.aestasit.ssh.dsl.SshDslEngine
import com.aestasit.ssh.log.SysOutLogger
import groovy.util.logging.Slf4j

/**
 *
 *
 *
 * @author Aestas/IT
 *
 */
@Slf4j
class SshSession {

  def engine

  SshSession(String host, String user, String pathToKey) {
    def options = new SshOptions()
    options.with {
      logger = new SysOutLogger()
      defaultHost = host
      defaultUser = user
      defaultKeyFile = new File(pathToKey)
      reuseConnection = true
      trustUnknownHosts = true
      verbose = true
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
      try {
        res = exec command
      } catch (com.aestasit.ssh.SshException se) {
        log.info "Previous command [$command] failed, let's try with sudo"
        res = exec "sudo $command"
      }
    }
    res
  }

  def uploadTxtAsRoot(String remoteFileLocation, String fileContent) {
    def res
    // TODO: Remove this
    engine.remoteSession {
      prefix("sudo") {
        res = exec "echo '$fileContent' | sudo tee $remoteFileLocation"
      }
    }
    res
  }

  def remoteFile(String _remoteFile, String content) {
    def res
    engine.remoteSession {
      res = remoteFile(_remoteFile).text = content
    }
    res
  }

}
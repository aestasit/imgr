package com.aestasit.infrastructure.provisioner

import com.aestasit.infrastructure.PackerException
import com.aestasit.ssh.SshOptions
import com.aestasit.ssh.dsl.SshDslEngine
import com.aestasit.ssh.log.SysOutLogger
import groovy.util.logging.Slf4j

@Slf4j
class SshSession {

  def engine

  SshSession(String host, String user, String pathToKey) {
    def options = new SshOptions()
    options.with {

      logger = new SysOutLogger()
      defaultHost = host
      defaultUser = user
      //defaultPassword = '123456'
      //defaultPort = aBox.port
      defaultKeyFile = new File(pathToKey)
      reuseConnection = true
      trustUnknownHosts = true

      verbose = true
    }
    engine = new SshDslEngine(options)

  }

  /**
   * upload a file or folder to a remote folder
   */
  def scp(String _from, String _to) {
    def res
    def fileFrom = new File(_from)
    boolean isDir = false
    if (fileFrom.exists()) {
      isDir = fileFrom.isDirectory()
    } else {
      throw new PackerException("File does not exist: ${_from}")
    }
    engine.remoteSession {
      scp {
        if (isDir) {
          from { localDir _from }
        } else {
          from { localFile _from }
        }
        into { remoteDir _to }
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
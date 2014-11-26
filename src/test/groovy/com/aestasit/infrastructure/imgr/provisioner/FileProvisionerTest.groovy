/*
 * Copyright (C) 2011-2014 Aestas/IT
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

import groovy.json.JsonSlurper

import org.junit.BeforeClass
import org.junit.Test

import com.aestasit.infrastructure.imgr.model.Box
import com.aestasit.ssh.mocks.MockSshServer

/**
 * Test puppet provisioner.
 * 
 * @author Aestas/IT
 *
 */
class FileProvisionerTest extends BaseTest {

  String configText = """
    {
      "provisioners": [
        {
          "type":"file",
          "user":"root",
          "source_path":"${currentDir}/src/test/resources/test.sh",
          "parameters": {
            "media_host":"127.0.0.1" 
          },
          "target_path":"/etc/puppet"
        },
      ]
    }
  """

  @Test
  void testPuppet() {
    new FileProvisioner(testBox, getProvisionerConfig(0)).provision()
  }

  def getProvisionerConfig(int index = 0) {
    new JsonSlurper().parseText(configText).provisioners[index]
  }

  @BeforeClass
  def static void mockSshCommands() {
    MockSshServer.with {
      command('^.*$') { inp, out, err, callback, env ->
        callback.onExit(0)
      }
    }
  }

  @BeforeClass
  def static void mockSshFiles() {
    MockSshServer.with {
      dir('.')
      dir('/tmp')
      dir('/etc/puppet')
    }
  }

}

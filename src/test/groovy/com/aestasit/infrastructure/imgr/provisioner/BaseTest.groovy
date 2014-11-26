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

import org.junit.AfterClass
import org.junit.BeforeClass

import com.aestasit.infrastructure.imgr.model.Box
import com.aestasit.ssh.mocks.MockSshServer

class BaseTest {

  def getKeyPath() {
    new File('./src/test/resources/dummy.pem').canonicalFile.absolutePath.replace('\\', '/')
  }
  
  def getCurrentDir() {
    new File('.').canonicalFile.absolutePath.replace('\\', '/')
  }
  
  def getTestBox() {
    new Box(host:'127.0.0.1', port:2733, keyPath: keyPath)
  }
  
  @BeforeClass
  def static void createServer() {
    MockSshServer.startSshd(2733)
  }

  @AfterClass
  def static void destroyServer() {
    MockSshServer.stopSshd()
  }
  
}

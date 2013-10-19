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

package com.aestasit.infrastructure.imgr

import com.aestasit.cloud.aws.EC2Client
import com.aestasit.infrastructure.imgr.builder.AmiBuilder
import com.aestasit.infrastructure.imgr.Imgr
import com.aestasit.infrastructure.imgr.model.Ec2Box;
import com.aestasit.infrastructure.imgr.provisioner.PuppetProvisioner
import com.aestasit.infrastructure.imgr.provisioner.ShellProvisioner

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static org.junit.Assert.assertEquals

/**
 * @author Aestas/IT
 *
 */
class ImgrTest extends BaseTest {

  def config = """{
  "builders": [{
    "type": "amazon-ebs",
    "access_key": "${AWS_ACCESS_KEY_ID}",
    "secret_key": "${AWS_SECRET_KEY}",
    "region": "eu-west-1",
    "source_ami": "ami-9bf6e0ef",
    "instance_type": "t1.micro",
    "ssh_username": "ec2-user",
    "ami_name": "packer-quick-start",
    "keypair": "${DEFAULT_KEY_NAME}",
    "keypair_location": "${KEY_LOCATION}",
    "security_group": "${SECURITY_GROUP}"
  }]
  }
  """

  def config2 = """{
  "builders": [{
    "type": "amazon-ebs",
    "access_key": "${AWS_ACCESS_KEY_ID}",
    "secret_key": "${AWS_SECRET_KEY}",
    "region": "eu-west-1",
    "source_ami": "ami-9bf6e0ef",
    "instance_type": "t1.micro",
    "ssh_username": "ec2-user",
    "ami_name": "packer-quick-start",
    "keypair": "${DEFAULT_KEY_NAME}",
    "keypair_location": "${KEY_LOCATION}",
    "security_group": "{SECURITY_GROUP}"
  }],
  "provisioners": [{
    "type": "shell",
    "inline": ["uname -a > uname.txt","touch test.txt"],
    "script":"test.sh"
  },
  {
    "type": "puppet-masterless",
    "hiera_config_path": "/Users/luciano/some.hra",
    "module_paths": [
      "/Users/luciano/some/module1",
      "/Users/luciano/some/module2"
    ],
    "manifest_file": "/Users/luciano/Downloads/site.pp",
    "staging_directory" : "/home/ec2-user"

  }]

  }
  """

  EC2Client ec2

  @Before
  public void prepare() {
    System.setProperty("aws.accessKeyId", AWS_ACCESS_KEY_ID)
    System.setProperty("aws.secretKey", AWS_SECRET_KEY)
    ec2 = new EC2Client(DEFAULT_REGION)
  }

  @After
  public void shutDown() {

    ec2.listInstances("packer*").each {
      //ec2.terminateInstances([it.instanceId])
    }
  }

  @Ignore
  void testConfig() {

    new Imgr().processConfiguration(getConfig(config))
    assertEquals(1, ec2.listInstances("packer-quick-start").size())
  }

  @Ignore
  void checkImageIsNotCreated() {

    def json = new JsonSlurper().parseText(config)
    json.put('skip_image', 'true')
    new Imgr().processConfiguration(getConfig(JsonOutput.toJson(json)))
    assertEquals(1, ec2.listInstances("packer-quick-start").size())
  }

  @Ignore
  void testCheckHasProvisioners() {

    def invocationCount = 0

    AmiBuilder.metaClass.startInstance = {->
      new Ec2Box(instanceId: '129', host: 'x', user: 'y', keyPath: '/')
    }

    AmiBuilder.metaClass.createImage = { Ec2Box box, String name, String description ->
      // Do nothing
    }

    PuppetProvisioner.metaClass.provision = {

      invocationCount++
    }

    ShellProvisioner.metaClass.provision = {

      invocationCount++
    }

    Imgr p = new Imgr()
    p.processConfiguration(getConfig(config2))
    assertEquals(2, invocationCount)

  }

}
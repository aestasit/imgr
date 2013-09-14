package com.aestasit.infrastructure.provisioner

import com.aestasit.infrastructure.BaseTest

import static org.junit.Assert.*
import groovy.time.TimeCategory
import com.aestasit.infrastructure.model.*
import com.aestasit.infrastructure.provisioner.*
import org.junit.*
import groovy.json.JsonSlurper

class ShellProvisionerTest extends BaseTest {


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
    "keypair": "${AWS_ACCESS_KEY_ID}"
  }],

  "provisioners": [{
    "type": "shell",
    "inline": ["uname -a > uname.txt","touch test.txt"],
    "script":"/Users/luciano/Downloads/test.sh"
  }]
  }
  """

  @Test
  void testConfig() {

    def testBox = new Box(host:'ec2-54-216-53-12.eu-west-1.compute.amazonaws.com',
        user:'ec2-user',
        port:21,
        keyPath:"${KEY_LOCATION}")
    ShellProvisioner sp = new ShellProvisioner(testBox, new JsonSlurper().parseText(config).provisioners[0])
    sp.provision()

  }
}
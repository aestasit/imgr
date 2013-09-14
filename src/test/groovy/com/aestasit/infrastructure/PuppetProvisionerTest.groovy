package com.aestasit.infrastructure

import static org.junit.Assert.*
import groovy.time.TimeCategory
import com.aestasit.infrastructure.model.*
import com.aestasit.infrastructure.provisioner.*
import org.junit.*
import groovy.json.JsonSlurper

class PuppetProvisionerTest extends BaseTest {


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


  @Ignore
  void testConfig() {
    def testBox = new Box(host:'ec2-176-34-93-116.eu-west-1.compute.amazonaws.com',
                          user:'ec2-user',
                          port:21,
                          keyPath:"${KEY_LOCATION}")

    def config = new JsonSlurper().parseText(config)
    new PuppetProvisioner(testBox, config.provisioners[0]).provision()
    // TODO write asserts!
  }

}
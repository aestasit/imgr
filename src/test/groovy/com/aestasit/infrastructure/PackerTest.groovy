package com.aestasit.infrastructure

import static org.junit.Assert.*
import groovy.time.TimeCategory
import org.junit.*
import com.aestasit.cloud.aws.*

class PackerTest extends BaseTest {

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
    "keypair_location": "${KEY_LOCATION}"
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
      ec2.terminateInstances([it.instanceId])
    }
  }

  @Test
  void testConfig() {

    new Packer().processConfiguration(new ByteArrayInputStream(config.getBytes("UTF-8")))
    assertEquals(1, ec2.listInstances("packer-quick-start").size())
  }

}
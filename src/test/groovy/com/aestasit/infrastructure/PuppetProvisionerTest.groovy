package com.aestasit.infrastructure

import static org.junit.Assert.*
import groovy.time.TimeCategory
import com.aestasit.infrastructure.model.*
import com.aestasit.infrastructure.provisioner.*
import org.junit.*

class PuppetProvisionerTest {

  def pp

  @Test
  void testConfig() {
    def testBox = new Box(host:'ec2-54-217-105-244.eu-west-1.compute.amazonaws.com',
                          user:'ec2-user',
                          port:21,
                          keyPath:'/Users/luciano/.ec2/aestas-ci')

    pp = new PuppetProvisioner(testBox)
    pp.provision()
  }

}
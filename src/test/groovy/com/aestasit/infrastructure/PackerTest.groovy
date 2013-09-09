package com.aestasit.infrastructure

import static org.junit.Assert.*
import groovy.time.TimeCategory
import org.junit.*

class PackerTest {

  def Packer packer = new Packer()
  def c1 = '/Users/luciano/Projects/clients/aestas/groovy-packer/conf1.json'
  @Test
  void testConfig() {

    packer.processConfiguration(new File(c1))

  }

}
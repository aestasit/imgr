package com.aestasit.infrastructure.builder

import com.aestasit.cloud.aws.*
import com.aestasit.infrastructure.model.Box

class AmiBuilder {

  String accessKey
  String secretKey
  String amiName
  String instanceType
  String sourceAmi
  String sshUsername
  String amiRegion

  AmiBuilder(jsonConfig) {
 
    accessKey = jsonConfig.access_key
    secretKey = jsonConfig.secret_key
    amiName = jsonConfig.ami_name
    instanceType = jsonConfig.instance_type
    sshUsername = jsonConfig.ssh_username
    sourceAmi = jsonConfig.source_ami
    amiRegion = jsonConfig.region

  }

  def getTempKeyName() {
    // TODO
    'aestas-ci'
  }

  Box startInstance() {

    // TODO allows reading from system properties
    System.setProperty("aws.accessKeyId", accessKey)
    System.setProperty("aws.secretKey", secretKey)

    def ec2 = new EC2Client(amiRegion)

    def instance = ec2.startInstance(getTempKeyName(),
                      sourceAmi,
                      'aestas-default', //TODO should use temporary security by default
                      instanceType,
                      true, -1, amiName)

    new Box(host:instance.host, port:22)
  }


}


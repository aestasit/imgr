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
  String keyPairName
  String keyPairPath // This should go

  AmiBuilder(conf) {

    accessKey = conf.access_key
    secretKey = conf.secret_key
    amiName = conf.ami_name
    instanceType = conf.instance_type
    sshUsername = conf.ssh_username
    sourceAmi = conf.source_ami
    amiRegion = conf.region
    keyPairName = conf.keypair

    keyPairPath = conf.keypair_location// This should go
  }

  Box startInstance() {

    // TODO allows reading from system properties
    System.setProperty("aws.accessKeyId", accessKey)
    System.setProperty("aws.secretKey", secretKey)

    def ec2 = new EC2Client(amiRegion)

    def instance = ec2.startInstance(keyPairName,
                      sourceAmi,
                      'aestas-default', //TODO should use temporary security by default
                      instanceType,
                      true, -1, amiName)

    new Box(host:instance.host,
            port:22,
            keyPath:keyPairPath,
            user:sshUsername)
  }


}


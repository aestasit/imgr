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

package com.aestasit.infrastructure.imgr.builder

import groovy.util.logging.Slf4j

import com.aestasit.cloud.aws.EC2Client
import com.aestasit.infrastructure.imgr.model.Box
import com.aestasit.infrastructure.imgr.model.Ec2Box;

/**
 * @author Aestas/IT
 *
 */
@Slf4j
class AmiBuilder extends BaseBuilder {

  String accessKey
  String secretKey
  String amiName
  String instanceType
  String sourceAmi
  String amiRegion

  String keyPairName // TODO this should go
  String keyPairPath // TODO This should go
  String securityGroup // TODO This should go
  
  def ec2

  AmiBuilder(config) {

    accessKey = config.access_key
    secretKey = config.secret_key
    amiName = config.ami_name
    instanceType = config.instance_type
    sourceAmi = config.source_ami
    amiRegion = config.region

    keyPairName = config.keypair // TODO This should go
    keyPairPath = config.keypair_location // TODO This should go
    securityGroup = config.security_group // TODO This should go

    accessKey = accessKey ?: System.getenv('AWS_ACCESS_KEY_ID')
    secretKey = secretKey ?: System.getenv('AWS_SECRET_ACCESS_KEY')

    if (!accessKey || !secretKey) {
      log.error 'Either "accessKey" or "secretKey" is null! They are required to make Amazon AWS connection.'
      System.exit(1)
    }

    System.setProperty("aws.accessKeyId", accessKey)
    System.setProperty("aws.secretKey", secretKey)

    ec2 = new EC2Client(amiRegion)

  }

  Box startInstance() {
    log.info 'Launching a source AWS instance...'
    def instance = ec2.startInstance(
        keyPairName,
        sourceAmi,
        securityGroup,
        instanceType,
        true, -1, amiName)
    new Ec2Box(
        host: instance.host,
        port: 22,
        keyPath: keyPairPath,
        instanceId: instance.instanceId
        )
  }

  void createImage(Ec2Box box, name, description) {
    // TODO better error handling
    ec2.createImage(box.instanceId, name, description, true, 120, 20)
  }

}

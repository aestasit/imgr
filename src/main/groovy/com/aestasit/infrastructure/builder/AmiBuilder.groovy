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

package com.aestasit.infrastructure.builder

import com.aestasit.cloud.aws.EC2Client
import com.aestasit.infrastructure.model.Box
import groovy.util.logging.Slf4j

@Slf4j
class AmiBuilder {

  String accessKey
  String secretKey
  String amiName
  String instanceType
  String sourceAmi
  String sshUsername
  String amiRegion
  String keyPairName // TODO this should gp
  String keyPairPath // TODO This should go
  String securityGroup // TODO This should go
  def ec2

  AmiBuilder(conf) {

    accessKey = conf.access_key
    secretKey = conf.secret_key
    amiName = conf.ami_name
    instanceType = conf.instance_type
    sshUsername = conf.ssh_username
    sourceAmi = conf.source_ami
    amiRegion = conf.region

    keyPairName = conf.keypair // TODO This should go
    keyPairPath = conf.keypair_location // TODO This should go
    securityGroup = conf.security_group // TODO This should go

    if (!accessKey) {
      accessKey = System.getenv('AWS_ACCESS_KEY_ID')
    }
    if (!secretKey) {
      secretKey = System.getenv('AWS_SECRET_ACCESS_KEY')
    }

    if (!accessKey || !secretKey) {

      log.error 'accessKey or secretKey are null'
      System.exit(0)
    }

    System.setProperty("aws.accessKeyId", accessKey)
    System.setProperty("aws.secretKey", secretKey)

    ec2 = new EC2Client(amiRegion)

  }

  Box startInstance() {
    log.info 'Launching a source AWS instance...'
    def instance = ec2.startInstance(keyPairName,
        sourceAmi,
        securityGroup,
        instanceType,
        true, -1, amiName)

    new Ec2Box(host: instance.host,
        port: 22,
        keyPath: keyPairPath,
        user: sshUsername,
        instanceId: instance.instanceId)
  }


  void createImage(Ec2Box box, name, description) {
    // TODO better error handling
    ec2.createImage(box.instanceId, name, description, true, 120, 20)
  }

}

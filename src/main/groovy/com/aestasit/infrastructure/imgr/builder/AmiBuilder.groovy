/*
 * Copyright (C) 2011-2014 Aestas/IT
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

import com.aestasit.infrastructure.aws.EC2Client
import com.aestasit.infrastructure.aws.model.KeyPair
import com.aestasit.infrastructure.imgr.model.Box
import com.aestasit.infrastructure.imgr.model.EC2Box

/**
 * EC2 AMI builder.
 * 
 * @author Aestas/IT
 *
 */
@Slf4j
class AmiBuilder extends BaseBuilder {

  String accessKey
  String secretKey
  String region
  
  String amiName
  String amiDescription  
  String instanceName
  String sourceAmi
  String instanceType
  
  String securityGroup 
  
  private EC2Client ec2
  private EC2Box box

  AmiBuilder(config) {

    accessKey = config.access_key
    secretKey = config.secret_key
    region = config.region ?: 'eu-west-1'
    
    amiName = config.ami_name ?: "Created with imgr at ${new Date().format('yyyy-MM-dd hh:mm')}"
    amiDescription = config.ami_description ?: amiName
    instanceName = config.instance_name ?: amiName 
    instanceType = config.instance_type
    sourceAmi = config.source_ami

    securityGroup = config.security_group ?: "default"

    accessKey = accessKey ?: System.getenv('AWS_ACCESS_KEY_ID')
    secretKey = secretKey ?: System.getenv('AWS_SECRET_ACCESS_KEY')

    if (!accessKey || !secretKey) {
      log.error 'Either "accessKey" or "secretKey" is null! They are required to make Amazon AWS connection.'
      System.exit(1)
    }

    System.setProperty("aws.accessKeyId", accessKey)
    System.setProperty("aws.secretKey", secretKey)

    ec2 = new EC2Client(region)

  }

  @Override
  Box initiate() {

    log.info '> Creating temporary key pair...'
    def keyPair = ec2.createKeyPair()
    def keyPath = saveKey(keyPair)
        
    log.info '> Launching a source AWS instance...'
    def instance = ec2.startInstance(
      keyPair.name,
      sourceAmi,
      securityGroup,
      instanceType,
      true, 
      22,
      -1, 
      instanceName
    )
    
    // Save box data.
    box = new EC2Box(
      id: "${instance?.instanceId}",      
      host: "${instance?.host}",
      port: 22,      
      keyPath: keyPath,
      keyPairName: keyPair.name
    )    
    box
    
  }
  
  @Override
  String createImage() {
    log.info "> Creating image..."
    ec2.createImage(box.id, amiName, amiDescription, true, 120, 20)
  }

  @Override
  void cleanup() {
    log.info '> Terminating instance...'
    ec2.terminateInstance(box.id)
    log.info '> Deleting temporary key pair...'
    ec2.destroyKeyPair(box.keyPairName)
  }

  /*
   * PRIVATE METHODS
   */
    
  private saveKey(KeyPair keyPair) {
    File keyFile = File.createTempFile(keyPair.name, '.pem')
    keyFile.text = keyPair.material
    keyFile.absolutePath
  }
  
}

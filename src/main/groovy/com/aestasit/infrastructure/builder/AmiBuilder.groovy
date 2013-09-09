class AmiBuilder {

  String accessKey
  String secretKey
  String amiName
  String instanceType
  String sourceAmi
  String sshUsername
  String amiRegion
  
  def config(jsonConfig) {

    jsonConfig.with {
      accessKey = access_key
      secretKey = secret_key
      amiName = ami_name
      instanceType = instance_type
      sshUsername = ssh_username
      sourceAmi = source_ami
      amiRegion = region
    }
  }


}


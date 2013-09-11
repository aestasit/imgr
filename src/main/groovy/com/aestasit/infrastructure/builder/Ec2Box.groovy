package com.aestasit.infrastructure.builder

import com.aestasit.cloud.aws.*
import com.aestasit.infrastructure.model.Box

class Ec2Box extends Box {

  String instanceId

  String getId() {
    instanceId
  }
}

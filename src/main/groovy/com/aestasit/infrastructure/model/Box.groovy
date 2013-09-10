package com.aestasit.infrastructure.model

import groovy.transform.Canonical

@Canonical
class Box {

  String host
  int port
  String user
  String keyPath

}

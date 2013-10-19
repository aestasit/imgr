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

package com.aestasit.infrastructure.imgr.provisioner

import groovy.util.logging.Slf4j

import com.aestasit.infrastructure.imgr.model.Box
import com.aestasit.infrastructure.imgr.transport.SshSession

/**
 * Provisioner class that does nothing except showing an error message. 
 *
 * @author Aestas/IT
 *
 */
@Slf4j
class UnsupportedProvisioner extends BaseProvisioner {

  UnsupportedProvisioner() {
    super(null, [:])
  }

  UnsupportedProvisioner(Box box, Map provisionerConfig) {
    super(box, provisionerConfig)
  }

  UnsupportedProvisioner(SshSession session, Map provisionerConfig) {
    super(session, provisionerConfig)
  }

  @Override
  public void provision() {
    log.error "Unsupported provisioner type!"
  }
}
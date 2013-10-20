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
 * File provisioner allows copying local files to remote instances. 
 *
 * @author Aestas/IT
 *
 */
@Slf4j
class FileProvisioner extends BaseProvisioner {

  FileProvisioner(Box box, Map provisionerConfig) {
    super(box, provisionerConfig)
  }

  @Override
  public void doProvision() {
    session.scp(new File(provisionerConfig.source_path), provisionerConfig.target_path)
  }
  
}
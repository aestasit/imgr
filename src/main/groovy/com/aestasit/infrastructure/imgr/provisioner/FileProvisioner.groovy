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

package com.aestasit.infrastructure.imgr.provisioner
import groovy.text.SimpleTemplateEngine
import groovy.text.Template
import groovy.util.logging.Slf4j

import com.aestasit.infrastructure.imgr.model.Box

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
    def sourceFile = new File(provisionerConfig.source_path).canonicalFile.absoluteFile
    def filesToClean = []
    if (sourceFile.exists()) {
      if (provisionerConfig.parameters) {
        if (sourceFile.isFile()) {
          if (provisionerConfig.parameters instanceof Map) {
            def replacementFile = new File(File.createTempDir(), sourceFile.name)            
            SimpleTemplateEngine engine = new SimpleTemplateEngine()
            Template template = engine.createTemplate(sourceFile.text)
            replacementFile.text = template.make(provisionerConfig.parameters)
            sourceFile = replacementFile
            filesToClean << replacementFile
            filesToClean << replacementFile.parentFile
          } else {
            log.warn "Template parameters are not defined as a map!"
          }
        } else {
          log.warn "Source path is a directory! Can't apply template parameters to it!"
        }
      }
      session.scp(sourceFile.absolutePath, provisionerConfig.target_path)      
    } else {
      log.warn "Source file or directory (${sourceFile.absolutePath}) does not exist!"
    }
    filesToClean.each { File file ->
      file.delete()
    }
  }
}

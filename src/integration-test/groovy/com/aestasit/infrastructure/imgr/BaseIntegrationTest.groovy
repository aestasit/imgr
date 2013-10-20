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

package com.aestasit.infrastructure.imgr

import static org.junit.Assert.*
import groovy.time.TimeCategory
import org.junit.*

/**
 * 
 * 
 * @author Aestas/IT
 *
 */
class BaseIntegrationTest {

  protected static String AWS_ACCESS_KEY_ID = ''
  protected static String AWS_SECRET_KEY = ''
  protected static String DEFAULT_KEY_NAME = ''
  protected static String KEY_LOCATION = ''
  protected static String DEFAULT_REGION = ''
  protected static String SECURITY_GROUP = ''

  @BeforeClass
  public static void setUp() {
    AWS_ACCESS_KEY_ID = readProperty('awsAccessKeyId')
    AWS_SECRET_KEY = readProperty('awsSecretKey')
    DEFAULT_KEY_NAME = readProperty('awsDefaultKeyName')
    KEY_LOCATION = readProperty('awsDefaultKeyFile')
    DEFAULT_REGION = readProperty('awsDefaultRegion', 'eu-west-1')
    SECURITY_GROUP = readProperty('awsDefaultSecurityGroup')
  }

  private static String readProperty(String key) {
    String value = System.getProperty(key)
    if (!value) {
      throw new RuntimeException("Missing property: '$key'. Please, pass it through system properties!")
    }
    return value
  }

  private static String readProperty(String key, String defaultValue) {
    String value = System.getProperty(key)
    if (!value) {
      value = defaultValue
    }
    return value
  }

  def getConfig(String c) {
    new ByteArrayInputStream(c.getBytes("UTF-8"))
  }
}

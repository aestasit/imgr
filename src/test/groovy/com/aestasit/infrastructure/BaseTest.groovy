package com.aestasit.infrastructure

import static org.junit.Assert.*
import groovy.time.TimeCategory
import org.junit.*

class BaseTest {

  protected static String AWS_ACCESS_KEY_ID = ''
  protected static String AWS_SECRET_KEY = ''
  protected static String DEFAULT_KEY_NAME = ''
  protected static String KEY_LOCATION = ''
  protected static String DEFAULT_REGION = ''
  


  @BeforeClass
  public static void setUp() {
    AWS_ACCESS_KEY_ID = readProperty('awsAccessKeyId')
    AWS_SECRET_KEY = readProperty('awsSecretKey')
    DEFAULT_KEY_NAME = readProperty('awsDefaultKeyName')
    KEY_LOCATION = readProperty('awsDefaultKeyFile')
    DEFAULT_REGION = readProperty('awsDefaultRegion', 'eu-west-1')
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
}

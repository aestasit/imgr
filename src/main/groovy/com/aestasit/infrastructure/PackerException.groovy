package com.aestasit.infrastructure


class PackerException extends RuntimeException {

  PackerException(String message, Throwable cause) {
    super(message, cause)
  }

  PackerException(String message) {
    super(message)
  }
}

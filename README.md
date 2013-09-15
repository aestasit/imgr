# groovy-packer

## A [Packer](http://www.packer.io/) inspired tool written in Groovy

_groovy-packer_ is a tool to create Amazon EC2 AMIs from a configuration file.

It is built with extensibility in mind, therefore it is easy to add new cloud providers.

Currently, it supports configuring a box via a [Puppet](http://projects.puppetlabs.com/) manifest or a Shell script.

## Requirements

- Java 6

## Building

`gradlew clean build`

Tests are disabled by default. To enable the tests, use

`gradlew clean build -Drun.tests=true`

## Installation

- Run `gradlew distZip` (or `gradlew.bat distZip` on Windows )
- An archive containing the application's runtime and script is generated in the 'build/distributions' folder
- Unzip the archive in any location

## Usage

- Run `bin/groovy-packer config.json` to execute a configuration located in the `config.json` file.

## Configuration

_groovy-packer_ configuration is based on JSON. A _groovy-packer_ configuration file is divided in two sections:

- Builder
- Provisioner


### Builders

A _builder_ represent the description of how to start a Virtual Machine. Currently, _groovy-packer_ only supports Amazon EC2.
The Amazon builder is named `amazon-ebs` and has the following properties:

- `type` (string) the type of builder. At the moment only `amazon-ebs` is supported
- `access_key` (string) the access key used to communicate with AWS. If not specified,
Packer will attempt to read this from the environmental variable `AWS_ACCESS_KEY_ID`
- secret_key (string) The secret key used to communicate with AWS.
If not specified, Packer will attempt to read this from environmental variables `AWS_SECRET_ACCESS_KEY`



### Provisioners

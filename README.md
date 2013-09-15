# groovy-packer

## A [Packer](http://www.packer.io/) inspired tool written in Groovy

_groovy-packer_ is a tool to create Amazon EC2 AMIs (images) from a configuration file.

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

## Template configuration

_groovy-packer_ configuration is based on a template written in in JSON. A template contains the various components
required to build a machine image. A template has two sections:

- Builder
- Provisioner

### Builders

A _builder_ represent the description of how to start a Virtual Machine. Currently, _groovy-packer_ only supports Amazon EC2.
The Amazon builder is named `amazon-ebs` and has the following properties:

- `type` (string) the type of builder. At the moment only `amazon-ebs` is supported
- `access_key` (string) the access key used to communicate with AWS. If not specified,
Packer will attempt to read this from the environmental variable `AWS_ACCESS_KEY_ID`
- `secret_key` (string) The secret key used to communicate with AWS.
If not specified, Packer will attempt to read this from environmental variables `AWS_SECRET_ACCESS_KEY`
- `region` (string) The name of the region, such as "us-east-1", in which to launch the EC2 instance to create the AMI.
- `source_ami` (string) The AMI used to start the machine that will be configured. 
- `instance_type` (string) The EC2 instance type used for starting the machine. For a list of valid instance types refer to the [Amazon documentation](https://aws.amazon.com/ec2/instance-types/#instance-details).
- `ssh_username` (string) The username used by _groovy-packer_ to communicate with the machine over SSH.
- `ami_name` (string) The name of the generated AMI. Please note that the name must be unique.
- `keypair` (string) The name of the key pair used to connect to your EC2 instance. __This option will be removed soon__
- `keypair_location`  (string) The path to the key pair stored on the local machine __This option will be removed soon__


The `amazon-ebs` builder creates an instance which root device is backed by Amazon EBS (for more information about instance types read [here](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ComponentsAMIs.html#storage-for-the-root-device).

Here is an example of a Builder:

	"builders": [{
    	"type": "amazon-ebs",
        "access_key": "AMAZON_1234556790",
        "secret_key": "AMAZON_SECRET_12345",
        "region": "eu-west-1",
        "source_ami": "ami-9bf6e0ef",
        "instance_type": "t1.micro",
        "ssh_username": "ec2-user",
        "ami_name": "groovy",
        "keypair": "mykeypair",
        "keypair_location":"/Users/John/.ec2/mykeypair.pem"
    }]


### Provisioners

The `provisioners` section of the template is an array of one or more objects that defines the provisioners that will be
used to install and configure software for the machines created by each of the builders

At the moment there are two provisioners supported: a Shell provisioner and a Puppet provisioner.

#### Shell provisioner


#### Puppet provisioner


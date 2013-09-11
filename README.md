# groovy-packer

## A [Packer](http://www.packer.io/) clone written in Groovy

## Requirements

- Java 6

## Installation

- Run `gradlew distZip` (or `gradlew.bat distZip` on Windows )
- An archive containing the application's runtime and script is generated in the 'build/distributions' folder
- Unzip the archive in any location

## Usage

- Run `bin/groovy-packer config.json` to execute a configuration located in the `config.json` file.

## Example configuration

- TODO
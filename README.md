# Handschriftenportal - Index Update Service

## Description

This repository contains a Spring Boot-based microservice that maps data in TEI (https://tei-c.org/) format to Apache Solr (https://solr.apache.org/) documents. Therefore a running Solr instance is necessary, using the Handschriftenportal Solr Core Schema and running Kafka Broker instance for providing the data.

## Technology Stack

This microservice is build on Spring Boot by using SolrJ for querying Solr. The TEI data is received from one or more Kafka Brokers. The TEI Evaluation is done by using Jaxen and DOM4J.

## Status

The status of this project is stable.

## Live

https://handschriftenportal.de/

## Getting Started

1. Get the source code

```
git clone https://github.com/handschriftenportal-dev/hsp-fo-indexer
```

2. Start the service

```
mvn spring-boot:run
```

Afterwards, any data received from Kafka will be transformed and saved to the Solr Index

## Configuration

The service can be configured by using Spring Boot YAML configuration files. Important properties are:

- _common.restart-interval:_ interval (in ms), that is uses for restarting the service after a serious error
- _kafka.bootstrap-servers:_ list of kafka brokers
- _kafka.topic_: the name of the kafka topic
- _solr.leader_: solr host, where the data should be written to.
- _solr.core_: name of the solr core (usually `hsp`)
- _solr.schema-version_: the schema version (can be found in version.yaml) - there's no need the change, as long there are no changes to the mapping algorithm. Increasing the schema version will cause a removal of the whole Solr dataset and re-indexing it.

## How to test the software

```
mvn test
```

## Known issues

## Getting help

To get help please use our contact possibilities on [twitter](https://twitter.com/hsprtl)
and [handschriftenportal.de](https://handschriftenportal.de/)

## Get involved

To get involved please contact our development team [handschriftenportal@sbb.spk-berlin.de](handschriftenportal-dev@sbb.spk-berlin.de)

## Open source licensing info

The project is published under the [MIT License](https://opensource.org/licenses/MIT).

## Credits and references

1. [Github Project Repository](https://github.com/handschriftenportal-dev)
2. [Project Page](https://handschriftenportal.de/)

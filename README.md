# R2RLint

R2RLint is a quality assessment tool to evaluate the quality of RDB2RDF mappings and the resulting data. R2RLint currently comes with 43 implemented metrics that can be switched on and configured with an individual threshold to customize your RDB2RDF quality assessment.

## Installation

To install R2RLint run the following commands:

* Get the source code from GitHub:
```bash
git clone https://github.com/AKSW/R2RLint.git
```
* Go to the Git repo directory and run `install.sh`:
```bash
cd R2RLint
./install.sh
```

## Configuration

Before running the assessment two configuration steps need to be taken: First the assessment environment needs to be set up and afterwards the metrics to run are configured

### Environment

To configure the assessment environment the file `etc/environment.properties` needs to be edited. The main configuration options are the following

#### relational database connection

These options contain settings for the relational database which is mapped to RDF. The options are:

* `rdb.host`: the hostname or IP address of the host the database management system is running on
* `rdb.port`: the TCP port the database management system is listening on
* `rdb.dbName`: the name of the database
* `rdb.user`: a user to access the database
* `rdb.password`: the password of the database user

#### RDF dataset

To run the assessment, an N-triples dump of the generated RDF has to be provided. Additionally the URL to a SPARQL endpoint can be provided, which allows to run SPARQL queries on the dataset without the need that it fits into your main memory. The configuration options are:

* `dataset.dumpFilePath`: the file path to a file containing N-triples of the generated RDF
* `dataset.serviceUri` (optional): the URI pointing to the SPARQL endpoint to use, e.g. `http://dbpedia.org/sparql`
* `dataset.graphIri` (optional): the graph to use for the assessment (note: the consideration of single graphs is not implemented, yet)
* `dataset.usedPrefixes`: since some metrics have to refer to the vocabularies used in the dataset, their prefixes have to be configured explicitly; the prefixes have to be given as comma separated values
* `dataset.prefixes`: since some metrics need to know, whether a certain resource is _local_ or _external_; thus the set of local prefixes is required, given as comma separated values

#### miscellaneous files

The following options provide access to some files needed to run an assessment

* `views.viewDefsFilePath`: the file containing the SML mapping definitions to assess
* `metrics.settingsFilePath`: the path the properties file containing the metrics settings introduced below
* `views.typeAliasFilePath`: a file containing type mappings (usually no changes of the provided `type-map.h2.tsv` file are needed)

#### sink settings

An assessment sink is the actual target where the evaluated quality scores with respect to a given metric is written to. Due to the modularity of the R2RLint framework, sinks can be added without bigger wiring efforts and without the need to know the framework internals. The sinks currently implemented are introduced in dedicated sections below. The configuration options are sink specific and thus discussed in the corresponding sink section.

### Metrics

The settings with regards to the actual metrics to run can be found in the file `etc/metrics.properties`. R2RLint provides metrics for the following quality dimensions:

* availability
* completeness
* conciseness
* consistency
* interlinking
* interoperability
* interpretability
* performance
* relevancy
* representational conciseness
* semantic accuracy
* syntactic validity
* understandability

To enable a certain dimension for the assessment its value in the `etc/metrics.properties` file has to be switched to `yes`, e.g.
```java
semantic_accuracy = yes
```
Only if a dimension is enabled, its metrics are considered. To also activate certain metrics, their values have to be switched to `yes`, too, e.g.
```java
semantic_accuracy.preservedFkeyConstraint = yes
```
Accordingly, in an assessment run, only those metrics are applied, that

* belong to an activated quality dimension
* are activated as well

Besides the activation, thresholds can be set up per metric. If a threshold is set, only those quality scores (and the corresponding metadata) are written to the sink, that have a score that is lower than the configured threshold. To configure a threshold value, a property named `<dimension>.<metric>.threshold` has to be added, e.g.:
```java
consistency.homogeneousDatatypes.threshold = 0.95
```

## Sinks

In this section the sink implementations provided by R2RLint are introduced.

### RDB Sink

The RDB sink is a measure data sink that writes the actual quality scores and metadata to a relational database. To set up such a sink, the following configuration options have to be provided in the `etc/environment.properties` file:

* `rdbSink.host`: the hostname or IP address of the host the database management system is running on
* `rdbSink.port`: the TCP port the database management system is listening on
* `rdbSink.dbName`: the name of the database
* `rdbSink.user`: a user to access the database
* `rdbSink.password`: the password of the database user

The sink reflects the class structure of the R2RLint framework and creates the following tables:

`measure_datum`

| `id` _bigint PRIMARY KEY_  | `dimension` _varchar(400)_ | `metric` _varchar(400)_ | `value` _real NOT NULL_ | `assessment_id` _bigint NOT NULL_ | `timestamp` _timestamp default current_timestamp_ |
| ------------------------ | ------------------------ | --------------------- | --------------------- | ------------------------------- | ----------------------------------------------- |



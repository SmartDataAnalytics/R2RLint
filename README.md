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


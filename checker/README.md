

# ViSearch Checker

ViSearch checker is the core of consistency measurement for replicated data type. 

ViSearch checker checks whether a history satisfies a given consistency model. The ViSearch paper defines the problem and gives context.

## INSTALL & RUN

The following steps build ViSearch Checker and run it on histories. 

This tutorial has been tested on Ubuntu 16.04, Java v1.9.0, and Maven v3.3.9. 

### Step 1: Build ViSearch checker

Install required package:

```
$ sudo apt-get install mvn
```

Build ViSearch Checker:

```
$ mvn compile && mvn package
```

### Step 2: Run ViSearch checker

ViSearch checker has different options for checking:

* check / measure: check whether a history satisfies a given consistency model / measure the consistency level of a history

* single / dataset: run on a history / run a batch of histories

```
$ ./check.sh 
```

There are several arguments:

|           argument           |                             info                             |
| :--------------------------: | :----------------------------------------------------------: |
|          -h, --help          |               Show this help message and exit                |
|   -t [type], --type [type]   |                    Data type for checking                    |
| -f [path], --filepath [path] |                      File path to check                      |
|    -p [n], --parallel [n]    |           Number of parallel threads (default: 16)           |
|    -v [vis], --vis [vis]     |             Visibility Level (default: complete)             |
|       --unset-measure        | Disable measure mode and enable check mode <br />(default: measure mode) |
|       --unset-dataset        | Disable dataset mode and enable single mode <br />(default: dataset mode) |

For example,  measure histories of data type set. 

```
$ ./check.sh --type set --filepath ../set-r1/ --parallel 8
```

For example,  check a history of data type map whether satisfies peer. 

```
$ ./check.sh --type map --filepath ../map-r1/map_r1_default_17832.trc --unset-measure --unset-dataset --vis peer --parallel 8
```
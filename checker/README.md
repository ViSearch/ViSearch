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

## Format of History

Histories are traces of data type access. The history defines how the clients interact with the data type store, and specifies what information is recorded about this interaction. 

We will demonstrate the format of history in ViSearch. 

### Information of History

* Sessions: the number of sessions and the number of operations for each session.
* Operations: method name, argument1, argument2, ... , argumentk, return value. 

For example, there are 3 sessions in this history. Session one has 6 operations, session two has 6 operations, and session three has 3 operations. 

Line 2\~7 belong to session one, line 8\~13 belong to session two, and line 14\~16 belong to session three. 

```
3 6 6 3
0,0,remove,0,null
0,0,contains,2,false
0,0,size,1
0,0,contains,1,false
0,0,add,3,null
0,0,add,0,null
0,0,remove,2,null
0,0,size,0
0,0,add,0,null
0,0,remove,1,null
0,0,size,3
0,0,size,2
0,0,contains,3,false
0,0,add,0,null
0,0,add,1,null
```
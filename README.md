# GenMarkOpus
Demonstration Java code to implement Opus atop MongoDB, using three primary collections and two-way linkages. opustestbed.jar is provided as a pre-compiled .jar file with all needed libraries.

## Dependencies
* To build: Java 8 or later JDK, internet access, Maven
* To run: Java 8 or later JRE, access to MongoDB 4.x instance

## Usage

```
usage: Testbed
 -c,--check            Run sanity checks on Opus tree
 -d,--delete <arg>     Remove any tree or sub-tree
 -g,--generate <arg>   Generate N Opus trees from scratch (default 20000) 
                       Note: code currently performs Check after every Generate 
 -h,--help             Show Help
 -r,--ID <arg>         Retrieve/print tree or sub-tree, given id
 -u,--uri <arg>        Connection String (URI) (default
                       'mongodb://localhost:27017/' )
```

## Assumptions
* Unique OPUS IDs (using _id) starting at 1
* Unique Function IDs (using _id) starting at maxOpus + 1000
* Unique Score IDs (using _id) starting at maxOpus + 1000 + maxFuncs^maxLevels
* One Opus->many Functions/many levels->one Score

## What This Code Demonstrates
* Creation of Opus/Function/Score collections with two-way pointers:
  opus.functions - list of children Functions

  functions.opusID - Parent OpusID (if any)
             or
  functions.groupFunctionID - Parent Function (if any)

  functions.functions - list of children Functions
             or
  functions.scores - list of children Scores

  scoreCommands.functionID - Parent function
* Traverse Opus or Function trees top-down
* Delete Opus tree or sub-tree
* 'Sanity Check' of all trees and sub-trees

## What This Does Not Code Demonstrate
* Reordering of sub-trees or nodes

## Best MongoDB Best Practices Demonstrated
* Use of _id for unique IDs for all three collections - saves space and indexes
* Not using null values - leave field out and use $exists instead - saves space
* Proper indexing
* Bulk inserts/updates for data generation

## Best MongoDB Best Practices Not Demonstrated
* Strictly speaking, one might consider using multi-document Transactions when deleting trees and associated links
* Indexes should NEVER be created from application code - done here for convenience

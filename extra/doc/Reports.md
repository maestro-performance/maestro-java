Maestro: Using
============

In older versions, prior to 1.5, Maestro reports would be downloaded and managed by the test front-end. Starting with 1.5
the management of the reports is done through a new component called "Maestro Reports Server". In addition to 
downloading the reports, it will also serve the files and generate the reports. The server is available on the TCP port 
6500.

The reports server provides two types of reports. One is an individual report per node and the other is an aggregated 
report. The aggregated report is displayed on a per test basis.

At the end of the tests, the test data will be downloaded by the report server and will be aggregated automatically. 
Maestro offers some features to extend and manage the tests. The management of reports is done via `maestro-reports-cli` 
CLI too. 


Including Hardware Information In the Reports
----

It is possible to display in the reports the hardware resources of the SUT node used during the test. This is optional.

First, load the sut node information in the database:

```
maestro-reports-cli sut-node -a insert --node-name fake-11.host.com --os-name RHEL --os-version 7.4 --os-arch x86_64 --hw-name true --hw-model "IBM x3550 M3" --hw-cpu "Intel Xeon X5650 @ 12x 2.66GHz" --hw-ram 15873 --hw-disk-type hd --hw-cpu-count 1
```

The SUT node information can be reused many times. If you forget the SUT node id, you can use the `view`
action to list all of the SUT nodes. 

Then, associate the SUT node information with the test:
```
maestro-reports-cli sut-node -a link -s 1 -t 1
```


Managing Reports
----

The reports can also be managed. There are 3 main states for them: valid, invalid, retired. They are:

* valid: the test is valid and the data is good
* invalid: the test is invalid for whatever reason the user decides
* retired: the test was valid, but it's not useful anymore 

All management commands accept an optional comment argument that is displayed in the report index.

To invalidate a test:

```
maestro-reports-cli invalidate -t 1 -c "Invalidated because of network issues on the test lab"
```

To validate a test (only required if you have invalidated it before):

```
maestro-reports-cli validate -t 1
```

To retire a test:
```
maestro-reports-cli retire -t 1 -c "Retired test because was run with an older JVM"
```

To reactivate a test that was previously retired:
```
maestro-reports-cli unretire -t 1
```

You can also add comments to a test:

```
maestro-reports-cli comment -t 1 -c "Good test on build 655. Can be promoted"
```

Finally, it is also possible to consolidate all the tests into a single location. This is useful if the data has moved 
over time and is laying around multiple locations:

```
maestro-reports-cli consolidate -d /new/path/for/reports
```


Converting the Reports
----

It is possible to convert old rate records used by maestro with the option ```convert``` of the CLI tool.

Loading Old Reports
----

```
maestro-reports-cli load -d /extra/opiske/maestro/1.4/baseline
```


Aggregating Old Reports
----

```
maestro-reports-cli aggregate -t 2518 -n 0 -d /tmp/agg
```

```
maestro-reports-cli aggregate --all -d /extra/opiske/maestro/1.4/baseline
```

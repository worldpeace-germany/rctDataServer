 # Introduction

The tool is based on Spring Boot in order to have the easiest possible way to spin up a web server. With a little bit of effort of course the Java classes could get separated and used for any other application server. The class _Application_ is the main class which is executed upon startup. It mainly does three things

- reads in method *readConfiguration()* the config file which needs to be present
- checks if Influx is configured in the configuration file, if it is a timer to poll data is set
- reads the resource *RCT_magic_numbers.csv*  which configures how different magic numbers are dealt with

## Communication
The communication with the inverter has to take place via a TCP/IP stream, there is no high level interface available. This Java tool offers a high level interface to avoid digging into the low level specs of RCT. Every client, regardless if the RCT mobile app or this Java tool, opens a TCP/IP stream to the inverter. If one client requests a certain data set, then the response is being sent to _all_ connected clients.

# Magic Numbers
The communication with the inverter is happening with a list of magic  numbers. Each magic number represents a certain data set. When requesting the data for such a magic number the response can be either a single value (short response) or an array (long response), that solely depends on the magic number being requested for. A list of all magic numbers are listed in the resource file *RCT_magic_numbers.csv*. Please obey there might be more magic numbers which are unknown to the author of this tool. In fact you will find some magic numbers in that CSV which are marked as unknown. That means that these magic numbers have been observed, but their meaning is unknown. They are still listed for future enhancements and to reduce logging. If you find new magic numbers, known or unknown, please report them and I will add them here.

Each entry in that CSV has the following format
```
<magic number>; <description>; <short/long>; <Influx DB>; <Influx key>
```
e.g.
```
1AC87AA0;Current house power usage in W;short;rctdb;PowerHouse
```
The first entry gives the magic number value, a 4 byte value represented as an 8 character string. Then a description explaining the value. The third column specifies if the response from the inverter will be long or short. The last two entries are only in use if you use an Influx DB to store your data, the fourth entry specifies in what DB the time series will be written to and the last entry is the key in the Influx DB.

# REST Interface
The application offers a REST interface to communicate with the inverter. The communication with the inverter itself is based on a proprietary TCP/IP protocol. The provided REST interface should make it easier to integrate the inverter into an own application. Basically two servlet classes are being provided.

## RequestData.java 
Using this REST interface it is possible to query a single magic number, e.g.
```
GET http://<IP of server>:8080/getData?magicNumber=B55BA2CE
```
Please obey that you need to specify the IP of the this server and not the IP of the inverter, it is not the inverter which offers a REST interface.

The response will be a JSON, e.g.
```json
{"type":"short","magicNumber":"B55BA2CE","value":557.44574,"text":"DC input voltage A in V","timestamp":1601535667,"sucess":true}
```
The keys of the JSON should be pretty obvious. The above example is to query a short value, the syntax to query a long value is slightly different, e.g.
```
GET http://<IP of server>:8080/getData?magicNumber=2F0A6B15&timestamp=1601535667
```
It is necessary to specify a timestamp. Based on the RCT inverter logic, this timestamp denotes the time until the data should be provided, in other words the end time and not the start time. The inverter provides in these cases a certain set of data, if you need older data you need to query again. From the response take the oldest timestamp and use it as timestamp value in the next GET request. The response of such a GET request is similar to

```json
{"valueArray":[{"timestamp":1601596799000,"value":876.23},{"timestamp":1599004799000,"value":448243.66},{"timestamp":1596326399000,"value":563352.56},{"timestamp":1593647999000,"value":761210.81},{"timestamp":1591055999000,"value":619530.63},{"timestamp":1588377599000,"value":748530.75},{"timestamp":1585785599000,"value":671891.5},{"timestamp":1583107199000,"value":420310.91},{"timestamp":1580601599000,"value":178612.55},{"timestamp":1577923199000,"value":113891.98},{"timestamp":1575244799000,"value":89456.44},{"timestamp":1572652799000,"value":99682.38},{"timestamp":1569974399000,"value":101047.77},{"timestamp":1567382399000,"value":339397.66},{"timestamp":1562025599000,"value":0.0}],"magicNumber":"2F0A6B15","type":"long","text":"Monthly energy on DC input A (Edc A) [Wh]"}
```
Again obey the meaning of the timestamp in the response as explained above.

## RequestAllData.java
Another option is to request *all* data, make sure to understand *all* in this context: All doesn't mean that all magic numbers are requested from the inverter when this request is being sent. Once you start this Java application all data which is retrieved will be stored application internally. That internal list of values is being returned when you request all data. Keep in mind that only the last data is stored and older data for the same magic number is being overwritten. That behavior especially means that for example if you request all data right after the start of this Java application you will receive a blank list since no data has been requested before.
```
GET http://<IP of server>:8080/getAllData
```
Keep in mind as well the way the communication with the inverter takes place (see at the top of this guide). Since a response from the inverter is being sent to all connected clients it might happen that you see data in the getAllData call which you never have requested, but some other client (e.g. mobile app) might have done.

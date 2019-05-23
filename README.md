DZB User Authentication
========================================================



 What is it?
-------------------
This is an implementing of an Keycloak User Storage SPI. It shows how to integrate our existing external DZB user database to Keycloak. The primary aim is to map our user data to the Keycloak user metamodel so that it can be consumed by the Keycloak runtime.   
ATM it is a simple blue print.


System Requirements
-------------------

You need to have <span>Keycloak</span> running.

All you need to build this project is Java 8.0 (Java SDK 1.8) or later and Maven 3.1.1 or later.


Build and Deploy the Quickstart
-------------------------------

To deploy this provider you must have <span>Keycloak</span> running in standalone or standalone-ha mode. Then type the follow maven command:

   ````
   mvn clean install ...
   ````
TODO


Enable the Provider for a Realm
-------------------------------
TODO

[Disabling Caching](https://www.keycloak.org/docs/2.5/server_installation/topics/cache/disable.html)

Integration test of the Quickstart
----------------------------------

TODO

More Information
----------------
https://www.keycloak.org/docs/2.5/server_development/topics/user-storage.html


Troubleshooting
---------------
[Notes](./notes.md)
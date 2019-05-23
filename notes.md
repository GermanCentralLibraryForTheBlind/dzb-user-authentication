Bind WildFly to a different IP address
--------------------------------------
WildFly “management” interface can be bound to a specific IP address as:

./bin/standalone.sh -bmanagement=192.168.1.1
Now Admin Console can be accessed at http://192.168.1.1:9990.

Or, bind “management” interface to all available IP addresses as:

./bin/standalone.sh -bmanagement=0.0.0.0
You can also bind to two specific addresses as explained here.

Of course, you can bind WildFly “public” and “management” interface together as:

./bin/standalone.sh -b=0.0.0.0 -bmanagement=0.0.0.0


##### via jboss cli

/interface=public/:write-attribute(name=inet-address,value=0.0.0.0)
/interface=management/:write-attribute(name=inet-address,value=0.0.0.0)


JBoss:reload configuration via CLI
----------------------------------
/:reload


ARJUNA012140: Adding multiple last resources is disallowed.
-----------------------------------------------------------------

/system-property=com.arjuna.ats.arjuna.allowMultipleLastResources:add(value="true")

Prevent printing warnings within logs:

/subsystem=logging/console-handler=CONSOLE:write-attribute(name=filter-spec, value="not(match(\"ARJUNA012141\"))"


JBoss Server Remote
-------------------
$ EAP_HOME/bin/jboss-cli.sh

connect http-remoting://192.168.0.1:9999

Adding system property using CLI
-------------------------------------------------------
/system-property=foo:add(value=bar)
Structured Card Query Language (SCQL) Java Card Applet
=================================================
Structured Card Query Language Java Smart Card Applet is an implementation of the ISO 7816-7 standard for smart cards.
The implementation allows to run a database on a smart card with limited memory and to execute SCQL APDUs
within the database environment.

Related project
---------------
[scql-interpreter](https://github.com/PopularTracy/scql-interpreter) - an interpreter, which translates SCQL commands into APDU calls.

Getting Started
---------------
Project is implemented by using `Oracle Java Card 3.1`.
Before running the project, make sure you have installed JDK 11, Java Card Development Kit Tools, Java Card Development Kit Simulator with 
configured system variables `JAVA_HOME` and `JC_HOME_TOOLS`.

All instructions for installing and setting up the environment for the project can be found in the [Oracle documentation](https://docs.oracle.com/en/java/javacard/3.1/guide/introduction.html#JCUGC111).

CAP Settings
------------
1. Right click on the imported `scql-applet` project in Eclipse and select `Java Card` from the drop down menu.
Then select `CAP Files Settings`;

2. Select `scql-applet` from the menu and click on the `Edit` button;

3. From there, select `Compact CAP file` and click next;

4. In the next window, make sure that the target platform version is `3.1.0`. 
And enable the `Support the 32-bit integer type` and `Output CAP file` checkboxes;

5. Switch to the `ScriptGen` tab and make sure that the `Suppress "PowerDown"` checkbox is selected and click `Finish`.

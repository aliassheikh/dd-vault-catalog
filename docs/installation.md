Installation
============

Currently, this project is built as an RPM package for RHEL8 and later. The RPM will install the binaries to `/opt/dans.knaw.nl/dd-vault-catalog` and the
configuration files to `/etc/opt/dans.knaw.nl/dd-vault-catalog`.

For installation on systems that do no support RPM and/or systemd:

1. Build the tarball (see next section).
2. Extract it to some location on your system, for example `/opt/dans.knaw.nl/dd-vault-catalog`.
3. Start the service with the following command
   ```
   /opt/dans.knaw.nl/dd-vault-catalog/bin/dd-vault-catalog server /opt/dans.knaw.nl/dd-vault-catalog/cfg/config.yml 
   ```

Building from source
--------------------

Prerequisites:

* Java 17 or higher
* Maven 3.3.3 or higher
* RPM (optional, only if you want to build the RPM package)

Steps:

    git clone https://github.com/DANS-KNAW/dd-vault-catalog.git
    cd dd-vault-catalog 
    mvn clean install

If the `rpm` executable is found at `/usr/local/bin/rpm`, the build profile that includes the RPM
packaging will be activated. If `rpm` is available, but at a different path, then activate it by using
Maven's `-P` switch: `mvn -Pprm install`.

Alternatively, to build the tarball execute:

    mvn clean install assembly:single

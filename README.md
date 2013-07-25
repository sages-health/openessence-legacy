# Introduction

OpenEssence is part of JHU/APL's Suite for Automated Global Electronic 
bioSurveillance (SAGES). Sages provides a modular, flexible, open source set of
tools to aid in the creation of electronic disease surveillance capabilities. 
OpenEssence provides a web application for data entry, reporting, and 
visualization with the Sages suite.

You can read more about Sages at the [publicity site](http://www.jhuapl.edu/sages).

# Building
OpenEssence uses [Gradle](http://www.gradle.org) as its build system. Since Gradle
comes with a [wrapper script](http://www.gradle.org/docs/current/userguide/gradle_wrapper.html) 
that's checked in to source control, you do __not__ need to install Gradle; just run 
`gradlew build` in the root project directory to build all artifacts. 
 
Build artifacts can be found in `$SUBPROJECT/build/libs`, as per Gradle convention. 
For example, the WAR file built from __core__ can be found in `core/build/libs/openessence.war`.

# Running
OpenEssence comes equipped with a Vagrantfile and [Chef](http://wiki.opscode.com/display/chef/Home)
scripts to get a development environment up and running as quickly as possible. Here are the steps:

## Install Vagrant
Vagrant can be found at [vagrantup.com](http://www.vagrantup.com). If you're on Windows, make sure the installer puts
Vagrant in a path with no spaces (the default on newer versions). For example, older versions of
Vagrant were installed in `C:\Program Files (x86)`, as per Windows convention. Unfortunately,
Vagrant ships with an embedded [Ruby](http://www.ruby-lang.org/) environment that
[doesn't like spaces in path names](https://github.com/mitchellh/vagrant/issues/1652).

## Install VirtualBox
Vagrant uses [VirtualBox](https://www.virtualbox.org) to manage the virtual
machines used for development. If you really don't want to use VirtualBox, you can use any of the
[providers supported by Vagrant](http://docs.vagrantup.com/v2/providers/index.html), e.g. VMWare,
but be prepared to modify the Vagrantfile.

## Install Vagrant plugins
OpenEssence currently uses two Vagrant plugins: [vagrant-omnbius](https://github.com/schisamo/vagrant-omnibus)
and [vagrant-librarian-chef](https://github.com/jimmycuadra/vagrant-librarian-chef). Install them by
running `vagrant plugin install vagrant-omnibus` and `vagrant plugin install vagrant-librarian-chef`
in your shell. Vagrant is kind enough to add itself to your `PATH` variable on Windows, so this
should just work.

## Install an SSH client
SSH is needed if you want to log in to the VMs. Note that you can get
OpenEssence up and running without logging into the VMs, but at some point you'll probably want to
do work on the guest instances. On Windows, you can install SSH via [Cygwin's](http://www.cygwin.com)
`openssh` package; or you can install a dedicated application like
[PuTTY](http://www.chiark.greenend.org.uk/~sgtatham/putty).

## Generate certificates and keys
OpenEssence needs some certs for the frontend to present to
the user, and for each machine to communicate with each other. Luckily, we have a convenient Ruby
script ([chef/sages_dev_cert.rb](https://github.com/sages-health/openessence/tree/master/chef/sages_dev_cert.rb), run it in the `chef` directory) you can run to generate everything you need. In the future, it
might be nice to integrate this into Vagrant so there's one less thing you have to run, or maybe
delegate this to a [Chef Server](http://docs.opscode.com/chef_overview_server.html). Pull requests welcome!

## Configure passwords
GeoServer and PostgreSQL require admin passwords. For GeoServer, create a file called `default.rb`
and put it in the [chef/cookbooks/oe-geoserver/attributes](https://github.com/sages-health/openessence/tree/master/chef/cookbooks/oe-geoserver/attributes)
directory. The files should look something like this:

    default['oe-geoserver']['password'] = 'PASSWORD'

Likewise for the database, create a file called `default.rb` and put it in the [chef/cookbooks/oe-db/attributes](https://github.com/sages-health/openessence/tree/master/chef/cookbooks/oe-db/attributes)
directory. The files should look something like this:

    default['oe-db']['password'] = 'PASSWORD'

## Add the machines to your hosts file.
This isn't strictly necessary, but it can make things
a lot easier. See the example hosts file in [chef/hosts](https://github.com/sages-health/openessence/tree/master/chef/hosts).
[This link](http://helpdeskgeek.com/windows-7/windows-7-hosts-file) provides a good tutorial for
doing this on Windows.

## Spin up the VMs
Run `vagrant up`. This should bring up three VMs: `db`, `geoserver`, and `web`. `db` contains the
PostgreSQL instance, `geoserver` contains the GIS services, and `web` contains the frontend webserver
(currently Apache). Note that this does not bring up a Tomcat instance to run the application code.
Instead, for development, we recommend running Tomcat on the host machine (it's hard to get IDE hot
code deployment working on a remote Tomcat). You should be able to hit the webserver at the
[web.local](https://web.local/) address (assuming you added it to your hosts file). GeoServer should be reachable at
[web.local/geoserver](https://web.local/geoserver)
and Tomcat should be reachable at [web.host.local](https://web.host.local/). See the
[Apache config](https://github.com/sages-health/openessence/tree/master/chef/cookbooks/oe-web/templates/default/frontend-oe-local.conf.erb)
for more information.

## Set up the database
There's still some work we need to do to make this a little easier. For
now, SSH into `db` (`vagrant ssh db`) or use [pgAdmin](http://www.pgadmin.org) to set up your
database.

## Drop the OpenEssence WAR into Tomcat
Again, there's more work we can do to integrate this into
Vagrant.

If everything goes well, you should now have a running OpenEssence instance.

# IDE configuration
OpenEssence leverages [Gradle's](http://www.gradle.org) built in IDE support. 

## IntelliJ IDEA
Importing the project into [IntelliJ IDEA](http://www.jetbrains.com/idea) should _just work_
as long as you have the Gradle plugin installed. However, as with most things, Eclipse can require
a little more work.

## Eclipse
You can either use the [Eclipse Gradle Plugin](https://github.com/SpringSource/eclipse-integration-gradle/) or
generate the Eclipse metadata manually. If you use the Gradle Plugin, the projects should be importable as Gradle
projects.

If you want to generate the Eclipse metadata manually, run `gradlew eclipse`. Each project should then be importable as
an __"Existing Project."__ Make sure to import the root project, as well as each subproject you want to work on.

### Unique Eclipse project names
There is one caveat to the Eclipse support. Eclipse doesn't support projects made up of subprojects, 
like IntelliJ does with their "modules." Consequently, multi-project builds in Maven and Gradle require
subprojects to be imported as top-level Eclipse projects. But Eclipse also has a restriction against
duplicate project names. This means that you would not be able to work on multiple forks of OpenEssence 
at the same time in Eclipse, since all the forks would have a __core__ project, __open-detectors__ project,
etc. 

Luckily, Gradle's Eclipse plugin allows us to work around these restrictions by assigning unique names to
each project. This is done in each fork's `site.gradle` file. For example, in our demo build of OpenEssence,
we prefix each Eclipse project name with __demo-__ like so:

    ext {
        eclipseProjectPrefix = 'demo-'
    }
    
This will result in a project called __demo-core__, __demo-graph__, etc, that can be imported alongside the
mainline projects. 

For more information, see the documentation on Gradle's [Eclipse](http://www.gradle.org/docs/current/userguide/eclipse_plugin.html) 
and [IDEA](http://www.gradle.org/docs/current/userguide/idea_plugin.html) plugins.

# Help
Feel free to ask technical questions on the
[sages-health-support](https://groups.google.com/forum/#!forum/sages-health-support) mailing list.

For general disease surveillance questions, check out the
[sages-health-epi](https://groups.google.com/forum/#!forum/sages-health-epi) mailing list.

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

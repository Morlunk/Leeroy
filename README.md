Leeroy
======

Leeroy is an app designed to manage updates for apps built by [Jenkins CI](http://jenkins-ci.org/).

Apps can integrate with Leeroy by exposing Jenkins-related meta-data in their AndroidManifest.xml.

**Leeroy is incredibly experimental- expect bugs and incompleteness.**

Integrating your app with Leeroy
--------------------------------

For gradle-based projects, integrating your app with Leeroy is as simple as adding the [Leeroy-Gradle](https://www.github.com/Morlunk/Leeroy-Gradle/) script to your project. Projects using Leeroy-Gradle will automatically expose necessary fields in their AndroidManifest.xml for integration with Leeroy upon being built by Jenkins. See the project page for more details.

Apps using Leeroy
-----------------

- [Plumble](https://www.github.com/Morlunk/Plumble)

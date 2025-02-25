# VendorTools

VendorTools is a set of build scripts designed to make creating WPILib Vendor Dependencies easier. This replaces the [WPILib Vendor Template](https://github.com/wpilibsuite/vendor-template)'s `publish.gradle` script with a Gradle plugin, making it easier to configure and maintain Vendordeps, since you don't have to maintain the build system yourself.

## What works

* Java builds
* Publishing to a local Maven repository to be hosted over HTTP
* Filling in template values in a Vendordep JSON
* Combiner plugin (replaces [WPILib's Build Tools Combiner](https://github.com/wpilibsuite/build-tools/tree/master/combiner))
* CI workflows

## What doesn't work (yet)

* C++ builds
* JNI builds (probably)
* Python builds

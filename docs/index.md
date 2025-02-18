---
title: Home
layout: home
---

# VendorTools
{: .fs-9 }

A set of build tools designed to make creating Vendordeps for FRC easier.
{: .fs-6 .fw-300 }

---

VendorTools is a set of build tools designed to replace the build scripts in the [WPILib Vendor Template](https://github.com/wpilibsuite/vendor-template). By replacing the `publish.gradle` script with a proper Gradle plugin, dependency developers no longer need to maintain their dependencies' build systems theirselves - just import the plugin and configure it, and you're set! I'm also planning on providing some CI workflows (likely just for GitHub Actions), which you will be able to depend on, but I haven't quite gotten around to that yet.

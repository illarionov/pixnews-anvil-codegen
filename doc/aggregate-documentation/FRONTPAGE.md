# Pixnews Anvil-KSP Code Generators

Code generators designed for use with [Anvil-KSP] to simplify dependency injection in Android applications.  
They make it easier to create [Dagger] components and modules with the Anvil-KSP compiler plugin.

Primarily shared as practical examples of how to use Anvil-KSP generators.

Below is a list of all implemented helpers:

* __[@ContributesActivity]__  
  For injecting dependencies into Android Activities.
* __[@ContributesViewModel]__  
  For injecting dependencies into Android ViewModels.
* __[@ContributesCoroutineWorker]__  
  For injecting dependencies into Android WorkManager CoroutineWorkers.
* __[@ContributesTest]__  
  For injecting dependencies into JUnit4 Android instrumented tests.
* __[@ContributesInitializer]__  
  For merging initialization code fragments from submodules into the main application and executing it at application startup.
* __[@ContributesExperiment]__  
  For declaring feature flags in submodules and aggregating them in the main application.

## Installation

Release and snapshot versions of the library are published to a self-hosted public repository, as it's currently used in a single project.

Add the following to your project's settings.gradle:

```kotlin
pluginManagement {
    repositories {
        maven {
            url = uri("https://maven.pixnews.ru")
            mavenContent {
                includeGroup("ru.pixnews.anvil.ksp.codegen")
            }
        }
    }
}
```

The following type aliases are used in the examples:

```kotlin
typealias DaggerSet<T> = Set<@JvmSuppressWildcards T>
typealias DaggerMap<K, V> = Map<@JvmSuppressWildcards K, @JvmSuppressWildcards V>
```

## Other generator samples

Some other good reposotories with Anvil generators

* [https://github.com/IlyaGulya/anvil-utils](https://github.com/IlyaGulya/anvil-utils)  
  Annotation that helps to automatically generate assisted factories
* [https://github.com/deliveryhero/whetstone](https://github.com/deliveryhero/whetstone)  
  Whetstone, DI framework for Android that greatly simplifies working with Dagger 2 using Anvil
* [https://github.com/RBusarow/Tangle](https://github.com/RBusarow/Tangle)  
  Tangle, android injection using the Anvil compiler plugin
* [https://github.com/duckduckgo/Android](https://github.com/duckduckgo/Android/tree/develop/anvil/anvil-compiler/src/main/java/com/duckduckgo/anvil/compiler)  
  Generators from the DuckDuckGo Android App
* [https://github.com/SteinerOk/sealant/](https://github.com/SteinerOk/sealant/)  
  Some code generators
* [https://slackhq.github.io/circuit/code-gen/](https://slackhq.github.io/circuit/code-gen/)  
  Sample of the KSP-based code generator that works with Anvil, Hilt, kotlin-inject

## Contributing

Any type of contributions are welcome. Please see the [contribution guide].

## License

These services are licensed under Apache 2.0 License. Authors and contributors are listed in the
[Authors] file.

```
Copyright 2024-2025 pixnews-anvil.codegen project authors and contributors.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

[Anvil-KSP]: https://github.com/ZacSweers/anvil
[Dagger]: https://github.com/google/dagger
[contribution guide]: https://github.com/illarionov/pixnews-anvil-codegen/blob/main/CONTRIBUTING.md
[Authors]: https://github.com/illarionov/pixnews-anvil-codegen/blob/main/AUTHORS
[@ContributesActivity]: https://illarionov.github.io/pixnews-anvil-codegen/activity-generator/
[@ContributesViewModel]: https://illarionov.github.io/pixnews-anvil-codegen/viewmodel-generator/
[@ContributesCoroutineWorker]: https://illarionov.github.io/pixnews-anvil-codegen/workmanager-generator/
[@ContributesTest]: https://illarionov.github.io/pixnews-anvil-codegen/test-generator/
[@ContributesInitializer]: https://illarionov.github.io/pixnews-anvil-codegen/initializer-generator/
[@ContributesExperiment]: https://illarionov.github.io/pixnews-anvil-codegen/experiment-generator/

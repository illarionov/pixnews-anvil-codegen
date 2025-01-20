# Pixnews Anvil-KSP Code Generators

Code generators designed for use with [Anvil-KSP] to simplify dependency injection in Android applications.  
Primarily shared as practical examples of how to use Anvil-KSP generators.

Below is a list of all implemented helpers:

* __@ContributesActivity__  
  For injecting dependencies into Android Activities.
* __@ContributesViewModel__  
  For injecting dependencies into Android ViewModels.
* __@ContributesCoroutineWorker__  
  For injecting dependencies into Android WorkManager CoroutineWorkers.
* __@ContributesTest__  
  For injecting dependencies into JUnit4 Android instrumented tests.
* __@ContributesInitializer__  
  For merging initialization code fragments from submodules into the main application and executing it at application startup.
* __@ContributesExperiment__  
  For declaring feature flags in submodules and aggregating them in the main application.

For more information, visit the project website: [https://illarionov.github.io/pixnews-anvil-codegen](https://illarionov.github.io/pixnews-anvil-codegen)

[Anvil-ksp]: https://github.com/square/anvil
[Dagger]: https://github.com/google/dagger

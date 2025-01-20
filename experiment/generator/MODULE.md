# Module experiment-generator

A code generator for use with [Anvil-KSP] that simplifies injection of the A/B experiments 
with a _@ContributesExperiment_ / _@ContributesExperimentVariantSerializer_ annotations.

`ContributesExperiment` and `ContributesExperimentVariantSerializer` annotations used to simplify creation of feature flags in different modules and adding them to a feature manager.

## Code generator setup

Before using the generator, make sure [Anvil-KSP] is already configured in the project.

Then add the required dependencies:

```kotlin
dependencies {
    anvil("ru.pixnews.anvil.ksp.codegen:experiment-generator:0.1")
    // Add this dependency to the ksp classpath if "anvil" configuration doesn't work for some reason:
    ksp("ru.pixnews.anvil.ksp.codegen:experiment-generator:0.1")

    implementation("ru.pixnews.anvil.ksp.codegen:experiment-inject:0.1")
}
```

## Project setup

To make use of this generator's output, the project must have injection set up using Dagger multibindings.
The configuration might look something like this (using classes from the [experiment-inject] module).

Create component with experiments:

```kotlin
import ru.pixnews.anvil.ksp.codegen.experiment.inject.ExperimentScope

@SingleIn(ExperimentScope::class)
@MergeComponent(scope = ExperimentScope::class)
interface ExperimentsComponent {
    fun appExperiments(): DaggerSet<Experiment>

    fun appExperimentVariantSerializers(): DaggerMap<ExperimentKey, ExperimentVariantSerializer>

    companion object {
        operator fun invoke(): ExperimentsComponent = DaggerExperimentsComponent.create()
    }
}
```

Module:

```kotlin
import ru.pixnews.anvil.ksp.codegen.experiment.inject.ExperimentScope

@Module
@ContributesTo(ExperimentScope::class)
abstract class ExperimentsModule {
    @Multibinds
    abstract fun appExperiments(): DaggerSet<Experiment>

    @Multibinds
    abstract fun appExperimentSerializers(): DaggerMap<String, ExperimentVariantSerializer>
}
```

## Usage

Annotate experiment with `@ContributesExperiment` and serializer with `@ContributesExperimentSerializer`:

```kotlin
@ContributesExperiment
object DarkModeExperiment : Experiment {
    …

    @ContributesExperimentVariantSerializer("ui.dark_mode")
    object Serializer : BooleanVariantSerializer(…)

}
```

The following binding will be generated based on this annotation:

```kotlin
@Module
@ContributesTo(ExperimentScope::class)
object DarkModeExperiment_Experiments_Module {
  @Provides
  @IntoSet
  fun provideDarkModeExperiment(): Experiment = DarkModeExperiment

  @Provides
  @IntoMap
  @ExperimentVariantMapKey(key = "ui.dark_mode")
  fun provideSerializer(): ExperimentVariantSerializer = DarkModeExperiment.Serializer
}
```

[Anvil-KSP]: https://github.com/ZacSweers/anvil
[experiment-inject]: https://illarionov.github.io/pixnews-anvil-codegen/experiment-inject/

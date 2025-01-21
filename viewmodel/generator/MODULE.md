# Module viewmodel-generator

A code generator for use with [Anvil-KSP] that simplifies adding Android ViewModels to the DI graph 
with a single _@ContributesViewModel_ annotation.

For each [ViewModel] annotated with [ContributesViewModel], a Dagger module will be generated with
content similar to the following:

```kotlin
import ru.pixnews.anvil.ksp.codegen.viewmodel.inject.ViewModelScope
import ru.pixnews.anvil.ksp.codegen.viewmodel.inject.wiring.ViewModelFactory
import ru.pixnews.anvil.ksp.codegen.viewmodel.inject.wiring.ViewModelMapKey

@Module
@ContributesTo(ViewModelScope::class)
object MyFactoryModule {
    @Provides
    @IntoMap
    @ViewModelMapKey(MyViewModel::class)
    fun providesMyViewModelViewModelFactory(
        featureManager: FeatureManager,
        …
    ): ViewModelFactory = ViewModelFactory {
        MyViewModel(
            featureManager = featureManager,
            …
            savedStateHandle = it.createSavedStateHandle()
        )
    }
}
```

You can pass additional arguments to ViewModel in [DEFAULT_ARGS_KEY](https://developer.android.com/reference/kotlin/androidx/lifecycle/package-summary#DEFAULT_ARGS_KEY()) of CreationExtras.

This module is intended to add the specified ViewModel to the ViewModelScope multibinding.

## Code generator setup

Before using the generator, make sure [Anvil-KSP] is already configured in the project.

Then add the required dependencies:

```kotlin
dependencies {
    ksp("ru.pixnews.anvil.ksp.codegen:viewmodel-generator:0.1")

    // viewmodel-inject dependency is optional, all declarations this module can be overridden.
    implementation("ru.pixnews.anvil.ksp.codegen:viewmodel-inject:0.1")
}
```

The fully qualified names of annotation classes can be customized via KSP arguments in `build.gradle.kts`;

```kotlin
ksp {
    arg(
        "ru.pixnews.anvil.ksp.viewmodel.ContributesViewModel",
        "ru.pixnews.anvil.ksp.codegen.viewmodel.inject.ContributesViewModel"
    )
    arg(
        "ru.pixnews.anvil.ksp.viewmodel.ViewModelFactory",
        "ru.pixnews.anvil.ksp.codegen.viewmodel.inject.wiring.ViewModelFactory"
    )
    arg(
        "ru.pixnews.anvil.ksp.viewmodel.ViewModelMapKey",
        "ru.pixnews.anvil.ksp.codegen.viewmodel.inject.wiring.ViewModelMapKey"
    )
    arg(
        "ru.pixnews.anvil.ksp.viewmodel.ViewModelScope",
        "ru.pixnews.anvil.ksp.codegen.viewmodel.inject.ViewModelScope"
    )
}
```

By default, annotations from the [viewmodel-inject] module are used.

## Project setup

To make use of this generator's output, the project must have viewmodel injection set up using Dagger multibindings.
The configuration might look something like this (using classes from the [viewmodel-inject] module).

Create a subcomponent for ViewModels and add it to your application component,
as shown in the following example:

```kotlin
import ru.pixnews.anvil.ksp.codegen.viewmodel.inject.ViewModelScope
import ru.pixnews.anvil.ksp.codegen.viewmodel.inject.wiring.ViewModelFactory
import <your app scope>.AppScope

@ContributesSubcomponent(scope = ViewModelScope::class, parentScope = AppScope::class)
@SingleIn(ViewModelScope::class)
interface ViewModelSubcomponent {
    val viewModelMap: DaggerMap<Class<out ViewModel>, Provider<ViewModel>>
    val viewModelFactoryMap: DaggerMap<Class<out ViewModel>, ViewModelFactory>

    @ContributesSubcomponent.Factory
    fun interface Factory {
        fun create(@BindsInstance savedStateHandle: SavedStateHandle): ViewModelSubcomponent
    }

    @ContributesTo(AppScope::class)
    public interface ViewModelSubcomponentFactoryHolder {
        public fun getViewModelSubcomponentFactory(): ViewModelSubcomponent.Factory
        public fun getViewModelFactory(): ViewModelProvider.Factory
    }
}
```

Declaration of the multibindings in module:

```kotlin
import ru.pixnews.anvil.ksp.codegen.viewmodel.inject.ViewModelScope

@Module
@ContributesTo(ViewModelScope::class)
interface ViewModelModule {
    @Multibinds
    fun viewModelProviders(): DaggerMap<Class<out ViewModel>, ViewModel>

    @Multibinds
    fun viewModelFactoryProviders(): DaggerMap<Class<out ViewModel>, ViewModelFactory>
}
```

Create implementation of `ViewModelProvider.Factory` and add it to app scope:

```kotlin
import <your app scope>.AppScope
        
@Reusable
@ContributesBinding(AppScope::class, boundType = ViewModelProvider.Factory::class)
@RestrictTo(RestrictTo.Scope.LIBRARY)
class ViewModelProviderFactory(
    private val vmSubcomponentFactory: ViewModelSubcomponent.Factory,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val savedSateHandle = extras.createSavedStateHandle()
        val viewModelComponent = vmSubcomponentFactory.create(savedSateHandle)
        val viewModelMap = viewModelComponent.viewModelMap

        val viewModelProvider = viewModelMap[modelClass]
        if (viewModelProvider != null) {
            return viewModelProvider.get() as T
        } else {
            val factory = viewModelComponent.viewModelFactoryMap[modelClass]
                ?: error("No factory for ${modelClass.name}")
            return factory.create(extras) as T
        }
    }
}
```

## Usage

You can use created `getViewModelFactory()` of the application component as a view model factory.

```kotlin
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.CreationExtras

inline fun <reified VM : ViewModel> ComponentActivity.injectedViewModel(
    noinline extrasProducer: (() -> CreationExtras) = { defaultViewModelCreationExtras },
): Lazy<VM> = viewModels(extrasProducer, appComponent::viewModelFactory)

@Composable
fun <VM : ViewModel> injectedViewModel(
    modelClass: Class<VM>,
    viewModelStoreOwner: ViewModelStoreOwner = …,
    key: String? = null,
    extras: CreationExtras = …,
): VM = viewModel(
    modelClass,
    viewModelStoreOwner,
    key,
    appComponent.getViewModelFactory(),
    extras,
)
```

Annotate your ViewModel with `ContributesViewModel` to add it to the multibinding:

```kotlin
@ContributesViewModel
class MyViewModel(
    featureManager: FeatureManager,
    …
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    …
}
```

The ViewModelModule module described above will be generated based on this annotation.

[Anvil-KSP]: https://github.com/ZacSweers/anvil
[viewmodel-inject]: https://illarionov.github.io/pixnews-anvil-codegen/viewmodel-inject/
[ViewModel]: https://developer.android.com/reference/androidx/lifecycle/ViewModel

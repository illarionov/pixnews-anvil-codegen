/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.anvil.codegen.viewmodel.inject

/**
 * Annotate a ViewModel class with this to automatically contribute it to the ViewModel scope multibinding.
 * Equivalent to the following declaration in an application module:
 *```
 *   @Provides
 *   @IntoMap
 *   @ViewModelKey(MainViewModel::class)
 *   public fun providesMainViewModel(
 *       <arguments>
 *   ): ViewModelFactory = ViewModelFactory {
 *       MainViewModel(
 *           <arguments>
 *       )
 *   }
 *```
 * The generated code created via the anvil-codegen module.
 */
public annotation class ContributesViewModel

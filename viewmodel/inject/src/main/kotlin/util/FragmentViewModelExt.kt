/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("KDOC_WITHOUT_PARAM_TAG")

package ru.pixnews.anvil.ksp.codegen.viewmodel.inject.util

import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras

/**
 * [Fragment.viewModels] that uses application's [ViewModelProvider.Factory]
 */
@MainThread
public inline fun <reified VM : ViewModel> Fragment.injectedViewModel(
    noinline ownerProducer: () -> ViewModelStoreOwner = { this },
    noinline extrasProducer: (() -> CreationExtras)? = null,
): Lazy<VM> = viewModels(
    ownerProducer = ownerProducer,
    extrasProducer = extrasProducer,
    factoryProducer = RootViewModelFactoryHolder::factory,
)

/**
 * [Fragment.activityViewModels] that uses application's [ViewModelProvider.Factory]
 */
@MainThread
public inline fun <reified VM : ViewModel> Fragment.injectedActivityViewModel(
    noinline extrasProducer: (() -> CreationExtras)? = null,
): Lazy<VM> = activityViewModels(
    extrasProducer = extrasProducer,
    factoryProducer = RootViewModelFactoryHolder::factory,
)

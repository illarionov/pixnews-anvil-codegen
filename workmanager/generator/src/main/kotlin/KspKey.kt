/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.anvil.ksp.codegen.workmanager.generator

internal object KspKey {
    const val CONTRIBUTE_COROUTINE_WORKER = "ru.pixnews.anvil.ksp.workmanager.ContributesCoroutineWorker"
    const val APPLICATION_CONTEXT_QUALIFIER = "ru.pixnews.anvil.ksp.workmanager.ApplicationContext"
    const val COROUTINE_WORKER_FACTORY = "ru.pixnews.anvil.ksp.workmanager.CoroutineWorkerFactory"
    const val COROUTINE_WORKER_MAP_KEY = "ru.pixnews.anvil.ksp.workmanager.CoroutineWorkerMapKey"
    const val WORK_MANAGER_SCOPE = "ru.pixnews.anvil.ksp.workmanager.WorkManagerScope"
}

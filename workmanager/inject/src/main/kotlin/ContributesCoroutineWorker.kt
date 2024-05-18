/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.anvil.codegen.workmanager.inject

/**
 * Annotate a CoroutineWorker class with this to automatically contribute it to the WorkManagerScope multibinding.
 * Equivalent to the following declaration in an application module:
 *```
 * @ContributesMultibinding(scope = WorkManagerScope::class)
 * @AssistedFactory
 * public interface TestWorkerFactory : CoroutineWorkerFactory {
 *     override fun create(@ApplicationContext context: Context, workerParameters: WorkerParameters): TestWorker
 * }
 *```
 * The generated code created via the anvil-codegen module.
 */
public annotation class ContributesCoroutineWorker

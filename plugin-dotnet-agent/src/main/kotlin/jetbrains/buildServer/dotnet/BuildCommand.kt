/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineResult
import jetbrains.buildServer.agent.runner.ParametersService
import kotlin.coroutines.experimental.buildSequence

class BuildCommand(
        parametersService: ParametersService,
        private val _failedTestDetector: FailedTestDetector,
        private val _targetService: TargetService,
        private val _commonArgumentsProvider: DotnetCommonArgumentsProvider,
        private val _dotnetToolResolver: DotnetToolResolver,
        private val _vstestLoggerEnvironment: EnvironmentBuilder)
    : DotnetCommandBase(parametersService) {

    override val commandType: DotnetCommandType
        get() = DotnetCommandType.Build

    override val toolResolver: ToolResolver
        get() = _dotnetToolResolver

    override val targetArguments: Sequence<TargetArguments>
        get() = _targetService.targets.map { TargetArguments(sequenceOf(CommandLineArgument(it.targetFile.path))) }

    override val arguments: Sequence<CommandLineArgument>
        get() = buildSequence {
            parameters(DotnetConstants.PARAM_FRAMEWORK)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--framework"))
                    yield(CommandLineArgument(it))
                }
            }

            parameters(DotnetConstants.PARAM_CONFIG)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--configuration"))
                    yield(CommandLineArgument(it))
                }
            }

            parameters(DotnetConstants.PARAM_RUNTIME)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--runtime"))
                    yield(CommandLineArgument(it))
                }
            }

            parameters(DotnetConstants.PARAM_OUTPUT_DIR)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--output"))
                    yield(CommandLineArgument(it))
                }
            }

            parameters(DotnetConstants.PARAM_VERSION_SUFFIX)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--version-suffix"))
                    yield(CommandLineArgument(it))
                }
            }

            yieldAll(_commonArgumentsProvider.arguments)
        }

    override fun isSuccessful(result: CommandLineResult) =
            result.exitCode == 0 || (result.exitCode > 0 && result.standardOutput.map { _failedTestDetector.hasFailedTest(it) }.filter { it }.any())

    override val environmentBuilders: Sequence<EnvironmentBuilder>
        get() = buildSequence { yield(_vstestLoggerEnvironment) }
}
package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.runners.CommandLineArgument
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class PublishCommandTest {
    @DataProvider
    fun testPublishArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_PATHS, "path/")),
                        listOf("customArg1")),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_PUBLISH_FRAMEWORK, "dotcore"),
                        Pair(DotnetConstants.PARAM_PUBLISH_CONFIG, "Release")),
                        listOf("--framework", "dotcore", "--configuration", "Release", "customArg1")),
                arrayOf(mapOf(
                        DotnetConstants.PARAM_PUBLISH_RUNTIME to " active"),
                        listOf("--runtime", "active", "customArg1")),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_PUBLISH_OUTPUT, "out"),
                        Pair(DotnetConstants.PARAM_PUBLISH_CONFIG, "Release")),
                        listOf("--configuration", "Release", "--output", "out", "customArg1")),
                arrayOf(mapOf(
                        DotnetConstants.PARAM_PUBLISH_OUTPUT to "c:\\build\\out",
                        DotnetConstants.PARAM_PATHS to "project.csproj",
                        DotnetConstants.PARAM_PUBLISH_CONFIG to "Release"),
                        listOf("--configuration", "Release", "--output", "c:\\build\\out", "customArg1"))
        )
    }

    @Test(dataProvider = "testPublishArgumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>) {
        // Given
        val command = createCommand(parameters=parameters, targets = sequenceOf("my.csproj"), arguments = sequenceOf(CommandLineArgument("customArg1")))

        // When
        val actualArguments = command.specificArguments.map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }

    @DataProvider
    fun projectsArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(listOf<String>("my.csproj") as Any, listOf<List<String>>(listOf<String>("my.csproj"))),
                arrayOf(emptyList<String>() as Any, emptyList<List<String>>()),
                arrayOf(listOf<String>("my.csproj", "my2.csproj") as Any, listOf<List<String>>(listOf<String>("my.csproj"), listOf<String>("my2.csproj"))))
    }

    @Test(dataProvider = "projectsArgumentsData")
    fun shouldProvideProjectsArguments(targets: List<String>, expectedArguments: List<List<String>>) {
        // Given
        val command = createCommand(targets = targets.asSequence())

        // When
        val actualArguments = command.targetArguments.map { it.arguments.map { it.value }.toList() }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }

    @Test
    fun shouldProvideCommandType() {
        // Given
        val command = createCommand()

        // When
        val actualCommand = command.commandType

        // Then
        Assert.assertEquals(actualCommand, DotnetCommandType.Publish)
    }

    @DataProvider
    fun checkSuccessData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(0, true),
                arrayOf(1, false),
                arrayOf(99, false),
                arrayOf(-1, false),
                arrayOf(-99, false))
    }

    @Test(dataProvider = "checkSuccessData")
    fun shouldImplementCheckSuccess(exitCode: Int, expectedResult: Boolean) {
        // Given
        val command = createCommand()

        // When
        val actualResult = command.isSuccess(exitCode)

        // Then
        Assert.assertEquals(actualResult, expectedResult)
    }

    fun createCommand(
            parameters: Map<String, String> = emptyMap(),
            targets: Sequence<String> = emptySequence(),
            arguments: Sequence<CommandLineArgument> = emptySequence()): DotnetCommand =
            PublishCommand(
                    ParametersServiceStub(parameters),
                    TargetServiceStub(targets.map { CommandTarget(File(it)) }.asSequence()),
                    DotnetCommonArgumentsProviderStub(arguments))
}
# [Gradle profiler](https://github.com/gradle/gradle-profiler)

This folder contains the Gradle profiler instructions, different scenarios and  results of their runs.

## Installing gradle-profiler

[Install](https://github.com/gradle/gradle-profiler#installing) gradle-profiler. There are a few options, one of them is `brew`

```shell
brew install gradle-profiler
```

## 2023 06 Kotlin K2 compiler

Enabling a new Kotlin K2 compiler.

- [Scenarios](./scenarios/2023-06-kotlin-k2-compiler/2023-06-k2.scenarios)
- [Results](./results/2023-06-kotlin-k2-compiler)

Compare run of the `buildDebug` task on two different branches.

```shell
gradle-profiler --benchmark \
--project-dir ./ \
--scenario-file gradle/gradle-profiler/scenarios/2023-06-kotlin-k2-compiler/2023-06-k2.scenarios \
--output-dir gradle/gradle-profiler/results/2023-06-kotlin-k2-compiler/master \
clean-buildDebug-master && \
gradle-profiler --benchmark \
--project-dir ./ \
--scenario-file gradle/gradle-profiler/scenarios/2023-06-kotlin-k2-compiler/2023-06-k2.scenarios \
--output-dir gradle/gradle-profiler/results/2023-06-kotlin-k2-compiler/k2 \
clean-buildDebug-k2
```

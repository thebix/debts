package common

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project

/**
 * Configure base Kotlin with Android options
 */
internal fun Project.configureLint(
    commonExtension: CommonExtension<*, *, *, *, *>,
) {
    commonExtension.apply {
        lint {
            /*
             * TODO:
             * 1. abortOnError = true
             * 2. run `./gradlew lintDebug`
             * 3. Fix issues
             */
            abortOnError = false
        }
    }
}

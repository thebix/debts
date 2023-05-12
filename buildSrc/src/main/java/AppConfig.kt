object AppConfig {

    private val major = 2
    private val minor = 5
    private val patch = 0
    private val buildId = System.getenv("ENV_BUILD_ID")?.toIntOrNull() ?: 0

    const val applicationId = "net.thebix.debts"

    object Version {

        val code = major * 1000000 + minor * 10000 + patch * 1000 + buildId
        val name = "$major.$minor.$patch.$buildId"
    }

}

object AndroidConfig {

    const val compileSdkVersion = 33
    const val minSdkVersion = 21
    const val targetSdkVersion = 33
}

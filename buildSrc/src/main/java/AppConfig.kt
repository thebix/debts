@file:Suppress("MagicNumber")

object AppConfig {

    private const val major = 2
    private const val minor = 6
    private const val patch = 0
    private val buildId = System.getenv("ENV_BUILD_ID")?.toIntOrNull() ?: 0

    const val applicationId = "net.thebix.debts"

    object Version {

        val code = major * 1000000 + minor * 10000 + patch * 1000 + buildId
        val name = "$major.$minor.$patch.$buildId"
    }
}

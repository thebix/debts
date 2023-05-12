import org.gradle.api.Project
import java.io.File

data class Credentials(
    val storeKeyAlias: String,
    val storeKeyAliasPassword: String,
    val storeKeyFile: String,
    val storeKeyPassword: String,
)

fun Project.credentials(): Credentials {
    val localDebtsCredentialsFile = "${project.rootDir}/private/debts_credentials.properties"
    val configuration: Map<String, String> = if (File(localDebtsCredentialsFile).exists()) {
        File(localDebtsCredentialsFile).useLines { lines ->
            lines.map { line ->
                val (key, value) = line.split("=")
                key to value
            }.toMap()
        }
    } else {
        emptyMap()
    }

    @Suppress("ThrowsCount")
    return Credentials(
        storeKeyAlias = configuration["DEBTS_STORE_KEY_ALIAS"] ?: System.getenv("DEBTS_STORE_KEY_ALIAS")
        ?: throw IllegalArgumentException("DEBTS_STORE_KEY_ALIAS is not set"),
        storeKeyAliasPassword = configuration["DEBTS_STORE_KEY_ALIAS_PASSWORD"] ?: System.getenv("DEBTS_STORE_KEY_ALIAS_PASSWORD")
        ?: throw IllegalArgumentException("DEBTS_STORE_KEY_ALIAS_PASSWORD is not set"),
        storeKeyFile = configuration["DEBTS_STORE_KEY_FILE"] ?: System.getenv("DEBTS_STORE_KEY_FILE")
        ?: throw IllegalArgumentException("DEBTS_STORE_KEY_FILE is not set"),
        storeKeyPassword = configuration["DEBTS_STORE_KEY_PASSWORD"] ?: System.getenv("DEBTS_STORE_KEY_PASSWORD")
        ?: throw IllegalArgumentException("DEBTS_STORE_KEY_PASSWORD is not set"),
    )
}

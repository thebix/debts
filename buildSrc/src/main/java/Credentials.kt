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
    } else emptyMap()

    return Credentials(
        storeKeyAlias = configuration["DEBTS_STORE_KEY_ALIAS"] ?: System.getenv("DEBTS_STORE_KEY_ALIAS"),
        storeKeyAliasPassword = configuration["DEBTS_STORE_KEY_ALIAS_PASSWORD"] ?: System.getenv("DEBTS_STORE_KEY_ALIAS_PASSWORD"),
        storeKeyFile = configuration["DEBTS_STORE_KEY_FILE"] ?: System.getenv("DEBTS_STORE_KEY_FILE"),
        storeKeyPassword = configuration["DEBTS_STORE_KEY_PASSWORD"] ?: System.getenv("DEBTS_STORE_KEY_PASSWORD"),
    )
}

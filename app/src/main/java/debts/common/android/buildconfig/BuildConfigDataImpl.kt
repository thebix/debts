package debts.common.android.buildconfig

import debts.core.common.android.buildconfig.BuildConfigData
import net.thebix.debts.BuildConfig

class BuildConfigDataImpl : BuildConfigData {

    override fun isDebug(): Boolean = BuildConfig.DEBUG

    override fun getApplicationId(): String = BuildConfig.APPLICATION_ID

    override fun getBuildType(): String = BuildConfig.BUILD_TYPE

    override fun getVersionCode(): Int = BuildConfig.VERSION_CODE

    override fun getVersionName(): String = BuildConfig.VERSION_NAME
}

package lib.toolkit.base.managers.tools

import android.content.Context
import android.net.Uri
import android.text.Spanned
import androidx.annotation.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import lib.toolkit.base.managers.utils.ContentResolverUtils
import lib.toolkit.base.managers.utils.StringUtils
import java.io.File
import javax.inject.Inject

class ResourcesProvider @Inject constructor(val context: Context) {

    fun getString(@StringRes res: Int) = context.getString(res)

    fun getString(@StringRes res: Int, vararg args: String) = context.getString(res, args)

    fun getStringArray(@ArrayRes res: Int): Array<String> = context.resources.getStringArray(res)

    fun getDrawable(@DrawableRes res: Int) = AppCompatResources.getDrawable(context, res)

    fun getColor(@ColorRes res: Int) = ContextCompat.getColor(context, res)

    fun getDimen(@DimenRes res: Int) = context.resources.getDimension(res)

    fun getCacheDir(isExternal: Boolean): File? {
        return if (isExternal) {
            context.externalCacheDir
        } else {
            context.cacheDir
        }
    }

    fun getLocale(): String? {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0].country
        } else {
            context.resources.configuration.locale.country
        }
    }

    fun getFileNameByUri(uri: Uri): String = ContentResolverUtils.getName(context, uri)

    fun getHtmlSpanned(@StringRes resource: Int, vararg args: String): Spanned {
        return StringUtils.getHtmlSpanned(
            context.getString(
                resource,
                *args
            )
        )
    }

}

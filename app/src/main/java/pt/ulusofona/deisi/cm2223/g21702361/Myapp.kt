package pt.ulusofona.deisi.cm2223.g21702361

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import java.util.*

class MyApp : Application() {

    private val supportedLocales = setOf(
        Locale("en"), // English
        Locale("pt", "PT"), // Portuguese (Portugal)
        Locale("es", "ES") // Spanish (Spain)
    )


    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(updateBaseContextLocale(base))
    }

    private fun updateBaseContextLocale(context: Context): Context {
        val locale = getPreferredLocale(context) // Define this function to determine preferred locale
        val config = Configuration(context.resources.configuration)
        Locale.setDefault(locale)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    private fun getPreferredLocale(context: Context): Locale {
        val systemLocale = Locale.getDefault()
        // Check if the user-selected locale is supported, and fallback to default if not
        return when {
            supportedLocales.contains(systemLocale) -> systemLocale
            else -> Locale.getDefault()
        }
    }
}

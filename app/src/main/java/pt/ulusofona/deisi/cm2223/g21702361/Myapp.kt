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


    override fun attachBaseContext(baseLanguage: Context) {
        super.attachBaseContext(updateLocalLanguage(baseLanguage))
    }

    private fun updateLocalLanguage(context: Context): Context {
        val chooseLocalLanguage = getPreferedLanguage(context) // Define this function to determine preferred local language
        val configureLanguage = Configuration(context.resources.configuration)
        Locale.setDefault(chooseLocalLanguage)
        configureLanguage.setLocale(chooseLocalLanguage)
        return context.createConfigurationContext(configureLanguage)
    }

    private fun getPreferedLanguage(context: Context): Locale {
        val systemLanguage = Locale.getDefault()
        // Check if the user-selected locale is supported, and fallback to default if not
        return when {
            supportedLocales.contains(systemLanguage) -> systemLanguage
            else -> Locale.getDefault()
        }
    }
}

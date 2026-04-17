package com.example.mobilo4ka.utils

import android.content.Context
import android.content.res.Configuration
import com.example.mobilo4ka.ui.main.Language
import java.util.Locale

object LocaleHelper {
    fun updateLocale(context: Context, language: Language): Context {
        val langCode = if (language == Language.EN) "en" else "ru"
        val locale = Locale(langCode)

        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }
}
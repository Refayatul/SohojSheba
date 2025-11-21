package com.bonfire.shohojsheba.data.database

import androidx.room.TypeConverter
import com.bonfire.shohojsheba.data.remote.LocalizedString

class Converters {
    private val separator = "|||"

    @TypeConverter
    fun fromLocalizedString(localizedString: LocalizedString): String {
        return "${localizedString.en}$separator${localizedString.bn}"
    }

    @TypeConverter
    fun toLocalizedString(data: String): LocalizedString {
        val parts = data.split(separator)
        return if (parts.size == 2) {
            LocalizedString(parts[0], parts[1])
        } else {
            LocalizedString(data, data) // Fallback
        }
    }
}

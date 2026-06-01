package dev.andrei.app_frontend.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class StringListConverter {
    @TypeConverter
    fun fromList(list: List<String>): String = Json.encodeToString(list)

    @TypeConverter
    fun toList(json: String): List<String> = Json.decodeFromString(json)
}

package dev.andrei.app_frontend.data.local

import androidx.room.TypeConverter
import dev.andrei.app_frontend.data.remote.dto.AttributeDto
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AttributeListConverter {
    @TypeConverter
    fun fromList(list: List<AttributeDto>): String = Json.encodeToString(list)

    @TypeConverter
    fun toList(json: String): List<AttributeDto> = Json.decodeFromString(json)
}

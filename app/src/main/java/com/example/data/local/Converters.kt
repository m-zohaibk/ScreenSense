package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.ActionEffort
import com.example.data.model.AppCategory
import com.example.data.model.ConfidenceLevel
import com.example.data.model.WellnessGoal

class Converters {
    @TypeConverter
    fun fromWellnessGoal(goal: WellnessGoal?): String? = goal?.name

    @TypeConverter
    fun toWellnessGoal(name: String?): WellnessGoal? =
        name?.let { runCatching { WellnessGoal.valueOf(it) }.getOrNull() }

    @TypeConverter
    fun fromAppCategory(category: AppCategory?): String? = category?.name

    @TypeConverter
    fun toAppCategory(name: String?): AppCategory? =
        name?.let { AppCategory.fromLegacy(it) } ?: AppCategory.UNKNOWN

    @TypeConverter
    fun fromConfidenceLevel(level: ConfidenceLevel?): String? = level?.name

    @TypeConverter
    fun toConfidenceLevel(name: String?): ConfidenceLevel? =
        name?.let { runCatching { ConfidenceLevel.valueOf(it) }.getOrNull() } ?: ConfidenceLevel.HIGH

    @TypeConverter
    fun fromActionEffort(effort: ActionEffort?): String? = effort?.name

    @TypeConverter
    fun toActionEffort(name: String?): ActionEffort? =
        name?.let { runCatching { ActionEffort.valueOf(it) }.getOrNull() } ?: ActionEffort.TINY

    @TypeConverter
    fun fromStringList(list: List<String>?): String? = list?.joinToString("|||")

    @TypeConverter
    fun toStringList(data: String?): List<String>? =
        if (data.isNullOrEmpty()) emptyList() else data.split("|||")
}

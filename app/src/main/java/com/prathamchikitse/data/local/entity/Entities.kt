package com.prathamchikitse.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

/**
 * Room database entities mirroring domain models.
 */

class ListConverter {
    @TypeConverter fun fromList(list: List<String>): String = list.joinToString("||")
    @TypeConverter fun toList(str: String): List<String> =
        if (str.isBlank()) emptyList() else str.split("||")
}

@Entity(tableName = "emergencies")
data class EmergencyEntity(
    @PrimaryKey val id: String,
    val titleEn: String,
    val titleKn: String,
    val descriptionEn: String,
    val descriptionKn: String,
    val category: String,
    val iconName: String,
    val keywordsEn: String = "",
    val keywordsKn: String = "",
    val callEmergency: Boolean
)

@Entity(
    tableName = "steps",
    foreignKeys = [ForeignKey(
        entity = EmergencyEntity::class,
        parentColumns = ["id"],
        childColumns = ["emergencyId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("emergencyId")]
)
@TypeConverters(ListConverter::class)
data class StepEntity(
    @PrimaryKey(autoGenerate = true) val stepId: Long = 0,
    val emergencyId: String,
    val stepNumber: Int,
    val titleEn: String,
    val titleKn: String,
    val instructionEn: String,
    val instructionKn: String,
    val dos: List<String>,
    val donts: List<String>,
    val dosKn: List<String>,
    val dontsKn: List<String>,
    val audioScriptEn: String,
    val audioScriptKn: String
)

@Entity(tableName = "hospitals")
data class HospitalEntity(
    @PrimaryKey val id: String,
    val name: String,
    val nameKn: String,
    val address: String,
    val district: String,
    val phone: String,
    val lat: Double,
    val lng: Double
)

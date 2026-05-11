package com.prathamchikitse.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Room relation POJO — Emergency with all its steps.
 */
data class EmergencyWithSteps(
    @Embedded val emergency: EmergencyEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "emergencyId"
    )
    val steps: List<StepEntity>
)

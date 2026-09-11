package com.example.mygymoffline.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.ColumnInfo

@Entity(
    tableName = "exercises",
    indices = [
        Index(value = ["category"]),
        Index(value = ["equipment"]),
        Index(value = ["is_favorite"]),
        Index(value = ["is_disliked"]),
        Index(value = ["target"]),
    ]
)
data class Exercise(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,           // body_part: waist, chest, back, etc.
    val equipment: String,          // body weight, dumbbell, barbell, etc.
    val target: String,             // primary target muscle
    val muscleGroup: String,        // primary synergist muscle group
    val secondaryMusclesJson: String, // JSON array of secondary muscles
    val instructionsJson: String,   // Map<String, String> language -> instructions
    val instructionStepsJson: String, // Map<String, List<String>> language -> steps
    val imagePath: String,          // assets/images/xxx.jpg
    val gifPath: String,            // assets/videos/xxx.gif or internal cache path
    val mediaId: String,
    val attribution: String,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false,
    @ColumnInfo(name = "is_disliked") val isDisliked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
package com.example.mygymoffline.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<Exercise>)

    @Update
    suspend fun update(exercise: Exercise)

    @Update
    suspend fun updateAll(exercises: List<Exercise>)

    @Query("SELECT * FROM exercises WHERE category = :category ORDER BY CASE WHEN is_favorite = 1 THEN 0 WHEN is_disliked = 1 THEN 2 ELSE 1 END, name ASC")
    fun getByCategory(category: String): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE category = :category AND equipment IN (:equipmentList) ORDER BY CASE WHEN is_favorite = 1 THEN 0 WHEN is_disliked = 1 THEN 2 ELSE 1 END, name ASC")
    fun getByCategoryAndEquipment(category: String, equipmentList: List<String>): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE is_favorite = 1 ORDER BY name ASC")
    fun getFavorites(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE is_favorite = 1 AND category = :category ORDER BY name ASC")
    fun getFavoritesByCategory(category: String): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' ORDER BY CASE WHEN is_favorite = 1 THEN 0 WHEN is_disliked = 1 THEN 2 ELSE 1 END, name ASC")
    fun searchByName(query: String): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' AND equipment IN (:equipmentList) ORDER BY CASE WHEN is_favorite = 1 THEN 0 WHEN is_disliked = 1 THEN 2 ELSE 1 END, name ASC")
    fun searchByNameAndEquipment(query: String, equipmentList: List<String>): Flow<List<Exercise>>

    @Query("SELECT DISTINCT category FROM exercises ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT DISTINCT equipment FROM exercises ORDER BY equipment ASC")
    fun getAllEquipment(): Flow<List<String>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: String): Exercise?

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(*) FROM exercises WHERE category = :category")
    suspend fun getCountByCategory(category: String): Int
}
package uz.alien.dictup.word


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction


@Dao
interface WordDao {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun export(words: List<Word>)
    @Query("SELECT * FROM word_table")
    fun load(): List<Word>
    @Query("UPDATE word_table SET level = level + :value WHERE id = :id")
    fun update(id: Int, value: Int)
}
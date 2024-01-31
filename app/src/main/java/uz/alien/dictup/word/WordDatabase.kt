package uz.alien.dictup.word


import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [Word::class], version = 3)
abstract class WordDatabase : RoomDatabase() {
    abstract fun getDatabase(): WordDao
}
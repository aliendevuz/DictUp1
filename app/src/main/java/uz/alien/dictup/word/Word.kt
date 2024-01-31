package uz.alien.dictup.word


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "word_table")
data class Word(

    @PrimaryKey
    val id: Int,

    @ColumnInfo(name = "english")
    val en: String,

    @ColumnInfo(name = "uzbek")
    val uz: String,

    @ColumnInfo(name = "transcript")
    val transcript: String,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "describe")
    val describe: String,

    @ColumnInfo(name = "sample")
    val sample: String,

    @ColumnInfo(name = "level")
    var level: Int = 0
)
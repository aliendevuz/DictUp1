package uz.alien.dictup


import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.room.Room
import uz.alien.dictup.word.Word
import uz.alien.dictup.word.WordDao
import uz.alien.dictup.word.WordDatabase
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale
import java.util.concurrent.Executors


class App : Application() {

    override fun onCreate() {
        super.onCreate()
        app = this
        soundPool.load(this, R.raw.correct, 1)
        soundPool.load(this, R.raw.incorrect, 1)
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        textToSpeech = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) textToSpeech.language = Locale.ENGLISH
        }
        textToSpeech.speak("  ", TextToSpeech.QUEUE_FLUSH, null, null)
        preferences = app.getSharedPreferences("DictUp", Context.MODE_PRIVATE)
        database = Room.databaseBuilder(applicationContext, WordDatabase::class.java, "app_db")
            .fallbackToDestructiveMigration().build().getDatabase()
        export()
    }

    fun onExported() {
        Log.d("@@@@", "exported!")
        load()
    }

    fun onLoaded() {
        Log.d("@@@@", "loaded!")
        Log.d("@@@@", words[0].en)
        Log.d("@@@@", "${words.size}")
        Log.d("@@@@", "${words[0].level}")
    }

    fun onUpdated(id: Int) {
        Log.d("@@@@", "$id word is updated - ${words[id].level}")
    }

    companion object {

        var bookNumber = 0
        var unitNumber = 0
        val testUnits = ArrayList<Int>()
        var testCount = 20

        @SuppressLint("StaticFieldLeak")
        lateinit var home: ActivityHome
        lateinit var app: App
        lateinit var database: WordDao
        lateinit var preferences: SharedPreferences
        lateinit var textToSpeech: TextToSpeech
        lateinit var vibrator: Vibrator
        val executor = Executors.newSingleThreadExecutor()!!
        val handler = Handler(Looper.getMainLooper())
        val soundPool = SoundPool.Builder().build()!!
        var words = ArrayList<Word>()
        var stories = ArrayList<String>()

        fun getTag(tag: String, word: String): String =
            word.subSequence(word.indexOf("<$tag>") + "<$tag>".length, word.indexOf("</$tag>"))
                .toString()

        fun saveValue(name: String, value: Any) {
            when (value) {
                is Int -> {
                    preferences.edit().putInt(name, value).apply()
                }

                is Long -> {
                    preferences.edit().putLong(name, value).apply()
                }

                is Float -> {
                    preferences.edit().putFloat(name, value).apply()
                }

                is String -> {
                    preferences.edit().putString(name, value).apply()
                }

                is Boolean -> {
                    preferences.edit().putBoolean(name, value).apply()
                }
            }
        }

        fun getInt(name: String, default: Int = 0) = preferences.getInt(name, default)

        fun getLong(name: String, default: Long = 0L) = preferences.getLong(name, default)

        fun getFloat(name: String, default: Float = 0.0f) = preferences.getFloat(name, default)

        fun getString(name: String, default: String = "") = preferences.getString(name, default)

        fun getBoolean(name: String, default: Boolean = false) =
            preferences.getBoolean(name, default)

        fun export() {
            val inputStreamS = app.assets.open("data/stories.txt")
            val readerS = BufferedReader(InputStreamReader(inputStreamS))
            val builderS = StringBuilder()
            var lineS: String?
            while (readerS.readLine().apply { lineS = this } != null) builderS.append(lineS)
                .append("\n")
            readerS.close()
            inputStreamS.close()
            stories = builderS.toString().split("@@@@@") as ArrayList<String>
            val inputStream = app.assets.open("data/words.txt")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val builder = StringBuilder()
            var line: String?
            while (reader.readLine().apply { line = this } != null) builder.append(line)
                .append("\n")
            reader.close()
            inputStream.close()
            val words = builder.toString().split("\n")
            val wordList = ArrayList<Word>()
            for (i in 0 until 3600) wordList.add(
                Word(
                    i,
                    getTag("EN", words[i]),
                    getTag("UZ", words[i]),
                    getTag("TR", words[i]),
                    getTag("TY", words[i]),
                    getTag("DC", words[i]),
                    getTag("SP", words[i])
                )
            )
            executor.execute {
                database.export(wordList)
                handler.postDelayed({
                    executor.execute {
                        if (database.load().size == 3600) app.onExported() else {
                            Log.d("@@@@", "WARNING! Qayta export qilishga urinish...")
                            export()
                        }
                    }
                }, 100L)
            }
        }

        fun load() {
            executor.execute {
                words.clear()
                val words = database.load() as ArrayList<Word>
                if (words.isEmpty()) Log.d(
                    "@@@@",
                    "WARNING! Word list database ga export bo'lmay qolgan..."
                )
                else {
                    App.words = words
                    app.onLoaded()
                }
            }
        }

        fun update(id: Int, value: Int) {
            executor.execute {
                words[id].level += value
                database.update(id, value)
                app.onUpdated(id)
            }
        }
    }
}
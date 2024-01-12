package uz.alien.dictup

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.room.Room
import uz.alien.dictup.adapter.AdapterVariant
import uz.alien.dictup.word.LevelComparator
import uz.alien.dictup.word.WordDatabase
import uz.alien.dictup.word.Word
import uz.alien.dictup.word.WordDao
import uz.alien.dictup.word.WordLang
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Collections
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.random.Random

class App : Application() {

    fun onExportedListener() {
        Log.d("@@@@", "exported!")
        load()
    }

    fun onLoadedListener() {
        Log.d("@@@@", "loaded!")
        Log.d("@@@@", words[0].en)
        Log.d("@@@@", "${words.size}")
        Log.d("@@@@", "${words[0].level}")
    }

    fun onUpdatedListener(id: Int) {
        Log.d("@@@@", "$id word is updated - ${words[id].level}")
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        textToSpeech = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) textToSpeech.language = Locale.ENGLISH
        }
        textToSpeech.speak("  ", TextToSpeech.QUEUE_FLUSH, null, null)
        preferences = app.getSharedPreferences("DictUp", Context.MODE_PRIVATE)
        database = Room.databaseBuilder(applicationContext, WordDatabase::class.java, "app_db").fallbackToDestructiveMigration().build().getDatabase()

        export()
    }

    companion object {

        @SuppressLint("StaticFieldLeak")
        lateinit var home: ActivityHome
        lateinit var app: App
        lateinit var database: WordDao
        lateinit var preferences: SharedPreferences
        lateinit var textToSpeech: TextToSpeech

        val executor = Executors.newSingleThreadExecutor()!!
        val handler = Handler(Looper.getMainLooper())

        lateinit var adapter: AdapterVariant

        var words = ArrayList<Word>()

        val testUnits = ArrayList<Int>()

        val testWords = ArrayList<Word>()

        val variants = ArrayList<Word>()

        lateinit var correctAnswer: Word

        var bookNumber = 0
        var unitNumber = 0

        var variantCount = 8
        var testLanguage = WordLang.RANDOM

        var currentTest = 0
        var currentLanguage = true

        var testCount = 5
        var isCheckable = true


        fun getTag(tag: String, word: String): String = word.subSequence(word.indexOf("<$tag>") + "<$tag>".length, word.indexOf("</$tag>")).toString()

        fun saveValue(name: String, value: Any) {
            when (value) {
                is Int -> { preferences.edit().putInt(name, value).apply() }
                is Long -> { preferences.edit().putLong(name, value).apply() }
                is Float -> { preferences.edit().putFloat(name, value).apply() }
                is String -> { preferences.edit().putString(name, value).apply() }
                is Boolean -> { preferences.edit().putBoolean(name, value).apply() }
            }
        }

        fun getInt(name: String, default: Int = 0) = preferences.getInt(name, default)

        fun getLong(name: String, default: Long = 0L) = preferences.getLong(name, default)

        fun getFloat(name: String, default: Float = 0.0f) = preferences.getFloat(name, default)

        fun getString(name: String, default: String = "") = preferences.getString(name, default)

        fun getBoolean(name: String, default: Boolean = false) = preferences.getBoolean(name, default)


        fun export() {
            if (!getBoolean("exported")) {
                val inputStream = app.assets.open("words.txt")
                val reader = BufferedReader(InputStreamReader(inputStream))
                val builder = StringBuilder()
                var line: String?
                while (reader.readLine().apply { line = this } != null) builder.append(line).append("\n")
                reader.close()
                inputStream.close()
                val words = builder.toString().split("\n")
                val wordList = ArrayList<Word>()
                for (i in 0 until 3600) wordList.add(Word(i, getTag("EN", words[i]), getTag("UZ", words[i]), getTag("TR", words[i]), getTag("TY", words[i]), getTag("DC", words[i]), getTag("SP", words[i])))
                executor.execute {
                    database.export(wordList)
                    handler.post {
                        saveValue("exported", true)
                        app.onExportedListener()
                    }
                }
            } else app.onExportedListener()
        }

        fun load() {
            executor.execute {
                words.clear()
                val words = database.load() as ArrayList<Word>
                if (words.isEmpty()) Log.d("@@@@", "Word list database ga export bo'lmay qolgan!!!") else App.words = words
                app.onLoadedListener()
            }
        }

        fun update(id: Int, value: Int) {
            executor.execute {
                words[id].level += value
                database.update(id, value)
                app.onUpdatedListener(id)
            }
        }


        fun next() {

            if (currentTest >= testCount) Toast.makeText(home, "Testlar tugadi", Toast.LENGTH_SHORT).show()

            else home.openUnbackableFrame(home.binding.pageTest) {

                currentLanguage = when (testLanguage) {
                    WordLang.EN_TO_UZ -> true
                    WordLang.UZ_TO_EN -> false
                    WordLang.RANDOM -> Random.nextInt(100) % 2 == 0
                }

                isCheckable = true

                variants.clear()
                val select = ArrayList<Word>()
                select.addAll(testWords)
                correctAnswer = select.removeAt(currentTest)
                variants.add(correctAnswer)

                home.binding.tvAsk.text = "\n${currentTest + 1}. ${if (currentLanguage) correctAnswer.en else correctAnswer.uz}'ning tarjimasini toping:"

                select.shuffle()
                for (i in 1 until variantCount) variants.add(select.removeAt(0))
                variants.shuffle()

                for (i in 0 until variantCount) {
                    val variant = if (currentLanguage) variants[i].uz else variants[i].en

                    for (i in 0 until variantCount) adapter.setColor(i, app.getColor(R.color.back_color))

                    adapter.setVariant(i, "$variant")
                }
            }
        }

        fun check(ans: Int) {

            if (isCheckable) if (ans in 0 until variantCount)

                if ((if (currentLanguage) variants[ans].uz else variants[ans].en) == (if (currentLanguage) correctAnswer.uz else correctAnswer.en)) {
                    adapter.setColor(ans, app.getColor(R.color.correct_answer))
                    update(correctAnswer.id, 1)
                    isCheckable = false
                } else {
                    adapter.setColor(ans, app.getColor(R.color.incorrect_answer))
                    update(correctAnswer.id, -1)
                }
        }

        fun createTestList() {
            currentTest = 0
            testWords.clear()
            for (unit in testUnits) for (i in 0..19) testWords.add(words[i + 20 * unit])
            testWords.shuffle()
        }

        fun createLowLevelTest(limit: Int) {
            testWords.clear()
            for (word in getLow(limit)) testWords.add(word)
        }

        fun getLow(limit: Int): ArrayList<Word> {

            val lowLevelWords = ArrayList<Word>()
            lowLevelWords.addAll(words)
            Collections.sort(lowLevelWords, LevelComparator())
            if (limit >= lowLevelWords.size) return lowLevelWords
            return lowLevelWords.subList(0, limit) as ArrayList<Word>
        }
    }
}
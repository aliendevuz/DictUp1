package uz.alien.dictup.adapter


import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import uz.alien.dictup.App
import uz.alien.dictup.R
import uz.alien.dictup.databinding.ItemVariantBinding
import uz.alien.dictup.word.LevelComparator
import uz.alien.dictup.word.Word
import java.util.Collections
import kotlin.random.Random


class AdapterVariant : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var correctAnswer: Word
    val buttons = ArrayList<CardView>()
    val testWords = ArrayList<Word>()
    val variantWords = ArrayList<Word>()
    val variants = ArrayList<Word>()
    val correctAnswers = HashMap<Int, Int>()
    val incorrectAnswers = HashMap<Int, Int>()
    var variantCount = 8
    var currentTest = 0
    var testLanguage = true
    var isCheckable = true

    class VariantViewHolder(view: View) : RecyclerView.ViewHolder(view) { val binding = ItemVariantBinding.bind(view) }
    override fun getItemCount() = variantCount
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = VariantViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_variant, parent, false))
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is VariantViewHolder) {
            if (buttons.size != variantCount) {
                buttons.add(holder.binding.root)
                Log.d("@@@@", "variant $position added!")
            }
            holder.binding.tvText.text = "variant ${position + 1}"
            holder.binding.root.setOnClickListener { check(position) }
        }
    }

    fun setVariant(position: Int, answer: String) {
        try {
            val tv = buttons[position].getChildAt(0) as TextView
            tv.text = answer
        } catch (e: Exception) {
            Log.d("@@@@", "${buttons.size}")
        }
    }

    fun setColor(position: Int, color: Int) { buttons[position].setCardBackgroundColor(color) }

    fun swapColor(position: Int, colorTo: Int, duration: Long = 200L) {
        buttons[position].let {
            val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), it.cardBackgroundColor.defaultColor, colorTo)
            colorAnimation.setDuration(duration)
            colorAnimation.addUpdateListener { animator ->
                buttons[position].setCardBackgroundColor(animator.animatedValue as Int)
            }
            colorAnimation.start()
        }
    }


    fun next() {
        App.home.openUnbackableFrame(App.home.binding.pageTest) {
            App.home.setNotBackable("Testni oxirigacha bajarish shart!")
            App.home.binding.bAccept.text = "Keyingisi"
            testLanguage = Random.nextInt(100) % 2 == 0
            isCheckable = true
            variants.clear()
            val select = ArrayList<Word>()
            select.addAll(variantWords)
            correctAnswer = testWords[currentTest]
            select.remove(correctAnswer)
            variants.add(correctAnswer)
            App.home.binding.tvTestNumber.text = "${currentTest + 1}/${App.testCount}"
            App.home.binding.tvAsk.text =
                "${if (testLanguage) correctAnswer.en else correctAnswer.uz}"
            select.shuffle()
            for (i in 1 until variantCount) variants.add(select.removeAt(0))
            variants.shuffle()
            for (i in 0 until variantCount) {
                val variant = if (testLanguage) variants[i].uz else variants[i].en
                for (j in 0 until variantCount) setColor(j, App.app.getColor(R.color.back_color_25))
                setVariant(i, "$variant")
            }
        }
    }

    fun check(ans: Int) {
        if (isCheckable) if (ans in 0 until variantCount) {
            if ((if (testLanguage) variants[ans].uz else variants[ans].en) == (if (testLanguage) correctAnswer.uz else correctAnswer.en)) {
                correctAnswers[currentTest] = 1
                swapColor(ans, App.app.getColor(R.color.correct_answer))
                App.soundPool.play(1, 1.0f, 1.0f, 0, 0, 1.0f)
                App.update(correctAnswer.id, 1)
                isCheckable = false
                if (currentTest >= App.testCount - 1) App.home.binding.bAccept.text =
                    "Natijani ko'rish"
            } else {
                if (buttons[ans].cardBackgroundColor.defaultColor != App.home.getColor(R.color.incorrect_answer)) {
                    if (incorrectAnswers.containsKey(currentTest)) incorrectAnswers[currentTest] =
                        incorrectAnswers[currentTest]!! + 1
                    else incorrectAnswers[currentTest] = 1
                    swapColor(ans, App.app.getColor(R.color.incorrect_answer))
                    App.soundPool.play(2, 1.0f, 1.0f, 0, 0, 1.0f)
                    App.update(correctAnswer.id, -1)
                }
            }
        }
    }

    fun createTestList() {
        currentTest = 0
        testWords.clear()
        variantWords.clear()
        correctAnswers.clear()
        incorrectAnswers.clear()
        for (unit in App.testUnits) for (i in 0..19) if (unit < 0) Log.d("@@@@", "Waring!!! Negadir manfiy qiymatga ega unit qo'shilib qolgan")
        else variantWords.add(App.words[i + 20 * unit])
        variantWords.shuffle()
        val lowLevelWords = ArrayList<Word>()
        lowLevelWords.addAll(variantWords)
        Collections.sort(lowLevelWords, LevelComparator())
        for (i in 0 until App.testCount) testWords.add(lowLevelWords[i])
        testWords.shuffle()
    }
}
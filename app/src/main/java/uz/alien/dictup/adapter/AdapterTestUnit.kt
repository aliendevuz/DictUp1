package uz.alien.dictup.adapter


import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import uz.alien.dictup.ActivityHome
import uz.alien.dictup.App
import uz.alien.dictup.R
import uz.alien.dictup.databinding.ItemTestUnitBinding
import kotlin.random.Random.Default.nextInt


class AdapterTestUnit(private val activity: ActivityHome) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var clickable = false
    val duration = 200L
    val shortDuration = 10L
    val randomChoice = false
    val buttons = HashMap<Int, CardView>()
    class TestUnitViewHolder(view: View) : RecyclerView.ViewHolder(view) { val binding = ItemTestUnitBinding.bind(view) }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TestUnitViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_test_unit, parent, false))
    override fun getItemCount() = 30
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) { if (holder is TestUnitViewHolder) { buttons[position] = holder.binding.root; holder.binding.tvText.text = "unit ${position + 1}" } }
    fun isClicked(unit: Int) = App.testUnits.contains(unit)
    fun random(position: Int, unit: Int) { if (nextInt(100) % 2 == 0) setClick(position, unit) else setUnClick(position, unit) }
    fun invert(position: Int, unit: Int) { if (isClicked(unit)) setUnClick(position, unit) else setClick(position, unit) }
    fun update() { if (App.testUnits.size != 0 && App.testUnits.size <= 5) App.home.binding.sbTestCount.max = App.testUnits.size * 4 - 1 }
    fun setClick(position: Int, unit: Int) {
        if (unit > 179) Log.d("@@@@", "$unit")
        if (!App.testUnits.contains(unit)) {
            if (unit in 0..179) App.testUnits.add(unit)
            else Log.d("@@@@", "Warning!!! Qo'shilayotgan unit qiymatdan tashqarida $unit")
            swapColor(position, activity.getColor(R.color.light_blue), duration)
        }
        update()
    }
    fun setUnClick(position: Int, unit: Int) { if (App.testUnits.contains(unit)) { App.testUnits.remove(unit); swapColor(position, activity.getColor(R.color.back_color_25), duration) }; update() }
    fun updateAllButtons() { for (k in buttons.keys) { val unit = k + App.bookNumber * 30; App.handler.postDelayed({ if (App.testUnits.contains(unit)) swapColor(k, activity.getColor(R.color.light_blue), duration) else swapColor(k, activity.getColor(R.color.back_color_25), duration) }, shortDuration * k) }; update() }
    fun randomize() {
        val unitList = IntArray(30) { it }
        if (randomChoice) unitList.shuffle()
        val unClickedList = ArrayList<Int>()
        for (i in unitList) {
            val unit = i + App.bookNumber * 30
            if (!App.testUnits.contains(unit)) unClickedList.add(i)
            if (unClickedList.size == 6) break
        }
        for (i in unClickedList) App.handler.postDelayed({ setClick(i, i + App.bookNumber * 30) }, shortDuration * i)
    }
    fun randomAll() {
        val unitList = IntArray(30) { it }
        unitList.shuffle()
        for (i in unitList) App.handler.postDelayed({ if (randomChoice) random(unitList[i], unitList[i] + App.bookNumber * 30) else random(i, i + App.bookNumber * 30) }, shortDuration * i)
    }
    fun invertAll() {
        val unitList = IntArray(30) { it }
        unitList.shuffle()
        for (i in unitList) App.handler.postDelayed({ if (randomChoice) invert(unitList[i], unitList[i] + App.bookNumber * 30) else invert(i, i + App.bookNumber * 30) }, shortDuration * i)
    }
    fun selectAll() {
        val unitList = IntArray(30) { it }
        unitList.shuffle()
        for (i in unitList) App.handler.postDelayed({ if (randomChoice) setClick(unitList[i], unitList[i] + App.bookNumber * 30) else setClick(i, i + App.bookNumber * 30) }, shortDuration * i)
    }
    fun clearAll() {
        val unitList = IntArray(30) { it }
        unitList.shuffle()
        for (i in 0 until 30) App.handler.postDelayed({ if (randomChoice) setUnClick(unitList[i], unitList[i] + App.bookNumber * 30) else setUnClick(i, i + App.bookNumber * 30) }, shortDuration * i)
    }
    fun swapColor(position: Int, colorTo: Int, duration: Long) {
        buttons[position]?.let {
            val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), it.cardBackgroundColor.defaultColor, colorTo)
            colorAnimation.setDuration(duration)
            colorAnimation.addUpdateListener { animator -> buttons[position]!!.setCardBackgroundColor(animator.animatedValue as Int) }
            colorAnimation.start()
        }
    }
}
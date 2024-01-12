package uz.alien.dictup.adapter


import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Color
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

    val buttons = HashMap<Int, CardView>()

    class TestUnitViewHolder(view: View) : RecyclerView.ViewHolder(view) { val binding = ItemTestUnitBinding.bind(view) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        TestUnitViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_test_unit, parent, false))

    override fun getItemCount() = 30

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is TestUnitViewHolder) {

            buttons[position] = holder.binding.root

            holder.binding.tvText.text = "unit ${position + 1}"
        }
    }

    fun isClicked(unit: Int) = App.testUnits.contains(unit)

    fun random(position: Int, unit: Int) {
        if (nextInt(100) % 2 == 0) setClick(position, unit) else setUnClick(position, unit)
    }

    fun invert(position: Int, unit: Int) {
        if (isClicked(unit)) setUnClick(position, unit) else setClick(position, unit)
    }

    fun update() {

        for (i in 0 until 30) if (!isClicked(i + App.bookNumber * 30)) {
            App.home.binding.tvSelectAll.text = "Select All"
            return
        }
        App.home.binding.tvSelectAll.text = "Clear All"
        return
    }

    fun setClick(position: Int, unit: Int) {

        if (!App.testUnits.contains(unit)) {
            App.testUnits.add(unit)
            swapColor(position, activity.getColor(R.color.light_blue), 100L)
        }
        update()
    }

    fun setUnClick(position: Int, unit: Int) {

        if (App.testUnits.contains(unit)) {
            App.testUnits.remove(unit)
            swapColor(position, activity.getColor(R.color.back_color), 100L)
        }
        update()
    }

    fun updateAllButtons() {

        for (k in buttons.keys) {

            val unit = k + App.bookNumber * 30

            if (App.testUnits.contains(unit)) swapColor(k, activity.getColor(R.color.light_blue), 100L)
            else swapColor(k, activity.getColor(R.color.back_color), 100L)
        }

        update()
    }

    fun randomAll() {
        for (i in 0 until 30) {
            random(i, i + App.bookNumber * 30)
        }
    }

    fun invertAll() {
        for (i in 0 until 30) {
            invert(i, i + App.bookNumber * 30)
        }
    }

    fun selectAll() {
        for (i in 0 until 30) {
            setClick(i, i + App.bookNumber * 30)
        }
    }

    fun clearAll() {
        for (i in 0 until 30) {
            setUnClick(i, i + App.bookNumber * 30)
        }
    }

    fun swapColor(position: Int, colorTo: Int, duration: Long) {

        buttons[position]?.let {

            val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), it.cardBackgroundColor.defaultColor, colorTo)
            colorAnimation.setDuration(duration)

            colorAnimation.addUpdateListener { animator ->
                buttons[position]!!.setCardBackgroundColor(animator.animatedValue as Int)
            }

            colorAnimation.start()
        }
    }
}
package uz.alien.dictup.adapter

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import uz.alien.dictup.ActivityHome
import uz.alien.dictup.App
import uz.alien.dictup.R
import uz.alien.dictup.databinding.ItemTestBookBinding

class AdapterTestBook(
    private val activity: ActivityHome,
    private val unitAdapter: AdapterTestUnit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val duration = 500L
    var started = false
    val buttons = HashMap<Int, CardView>()

    class TestBookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemTestBookBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TestBookViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_test_book, parent, false)
        )
    }

    override fun getItemCount() = 6

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TestBookViewHolder) {

            buttons[position] = holder.binding.root


            if (position == 0 && !started) {
                started = true
                swapColor(position, App.home.getColor(R.color.light_blue), duration)
            }

            holder.binding.tvTestBook.text = "${position + 1}"

            holder.binding.root.setOnClickListener {

                swapColor(App.bookNumber, App.home.getColor(R.color.back_color_25), duration)

                App.bookNumber = position

                swapColor(App.bookNumber, App.home.getColor(R.color.light_blue), duration)

                unitAdapter.updateAllButtons()
            }
        }
    }

    fun swapColor(position: Int, colorTo: Int, duration: Long) {

        buttons[position]?.let {

            val colorAnimation = ValueAnimator.ofObject(
                ArgbEvaluator(),
                it.cardBackgroundColor.defaultColor,
                colorTo
            )
            colorAnimation.setDuration(duration)

            colorAnimation.addUpdateListener { animator ->
                buttons[position]!!.setCardBackgroundColor(animator.animatedValue as Int)
            }

            colorAnimation.start()
        }
    }
}
package uz.alien.dictup.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import uz.alien.dictup.ActivityHome
import uz.alien.dictup.App
import uz.alien.dictup.R
import uz.alien.dictup.databinding.ItemTestBookBinding

class AdapterTestBook(private val activity: ActivityHome, private val unitAdapter: AdapterTestUnit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val buttons = ArrayList<CardView>()
    var started = false

    class TestBookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemTestBookBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TestBookViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_test_book, parent, false))
    }

    override fun getItemCount(): Int {
        return 6
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TestBookViewHolder) {

            if (buttons.size != 6) buttons.add(holder.binding.root)
            if (position == 0 && !started) {
                started = true
                holder.binding.root.setCardBackgroundColor(activity.getColor(R.color.light_blue))
            }

            holder.binding.tvTestBook.text = "${position + 1}"

            holder.binding.root.setOnClickListener {

                buttons[App.bookNumber].setCardBackgroundColor(activity.getColor(R.color.back_color))

                holder.binding.root.setCardBackgroundColor(activity.getColor(R.color.light_blue))

                App.bookNumber = position

                unitAdapter.updateAllButtons()
            }
        }
    }
}
package uz.alien.dictup.adapter


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


class AdapterVariant : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val buttons = ArrayList<CardView>()

    class VariantViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemVariantBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return VariantViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_variant, parent, false))
    }

    override fun getItemCount(): Int {
        return App.variantCount
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is VariantViewHolder) {

            if (buttons.size != App.variantCount) {
                buttons.add(holder.binding.root)
                Log.d("@@@@", "variant ${position} added!")
            }

            holder.binding.tvText.text = "variant ${position + 1}"

            holder.binding.root.setOnClickListener {
                App.check(position)
            }

//            if (App.variantCount == position) App.next()
        }
    }

    fun setVariant(position: Int, answer: String) {
        try {
            val tv = buttons[position].getChildAt(0) as TextView
            tv.text = answer
        } catch(e: Exception) {
            Log.d("@@@@", "${buttons.size}")
        }
    }

    fun setColor(position: Int, color: Int) {
        buttons[position].setCardBackgroundColor(color)
    }
}
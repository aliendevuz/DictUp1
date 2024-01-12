package uz.alien.dictup.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import uz.alien.dictup.ActivityHome
import uz.alien.dictup.App
import uz.alien.dictup.R
import uz.alien.dictup.databinding.ItemListBinding

class AdapterUnit(private val activity: ActivityHome) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class UnitViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val binding = ItemListBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return UnitViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false))
    }

    override fun getItemCount(): Int {
        return 30
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is UnitViewHolder) {
            holder.binding.tvText.text = "unit ${position + 1}"

            holder.binding.root.setOnClickListener {

                activity.openFrame(activity.binding.pageWords) {

                    App.unitNumber = position

                    activity.binding.rvWords.layoutManager = LinearLayoutManager(activity)

                    activity.binding.rvWords.adapter = AdapterWord(activity)
                }
            }
        }
    }
}
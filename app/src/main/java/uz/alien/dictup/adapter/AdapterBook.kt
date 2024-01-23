package uz.alien.dictup.adapter

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uz.alien.dictup.ActivityHome
import uz.alien.dictup.App
import uz.alien.dictup.R
import uz.alien.dictup.databinding.ItemBookBinding
import uz.alien.dictup.source.CustomLayoutManager


class AdapterBook(private val activity: ActivityHome) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val books = arrayListOf(
        R.drawable.book1,
        R.drawable.book2,
        R.drawable.book3,
        R.drawable.book4,
        R.drawable.book5,
        R.drawable.book6
    )

    class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemBookBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return BookViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return books.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BookViewHolder) {

            holder.binding.ivBook.setImageResource(books[position])

            holder.binding.root.setOnClickListener {

                activity.openFrame(activity.binding.pageUnits) {

                    App.bookNumber = position

                    val colorOn = activity.getColor(R.color.correct_answer)
                    val colorOff = activity.getColor(R.color.app_theme_color)
                    val colorDisabled = -0xcccccd
                    val thumbStates = StateListDrawable()
                    thumbStates.addState(
                        intArrayOf(android.R.attr.state_checked),
                        ColorDrawable(colorOn)
                    )
                    thumbStates.addState(
                        intArrayOf(-android.R.attr.state_enabled),
                        ColorDrawable(colorDisabled)
                    )
                    thumbStates.addState(intArrayOf(), ColorDrawable(colorOff))

//                    App.home.binding.sEnglish.thumbDrawable = thumbStates

                    App.home.binding.sEnglish.setOnCheckedChangeListener { buttonView, isChecked ->
                        App.home.binding.sEnglish.text =
                            if (isChecked) "Inglizcha yodlash" else "O'zbekcha yodlash"
                        App.saveValue("word_english", isChecked)
                    }

                    App.home.binding.sEnglish.isChecked = App.getBoolean("word_english", true)


                    activity.binding.rvUnits.layoutManager = CustomLayoutManager(activity, 3)

                    activity.binding.rvUnits.adapter = AdapterUnit(activity)
                }
            }
        }
    }
}
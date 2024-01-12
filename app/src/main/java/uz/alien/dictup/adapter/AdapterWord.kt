package uz.alien.dictup.adapter

import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uz.alien.dictup.ActivityHome
import uz.alien.dictup.App
import uz.alien.dictup.R
import uz.alien.dictup.databinding.ItemListBinding

class AdapterWord(private val activity: ActivityHome) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class WordViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val binding = ItemListBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return WordViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false))
    }

    override fun getItemCount(): Int {
        return 20
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is WordViewHolder) {

            val word = App.words[position + App.unitNumber * 20 + App.bookNumber * 600]
            holder.binding.tvText.text = word.en

            holder.binding.root.setOnClickListener {

                activity.openFrame(activity.binding.pageWordDetails) {

                    App.textToSpeech.speak(word.en, TextToSpeech.QUEUE_FLUSH, null, null)

                    activity.binding.tvEn.text = word.en
                    activity.binding.tvUz.text = word.uz
                    activity.binding.tvTranscript.text = word.transcript
                    activity.binding.tvType.text = word.type
                    activity.binding.tvDescribe.text = word.describe
                    activity.binding.tvSample.text = word.sample
                }
            }
        }
    }
}

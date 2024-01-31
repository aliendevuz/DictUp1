package uz.alien.dictup.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uz.alien.dictup.ActivityHome
import uz.alien.dictup.App
import uz.alien.dictup.R
import uz.alien.dictup.databinding.ItemWordBinding
import java.io.IOException
import java.io.InputStream

class AdapterWord(private val activity: ActivityHome) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class WordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemWordBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return WordViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_word, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return 20
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is WordViewHolder) {

            val word = App.words[position + App.unitNumber * 20 + App.bookNumber * 600]
            holder.binding.tvText.text =
                if (App.home.binding.sEnglish.isChecked) "${word.en}" else "${word.uz}"

            holder.binding.root.setOnClickListener {

                activity.openFrame(activity.binding.pageWordDetails) {

                    App.handler.postDelayed({
                        App.textToSpeech.speak(word.en, TextToSpeech.QUEUE_FLUSH, null, null)
                    }, 600L)

                    activity.binding.ivEn.setOnClickListener {
                        App.textToSpeech.speak(word.en, TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                    activity.binding.ivDescribe.setOnClickListener {
                        App.textToSpeech.speak(word.describe, TextToSpeech.QUEUE_ADD, null, null)
                    }
                    activity.binding.ivSample.setOnClickListener {
                        App.textToSpeech.speak(word.sample, TextToSpeech.QUEUE_ADD, null, null)
                    }

                    activity.binding.ivWord.setImageBitmap(getBitmapFromAsset("pictures/book${App.bookNumber}/unit${App.unitNumber}/${position}.jpg"))
                    activity.binding.tvEn.text = "${word.en}  [${word.transcript}] ${word.type}"
                    activity.binding.tvUz.text = word.uz
                    activity.binding.tvDescribe.text = "${word.describe}"
                    activity.binding.tvSample.text = "${word.sample}"
                }
            }
        }
    }

    private fun getBitmapFromAsset(fileName: String): Bitmap {
        var istr: InputStream? = null
        try {
            istr = activity.assets.open(fileName)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return BitmapFactory.decodeStream(istr)
    }
}

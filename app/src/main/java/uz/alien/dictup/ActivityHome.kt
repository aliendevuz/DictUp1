package uz.alien.dictup


import android.R
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import uz.alien.dictup.adapter.AdapterBook
import uz.alien.dictup.adapter.AdapterTestBook
import uz.alien.dictup.adapter.AdapterTestUnit
import uz.alien.dictup.adapter.AdapterVariant
import uz.alien.dictup.databinding.ActivityHomeBinding
import uz.alien.dictup.source.Activity
import uz.alien.dictup.source.CustomLayoutManager


class ActivityHome : Activity() {

    lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.home = this
        binding = ActivityHomeBinding.inflate(layoutInflater)
        initialize(binding.root)
        if (App.getBoolean("welcome_showed")) startFrame(binding.pageHome, startHomeTasks)
        else startFrameUnbackable(binding.pageWelcome) {
            binding.bGetStart.setOnClickListener {
                App.saveValue("welcome_showed", true)
                openFrameLong(binding.pageHome, startHomeTasks)
            }
        }
    }

    var isScrollable = true
    var isPosChanged = false
    var isDone = false
    var position = -1
    var startX = -1.0f
    var startY = -1.0f
    var currentX = -1.0f
    var currentY = -1.0f
    val range = 10

    @SuppressLint("ClickableViewAccessibility")
    val startHomeTasks = {
        binding.rvBooks.layoutManager = CustomLayoutManager(this, 3)
        binding.rvBooks.adapter = AdapterBook(this)
        binding.bTest.setOnClickListener {
            openFrame(binding.pagePickUnits) {
                App.testUnits.clear()
                binding.rvTestUnits.layoutManager = CustomLayoutManager(this, 3)
                val unitAdapter = AdapterTestUnit(this)
                binding.rvTestUnits.adapter = unitAdapter
                binding.rvTestUnits.setOnTouchListener { _, event ->
                    if (isScrollable) binding.rvTestUnits.onTouchEvent(event)
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            startX = event.x
                            startY = event.y
                            currentX = event.x
                            currentY = event.y
                            binding.rvTestUnits.findChildViewUnder(event.x, event.y)?.let {
                                position = binding.rvTestUnits.getChildAdapterPosition(it)
                                unitAdapter.clickable =
                                    unitAdapter.isClicked(position + App.bookNumber * 30)
                            }
                            App.handler.postDelayed({
                                if (!isPosChanged) {
                                    binding.rvTestUnits.findChildViewUnder(currentX, currentY)
                                        ?.let {
                                            isScrollable =
                                                if (position == binding.rvTestUnits.getChildAdapterPosition(
                                                        it
                                                    )
                                                ) {
                                                    unitAdapter.invert(
                                                        position,
                                                        position + App.bookNumber * 30
                                                    )
                                                    App.vibrator.vibrate(40)
                                                    false
                                                } else true
                                        }
                                }
                                isDone = true
                            }, 500L)
                        }

                        MotionEvent.ACTION_MOVE -> {
                            currentX = event.x
                            currentY = event.y
                            isPosChanged =
                                (event.x < startX - range || startX + range < event.x || event.y < startY - range || startY + range < event.y) && !isDone
                            binding.rvTestUnits.findChildViewUnder(event.x, event.y)?.let { it1 ->
                                if (!isScrollable) {
                                    position = binding.rvTestUnits.getChildAdapterPosition(it1)
                                    val unit = position + App.bookNumber * 30
                                    if (unitAdapter.clickable) unitAdapter.setUnClick(
                                        position,
                                        unit
                                    )
                                    else unitAdapter.setClick(position, unit)
                                }
                            }
                        }

                        MotionEvent.ACTION_UP -> {
                            if (!isPosChanged && !isDone) {
                                val unit = position + App.bookNumber * 30
                                if (unitAdapter.clickable) unitAdapter.setUnClick(position, unit)
                                else unitAdapter.setClick(position, unit)
                            }
                            App.handler.removeCallbacksAndMessages(null)
                            isPosChanged = false
                            isScrollable = true
                            isDone = false
                            unitAdapter.clickable = false
                            position = -1
                        }
                    }
                    true
                }
                binding.sbTestCount.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        App.saveValue("test_count", progress)
                        App.testCount = progress * 5 + 5
                        binding.tvTestCount.text = "${App.testCount}"
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })
                binding.tvSelectAll.setOnClickListener { unitAdapter.selectAll() }
                binding.tvRandom.setOnClickListener { unitAdapter.randomAll() }
                binding.tvInvert.setOnClickListener { unitAdapter.invertAll() }
                binding.tvClear.setOnClickListener { unitAdapter.clearAll() }
                binding.rvTestBooks.layoutManager = CustomLayoutManager(this, 6)
                App.bookNumber = 0
                binding.rvTestBooks.adapter = AdapterTestBook(this, unitAdapter)
                binding.bStartTest.setOnClickListener {
                    if (App.testUnits.isEmpty()) Toast.makeText(
                        this,
                        "Unitlar tanlanishi kerak!",
                        Toast.LENGTH_SHORT
                    ).show()
                    else {
                        if (App.getInt("warned") < 3) {
                            Toast.makeText(this, "Imkon qadar to'g'ri bajaring!", Toast.LENGTH_LONG).show()
                            Toast.makeText(this, "Aks holda o'sha so'z ko'p chiqariladi!", Toast.LENGTH_SHORT).show()
                            App.saveValue("warned", App.getInt("warned") + 1)
                        }
                        binding.pageTest.alpha = 0.0f
                        binding.pageTest.visibility = View.VISIBLE
                        binding.rvTestAnswers.layoutManager = CustomLayoutManager(this)
                        val adapter = AdapterVariant()
                        binding.rvTestAnswers.adapter = adapter
                        adapter.createTestList()
                        App.handler.post { adapter.next() }
                        binding.bAccept.setOnClickListener {
                            if (!adapter.isCheckable) {
                                if (adapter.currentTest >= App.testCount - 1)
                                    openFrame(binding.pageResult) {
                                        val size = adapter.incorrectAnswers.size
                                        if (size == 0) {
                                            binding.tvIncorrectAnswers.text = "Tabriklayman 🥳\nBarchasini to'g'ri topdingiz!"
                                            binding.tvCorrectAnswers.visibility = View.GONE
                                            binding.tvBall.visibility = View.GONE
                                        }
                                        else {
                                            binding.tvCorrectAnswers.visibility = View.VISIBLE
                                            binding.tvCorrectAnswers.text = "To'g'ri javoblar soni - ${App.testCount - size} ta"
                                            binding.tvIncorrectAnswers.text = "Noto'g'ri javoblar soni - ${size} ta"
                                            binding.tvBall.visibility = View.VISIBLE
                                            binding.tvBall.text = "Sizning natijangiz ${100 - ((size.toFloat() / App.testCount.toFloat()) * 100.0).toInt()}%"
                                        }
                                    }
                                else {
                                    adapter.currentTest++
                                    adapter.next()
                                }
                            }
                        }
                    }
                }
            }
        }
        binding.bAbout.setOnClickListener {
            openFrame(binding.pageAbout) {
                val author = "More about: wiki/Paul_Nation"
                val contact1 = "Contact: t.me/abc_2202"
                val contact2 = "Contact: in/aliendevuz"
                val spannableText1 = SpannableString(author)
                val spannableText2 = SpannableString(contact1)
                val spannableText3 = SpannableString(contact2)
                spannableText1.setSpan(URLSpan("https://en.wikipedia.org/wiki/Paul_Nation"), 12, author.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannableText2.setSpan(URLSpan("https://t.me/abc_2202"), 9, contact1.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannableText3.setSpan(URLSpan("https://linkedin.com/in/aliendevuz"), 9, contact2.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                binding.tvAuthor.text = spannableText1
                binding.tvContact1.text = spannableText2
                binding.tvContact2.text = spannableText3
                binding.tvAuthor.movementMethod = LinkMovementMethod.getInstance()
                binding.tvContact1.movementMethod = LinkMovementMethod.getInstance()
                binding.tvContact2.movementMethod = LinkMovementMethod.getInstance()
            }
        }
    }
}
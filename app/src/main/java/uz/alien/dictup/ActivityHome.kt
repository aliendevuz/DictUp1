package uz.alien.dictup


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import uz.alien.dictup.adapter.AdapterBook
import uz.alien.dictup.adapter.AdapterTestBook
import uz.alien.dictup.adapter.AdapterTestUnit
import uz.alien.dictup.adapter.AdapterVariant
import uz.alien.dictup.databinding.ActivityHomeBinding
import uz.alien.dictup.source.Activity
import uz.alien.dictup.source.Runner


class ActivityHome : Activity() {

    lateinit var binding: ActivityHomeBinding
    var isScrollable = true
    var isPosChanged = false
    var position = -1
    var startX = -1.0f
    var startY = -1.0f
    var currentX = -1.0f
    var currentY = -1.0f
    val range = 5

    val runners = HashMap<Long, Runner>()
    var currentRunner = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.home = this
        binding = ActivityHomeBinding.inflate(layoutInflater)
        initialize(binding.root)

        App.handler.post {
            if (App.getBoolean("welcome_showed")) startFrame(binding.pageHome, startHomeTasks)
            else startFrameUnbackable(binding.pageWelcome) {
                App.saveValue("welcome_showed", true)
                binding.cvGetStart.setOnClickListener { openFrameLong(binding.pageHome, startHomeTasks) }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    val startHomeTasks = {

        binding.rvBooks.layoutManager = GridLayoutManager(this, 3)

        binding.rvBooks.isNestedScrollingEnabled = false

        binding.rvBooks.adapter = AdapterBook(this)

        binding.cvTest.setOnClickListener {

            openFrame(binding.pagePickUnits) {

                App.testUnits.clear()

                binding.rvTestUnits.layoutManager = GridLayoutManager(this, 3)

                val unitAdapter = AdapterTestUnit(this)

                binding.rvTestUnits.adapter = unitAdapter

                binding.rvTestUnits.setOnTouchListener { _, event ->

                    if (isScrollable) binding.rvTestUnits.onTouchEvent(event)

                    when (event.action) {

                        MotionEvent.ACTION_DOWN -> {

                            binding.rvTestUnits.findChildViewUnder(event.x, event.y)?.let {
                                startX = event.x
                                startY = event.y
                                currentX = event.x
                                currentY = event.y
                                position = binding.rvTestUnits.getChildAdapterPosition(it)
                                val unit = position + App.bookNumber * 30
                                unitAdapter.clickable = unitAdapter.isClicked(unit)
                            }

                            currentRunner = System.nanoTime()
                            runners[currentRunner] = Runner {
                                binding.rvTestUnits.findChildViewUnder(currentX, currentY)?.let {
                                    isScrollable = position != binding.rvTestUnits.getChildAdapterPosition(it)
                                }
                            }

                            App.handler.postDelayed({
                                runners[currentRunner]!!.run()
                            }, 240L)
                        }

                        MotionEvent.ACTION_MOVE -> {
                            currentX = event.x
                            currentY = event.y
                            isPosChanged = (event.x < startX - range || startX + range < event.x || event.y < startY - range || startY + range < event.y) && !runners[currentRunner]!!.isDone
                            binding.rvTestUnits.findChildViewUnder(event.x, event.y)?.let { it1 ->
                                if (!isScrollable) {
                                    position = binding.rvTestUnits.getChildAdapterPosition(it1)
                                    val unit = position + App.bookNumber * 30
                                    if (unitAdapter.clickable) unitAdapter.setUnClick(position, unit)
                                    else unitAdapter.setClick(position, unit)
                                }
                            }
                        }

                        MotionEvent.ACTION_UP -> {

                            if (!isPosChanged) {
                                val unit = position + App.bookNumber * 30
                                if (unitAdapter.clickable) unitAdapter.setUnClick(position, unit)
                                else unitAdapter.setClick(position, unit)
                            }

                            val keys = ArrayList<Long>()
                            for (key in runners.keys) if (key != currentRunner) keys.add(key)
                            for (key in keys) runners.remove(key)

                            isScrollable = true
                            unitAdapter.clickable = false
                            position = -1
                        }
                    }
                    true
                }

                binding.tvRandom.setOnClickListener {
                    unitAdapter.randomAll()
                }

                binding.tvInvert.setOnClickListener {
                    unitAdapter.invertAll()
                }

                binding.tvSelectAll.setOnClickListener {
                    if (binding.tvSelectAll.text.toString().lowercase() == "select all") unitAdapter.selectAll()
                    else unitAdapter.clearAll()
                }

                binding.rvTestBooks.layoutManager = GridLayoutManager(this, 6)

                binding.rvTestBooks.isNestedScrollingEnabled = false

                App.bookNumber = 0

                binding.rvTestBooks.adapter = AdapterTestBook(this, unitAdapter)

                binding.cvStartTest.setOnClickListener {

                    if (App.testUnits.isEmpty()) Toast.makeText(this, "Unitlar tanlanishi kerak!", Toast.LENGTH_SHORT).show()

                    else {

                        binding.pageTest.alpha = 0.0f
                        binding.pageTest.visibility = View.VISIBLE

                        App.createTestList()

                        binding.rvTestAnswers.layoutManager = LinearLayoutManager(this)

                        App.adapter = AdapterVariant()

                        binding.rvTestAnswers.adapter = App.adapter

                        App.next()

                        binding.cvAccept.setOnClickListener {

                            if (!App.isCheckable) {
                                App.currentTest++
                                App.next()
                            }
                        }
                    }
                }
            }
        }
    }
}
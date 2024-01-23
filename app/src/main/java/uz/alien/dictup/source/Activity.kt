package uz.alien.dictup.source


import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import uz.alien.dictup.App


open class Activity(var duration: Long = 150L, var longDuration: Long = duration * 3) :
    AppCompatActivity() {

    lateinit var bInactive: Button

    val fadeIn = AlphaAnimation(0.0f, 1.0f)
    val fadeOut = AlphaAnimation(1.0f, 0.0f)
    val fadeInLong = AlphaAnimation(0.0f, 1.0f)
    val fadeOutLong = AlphaAnimation(1.0f, 0.0f)

    private var isNotBackable = false
    private var causeMessage = ""

    var isUnbackable = false
    var isBackPressAvailable = true
    val openedFrames = ArrayList<LinearLayout>()
    lateinit var currentFrame: LinearLayout

    fun initialize(root: FrameLayout) {
        fadeIn.duration = duration
        fadeOut.duration = duration
        fadeInLong.duration = longDuration
        fadeOutLong.duration = longDuration
        setContentView(root)
        bInactive = Button(this)
        bInactive.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        bInactive.alpha = 0.0f
        root.addView(bInactive)
        for (child in root.children) {
            child.visibility = View.GONE
        }
    }

    fun setNotBackable(cause: String) {
        isNotBackable = true
        causeMessage = cause
    }

    fun resetBackable() {
        isNotBackable = false
        causeMessage = ""
    }

    fun startFrame(frame: LinearLayout, runnable: Runnable = Runnable {}) {
        resetBackable()
        frame.alpha = 0.0f
        frame.visibility = View.VISIBLE
        isUnbackable = false
        currentFrame = frame
        openedFrames.add(frame)
        runnable.run()
        App.handler.post {
            frame.startAnimation(fadeInLong)
            frame.alpha = 1.0f
        }
    }

    fun startFrameUnbackable(frame: LinearLayout, runnable: Runnable = Runnable {}) {
        resetBackable()
        frame.alpha = 0.0f
        frame.visibility = View.VISIBLE
        isUnbackable = true
        currentFrame = frame
        runnable.run()
        App.handler.post {
            frame.startAnimation(fadeInLong)
            frame.alpha = 1.0f
        }
    }

    fun openFrame(frame: LinearLayout, runnable: Runnable = Runnable {}) {
        resetBackable()
        bInactive.visibility = View.VISIBLE
        frame.alpha = 0.0f
        frame.visibility = View.VISIBLE
        runnable.run()
        App.handler.post {
            currentFrame.startAnimation(fadeOut)
            App.handler.postDelayed({
                currentFrame.visibility = View.GONE
                bInactive.visibility = View.GONE
                isUnbackable = false
                currentFrame = frame
                openedFrames.add(frame)
                frame.alpha = 1.0f
                frame.startAnimation(fadeIn)
            }, duration)
        }
    }

    fun openFrameLong(frame: LinearLayout, runnable: Runnable = Runnable {}) {
        resetBackable()
        bInactive.visibility = View.VISIBLE
        frame.alpha = 0.0f
        frame.visibility = View.VISIBLE
        runnable.run()
        App.handler.post {
            currentFrame.startAnimation(fadeOutLong)
            App.handler.postDelayed({
                currentFrame.visibility = View.GONE
                bInactive.visibility = View.GONE
                isUnbackable = false
                currentFrame = frame
                openedFrames.add(frame)
                App.handler.post {
                    frame.alpha = 1.0f
                    frame.startAnimation(fadeInLong)
                }
            }, longDuration)
        }
    }

    fun openUnbackableFrame(frame: LinearLayout, runnable: Runnable = Runnable {}) {
        bInactive.visibility = View.VISIBLE
        currentFrame.startAnimation(fadeOut)
        App.handler.postDelayed({
            currentFrame.visibility = View.GONE
            bInactive.visibility = View.GONE
            frame.alpha = 0.0f
            frame.visibility = View.VISIBLE
            isUnbackable = true
            currentFrame = frame
            runnable.run()
            App.handler.post {
                frame.alpha = 1.0f
                frame.startAnimation(fadeIn)
            }
        }, duration)
    }

    fun openUnbackableFrameLong(frame: LinearLayout, runnable: Runnable = Runnable {}) {
        bInactive.visibility = View.VISIBLE
        frame.alpha = 0.0f
        frame.visibility = View.VISIBLE
        runnable.run()
        App.handler.post {
            currentFrame.startAnimation(fadeOutLong)
            App.handler.postDelayed({
                currentFrame.visibility = View.GONE
                bInactive.visibility = View.GONE
                isUnbackable = true
                currentFrame = frame
                App.handler.post {
                    frame.alpha = 1.0f
                    frame.startAnimation(fadeInLong)
                }
            }, longDuration)
        }
    }

    fun backFrame() {
        bInactive.visibility = View.VISIBLE
        currentFrame.startAnimation(fadeOut)
        App.handler.postDelayed({
            currentFrame.visibility = View.GONE
            bInactive.visibility = View.GONE
            openedFrames.removeLast()
            val frame = openedFrames.last()
            frame.alpha = 0.0f
            frame.visibility = View.VISIBLE
            isUnbackable = false
            currentFrame = frame
            App.handler.post {
                frame.alpha = 1.0f
                frame.startAnimation(fadeIn)
            }
        }, duration)
    }

    fun backUnbackableFrame() {
        bInactive.visibility = View.VISIBLE
        currentFrame.startAnimation(fadeOut)
        App.handler.postDelayed({
            currentFrame.visibility = View.GONE
            bInactive.visibility = View.GONE
            val frame = openedFrames.last()
            frame.alpha = 0.0f
            frame.visibility = View.VISIBLE
            isUnbackable = false
            currentFrame = frame
            App.handler.post {
                frame.alpha = 1.0f
                frame.startAnimation(fadeIn)
            }
        }, duration)
    }

    @Deprecated(
        "Deprecated in Java",
        ReplaceWith(
            "if (openedFrames.size > 1) if (isUnbackable) backUnbackableFrame() else backFrame() else super.onBackPressed()",
            "androidx.appcompat.app.AppCompatActivity"
        )
    )
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (!isNotBackable) {
            if (isBackPressAvailable)
                if (openedFrames.size > 1) {
                    if (isUnbackable) backUnbackableFrame() else backFrame()
                    isBackPressAvailable = false
                    App.handler.postDelayed({ isBackPressAvailable = true }, duration)
                } else super.onBackPressed()
        } else {
            Toast.makeText(this, causeMessage, Toast.LENGTH_SHORT).show()
        }
    }
}
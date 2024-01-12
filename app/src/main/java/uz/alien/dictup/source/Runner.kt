package uz.alien.dictup.source


class Runner(val runnable: Runnable) : Runnable {

    var isDone = false
    var isCanceled = false

    override fun run() {
        if (isCanceled) {
            runnable.run()
            isDone = true
        }
    }
}
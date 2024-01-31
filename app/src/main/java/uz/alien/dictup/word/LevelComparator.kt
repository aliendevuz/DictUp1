package uz.alien.dictup.word


class LevelComparator : Comparator<Word> { override fun compare(w1: Word, w2: Word) = w1.level - w2.level }
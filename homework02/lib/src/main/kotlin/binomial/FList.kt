package binomial

/*
 * FList - реализация функционального списка
 *
 * Пустому списку соответствует тип Nil, непустому - Cons
 *
 * Запрещено использовать
 *
 *  - var
 *  - циклы
 *  - стандартные коллекции
 *
 *  Исключение Array-параметр в функции flistOf. Но даже в ней нельзя использовать цикл и forEach.
 *  Только обращение по индексу
 */
sealed class FList<T> : Iterable<T> {
    // размер списка, 0 для Nil, количество элементов в цепочке для Cons
    abstract val size: Int

    // пустой ли списк, true для Nil, false для Cons
    abstract val isEmpty: Boolean

    // получить список, применив преобразование
    // требуемая сложность - O(n)
    abstract fun <U> map(f: (T) -> U): FList<U>

    // получить список из элементов, для которых f возвращает true
    // требуемая сложность - O(n)
    abstract fun filter(f: (T) -> Boolean): FList<T>

    // свертка
    // требуемая сложность - O(n)
    // Для каждого элемента списка (curr) вызываем f(acc, curr),
    // где acc - это base для начального элемента, или результат вызова
    // f(acc, curr) для предыдущего
    // Результатом fold является результат последнего вызова f(acc, curr)
    // или base, если список пуст
    abstract fun <U> fold(base: U, f: (U, T) -> U): U

    // разворот списка
    // требуемая сложность - O(n)
    fun reverse(): FList<T> = fold<FList<T>>(nil()) { acc, current ->
        Cons(current, acc)
    }

    /*
     * Это не очень красиво, что мы заводим отдельный Nil на каждый тип
     * И вообще лучше, чтобы Nil был объектом
     *
     * Но для этого нужны приседания с ковариантностью
     *
     * dummy - костыль для того, что бы все Nil-значения были равны
     *         и чтобы Kotlin-компилятор был счастлив (он требует, чтобы у Data-классов
     *         были свойство)
     *
     * Также для борьбы с бойлерплейтом были введены функция и свойство nil в компаньоне
     */
    data class Nil<T>(private val dummy: Int = 0) : FList<T>() {
        override val size: Int = 0

        override val isEmpty: Boolean = true

        override fun <U> map(f: (T) -> U): FList<U> = nil()


        override fun filter(f: (T) -> Boolean): FList<T> = nil()

        override fun <U> fold(base: U, f: (U, T) -> U): U = base

        override fun iterator(): Iterator<T> = object : Iterator<T> {
            override fun hasNext(): Boolean = false

            override fun next(): T = throw NoSuchElementException()
        }
    }

    data class Cons<T>(val head: T, val tail: FList<T>) : FList<T>() {
        override val size: Int = 1 + tail.size;

        override val isEmpty: Boolean = false

        override fun <U> map(f: (T) -> U): FList<U> = Cons(f(head), tail.map(f))


        override fun filter(f: (T) -> Boolean): FList<T> {
            return if (f(head)) {
                Cons(head, tail.filter(f))
            } else {
                tail.filter(f)
            }
        }

        override fun <U> fold(base: U, f: (U, T) -> U): U = tail.fold(f(base, head), f)

        override fun iterator(): Iterator<T> = object : Iterator<T> {
            var it: FList<T> = this@Cons

            override fun hasNext(): Boolean {
                return it is Cons<T>
            }

            override fun next(): T {
                if (it is Cons<T>) {
                    val result = (it as Cons<T>).head
                    it = (it as Cons<T>).tail
                    return result
                } else {
                    throw NoSuchElementException()
                }
            }
        }
    }

    companion object {
        fun <T> nil() = Nil<T>()
        val nil = Nil<Any>()
    }
}

// конструирование функционального списка в порядке следования элементов
// требуемая сложность - O(n)
fun <T> flistOf(vararg values: T): FList<T> = helper(0, values, FList.nil())

fun <T> helper(valueIterator: Int, values: Array<out T>, list: FList<T>): FList<T> {
    if (valueIterator >= values.size) {
        return list;
    } else {
        return helper(valueIterator + 1, values, FList.Cons(values[valueIterator], list))
    }
}

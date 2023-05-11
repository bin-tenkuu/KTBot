package my

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentLinkedDeque

/**
 *  @Date:2023/5/11
 *  @author bin
 *  @version 1.0.0
 */
object MutexTest {
    private val mutex = Mutex()
    private val queue = ConcurrentLinkedDeque<Int>()

    @JvmStatic
    fun main(args: Array<String>) {
        println(mutex.tryLock(1))
        println(mutex.tryLock(1))
        println(mutex.tryLock(2))
        mutex.unlock(1)
        runBlocking {
            val function: () -> Unit = {
                queue.add(queue.size)
                // 同一时刻只有一个协程能执行这里的代码
                launch(Dispatchers.Default) {
                    mutex.withLock() {
                        println("协程 ${queue.removeFirst()}")
                    }
                }
            }
            repeat(1000) {
                Thread(function).start()
            }
            delay(1000)
        }
    }
}


import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import kotlin.coroutines.experimental.CoroutineContext
import org.junit.Test as test

class TestSource() {

    fun recursiveLaunch(ctx: CoroutineContext, idx: Int = 0){
        if ( idx > 2 )return
        repeat(idx+1){ n ->
            println("${IntRange(0, idx)} $n")
            launch(ctx){recursiveLaunch(ctx, idx+1)}
        }
    }

    @test
    fun fa() {
        val e = launch { recursiveLaunch(coroutineContext) }
        while(!e.isCompleted){
            Thread.sleep(1000L)
        }
    }

    @test
    fun f() {
        runBlocking<Unit> {
            // launch a coroutine to process some kind of incoming request
            val request = launch {
                repeat(3) { i ->
                    // launch a few children jobs
                    launch(coroutineContext) {
                        delay((i + 1) * 200L) // variable delay 200ms, 400ms, 600ms
                        println("Coroutine $i is done")
                    }
                }
                println("request: I'm done and I don't explicitly join my children that are still active")
            }
            request.join() // wait for completion of the request, including all its children
            println("Now processing of the request is complete")
        }
    }
}
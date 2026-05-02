package cakar.search.com

import cakar.search.com.ProjectComponent.*
import kotlin.math.*

enum class EventType {
    KEY_PRESSED,
    KEY_RELEASED,
    SPRITE_CLICKED,
    STAGE_CLICKED,
    GREEN_FLAG,
    STOP_ALL
}

data class Event(
    val type: EventType,
    val data: Any? = null
)

class EventManager {
    private val listeners = mutableMapOf<EventType, MutableList<(Event) -> Unit>>()

    fun on(eventType: EventType, callback: (Event) -> Unit) {
        listeners.getOrPut(eventType) { mutableListOf() }.add(callback)
    }

    fun emit(event: Event) {
        listeners[event.type]?.forEach { it(event) }
    }

    fun removeAllListeners() {
        listeners.clear()
    }
}


class EventDrivenRuntime {
    val eventManager = EventManager()
    private val targets = mutableListOf<Sprite>()
    private val blocks = mutableMapOf<String, Block>()
    private val activeThreads = mutableListOf<Thread>()
    private var isRunning = false

    // Daftar blok yang menunggu event tertentu
    private val eventHandlers = mutableMapOf<EventType, MutableList<Block>>()

    init {
        // Setup event listeners
        setupEventListeners()
    }

    private fun setupEventListeners() {
        eventManager.on(EventType.GREEN_FLAG) {
            executeEventBlocks("event.whenflagclicked")
        }

        eventManager.on(EventType.KEY_PRESSED) { event ->
            val key = event.data as? String
            executeEventBlocks("event.whenkeypressed", key)
        }

        eventManager.on(EventType.SPRITE_CLICKED) { event ->
            val spriteName = event.data as? String
            executeEventBlocks("event.whenthisspriteclicked", spriteName)
        }

        eventManager.on(EventType.STOP_ALL) {
            stopAll()
        }
    }

    private fun executeEventBlocks(opcode: String, filter: String? = null) {
        blocks.values
            .filter { it.opcode == opcode }
            .forEach { block ->
                // Filter berdasarkan parameter (misal: tombol tertentu)
                if (filter != null) {
                    val blockKey = block.fields["KEY_OPTION"] as? String
                    if (blockKey != filter) return@forEach
                }

                // Cari Sprite (sprite) yang memiliki blok ini
                val Sprite = targets.find { it.name == block.parent }
                if (Sprite != null) {
                    startThread(block.id, Sprite)
                }
            }
    }

    private fun startThread(blockId: String, Sprite: Sprite) {
        val thread = Thread {
            executeBlockSequence(blockId, Sprite)
        }
        thread.start()
        activeThreads.add(thread)
    }

    private fun executeBlockSequence(startBlockId: String, Sprite: Sprite) {
        var currentId = startBlockId
        while (currentId != null && isRunning) {
            val block = blocks[currentId] ?: break

            // Handle blok kontrol khusus
            when (block.opcode) {
                "control.waituntil" -> {
                    val condition = evaluateCondition(block, Sprite)
                    while (!condition && isRunning) {
                        Thread.sleep(50) // polling setiap 50ms
                    }
                }
                "control.stop" -> {
                    break
                }
                else -> {
                    val primitive = primitives[block.opcode]
                    primitive?.invoke(block, Sprite)
                }
            }

            currentId = block.next.orEmpty()
        }

        // Hapus thread dari list ketika selesai
        activeThreads.remove(Thread.currentThread())
    }

    private fun evaluateCondition(block: Block, Sprite: Sprite): Boolean {
        // Contoh: kondisi "key space pressed?"
        val condition = block.fields["CONDITION"] as? String
        return when (condition) {
            "key pressed" -> {
                val key = block.fields["KEY"] as? String
                keyState[key] == true
            }
            else -> false
        }
    }

    // State untuk tombol yang sedang ditekan
    private val keyState = mutableMapOf<String, Boolean>()

    fun setKeyState(key: String, pressed: Boolean) {
        keyState[key] = pressed
        if (pressed) {
            eventManager.emit(Event(EventType.KEY_PRESSED, key))
        } else {
            eventManager.emit(Event(EventType.KEY_RELEASED, key))
        }
    }

    fun start() {
        isRunning = true
        eventManager.emit(Event(EventType.GREEN_FLAG))
    }

    fun stopAll() {
        isRunning = false
        activeThreads.forEach { it.interrupt() }
        activeThreads.clear()
    }

    fun addTarget(Sprite: Sprite) = targets.add(Sprite)
    fun addBlock(block: Block, parentTarget: String) {
        block.parent = parentTarget
        blocks[block.id] = block
    }

    companion object {
        val primitives = mapOf(
            "motion.move" to { block: Block, Sprite: Sprite ->
                val steps = (block.fields["STEPS"] as? Number)?.toFloat() ?: 0f
                val rad = Math.toRadians(Sprite.direction.toDouble())
                Sprite.x += steps * cos(rad).toFloat()
                Sprite.y += steps * sin(rad).toFloat()
                true
            },
            "motion.turnright" to { block: Block, Sprite: Sprite ->
                val degrees = (block.fields["DEGREES"] as? Number)?.toInt() ?: 15
                Sprite.direction = (Sprite.direction + degrees) % 360
                true
            },
            "looks.say" to { block: Block, sprite: Sprite ->
                val message = block.fields["MESSAGE"] as? String ?: ""
                sprite.currentMessage = message
                sprite.messageExpiry = System.currentTimeMillis() + 2000
                true
            }
        )
    }
}


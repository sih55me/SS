package cakar.search.com

import cakar.search.com.ProjectComponent.*
import kotlin.math.cos
import kotlin.math.sin

class SVM {
    private val targets = mutableListOf<Sprite>()
    private val blocks = mutableMapOf<String, Block>()
    private val threads = mutableListOf<Thread>()
    private var isRunning = false

    // Daftar "Primitive" (fungsi untuk setiap opcode)
    private val primitives = mapOf(
        "motion.move" to { block: Block, target: Sprite ->
            val steps = (block.fields["STEPS"] as? Number)?.toFloat() ?: 0f

            target.x += steps * cos(Math.toRadians(target.direction.toDouble())).toFloat()
            target.y += steps * sin(Math.toRadians(target.direction.toDouble())).toFloat()
            true
        },
        "motion.turnright" to { block: Block, target: Sprite ->
            val degrees = (block.fields["DEGREES"] as? Number)?.toInt() ?: 15
            target.direction = (target.direction + degrees) % 360
            true
        },
        "control.wait" to { block: Block, target: Sprite ->
            val seconds = (block.fields["DURATION"] as? Number)?.toLong() ?: 1000L
            Thread.sleep(seconds)
            true
        },
        "data.setvariable" to { block: Block, target: Sprite ->
            val varName = block.fields["VARIABLE"] as? String
            val value = block.inputs["VALUE"] ?: 0
            target.variables[varName.orEmpty()] = value
            true
        }
    )

    // Eksekusi blok dengan sequencer sederhana
    fun executeBlock(blockId: String, target: Sprite): Boolean {
        var currentId = blockId
        while (currentId != null && isRunning) {
            val block = blocks[currentId] ?: break
            val primitive = primitives[block.opcode]

            if (primitive != null) {
                val success = primitive(block, target)
                if (!success) break
            }

            currentId = block.next.orEmpty()
        }
        return true
    }

    // Green Flag - mulai eksekusi dari semua script
    fun start() {
        isRunning = true
        // Cari semua blok "event.whenflagclicked"
        val startBlocks = blocks.values.filter { it.opcode == "event.whenflagclicked" }

        startBlocks.forEach { block ->
            Thread {
                val stage = targets.find { it.isStage } ?: return@Thread
                executeBlock(block.id, stage)
            }.start()
        }
    }

    fun stopAll() {
        isRunning = false
    }

    // Tambah target (sprite)
    fun addTarget(target: Sprite) = targets.add(target)

    // Tambah block
    fun addBlock(block: Block) = blocks.put(block.id, block)



}
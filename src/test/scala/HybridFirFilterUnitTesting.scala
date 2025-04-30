package cancellers

import chisel3._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import scala.math._

import cancellers.CancellersTopModule
// import cancellers.{LMSFIRFilter, LMSFIRFilter_Transpose}

class HybridFir(tapCount: Int, segmentCount: Int) extends Module {
    val io = IO(new Bundle {
        val din          = Input(SInt(7.W))
        val dinValid     = Input(Bool())
        val dout         = Output(SInt(20.W))
        val desired      = Input(SInt(8.W))

        // For debugging
        val weightPeek   = Output(Vec(segmentCount, SInt(10.W)))
        val errors       = Output(Vec(tapCount/segmentCount - 1, SInt(20.W)))
        val inputWeightShifters = Output(Vec(((tapCount/segmentCount * (segmentCount - 1)) + tapCount/segmentCount), SInt(10.W)))
    })
    val dut = Module(new HybridAdaptiveFIRFilter(tapCount, segmentCount))
    dut.io.din := io.din
    dut.io.dinValid := io.dinValid
    dut.io.desired := io.desired
    io.weightPeek := dut.io.weightPeek
    io.dout := dut.io.dout
    // io.input0 := dut.io.input0

    io.dout := dut.io.dout
    io.errors := dut.io.errors
    io.inputWeightShifters := dut.io.inputWeightShifters
}            

class HybridFirFilterTest extends AnyFreeSpec with ChiselScalatestTester {

    // TODO: use actual data
  "Basic echo functionality test" in {
    test(
      new HybridFir(
        6, 3
      )
    ).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.din.poke(2.S(3.W))
      dut.io.dinValid.poke(true.B)
      dut.io.desired.poke(64.S(18.W))

      // CYCLE 0 -> 1: InputWeightShifters(0) = 2, inputShifters(0) = 2, errorShifter(0) = 0, dout = 0
      dut.clock.step()
      dut.io.din.poke(2.S(3.W))
      dut.io.desired.poke(64.S(18.W))
      dut.io.dout.expect(0.S)
      // CYCLE 1 -> 2: InputWeightShifters(1) = 2, inputShifters(1) = 2, errorShifter(0) = (64 - 0) >> 5, dout = 0
      dut.clock.step()
      dut.io.din.poke(2.S(3.W))
      dut.io.desired.poke(64.S(18.W))
      dut.io.dout.expect(0.S)
      // CYCLE 2 -> 3: InputWeightShifters(2) = 2, inputShifters(2) = 2, errorShifter(0) = (64 - 0) >> 5, dout = 0
      dut.clock.step()
      dut.io.din.poke(2.S(3.W))
      dut.io.desired.poke(64.S(18.W))
      dut.io.dout.expect(8.S)
      // CYCLE 3 -> 4: InputWeightShifters(3) = 2, inputShifters(3) = 2, errorShifter(0) = (64 - 0) >> 5
      dut.clock.step()
      dut.io.din.poke(2.S(3.W))
      dut.io.desired.poke(64.S(18.W))
      dut.io.dout.expect(24.S)
      // CYCLE 4 -> 5: InputWeightShifters(4) = 2, inputShifters(4) = 2, errorShifter(0) = (64 - 0) >> 5
      dut.clock.step()
      dut.io.din.poke(2.S(3.W))
      dut.io.desired.poke(64.S(18.W))
      dut.io.dout.expect(48.S)
      // CYCLE 5 -> 6: InputWeightShifters(5) = 2, inputShifters(4) = 2, errorShifter(0) = (64 - 0) >> 5
      dut.clock.step()
      dut.io.din.poke(2.S(3.W))
      dut.io.desired.poke(64.S(18.W))
      dut.io.dout.expect(80.S)
      // CYCLE 6 -> 7: InputWeightShifters(5) = 2, inputShifters(4) = 2, errorShifter(0) = (64 - 0) >> 5, dout = 4
      dut.clock.step()
      dut.io.din.poke(2.S(3.W))
      dut.io.desired.poke(64.S(18.W))
      dut.io.dout.expect(100.S)

      dut.clock.step() // din gets added to first reg in input shifters
      // first check the output (0 * 2)
      // poke new in value and io.desired
      // clock step
      // new weight should be calculated 
      // check first inputWeightShifter, should be 2

    }
  }

  // TODO: would be nice to work with realistic data
  // This is closer to what we'd be running with the correct FIRSegment size of 4 taps.
  "Testing a twelve-tap FIR with four-tap FIRSegments" in {
    test(
      new HybridFir(
        12, 4
      )
    ).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.din.poke(2.S(3.W))
      dut.io.dinValid.poke(true.B)
      dut.io.desired.poke(64.S(18.W))
      // println("Weight Peek: " + dut.io.weightPeek.peek())

      // CYCLE 0 -> 1: 
      // InputWeightShifters(0) = 2
      // inputShifters(0) = 2
      // errorShifter(0) = 0, dout = 0
      dut.clock.step()
      dut.io.din.poke(-2.S(3.W))
      dut.io.desired.poke(128.S(18.W))
      // println("Weight Peek: " + dut.io.weightPeek.peek())
      dut.io.dout.expect(0.S)

      // CYCLE 1 -> 2: 
      // InputWeightShifters(0) = -2, InputWeightShifters(1) = 2
      // inputShifters(0) = -2, inputShifters(1) = 2
      // errorShifter(0) = -4, errorShifter(1) = 0
      dut.clock.step()
      dut.io.din.poke(1.S(3.W))
      dut.io.desired.poke(84.S(18.W))
      // println("Weight Peek: " + dut.io.weightPeek.peek())
      dut.io.dout.expect(0.S)

      // CYCLE 2 -> 3:
      // InputWeightShifters(0) = 1, InputWeightShifters(1) = -2, InputWeightShifters(2) = 2
      // inputShifters(0) = 1, inputShifters(1) = -2, inputShifters(2) = 2
      // errorShifter(0) = -3, errorShifter(1) = -4
      // Note that -84 >> 5 == 3
      // weight(0) = 8
      dut.clock.step()
      dut.io.din.poke(-1.S(3.W))
      dut.io.desired.poke(33.S(18.W))
      // println("Weight Peek: " + dut.io.weightPeek.peek())
      dut.io.dout.expect(0.S)

      // CYCLE 3 -> 4:
      // InputWeightShifters(0) = -1, InputWeightShifters(1) = 1, InputWeightShifters(2) = -2, InputWeightShifters(3) = 2
      // inputShifters(0) = -1, inputShifters(1) = 1, inputShifters(2) = -2, inputShifters(3) = 2
      // errorShifter(0) = -2, errorShifter(1) = -3
      // weight(0) = 2, weight(1) = 6 
      dut.clock.step()
      dut.io.din.poke(0.S(3.W))
      dut.io.desired.poke(47.S(18.W))
      // println("Weight Peek: " + dut.io.weightPeek.peek())
      dut.io.dout.expect(-8.S)

      // CYCLE 4 -> 5:
      // InputWeightShifters(0) = 0, InputWeightShifters(1) = -1, InputWeightShifters(2) = 1, InputWeightShifters(3) = -2, InputWeightShifters(4) = 2
      // inputShifters(0) = 0, inputShifters(1) = -1, inputShifters(2) = 1, inputShifters(3) = -2, inputShifters(4) = 2
      // errorShifter(0) = -2, errorShifter(1) = -2
      // weight(0) = 4, weight(1) = 2, weight(2) = 4
      dut.clock.step()
      dut.io.din.poke(2.S(3.W))
      dut.io.desired.poke(22.S(18.W))
      // println("Weight Peek: " + dut.io.weightPeek.peek())
      dut.io.dout.expect(-6.S) // weight(0) * InputWeightShifters(0) + weight(1) * InputWeightShifters(1)

      // CYCLE 5 -> 6:
      // InputWeightShifters(0) = 2, InputWeightShifters(1) = 0, InputWeightShifters(2) = -1, InputWeightShifters(3) = 1, InputWeightShifters(4) = -2, InputWeightShifters(5) = 2
      // inputShifters(0) = 2, inputShifters(1) = 0, inputShifters(2) = -1, inputShifters(3) = 1, inputShifters(4) = -2, inputShifters(5) = 2
      // errorShifter(0) = -1, errorShifter(1) = -2
      // weight(0) = 2, weight(1) = 4, weight(2) = 0, weight(3) = 4
      dut.clock.step()
      dut.io.din.poke(1.S(3.W))
      dut.io.desired.poke(-11.S(18.W))
      // println("Weight Peek: " + dut.io.weightPeek.peek())
      dut.io.dout.expect(4.S) // weight(0) * InputWeightShifters(0) + weight(1) * InputWeightShifters(1) + weight(2) * InputWeightShifters(2)

      // CYCLE 6 -> 7:
      // InputWeightShifters(0) = 1, InputWeightShifters(1) = 2, InputWeightShifters(2) = 0, InputWeightShifters(3) = -1, InputWeightShifters(4) = 1, InputWeightShifters(5) = -2, InputWeightShifters(6) = 2
      // inputShifters(0) = 1, inputShifters(1) = 2, inputShifters(2) = 0, inputShifters(3) = -1, inputShifters(4) = 1, inputShifters(5) = -2, inputShifters(6) = 2
      // errorShifter(0) = 0, errorShifter(1) = -1
      dut.clock.step()
      dut.io.din.poke(-1.S(3.W))
      dut.io.desired.poke(33.S(18.W))
      // println("Weight Peek: " + dut.io.weightPeek.peek())
      dut.io.dout.expect(6.S)
    }
  }

  "Simulated Incoming Rx Data with One 'NEXT' Source" in {
    test(
      new HybridFir(6, 2)
    ) // 20-bit coefficients, 4 taps
    .withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

      // Set a seed 
      val rand = new Random(1)
      val steps = 50

      // // Buffers for debugging/plotting
      // val remoteSignalHistory = scala.collection.mutable.ArrayBuffer[Int]()
      // val localTx1History = scala.collection.mutable.ArrayBuffer[Int]()
      // val receivedHistory = scala.collection.mutable.ArrayBuffer[Int]()
      val outputHistory = scala.collection.mutable.ArrayBuffer[Int]()

      // // NEXT parameters
      // val nextLossDb = 36 // Typical 1000Base-T NEXT
      // val nextCoupling = math.pow(10, -nextLossDb / 20.0)

      // // Initial signals
      // var remoteSignal = Random.between(-4, 4)
      // var localTx1 = Random.between(-4, 4)

      val localTx1 = ArrayBuffer(
        -28, 22, 29, -31, -6, 27, 30, 3, -12, -28,
        30, 9, -23, -1, 14, -27, 21, -15, 13, 16,
        21, 4, 1, 26, -10, 6, 14, -15, 26, -2,
        24, 16, -27, -32, -2, -15, -8, 6, 14, -2,
        8, 25, 23, 28, -24, 9, -12, -4, 20, -2
      )

      val received = ArrayBuffer(
        -56, 29, 46, -51, -27, 15, 20, 25, 1, -70,
        25, -3, -13, 5, -5, -45, 53, -15, 23, 48,
        54, -24, -4, 27, -21, -14, -7, -37, 41, 6,
        36, 36, -67, -63, -9, -5, -12, 39, 43, 15,
        36, 50, 55, 51, -39, 21, 0, -33, 0, 27
      )

      val remoteSignal = ArrayBuffer(
        -14, -4, 3, -4, -18, -25, -25, 21, 19, -28,
        -20, -16, 22, 7, -26, -4, 22, 8, 4, 24,
        23, -30, -5, -12, -6, -23, -28, -14, 2, 9,
        0, 12, -26, -15, -6, 18, 0, 30, 22, 18,
        24, 13, 21, 9, -3, 8, 18, -27, -30, 30
      )

      dut.io.dinValid.poke(false.B)
      dut.clock.step()


      for (i <- 0 until steps) {

        // // Random walks for signal changes
        // remoteSignal = (Random.between(-32, 31)).max(-32).min(31)
        // localTx1 = (Random.between(-32, 31)).max(-32).min(31)

        // // NEXT contribution from localTx1. Based on Python model to just check for accuracy.
        // val next1 = ((localTx1 * 3) >> 1).round.toInt

        // // Received signal = Remote + NEXT1 only. Probably won't hit these limits
        // val received = (remoteSignal + next1).max(-64).min(63)

        // Feed into DUT
        dut.io.din.poke(localTx1(i).S(7.W))          // TX1 data
        dut.io.desired.poke(received(i).S(8.W))       // RX signal (remote + NEXT1)
        dut.io.dinValid.poke(true.B)
        dut.clock.step()

        // Record
        // remoteSignalHistory += remoteSignal
        // localTx1History += localTx1
        // receivedHistory += received
        outputHistory += (dut.io.dout.peek().litValue.toInt)

        // println(s"$i, $remoteSignal, $receivedSignal, ${receivedSignal - cleanedOutputs.last}, ${noise}")
        // println(s"$i, $remoteSignal, $received, ${received - outputHistory.last}, ${localTx1}, ${next1}, ${dut.io.weightPeek.peek()}")
        println(s"$i, Input: ${localTx1(i)}, Received: ${received(i)}, DOut: ${dut.io.dout.peek()}, Error: ${received(i) - outputHistory.last}, Weights: ${dut.io.weightPeek.peek()}")
        // println(s"$i, Input: $localTx1, Received: $received, DOut: ${dut.io.dout.peek()}, Error: ${received - outputHistory.last}, Weights: ${dut.io.weightPeek.peek()}, Errors: ${dut.io.errors.peek()}, Delayed Inputs: ${dut.io.inputWeightShifters.peek()}")
      }
    }
  }
}
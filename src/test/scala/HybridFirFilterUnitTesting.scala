package cancellers

import chisel3._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec
import scala.util.Random
import scala.math._

import cancellers.CancellersTopModule
// import cancellers.{LMSFIRFilter, LMSFIRFilter_Transpose}

class HybridFir(tapCount: Int, segmentCount: Int) extends Module {
    val io = IO(new Bundle {
        val din          = Input(SInt(3.W))
        val dinValid     = Input(Bool())
        val dout         = Output(SInt(10.W))
        val desired      = Input(SInt(6.W))

        // For debugging
        // val weightPeek   = Output(Vec(segmentCount, SInt(10.W)))
    })
    val dut = Module(new HybridAdaptiveFIRFilter(tapCount, segmentCount))
    dut.io.din := io.din
    dut.io.dinValid := io.dinValid
    dut.io.desired := io.desired
    // io.weightPeek := dut.io.weightPeek
    // io.input0 := dut.io.input0

    io.dout := dut.io.dout
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

  "Simulated Incoming Rx Data" in {
    test(
      new HybridFir(40, 2)
    ) // 20-bit coefficients, 4 taps
    .withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val steps = 500
      val noiseAmplitude = 1
      
      // Signal containers
      val perfectRemoteTx = scala.collection.mutable.ArrayBuffer[Int]()
      val localTxNoise = scala.collection.mutable.ArrayBuffer[Int]()
      val receivedNoisySignal = scala.collection.mutable.ArrayBuffer[Int]()
      val cleanedOutputs = scala.collection.mutable.ArrayBuffer[Int]()

      // 1. Generate independent signals
      var remoteSignal = Random.between(-4, 4)  // Perfect data we want to recover
      var localNoise = Random.between(-4, 4)    // Local TX interference

      for (i <- 0 until steps) {
        remoteSignal = (remoteSignal + Random.between(-1, 2)).max(-4).min(3)
        localNoise = (localNoise + Random.between(-1, 2)).max(-4).min(3)

        val channelNoise = (Random.nextGaussian() * noiseAmplitude).round.toInt
        val receivedSignal = remoteSignal + (localNoise * 1) // + channelNoise  // Scale local noise

        dut.io.din.poke(localNoise.S(5.W))       // Local TX interference we know about
        dut.io.desired.poke(receivedSignal.S(6.W)) // Received signal (remote + noise)
        dut.io.dinValid.poke(true.B)
        dut.clock.step()

        perfectRemoteTx += remoteSignal
        localTxNoise += localNoise
        receivedNoisySignal += receivedSignal
        cleanedOutputs += dut.io.dout.peek().litValue.toInt

        println(s"$i, $remoteSignal, $receivedSignal, ${receivedSignal - cleanedOutputs.last}")
      }
    }
  }
}
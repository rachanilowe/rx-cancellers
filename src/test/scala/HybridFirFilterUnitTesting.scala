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

  "Clean up sine wave" in {
    test(
      new HybridFir(
        20, 4
      )
    ).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val steps = 150
      val period = 30  // you can tweak this for faster/slower oscillation
      val amplitude = 5  // for 3-bit signed: max abs value = 3
      val noiseAmplitude = 1  // noise added to the desired signal
      val quantizedSines = scala.collection.mutable.ArrayBuffer[Int]()
      val noisySines = scala.collection.mutable.ArrayBuffer[Int]()
      val outputs = scala.collection.mutable.ArrayBuffer[BigInt]()
      var currentValue = Random.between(-4, 4)

      for (i <- 0 until steps) {
        // val quantizedSine = Random.between(-3, 4)  // Matches 3-bit signed range (-4 to 3)

        // val noise = Random.nextGaussian() * noiseAmplitude
        // val noisySine = (quantizedSine * 4 + noise).round.toInt
        val delta = Random.between(-1, 2)
        currentValue = (currentValue + delta).max(-4).min(3)

        val quantizedSine = currentValue
        val noise = Random.nextGaussian() * noiseAmplitude
        val noisySine = (quantizedSine * 4 + noise).round.toInt

        // Poke signals
        dut.io.din.poke(quantizedSine.S(5.W))
        dut.io.dinValid.poke(true.B)
        dut.io.desired.poke(noisySine.S(5.W))

        // Advance clock
        dut.clock.step()
        val dout = dut.io.dout.peek().litValue

        quantizedSines += quantizedSine
        noisySines += noisySine
        outputs += dout
        
        println(s"$i, $quantizedSine, $noisySine, $dout")
      }
    }
  }
}
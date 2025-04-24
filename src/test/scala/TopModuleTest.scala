package cancellers

import chisel3._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec
import scala.util.Random
import scala.math._

import cancellers.CancellersTopModule
// import cancellers.{LMSFIRFilter, LMSFIRFilter_Transpose}

class TopModuleBlock(echoTapCount: Int, nextTapCount: Int, segmentSize: Int) extends Module {
    val io = IO(new Bundle {
      val tx0 = Input(SInt(5.W)) // echo
      val tx1 = Input(SInt(5.W)) // next 1
      val tx2 = Input(SInt(5.W)) // next 2
      val tx3 = Input(SInt(5.W)) // next3
      val txValid = Input(Bool())

      val desired   = Input(SInt(6.W)) // RX signal
      val desiredCancelled = Output(SInt(6.W)) // Cancelled RX signal

    })
    val dut = Module(new CancellersTopModule(echoTapCount, nextTapCount, segmentCount))
    dut.io.tx0 := io.tx0
    dut.io.tx1 := io.tx1
    dut.io.tx2 := io.tx2
    dut.io.tx3 := io.tx3

    dut.io.txValid := io.txValid
    dut.io.desired := io.desired

    io.desiredCancelled := dut.io.desiredCancelled
}            

class TopModuleTest extends AnyFreeSpec with ChiselScalatestTester {

    // TODO: use actual data
  "Basic topmodule test" in {
    test(
      new TopModuleBlock(
        6, 6, 3 // echo and next have 6 taps with segment size of 3
      )
    ).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.tx0.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.txValid.poke(true.B)
      dut.io.desired.poke(32.S(6.W)) 

      // CYCLE 0 -> 1: InputWeightShifters(0) = 2, inputShifters(0) = 2, errorShifter(0) = 0, dout = 0
      dut.clock.step()
      dut.io.tx0.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.txValid.poke(true.B) 
      dut.io.desired.poke(32.S(6.W))
      dut.io.desiredCancelled.expect(32.S)
      val peek_out = dut.io.desiredCancelled.peek()
      println(s"$peek_out")
      // CYCLE 1 -> 2: InputWeightShifters(1) = 2, inputShifters(1) = 2, errorShifter(0) = (64 - 0) >> 5, dout = 0
      dut.clock.step()
      dut.io.tx0.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.txValid.poke(true.B)
      dut.io.desired.poke(32.S(6.W))
      dut.io.desiredCancelled.expect(32.S)
      val peek_out = dut.io.desiredCancelled.peek()
      println(s"$peek_out")
      // CYCLE 2 -> 3: InputWeightShifters(2) = 2, inputShifters(2) = 2, errorShifter(0) = (64 - 0) >> 5, dout = 0
      dut.clock.step()
      dut.io.tx0.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.txValid.poke(true.B)
      dut.io.desired.poke(32.S(6.W))
      dut.io.desiredCancelled.expect(0.S)
      val peek_out = dut.io.desiredCancelled.peek()
      println(s"$peek_out")
      // CYCLE 3 -> 4: InputWeightShifters(3) = 2, inputShifters(3) = 2, errorShifter(0) = (64 - 0) >> 5
      dut.clock.step()
      dut.io.tx0.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.txValid.poke(true.B)
      dut.io.desired.poke(32.S(6.W))
      val peek_out = dut.io.desiredCancelled.peek()
      println(s"$peek_out")
      // CYCLE 4 -> 5: InputWeightShifters(4) = 2, inputShifters(4) = 2, errorShifter(0) = (64 - 0) >> 5
      dut.clock.step()
      dut.io.tx0.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.txValid.poke(true.B)
      dut.io.desired.poke(32.S(6.W))
      val peek_out = dut.io.desiredCancelled.peek()
      println(s"$peek_out")
      // CYCLE 5 -> 6: InputWeightShifters(5) = 2, inputShifters(4) = 2, errorShifter(0) = (64 - 0) >> 5
      dut.clock.step()
      dut.io.tx0.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.txValid.poke(true.B)
      dut.io.desired.poke(32.S(6.W))
      val peek_out = dut.io.desiredCancelled.peek()
      println(s"$peek_out")
      // CYCLE 6 -> 7: InputWeightShifters(5) = 2, inputShifters(4) = 2, errorShifter(0) = (64 - 0) >> 5, dout = 4
      dut.clock.step()
      dut.io.tx0.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.txValid.poke(true.B)
      dut.io.desired.poke(32.S(6.W))
      val peek_out = dut.io.desiredCancelled.peek()
      println(s"$peek_out")

      dut.clock.step() // din gets added to first reg in input shifters
      // first check the output (0 * 2)
      // poke new in value and io.desired
      // clock step
      // new weight should be calculated 
      // check first inputWeightShifter, should be 2

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
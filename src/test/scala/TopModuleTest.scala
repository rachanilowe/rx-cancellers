package cancellers

import chisel3._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec
import scala.util.Random
import scala.math._

import cancellers.CancellersTopModule
// import cancellers.{LMSFIRFilter, LMSFIRFilter_Transpose}

class TopModuleBlock(echoTapCount: Int, nextTapCount: Int, segmentCount: Int) extends Module {
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
      dut.io.desired.poke(31.S(6.W)) 

      // CYCLE 0 -> 1: InputWeightShifters(0) = 2, inputShifters(0) = 2, errorShifter(0) = 0, dout = 0
      dut.clock.step()
      dut.io.tx0.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.txValid.poke(true.B) 
      dut.io.desired.poke(31.S(6.W))
      dut.io.desiredCancelled.expect(31.S)
      val peek_out = dut.io.desiredCancelled.peek()
      println(s"$peek_out")
      // CYCLE 1 -> 2: InputWeightShifters(1) = 2, inputShifters(1) = 2, errorShifter(0) = (64 - 0) >> 5, dout = 0
      dut.clock.step()
      dut.io.tx0.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.txValid.poke(true.B)
      dut.io.desired.poke(31.S(6.W))
      dut.io.desiredCancelled.expect(31.S)
      println(s"$peek_out")
      // CYCLE 2 -> 3: InputWeightShifters(2) = 2, inputShifters(2) = 2, errorShifter(0) = (64 - 0) >> 5, dout = 0
      dut.clock.step()
      dut.io.tx0.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.txValid.poke(true.B)
      dut.io.desired.poke(31.S(6.W))
      // dut.io.desiredCancelled.expect(31.S)
      println(s"$peek_out")
      // CYCLE 3 -> 4: InputWeightShifters(3) = 2, inputShifters(3) = 2, errorShifter(0) = (64 - 0) >> 5
      dut.clock.step()
      dut.io.tx0.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.txValid.poke(true.B)
      dut.io.desired.poke(31.S(6.W))
      println(s"$peek_out")
      // CYCLE 4 -> 5: InputWeightShifters(4) = 2, inputShifters(4) = 2, errorShifter(0) = (64 - 0) >> 5
      dut.clock.step()
      dut.io.tx0.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.txValid.poke(true.B)
      dut.io.desired.poke(31.S(6.W))
      println(s"$peek_out")
      // CYCLE 5 -> 6: InputWeightShifters(5) = 2, inputShifters(4) = 2, errorShifter(0) = (64 - 0) >> 5
      dut.clock.step()
      dut.io.tx0.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.txValid.poke(true.B)
      dut.io.desired.poke(31.S(6.W))
      println(s"$peek_out")
      // CYCLE 6 -> 7: InputWeightShifters(5) = 2, inputShifters(4) = 2, errorShifter(0) = (64 - 0) >> 5, dout = 4
      dut.clock.step()
      dut.io.tx0.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.tx1.poke(2.S(3.W))
      dut.io.txValid.poke(true.B)
      dut.io.desired.poke(31.S(6.W))
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
      new TopModuleBlock(
        80, 60, 4 // echo and next have 6 taps with segment size of 3
      )
    ).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val steps = 150
    val period = 30
    val noiseAmplitude = 1

    val randWalk = Array.fill(4)(Random.between(-4, 4))
    val outputs = scala.collection.mutable.ArrayBuffer[BigInt]()

    for (i <- 0 until steps) {
      val quantizedSines = new Array[Int](4)  
      val noisySines = new Array[Int](4)    

      for (j <- 0 until 4) {
        val delta = Random.between(-1, 2)
        randWalk(j) = (randWalk(j) + delta).max(-4).min(3)
        quantizedSines(j) = randWalk(j)
        val noise = Random.nextGaussian() * noiseAmplitude
        noisySines(j) = (quantizedSines(j) * 4 + noise).round.toInt
      }

      // Inject signals
      dut.io.tx0.poke(quantizedSines(0).S)
      dut.io.tx1.poke(quantizedSines(1).S)
      dut.io.tx2.poke(quantizedSines(2).S)
      dut.io.tx3.poke(quantizedSines(3).S)
      dut.io.txValid.poke(true.B)

      // Sum the noisy transmit signals and inject as desired
      val desiredSignal = noisySines.sum.max(-32).min(31)
      dut.io.desired.poke(desiredSignal.S)

      dut.clock.step()
      val dout = dut.io.desiredCancelled.peek().litValue
      outputs += dout

      println(f"$i%3d | tx0: ${quantizedSines(0)}%2d tx1: ${quantizedSines(1)}%2d tx2: ${quantizedSines(2)}%2d tx3: ${quantizedSines(3)}%2d | desired: $desiredSignal%4d | dout: $dout%4d")
      }
    }
  }
}
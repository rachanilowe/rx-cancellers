package cancellers

import chisel3._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import scala.math._
import scala.io.Source
import java.io.{File, PrintWriter}

import cancellers.CancellersTopModule
// import cancellers.{LMSFIRFilter, LMSFIRFilter_Transpose}

class TopModuleBlock(echoTapCount: Int, nextTapCount: Int, segmentCount: Int) extends Module {
    val io = IO(new Bundle {
      val tx0 = Input(SInt(6.W)) // echo
      val tx1 = Input(SInt(6.W)) // next 1
      val tx2 = Input(SInt(6.W)) // next 2
      val tx3 = Input(SInt(6.W)) // next3
      val txValid = Input(Bool())

      val desired   = Input(SInt(8.W)) // RX signal
      val desiredCancelled = Output(SInt(8.W)) // Cancelled RX signal

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

  "Simulated Incoming Rx Data" in {
    test(
      new TopModuleBlock(40, 20, 4)
    ) // 20-bit coefficients, 4 taps
    .withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val steps = 500      
      // Signal containers
      val perfectRemoteTx = scala.collection.mutable.ArrayBuffer[Int]()
      val localTxNoise0 = scala.collection.mutable.ArrayBuffer[Int]()
      val localTxNoise1 = scala.collection.mutable.ArrayBuffer[Int]()
      val localTxNoise2 = scala.collection.mutable.ArrayBuffer[Int]()
      val localTxNoise3 = scala.collection.mutable.ArrayBuffer[Int]()
      val receivedNoisySignal = scala.collection.mutable.ArrayBuffer[Int]()
      val cleanedOutputs = scala.collection.mutable.ArrayBuffer[Int]()

      // 1. Generate independent signals
      var remoteSignal = Random.between(-4, 4)  // Perfect data we want to recover
      var localNoise0 = Random.between(-4, 4)    // Local TX interference
      var localNoise1 = Random.between(-4, 4)    // Local TX interference
      var localNoise2 = Random.between(-4, 4)    // Local TX interference
      var localNoise3 = Random.between(-4, 4)    // Local TX interference

      for (i <- 0 until steps) {
        remoteSignal = (remoteSignal + Random.between(-3, 4)).max(-128).min(127)
        localNoise0 = (localNoise0 + Random.between(-2, 3)).max(-32).min(31)
        localNoise1 = (localNoise1 + Random.between(-2, 3)).max(-32).min(31)
        localNoise2 = (localNoise2 + Random.between(-2, 3)).max(-32).min(31)
        localNoise3 = (localNoise3 + Random.between(-2, 3)).max(-32).min(31)

        // val channelNoise = (Random.nextGaussian() * noiseAmplitude).round.toInt
        val receivedSignal = remoteSignal + (localNoise0 >> 3) + (localNoise1 >> 4) + (localNoise2 >> 4) + (localNoise2 >> 4) // + channelNoise  // Scale local noise

        // dut.io.din.poke(localNoise.S(6.W))       // Local TX interference we know about
        // dut.io.desired.poke(receivedSignal.S(8.W)) // Received signal (remote + noise)
        // dut.io.dinValid.poke(true.B)
        // dut.clock.step()

        dut.io.tx0.poke(localNoise0.S(6.W))
        dut.io.tx1.poke(localNoise1.S(6.W))
        dut.io.tx2.poke(localNoise2.S(6.W))
        dut.io.tx3.poke(localNoise3.S(6.W))
        dut.io.txValid.poke(true.B)
        dut.io.desired.poke(receivedSignal.S(8.W))
        dut.clock.step()

        perfectRemoteTx += remoteSignal
        localTxNoise0 += localNoise0
        localTxNoise1 += localNoise1
        localTxNoise2 += localNoise2
        localTxNoise3 += localNoise3
        receivedNoisySignal += receivedSignal
        cleanedOutputs += (dut.io.desiredCancelled.peek().litValue.toInt)

        println(s"$i, $remoteSignal, $receivedSignal, ${cleanedOutputs.last}")
      }
    }
  }

  "same data" in {
    test(
      new TopModuleBlock(40, 20, 4)
    ) // 20-bit coefficients, 4 taps
    .withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
        val lines = Source.fromFile("random_signals.csv").getLines().drop(1) // skip header
        val data = lines.map(_.split(",").map(_.toInt)).toArray
        val cleanedOutputs = scala.collection.mutable.ArrayBuffer[Int]()

        for (i <- data.indices) {
          val Array(remote, tx0, tx1, tx2, tx3) = data(i)
          val recieved = remote + (tx0>>3) + (tx1 >> 4) + (tx2 >> 4) + (tx3 >> 4)
          dut.io.tx0.poke(tx0.S(6.W))
          dut.io.tx1.poke(tx1.S(6.W))
          dut.io.tx2.poke(tx2.S(6.W))
          dut.io.tx3.poke(tx3.S(6.W))
          dut.io.txValid.poke(true.B)
          dut.io.desired.poke(recieved.S(8.W))
          dut.clock.step()
          cleanedOutputs += (dut.io.desiredCancelled.peek().litValue.toInt)
          println(s"$i, $remote, $recieved, ${cleanedOutputs.last}")
        }
      }
    }

    "python data" in {
    test(
      new TopModuleBlock(12, 4, 2)
    ) // 20-bit coefficients, 4 taps
    .withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val cleanedOutputs = scala.collection.mutable.ArrayBuffer[Int]()
      val originalData = ArrayBuffer(2, -14, -11, 8, -11, 5, 8, -1, 7, 11, -16, -15, -2, -7, 8, -4, 1, -5, -4, 4)
      val input0   = ArrayBuffer(-14, 11, 14, -16, -3, 13, 15, 1, -6, -14, 15, 4, -12, -1, 7, -14, 10, -8, 6, 8)
      val input1   = ArrayBuffer(-7, -2, 1, -2, -9, -13, -13, 10, 9, -14, -10, -8, 11, 3, -13, -2, 11, 4, 2, 12)
      val input2   = ArrayBuffer(12, 5, -1, -13, 11, 13, -8, 6, 5, 1, -5, -5, 12, 6, -10, 7, -7, 1, 3, -8)
      val input3   = ArrayBuffer(1, -9, -14, -14, -13, -3, 12, 3, -13, 10, 1, 5, 7, -14, 12, -13, -15, 12, 1, 4)
      val desired  = ArrayBuffer(-17, -16, -9, -22, -6, -11, -6, -3, -15, 9, 13, -1, 3, 3, -11, -15, -8, -9, 9, -13)

        for (i <- 0 until 20) {
          dut.io.tx0.poke(input0(i).S(6.W))
          dut.io.tx1.poke(input1(i).S(6.W))
          dut.io.tx2.poke(input2(i).S(6.W))
          dut.io.tx3.poke(input3(i).S(6.W))
          dut.io.txValid.poke(true.B)
          dut.io.desired.poke(desired(i).S(8.W))
          dut.clock.step()
          cleanedOutputs += (dut.io.desiredCancelled.peek().litValue.toInt)
          println(s"$i, ${originalData(i)}, ${desired(i)}, ${cleanedOutputs.last}")
        }
      }
    }
}
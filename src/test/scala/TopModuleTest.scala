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

  "Simulated Incoming Rx Data" in {
    test(
  new TopModuleBlock(
    echoTapCount = 40,  // Number of taps for echo canceller
    nextTapCount = 40,  // Number of taps for NEXT cancellers
    segmentCount = 2    // Number of segments for hybrid FIR
  )
).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
  val steps = 500
  val noiseAmplitude = 1
  
  // Signal containers
  val perfectRemoteTx = scala.collection.mutable.ArrayBuffer[Int]()
  val txSignals = (
    scala.collection.mutable.ArrayBuffer[Int](),
    scala.collection.mutable.ArrayBuffer[Int](),
    scala.collection.mutable.ArrayBuffer[Int](),
    scala.collection.mutable.ArrayBuffer[Int]()
  )
  val receivedNoisySignal = scala.collection.mutable.ArrayBuffer[Int]()
  val cleanedOutputs = scala.collection.mutable.ArrayBuffer[Int]()

  // Initialize random walk states
  var remoteSignal = Random.between(-4, 4)
  var tx0 = Random.between(-4, 4)
  var tx1 = Random.between(-4, 4)
  var tx2 = Random.between(-4, 4)
  var tx3 = Random.between(-4, 4)

  for (i <- 0 until steps) {
    // Update signals with bounded random walks
    remoteSignal = (remoteSignal + Random.between(-1, 2)).max(-4).min(3)
    tx0 = (tx0 + Random.between(-1, 2)).max(-4).min(3)
    tx1 = (tx1 + Random.between(-1, 2)).max(-4).min(3)
    tx2 = (tx2 + Random.between(-1, 2)).max(-4).min(3)
    tx3 = (tx3 + Random.between(-1, 2)).max(-4).min(3)

    // Simulate received signal (remote + all interference sources)
    val channelNoise = (Random.nextGaussian() * noiseAmplitude).round.toInt
    val receivedSignal = remoteSignal + tx0 + tx1 + tx2 + tx3 // + channelNoise

    // Connect to DUT
    dut.io.tx0.poke(tx0.S(5.W))
    dut.io.tx1.poke(tx1.S(5.W))
    dut.io.tx2.poke(tx2.S(5.W))
    dut.io.tx3.poke(tx3.S(5.W))
    dut.io.txValid.poke(true.B)
    dut.io.desired.poke(receivedSignal.S(6.W))
    dut.clock.step()

    // Record signals
    perfectRemoteTx += remoteSignal
    txSignals._1 += tx0
    txSignals._2 += tx1
    txSignals._3 += tx2
    txSignals._4 += tx3
    receivedNoisySignal += receivedSignal
    cleanedOutputs += dut.io.desiredCancelled.peek().litValue.toInt

    // Print diagnostic output
    println(s"$i, $remoteSignal, $receivedSignal, ${cleanedOutputs.last}")
  }
}

  }
}
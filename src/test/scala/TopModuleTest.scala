package cancellers

import chisel3._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import scala.math._

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

  // "Simulated Incoming Rx Data for all Cancellers" in {
  //   test(
  //     new TopModuleBlock(80, 60, 4)
  //   ) // 20-bit coefficients, 4 taps
  //   .withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

  //     // Set a seed 
  //     val rand = new Random(1)
  //     val steps = 400

  //     // // Buffers for debugging/plotting
  //     // val remoteSignalHistory = scala.collection.mutable.ArrayBuffer[Int]()
  //     // val localTx1History = scala.collection.mutable.ArrayBuffer[Int]()
  //     // val receivedHistory = scala.collection.mutable.ArrayBuffer[Int]()
  //     val outputHistory = scala.collection.mutable.ArrayBuffer[Int]()

  //     val localTx0 = ArrayBuffer(
  //         -29, 23, 28, -31, -7, 27, 30, 3, -11, -28, 29, 8, -23, -2, 14, -27, 22, -16, 14, 16,
  //         21, 5, 0, 27, -11, 6, 13, -16, 25, -1, 25, 15, -27, -31, -3, -15, -7, 5, 15, -3,
  //         8, 25, 24, 27, -24, 8, -11, -5, 20, -2, -29, -28, 31, 7, -24, -23, -12, 18, 15, -14,
  //         -17, -20, 25, -10, -7, 12, 24, 22, 24, -1, 3, -13, -10, -16, 2, 27, 5, -11, -11, -9,
  //         29, 12, 10, 22, -4, -31, -26, 11, 9, -1, -23, 1, 26, 20, -13, 16, 31, -2, 2, 29,
  //         31, -25, -11, 29, 27, 20, -14, 22, 13, 17, 31, -12, 23, -19, 20, -29, -32, 25, -23, -27,
  //         12, -20, -13, -18, 26, 30, -15, 27, 24, 26, 12, 0, 26, 23, 11, -14, -1, 10, -29, -7,
  //         28, -29, -18, 6, 24, -2, 6, -21, -27, -19, 4, -12, 28, -13, 15, 20, -7, 12, -9, -21,
  //         -9, 21, -5, -21, 8, 28, 21, 24, -5, 2, 13, -17, -32, -7, 21, 20, 15, -7, -25, 22,
  //         -22, -24, 11, 0, 14, 25, 21, -9, 4, 28, 4, -7, 27, 6, 31, 1, 19, 25, 11, 12,
  //         3, 28, 24, -28, 22, 29, -31, -7, 27, 29, 3, -11, -27, 29, 10, -23, -2, 13, -28, 21,
  //         -16, 12, 17, 20, 4, 2, 27, -9, 6, 15, -16, 27, -1, 25, 16, -28, -31, -1, -16, -8,
  //         7, 15, -2, 9, 26, 23, 27, -23, 9, -13, -5, 19, -2, -29, -29, 31, 6, -22, -23, -13,
  //         18, 15, -14, -19, -19, 23, -10, -9, 13, 23, 20, 26, 0, 4, -15, -10, -18, 2, 25, 5,
  //         -10, -9, -10, 30, 11, 9, 22, -3, -32, -27, 11, 8, -2, -22, 2, 25, 19, -13, 16, 30,
  //         -2, 3, 28, 31, -24, -12, 30, 28, 18, -15, 22, 12, 18, 31, -11, 25, -21, 20, -27, -32,
  //         23, -25, -27, 11, -20, -14, -19, 26, 31, -14, 26, 23, 25, 11, 0, 26, 21, 11, -12, -2,
  //         8, -27, -6, 28, -29, -19, 5, 22, -1, 4, -22, -27, -20, 5, -11, 29, -15, 15, 20, -9,
  //         13, -10, -22, -8, 22, -5, -19, 6, 27, 22, 24, -7, 2, 13, -17, -31, -7, 21, 20, 15,
  //         -8, -25, 21, -20, -25, 11, -1, 14, 25, 23, -9, 3, 28, 4, -8, 27, 6, 31, 2, 19,
  //         25, 11, 12, 2, 28, 26, -28, 21, 28, -32, -6, 28, 31, 3, -12, -28, 30, 9, -24, -2,
  //         14, -26, 21, -16, 13, 15, 22, 5, 2, 26, -9, 6, 14, -16, 27, -3, 24, 15, -28, -32,
  //         -3, -15, -9, 6, 13, -1, 9, 26, 23, 27, -24, 9, -13, -4, 19, -2, -27, -28, 30, 6,
  //         -24, -21, -12, 18, 16, -14, -19, -21, 23, -12, -8, 11, 23, 22, 24, 0, 2, -15, -9, -18,
  //         2, 26, 6, -11, -11, -11, 30, 12, 8, 24, -3, -32, -28, 9, 7, -1, -22, 0, 26, 20
  //     )

  //     val localTx1 = ArrayBuffer(
  //         -28, 22, 29, -31, -6, 27, 30, 3, -12, -28, 30, 9, -23, -1, 14, -27, 21, -15, 13, 16,
  //         21, 4, 1, 26, -10, 6, 14, -15, 26, -2, 24, 16, -27, -32, -2, -15, -8, 6, 14, -2,
  //         8, 25, 23, 28, -24, 9, -12, -4, 20, -2, -28, -28, 31, 6, -23, -22, -13, 17, 15, -13,
  //         -18, -20, 24, -11, -8, 12, 23, 21, 25, -1, 3, -14, -10, -17, 2, 26, 6, -11, -10, -10,
  //         29, 12, 9, 23, -4, -32, -27, 10, 8, -1, -22, 1, 25, 19, -12, 17, 31, -2, 2, 29,
  //         31, -24, -11, 30, 27, 19, -15, 21, 12, 17, 30, -11, 24, -20, 21, -28, -32, 24, -24, -26,
  //         12, -21, -13, -19, 26, 30, -15, 26, 23, 25, 11, 1, 26, 22, 10, -13, -2, 9, -28, -7,
  //         29, -29, -18, 5, 23, -1, 5, -21, -27, -19, 4, -12, 29, -14, 14, 19, -8, 12, -9, -22,
  //         -8, 22, -6, -20, 7, 27, 22, 25, -6, 2, 14, -18, -32, -6, 22, 20, 16, -7, -24, 22,
  //         -21, -25, 12, 0, 14, 26, 22, -8, 4, 27, 5, -8, 27, 5, 31, 1, 19, 24, 11, 12,
  //         3, 29, 25, -28, 22, 29, -31, -6, 27, 30, 3, -12, -28, 30, 9, -23, -1, 14, -27, 21,
  //         -15, 13, 16, 21, 4, 1, 26, -10, 6, 14, -15, 26, -2, 24, 16, -27, -32, -2, -15, -8,
  //         6, 14, -2, 8, 25, 23, 28, -24, 9, -12, -4, 20, -2, -28, -28, 31, 6, -23, -22, -13,
  //         17, 15, -13, -18, -20, 24, -11, -8, 12, 23, 21, 25, -1, 3, -14, -10, -17, 2, 26, 6,
  //         -11, -10, -10, 29, 12, 9, 23, -4, -32, -27, 10, 8, -1, -22, 1, 25, 19, -12, 17, 31,
  //         -2, 2, 29, 31, -24, -11, 30, 27, 19, -15, 21, 12, 17, 30, -11, 24, -20, 21, -28, -32,
  //         24, -24, -26, 12, -21, -13, -19, 26, 30, -15, 26, 23, 25, 11, 1, 26, 22, 10, -13, -2,
  //         9, -28, -7, 29, -29, -18, 5, 23, -1, 5, -21, -27, -19, 4, -12, 29, -14, 14, 19, -8,
  //         12, -9, -22, -8, 22, -6, -20, 7, 27, 22, 25, -6, 2, 14, -18, -32, -6, 22, 20, 16,
  //         -7, -24, 22, -21, -25, 12, 0, 14, 26, 22, -8, 4, 27, 5, -8, 27, 5, 31, 1, 19,
  //         24, 11, 12, 3, 29, 25, -28, 22, 29, -31, -6, 27, 30, 3, -12, -28, 30, 9, -23, -1,
  //         14, -27, 21, -15, 13, 16, 21, 4, 1, 26, -10, 6, 14, -15, 26, -2, 24, 16, -27, -32,
  //         -2, -15, -8, 6, 14, -2, 8, 25, 23, 28, -24, 9, -12, -4, 20, -2, -28, -28, 31, 6,
  //         -23, -22, -13, 17, 15, -13, -18, -20, 24, -11, -8, 12, 23, 21, 25, -1, 3, -14, -10, -17,
  //         2, 26, 6, -11, -10, -10, 29, 12, 9, 23, -4, -32, -27, 10, 8, -1, -22, 1, 25, 19
  //     )

  //     val localTx2 = ArrayBuffer(
  //         -29, 21, 28, -31, -7, 28, 31, 3, -12, -27, 29, 10, -24, 0, 15, -28, 21, -14, 13, 17,
  //         22, 4, 2, 26, -9, 6, 13, -16, 26, -2, 24, 16, -27, -31, -3, -14, -9, 5, 13, -3,
  //         7, 25, 22, 27, -23, 10, -12, -3, 21, -1, -29, -28, 31, 7, -22, -22, -12, 17, 15, -13,
  //         -19, -20, 25, -10, -8, 13, 24, 20, 25, -1, 3, -13, -9, -17, 3, 26, 6, -11, -9, -9,
  //         30, 13, 9, 23, -3, -32, -27, 11, 7, 0, -22, 1, 25, 19, -11, 18, 31, -1, 3, 30,
  //         31, -23, -11, 30, 28, 18, -15, 22, 12, 18, 31, -12, 24, -19, 20, -29, -31, 23, -25, -25,
  //         13, -22, -13, -18, 25, 31, -16, 27, 22, 25, 10, 0, 25, 22, 11, -14, -3, 9, -28, -8,
  //         28, -28, -19, 4, 22, -2, 4, -22, -26, -20, 4, -12, 28, -15, 15, 18, -7, 13, -10, -22,
  //         -7, 21, -7, -21, 6, 26, 22, 26, -5, 3, 15, -19, -32, -6, 22, 19, 16, -7, -23, 23,
  //         -20, -26, 12, 0, 15, 27, 21, -8, 3, 26, 6, -7, 27, 4, 30, 1, 18, 25, 12, 12,
  //         3, 30, 25, -29, 22, 29, -31, -5, 27, 31, 2, -11, -27, 29, 10, -24, -1, 13, -28, 20,
  //         -16, 12, 16, 22, 3, 2, 27, -11, 5, 13, -14, 26, -3, 24, 15, -26, -32, -1, -14, -7,
  //         6, 14, -1, 8, 25, 24, 27, -25, 8, -12, -4, 19, -3, -27, -27, 30, 5, -24, -23, -14,
  //         16, 14, -14, -19, -21, 25, -10, -8, 12, 23, 22, 26, -1, 2, -13, -11, -16, 2, 26, 7,
  //         -12, -9, -9, 28, 12, 10, 24, -5, -32, -26, 10, 8, -2, -21, 0, 26, 19, -12, 18, 31,
  //         -2, 1, 30, 31, -25, -12, 30, 26, 20, -16, 21, 11, 17, 31, -11, 24, -21, 22, -27, -32,
  //         23, -24, -27, 12, -21, -12, -20, 25, 30, -16, 25, 24, 25, 11, 2, 26, 21, 10, -13, -3,
  //         9, -29, -8, 28, -28, -18, 6, 23, -2, 6, -22, -28, -18, 5, -12, 28, -13, 15, 19, -8,
  //         12, -9, -23, -8, 22, -6, -19, 7, 28, 23, 25, -6, 3, 14, -18, -32, -7, 22, 21, 16,
  //         -6, -24, 23, -20, -24, 11, 1, 15, 27, 21, -9, 4, 26, 6, -9, 27, 4, 30, 2, 20,
  //         23, 10, 12, 3, 28, 26, -27, 23, 29, -31, -7, 28, 30, 2, -13, -27, 30, 8, -23, 0,
  //         13, -26, 22, -15, 12, 15, 21, 5, 0, 26, -10, 7, 15, -16, 26, -2, 25, 15, -26, -32,
  //         -1, -16, -8, 7, 13, -2, 8, 25, 23, 29, -23, 9, -11, -3, 20, -2, -27, -28, 31, 7,
  //         -22, -23, -12, 16, 16, -12, -19, -20, 25, -11, -7, 11, 23, 21, 26, 0, 3, -13, -11, -18,
  //         1, 26, 5, -10, -9, -9, 29, 11, 8, 23, -3, -32, -27, 11, 9, 0, -23, 1, 26, 18
  //     )

  //     val localTx3 = ArrayBuffer(
  //         -29, 23, 30, -32, -6, 28, 30, 4, -11, -29, 31, 8, -23, -1, 15, -28, 20, -14, 13, 17,
  //         22, 4, 1, 27, -11, 5, 15, -16, 27, -2, 25, 15, -26, -32, -3, -14, -9, 6, 13, -2,
  //         8, 26, 24, 28, -23, 9, -12, -3, 21, -2, -29, -28, 30, 5, -24, -22, -14, 17, 16, -13,
  //         -17, -20, 24, -10, -8, 13, 23, 22, 26, -1, 4, -15, -10, -16, 1, 26, 7, -10, -9, -11,
  //         30, 12, 10, 24, -3, -32, -26, 11, 7, 0, -21, 1, 25, 18, -13, 17, 31, -2, 1, 29,
  //         30, -24, -12, 29, 27, 19, -15, 20, 11, 18, 31, -12, 24, -19, 22, -28, -31, 24, -23, -27,
  //         11, -21, -14, -20, 25, 31, -14, 25, 22, 25, 11, 2, 26, 21, 11, -14, -2, 9, -28, -8,
  //         29, -29, -18, 6, 23, 0, 6, -20, -26, -20, 5, -11, 29, -14, 15, 20, -7, 11, -9, -22,
  //         -8, 23, -6, -19, 7, 26, 22, 26, -6, 1, 14, -17, -31, -5, 21, 19, 17, -6, -24, 22,
  //         -21, -24, 12, 1, 15, 26, 23, -8, 3, 28, 4, -7, 26, 5, 31, 2, 19, 24, 12, 13,
  //         3, 28, 25, -29, 22, 29, -30, -6, 27, 30, 2, -13, -27, 31, 10, -24, -1, 15, -28, 22,
  //         -15, 12, 16, 20, 5, 1, 27, -9, 5, 13, -14, 26, -2, 25, 15, -27, -32, -3, -15, -7,
  //         7, 13, -1, 8, 25, 22, 27, -25, 10, -13, -4, 21, -3, -28, -28, 31, 5, -22, -22, -12,
  //         16, 15, -13, -17, -20, 24, -11, -7, 12, 23, 21, 26, -1, 2, -14, -11, -18, 1, 26, 7,
  //         -10, -10, -9, 30, 11, 8, 24, -4, -31, -26, 11, 8, 0, -22, 0, 24, 20, -12, 16, 30,
  //         -3, 1, 30, 31, -25, -11, 31, 26, 18, -15, 22, 11, 16, 31, -11, 23, -19, 20, -27, -31,
  //         24, -25, -25, 11, -21, -14, -19, 27, 30, -16, 26, 22, 24, 10, 2, 25, 21, 9, -13, -3,
  //         8, -28, -6, 30, -29, -19, 5, 22, -1, 6, -20, -26, -19, 3, -12, 29, -15, 13, 18, -9,
  //         11, -10, -23, -8, 21, -5, -21, 8, 28, 22, 25, -7, 2, 13, -18, -32, -5, 22, 21, 16,
  //         -7, -24, 21, -21, -25, 11, -1, 15, 25, 23, -7, 4, 26, 6, -9, 26, 5, 31, 2, 20,
  //         25, 11, 13, 2, 30, 25, -29, 22, 30, -31, -5, 27, 30, 4, -12, -28, 29, 8, -24, 0,
  //         14, -26, 22, -16, 13, 15, 21, 3, 0, 26, -10, 7, 14, -16, 26, -1, 23, 17, -26, -31,
  //         -2, -14, -9, 7, 14, -1, 9, 24, 24, 29, -25, 9, -13, -5, 20, -3, -27, -29, 31, 5,
  //         -22, -22, -14, 16, 15, -14, -19, -21, 25, -11, -7, 12, 24, 21, 24, -2, 4, -13, -9, -16,
  //         1, 25, 7, -10, -11, -9, 28, 13, 9, 24, -5, -32, -28, 9, 9, -2, -22, 2, 26, 19
  //     )

  //     val remoteSignal = ArrayBuffer(
  //       29, 29, -22, 14, 19, -11, 21, -24, 7, -30, 9, 9, -23, 18, 15, 4, -9, -2, -11, 7,
  //       -20, -26, -4, -23, 21, -8, 24, -30, 18, -7, 11, -8, 9, -26, -26, -28, 14, 18, 9, -11,
  //       -12, -17, 12, -11, -29, 12, -21, 18, 24, 12, -25, -14, 24, 28, 25, 24, -10, 13, 22, 11,
  //       26, 17, -8, -20, 23, -13, -27, -9, -8, 22, 14, 16, -26, -26, -11, 7, -4, -26, -10, -20,
  //       14, -9, 6, -27, 18, 22, -23, 12, -8, 30, -12, -11, 19, 27, 10, -28, -10, -10, 21, -20,
  //       18, -20, 17, -17, -2, -19, 22, 7, 5, -20, 12, 28, -1, 5, -31, -12, 17, -3, -20, 28,
  //       -20, 19, -5, 4, 13, -24, -8, -29, -23, 19, -23, -30, 23, 11, -10, -26, 13, 9, 28, -11,
  //       -30, -6, -16, 5, -7, -1, -1, 17, 21, -29, -16, -32, -17, -29, -25, -10, -17, 17, -24, -31,
  //       23, -9, 21, -26, -11, -26, 16, 26, 26, 10, -9, -28, 19, 26, 30, 14, -23, -15, 1, 0,
  //       -10, -21, 26, -31, -13, 3, -22, 7, -14, -19, -17, -17, 4, 10, -32, 1, 22, -14, -25, -25,
  //       13, -24,
  //       // repeat from start for next 200
  //       29, 29, -22, 14, 19, -11, 21, -24, 7, -30, 9, 9, -23, 18, 15, 4, -9, -2, -11, 7,
  //       -20, -26, -4, -23, 21, -8, 24, -30, 18, -7, 11, -8, 9, -26, -26, -28, 14, 18, 9, -11,
  //       -12, -17, 12, -11, -29, 12, -21, 18, 24, 12, -25, -14, 24, 28, 25, 24, -10, 13, 22, 11,
  //       26, 17, -8, -20, 23, -13, -27, -9, -8, 22, 14, 16, -26, -26, -11, 7, -4, -26, -10, -20,
  //       14, -9, 6, -27, 18, 22, -23, 12, -8, 30, -12, -11, 19, 27, 10, -28, -10, -10, 21, -20,
  //       18, -20, 17, -17, -2, -19, 22, 7, 5, -20, 12, 28, -1, 5, -31, -12, 17, -3, -20, 28,
  //       -20, 19, -5, 4, 13, -24, -8, -29, -23, 19, -23, -30, 23, 11, -10, -26, 13, 9, 28, -11,
  //       -30, -6, -16, 5, -7, -1, -1, 17, 21, -29, -16, -32, -17, -29, -25, -10, -17, 17, -24, -31,
  //       23, -9, 21, -26, -11, -26, 16, 26, 26, 10, -9, -28, 19, 26, 30, 14, -23, -15, 1, 0,
  //       -10, -21, 26, -31, -13, 3, -22, 7, -14, -19, -17, -17, 4, 10, -32, 1, 22, -14, -25, -25,
  //       13, -24,
  //       // repeat again for next 100 (for 500 total)
  //       29, 29, -22, 14, 19, -11, 21, -24, 7, -30, 9, 9, -23, 18, 15, 4, -9, -2, -11, 7,
  //       -20, -26, -4, -23, 21, -8, 24, -30, 18, -7, 11, -8, 9, -26, -26, -28, 14, 18, 9, -11,
  //       -12, -17, 12, -11, -29, 12, -21, 18, 24, 12, -25, -14, 24, 28, 25, 24, -10, 13, 22, 11,
  //       26, 17, -8, -20, 23, -13, -27, -9, -8, 22, 14, 16, -26, -26, -11, 7, -4, -26, -10, -20,
  //       14, -9, 6, -27, 18, 22, -23, 12, -8, 30, -12, -11, 19, 27, 10, -28, -10, -10, 21, -20,
  //       18, -20, 17, -17, -2, -19, 22, 7, 5, -20, 12, 28, -1, 5, -31, -12, 17, -3, -20, 28,
  //       -20, 19, -5, 4, 13, -24, -8, -29, -23, 19, -23, -30, 23, 11, -10, -26, 13, 9, 28, -11,
  //       -30, -6, -16, 5, -7, -1, -1, 17, 21, -29, -16, -32, -17, -29, -25, -10, -17, 17, -24, -31,
  //       23, -9, 21, -26, -11, -26, 16, 26, 26, 10, -9, -28, 19, 26, 30, 14, -23, -15, 1, 0,
  //       -10, -21, 26, -31, -13, 3, -22, 7, -14, -19, -17, -17, 4, 10, -32, 1, 22, -14, -25, -25,
  //       13, -24
  //     )

  //     dut.io.txValid.poke(false.B)
  //     dut.clock.step()


  //     for (i <- 0 until steps) {

  //       // // Random walks for signal changes
  //       // remoteSignal = (Random.between(-32, 31)).max(-32).min(31)
  //       // localTx1 = (Random.between(-32, 31)).max(-32).min(31)

  //       // // NEXT contribution from localTx1. Based on Python model to just check for accuracy.
  //       // val next1 = ((localTx1 * 3) >> 1).round.toInt

  //       // // Received signal = Remote + NEXT1 only. Probably won't hit these limits
  //       // val received = (remoteSignal + next1).max(-64).min(63)

  //       // Feed into DUT
  //       val receivedSignal = remoteSignal(i) + (localTx0(i) >> 3) + (localTx1(i) >> 4) + (localTx2(i) >> 4) + (localTx3(i) >> 4)
  //       // dut.io.din.poke(localTx1(i).S(7.W))          // TX1 data
  //       // dut.io.desired.poke(receivedSignal.S(8.W))       // RX signal (remote + NEXT1)
  //       // dut.io.dinValid.poke(true.B)
  //       // dut.clock.step()

  //       dut.io.tx0.poke(localTx0(i).S(6.W))
  //       dut.io.tx1.poke(localTx1(i).S(6.W))
  //       dut.io.tx2.poke(localTx2(i).S(6.W))
  //       dut.io.tx3.poke(localTx3(i).S(6.W))
  //       dut.io.txValid.poke(true.B)
  //       dut.io.desired.poke(receivedSignal.S(8.W))
  //       dut.clock.step()


  //       // Record
  //       // remoteSignalHistory += remoteSignal
  //       // localTx1History += localTx1
  //       // receivedHistory += received
  //       outputHistory += (dut.io.desiredCancelled.peek().litValue.toInt)

  //       // println(s"$i, $remoteSignal, $receivedSignal, ${receivedSignal - cleanedOutputs.last}, ${noise}")
  //       // println(s"$i, $remoteSignal, $received, ${received - outputHistory.last}, ${localTx1}, ${next1}, ${dut.io.weightPeek.peek()}")
  //       // println(s"$i, Input: ${localTx1(i)}, Received: ${received(i)}, DOut: ${dut.io.dout.peek()}, Error: ${received(i) - outputHistory.last}, Weights: ${dut.io.weightPeek.peek()}")
  //       println(s"$i, ${remoteSignal(i)}, ${receivedSignal}, ${outputHistory.last}") // ", Weights: ${dut.io.weightPeek.peek()}")
  //       // println(s"$i, Input: $localTx1, Received: $received, DOut: ${dut.io.dout.peek()}, Error: ${received - outputHistory.last}, Weights: ${dut.io.weightPeek.peek()}, Errors: ${dut.io.errors.peek()}, Delayed Inputs: ${dut.io.inputWeightShifters.peek()}")
  //     }
  //   }
  // }

  "Simulated Incoming Rx Data" in {
    test(
      new TopModuleBlock(80, 60, 4)
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
        remoteSignal = (remoteSignal + Random.between(-3, 4)).max(-100).min(100)
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

}
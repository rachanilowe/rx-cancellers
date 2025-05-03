// package cancellers

// import chisel3._
// import chiseltest._
// import org.scalatest.freespec.AnyFreeSpec
// import scala.collection.mutable.ArrayBuffer
// import scala.util.Random
// import scala.math._

// import cancellers.CancellersTopModule
// // import cancellers.{LMSFIRFilter, LMSFIRFilter_Transpose}

// class HybridFir(tapCount: Int, segmentCount: Int) extends Module {
//     val io = IO(new Bundle {
//         val din          = Input(SInt(7.W))
//         val dinValid     = Input(Bool())
//         val dout         = Output(SInt(20.W))
//         val desired      = Input(SInt(8.W))

//         // For debugging
//         val weightPeek   = Output(Vec(segmentCount, SInt(10.W)))
//         val errors       = Output(Vec(tapCount/segmentCount - 1, SInt(20.W)))
//         val inputWeightShifters = Output(Vec(((tapCount/segmentCount * (segmentCount - 1)) + tapCount/segmentCount), SInt(10.W)))
//     })
//     val dut = Module(new HybridAdaptiveFIRFilter(tapCount, segmentCount))
//     dut.io.din := io.din
//     dut.io.dinValid := io.dinValid
//     dut.io.desired := io.desired
//     io.weightPeek := dut.io.weightPeek
//     io.dout := dut.io.dout
//     // io.input0 := dut.io.input0

//     io.dout := dut.io.dout
//     io.errors := dut.io.errors
//     io.inputWeightShifters := dut.io.inputWeightShifters
// }            

// class HybridFirFilterTest extends AnyFreeSpec with ChiselScalatestTester {

//     // TODO: use actual data
//   "Basic echo functionality test" in {
//     test(
//       new HybridFir(
//         6, 3
//       )
//     ).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
//       dut.io.din.poke(2.S(3.W))
//       dut.io.dinValid.poke(true.B)
//       dut.io.desired.poke(64.S(18.W))

//       // CYCLE 0 -> 1: InputWeightShifters(0) = 2, inputShifters(0) = 2, errorShifter(0) = 0, dout = 0
//       dut.clock.step()
//       dut.io.din.poke(2.S(3.W))
//       dut.io.desired.poke(64.S(18.W))
//       dut.io.dout.expect(0.S)
//       // CYCLE 1 -> 2: InputWeightShifters(1) = 2, inputShifters(1) = 2, errorShifter(0) = (64 - 0) >> 5, dout = 0
//       dut.clock.step()
//       dut.io.din.poke(2.S(3.W))
//       dut.io.desired.poke(64.S(18.W))
//       dut.io.dout.expect(0.S)
//       // CYCLE 2 -> 3: InputWeightShifters(2) = 2, inputShifters(2) = 2, errorShifter(0) = (64 - 0) >> 5, dout = 0
//       dut.clock.step()
//       dut.io.din.poke(2.S(3.W))
//       dut.io.desired.poke(64.S(18.W))
//       dut.io.dout.expect(8.S)
//       // CYCLE 3 -> 4: InputWeightShifters(3) = 2, inputShifters(3) = 2, errorShifter(0) = (64 - 0) >> 5
//       dut.clock.step()
//       dut.io.din.poke(2.S(3.W))
//       dut.io.desired.poke(64.S(18.W))
//       dut.io.dout.expect(24.S)
//       // CYCLE 4 -> 5: InputWeightShifters(4) = 2, inputShifters(4) = 2, errorShifter(0) = (64 - 0) >> 5
//       dut.clock.step()
//       dut.io.din.poke(2.S(3.W))
//       dut.io.desired.poke(64.S(18.W))
//       dut.io.dout.expect(48.S)
//       // CYCLE 5 -> 6: InputWeightShifters(5) = 2, inputShifters(4) = 2, errorShifter(0) = (64 - 0) >> 5
//       dut.clock.step()
//       dut.io.din.poke(2.S(3.W))
//       dut.io.desired.poke(64.S(18.W))
//       dut.io.dout.expect(80.S)
//       // CYCLE 6 -> 7: InputWeightShifters(5) = 2, inputShifters(4) = 2, errorShifter(0) = (64 - 0) >> 5, dout = 4
//       dut.clock.step()
//       dut.io.din.poke(2.S(3.W))
//       dut.io.desired.poke(64.S(18.W))
//       dut.io.dout.expect(100.S)

//       dut.clock.step() // din gets added to first reg in input shifters
//       // first check the output (0 * 2)
//       // poke new in value and io.desired
//       // clock step
//       // new weight should be calculated 
//       // check first inputWeightShifter, should be 2

//     }
//   }

//   // TODO: would be nice to work with realistic data
//   // This is closer to what we'd be running with the correct FIRSegment size of 4 taps.
//   "Testing a twelve-tap FIR with four-tap FIRSegments" in {
//     test(
//       new HybridFir(
//         12, 4
//       )
//     ).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
//       dut.io.din.poke(2.S(3.W))
//       dut.io.dinValid.poke(true.B)
//       dut.io.desired.poke(64.S(18.W))
//       // println("Weight Peek: " + dut.io.weightPeek.peek())

//       // CYCLE 0 -> 1: 
//       // InputWeightShifters(0) = 2
//       // inputShifters(0) = 2
//       // errorShifter(0) = 0, dout = 0
//       dut.clock.step()
//       dut.io.din.poke(-2.S(3.W))
//       dut.io.desired.poke(128.S(18.W))
//       // println("Weight Peek: " + dut.io.weightPeek.peek())
//       dut.io.dout.expect(0.S)

//       // CYCLE 1 -> 2: 
//       // InputWeightShifters(0) = -2, InputWeightShifters(1) = 2
//       // inputShifters(0) = -2, inputShifters(1) = 2
//       // errorShifter(0) = -4, errorShifter(1) = 0
//       dut.clock.step()
//       dut.io.din.poke(1.S(3.W))
//       dut.io.desired.poke(84.S(18.W))
//       // println("Weight Peek: " + dut.io.weightPeek.peek())
//       dut.io.dout.expect(0.S)

//       // CYCLE 2 -> 3:
//       // InputWeightShifters(0) = 1, InputWeightShifters(1) = -2, InputWeightShifters(2) = 2
//       // inputShifters(0) = 1, inputShifters(1) = -2, inputShifters(2) = 2
//       // errorShifter(0) = -3, errorShifter(1) = -4
//       // Note that -84 >> 5 == 3
//       // weight(0) = 8
//       dut.clock.step()
//       dut.io.din.poke(-1.S(3.W))
//       dut.io.desired.poke(33.S(18.W))
//       // println("Weight Peek: " + dut.io.weightPeek.peek())
//       dut.io.dout.expect(0.S)

//       // CYCLE 3 -> 4:
//       // InputWeightShifters(0) = -1, InputWeightShifters(1) = 1, InputWeightShifters(2) = -2, InputWeightShifters(3) = 2
//       // inputShifters(0) = -1, inputShifters(1) = 1, inputShifters(2) = -2, inputShifters(3) = 2
//       // errorShifter(0) = -2, errorShifter(1) = -3
//       // weight(0) = 2, weight(1) = 6 
//       dut.clock.step()
//       dut.io.din.poke(0.S(3.W))
//       dut.io.desired.poke(47.S(18.W))
//       // println("Weight Peek: " + dut.io.weightPeek.peek())
//       dut.io.dout.expect(-8.S)

//       // CYCLE 4 -> 5:
//       // InputWeightShifters(0) = 0, InputWeightShifters(1) = -1, InputWeightShifters(2) = 1, InputWeightShifters(3) = -2, InputWeightShifters(4) = 2
//       // inputShifters(0) = 0, inputShifters(1) = -1, inputShifters(2) = 1, inputShifters(3) = -2, inputShifters(4) = 2
//       // errorShifter(0) = -2, errorShifter(1) = -2
//       // weight(0) = 4, weight(1) = 2, weight(2) = 4
//       dut.clock.step()
//       dut.io.din.poke(2.S(3.W))
//       dut.io.desired.poke(22.S(18.W))
//       // println("Weight Peek: " + dut.io.weightPeek.peek())
//       dut.io.dout.expect(-6.S) // weight(0) * InputWeightShifters(0) + weight(1) * InputWeightShifters(1)

//       // CYCLE 5 -> 6:
//       // InputWeightShifters(0) = 2, InputWeightShifters(1) = 0, InputWeightShifters(2) = -1, InputWeightShifters(3) = 1, InputWeightShifters(4) = -2, InputWeightShifters(5) = 2
//       // inputShifters(0) = 2, inputShifters(1) = 0, inputShifters(2) = -1, inputShifters(3) = 1, inputShifters(4) = -2, inputShifters(5) = 2
//       // errorShifter(0) = -1, errorShifter(1) = -2
//       // weight(0) = 2, weight(1) = 4, weight(2) = 0, weight(3) = 4
//       dut.clock.step()
//       dut.io.din.poke(1.S(3.W))
//       dut.io.desired.poke(-11.S(18.W))
//       // println("Weight Peek: " + dut.io.weightPeek.peek())
//       dut.io.dout.expect(4.S) // weight(0) * InputWeightShifters(0) + weight(1) * InputWeightShifters(1) + weight(2) * InputWeightShifters(2)

//       // CYCLE 6 -> 7:
//       // InputWeightShifters(0) = 1, InputWeightShifters(1) = 2, InputWeightShifters(2) = 0, InputWeightShifters(3) = -1, InputWeightShifters(4) = 1, InputWeightShifters(5) = -2, InputWeightShifters(6) = 2
//       // inputShifters(0) = 1, inputShifters(1) = 2, inputShifters(2) = 0, inputShifters(3) = -1, inputShifters(4) = 1, inputShifters(5) = -2, inputShifters(6) = 2
//       // errorShifter(0) = 0, errorShifter(1) = -1
//       dut.clock.step()
//       dut.io.din.poke(-1.S(3.W))
//       dut.io.desired.poke(33.S(18.W))
//       // println("Weight Peek: " + dut.io.weightPeek.peek())
//       dut.io.dout.expect(6.S)
//     }
//   }

//   "Simulated Incoming Rx Data with One 'NEXT' Source" in {
//     test(
//       new HybridFir(12, 2)
//     ) // 20-bit coefficients, 4 taps
//     .withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

//       // Set a seed 
//       val rand = new Random(1)
//       val steps = 200

//       // // Buffers for debugging/plotting
//       // val remoteSignalHistory = scala.collection.mutable.ArrayBuffer[Int]()
//       // val localTx1History = scala.collection.mutable.ArrayBuffer[Int]()
//       // val receivedHistory = scala.collection.mutable.ArrayBuffer[Int]()
//       val outputHistory = scala.collection.mutable.ArrayBuffer[Int]()

//       // // NEXT parameters
//       // val nextLossDb = 36 // Typical 1000Base-T NEXT
//       // val nextCoupling = math.pow(10, -nextLossDb / 20.0)

//       // // Initial signals
//       // var remoteSignal = Random.between(-4, 4)
//       // var localTx1 = Random.between(-4, 4)

//       // val localTx1 = ArrayBuffer(
//       //   -28, 22, 29, -31, -6, 27, 30, 3, -12, -28,
//       //   30, 9, -23, -1, 14, -27, 21, -15, 13, 16,
//       //   21, 4, 1, 26, -10, 6, 14, -15, 26, -2,
//       //   24, 16, -27, -32, -2, -15, -8, 6, 14, -2,
//       //   8, 25, 23, 28, -24, 9, -12, -4, 20, -2
//       // )

//       // val received = ArrayBuffer(
//       //   -56, 29, 46, -51, -27, 15, 20, 25, 1, -70,
//       //   25, -3, -13, 5, -5, -45, 53, -15, 23, 48,
//       //   54, -24, -4, 27, -21, -14, -7, -37, 41, 6,
//       //   36, 36, -67, -63, -9, -5, -12, 39, 43, 15,
//       //   36, 50, 55, 51, -39, 21, 0, -33, 0, 27
//       // )

//       // val remoteSignal = ArrayBuffer(
//       //   -14, -4, 3, -4, -18, -25, -25, 21, 19, -28,
//       //   -20, -16, 22, 7, -26, -4, 22, 8, 4, 24,
//       //   23, -30, -5, -12, -6, -23, -28, -14, 2, 9,
//       //   0, 12, -26, -15, -6, 18, 0, 30, 22, 18,
//       //   24, 13, 21, 9, -3, 8, 18, -27, -30, 30
//       // )


//       val localTx1 = ArrayBuffer(
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
//         3, 29, 25
//       )

//       val received = ArrayBuffer(
//         -13, 62, 21, -33, 10, 29, 66, -20, -11, -72, 54, 22, -58, 16, 36, -37, 22, -25, 8, 31,
//         11, -20, -3, 16, 6, 1, 45, -53, 57, -10, 47, 16, -32, -74, -29, -51, 2, 27, 30, -14,
//         0, 20, 46, 31, -65, 25, -39, 12, 54, 9, -67, -56, 70, 37, -10, -9, -30, 38, 44, -9,
//         -1, -13, 28, -37, 11, 5, 7, 22, 29, 20, 18, -5, -41, -52, -8, 46, 5, -43, -25, -35,
//         57, 9, 19, 7, 12, -26, -64, 27, 4, 28, -45, -10, 56, 55, -8, -3, 36, -13, 24, 30,
//         64, -56, 1, 28, 38, 9, -1, 38, 23, 5, 57, 11, 35, -25, 0, -54, -31, 33, -56, -11,
//         -2, -13, -25, -25, 52, 21, -31, 10, 11, 56, -12, -29, 62, 44, 5, -46, 10, 22, -14, -22,
//         13, -50, -43, 12, 27, -3, 6, -15, -20, -58, -10, -50, 26, -50, -4, 18, -29, 35, -38, -64,
//         11, 24, 12, -56, -1, 14, 49, 63, 17, 13, 12, -55, -29, 17, 63, 44, 1, -26, -35, 33,
//         -42, -59, 44, -31, 8, 42, 11, -5, -8, 21, -10, -5, 50, 11, -4, 37, 38, 4, -21, 18,
//         12, 56
//       )

//       val remoteSignal = ArrayBuffer(
//         29, 29, -22, 14, 19, -11, 21, -24, 7, -30, 9, 9, -23, 18, 15, 4, -9, -2, -11, 7,
//         -20, -26, -4, -23, 21, -8, 24, -30, 18, -7, 11, -8, 9, -26, -26, -28, 14, 18, 9, -11,
//         -12, -17, 12, -11, -29, 12, -21, 18, 24, 12, -25, -14, 24, 28, 25, 24, -10, 13, 22, 11,
//         26, 17, -8, -20, 23, -13, -27, -9, -8, 22, 14, 16, -26, -26, -11, 7, -4, -26, -10, -20,
//         14, -9, 6, -27, 18, 22, -23, 12, -8, 30, -12, -11, 19, 27, 10, -28, -10, -10, 21, -20,
//         18, -20, 17, -17, -2, -19, 22, 7, 5, -20, 12, 28, -1, 5, -31, -12, 17, -3, -20, 28,
//         -20, 19, -5, 4, 13, -24, -8, -29, -23, 19, -23, -30, 23, 11, -10, -26, 13, 9, 28, -11,
//         -30, -6, -16, 5, -7, -1, -1, 17, 21, -29, -16, -32, -17, -29, -25, -10, -17, 17, -24, -31,
//         23, -9, 21, -26, -11, -26, 16, 26, 26, 10, -9, -28, 19, 26, 30, 14, -23, -15, 1, 0,
//         -10, -21, 26, -31, -13, 3, -22, 7, -14, -19, -17, -17, 4, 10, -32, 1, 22, -14, -25, -25,
//         13, -24
//       )

//       dut.io.dinValid.poke(false.B)
//       dut.clock.step()


//       for (i <- 0 until steps) {

//         // // Random walks for signal changes
//         // remoteSignal = (Random.between(-32, 31)).max(-32).min(31)
//         // localTx1 = (Random.between(-32, 31)).max(-32).min(31)

//         // // NEXT contribution from localTx1. Based on Python model to just check for accuracy.
//         // val next1 = ((localTx1 * 3) >> 1).round.toInt

//         // // Received signal = Remote + NEXT1 only. Probably won't hit these limits
//         // val received = (remoteSignal + next1).max(-64).min(63)

//         // Feed into DUT
//         dut.io.din.poke(localTx1(i).S(7.W))          // TX1 data
//         dut.io.desired.poke(received(i).S(8.W))       // RX signal (remote + NEXT1)
//         dut.io.dinValid.poke(true.B)
//         dut.clock.step()

//         // Record
//         // remoteSignalHistory += remoteSignal
//         // localTx1History += localTx1
//         // receivedHistory += received
//         outputHistory += (dut.io.dout.peek().litValue.toInt)

//         // println(s"$i, $remoteSignal, $receivedSignal, ${receivedSignal - cleanedOutputs.last}, ${noise}")
//         // println(s"$i, $remoteSignal, $received, ${received - outputHistory.last}, ${localTx1}, ${next1}, ${dut.io.weightPeek.peek()}")
//         // println(s"$i, Input: ${localTx1(i)}, Received: ${received(i)}, DOut: ${dut.io.dout.peek()}, Error: ${received(i) - outputHistory.last}, Weights: ${dut.io.weightPeek.peek()}")
//         println(s"$i, ${localTx1(i)}, ${received(i)}, ${received(i) - outputHistory.last}, Weights: ${dut.io.weightPeek.peek()}")
//         // println(s"$i, Input: $localTx1, Received: $received, DOut: ${dut.io.dout.peek()}, Error: ${received - outputHistory.last}, Weights: ${dut.io.weightPeek.peek()}, Errors: ${dut.io.errors.peek()}, Delayed Inputs: ${dut.io.inputWeightShifters.peek()}")
//       }
//     }
//   }
// }
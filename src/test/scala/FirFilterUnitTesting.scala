// package cancellers

// import chisel3._
// import chiseltest._
// import org.scalatest.freespec.AnyFreeSpec

// import cancellers.CancellersTopModule

// class Wrapper(echoTapCount: Int, nextTapCount: Int) extends Module {
//     val io = IO(new Bundle {
//         val tx0 = Input(SInt(3.W))
//         val tx1 = Input(SInt(3.W))
//         val tx2 = Input(SInt(3.W))
//         val tx3 = Input(SInt(3.W))
//         val txValid = Input(Bool())
//         val desired = Input(SInt(18.W))
//         val doutValid = Output(Bool())
//         val desiredCancelled = Output(SInt(18.W))
//     })
//     val dut = Module(new CancellersTopModule(echoTapCount, nextTapCount))
//     dut.io.tx0 := io.tx0
//     dut.io.tx1 := io.tx1
//     dut.io.tx2 := io.tx2
//     dut.io.tx3 := io.tx3
//     dut.io.txValid := io.txValid
//     dut.io.desired := io.desired

//     io.doutValid := dut.io.doutValid
//     io.desiredCancelled := dut.io.desiredCancelled
// }            

// class FirFilterTest extends AnyFreeSpec with ChiselScalatestTester {

//     // TODO: use actual data
//   "Basic echo functionality test" in {
//     test(
//       new Wrapper(
//         8, 8
//       )
//     ).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
//       dut.io.tx0.poke(2.S(3.W))
//       dut.io.tx1.poke(0.S(3.W))
//       dut.io.tx2.poke(0.S(3.W))
//       dut.io.tx3.poke(0.S(3.W))
//       dut.io.txValid.poke(true.B)
//       dut.io.desired.poke(-3.S(18.W))

//       while (!dut.io.done.peek().litToBoolean) {
//         dut.clock.step()
//       }

//       dut.io.fail.expect(false.B)
//     }
//   }
// }
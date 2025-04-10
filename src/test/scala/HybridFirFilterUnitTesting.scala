package cancellers

import chisel3._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec

import cancellers.CancellersTopModule

class HybridFir(tapCount: Int, segmentCount: Int) extends Module {
    val io = IO(new Bundle {
        val din          = Input(SInt(3.W))
        val dinValid     = Input(Bool())
        val dout         = Output(SInt(18.W))
        val desired      = Input(SInt(18.W))
    })
    val dut = Module(new HybridAdaptiveFIRFilter(tapCount, segmentCount))
    dut.io.din := io.din
    dut.io.dinValid := io.dinValid
    dut.io.desired := io.desired

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
      println(dut.io.dout.peek())
      dut.io.din.poke(2.S(3.W))
      dut.io.desired.poke(64.S(18.W))
      // CYCLE 1 -> 2: InputWeightShifters(1) = 2, inputShifters(1) = 2, errorShifter(0) = (64 - 0) >> 5, dout = 0
      dut.clock.step()
      println(dut.io.dout.peek())
      dut.io.din.poke(2.S(3.W))
      dut.io.desired.poke(64.S(18.W))
      // CYCLE 2 -> 3: InputWeightShifters(2) = 2, inputShifters(2) = 2, errorShifter(0) = (64 - 0) >> 5, dout = 0
      dut.clock.step()
      println(dut.io.dout.peek())
      dut.io.din.poke(2.S(3.W))
      dut.io.desired.poke(64.S(18.W))
      // CYCLE 3 -> 4: InputWeightShifters(3) = 2, inputShifters(3) = 2, errorShifter(0) = (64 - 0) >> 5
      dut.clock.step()
      println(dut.io.dout.peek())
      dut.io.din.poke(2.S(3.W))
      dut.io.desired.poke(64.S(18.W))
      // CYCLE 4 -> 5: InputWeightShifters(4) = 2, inputShifters(4) = 2, errorShifter(0) = (64 - 0) >> 5
      dut.clock.step()
      println(dut.io.dout.peek())
      dut.io.din.poke(2.S(3.W))
      dut.io.desired.poke(64.S(18.W))
      // CYCLE 5 -> 6: InputWeightShifters(5) = 2, inputShifters(4) = 2, errorShifter(0) = (64 - 0) >> 5
      dut.clock.step()
      println(dut.io.dout.peek())
      dut.io.din.poke(2.S(3.W))
      dut.io.desired.poke(64.S(18.W))
      // CYCLE 6 -> 7: InputWeightShifters(5) = 2, inputShifters(4) = 2, errorShifter(0) = (64 - 0) >> 5, dout = 4
      dut.clock.step()
      println(dut.io.dout.peek())
      dut.io.din.poke(2.S(3.W))
      dut.io.desired.poke(64.S(18.W))

      dut.clock.step() // din gets added to first reg in input shifters
      // first check the output (0 * 2)
      // poke new in value and io.desired
      // clock step
      // new weight should be calculated 
      // check first inputWeightShifter, should be 2

    }
  }
}
import chisel3._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec

import cancellers.RxCancellerTopIO

class Wrapper(tx0: SInt, tx1: SInt, tx2: SInt, tx3: SInt, txValid: Bool, desired: SInt) extends Module {
    val dut = Module(new RxCancellerTopIO)
    dut.io.tx0 := tx0
    dut.io.tx1 := tx1
    dut.io.tx2 := tx2
    dut.io.tx3 := tx3
    dut.io.txValid := txValid
    dut.io.desired := desired
}            

class FirFilterUnitTesting extends AnyFreeSpec with ChiselScalatestTester {

    "Test that Data Arrives at Right Time" in {
        test(
            new Wrapper()
        ) { dut =>

        }
    }

}
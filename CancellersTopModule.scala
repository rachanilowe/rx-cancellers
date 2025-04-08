package srambist

import chisel3._
import chisel3.util._

class CancellersTopModule(val tapCount: Int) extends Module {
  val io = IO(new Bundle {
    // different Tx signals coming from different twisted pairs
    val tx0 = Input(SInt(3.W)) // echo
    val tx1 = Input(SInt(3.W)) // next 1
    val tx2 = Input(SInt(3.W)) // next 2
    val tx3 = Input(SInt(3.W)) // next3
    // don't know yet how TX digial will be outputing
    val txValid = Input(Bool()) // per Richard only need tx valid

    val doutValid = Output(Bool()) 
    val desired   = Input(SInt(18.W)) // RX signal
    val desiredCancelled = Output(SInt(18.W)) // Cancelled RX signal
  }) 

    // Instantiate three NEXT cancellers and one echo canceller
    val echoCaneller = Module(new AdaptiveFIRFilter(80, log2Ceil(80)))
    val nextCanceller1 = Module(new AdaptiveFIRFilter(60, log2Ceil(60)))
    val nextCanceller2 = Module(new AdaptiveFIRFilter(60, log2Ceil(60)))
    val nextCanceller3 = Module(new AdaptiveFIRFilter(60, log2Ceil(60)))
    
    echoCanceller.io.din := tx0
    nextCanceller1.io.din := tx1
    nextCanceller2.io.din := tx2
    nextCanceller3.io.din := tx3

    echoCanceller.io.desired := io.desired
    nextCanceller1.io.desired := io.desired
    nextCanceller2.io.desired := io.desired
    nextCanceller3.io.desired := io.desired

    echoCanceller.io.dinValid :=  Mux(io.txValid, true.B, false.B)
    nextCanceller1.io.dinValid := Mux(io.txValid, true.B, false.B)
    nextCanceller2.io.dinValid := Mux(io.txValid, true.B, false.B)
    nextCanceller3.io.dinValid := Mux(io.txValid, true.B, false.B)

    // Might also need to check if the desired signal is valid?
    val validOutput = echoCanceller.io.doutValid & nextCanceller1.io.doutValid & nextCanceller2.io.doutValid & nextCanceller3.io.doutValid
    io.validOutput := validOutput
    // Filtered data
    // if tx is not valid input then we are not cancelling anything
    io.desiredCancelled = Mux(validOutput, io.desired - (echoCanceller.io.dout + nextCanceller1.io.dout + nextCanceller2.io.dout + nextCanceller3.io.dout), io.desired)
}
    
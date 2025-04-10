package cancellers

import chisel3._
import chisel3.util._
// import org.chipsalliance.cde.config.{Parameters, Field, Config}
// import freechips.rocketchip.diplomacy._
// import freechips.rocketchip.regmapper._
// import freechips.rocketchip.subsystem._
// import freechips.rocketchip.tilelink._

class RxCancellerTopIO() extends Bundle {
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
}

class CancellersTopModule(val tapCount: Int) extends Module {
  val io = IO(new RxCancellerTopIO) 

    // Instantiate three NEXT cancellers and one echo canceller
    val echoCanceller = Module(new HybridAdaptiveFIRFilter(80, log2Ceil(80)))
    val nextCanceller1 = Module(new HybridAdaptiveFIRFilter(60, log2Ceil(60)))
    val nextCanceller2 = Module(new HybridAdaptiveFIRFilter(60, log2Ceil(60)))
    val nextCanceller3 = Module(new HybridAdaptiveFIRFilter(60, log2Ceil(60)))
    
    echoCanceller.io.din := io.tx0
    nextCanceller1.io.din := io.tx1
    nextCanceller2.io.din := io.tx2
    nextCanceller3.io.din := io.tx3

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
    io.doutValid := validOutput
    // Filtered data
    // if tx is not valid input then we are not cancelling anything
    io.desiredCancelled := Mux(validOutput, io.desired - (echoCanceller.io.dout + nextCanceller1.io.dout + nextCanceller2.io.dout + nextCanceller3.io.dout), io.desired)
}

// class RxCancellersTL(params: RxCancellersParams, beatBytes: Int)(implicit p: Parameters)
//   extends TLRegisterRouter(
//     "cancellers", Seq("eecs251b,cancellers"),
//     beatBytes = beatBytes)(
//       new TLRegBundle(params, _) with RxCancellerTopIO)(
//       new TLRegModule(params, _, _) with CancellersTopModule)

// case class RxCancellersParams(
// )

// case object RxCancellersKey extends Field[Option[RxCancellersParams]](None)

// trait CanHavePeripheryRxCancellers { this: BaseSubsystem =>
//   private val portName = "cancellers"

//   // private val sbus = locateTLBusWrapper(SBUS)
//   private val pbus = locateTLBusWrapper(PBUS)

//   val rxcancellers = p(RxCancellersKey) match {
//     case Some(params) => {
//       val rxcanceller = LazyModule(new RxCancellersTL(params, pbus.beatBytes)(p))
//       pbus.coupleTo(portName) { rxcanceller.node := TLFragmenter(pbus.beatBytes, pbus.blockBytes) := _ }
//       Some(rxcanceller)
//     }
//     case None => None
//   }
// }

// trait CanHavePeripheryRxCancellersImp extends LazyModuleImp {
//   val outer: CanHavePeripheryRxCancellers
// }

// class WithRxCancellers(params: RxCancellersParams) extends Config((site, here, up) => {
//   case RxCancellersKey => Some(params)
// })
    
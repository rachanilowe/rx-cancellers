package cancellers

import chisel3._
import chisel3.util._
import org.chipsalliance.cde.config.{Parameters, Field, Config}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.regmapper._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.tilelink._

trait RxCancellerTopIO extends Bundle {
    // // different Tx signals coming from different twisted pairs
    // val tx0 = Input(SInt(3.W)) // echo
    // val tx1 = Input(SInt(3.W)) // next 1
    // val tx2 = Input(SInt(3.W)) // next 2
    // val tx3 = Input(SInt(3.W)) // next3
    // // don't know yet how TX digial will be outputing
    // val txValid = Input(Bool()) // per Richard only need tx valid

    // // val doutValid = Output(Bool()) 
    // val desired   = Input(SInt(18.W)) // RX signal
    // val desiredCancelled = Output(SInt(18.W)) // Cancelled RX signal
}

class CancellersTopModule() extends Module {
    val io = IO(new Bundle {
      val tx0 = Input(SInt(5.W)) // echo
      val tx1 = Input(SInt(5.W)) // next 1
      val tx2 = Input(SInt(5.W)) // next 2
      val tx3 = Input(SInt(5.W)) // next3
      // don't know yet how TX digial will be outputing
      val txValid = Input(Bool()) // per Richard only need tx valid

      // val doutValid = Output(Bool()) 
      val desired   = Input(SInt(6.W)) // RX signal
      val desiredCancelled = Output(SInt(6.W)) // Cancelled RX signal
    })

    // Instantiate three NEXT cancellers and one echo canceller
    val echoCanceller = Module(new HybridAdaptiveFIRFilter(80, 4))
    val nextCanceller1 = Module(new HybridAdaptiveFIRFilter(60, 4))
    val nextCanceller2 = Module(new HybridAdaptiveFIRFilter(60, 4))
    val nextCanceller3 = Module(new HybridAdaptiveFIRFilter(60, 4))
    
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
    // val validOutput = echoCanceller.io.doutValid & nextCanceller1.io.doutValid & nextCanceller2.io.doutValid & nextCanceller3.io.doutValid
    // io.doutValid := validOutput
    // Filtered data
    // if tx is not valid input then we are not cancelling anything
    io.desiredCancelled := io.desired - (echoCanceller.io.dout + nextCanceller1.io.dout + nextCanceller2.io.dout + nextCanceller3.io.dout)
}

trait CancellersTop extends HasRegMap {
    val io: RxCancellerTopIO
    val cancellers = Module(new CancellersTopModule())

    // Define a helper for read/write RegFields for SInt
    def RegFieldSInt(width: Int, reg: SInt): RegField = {
      RegField(width,
        RegReadFn(reg.asUInt),  // Convert to UInt for reading
        RegWriteFn((valid, data) => {
          when(valid) {
            reg := data.asSInt  // Convert back to SInt for writing
          }
          true.B
        })
      )
    }

    // registers for the tx
    val tx0Reg = RegInit(0.S(5.W))
    val tx1Reg = RegInit(0.S(5.W))
    val tx2Reg = RegInit(0.S(5.W))
    val tx3Reg = RegInit(0.S(5.W))
    val txValidReg = RegInit(0.U(1.W))
    val desiredSignalReg = RegInit(0.S(6.W))
    val desiredCanceledReg = RegInit(0.S(6.W))

    cancellers.io.tx0 := tx0Reg
    cancellers.io.tx1 := tx1Reg
    cancellers.io.tx2 := tx2Reg
    cancellers.io.tx3 := tx3Reg
    cancellers.io.txValid := txValidReg
    cancellers.io.desired := desiredSignalReg
    desiredCanceledReg := cancellers.io.desiredCancelled

    regmap(
      0x00 -> Seq(RegFieldSInt(5, tx0Reg)),
      0x01 -> Seq(RegFieldSInt(5, tx1Reg)),
      0x02 -> Seq(RegFieldSInt(5, tx2Reg)),
      0x03 -> Seq(RegFieldSInt(5, tx3Reg)),
      0x04 -> Seq(RegField(1, txValidReg)), // this one's fine as UInt
      0x08 -> Seq(RegFieldSInt(6, desiredSignalReg)),
      0x10 -> Seq(RegField.r(6, desiredCanceledReg.asUInt)) // read-only
    )
}

class RxCancellersTL(params: RxCancellersParams, beatBytes: Int)(implicit p: Parameters)
  extends TLRegisterRouter(
    params.address, "cancellers", Seq("eecs251b,cancellers"),
    beatBytes = beatBytes)(
      new TLRegBundle(params, _) with RxCancellerTopIO)(
      new TLRegModule(params, _, _) with CancellersTop)

case class RxCancellersParams(address: BigInt = 0x5000)

case object RxCancellersKey extends Field[Option[RxCancellersParams]](None)

trait CanHavePeripheryRxCancellers { this: BaseSubsystem =>
  private val portName = "cancellers"
  val rxcancellers = p(RxCancellersKey) match {
    case Some(params) => {
      val mod = LazyModule(new RxCancellersTL(params, pbus.beatBytes)(p))
      pbus.coupleTo(portName) { mod.node := TLFragmenter(pbus.beatBytes, pbus.blockBytes) := _ }
      Some(mod)
    }
    case None => None
  }
}

trait CanHavePeripheryRxCancellersImp extends LazyModuleImp {
  val outer: CanHavePeripheryRxCancellers
}

class WithRxCancellers(params: RxCancellersParams) extends Config((site, here, up) => {
  case RxCancellersKey => Some(params)
})
    
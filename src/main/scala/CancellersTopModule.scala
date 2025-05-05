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

class CancellersTopModule(val echoTapCount: Int, val nextTapCount: Int, val segSizeEcho: Int, val segSizeNext: Int, val echoGammaFactor: Int, val echoMuFactor: Int, val nextGammaFactor: Int, val nextMuFactor: Int) extends Module {
    val io = IO(new Bundle {
      val tx0 = Input(SInt(3.W)) // echo
      val tx1 = Input(SInt(3.W)) // next 1
      val tx2 = Input(SInt(3.W)) // next 2
      val tx3 = Input(SInt(3.W)) // next3
      val txValid = Input(Bool())

      // val doutValid = Output(Bool()) 
      val desired   = Input(SInt(8.W)) // RX signal
      val desiredCancelled = Output(SInt(8.W)) // Cancelled RX signal
    })

      val desiredReg = RegInit(0.S(8.W))
      desiredReg := io.desired
      val tx0Reg = RegInit(0.S(3.W))
      val tx1Reg = RegInit(0.S(3.W))
      val tx2Reg = RegInit(0.S(3.W))
      val tx3Reg = RegInit(0.S(3.W))
      tx0Reg := io.tx0
      tx1Reg := io.tx1
      tx2Reg := io.tx2
      tx3Reg := io.tx3
      val validReg = RegInit(false.B)
      validReg := io.txValid

    // Instantiate three NEXT cancellers and one echo canceller
    val echoCanceller = Module(new HybridAdaptiveFIRFilter(echoTapCount, segSizeEcho, echoGammaFactor, echoMuFactor))
    val nextCanceller1 = Module(new HybridAdaptiveFIRFilter(nextTapCount, segSizeNext, nextGammaFactor, nextMuFactor))
    val nextCanceller2 = Module(new HybridAdaptiveFIRFilter(nextTapCount, segSizeNext, nextGammaFactor, nextMuFactor))
    val nextCanceller3 = Module(new HybridAdaptiveFIRFilter(nextTapCount, segSizeNext, nextGammaFactor, nextMuFactor))
    val desiredDelayed = RegInit(0.S(8.W))
    
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

    // val echoShift = WireInit(0.S(5.W))
    // val nextCanceller1Shift = WireInit(0.S(5.W))
    // val nextCanceller2Shift = WireInit(0.S(5.W))
    // val nextCanceller3Shift = WireInit(0.S(5.W))
    // val firOutput = WireInit(0.S(8.W))
    val echoShift = echoCanceller.io.dout >> 10
    val nextCanceller1Shift = nextCanceller1.io.dout >> 10
    val nextCanceller2Shift = nextCanceller2.io.dout >> 10
    val nextCanceller3Shift = nextCanceller3.io.dout >> 10
    val firOutput = echoShift + nextCanceller1Shift + nextCanceller2Shift + nextCanceller3Shift

    // echoShift := echoCanceller.io.dout >> 10
    // nextCanceller1Shift := nextCanceller1.io.dout >> 10
    // nextCanceller2Shift := nextCanceller2.io.dout >> 10
    // nextCanceller3Shift := nextCanceller3.io.dout >> 10
    // firOutput := echoShift + nextCanceller1Shift + nextCanceller2Shift + nextCanceller3Shift

    desiredDelayed := io.desired

    val error = desiredDelayed - firOutput

    echoCanceller.io.error := error
    nextCanceller1.io.error := error
    nextCanceller2.io.error := error
    nextCanceller3.io.error := error

    io.desiredCancelled := error
}

trait CancellersTop extends HasRegMap {
    val io: RxCancellerTopIO
    val cancellers = Module(new CancellersTopModule(12, 12, 4, 4, 0, 1, 0, 1))

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
    val tx0Reg = RegInit(0.S(3.W))
    val tx1Reg = RegInit(0.S(3.W))
    val tx2Reg = RegInit(0.S(3.W))
    val tx3Reg = RegInit(0.S(3.W))
    val txValidReg = RegInit(0.U(1.W))
    val desiredSignalReg = RegInit(0.S(8.W))
    val desiredCanceledReg = RegInit(0.S(8.W))

    cancellers.io.tx0 := tx0Reg
    cancellers.io.tx1 := tx1Reg
    cancellers.io.tx2 := tx2Reg
    cancellers.io.tx3 := tx3Reg
    cancellers.io.txValid := txValidReg
    cancellers.io.desired := desiredSignalReg
    desiredCanceledReg := cancellers.io.desiredCancelled

    regmap(
      0x00 -> Seq(RegFieldSInt(3, tx0Reg)),
      0x01 -> Seq(RegFieldSInt(3, tx1Reg)),
      0x02 -> Seq(RegFieldSInt(3, tx2Reg)),
      0x03 -> Seq(RegFieldSInt(3, tx3Reg)),
      0x04 -> Seq(RegField(1, txValidReg)), // this one's fine as UInt
      0x08 -> Seq(RegFieldSInt(8, desiredSignalReg)),
      0x10 -> Seq(RegField.r(8, desiredCanceledReg.asUInt)) // read-only
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
    
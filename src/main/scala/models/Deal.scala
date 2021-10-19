package models

import java.util.Date

case class Deal(contractNumber: Option[String] = None, //A
                dealNumber: Option[Long] = None, //B
                dealDate: Option[Date] = None, //C
                settlementDate: Option[Date] = None, //D
                instrumentCode: Option[String] = None, //E
                instrumentType: Option[String] = None, //F
                marketType: Option[String] = None,//G
                transaction: Option[String] = None,//H
                quantity: Option[Long] = None,//I
                price: Option[Double] = None,//J
                accumulatedCouponYield: Option[Double] = None,//K
                volume: Option[Double] = None,//L
                currency: Option[String] = None,//M
                rate: Option[Double] = None,//N
                systemCommission: Option[Double] = None,//O
                bankCommission: Option[Double] = None,//P
                transactionAmount: Option[Double] = None,//Q
                dealType: Option[Double] = None//R
               )

object Deal{
  def apply(): Deal = new Deal(None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None)
}

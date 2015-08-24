package models

import anorm._

case class CustomerOrderItem( customerOrderId: Option[Long], itemId: Long, quantity: Long, unitPrice: BigDecimal )

object CustomerOrderItem {

}
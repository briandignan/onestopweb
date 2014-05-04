package models

import anorm._

case class CustomerOrderItem( customerOrderId: Pk[Long], itemId: Long, quantity: Long, unitPrice: BigDecimal )

object CustomerOrderItem {

}
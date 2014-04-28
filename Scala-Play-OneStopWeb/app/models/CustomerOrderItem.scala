package models

import anorm._

case class CustomerOrderItem( customerOrderId: Pk[Long], itemId: Long, quantity: Int, unitPrice: BigDecimal )

object CustomerOrderItem {

}
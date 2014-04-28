package models

import java.util.Date
import anorm._

case class CustomerOrderSummary( customerOrderId: Pk[Long], customer: Option[Customer], dateTime: Date, quantityItems: Int, totalSalesPrice: BigDecimal )

object CustomerOrderSummary {


	def list(): Seq[CustomerOrderSummary] = {
		val orders = CustomerOrder.list
		orders.map{ 
			case CustomerOrder( customerOrderId, customerId, dateTime, items ) => {
				val customer = customerId map { id => // TODO make this more efficient. It will have to do a ton of individual queries. 
					Customer.findById(id).get  // TODO Fix the issue where a deleted customer would cause the order retrieval to fail
				}
				val quantityItems = items.foldLeft(0)( (acc, item) => (acc + item.quantity) )
				val totalSalesPrice = items.foldLeft(BigDecimal(0,0))( (acc, item) => (acc + (item.unitPrice * item.quantity) ) )
				CustomerOrderSummary( customerOrderId, customer, dateTime, quantityItems, totalSalesPrice )
			}
		}
	}
}
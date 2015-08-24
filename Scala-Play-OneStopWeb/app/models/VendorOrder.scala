package models

import java.util.Date
import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._

import scala.language.postfixOps

case class VendorOrder(dateOrdered: Date, dateReceived: Option[Date], emailInfo: Option[EmailInfo], items: Seq[VendorPurchaseItem])

object VendorOrder {
	
	def add( vendorId: Long, order: VendorOrder ) = {
		DB.withConnection { implicit connection =>
			val vendorOrderId = SQL(
				"""
					INSERT INTO VendorOrders
					(VendorID, DateOrdered, DateDelivered)
					VALUES
					({vendorId}, {dateOrdered}, {dateDelivered})
				"""
			).on(
				'vendorId -> vendorId,
				'dateOrdered -> order.dateOrdered,
				'dateDelivered -> order.dateReceived
			).executeInsert( scalar[Long] single )
			
			order.items.foreach( item =>
				SQL(
					"""
						INSERT INTO VendorOrderItems
						(VendorOrderID, ItemID, QuantityOrdered, UnitCost)
						VALUES
						({vendorOrderId}, {itemId}, {quantityOrdered}, {unitCost})
					"""
				).on( 
					'vendorOrderId -> vendorOrderId,
					'itemId -> item.itemId,
					'quantityOrdered -> item.quantity,
					'unitCost -> item.unitCost.toString
				).executeUpdate()
			)
			
			
			// If the delivered date isn't None, then increase the inventory count appropriately
			order.dateReceived.map{ dateReceived => 
				// The date received is populated. Increase the inventory count
				order.items.foreach( item =>
					Item.increaseCount(item.itemId, item.quantity)						
				)
			}
		}
	}
	
	val vendorOrderParser = {
		get[Date]( "VendorOrders.DateOrdered" ) ~
		get[Option[Date]]( "VendorOrders.DateDelivered" ) map {
			case dateOrdered~dateDelivered => ( dateOrdered, dateDelivered )
		}
	}
	
	def findById( orderId: Long ): Option[VendorOrder] = {
		DB.withConnection { implicit connection =>
			val vendorOrderResult = SQL(
				"""
					SELECT DateOrdered, DateDelivered FROM VendorOrders
					WHERE VendorOrderID={orderId}
				"""
			).on( 
				'orderId -> orderId
			).as( vendorOrderParser.singleOpt )
			
			vendorOrderResult.map { case ( dateOrdered, dateDelivered ) =>
				// We found the order. Find the items within the order. 
				val items = VendorPurchaseItem.findById( orderId )
				
				VendorOrder( dateOrdered, dateDelivered, None, items )
			}
		}
	}
	
	
	def update( orderId: Long, order: VendorOrder ) = {
		// The items are ignored intentionally, because they can't be updated in the view
		DB.withConnection { implicit connection =>
			val previousOrder = findById( orderId ).get
			if ( previousOrder.dateReceived == None && order.dateReceived != None ) {
				// We've updated the received date, which means the inventory should also be updated. 
				order.items.foreach { item =>
					Item.increaseCount( item.itemId, item.quantity )
				}
			}

			SQL(
				"""
					UPDATE VendorOrders
					SET DateOrdered={dateOrdered}, DateDelivered={dateDelivered}
					WHERE VendorOrderID={orderId}
				"""
			).on(
				'orderId -> orderId,
				'dateOrdered -> order.dateOrdered,
				'dateDelivered -> order.dateReceived
			).executeUpdate
		}
	}
	
}